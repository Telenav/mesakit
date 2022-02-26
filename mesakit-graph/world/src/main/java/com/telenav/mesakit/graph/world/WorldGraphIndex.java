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

package com.telenav.mesakit.graph.world;

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.iteration.BaseIterable;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.language.values.version.VersionedObject;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToIntMap;
import com.telenav.kivakit.resource.compression.archive.FieldArchive;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.serialization.core.SerializationSession;
import com.telenav.kivakit.serialization.core.SerializationSessionFactory;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.grid.WorldGrid;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepository;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.quadtree.QuadTreeSpatialIndex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.project.RegionLimits;
import com.telenav.mesakit.map.utilities.grid.GridCell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.READ;
import static com.telenav.kivakit.resource.compression.archive.ZipArchive.Mode.WRITE;

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
 * the {@link #worldCellForWayIdentifier(WorldGrid, MapWayIdentifier)} method to find one arbitrary cell containing a given way identifier.
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

    public static WorldGraphIndex load(File file)
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

        public CastingIterableAdapter(Source<Iterator<T>> iteratorSource)
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

    /** The archive where this is stored */
    private FieldArchive archive;

    /** Edge identifier to cell map */
    @KivaKitArchivedField(lazy = true)
    private SplitLongToIntMap cellForWayIdentifier;

    @KivaKitArchivedField
    private final Map<GridCell, Bytes> memorySize = new HashMap<>();

    /** Meta data about this world graph */
    private Metadata metadata;

    /** Spatial index of places */
    private transient QuadTreeSpatialIndex<WorldPlace> placeSpatialIndex;

    @KivaKitArchivedField(lazy = true)
    private List<WorldPlace> places;

    private WorldGraph worldGraph;

    private WorldGraphIndex()
    {
        cellForWayIdentifier = new SplitLongToIntMap("WorldGraphIndex.cellForPbfWayIdentifier");
        cellForWayIdentifier.initialSize(RegionLimits.ESTIMATED_WAYS);
        cellForWayIdentifier.initialize();
    }

    public synchronized void index(WorldCell worldCell, Graph cellGraph)
    {
        var worldGrid = worldCell.worldGrid();
        assert worldGrid.worldGraph() != null;

        // store the size of the graph in the index
        if (DEBUG.isDebugOn())
        {
            memorySize.put(worldCell.gridCell(), JavaVirtualMachine.local().sizeOfObjectGraph(cellGraph.loadAll(),
                    "WorldGraphIndex.graph", Bytes.kilobytes(100)));
        }

        // and add all places having a population
        for (var place : cellGraph.placesWithPopulationOfAtLeast(Count._1))
        {
            places().add(new WorldPlace(worldCell, place));
        }

        // add way identifiers to map
        for (var edge : cellGraph.forwardEdges())
        {
            cellForWayIdentifier.put(edge.wayIdentifier().asLong(), worldCell.gridCell().identifier().identifier());
        }
    }

    /**
     * @param file The world graph index resource to load
     */
    public WorldGraphIndex loadFrom(File file)
    {
        // Validate that the resource is a zip archive. If it's not then the file is some earlier
        // placeholder index file that we can ignore.
        if (ZipArchive.is(LOGGER, file))
        {
            // Record start time
            var start = Time.now();

            // If the resource is a virtual file, it must be materialized to read it as a zip file
            file = file.materialized(ProgressReporter.NULL);

            // Attach the zip archive and the field archive based on it
            archive = new FieldArchive(file, SerializationSessionFactory.threadLocal(), ProgressReporter.NULL, READ);

            // Clear out fields we will load from archive
            clearLazyLoadedFields();

            try
            {
                // Load archived fields
                var version = archive.version();
                VersionedObject<Metadata> metadata = archive.zip().load(SerializationSession.threadLocal(LOGGER), "metadata");
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
            catch (Exception e)
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

    public Bytes memorySize(WorldCell worldCell)
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
            return Count.count(places().size());
        }
    }

    public Place placeForLocation(Location location)
    {
        for (var place : placesInside(location.bounds().expanded(Distance.ONE_METER)))
        {
            if (place.location().equals(location))
            {
                return place;
            }
        }
        return null;
    }

    public Iterable<Place> placesInside(Rectangle bounds)
    {
        return new CastingIterableAdapter<>(() -> placeSpatialIndex().inside(bounds));
    }

    /**
     * Saves this world graph index to the given file
     */
    public final void save(File file, Metadata metadata)
    {
        assert metadata != null;

        // Record start time
        var start = Time.now();

        try
        {
            // Create archive and save all non-null archived fields
            var archive = new FieldArchive(file, SerializationSessionFactory.threadLocal(), ProgressReporter.NULL, WRITE);
            var version = GraphArchive.VERSION;
            archive.version(version);
            archive.save("metadata", new VersionedObject<>(version, metadata));
            archive.saveFieldsOf(this, version);
            archive.close();

            // We're done!
            DEBUG.trace("Saved $ (version $) in ${debug}", file, version, start.elapsedSince());
        }
        catch (Exception e)
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
    public WorldCell worldCellForWayIdentifier(WorldGrid grid, MapWayIdentifier wayIdentifier)
    {
        if (cellForWayIdentifier == null)
        {
            archive.loadFieldOf(this, "cell-for-way-identifier");
        }
        var cellIdentifier = cellForWayIdentifier.get(wayIdentifier.asLong());
        if (!cellForWayIdentifier.isNull(cellIdentifier))
        {
            return grid.worldCell(cellIdentifier);
        }
        return null;
    }

    public void worldGraph(WorldGraph worldGraph)
    {
        this.worldGraph = worldGraph;
    }

    private void clearLazyLoadedFields()
    {
        // These will be lazy loaded from the archive, as needed
        places = null;
        cellForWayIdentifier = null;
    }

    private synchronized QuadTreeSpatialIndex<WorldPlace> placeSpatialIndex()
    {
        if (placeSpatialIndex == null)
        {
            placeSpatialIndex = new QuadTreeSpatialIndex<>();
            synchronized (this)
            {
                for (var place : places())
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
                for (var place : places)
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
