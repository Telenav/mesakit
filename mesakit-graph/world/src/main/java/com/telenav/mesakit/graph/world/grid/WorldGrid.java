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

import com.telenav.kivakit.core.collections.set.ObjectSet;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.interfaces.value.Source;
import com.telenav.kivakit.resource.ResourceList;
import com.telenav.kivakit.settings.SettingsRegistry;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphProject;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.WorldGraphConfiguration;
import com.telenav.mesakit.graph.world.WorldGraphIndex;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepository;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.mesakit.graph.world.virtual.VirtualReferenceTracker;
import com.telenav.mesakit.graph.world.virtual.VirtualReferenceType;
import com.telenav.mesakit.map.cutter.PbfRegionCutter;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm.OsmNavigableWayFilter;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionSet;
import com.telenav.mesakit.map.region.RegionType;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.utilities.geojson.GeoJsonDocument;
import com.telenav.mesakit.map.utilities.geojson.GeoJsonFeature;
import com.telenav.mesakit.map.utilities.geojson.GeoJsonPolyline;
import com.telenav.mesakit.map.utilities.grid.Grid;
import com.telenav.mesakit.map.utilities.grid.GridCell;
import com.telenav.mesakit.map.utilities.grid.GridCellIdentifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.kivakit.core.project.Project.resolveProject;
import static com.telenav.kivakit.resource.Extension.GRAPH;
import static com.telenav.kivakit.resource.Extension.OSM_PBF;
import static com.telenav.kivakit.resource.Extension.parseExtension;

/**
 * A grid of {@link WorldCell}s, each containing its own cell-{@link Graph}. The grid is stored in a
 * {@link WorldGraphRepository} inside a {@link WorldGraphRepositoryFolder} and it consists of a {@link WorldGraphIndex}
 * stored in a file called "index.world" and a set of cell graphs, each named according to its index in the world grid.
 * For example, the graph file for the cell at grid coordinate (60, 36) would be called "cell-60-36.graph".
 * <p>
 * <b>Attributes</b>
 * <ul>
 *     <li>{@link #worldGraph()} - The world graph that owns this grid</li>
 *     <li>{@link #name()} - The name of this grid from the repository folder</li>
 *     <li>{@link #mode()} - The {@link WorldGraph.AccessMode} for this grid</li>
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
@SuppressWarnings({ "rawtypes", "unused" })
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

    /** WorldCells for each cell included in the grid */
    private WorldCell[][] cells;

    /** The logical world grid */
    private Grid grid;

    /** The set of all world cells included */
    private ObjectSet<WorldCell> included;

    /** The index for this graph */
    private WorldGraphIndex index;

    /** Approximate maximum amount of memory to hard reference */
    private final Bytes maximumMemory = Bytes.gigabytes(26);

    /** Graph meta data */
    private Metadata metadata;

    /** The grid mode */
    private WorldGraph.AccessMode mode;

    /** The repository folder holding cell data for this grid */
    private WorldGraphRepositoryFolder repositoryFolder;

    /** Graph reference tracker for cells belonging to the grid */
    private VirtualReferenceTracker<Graph> tracker;

    /** The world graph */
    private WorldGraph worldGraph;

    public WorldGrid(WorldGraph worldGraph, WorldGraph.AccessMode mode,
                     WorldGraphRepositoryFolder folder)
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
        List<WorldCell> cells = new ArrayList<>();
        for (var gridCell : grid.cells())
        {
            cells.add(worldCell(gridCell));
        }
        return cells;
    }

    /**
     * Returns list of world cells in the given grid folder with the given data
     */
    public WorldCellList cells(WorldGraphRepositoryFolder repositoryFolder, WorldCell.DataType data)
    {
        return cells(repositoryFolder, data, Rectangle.MAXIMUM);
    }

    /**
     * Returns list of world cells intersecting the given bounds in the given grid folder with the given data
     */
    public WorldCellList cells(WorldGraphRepositoryFolder repositoryFolder, WorldCell.DataType data,
                               Rectangle bounds)
    {
        var cells = new WorldCellList();
        for (var gridCell : grid.cellsIntersecting(bounds))
        {
            var worldCell = worldCell(gridCell);
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
    public Count extract(WorldGraphRepositoryFolder repositoryFolder, Source<PbfDataSource> data)
    {
        // Start time
        var start = Time.now();

        // Clear our data folder before writing to it
        LOGGER.information(AsciiArt.textBox("Extracting", "input: $\noutput: $", data.get().resource(), repositoryFolder));
        repositoryFolder.clearAll();

        // Extract regions from PBF file
        var extracted = extractCells(data, repositoryFolder);
        if (extracted != null)
        {
            refreshCellData(repositoryFolder);
            LOGGER.information(AsciiArt.box("Extracted $ cells to $ in $", extracted.count(), repositoryFolder, start.elapsedSince()));
            return extracted.count();
        }
        return Count._0;
    }

    public Grid grid()
    {
        return grid;
    }

    /**
     * Returns the world cells included in the current set of regions
     */
    public ObjectSet<WorldCell> included()
    {
        return included;
    }

    /**
     * Returns true if the cell is included in the current set of regions
     */
    public boolean included(WorldCell worldCell)
    {
        return included().contains(worldCell);
    }

    /**
     * Returns the loaded {@link WorldGraphIndex} for this folder or null if there is no index
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

    public WorldGraph.AccessMode mode()
    {
        return mode;
    }

    public String name()
    {
        return repositoryFolder().name().name();
    }

    /**
     * Returns the cells near the given location having graphs
     */
    public WorldCellList neighbors(Location location)
    {
        return cellsNear(repositoryFolder(), location);
    }

    /**
     * Dumps included cells to the user's desktop
     */
    public void outputCellsAsGeoJson()
    {
        var output = new GeoJsonDocument();
        for (var worldCell : included())
        {
            var feature = new GeoJsonFeature(worldCell.toString());
            feature.title(worldCell.identity().mesakit().code());
            feature.add(new GeoJsonPolyline(worldCell.bounds().asPolyline()));
            output.add(feature);
        }

        //noinspection SpellCheckingInspection
        output.save(File.parseFile(Listener.consoleListener(), "data/world-graph-2-degree-cells.geojson"));
    }

    /**
     * Returns the repository folder
     */
    public WorldGraphRepositoryFolder repositoryFolder()
    {
        return repositoryFolder;
    }

    public void saveIndex(WorldGraphRepositoryFolder repositoryFolder, Metadata metadata)
    {
        index().save(repositoryFolder.indexFile(), metadata);
    }

    public WorldCell worldCell(WorldCell.WorldCellIdentifier identifier)
    {
        return worldCell(identifier.gridCell());
    }

    /**
     * Returns the world cell for the given grid cell
     */
    public WorldCell worldCell(GridCell gridCell)
    {
        var latitudeIndex = gridCell.identifier().latitudeIndex();
        var longitudeIndex = gridCell.identifier().longitudeIndex();
        var worldCell = cells[latitudeIndex][longitudeIndex];
        if (worldCell == null)
        {
            worldCell = new WorldCell(this, gridCell);
            cells[latitudeIndex][longitudeIndex] = worldCell;
        }
        return worldCell;
    }

    public WorldCell worldCell(int identifier)
    {
        return worldCell(grid.cellForIdentifier(new GridCellIdentifier(grid, identifier)));
    }

    /**
     * Returns the world cell for the given location
     */
    public WorldCell worldCell(Location location)
    {
        assert location != null;
        return worldCell(grid.cell(location));
    }

    public WorldCell worldCell(String cellName)
    {
        // Break "cell-12-34" into words "cell", "12", "34"
        var words = cellName.split("-");

        // If a valid cell name was specified
        if ("cell".equalsIgnoreCase(words[0]) && Strings.isNaturalNumber(words[1]) && Strings.isNaturalNumber(words[2]))
        {
            // look the cell up
            var gridCell = grid.cellForIdentifier(
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
     * Returns list of world cells near the given location in the given grid folder with the given data
     */
    private WorldCellList cellsNear(WorldGraphRepositoryFolder repositoryFolder, Location location)
    {
        return cells(repositoryFolder, WorldCell.DataType.GRAPH, location.bounds().expanded(Distance.meters(1)));
    }

    private WorldGraphConfiguration configuration()
    {
        return SettingsRegistry.settingsFor(this).requireSettings(WorldGraphConfiguration.class);
    }

    private void createGrid()
    {
        // Create grid object
        grid = new Grid(configuration().cellSize(), Latitude.MAXIMUM);

        // Create 2D array of cells
        var latitudeCells = grid.latitudeCellCount().asInt();
        var longitudeCells = grid.longitudeCellCount().asInt();
        cells = new WorldCell[latitudeCells][longitudeCells];

        // Get set of cells that are geographically included
        included = findIncludedCells();
    }

    /**
     * Returns the PBF files extracted from the given pbf file into the given world grid folder
     */
    private ResourceList extractCells(Source<PbfDataSource> data,
                                      WorldGraphRepositoryFolder repositoryFolder)
    {
        // Extract grid cells into the grid folder
        var cutter = new PbfRegionCutter(data, repositoryFolder, new OsmNavigableWayFilter())
        {
            @Override
            public List<Region> regionsForLocation(Location location)
            {
                // Since grid cells do not overlap, there is only one cell region for the given location
                return Collections.singletonList(worldCell(location));
            }
        };

        // Configure extractor and extract
        cutter.regionsToExtract(new RegionSet(included()));
        return cutter.cut();
    }

    /**
     * Returns a set of cells that are included by the {@link #includedRegions()}
     */
    private ObjectSet<WorldCell> findIncludedCells()
    {
        // Go through cells in included regions
        var included = new ObjectSet<WorldCell>(Maximum._10_000);
        for (var region : includedRegions())
        {
            var cached = regionCache().file(region.fileName()
                    .withSuffix("-" + Math.round(grid.approximateCellSize().asDegrees()) + "-degree-grid")
                    .withExtension(parseExtension(Listener.consoleListener(), ".cells")));
            if (cached.exists())
            {
                var cellNames = cached.reader().asString();
                if (!Strings.isEmpty(cellNames))
                {
                    for (var cellName : cellNames.split(","))
                    {
                        var worldCell = worldCell(cellName);
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
                List<WorldCell> cells = new ArrayList<>();
                @SuppressWarnings("unchecked") Collection<Polygon> polygons = region.borders();
                for (var polygon : polygons)
                {
                    // find all the cells that might intersect with it
                    for (var gridCell : grid.cellsIntersecting(polygon.bounds()))
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
                cached.saveText(toString(cells));
            }
        }
        return included;
    }

    /**
     * Returns the regions we want data for
     */
    private RegionSet includedRegions()
    {
        var regions = new RegionSet();
        regions.addAll(Continent.NORTH_AMERICA.children());
        regions.addAll(Continent.EUROPE.children());
        regions.addAll(Continent.SOUTH_AMERICA.children());
        regions.addAll(Continent.AFRICA.children());
        regions.addAll(Continent.ASIA.children());
        regions.addAll(Continent.OCEANIA.children());
        return regions;
    }

    private void initialize(WorldGraph.AccessMode mode, WorldGraphRepositoryFolder folder)
    {
        // Save the grid mode
        this.mode = mode;

        switch (mode)
        {
            case READ:
            {
                ensureNotNull(folder, "Required repository folder is missing");
                folder.ensure(WorldGraphRepositoryFolder.Check.EXISTS);

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
        if (this.mode == WorldGraph.AccessMode.READ)
        {
            // refresh cell data
            refreshCellData(repositoryFolder());
            DEBUG.trace("Cells with graphs: $", cells(repositoryFolder(), WorldCell.DataType.GRAPH));
        }
    }

    /**
     * @param repositoryFolder The repository folder for which cell data should be refreshed from the filesystem
     */
    private void refreshCellData(WorldGraphRepositoryFolder repositoryFolder)
    {
        // Update status of PBF files
        @SuppressWarnings("SpellCheckingInspection")
        var pbfs = repositoryFolder.files(OSM_PBF.matcher()).asSet();
        for (var worldCell : included)
        {
            worldCell.hasPbf(repositoryFolder, pbfs.contains(worldCell.pbfFile(repositoryFolder)));
        }

        // Update status and size of graph files
        var graphs = repositoryFolder.files(GRAPH.matcher()).asSet();
        for (var worldCell : included)
        {
            var graphFile = worldCell.cellGraphFile(repositoryFolder);
            worldCell.hasGraph(repositoryFolder, graphs.contains(graphFile));
            for (var file : graphs)
            {
                if (file.equals(graphFile))
                {
                    worldCell.fileSize(file.sizeInBytes());
                }
            }
        }
    }

    private Folder regionCache()
    {
        return resolveProject(GraphProject.class).graphFolder().folder("world-graph/regions").mkdirs();
    }

    private String toString(List<WorldCell> cells)
    {
        var builder = new StringBuilder();
        for (var worldCell : cells)
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
