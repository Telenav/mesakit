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

import com.telenav.kivakit.configuration.settings.Settings;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.collections.set.ObjectSet;
import com.telenav.kivakit.kernel.language.objects.reference.virtual.VirtualReferenceTracker;
import com.telenav.kivakit.kernel.language.objects.reference.virtual.VirtualReferenceType;
import com.telenav.kivakit.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.resource.ResourceList;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphProject;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.WorldGraphConfiguration;
import com.telenav.mesakit.graph.world.WorldGraphIndex;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepository;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
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

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

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
    private ObjectSet<WorldCell> included;

    /** The world graph */
    private WorldGraph worldGraph;

    /** The grid mode */
    private WorldGraph.AccessMode mode;

    /** Graph reference tracker for cells belonging to the grid */
    private VirtualReferenceTracker<Graph> tracker;

    /** Approximate maximum amount of memory to hard reference */
    private final Bytes maximumMemory = Bytes.gigabytes(26);

    /** Graph meta data */
    private Metadata metadata;

    /** The index for this graph */
    private WorldGraphIndex index;

    public WorldGrid(final WorldGraph worldGraph, final WorldGraph.AccessMode mode,
                     final WorldGraphRepositoryFolder folder)
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
    public WorldCellList cells(final WorldGraphRepositoryFolder repositoryFolder, final WorldCell.DataType data)
    {
        return cells(repositoryFolder, data, Rectangle.MAXIMUM);
    }

    /**
     * @return List of world cells intersecting the given bounds in the given grid folder with the given data
     */
    public WorldCellList cells(final WorldGraphRepositoryFolder repositoryFolder, final WorldCell.DataType data,
                               final Rectangle bounds)
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
    public Count extract(final WorldGraphRepositoryFolder repositoryFolder, final Source<PbfDataSource> data)
    {
        // Start time
        final var start = Time.now();

        // Clear our data folder before writing to it
        LOGGER.information(AsciiArt.textBox("Extracting", "input: $\noutput: $", data.get().resource(), repositoryFolder));
        repositoryFolder.clearAll();

        // Extract regions from PBF file
        final var extracted = extractCells(data, repositoryFolder);
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
     * @return The world cells included in the current set of regions
     */
    public ObjectSet<WorldCell> included()
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

    public WorldGraph.AccessMode mode()
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
            feature.title(worldCell.identity().mesakit().code());
            feature.add(new GeoJsonPolyline(worldCell.bounds().asPolyline()));
            output.add(feature);
        }
        output.save(File.parse("data/world-graph-2-degree-cells.geojson"));
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
        return cells(repositoryFolder, WorldCell.DataType.GRAPH, location.bounds().expanded(Distance.meters(1)));
    }

    private WorldGraphConfiguration configuration()
    {
        return Settings.of(this).requireSettings(WorldGraphConfiguration.class);
    }

    private void createGrid()
    {
        // Create grid object
        grid = new Grid(configuration().cellSize(), Latitude.MAXIMUM);

        // Create 2D array of cells
        final var latitudeCells = grid.latitudeCellCount().asInt();
        final var longitudeCells = grid.longitudeCellCount().asInt();
        cells = new WorldCell[latitudeCells][longitudeCells];

        // Get set of cells that are geographically included
        included = findIncludedCells();
    }

    /**
     * @return The PBF files extracted from the given pbf file into the given world grid folder
     */
    private ResourceList extractCells(final Source<PbfDataSource> data,
                                      final WorldGraphRepositoryFolder repositoryFolder)
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
        return cutter.cut();
    }

    /**
     * @return A set of cells that are included by the {@link #includedRegions()}
     */
    private ObjectSet<WorldCell> findIncludedCells()
    {
        // Go through cells in included regions
        final var included = new ObjectSet<WorldCell>(Maximum._10_000);
        for (final var region : includedRegions())
        {
            final var cached = regionCache().file(region.fileName()
                    .withSuffix("-" + Math.round(grid.approximateCellSize().asDegrees()) + "-degree-grid")
                    .withExtension(Extension.parse(".cells")));
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

    private void initialize(final WorldGraph.AccessMode mode, final WorldGraphRepositoryFolder folder)
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
    private void refreshCellData(final WorldGraphRepositoryFolder repositoryFolder)
    {
        // Update status of PBF files
        final var pbfs = repositoryFolder.files(Extension.OSM_PBF.fileMatcher()).asSet();
        for (final var worldCell : included)
        {
            worldCell.hasPbf(repositoryFolder, pbfs.contains(worldCell.pbfFile(repositoryFolder)));
        }

        // Update status and size of graph files
        final var graphs = repositoryFolder.files(Extension.GRAPH.fileMatcher()).asSet();
        for (final var worldCell : included)
        {
            final var graphFile = worldCell.cellGraphFile(repositoryFolder);
            worldCell.hasGraph(repositoryFolder, graphs.contains(graphFile));
            for (final var file : graphs)
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
        return GraphProject.get().graphFolder().folder("world-graph/regions").mkdirs();
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
