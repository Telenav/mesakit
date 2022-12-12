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

package com.telenav.mesakit.graph.world.grid;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.interfaces.loading.Unloadable;
import com.telenav.kivakit.resource.Extension;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.world.WorldEdge;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.mesakit.graph.world.virtual.VirtualReference;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.utilities.grid.Grid;
import com.telenav.mesakit.map.utilities.grid.GridCell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.telenav.kivakit.resource.compression.archive.ZipArchive.AccessMode.READ;

/**
 * A world cell is a square {@link Region} in a {@link WorldGrid}. The actual layout of this cell in a grid is handled
 * by {@link GridCell} which is a rectangle in a {@link Grid}. These classes are found in a separate utility package in
 * the mesakit-map project.
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
 * the cell can be accessed with {@link #cellGraphFile()} and {@link #cellGraph()}. The size of data in bytes is
 * available with {@link #fileSize()}.
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
@SuppressWarnings("unused")
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

        public WorldCellIdentifier(WorldGrid grid, GridCell gridCell)
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
    public WorldCell(WorldGrid worldGrid, GridCell gridCell)
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

    public Iterable<Edge> asWorldEdges(Iterable<Edge> edges)
    {
        List<Edge> worldEdges = new ArrayList<>();
        for (var edge : edges)
        {
            worldEdges.add(new WorldEdge(this, edge));
        }
        return worldEdges;
    }

    @Override
    public Collection<Polygon> borders()
    {
        return Set.of(bounds().asPolygon());
    }

    @Override
    public Rectangle bounds()
    {
        return gridCell.bounds();
    }

    /**
     * Returns the graph for this cell
     */
    public Graph cellGraph()
    {
        var graph = cellGraphReference().get();
        if (graph == null)
        {
            worldGrid().worldGraph().newCellGraph();
        }
        return graph;
    }

    /**
     * Returns the graph file for this cell
     */
    public File cellGraphFile()
    {
        return cellGraphFile(worldGrid.repositoryFolder());
    }

    /**
     * Returns the graph file in the given grid folder
     */
    public File cellGraphFile(WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder.file(fileName().withExtension(Extension.GRAPH));
    }

    @Override
    public Containment containment(Location location)
    {
        return bounds().containment(location);
    }

    @Override
    public boolean contains(Location location)
    {
        return gridCell.contains(location);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof WorldCell that)
        {
            return this == that || gridCell.equals(that.gridCell);
        }
        return false;
    }

    public Bytes fileSize()
    {
        if (fileSize == null)
        {
            fileSize = cellGraphFile().sizeInBytes();
        }
        return fileSize;
    }

    public void fileSize(Bytes fileSize)
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
     * Returns the grid cell
     */
    public GridCell gridCell()
    {
        return gridCell;
    }

    /**
     * Returns true if the given grid folder has the given data
     */
    public boolean hasData(WorldGraphRepositoryFolder repositoryFolder, DataType data)
    {
        return switch (data)
                {
                    case GRAPH -> hasGraphFile(repositoryFolder);
                    case PBF -> hasPbfFile(repositoryFolder);
                };
    }

    /**
     * Updates graph data presence for the this cell in the given grid folder
     */
    public void hasGraph(WorldGraphRepositoryFolder repositoryFolder, boolean has)
    {
        hasGraph.put(repositoryFolder, has);
    }

    /**
     * Returns true if there is a graph file for this cell in the given grid folder
     */
    public boolean hasGraphFile(WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder != null
                && hasGraph.computeIfAbsent(repositoryFolder, folder -> isIncluded() && cellGraphFile(folder).exists());
    }

    /**
     * Updates PBF data presence for the this cell in the given grid folder
     */
    public void hasPbf(WorldGraphRepositoryFolder repositoryFolder, boolean has)
    {
        hasPbf.put(repositoryFolder, has);
    }

    /**
     * Returns true if there is a PBF file for this cell in the given grid folder
     */
    public boolean hasPbfFile(WorldGraphRepositoryFolder repositoryFolder)
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
     * Returns true if this cell is included
     */
    public boolean isIncluded()
    {
        return worldGrid.included(this);
    }

    /**
     * Returns the (up to) 8 cells that are neighbors of this cell (and have graph data)
     */
    public WorldCellList neighbors()
    {
        var neighbors = neighborsAndThis();
        neighbors.remove(this);
        return neighbors;
    }

    /**
     * Returns the (up to) 8 cells that are neighbors of this cell (and have graph data)
     */
    public WorldCellList neighborsAndThis()
    {
        return worldGrid.cells(worldGrid.repositoryFolder(), DataType.GRAPH, bounds().expanded(Distance.ONE_METER));
    }

    @Override
    public void onInitialize()
    {
        // Create a variable strength reference that installs and loads this cell's graph when
        // needed. When the reference is hard (vs soft or weak or null) is determined by the
        // logic in ReferenceTracker, which keeps the N most recently accessed references as
        // hard references and softens the remaining references.
        var outer = this;
        cellGraphReference = new VirtualReference<>(worldGrid.tracker())
        {
            @Override
            public String name()
            {
                return outer.name();
            }

            @Override
            protected Graph onLoad()
            {
                // Load the graph file
                DEBUG.trace("Loading graph for $", name());
                @SuppressWarnings(
                        "resource") var archive = new GraphArchive(LOGGER, cellGraphFile(), READ, ProgressReporter.nullProgressReporter());
                var graph = archive.load(DEBUG.isDebugOn() ? DEBUG.listener() : Listener.nullListener());
                if (graph == null)
                {
                    LOGGER.warning("Unable to load graph for $", name());
                }
                return graph;
            }
        };
    }

    public File pbfFile()
    {
        return pbfFile(worldGrid.repositoryFolder());
    }

    /**
     * Returns the PBF file in the given grid folder
     */
    public File pbfFile(WorldGraphRepositoryFolder repositoryFolder)
    {
        return repositoryFolder.file(fileName().withExtension(Extension.OSM_PBF));
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

    private VirtualReference<Graph> cellGraphReference()
    {
        return cellGraphReference;
    }
}
