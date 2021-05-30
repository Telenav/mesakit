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

import com.telenav.tdk.core.collections.set.BoundedSet;
import com.telenav.tdk.core.configuration.ConfigurationStore;
import com.telenav.tdk.core.filesystem.*;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.object.Source;
import com.telenav.tdk.core.kernel.language.string.Strings;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.operation.progress.ProgressReporter;
import com.telenav.tdk.core.kernel.scalars.bytes.Bytes;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.core.resource.ResourceList;
import com.telenav.tdk.core.resource.path.Extension;
import com.telenav.tdk.core.utilities.reference.virtual.*;
import com.telenav.tdk.data.formats.pbf.processing.PbfDataSource;
import com.telenav.tdk.data.formats.pbf.processing.filters.osm.OsmNavigableWayFilter;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.project.TdkGraphCore;
import com.telenav.tdk.graph.traffic.historical.SpeedPatternResource;
import com.telenav.tdk.graph.world.*;
import com.telenav.tdk.graph.world.WorldGraph.AccessMode;
import com.telenav.tdk.graph.world.grid.WorldCell.DataType;
import com.telenav.tdk.graph.world.repository.*;
import com.telenav.tdk.graph.world.repository.WorldGraphRepositoryFolder.Check;
import com.telenav.tdk.map.cutter.PbfRegionCutter;
import com.telenav.tdk.map.geography.*;
import com.telenav.tdk.map.geography.polyline.Polygon;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.region.*;
import com.telenav.tdk.map.utilities.geojson.*;
import com.telenav.tdk.map.utilities.grid.*;

import java.util.*;

import static com.telenav.tdk.core.kernel.validation.Validate.*;

/**
 * A grid of {@link WorldCell}s, each containing its own cell-{@link Graph}. The grid is stored in a {@link
 * WorldGraphRepository} inside a {@link WorldGraphRepositoryFolder} and it consists of a {@link WorldGraphIndex} stored
 * in a file called "index.world" and a set of cell graphs, each named according to its index in the world grid. For
 * example, the graph file for the cell at grid coordinate (60, 36) would be called "cell-60-36.graph".
 * <p>
 * <b>Attributes</b>
 * <ul>
 *     <li>{@link #worldGraph()} - The world graph that owns this grid</li>
 *     <li>{@link #name()} - The name of this grid from the repository folder</li>
 *     <li>{@link #mode()} - The {@link AccessMode} for this grid</li>
 *     <li>{@link #metadata()} - Metadata describing the entire grid</li>
 *     <li>{@link #grid()} - The grid layout for this grid</li>
 * </ul>
 * <p>
 * <b>Repositories</b>
 * <ul>
 *     <li>{@link #repositoryFolder()} - The repository folder holding this grid</li>
 * </ul>
 * <p>
 * <b>Cells</b>
 * <ul>
 *     <li>{@link #cells()} - All cells in this grid</li>
 *     <li>{@link #worldCell(WorldCell.WorldCellIdentifier)} - The cell in this grid with the given identifier</li>
 *     <li>{@link #worldCell(int)} - The cell for the identifier</li>
 *     <li>{@link #worldCell(String)} - The cell with the given name</li>
 *     <li>{@link #worldCell(GridCell)} - The cell for the given grid cell</li>
 *     <li>{@link #worldCell(Location)}  - The cell containing the given location</li>
 *     <li>{@link #neighbors(Location)} - Cells within one meter of the given location</li>
 *     <li>{@link #included()} - The world cells included in this grid</li>
 *     <li>{@link #included(WorldCell)} - True if the given cell is in this grid</li>
 * </ul>
 * <p>
 * <b>Indexing</b>
 * <ul>
 *     <li>{@link #index()} - The {@link WorldGraphIndex} for this grid</li>
 *     <li>{@link #saveIndex(WorldGraphRepositoryFolder, Metadata)} - Saves the index for this grid to the given repository folder with the given metadata</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see WorldCell
 * @see WorldCellList
 * @see Grid
 * @see GridCell
 * @see Graph
 * @see Metadata
 * @see WorldGraph
 * @see WorldGraphIndex
 * @see WorldGraphRepositoryFolder
 * @see WorldGraphRepository
 */
@SuppressWarnings({ "rawtypes" })
public class WorldGrid
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    static
    {
        Region.register(WorldCell.class, new RegionType<>(WorldCell.class)
                .withName("WorldCell")
                .withMinimumIdentifier(Region.WORLD_CELL_IDENTIFIER_MINIMUM)
                .withMaximumIdentifier(Region.WORLD_CELL_IDENTIFIER_MAXIMUM));
    }

    /** The logical world grid */
    private Grid grid;

    /** The repository folder holding cell data for this grid */
    private WorldGraphRepositoryFolder repositoryFolder;

    /** WorldCells for each cell included in the grid */
    private WorldCell[][] cells;

    /** The set of all world cells included */
    private BoundedSet<WorldCell> included;

    /** The world graph */
    private WorldGraph worldGraph;

    /** The grid mode */
    private AccessMode mode;

    /** Graph reference tracker for cells belonging to the grid */
    private VirtualReferenceTracker<Graph> tracker;

    /** Approximate maximum amount of memory to hard reference */
    private final Bytes maximumMemory = Bytes.gigabytes(26);

    /** Graph meta data */
    private Metadata metadata;

    /** The index for this graph */
    private WorldGraphIndex index;

    public WorldGrid(final WorldGraph worldGraph, final AccessMode mode, final WorldGraphRepositoryFolder folder)
    {
        assert mode != null;
        assert worldGraph != null;

        this.worldGraph = worldGraph;
        this.mode = mode;

        initialize(mode, folder);
    }

    protected WorldGrid()
    {
    }

    public Iterable<WorldCell> cells()
    {
        final List<WorldCell> cells = new ArrayList<>();
        for (final var gridCell : grid.cells())
        {
            cells.add(worldCell(gridCell));
        }
        return cells;
    }

    /**
     * @return List of world cells in the given grid folder with the given data
     */
    public WorldCellList cells(final WorldGraphRepositoryFolder repositoryFolder, final DataType data)
    {
        return cells(repositoryFolder, data, Rectangle.MAXIMUM);
    }

    /**
     * @return List of world cells intersecting the given bounds in the given grid folder with the given data
     */
    public WorldCellList cells(final WorldGraphRepositoryFolder repositoryFolder, final DataType data, final Rectangle bounds)
    {
        final var cells = new WorldCellList();
        for (final var gridCell : grid.cellsIntersecting(bounds))
        {
            final var worldCell = worldCell(gridCell);
            if (worldCell.hasData(repositoryFolder, data))
            {
                cells.add(worldCell);
            }
        }
        return cells;
    }

    /**
     * Extracts the given PBF file into the given grid folder and converts each cell's PBF file into a graph file
     *
     * @param data The PBF data to extract and convert
     * @param repositoryFolder The folder to extract to
     * @return The number of cells for which graph data was successfully extracted
     */
    public Count extract(final WorldGraphRepositoryFolder repositoryFolder, final Source<PbfDataSource> data, final File speedPattern)
    {
        // Start time
        final var start = Time.now();

        // Clear our data folder before writing to it
        LOGGER.information(Strings.textBox("Extracting", "input: $\noutput: $", data.get().resource(), repositoryFolder));
        repositoryFolder.clear();

        // Extract regions from PBF file
        final var extracted = extractCells(data, repositoryFolder);
        if (extracted != null)
        {
            if (speedPattern != null)
            {
                duplicateSpeedPattern(extracted, speedPattern, repositoryFolder);
            }

            refreshCellData(repositoryFolder);
            LOGGER.information(Strings.box("Extracted $ cells to $ in $", extracted.count(), repositoryFolder, start.elapsedSince()));
            return extracted.count();
        }
        return Count._0;
    }

    public Grid grid()
    {
        return grid;
    }

    /**
     * @return The world cells included in the current set of regions
     */
    public BoundedSet<WorldCell> included()
    {
        return included;
    }

    /**
     * @return True if the cell is included in the current set of regions
     */
    public boolean included(final WorldCell worldCell)
    {
        return included().contains(worldCell);
    }

    /**
     * @return The loaded {@link WorldGraphIndex} for this folder or null if there is no index
     */
    public WorldGraphIndex index()
    {
        if (index == null)
        {
            switch (mode())
            {
                case READ:
                    index = WorldGraphIndex.load(repositoryFolder().indexFile());
                    break;

                case CREATE:
                    index = WorldGraphIndex.create();
                    break;
            }
        }
        return index;
    }

    public Bytes maximumMemory()
    {
        return maximumMemory;
    }

    public Metadata metadata()
    {
        return metadata;
    }

    public AccessMode mode()
    {
        return mode;
    }

    public String name()
    {
        return repositoryFolder().name().name();
    }

    /**
     * @return The cells near the given location having graphs
     */
    public WorldCellList neighbors(final Location location)
    {
        return cellsNear(repositoryFolder(), location);
    }

    /**
     * Dumps included cells to the user's desktop
     */
    public void outputCellsAsGeoJson()
    {
        final var output = new GeoJsonDocument();
        for (final var worldCell : included())
        {
            final var feature = new GeoJsonFeature(worldCell.toString());
            feature.title(worldCell.identity().tdk().code());
            feature.add(new GeoJsonPolyline(worldCell.bounds().asPolyline()));
            output.add(feature);
        }
        output.save(new File("data/world-graph-2-degree-cells.geojson"));
    }

    /**
     * @return The repository folder
     */
    public WorldGraphRepositoryFolder repositoryFolder()
    {
        return repositoryFolder;
    }

    public void saveIndex(final WorldGraphRepositoryFolder repositoryFolder, final Metadata metadata)
    {
        index().save(repositoryFolder.indexFile(), metadata);
    }

    public WorldCell worldCell(final WorldCell.WorldCellIdentifier identifier)
    {
        return worldCell(identifier.gridCell());
    }

    /**
     * @return The world cell for the given grid cell
     */
    public WorldCell worldCell(final GridCell gridCell)
    {
        final var latitudeIndex = gridCell.identifier().latitudeIndex();
        final var longitudeIndex = gridCell.identifier().longitudeIndex();
        var worldCell = cells[latitudeIndex][longitudeIndex];
        if (worldCell == null)
        {
            worldCell = new WorldCell(this, gridCell);
            cells[latitudeIndex][longitudeIndex] = worldCell;
        }
        return worldCell;
    }

    public WorldCell worldCell(final int identifier)
    {
        return worldCell(grid.cellForIdentifier(new GridCellIdentifier(grid, identifier)));
    }

    /**
     * @return The world cell for the given location
     */
    public WorldCell worldCell(final Location location)
    {
        assert location != null;
        return worldCell(grid.cell(location));
    }

    public WorldCell worldCell(final String cellName)
    {
        // Break "cell-12-34" into words "cell", "12", "34"
        final var words = cellName.split("-");

        // If a valid cell name was specified
        if ("cell".equalsIgnoreCase(words[0]) && Strings.isNaturalNumber(words[1]) && Strings.isNaturalNumber(words[2]))
        {
            // look the cell up
            final var gridCell = grid.cellForIdentifier(
                    new GridCellIdentifier(grid, Integer.parseInt(words[1]), Integer.parseInt(words[2])));

            // and if we found it
            if (gridCell != null)
            {
                // return it
                return worldCell(gridCell);
            }
        }
        return null;
    }

    public WorldGraph worldGraph()
    {
        return worldGraph;
    }

    VirtualReferenceTracker<Graph> tracker()
    {
        if (tracker == null)
        {
            tracker = new VirtualReferenceTracker<>(maximumMemory, VirtualReferenceType.WEAK);
        }
        return tracker;
    }

    /**
     * @return List of world cells near the given location in the given grid folder with the given data
     */
    private WorldCellList cellsNear(final WorldGraphRepositoryFolder repositoryFolder, final Location location)
    {
        return cells(repositoryFolder, DataType.GRAPH, location.bounds().expanded(Distance.meters(1)));
    }

    private WorldGraphConfiguration configuration()
    {
        return ConfigurationStore.global().require(WorldGraphConfiguration.class);
    }

    private void createGrid()
    {
        // Create grid object
        grid = new Grid(configuration().cellSize(), Latitude.OSM_MAXIMUM);

        // Create 2D array of cells
        final var latitudeCells = grid.latitudeCellCount().asInt();
        final var longitudeCells = grid.longitudeCellCount().asInt();
        cells = new WorldCell[latitudeCells][longitudeCells];

        // Get set of cells that are geographically included
        included = findIncludedCells();
    }

    private void duplicateSpeedPattern(final ResourceList cells, final File speedPattern, final WorldGraphRepositoryFolder repositoryFolder)
    {
        ensure(speedPattern.exists());

        for (final var resource : cells)
        {
            if (resource.fileName().endsWith(Extension.OSM_PBF))
            {
                speedPattern.copyTo(repositoryFolder.file(resource.baseName().withExtension(SpeedPatternResource.EXTENSION)),
                        ProgressReporter.NULL);
            }
        }
    }

    /**
     * @return The PBF files extracted from the given pbf file into the given world grid folder
     */
    private ResourceList extractCells(final Source<PbfDataSource> data, final WorldGraphRepositoryFolder repositoryFolder)
    {
        // Extract grid cells into the grid folder
        final var cutter = new PbfRegionCutter(data, repositoryFolder, new OsmNavigableWayFilter())
        {
            @Override
            public List<Region> regionsForLocation(final Location location)
            {
                // Since grid cells do not overlap, there is only one cell region for the given location
                return Collections.singletonList(worldCell(location));
            }
        };

        // Configure extractor and extract
        cutter.regionsToExtract(new RegionSet(included()));
        return cutter.extract();
    }

    /**
     * @return A set of cells that are included by the {@link #includedRegions()}
     */
    private BoundedSet<WorldCell> findIncludedCells()
    {
        // Go through cells in included regions
        final var included = new BoundedSet<WorldCell>(Count._10_000);
        for (final var region : includedRegions())
        {
            final var cached = regionCache().file(region.fileName()
                    .withSuffix("-" + Math.round(grid.approximateCellSize().asDegrees()) + "-degree-grid")
                    .withExtension(new Extension(".cells")));
            if (cached.exists())
            {
                final var cellNames = cached.reader().string();
                if (!Strings.isEmpty(cellNames))
                {
                    for (final var cellName : cellNames.split(","))
                    {
                        final var worldCell = worldCell(cellName);
                        if (worldCell != null)
                        {
                            included.add(worldCell);
                        }
                        else
                        {
                            LOGGER.warning("Unable to find cell " + cellName);
                        }
                    }
                }
            }
            else
            {
                // and for each polygon of the region
                final List<WorldCell> cells = new ArrayList<>();
                @SuppressWarnings("unchecked") final Collection<Polygon> polygons = region.borders();
                for (final var polygon : polygons)
                {
                    // find all the cells that might intersect with it
                    for (final var gridCell : grid.cellsIntersecting(polygon.bounds()))
                    {
                        // and if the cell does intersect
                        if (polygon.intersectsOrContains(gridCell.bounds().asPolyline())
                                || gridCell.asPolygon().intersectsOrContains(polygon))
                        {
                            // then it is included
                            cells.add(worldCell(gridCell));
                        }
                    }
                }
                included.addAll(cells);
                if (cells.isEmpty())
                {
                    LOGGER.warning("No cells found for " + region);
                }
                cached.print(toString(cells));
            }
        }
        return included;
    }

    /**
     * @return The regions we want data for
     */
    private RegionSet includedRegions()
    {
        final var regions = new RegionSet();
        regions.addAll(Continent.NORTH_AMERICA.children());
        regions.addAll(Continent.EUROPE.children());
        regions.addAll(Continent.SOUTH_AMERICA.children());
        regions.addAll(Continent.AFRICA.children());
        regions.addAll(Continent.ASIA.children());
        regions.addAll(Continent.OCEANIA.children());
        return regions;
    }

    private void initialize(final AccessMode mode, final WorldGraphRepositoryFolder folder)
    {
        // Save the grid mode
        this.mode = mode;

        switch (mode)
        {
            case READ:
            {
                ensureNotNull(folder, "Required repository folder is missing");
                folder.ensure(Check.EXISTS);

                repositoryFolder = folder;
                metadata = index().metadata();
            }
            break;

            case WRITE:
            case CREATE:
                repositoryFolder = folder;
                break;

            default:
                fail("Unknown mode $", mode);
        }

        // Create grid
        DEBUG.trace("[WorldGrid mode = $, folder = $]", mode, folder);
        DEBUG.trace("Grid configuration is $", configuration());
        createGrid();

        // Make sure repository folder exists
        repositoryFolder().mkdirs();

        // If we're reading data,
        if (this.mode == AccessMode.READ)
        {
            // refresh cell data
            refreshCellData(repositoryFolder());
            DEBUG.trace("Cells with graphs: $", cells(repositoryFolder(), DataType.GRAPH));
        }
    }

    /**
     * @param repositoryFolder The repository folder for which cell data should be refreshed from the filesystem
     */
    private void refreshCellData(final WorldGraphRepositoryFolder repositoryFolder)
    {
        // Update status of PBF files
        final var pbfs = repositoryFolder.files(Extension.OSM_PBF).asSet();
        for (final var worldCell : included)
        {
            worldCell.hasPbf(repositoryFolder, pbfs.contains(worldCell.pbfFile(repositoryFolder)));
        }

        // Update status and size of graph files
        final var graphs = repositoryFolder.files(Extension.GRAPH).asSet();
        for (final var worldCell : included)
        {
            final var graphFile = worldCell.cellGraphFile(repositoryFolder);
            worldCell.hasGraph(repositoryFolder, graphs.contains(graphFile));
            for (final var file : graphs)
            {
                if (file.equals(graphFile))
                {
                    worldCell.fileSize(file.size());
                }
            }
        }
    }

    private Folder regionCache()
    {
        return TdkGraphCore.get().graphFolder().folder("world-graph/regions").mkdirs();
    }

    private String toString(final List<WorldCell> cells)
    {
        final var builder = new StringBuilder();
        for (final var worldCell : cells)
        {
            if (builder.length() > 0)
            {
                builder.append(',');
            }
            builder.append(worldCell.name());
        }
        return builder.toString();
    }
}
