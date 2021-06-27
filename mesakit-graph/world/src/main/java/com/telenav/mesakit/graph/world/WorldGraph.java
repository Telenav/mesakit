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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.configuration.deployment.Deployment;
import com.telenav.kivakit.kernel.interfaces.code.Callback;
import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.kernel.language.threading.Threads;
import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.values.count.Bytes;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.language.values.version.Version;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.filters.operators.All;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.RelationSet;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.navigation.navigators.WayNavigator;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeSet;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.grid.WorldCellList;
import com.telenav.mesakit.graph.world.grid.WorldGrid;
import com.telenav.mesakit.graph.world.identifiers.WorldEdgeIdentifier;
import com.telenav.mesakit.graph.world.identifiers.WorldPlaceIdentifier;
import com.telenav.mesakit.graph.world.identifiers.WorldRelationIdentifier;
import com.telenav.mesakit.graph.world.identifiers.WorldVertexIdentifier;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;

import static com.telenav.kivakit.commandline.SwitchParser.enumSwitchParser;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;
import static com.telenav.mesakit.graph.Metadata.CountType.REQUIRE_EXACT;

/**
 * A virtual graph composed of many smaller sub-graphs arranged in {@link WorldCell}s of a {@link WorldGrid}. The
 * virtualization is mostly transparent and a {@link WorldGraph} largely functions in the same way as a {@link Graph}.
 * The grid of cells that compose the world graph can be retrieved with {@link #worldGrid()}.
 * <p>
 * World graphs have {@link Deployment} configurations that look like this:
 * <pre>
 * #
 * # Configuration of WorldGraph for myteam
 * #
 * configuration-class=com.telenav.kivakit.graph.world.WorldGraphConfiguration
 * cell-size=2 degrees
 * local-repository=${mesakit.graph.folder}/world-graph/repositories/myteam
 * remote-repository=hdfs://myteam/world-graph/repositories/myteam
 * </pre>
 * The cell-size specifies how large the cells are in the {@link WorldGrid}. The local-repository is the location of the
 * world graph data on the local machine. If there is a remote location where data is being transferred from,
 * remote-repository specifies that location and the local repository is a cache of that data.
 * <p>
 * <b>Creating and Loading World Graphs</b>
 * <p>
 * The factory method {@link #create(WorldGraphRepositoryFolder, Metadata)} is used to create a new world graph. If the
 * world graph already exists locally, it can be loaded with {@link #load(WorldGraphRepositoryFolder)}. If it exists
 * remotely, it can be loaded with {@link #loadRemote(WorldGraphRepositoryFolder, WorldGraphRepositoryFolder)}. Remote
 * world graphs are copied to the local repository in ~/.mesakit so that data can be read by the graph API (graph files
 * are zip files which must be local files to be accessed in Java).
 * <p>
 * Methods in {@link Graph} are overridden to provide scoping of virtual graph elements ({@link WorldEdge}, {@link
 * WorldVertex}, {@link WorldRelation} and {@link WorldPlace} by the sub-graph (cell) containing their data. This gives
 * the illusion of one large graph even though the graph is broken down into cells each having their own graph object.
 * <p>
 * <b>Force-Loading Graph Element Attributes</b>
 * <p>
 * The graphs and graph element attributes in all cells are lazy-loaded, but they can be forced into memory with these
 * load methods:
 * <ul>
 *     <li>{@link #loadAll()} - Loads all data in all cells unconditionally</li>
 *     <li>{@link #loadAll(AttributeSet)} - Loads only the set of attributes specified</li>
 *     <li>{@link #loadAllExcept(AttributeSet)} - Loads all attributes except those specified</li>
 * </p>
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class WorldGraph extends Graph
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static WorldGraph create(final WorldGraphRepositoryFolder folder, final Metadata metadata)
    {
        return new WorldGraph(AccessMode.CREATE, metadata, folder);
    }

    public static WorldGraph load(final WorldGraphRepositoryFolder local)
    {
        return loadRemote(local, null);
    }

    public static WorldGraph loadRemote(final WorldGraphRepositoryFolder local, final WorldGraphRepositoryFolder remote)
    {
        if (remote != null)
        {
            final var progress = Progress.create(LOGGER, "bytes");
            remote.copyTo(local, CopyMode.OVERWRITE, progress);
        }

        final var index = WorldGraphIndex.load(local.indexFile());
        if (index != null)
        {
            final var metadata = index.metadata();
            return new WorldGraph(AccessMode.READ, metadata, local);
        }

        return fail("Unable to load world graph index file $", local.indexFile());
    }

    /**
     * The mode for accessing world graph data
     */
    @SuppressWarnings("unused")
    public enum AccessMode
    {
        /** Read data */
        READ,

        /** Write data */
        WRITE,

        /** Create a graph during extraction or for test purposes */
        CREATE;

        public static SwitchParser.Builder<AccessMode> switchParser()
        {
            return enumSwitchParser("world-graph-mode", "The world graph access mode", AccessMode.class);
        }
    }

    /** The world grid, which breaks the world up into world cells */
    private final WorldGrid worldGrid;

    /** The smallest sub-graph for accessing attributes common to all cells */
    private Graph smallest;

    /**
     * Constructs a world graph with a repository folder
     *
     * @param mode The mode to access data
     * @param metadata Metadata for the graph
     * @param folder The local data folder to read from
     */
    private WorldGraph(final AccessMode mode, final Metadata metadata, final WorldGraphRepositoryFolder folder)
    {
        super(metadata);
        worldGrid = new WorldGrid(this, mode, folder);
    }

    @Override
    public Rectangle bounds()
    {
        return metadata().dataBounds();
    }

    @Override
    public boolean contains(final Edge edge)
    {
        return worldCellsWithin(edge.bounds()).contains(edge);
    }

    @Override
    public boolean contains(final EdgeRelation relation)
    {
        return worldCellsWithin(relation.bounds()).contains(relation);
    }

    @Override
    public boolean contains(final Vertex vertex)
    {
        final var worldCell = worldGrid().worldCell(vertex.location());
        if (worldCell.hasGraphFile(worldGrid.repositoryFolder()))
        {
            return worldCell.cellGraph().contains(vertex);
        }
        return false;
    }

    @Override
    public Count edgeCount()
    {
        return metadata().edgeCount(REQUIRE_EXACT);
    }

    @Override
    public Edge edgeForIdentifier(final EdgeIdentifier identifier)
    {
        // If we have a fully-qualified world edge identifier
        if (identifier instanceof WorldEdgeIdentifier)
        {
            // then return the world graph edge
            final var worldIdentifier = (WorldEdgeIdentifier) identifier;
            return worldIdentifier.edge();
        }

        // Look up the cell for the way identifier
        final var worldCell = worldGrid().index().worldCellForWayIdentifier(worldGrid,
                isOsm() ? identifier.asWayIdentifier() : new PbfWayIdentifier(identifier.asLong()));
        if (worldCell != null)
        {
            // and look in the graph
            final var cellGraph = worldCell.cellGraph();
            if (cellGraph != null)
            {
                // for the edge
                return cellGraph.edgeForIdentifier(identifier);
            }
        }

        return null;
    }

    @Override
    public EdgeSequence edges()
    {
        return allWorldCells().edges();
    }

    public EdgeSequence edgesInside(final Region region)
    {
        return edgesIntersecting(region.bounds()).matching(edge -> region.intersectsOrContains(edge.roadShape()));
    }

    @Override
    public EdgeSequence edgesIntersecting(final Rectangle bounds, final Matcher<Edge> matcher)
    {
        return worldCellsWithin(bounds).edgesIntersecting(bounds, matcher);
    }

    public Bytes estimatedMemorySize(final WorldCell worldCell)
    {
        final var size = worldGrid().index().memorySize(worldCell);
        if (size != null)
        {
            return size;
        }
        if (worldCell.fileSize() != null)
        {
            return worldCell.fileSize().times(2);
        }
        return Bytes.megabytes(32);
    }

    @Override
    public Bytes estimatedMemorySize()
    {
        var total = Bytes._0;
        for (final var graph : worldCells().cellGraphs())
        {
            final var size = graph.estimatedMemorySize();
            LOGGER.information("$ => $", graph.name(), size);
            total = total.add(size);
        }
        return total;
    }

    public WorldGraphRepositoryFolder folder()
    {
        return worldGrid().repositoryFolder();
    }

    @Override
    public Count forwardEdgeCount()
    {
        return metadata().forwardEdgeCount(REQUIRE_EXACT);
    }

    @Override
    public EdgeSequence forwardEdges()
    {
        return allWorldCells().forwardEdges();
    }

    public EdgeSequence forwardEdgesInside(final Region region)
    {
        return forwardEdgesIntersecting(region.bounds())
                .matching(edge -> region.contains(edge.fromLocation()) || region.contains(edge.toLocation()));
    }

    @Override
    public EdgeSequence forwardEdgesIntersecting(final Rectangle bounds, final Matcher<Edge> matcher)
    {
        return worldCellsWithin(bounds).forwardEdgesIntersecting(bounds, matcher);
    }

    @Override
    public boolean isComposite()
    {
        return true;
    }

    @Override
    public final boolean isUnloaded()
    {
        return unsupported();
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public final void load(final GraphArchive archive)
    {
        unsupported();
    }

    @Override
    public Graph loadAll(final AttributeSet attributes)
    {
        LOGGER.information("Loading $", attributes);
        forEachWorldCell(worldCell ->
        {
            LOGGER.information("Loading $", worldCell);
            worldCell.cellGraph().loadAll(attributes);
            LOGGER.information("Loaded $", worldCell);
            LOGGER.flush(Duration.ONE_MINUTE);
        });
        LOGGER.information("Loaded $", attributes);
        return this;
    }

    @Override
    public Graph loadAll()
    {
        LOGGER.information("Loading all attributes");
        forEachWorldCell(worldCell ->
        {
            LOGGER.information("Loading $", worldCell);
            worldCell.cellGraph().loadAll();
            LOGGER.information("Loaded $", worldCell);
            LOGGER.flush(Duration.ONE_MINUTE);
        });
        LOGGER.information("Loaded all attributes");
        return this;
    }

    @Override
    public Graph loadAllExcept(final AttributeSet attributes)
    {
        LOGGER.information("Loading all attributes except $", attributes);
        forEachWorldCell(worldCell ->
        {
            LOGGER.information("Loading $", worldCell);
            worldCell.cellGraph().loadAllExcept(attributes);
            LOGGER.information("Loaded $", worldCell);
            LOGGER.flush(Duration.ONE_MINUTE);
        });
        LOGGER.information("Loaded all attributes except $", attributes);
        return this;
    }

    /**
     * @return The access mode for this world graph
     */
    public AccessMode mode()
    {
        return worldGrid.mode();
    }

    @Override
    public String name()
    {
        return worldGrid.name();
    }

    @SuppressWarnings("UnusedReturnValue")
    public Graph newCellGraph()
    {
        return dataSpecification().newGraph(metadata());
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldEdge newEdge(final EdgeIdentifier identifier)
    {
        return ((WorldEdgeIdentifier) identifier).edge();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldPlace newPlace(final PlaceIdentifier identifier)
    {
        return ((WorldPlaceIdentifier) identifier).place();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldRelation newRelation(final RelationIdentifier identifier)
    {
        return ((WorldRelationIdentifier) identifier).relation();
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorldVertex newVertex(final VertexIdentifier identifier)
    {
        return ((WorldVertexIdentifier) identifier).vertex();
    }

    @Override
    public Count placeCount()
    {
        return worldGrid().index().placeCount();
    }

    @Override
    public Place placeNear(final Location location)
    {
        return worldGrid().index().placeForLocation(location);
    }

    @Override
    public Iterable<Place> places()
    {
        return placesInside(Rectangle.MAXIMUM);
    }

    @Override
    public Iterable<Place> placesInside(final Rectangle bounds)
    {
        return worldGrid().index().placesInside(bounds);
    }

    @Override
    public Precision precision()
    {
        return smallestGraph().precision();
    }

    @Override
    public Count relationCount()
    {
        return metadata().relationCount(REQUIRE_EXACT);
    }

    @Override
    public EdgeRelation relationForIdentifier(final RelationIdentifier identifier)
    {
        return unsupported();
    }

    @Override
    public Iterable<EdgeRelation> relations()
    {
        return allWorldCells().relations();
    }

    public RelationSet relationsInside(final Region region)
    {
        return relationsIntersecting(region.bounds(), new All<>()).matching(relation ->
        {
            for (final var edge : relation.edgeSet())
            {
                if (region.intersectsOrContains(edge.roadShape()))
                {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public RelationSet relationsIntersecting(final Rectangle bounds, final Matcher<EdgeRelation> matcher)
    {
        return worldCellsWithin(bounds).relationsIntersecting(bounds, matcher);
    }

    @Override
    public final Resource resource()
    {
        return worldGrid().repositoryFolder().indexFile();
    }

    @Override
    public Route routeForWayIdentifier(final MapWayIdentifier wayIdentifier)
    {
        final var worldCell = worldGrid().index().worldCellForWayIdentifier(worldGrid, wayIdentifier);
        if (worldCell != null)
        {
            final var graph = worldCell.cellGraph();
            if (graph != null)
            {
                final var route = graph.routeForWayIdentifier(wayIdentifier);
                if (route != null)
                {
                    final var edge = new WorldEdge(worldCell, route.first());
                    return edge.route(new WayNavigator(edge), Maximum.MAXIMUM);
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public final void save(final GraphArchive archive)
    {
        unsupported();
    }

    @Override
    public boolean supports(final Attribute attribute)
    {
        return smallestGraph().supports(attribute);
    }

    @Override
    public boolean supportsFullPbfNodeInformation()
    {
        return false;
    }

    @Override
    public final void unload()
    {
        unsupported();
    }

    /**
     * The version of the underlying graph data (as opposed to the world graph version)
     */
    @Override
    public Version version()
    {
        return smallestGraph().version();
    }

    @Override
    public Count vertexCount()
    {
        return metadata().vertexCount(REQUIRE_EXACT);
    }

    @Override
    public Vertex vertexForIdentifier(final VertexIdentifier identifier)
    {
        return unsupported();
    }

    @Override
    public Vertex vertexNearest(final Location location, final Distance maximum,
                                final RoadFunctionalClass functionalClass)
    {
        Vertex nearest = null;
        var nearestDistance = Distance.MAXIMUM;
        for (final var vertex : worldCellsWithin(location.within(maximum)).vertexesNearest(location, maximum, functionalClass))
        {
            final var distance = vertex.location().distanceTo(location);
            if (nearest == null || distance.isLessThan(nearestDistance))
            {
                nearest = vertex;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    @Override
    public VertexSequence vertexes()
    {
        return allWorldCells().vertexes();
    }

    @Override
    public VertexSequence vertexesInside(final Rectangle bounds)
    {
        return worldCellsWithin(bounds).vertexesInside(bounds);
    }

    @Override
    public VertexSequence vertexesInside(final Rectangle bounds, final Matcher<Vertex> matcher)
    {
        return worldCellsWithin(bounds).vertexesInside(bounds, matcher);
    }

    public VertexSequence vertexesInside(final Region region)
    {
        return vertexesInside(region.bounds()).matching(region::contains);
    }

    /**
     * @return The version of the world graph (as opposed to the underlying graph file version)
     */
    public Version worldGraphVersion()
    {
        return worldGrid().index().version();
    }

    public WorldGrid worldGrid()
    {
        return worldGrid;
    }

    /**
     * @return A list of the world cells in this graph, with data installed locally
     */
    private WorldCellList allWorldCells()
    {
        return worldCells();
    }

    /**
     * Executes the given code on all world cells in parallel
     *
     * @param code The code to execute for each world cell
     */
    private void forEachWorldCell(final Callback<WorldCell> code)
    {
        final var executor = Threads.threadPool("WorldCell");
        allWorldCells().forEach(worldCell -> executor.submit(() -> code.onCallback(worldCell)));
        Threads.shutdownAndAwait(executor);
    }

    private Graph smallestGraph()
    {
        if (smallest == null)
        {
            final var smallestCell = worldCells().smallest();
            if (smallestCell != null)
            {
                smallest = smallestCell.cellGraph();
            }
            else
            {
                return fail("World graph is empty");
            }
        }
        return smallest;
    }

    /**
     * @return A list of world cells in this world graph
     */
    private WorldCellList worldCells()
    {
        return worldGrid().cells(worldGrid.repositoryFolder(), WorldCell.DataType.GRAPH);
    }

    private WorldCellList worldCellsWithin(final Rectangle bounds)
    {
        return worldGrid().cells(worldGrid.repositoryFolder(), WorldCell.DataType.GRAPH, bounds);
    }
}
