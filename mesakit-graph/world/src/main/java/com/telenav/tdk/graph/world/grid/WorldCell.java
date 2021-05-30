////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.tdk.graph.world.grid;

import com.telenav.tdk.core.filesystem.*;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.persistence.Unloadable;
import com.telenav.tdk.core.kernel.language.collections.set.Sets;
import com.telenav.tdk.core.kernel.language.vm.JavaVirtualMachine.TdkExcludeFromSizeOf;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.operation.progress.ProgressReporter;
import com.telenav.tdk.core.kernel.scalars.bytes.Bytes;
import com.telenav.tdk.core.resource.path.Extension;
import com.telenav.tdk.core.utilities.reference.virtual.VirtualReference;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.io.archive.GraphArchive;
import com.telenav.tdk.graph.traffic.historical.SpeedPatternResource;
import com.telenav.tdk.graph.world.*;
import com.telenav.tdk.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.geography.polyline.Polygon;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.region.*;
import com.telenav.tdk.map.utilities.grid.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.telenav.tdk.core.resource.compression.archive.ZipArchive.Mode.READ;

/**
 * A world cell is a square {@link Region} in a {@link WorldGrid}. The actual layout of this cell in a grid is handled
 * by {@link GridCell} which is a rectangle in a {@link Grid}. These classes are found in a separate utility package in
 * the tdk-map project.
 * <p>
 * <b>Identifiers</b>
 * <p>
 * A {@link WorldCell} has a reference to the world grid that owns it which can be retrieved with {@link #worldGrid()}.
 * It also has a convenience method to access the {@link WorldGraph} that owns the world grid. The cell can be
 * identified by a {@link WorldCellIdentifier} retrieved from {@link #worldCellIdentifier()}.
 * <p>
 * <b>Graph Data</b>
 * <p>
 * The data for a world cell is contained in a {@link Folder} accessed with {@link #folder()} and the specific data for
 * the cell can be accessed with {@link #cellGraphFile()} and {@link #cellGraph()}. The size of data in byes is
 * available with {@link #fileSize()} and t estimated size in memory with {@link #memorySize()}.
 * <p>
 * <b>Neighboring Cells</b>
 * <p>
 * World cells that are next to this world cell can be retrieved with {@link #neighbors()} and if this cell should
 * itself be included, with {@link #neighborsAndThis()}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldGrid
 * @see WorldCellList
 * @see WorldGraphRepositoryFolder
 * @see GridCell
 */
@TdkExcludeFromSizeOf
public class WorldCell extends Region<WorldCell> implements Unloadable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Cell data types
     */
    public enum DataType
    {
        /** The cell is included and has a graph */
        GRAPH,

        /** The cell is included and has PBF data */
        PBF
    }

    /**
     * WorldCellIdentifier of a world cell in a world grid
     */
    public static class WorldCellIdentifier extends RegionIdentifier
    {
        /** The grid that this world cell identifier is a part of */
        private final WorldGrid grid;

        /** The underlying {@link GridCell} */
        private final GridCell gridCell;

        public WorldCellIdentifier(final WorldGrid grid, final GridCell gridCell)
        {
            super(gridCell.identifier().identifier());
            this.grid = grid;
            this.gridCell = gridCell;
        }

        public GridCell gridCell()
        {
            return gridCell;
        }

        public Region<?> region()
        {
            return grid.worldCell(this);
        }
    }

    /** The world grid */
    private WorldGrid worldGrid;

    /** The cell in the grid */
    private GridCell gridCell;

    /** Cached hash code for cell */
    private int hashCode;

    /** Any soft-reference to a graph */
    private VirtualReference<Graph> cellGraphReference;

    /** The size of the graph file for this world cell */
    private Bytes fileSize;

    /** Graph data presence for each repository folder */
    private final Map<WorldGraphRepositoryFolder, Boolean> hasGraph = new ConcurrentHashMap<>();

    /** PBF data presence for each repository folder */
    private final Map<WorldGraphRepositoryFolder, Boolean> hasPbf = new ConcurrentHashMap<>();

    /**
     * @param worldGrid The parent grid
     * @param gridCell The logical cell
     */
    public WorldCell(final WorldGrid worldGrid, final GridCell gridCell)
    {
        super(null, new RegionInstance<>(WorldCell.class)
                .withIdentity(new RegionIdentity("cell-" + gridCell.identifier().latitudeIndex() + "-" + gridCell.identifier().longitudeIndex())
                        .withIdentifier(new WorldCellIdentifier(worldGrid, gridCell))));
        this.worldGrid = worldGrid;
        this.gridCell = gridCell;
        hashCode = gridCell.hashCode();
        initialize();
    }

    protected WorldCell()
    {
    }

    public Iterable<Edge> asWorldEdges(final Iterable<Edge> edges)
    {
        final List<Edge> worldEdges = new ArrayList<>();
        for (final var edge : edges)
        {
            worldEdges.add(new WorldEdge(this, edge));
        }
        return worldEdges;
    }

    @Override
    public Collection<Polygon> borders()
    {
        return Sets.of(bounds().asPolygon());
    }

    @Override
    public Rectangle bounds()
    {
        return gridCell.bounds();
    }

    /**
     * @return The graph for this cell
     */
    public Graph cellGraph()
    {
        final var graph = cellGraphReference().get();
        if (graph == null)
        {
            worldGrid().worldGraph().newCellGraph();
        }
        return graph;
    }

    /**
     * @return The graph file for this cell
     */
    public File cellGraphFile()
    {
        return cellGraphFile(worldGrid.repositoryFolder());
    }

    /**
     * @return The graph file in the given grid folder
     */
    public File cellGraphFile(final WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder.file(fileName().withExtension(Extension.GRAPH));
    }

    @Override
    public Containment containment(final Location location)
    {
        return bounds().containment(location);
    }

    @Override
    public boolean contains(final Location location)
    {
        return gridCell.contains(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof WorldCell)
        {
            final var that = (WorldCell) object;
            return this == that || gridCell.equals(that.gridCell);
        }
        return false;
    }

    public Bytes fileSize()
    {
        if (fileSize == null)
        {
            fileSize = cellGraphFile().size();
        }
        return fileSize;
    }

    public void fileSize(final Bytes fileSize)
    {
        this.fileSize = fileSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder folder()
    {
        return null;
    }

    /**
     * @return The grid cell
     */
    public GridCell gridCell()
    {
        return gridCell;
    }

    /**
     * @return True if the given grid folder has the given data
     */
    public boolean hasData(final WorldGraphRepositoryFolder repositoryFolder, final DataType data)
    {
        switch (data)
        {
            case GRAPH:
                return hasGraphFile(repositoryFolder);

            case PBF:
                return hasPbfFile(repositoryFolder);
        }
        return false;
    }

    /**
     * Updates graph data presence for the this cell in the given grid folder
     */
    public void hasGraph(final WorldGraphRepositoryFolder repositoryFolder, final boolean has)
    {
        hasGraph.put(repositoryFolder, has);
    }

    /**
     * @return True if there is a graph file for this cell in the given grid folder
     */
    public boolean hasGraphFile(final WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder != null
                && hasGraph.computeIfAbsent(repositoryFolder, folder -> isIncluded() && cellGraphFile(folder).exists());
    }

    /**
     * Updates PBF data presence for the this cell in the given grid folder
     */
    public void hasPbf(final WorldGraphRepositoryFolder repositoryFolder, final boolean has)
    {
        hasPbf.put(repositoryFolder, has);
    }

    /**
     * @return True if there is a PBF file for this cell in the given grid folder
     */
    public boolean hasPbfFile(final WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder != null
                && hasPbf.computeIfAbsent(repositoryFolder, folder -> isIncluded() && pbfFile(folder).exists());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * @return True if this cell is included
     */
    public boolean isIncluded()
    {
        return worldGrid.included(this);
    }

    public Bytes memorySize()
    {
        final var graph = worldGrid.worldGraph();
        return graph == null ? null : graph.estimatedMemorySize(this);
    }

    /**
     * @return The (up to) 8 cells that are neighbors of this cell (and have graph data)
     */
    public WorldCellList neighbors()
    {
        final var neighbors = neighborsAndThis();
        neighbors.remove(this);
        return neighbors;
    }

    /**
     * @return The (up to) 8 cells that are neighbors of this cell (and have graph data)
     */
    public WorldCellList neighborsAndThis()
    {
        return worldGrid.cells(worldGrid.repositoryFolder(), DataType.GRAPH, bounds().expanded(Distance.ONE_METER));
    }

    public File pbfFile()
    {
        return pbfFile(worldGrid.repositoryFolder());
    }

    /**
     * @return The PBF file in the given grid folder
     */
    public File pbfFile(final WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder.file(fileName().withExtension(Extension.OSM_PBF));
    }

    public File speedPatternFile(final WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder.file(fileName().withExtension(SpeedPatternResource.EXTENSION));
    }

    @Override
    public Class<?> subclass()
    {
        return WorldCell.class;
    }

    @Override
    public String toString()
    {
        return "[" + name() +
                (hasGraphFile(worldGrid.repositoryFolder()) ? " graph" : "") +
                (hasPbfFile(worldGrid.repositoryFolder()) ? " pbf" : "") +
                "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload()
    {
        cellGraph().unload();
    }

    public WorldCellIdentifier worldCellIdentifier()
    {
        return new WorldCellIdentifier(worldGrid(), gridCell());
    }

    public WorldGraph worldGraph()
    {
        return worldGrid().worldGraph();
    }

    public WorldGrid worldGrid()
    {
        return worldGrid;
    }

    @Override
    protected void onInitialize()
    {
        // Create a variable strength reference that installs and loads this cell's graph when
        // needed. When the reference is hard (vs soft or weak or null) is determined by the
        // logic in ReferenceTracker, which keeps the N most recently accessed references as
        // hard references and softens the remaining references.
        final var outer = this;
        cellGraphReference = new VirtualReference<>(worldGrid.tracker())
        {
            @Override
            public String name()
            {
                return outer.name() + " (" + memorySize() + ")";
            }

            @Override
            protected Bytes memorySize()
            {
                return outer.memorySize();
            }

            @Override
            protected Graph onLoad()
            {
                // Load the graph file
                DEBUG.trace("Loading graph for $", name());
                @SuppressWarnings(
                        "resource") final var archive = new GraphArchive(cellGraphFile(), ProgressReporter.NULL, READ);
                final var graph = archive.load(DEBUG.isEnabled() ? DEBUG.listener() : Listener.NULL);
                if (graph == null)
                {
                    LOGGER.warning("Unable to load graph for $", name());
                }
                return graph;
            }
        };
    }

    private VirtualReference<Graph> cellGraphReference()
    {
        return cellGraphReference;
    }
}
