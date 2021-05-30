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

package com.telenav.tdk.graph.world;

import com.telenav.tdk.core.collections.primitive.map.split.SplitLongToIntMap;
import com.telenav.tdk.core.filesystem.File;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.naming.*;
import com.telenav.tdk.core.kernel.interfaces.object.Source;
import com.telenav.tdk.core.kernel.language.io.serialization.TdkSerializer;
import com.telenav.tdk.core.kernel.language.iteration.*;
import com.telenav.tdk.core.kernel.language.vm.JavaVirtualMachine;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.operation.progress.ProgressReporter;
import com.telenav.tdk.core.kernel.scalars.bytes.Bytes;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.scalars.versioning.*;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.core.resource.compression.archive.*;
import com.telenav.tdk.data.formats.library.map.identifiers.WayIdentifier;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.io.archive.GraphArchive;
import com.telenav.tdk.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeAttributes;
import com.telenav.tdk.graph.traffic.roadsection.codings.tmc.TmcTableIdentifier;
import com.telenav.tdk.graph.world.grid.*;
import com.telenav.tdk.graph.world.repository.*;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.geography.indexing.quadtree.QuadTreeSpatialIndex;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.region.project.TdkMapRegionLimits;
import com.telenav.tdk.map.utilities.grid.*;

import java.io.Serializable;
import java.util.*;

import static com.telenav.tdk.core.kernel.validation.Validate.fail;
import static com.telenav.tdk.core.resource.compression.archive.ZipArchive.Mode.*;

/**
 * The world graph index stores metadata and provides spatial indexing services for a {@link WorldGraph} which can't
 * easily be composed from the spatial indexes in individual cell graphs.
 * <p>
 * This class is an implementation detail of the {@link WorldGraph} and is only public because it has to be accessed
 * from other packages.
 * <p>
 * <b>Attributes</b>
 * <p>
 * A world graph index has metadata describing the contents of the entire world graph. This is accessed with {@link
 * #metadata()}. The version of the graph index can be retrieved with {@link #version()}.
 * <p>
 * <b>Persistence</b>
 * <p>
 * The world graph index for a {@link WorldGraph} is stored in the {@link WorldGraphRepositoryFolder} that contains the
 * cell graphs. The file containing the index is called "index.world". This file is a {@link FieldArchive} stored in zip
 * format where each stream in the zip file stores an object in this index. A world graph index can be loaded from an
 * "index.world" file with {@link #loadFrom(File)} and it can be saved to such a file with {@link #save(File,
 * Metadata)}.
 * <p>
 * <b>Places</b>
 * <p>
 * Since there is a frequent need to query places from many neighboring cells, this is made more efficient with a single
 * spatial index for all places in a world graph. The number of places in this index can be retrieved with {@link
 * #placeCount()} and queries for places can be conducted with:
 * <ul>
 *     <li>{@link #placesInside(Rectangle)} - A sequence of places inside the given bounds</li>
 *     <li>{@link #placeForLocation(Location)} - The place (if any) at the given location</li>
 * </ul>
 * <p>
 * <b>Ways</b>
 * <p>
 * It would be time consuming to search all cells in the world graph for a way identifier, so this index provides
 * the {@link #worldCellForWayIdentifier(WorldGrid, WayIdentifier)} method to find one arbitrary cell containing a given way identifier.
 * Any other cells that contain the way identifier can be found by navigating the way starting within the given cell.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldGraphRepository
 * @see FieldArchive
 * @see Metadata
 */
public class WorldGraphIndex implements Named, Serializable, NamedObject
{
    private static final long serialVersionUID = -9197937010512624042L;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public static WorldGraphIndex create()
    {
        return new WorldGraphIndex();
    }

    public static WorldGraphIndex load(final File file)
    {
        if (file.isRemote())
        {
            LOGGER.warning("Cannot load remote world graph index: $", file);
        }
        else
        {
            if (file.exists())
            {
                return new WorldGraphIndex().loadFrom(file);
            }
        }
        return null;
    }

    public static class CastingIterableAdapter<T, AS> extends BaseIterable<AS>
    {
        private final Source<Iterator<T>> iteratorSource;

        public CastingIterableAdapter(final Source<Iterator<T>> iteratorSource)
        {
            this.iteratorSource = iteratorSource;
        }

        @Override
        protected Next<AS> newNext()
        {
            return new Next<>()
            {
                private final Iterator<T> iterator = iteratorSource.get();

                @SuppressWarnings("unchecked")
                @Override
                public AS onNext()
                {
                    if (iterator.hasNext())
                    {
                        return (AS) iterator.next();
                    }
                    return null;
                }
            };
        }
    }

    @TdkArchivedField(lazy = true)
    private List<WorldPlace> places;

    @TdkArchivedField
    private final Map<GridCell, Bytes> memorySize = new HashMap<>();

    @TdkArchivedField(lazy = true)
    private Map<TmcTableIdentifier, Set<GridCellIdentifier>> cellIdsForTmcTable;

    /** Spatial index of places */
    private transient QuadTreeSpatialIndex<WorldPlace> placeSpatialIndex;

    /** Edge identifier to cell map */
    @TdkArchivedField(lazy = true)
    private SplitLongToIntMap cellForWayIdentifier;

    /** The archive where this is stored */
    private FieldArchive archive;

    /** Meta data about this world graph */
    private Metadata metadata;

    private WorldGraph worldGraph;

    private WorldGraphIndex()
    {
        cellForWayIdentifier = new SplitLongToIntMap("WorldGraphIndex.cellForPbfWayIdentifier");
        cellForWayIdentifier.initialSize(TdkMapRegionLimits.ESTIMATED_WAYS);
        cellForWayIdentifier.initialize();
    }

    public WorldCellList cellsForTmcTable(final TmcTableIdentifier tableIdentifier, final WorldGrid grid)
    {
        final var cells = new WorldCellList();
        if (cellIdsForTmcTable().containsKey(tableIdentifier))
        {
            for (final var cellIdentifier : cellIdsForTmcTable().get(tableIdentifier))
            {
                final var gridCell = grid.grid().cellForIdentifier(cellIdentifier);
                cells.add(grid.worldCell(gridCell));
            }
        }
        return cells;
    }

    public synchronized void index(final WorldCell worldCell, final Graph cellGraph)
    {
        final var worldGrid = worldCell.worldGrid();
        assert worldGrid.worldGraph() != null;

        // store the size of the graph in the index
        if (DEBUG.isEnabled())
        {
            memorySize.put(worldCell.gridCell(), JavaVirtualMachine.local().sizeOfObjectGraph(cellGraph.loadAll(),
                    "WorldGraphIndex.graph", Bytes.kilobytes(100)));
        }

        // and add all places having a population
        for (final var place : cellGraph.placesWithPopulationOfAtLeast(Count._1))
        {
            places().add(new WorldPlace(worldCell, place));
        }

        // add way identifiers to map
        for (final var edge : cellGraph.forwardEdges())
        {
            cellForWayIdentifier.put(edge.wayIdentifier().asLong(), worldCell.gridCell().identifier().identifier());
        }

        // search tmc tables in this cell and store them into index
        indexTmcTables(worldCell, cellGraph);
    }

    /**
     * @param file The world graph index resource to load
     */
    public WorldGraphIndex loadFrom(File file)
    {
        // Validate that the resource is a zip archive. If it's not then the file is some earlier
        // placeholder index file that we can ignore.
        if (FieldArchive.is(file))
        {
            // Record start time
            final var start = Time.now();

            // If the resource is a virtual file, it must be materialized to read it as a zip file
            file = file.materialized(ProgressReporter.NULL);

            // Attach the zip archive and the field archive based on it
            archive = new FieldArchive(file, ProgressReporter.NULL, READ);

            // Clear out fields we will load from archive
            clearLazyLoadedFields();

            try
            {
                // Load archived fields
                final var version = archive.version();
                final VersionedObject<Metadata> metadata = archive.zip().load(TdkSerializer.threadSerializer(LOGGER), "metadata");
                if (metadata != null)
                {
                    this.metadata = metadata.get();
                    archive.loadFieldsOf(this);

                    // Done!
                    DEBUG.trace("Loaded $ (version $) in ${debug}", file, version, start.elapsedSince());
                }
                else
                {
                    fail("Unable to load metadata");
                }
            }
            catch (final Exception e)
            {
                fail(e, "Unable to load from " + file);
            }
        }
        else
        {
            fail("Unable to load index: $ is not a zip archive", file);
        }
        return this;
    }

    public Bytes memorySize(final WorldCell worldCell)
    {
        synchronized (memorySize)
        {
            return memorySize.get(worldCell.gridCell());
        }
    }

    public Metadata metadata()
    {
        return metadata;
    }

    @Override
    public String name()
    {
        return "world.graph.index";
    }

    @Override
    public String objectName()
    {
        return "world.graph";
    }

    public Count placeCount()
    {
        synchronized (this)
        {
            return Count.of(places().size());
        }
    }

    public Place placeForLocation(final Location location)
    {
        for (final var place : placesInside(location.bounds().expanded(Distance.ONE_METER)))
        {
            if (place.location().equals(location))
            {
                return place;
            }
        }
        return null;
    }

    public Iterable<Place> placesInside(final Rectangle bounds)
    {
        return new CastingIterableAdapter<>(() -> placeSpatialIndex().inside(bounds));
    }

    /**
     * Saves this world graph index to the given file
     */
    public final void save(final File file, final Metadata metadata)
    {
        assert metadata != null;

        // Record start time
        final var start = Time.now();

        try
        {
            // Create archive and save all non-null archived fields
            final var archive = new FieldArchive(file, ProgressReporter.NULL, WRITE);
            final var version = GraphArchive.VERSION;
            archive.version(version);
            archive.save("metadata", new VersionedObject<>(version, metadata));
            archive.saveFieldsOf(this, version);
            archive.close();

            // We're done!
            DEBUG.trace("Saved $ (version $) in ${debug}", file, version, start.elapsedSince());
        }
        catch (final Exception e)
        {
            fail(e, "Unable to save to" + file);
        }
    }

    public Version version()
    {
        if (archive != null)
        {
            return archive.version();
        }
        return null;
    }

    @SuppressWarnings({ "exports" })
    public WorldCell worldCellForWayIdentifier(final WorldGrid grid, final WayIdentifier wayIdentifier)
    {
        if (cellForWayIdentifier == null)
        {
            archive.loadFieldOf(this, "cell-for-way-identifier");
        }
        final var cellIdentifier = cellForWayIdentifier.get(wayIdentifier.asLong());
        if (!cellForWayIdentifier.isNull(cellIdentifier))
        {
            return grid.worldCell(cellIdentifier);
        }
        return null;
    }

    public void worldGraph(final WorldGraph worldGraph)
    {
        this.worldGraph = worldGraph;
    }

    private Map<TmcTableIdentifier, Set<GridCellIdentifier>> cellIdsForTmcTable()
    {
        if (cellIdsForTmcTable == null)
        {
            archive.loadFieldOf(this, "cellIdsForTmcTable");
        }
        return cellIdsForTmcTable;
    }

    private void clearLazyLoadedFields()
    {
        // These will be lazy loaded from the archive, as needed
        places = null;
        cellForWayIdentifier = null;
    }

    private void indexTmcTables(final WorldCell worldCell, final Graph graph)
    {
        for (final var edge : graph.edges())
        {
            if (!edge.supports(OsmEdgeAttributes.get().FORWARD_TMC_IDENTIFIERS))
            {
                continue;
            }

            if (cellIdsForTmcTable == null)
            {
                cellIdsForTmcTable = new HashMap<>();
            }

            for (final var tmc : edge.tmcIdentifiers())
            {
                final var table = TmcTableIdentifier.fromTmcIdentifier(tmc);
                final var cellIds = cellIdsForTmcTable.computeIfAbsent(table, k -> new HashSet<>());
                cellIds.add(worldCell.gridCell().identifier());
            }
        }
    }

    private synchronized QuadTreeSpatialIndex<WorldPlace> placeSpatialIndex()
    {
        if (placeSpatialIndex == null)
        {
            placeSpatialIndex = new QuadTreeSpatialIndex<>();
            synchronized (this)
            {
                for (final var place : places())
                {
                    placeSpatialIndex.add(place);
                }
            }
        }
        return placeSpatialIndex;
    }

    private synchronized List<WorldPlace> places()
    {
        // If lazy-loaded field isn't loaded yet,
        if (places == null)
        {
            // load the field from the archive
            if (archive != null)
            {
                archive.loadFieldOf(this, "places");
                for (final var place : places)
                {
                    place.graph(worldGraph);
                }
            }
            else
            {
                places = new ArrayList<>();
            }
        }

        return places;
    }
}
