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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.kivakit.core.language.Classes;
import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.Message;
import com.telenav.kivakit.core.messaging.Repeater;
import com.telenav.kivakit.core.messaging.context.CallStack;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.core.string.AsIndentedString;
import com.telenav.kivakit.core.string.Differences;
import com.telenav.kivakit.core.string.ObjectIndenter;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.version.Version;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.interfaces.collection.NextIterator;
import com.telenav.kivakit.interfaces.comparison.Filter;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.collections.RelationSet;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.identifiers.ShapePointIdentifier;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.io.load.SmartGraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.copying.CopyingGraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.decimation.DecimatingGraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.region.regions.ContinentLoader;
import com.telenav.mesakit.graph.io.load.loaders.region.regions.CountryLoader;
import com.telenav.mesakit.graph.io.load.loaders.region.regions.MetropolitanAreaLoader;
import com.telenav.mesakit.graph.io.load.loaders.region.regions.StateLoader;
import com.telenav.mesakit.graph.map.MapEdgeIdentifier;
import com.telenav.mesakit.graph.matching.snapping.GraphSnapper;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.specifications.common.CommonGraph;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.common.graph.store.CommonGraphStore;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.graph.specifications.common.place.PlaceAttributes;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.relation.RelationAttributes;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.common.vertex.VertexAttributes;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeSet;
import com.telenav.mesakit.graph.specifications.library.store.ArchivedGraphStore;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.OsmPbfGraphLoader;
import com.telenav.mesakit.map.data.formats.library.DataFormat;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfRelationIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.core.messaging.context.CallStack.Matching.SUBCLASS;
import static com.telenav.kivakit.core.messaging.context.CallStack.Proximity.DISTANT;
import static com.telenav.kivakit.core.string.AsciiArt.textBox;
import static com.telenav.kivakit.core.string.Formatter.format;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.graph.Metadata.CountType.REQUIRE_EXACT;
import static com.telenav.mesakit.graph.Metadata.VALIDATE_EXCEPT_STATISTICS;
import static com.telenav.mesakit.graph.collections.EdgeSequence.Type.EDGES;
import static com.telenav.mesakit.graph.collections.EdgeSequence.Type.FORWARD_EDGES;

/**
 * The base class for directional graphs representing a road network, composed of {@link GraphElement}s.
 * {@link Vertex}es are connected by {@link Edge}s and {@link EdgeRelation}s relate vertexes and edges to each other to
 * form higher level features like turn restrictions.
 * <p>
 * Graphs support spatial indexing and implement a variety of efficient queries for graph elements as well as simple
 * data access methods that give statistics about the elements and retrieve elements by identifier.
 * <p>
 * All {@link Graph}s implement a {@link DataSpecification}, which determines which attributes are supported by graph
 * elements. The data specification for a graph is one part of its {@link Metadata}, which also specifies the data
 * version, build, supplier, format, spatial precision, size and bounds, as well as statistical information like the
 * number of graph elements of different types.
 * <p>
 * Graphs are abstracted from their underlying store of vertex and edge data, making different implementations possible,
 * for example mock graphs for testing purposes.
 * <p>
 * {@link CommonGraph} is the base graph implementation used by data specifications. Its {@link CommonGraphStore} holds
 * graph data in memory in a compressed form to reduce storage requirements. It also permits fast saving and loading of
 * {@link GraphArchive}s, which are a Kryo-serialized zip file of graph element attributes. More general reading of
 * graphs can be achieved using different subclasses of {@link BaseGraphLoader}.
 * <p>
 * For details on flyweight pattern and how it is used to make graphs efficient, refer to {@link GraphElement}.
 * <p>
 * The basic properties of graphs include various kinds of metadata and attribute support:
 * <p>
 * <b>PropertyMap</b>
 * <ul>
 *     <li>{@link #name()} - The name of the graph, normally derived from the resource filename</li>
 *     <li>{@link #archive()} - The archive that this graph was loaded from, if any</li>
 *     <li>{@link #resource()} - The resource that this graph was loaded from, if any</li>
 *     <li>{@link #metadata()} - {@link Metadata} describing the content and origin of the graph</li>
 *     <li>{@link #dataSpecification()} - The {@link DataSpecification} for data in the graph</li>
 *     <li>{@link #bounds()} - The bounding rectangle that encloses all graph elements</li>
 *     <li>{@link #precision()} - The {@link Precision} of graph data, and in particular polygon data</li>
 *     <li>{@link #supports(Attribute)} - True if the graph supports the given {@link Attribute}</li>
 *     <li>{@link #supportedEdgeAttributes()} - The list of all {@link Edge} attributes in the graph</li>
 *     <li>{@link #supportedPlaceAttributes()} - The list of all {@link Place} attributes in the graph</li>
 *     <li>{@link #supportedRelationAttributes()} - The list of all {@link EdgeRelation} attributes in the graph</li>
 *     <li>{@link #supportedVertexAttributes()} - The list of all {@link Vertex} attributes in the graph</li>
 *     <li>{@link #supportsFullPbfNodeInformation()} - True if the graph contains information for map enhancement</li>
 *     <li>{@link #isEmpty()} - True if the graph has no graph elements</li>
 *     <li>{@link #isOsm()} - True if the graph meets the {@link OsmDataSpecification}</li>
 * </ul>
 * <p>
 * Graphs can be loaded and saved to {@link GraphArchive}s with {@link #load(GraphArchive)} and {@link #save(GraphArchive)}.
 * For a turn-key way to load graphs, see {@link SmartGraphLoader}. This loader can identify and load data stored in
 * all the supported data formats.
 * <p>
 * The methods that load data with {@link GraphLoader} objects are used to load data from some {@link DataSupplier}
 * in a {@link DataFormat} under a {@link DataSpecification}. {@link GraphLoader}s should be of limited use to end-users,
 * but are a useful extension point to import data from new sources.
 * <p>
 * Graphs support a few kinds of "side-files", which are a kind of second data source. Side files can be used to add
 * information like free flow speeds and patterns, trace counts and turns restrictions to a graph at build time.
 * <p>
 * <b>Loading and Saving</b>
 * <ul>
 *     <li>{@link #load(GraphArchive)} - Loads from a .graph file. A better way is to use {@link SmartGraphLoader}</li>
 *     <li>{@link #loadFreeFlow(Resource)} - Loads free flow information from the given side-file</li>
 *     <li>{@link #loadSpeedPattern(Resource)} - Loads speed pattern information from the given side-file</li>
 *     <li>{@link #loadTurnRestrictions(Resource)} - Loads turn restrictions from the given side-file</li>
 *     <li>{@link #loadAll()} - Forces all graph attributes to load into memory</li>
 *     <li>{@link #loadAll(AttributeSet attributes)} - Loads the specified attributes into memory</li>
 *     <li>{@link #loadAllExcept(AttributeSet attributes)} - Loads all attributes except those given into memory</li>
 *     <li>{@link #load(GraphLoader)} - Loads graph data from some data source</li>
 *     <li>{@link #load(GraphLoader, GraphConstraints)} - Loads graph data that meets the given constraints</li>
 *     <li>{@link #save(GraphArchive)} - Saves this graph to the given archive</li>
 *     <li>{@link #archive()} - The archive that this graph was loaded from, if any</li>
 *     <li>{@link #resource()} - The resource that this graph was loaded from, if any</li>
 *     <li>{@link #unload()} - Unloads the graph's attributes from memory</li>
 *     <li>{@link #isUnloaded()} - True if ALL graph attributes are unloaded</li>
 * </ul>
 * <p>
 * Although there are a number of different graph loaders, the easiest to use is the {@link SmartGraphLoader},
 * because it can identify and load graphs from all supported formats.
 * <p>
 * <b>Graph Loaders</b>
 * <ul>
 *     <li>{@link SmartGraphLoader} - An easy way to load any kind of supported graph resource</li>
 *     <li>{@link DecimatingGraphLoader} - Simplifies a graph</li>
 *     <li>{@link CopyingGraphLoader} - Copies one graph to another</li>
 *     <li>{@link OsmPbfGraphLoader} - Loads and processes data from the OpenStreetMap community</li>
 *     <li>{@link ContinentLoader} - Loads data only from a particular {@link Continent}</li>
 *     <li>{@link CountryLoader} - Loads data only from a particular {@link Country}</li>
 *     <li>{@link StateLoader} - Loads data only from a particular {@link State}</li>
 *     <li>{@link MetropolitanAreaLoader} - Loads data from a particular {@link MetropolitanArea}</li>
 * </ul>
 * <p>
 * <b>Graph Creation</b>
 * <ul>
 *     <li>{@link #createCompatible()} - Creates a graph that is compatible with the metadata from this one</li>
 *     <li>{@link #createCompatible(Metadata)} - Creates a graph compatible with the given metadata</li>
 *     <li>{@link #createCompatible(String)} - Creates a compatible graph with the given name</li>
 *     <li>{@link #createCompatible(File)} - Creates a graph using the metadata in the given file</li>
 *     <li>{@link #createConstrained(Matcher)} - Creates a graph constrained to data that matches the given matcher</li>
 *     <li>{@link #createConstrained(GraphConstraints)} - Creates a graph matching the given {@link GraphConstraints}</li>
 *     <li>{@link #createDecimated(Distance, Angle, ProgressReporter)} - Creates a graph that has been simplified</li>
 * </ul>
 * <p>
 * <b>Edges</b>
 * <ul>
 *     <li>{@link #supportedEdgeAttributes()}</li>
 *     <li>{@link #contains(Edge)}</li>
 *     <li>{@link #contains(EdgeIdentifier)}</li>
 *     <li>{@link #edgeCount()}</li>
 *     <li>{@link #edges()}</li>
 *     <li>{@link #edgesMatching(Edge)}</li>
 *     <li>{@link #edgesIntersecting(Rectangle)}</li>
 *     <li>{@link #edgesIntersecting(Rectangle, Matcher)}</li>
 *     <li>{@link #edgeForIdentifier(EdgeIdentifier)}</li>
 *     <li>{@link #edgeForIdentifier(MapEdgeIdentifier)}</li>
 *     <li>{@link #edgeForIdentifier(long)}</li>
 *     <li>{@link #edgeNearest(Location)}</li>
 *     <li>{@link #edgeNearest(Location, Distance)}</li>
 *     <li>{@link #edgeNearest(Location, Distance, Edge.TransportMode)}</li>
 *     <li>{@link #edgeNearest(Location, Distance, Heading, Edge.TransportMode)}</li>
 *     <li>{@link #forwardEdgeCount()}</li>
 *     <li>{@link #forwardEdges()}</li>
 *     <li>{@link #forwardEdgesIntersecting(Rectangle)}</li>
 *     <li>{@link #forwardEdgesIntersecting(Rectangle, Matcher)}</li>
 *     <li>{@link #forwardEdgesNear(Location, Distance)}</li>
 *     <li>{@link #equivalentEdges(Edge)}</li>
 * </ul>
 * <p>
 * <b>Vertexes</b>
 * <ul>
 *     <li>{@link #supportedVertexAttributes()}</li>
 *     <li>{@link #contains(Vertex)}</li>
 *     <li>{@link #contains(VertexIdentifier)}</li>
 *     <li>{@link #vertexCount()}</li>
 *     <li>{@link #vertexes()}</li>
 *     <li>{@link #vertexesInside(Rectangle)}</li>
 *     <li>{@link #vertexesInside(Rectangle, Matcher)}</li>
 *     <li>{@link #vertexForIdentifier(VertexIdentifier)}</li>
 *     <li>{@link #vertexForNodeIdentifier(MapNodeIdentifier)}</li>
 *     <li>{@link #vertexNear(Location, Distance)}</li>
 *     <li>{@link #vertexNear(Location, Distance, RoadFunctionalClass)}</li>
 *     <li>{@link #vertexNearest(Location)}</li>
 *     <li>{@link #vertexNearest(Location, Distance)}</li>
 *     <li>{@link #vertexNearest(Location, Distance, RoadFunctionalClass)}</li>
 * </ul>
 * <p>
 * <b>Relations</b>
 * <ul>
 *     <li>{@link #supportedRelationAttributes()}</li>
 *     <li>{@link #contains(EdgeRelation)}</li>
 *     <li>{@link #contains(MapRelationIdentifier)}</li>
 *     <li>{@link #relationCount()}</li>
 *     <li>{@link #relations()}</li>
 *     <li>{@link #relationForIdentifier(RelationIdentifier)}</li>
 *     <li>{@link #relationForMapRelationIdentifier(MapRelationIdentifier)}</li>
 *     <li>{@link #relationsIntersecting(Rectangle)}</li>
 *     <li>{@link #relationsIntersecting(Rectangle, Matcher)}</li>
 *     <li>{@link #turnRestrictionRelations()}</li>
 *     <li>{@link #restrictionRelationsIntersecting(Rectangle)}</li>
 * </ul>
 * <p>
 * <b>Places</b>
 * <ul>
 *     <li>{@link #supportedPlaceAttributes()}</li>
 *     <li>{@link #contains(PlaceIdentifier)}</li>
 *     <li>{@link #placeCount()}</li>
 *     <li>{@link #placeForIdentifier(PlaceIdentifier)}</li>
 *     <li>{@link #placesWithPopulationOfAtLeast(Count)}</li>
 *     <li>{@link #placesWithNonZeroPopulation()}</li>
 *     <li>{@link #placesInside(Rectangle)}</li>
 *     <li>{@link #placesInside(Region)}</li>
 *     <li>{@link #placeNear(Location)}</li>
 *     <li>{@link #placesNear(Location, Count, Distance, Count)}</li>
 *     <li>{@link #placesNear(Location, Count, Distance, Distance, Count)}</li>
 * </ul>
 * <p>
 * <b>Routes</b>
 * <ul>
 *     <li>{@link #routeForWayIdentifier(MapWayIdentifier)}</li>
 * </ul>
 * <p>
 * <b>Ways</b>
 * <ul>
 *     <li>{@link #wayCount()}</li>
 *     <li>{@link #wayIdentifiers()}</li>
 *     <li>{@link #routeForWayIdentifier(MapWayIdentifier)}</li>
 * </ul>
 * <p>
 * <b>Testing</b>
 * <ul>
 *     <li>{@link #asString()}</li>
 *     <li>{@link #newHeavyWeightEdge(EdgeIdentifier)}</li>
 *     <li>{@link #newHeavyWeightVertex(VertexIdentifier)}</li>
 *     <li>{@link #newHeavyWeightRelation(RelationIdentifier)}</li>
 *     <li>{@link #newHeavyWeightPlace(PlaceIdentifier)}</li>
 * </ul>
 * <p>
 * Graph objects are {@link Repeater}s that broadcast relevant messages to {@link Listener}s during operations such
 * as saving and loading graphs. Graphs support the {@link #equals(Object)} / {@link #hashCode()} contract, but only
 * by comparing metadata. Deeper comparison is available through {@link #differencesFrom(Graph, Rectangle, Maximum)}. Debug
 * information is available through {@link AsIndentedString#asString()}, which is also implemented by graph
 * elements.
 *
 * @author jonathanl (shibo)
 * @see GraphArchive
 * @see DataSpecification
 * @see Metadata
 * @see GraphElement
 * @see Edge
 * @see Vertex
 * @see EdgeRelation
 * @see Place
 * @see GraphStore
 * @see GraphLoader
 * @see CommonGraph
 * @see CommonGraphStore
 * @see Repeater
 * @see Message
 * @see Listener
 * @see AsIndentedString
 */
@SuppressWarnings("unused") public abstract class Graph extends BaseRepeater implements AsIndentedString, Named
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Creates a graph based on the data specification determined for the given input resource
     *
     * @param input The input data
     * @return A graph that is compatible with the data source represented by the input parameter
     */
    public static Graph createCompatible(File input)
    {
        var metadata = Metadata.metadata(input);
        return metadata == null ? null : metadata.newGraph();
    }

    /** The archive that this graph was loaded from, if any */
    private GraphArchive archive;

    /**
     * An attached object, defined by a user application. When this graph is in a compound graph, like a world graph,
     * this can be used to attach which grid cell this graph is in.
     */
    private transient Object attachedObject;

    /** The data specification for data in this graph */
    private final DataSpecification dataSpecification;

    /** A copy of the reference to the graph store's edge attributes for efficiency */
    private final EdgeStore edgeStore;

    /** Size of the graph, if debugging */
    private Bytes estimatedMemorySize;

    /**
     * The store where graph data for this graph is located. The {@link GraphStore} has sub-stores that are subclasses
     * of {@link GraphElementStore} which vary depending on the {@link DataSpecification}.
     */
    private final ArchivedGraphStore graphStore;

    /**
     * Metadata about this graph
     */
    private Metadata metadata;

    /** A copy of the reference to the graph store's place attributes for efficiency */
    private final PlaceStore placeStore;

    /** A copy of the reference to the graph store's edge relation attributes for efficiency */
    private final RelationStore relationStore;

    /** A copy of the reference to the graph store's shape point attributes for efficiency */
    private ShapePointStore shapePointStore;

    /** A copy of the reference to the graph store's vertex attributes for efficiency */
    private final VertexStore vertexStore;

    /**
     * @param metadata The metadata for this graph
     */
    protected Graph(Metadata metadata)
    {
        this.metadata = metadata;
        dataSpecification = metadata.dataSpecification();
        graphStore = (ArchivedGraphStore) dataSpecification.newGraphStore(this);
        listenTo(graphStore);

        edgeStore = graphStore.edgeStore();
        vertexStore = graphStore.vertexStore();
        relationStore = graphStore.relationStore();
        placeStore = graphStore.placeStore();

        // If we are not a world graph, check to see if we were called from a data specification at some point. No graph
        // should ever be constructed except from a data specification.
        assert isComposite() || CallStack.callerOf(DISTANT, SUBCLASS, DataSpecification.class) != null :
                Classes.simpleName(getClass()) + " was not constructed from a data specification";
    }

    /**
     * Returns the archive that this graph was loaded from, if any
     */
    public GraphArchive archive()
    {
        return archive;
    }

    @Override
    public ObjectIndenter asString(Format format, ObjectIndenter indenter)
    {
        indenter.labeled("resource", resource().path().asContraction(100));
        indenter.indented("metadata", () -> metadata().asString(format, indenter));
        return indenter;
    }

    /**
     * Attaches the given user defined object to this graph. This can be used to provide quick access to a value
     * associated with the graph without the use of a map.
     * <p>
     * NOTE: This method is used internally by world graph, so it's not a good idea to use it
     */
    public <T> Graph attachObject(T object)
    {
        attachedObject = object;
        return this;
    }

    /**
     * Returns the user defined object attached to this graph.
     * <p>
     * NOTE: This method is used internally by world graph, so it's not a good idea to use it
     */
    @SuppressWarnings({ "unchecked" })
    public <T> T attachedObject()
    {
        return (T) attachedObject;
    }

    /**
     * Returns batches of edges of the given size
     */
    public Iterator<List<Edge>> batches(Count batchSize)
    {
        return edgeStore.batches(batchSize);
    }

    /**
     * Returns the bounding rectangle that encloses all elements in this graph
     */
    public Rectangle bounds()
    {
        return graphStore.bounds();
    }

    /**
     * Returns a new graph restricted to all the graph elements that intersect the given bounding rectangle
     */
    public final Graph clippedTo(Rectangle bounds)
    {
        return createConstrained(GraphConstraints.ALL.withBounds(bounds));
    }

    /**
     * Close the archive associated with this graph
     */
    public void close()
    {
        archive().close();
    }

    /**
     * Returns true if the given edge is in this graph
     */
    public boolean contains(Edge edge)
    {
        return contains(edge.identifier());
    }

    /**
     * Returns true if the given edge identifier is in this graph
     */
    public final boolean contains(EdgeIdentifier identifier)
    {
        return edgeStore.contains(identifier);
    }

    /**
     * Returns true if the given edge relation is in this graph
     */
    public boolean contains(EdgeRelation relation)
    {
        return contains(relation.identifier());
    }

    /**
     * Returns true if the given node, way or relation identifier is in this graph
     */
    public final boolean contains(MapIdentifier identifier)
    {
        switch (identifier.type())
        {
            case NODE:
                return contains((PbfNodeIdentifier) identifier);

            case WAY:
                return contains((PbfWayIdentifier) identifier);

            case RELATION:
                return contains((PbfRelationIdentifier) identifier);

            case INVALID:
            default:
                fail();
                return false;
        }
    }

    /**
     * Returns true if the given node identifier is in this graph
     */
    public final boolean contains(MapNodeIdentifier identifier)
    {
        return vertexStore.contains(identifier);
    }

    /**
     * Returns true if the given relation identifier is in this graph
     */
    public final boolean contains(MapRelationIdentifier identifier)
    {
        return relationStore.contains(identifier);
    }

    /**
     * Returns true if the given way identifier is in this graph
     */
    public final boolean contains(PbfWayIdentifier identifier)
    {
        return edgeStore.contains(identifier);
    }

    /**
     * Returns true if the given place identifier is in this graph
     */
    public final boolean contains(PlaceIdentifier identifier)
    {
        return placeStore.containsIdentifier(identifier);
    }

    /**
     * Returns true if the given vertex is in this graph
     */
    public boolean contains(Vertex vertex)
    {
        return vertexStore.contains(vertex);
    }

    /**
     * Returns true if the given vertex identifier is in this graph
     */
    public final boolean contains(VertexIdentifier identifier)
    {
        return vertexStore.contains(identifier);
    }

    /**
     * Returns an empty graph that is compatible with this graph's data specification and which has the same tag codecs
     * and other special attributes.
     */
    public final Graph createCompatible()
    {
        return createCompatible(metadata());
    }

    /**
     * Returns an empty graph that is compatible with the data specification in the given metadata.
     */
    public final Graph createCompatible(Metadata metadata)
    {
        return metadata.newGraph();
    }

    /**
     * Returns an empty graph that is compatible with this graph. Any special attributes like tag codecs will be copied
     * into the clone.
     */
    public final Graph createCompatible(String name)
    {
        return createCompatible(metadata().withName(name));
    }

    /**
     * A new graph that contains graph elements from this graph which match the given constraints.
     *
     * @param constraints The graph constraints to apply
     * @return A copy of this graph limited to the graph elements that match the constraints
     */
    public final Graph createConstrained(GraphConstraints constraints)
    {
        // Create constrained graph to copy this graph into
        var constrained = listenTo(createCompatible(metadata().withName(name() + "-constrained")));

        // Load the constrained graph with information from this graph
        var loader = listenTo(new CopyingGraphLoader(this));
        var metadata = constrained.load(loader, constraints);
        return metadata == null || metadata.edgeCount(REQUIRE_EXACT).isZero() ? null : constrained;
    }

    /**
     * Returns a copy of this graph containing only edges that match the given edge matcher
     */
    public final Graph createConstrained(Matcher<Edge> matcher)
    {
        return createConstrained(GraphConstraints.ALL.withEdgeMatcher(matcher).withoutEdgeRelations());
    }

    /**
     * Decimates this graph. Decimation is used to reduce the data present when viewing a graph from a low level of
     * zooming (from far out). The more zoomed out you are, the fewer details are visible, so processing required to
     * render the data can be reduced.
     *
     * @param minimumLength The desired minimum length of edges to be included in the decimated graph
     * @param maximumDeviation The maximum amount of turning the edges can make before being decimated
     * @return This graph, decimated
     */
    public final Graph createDecimated(Distance minimumLength,
                                       Angle maximumDeviation,
                                       ProgressReporter reporter)
    {
        var decimated = createCompatible(metadata().withName(name() + "-decimated"));
        decimated.addListener(this);
        decimated.load(new DecimatingGraphLoader(this, minimumLength, maximumDeviation, reporter), GraphConstraints.ALL);
        return decimated;
    }

    /**
     * Returns the data specification for this graph
     *
     * @see DataSpecification
     */
    public DataSpecification dataSpecification()
    {
        return dataSpecification;
    }

    /**
     * Returns the deep differences between this graph and the given graph within the given bounds.
     */
    public final Differences differencesFrom(Graph that, Rectangle bounds, Maximum maximumDifferences)
    {
        var differences = new Differences();
        var progress = BroadcastingProgressReporter.progressReporter(this);

        // Then compare edge counts
        if (!edgeCount().equals(that.edgeCount()))
        {
            var thisCount = Count.count(edges().within(bounds));
            var thatCount = Count.count(that.edges().within(bounds));
            if (!thisCount.equals(thatCount))
            {
                differences.add("Graph '" + name() + "' has " + thisCount + " edges, while graph '" + that.name() + "' has " + thatCount);
            }
        }

        // Compare all edges in this graph with corresponding edges in that graph
        progress = progress.withUnits("edges");
        progress.feedback("Comparing edges");
        progress.start();
        for (var edge : edges().within(bounds))
        {
            progress.next();
            var thatEdge = that.edgeForIdentifier(edge.identifier());
            if (thatEdge != null)
            {
                var edgeDifferences = edge.differencesFrom(thatEdge);
                if (edgeDifferences.isDifferent())
                {
                    differences.add(edge + " property differences: " + differences);
                }
            }
            else
            {
                differences.add(edge + " exists in graph + '" + name() + "', but doesn't exist in graph '" + that.name() + "'");
            }
        }
        progress.end();

        // Record all edges in that graph which don't exist in this graph
        for (var edge : that.edges().within(bounds))
        {
            if (edgeForIdentifier(edge.identifier()) == null)
            {
                differences.add("Edge " + edge + " exists in graph + '" + that.name() + "', but doesn't exist in graph '" + name() + "'");
            }
        }

        // Validate that the edge spatial indexes are exactly the same
        if (!edgeStore.spatialIndex().equals(that.edgeStore.spatialIndex()))
        {
            differences.add("Edge spatial index in graph " + name() + " is different from spatial index in graph " + that.name());
        }

        progress = progress.withUnits("places");
        progress.feedback("Comparing places");
        progress.reset();
        for (var place : placesInside(bounds))
        {
            progress.next();
            var thatPlace = that.placeForIdentifier(place.identifier());
            if (thatPlace != null)
            {
                var placeDifferences = place.differences(thatPlace);
                if (placeDifferences.isDifferent())
                {
                    differences.add(place + " property differences: " + placeDifferences);
                }
            }
            else
            {
                differences.add(place + " exists in graph + '" + name() + "', but doesn't exist in graph '" + that.name() + "'");
            }
        }
        progress.end();

        return differences;
    }

    /**
     * Returns the number of edges in this graph
     */
    public Count edgeCount()
    {
        return edgeStore.retrieveCount();
    }

    /**
     * @param identifier An edge identifier
     * @return The edge for the given identifier
     */
    public Edge edgeForIdentifier(EdgeIdentifier identifier)
    {
        if (edgeStore.contains(identifier))
        {
            return newEdge(identifier);
        }
        return fail("No edge found for identifier " + identifier);
    }

    /**
     * @param identifier An edge identifier
     * @return The edge for the given identifier
     */
    public final Edge edgeForIdentifier(long identifier)
    {
        if (edgeStore.containsIdentifier(identifier))
        {
            return dataSpecification.newEdge(this, identifier);
        }
        return fail("No edge found for identifier " + identifier);
    }

    /**
     * @param identifier A map edge identifier of the form [way-identifier]:[from-node-identifier]:[to-node-identifier]
     * @return The graph edge for the given map "edge" identifier, if any
     */
    public final Edge edgeForIdentifier(MapEdgeIdentifier identifier)
    {
        if (supports(EdgeAttributes.get().FROM_NODE_IDENTIFIER)
                && supports(EdgeAttributes.get().TO_NODE_IDENTIFIER))
        {
            var edgeIdentifier = new EdgeIdentifier(
                    identifier.way().asLong() * EdgeIdentifier.SEQUENCE_NUMBER_SHIFT);
            var from = identifier.from();
            var to = identifier.to();

            Edge edge;
            while (contains(edgeIdentifier))
            {
                edge = edgeForIdentifier(edgeIdentifier);
                if (edge.fromNodeIdentifier().equals(from) && edge.toNodeIdentifier().equals(to))
                {
                    return edge;
                }
                if (edge.isTwoWay() && edge.fromNodeIdentifier().equals(to)
                        && edge.toNodeIdentifier().equals(from))
                {
                    return edge.reversed();
                }
                edgeIdentifier = edgeIdentifier.next();
            }
        }
        return null;
    }

    /**
     * Returns the closest edge to the given location
     */
    public final Edge edgeNearest(Location location)
    {
        return edgeNearest(location, Distance.meters(1000));
    }

    /**
     * Returns the closest edge to the given location
     */
    public final Edge edgeNearest(Location location, Distance near)
    {
        return edgeNearest(location, near, null, Edge.TransportMode.ANY);
    }

    /**
     * Returns the closest edge to the given location
     */
    public final Edge edgeNearest(Location location, Distance near, Edge.TransportMode mode)
    {
        return edgeNearest(location, near, null, mode);
    }

    /**
     * Returns the edge store used to store edge information, generally in a compressed format in memory.
     */
    public final EdgeStore edgeStore()
    {
        return edgeStore;
    }

    /**
     * Returns a sequence of all edges in this graph
     */
    public EdgeSequence edges()
    {
        return edgeStore.edges();
    }

    /**
     * Returns the edges whose road shape intersects the given bounding rectangle
     */
    public final EdgeSequence edgesIntersecting(Rectangle bounds)
    {
        return edgesIntersecting(bounds, Filter.acceptAll(), EDGES);
    }

    /**
     * Returns the edges whose road shape intersects the given bounding rectangle which also match the given matcher
     */
    public EdgeSequence edgesIntersecting(Rectangle bounds, Matcher<Edge> matcher)
    {
        return edgesIntersecting(bounds, matcher, EDGES);
    }

    /**
     * Returns the edges whose road shape intersects the given bounding rectangle which also match the given matcher
     */
    public EdgeSequence edgesIntersecting(Rectangle bounds, Matcher<Edge> matcher,
                                          EdgeSequence.Type type)
    {
        return unsupported();
    }

    /**
     * @param identifier A map identifier (node, way or relation) with the identifier type encoded in the high bits
     * @return The graph element associated with the identifier. In the case of a node, this would be a {@link Vertex},
     * in the case of a way, a {@link Route} and in the case of a relation, an {@link EdgeRelation}.
     */
    public final GraphElement elementForMapIdentifier(MapIdentifier identifier)
    {
        switch (identifier.type())
        {
            case NODE:
                return vertexForNodeIdentifier((MapNodeIdentifier) identifier);

            case WAY:
                var route = routeForWayIdentifier((PbfWayIdentifier) identifier);
                if (route != null)
                {
                    return route.first();
                }
                return null;

            case RELATION:
                return relationForMapRelationIdentifier((RelationIdentifier) identifier);

            case INVALID:
            default:
                return fail();
        }
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Graph that)
        {
            return metadata().equals(that.metadata());
        }
        return false;
    }

    /**
     * Returns the edge in this graph that is equivalent to the given edge (probably an edge in another graph)
     */
    public final EdgeSet equivalentEdges(Edge edge)
    {
        var matches = edgesMatching(edge);
        if (matches.isEmpty())
        {
            if (edge.isTwoWay())
            {
                matches = edgesMatching(edge.reversed());
            }
        }
        return matches;
    }

    /**
     * @param size An estimate of the in-memory size of this graph
     */
    public final void estimatedMemorySize(Bytes size)
    {
        estimatedMemorySize = size;
    }

    /**
     * Returns the number of forward edges in this graph
     */
    public Count forwardEdgeCount()
    {
        return edgeStore.retrieveForwardEdgeCount();
    }

    /**
     * Returns the forward (non-reversed) edges in this graph. All forward edges have positive edge identifiers. All
     * reversed edges have negative edge identifiers.
     */
    public EdgeSequence forwardEdges()
    {
        return edgeStore.forwardEdges();
    }

    /**
     * Returns the forward (non-reversed) edges intersecting the given bounds which match the given predicate.
     */
    public EdgeSequence forwardEdgesIntersecting(Rectangle bounds, Matcher<Edge> matcher)
    {
        return edgesIntersecting(bounds, matcher, FORWARD_EDGES);
    }

    /**
     * Returns forward (non-reversed) edges that intersect the given bounds
     */
    public final EdgeSequence forwardEdgesIntersecting(Rectangle bounds)
    {
        return forwardEdgesIntersecting(bounds, Filter.acceptAll());
    }

    /**
     * Returns the number of forward edges within the given distance of the given location
     */
    public final EdgeSequence forwardEdgesNear(Location location, Distance distance)
    {
        return forwardEdgesIntersecting(location.within(distance));
    }

    /**
     * Returns the store that is holding all of this graph's attributes
     *
     * @see GraphStore
     */
    public ArchivedGraphStore graphStore()
    {
        return graphStore;
    }

    public boolean hasTags()
    {
        return supports(GraphElementAttributes.get().TAGS);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(metadata().hashCode());
    }

    /**
     * Returns true if this graph is composed of sub-graphs (as in a WorldGraph)
     */
    public boolean isComposite()
    {
        return false;
    }

    /**
     * Returns true if there is no data in this graph
     */
    public final boolean isEmpty()
    {
        return edgeCount().equals(Count._0);
    }

    /**
     * Returns true if this is graph meets the {@link OsmDataSpecification}
     */
    public final boolean isOsm()
    {
        return dataSpecification.isOsm();
    }

    /**
     * Returns true if the graph store for this graph is not loaded
     */
    public boolean isUnloaded()
    {
        return graphStore.isUnloaded();
    }

    /**
     * Loads this graph with data using the given graph loader
     *
     * @return Metadata describing the data that was loaded
     */
    public final Metadata load(GraphLoader loader)
    {
        return load(loader, GraphConstraints.ALL);
    }

    /**
     * Loads this graph with data from the given graph loader using the given constraints. The store is marked as locked
     * when the operation completes and no further data can be loaded.
     *
     * @param loader The graph loader to use to populate this graph
     * @param constraints Restrictions that determine what graph elements to include
     * @return Metadata describing what was loaded into the graph
     */
    public final Metadata load(GraphLoader loader, GraphConstraints constraints)
    {
        // Start loading
        var start = Time.now();
        graphStore.loading(loader.resource());

        try
        {
            // Load the graph
            var metadata = loader.onLoad(graphStore, constraints);
            if (metadata != null && metadata.edgeCount(ALLOW_ESTIMATE).isNonZero()
                    && metadata.validator(loader.validation()).validate(this))
            {
                // flush any queued changes to the graph store,
                graphStore.flush();

                // and if at least one edge was loaded,
                var edgeCount = metadata.edgeCount(REQUIRE_EXACT);
                if (edgeCount != null && !edgeCount.isZero())
                {
                    // let the loader commit any final changes,
                    loader.onCommit(graphStore);

                    // commit the graph store,
                    graphStore.commit();

                    // tell the graph store we're done loading,
                    graphStore.loaded(loader.resource());

                    // update the metadata for the graph,
                    metadata(metadata
                            .withVertexCount(graphStore.vertexStore().retrieveCount())
                            .withEdgeRelationCount(graphStore.relationStore().retrieveCount())
                            .withEdgeCount(graphStore.edgeStore().retrieveCount())
                            .withForwardEdgeCount(graphStore.edgeStore().retrieveForwardEdgeCount()));

                    // and if the graph store is valid,
                    if (graphStore.validator(loader.validation()).validate(this))
                    {
                        // we have succeeded in loading the graph,
                        information(textBox(format("${class} loaded $ in $", loader.getClass(),
                                metadata().descriptor(), start.elapsedSince()), asString()));

                        // so return its metadata.
                        return metadata();
                    }
                }
            }
        }
        catch (Exception e)
        {
            problem(e, "Unable to load $", loader.resource());
            return null;
        }

        DEBUG.trace("Unable to load $", name());
        return null;
    }

    /**
     * Loads a graph from the given .graph archive.
     *
     * @param archive The archive to load
     * @see #load(GraphArchive)
     */
    @MustBeInvokedByOverriders
    public void load(GraphArchive archive)
    {
        this.archive = archive;
    }

    /**
     * Forces any lazy-loaded data on disk to load into memory
     */
    public Graph loadAll()
    {
        // Start timer
        var start = Time.now();

        graphStore.loadAll();

        DEBUG.trace("Graph.loadAll() completed in ${debug}", start.elapsedSince());
        return this;
    }

    /**
     * Forces any lazy-loaded data on disk to load into memory
     */
    public Graph loadAll(AttributeSet attributes)
    {
        // Start timer
        var start = Time.now();
        graphStore.loadAll(attributes);
        DEBUG.trace("Graph.loadAll($) completed in ${debug}", attributes, start.elapsedSince());
        return this;
    }

    /**
     * Forces any lazy-loaded data on disk to load into memory
     */
    public Graph loadAllExcept(AttributeSet attributes)
    {
        // Start timer
        var start = Time.now();
        graphStore.loadAllExcept(attributes);
        DEBUG.trace("Graph.loadAllExcept($) completed in ${debug}", attributes, start.elapsedSince());
        return this;
    }

    public void loadFreeFlow(Resource resource)
    {
    }

    public void loadSpeedPattern(Resource resource)
    {
    }

    public void loadTurnRestrictions(Resource resource)
    {
    }

    /**
     * Returns information about the data in this graph
     */
    public final Metadata metadata()
    {
        return metadata;
    }

    /**
     * Called to update the metadata for this graph when a graph is loaded by a {@link GraphLoader}.
     */
    public final Metadata metadata(Metadata metadata)
    {
        metadata.assertValid(VALIDATE_EXCEPT_STATISTICS);
        this.metadata = metadata;
        return metadata;
    }

    /** Sets the name of this graph */
    public final Graph name(String name)
    {
        metadata(metadata().withName(name));
        return this;
    }

    /**
     * Returns the name of this graph from the graph {@link Metadata}.
     */
    @Override
    public String name()
    {
        var metadata = metadata();
        return metadata == null ? NamedObject.syntheticName(this) : metadata.name();
    }

    /**
     * Returns a new {@link Edge} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public <T extends Edge> T newEdge(EdgeIdentifier identifier)
    {
        return (T) dataSpecification.newEdge(this, identifier.asLong());
    }

    /**
     * Returns a new {@link HeavyWeightEdge} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public final <T extends HeavyWeightEdge> T newHeavyWeightEdge(EdgeIdentifier identifier)
    {
        return (T) dataSpecification.newHeavyWeightEdge(this, identifier.asLong());
    }

    /**
     * Returns a new {@link HeavyWeightPlace} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings({ "unchecked" })
    public final <T extends HeavyWeightPlace> T newHeavyWeightPlace(PlaceIdentifier identifier)
    {
        return (T) dataSpecification.newHeavyWeightPlace(this, identifier.asLong());
    }

    /**
     * Returns a new {@link HeavyWeightRelation} with the given identifier, created from this graph's data
     * specification
     */
    @SuppressWarnings("unchecked")
    public final <T extends HeavyWeightRelation> T newHeavyWeightRelation(RelationIdentifier identifier)
    {
        return (T) dataSpecification.newHeavyWeightRelation(this, identifier.asLong());
    }

    /**
     * Returns a new {@link HeavyWeightVertex} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public final <T extends HeavyWeightVertex> T newHeavyWeightVertex(VertexIdentifier identifier)
    {
        return (T) dataSpecification.newHeavyWeightVertex(this, identifier.asLong());
    }

    /**
     * Returns a new {@link Place} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public <T extends Place> T newPlace(PlaceIdentifier identifier)
    {
        return (T) dataSpecification.newPlace(this, identifier.asLong());
    }

    /**
     * Returns a new {@link EdgeRelation} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public <T extends EdgeRelation> T newRelation(RelationIdentifier identifier)
    {
        return (T) dataSpecification.newRelation(this, identifier.asLong());
    }

    /**
     * Returns a new {@link ShapePoint} with the given identifier, created from this graph's data specification
     */
    public final ShapePoint newShapePoint(ShapePointIdentifier identifier, Location location)
    {
        var point = new ShapePoint(this, identifier.asLong());
        point.location(location.asLong());
        return point;
    }

    /**
     * Returns a new {@link Vertex} with the given identifier, created from this graph's data specification
     */
    @SuppressWarnings("unchecked")
    public <T extends Vertex> T newVertex(VertexIdentifier identifier)
    {
        return (T) dataSpecification.newVertex(this, identifier.asLong());
    }

    /**
     * Returns a parallel {@link Stream} of edges in this graph
     */
    public final Stream<Edge> parallelStream()
    {
        return Streams.parallelStream(edges());
    }

    /**
     * w
     *
     * @return The number of places in this graph
     */
    public Count placeCount()
    {
        return placeStore.retrieveCount();
    }

    /**
     * Returns the place for the given identifier, if any
     */
    public final Place placeForIdentifier(PlaceIdentifier identifier)
    {
        if (placeStore.containsIdentifier(identifier))
        {
            return newPlace(identifier);
        }
        return fail("No place found for identifier " + identifier);
    }

    /**
     * Returns any place within 5 meters of the given location
     */
    public Place placeNear(Location location)
    {
        var places = placesInside(Rectangle.fromLocation(location).expanded(Distance.meters(5)));
        for (var place : places)
        {
            if (place.location().equals(location))
            {
                return place;
            }
        }
        return null;
    }

    /**
     * Returns the place sub-store of the graph store
     */
    public final PlaceStore placeStore()
    {
        return placeStore;
    }

    /**
     * Returns the places in this graph
     */
    public Iterable<Place> places()
    {
        return placeStore;
    }

    /**
     * Returns the places inside the given bounding rectangle
     */
    public Iterable<Place> placesInside(Rectangle bounds)
    {
        return unsupported();
    }

    /**
     * Returns the places inside the given region
     */
    public final Iterable<Place> placesInside(Region<?> region)
    {
        return Iterables.iterable(() -> new NextIterator<>()
        {
            final Iterator<Place> places = placesInside(region.bounds()).iterator();

            @Override
            public Place next()
            {
                while (places.hasNext())
                {
                    var place = places.next();
                    if (region.contains(place.location()))
                    {
                        return place;
                    }
                }
                return null;
            }
        });
    }

    /**
     * Returns the closest maximum number of places to the given location within the given maximum distance with the
     * given minimum population
     */
    public final Iterable<Place> placesNear(Location location, Count minimumPopulation,
                                            Distance maximumDistance, Count maximum)
    {
        return placesNear(location, minimumPopulation, Distance.ZERO, maximumDistance, maximum);
    }

    /**
     * Returns the closest maximum places from a minimum distance to the given location within the given maximum
     * distance with the given minimum population
     */
    @SuppressWarnings("SameParameterValue")
    public List<Place> placesNear(Location location, Count minimumPopulation,
                                  Distance minimumDistanceBetweenPlaces,
                                  Distance maximumDistance, Count maximum)
    {
        List<Place> places = new ArrayList<>();
        for (var place : placesInside(location.bounds().expanded(maximumDistance)))
        {
            if (place.population().isGreaterThanOrEqualTo(minimumPopulation))
            {
                if (location.distanceTo(place.location()).isLessThan(maximumDistance)
                        && location.distanceTo(place.location()).isGreaterThanOrEqualTo(minimumDistanceBetweenPlaces))
                {
                    places.add(place);
                }
            }
        }
        places.sort(Comparator.comparing(a -> location.distanceTo(a.location())));
        return places.subList(0, Math.min(places.size(), maximum.asInt() + 1));
    }

    /**
     * Returns a list of places in this graph with a non-zero population
     */
    public final List<Place> placesWithNonZeroPopulation()
    {
        return placesWithPopulationOfAtLeast(Count._1);
    }

    /**
     * Returns a list of places in this graph with at least the given population
     */
    public final List<Place> placesWithPopulationOfAtLeast(Count minimumPopulation)
    {
        return Streams.stream(places()).filter(place -> place.population().isGreaterThanOrEqualTo(minimumPopulation))
                .collect(Collectors.toList());
    }

    /**
     * Returns the data {@link Precision} of elements in this graph (from DM5 to DM7)
     */
    public Precision precision()
    {
        return metadata.dataPrecision();
    }

    /**
     * Returns the number of edge relations in this graph
     */
    public Count relationCount()
    {
        return relationStore.retrieveCount();
    }

    /**
     * Returns the relation for the given identifier
     */
    public EdgeRelation relationForIdentifier(RelationIdentifier identifier)
    {
        if (relationStore.contains(identifier))
        {
            return newRelation(identifier);
        }
        return fail("No edge relation found for identifier " + identifier);
    }

    /**
     * Returns the relation for the given map relation identifier, such as a PBF relation id.
     */
    public final EdgeRelation relationForMapRelationIdentifier(MapRelationIdentifier identifier)
    {
        return relationStore.relationForIdentifier(identifier);
    }

    /**
     * Returns the graph store's relation sub-store
     */
    public final RelationStore relationStore()
    {
        return relationStore;
    }

    /**
     * Returns the edge relations in this graph
     */
    public Iterable<EdgeRelation> relations()
    {
        return relationStore;
    }

    /**
     * Returns the edge relations that intersect the given bounding rectangle
     */
    public final RelationSet relationsIntersecting(Rectangle bounds)
    {
        return relationsIntersecting(bounds, Filter.acceptAll());
    }

    /**
     * Returns the set of relations inside the given bounds that match the given matcher predicate
     */
    public RelationSet relationsIntersecting(Rectangle bounds, Matcher<EdgeRelation> matcher)
    {
        var relations = new RelationSet(Estimate._1024);
        for (var edge : edgesIntersecting(bounds))
        {
            for (var relation : edge.relations())
            {
                if (matcher.matches(relation))
                {
                    relations.add(relation);
                }
            }
        }
        return relations;
    }

    /**
     * Returns any resource this graph was loaded from
     */
    public Resource resource()
    {
        if (archive != null)
        {
            return archive.resource();
        }
        return null;
    }

    /**
     * Returns restriction relations intersecting the given bounds
     */
    public final Set<EdgeRelation> restrictionRelationsIntersecting(Rectangle bounds)
    {
        Set<EdgeRelation> restrictions = new HashSet<>();
        for (var relation : relationsIntersecting(bounds))
        {
            if (relation.isRestriction())
            {
                restrictions.add(relation);
            }
        }
        return restrictions;
    }

    /**
     * Returns the sequence of edges that covers the given way
     */
    public Route routeForWayIdentifier(MapWayIdentifier wayIdentifier)
    {
        return edgeStore.retrieveRouteForWayIdentifier(wayIdentifier);
    }

    /**
     * Saves this graph to a .graph archive for future loading by {@link #load(GraphArchive)}. This method must be
     * overridden by the subclass to provide the implementation.
     *
     * @param archive The resource to write to
     */
    @MustBeInvokedByOverriders
    public void save(GraphArchive archive)
    {
    }

    /**
     * Returns the shape point at the given location
     */
    public final ShapePoint shapePointForLocation(Location location)
    {
        return shapePointStore().forLocation(location);
    }

    /**
     * Returns the graph store's shape point sub-store
     */
    public final ShapePointStore shapePointStore()
    {
        if (shapePointStore == null)
        {
            shapePointStore = graphStore.shapePointStore();
        }
        return shapePointStore;
    }

    /**
     * Returns shape points for the given polyline
     */
    public final List<ShapePoint> shapePoints(Polyline line)
    {
        List<ShapePoint> points = new ArrayList<>();
        if (supportsFullPbfNodeInformation())
        {
            for (var location : line)
            {
                var point = shapePointForLocation(location);
                if (point != null)
                {
                    points.add(point);
                }
            }
        }
        return points;
    }

    /**
     * Returns a {@link Stream} of the edges in this graph
     */
    public final Stream<Edge> stream()
    {
        return Streams.stream(edges());
    }

    /**
     * Returns the edge attributes that are supported by this graph.
     */
    public final EdgeAttributes supportedEdgeAttributes()
    {
        return dataSpecification.edgeAttributes();
    }

    /**
     * Returns the place attributes supported by this graph
     */
    public final PlaceAttributes supportedPlaceAttributes()
    {
        return dataSpecification.placeAttributes();
    }

    /**
     * Returns the relation attributes supported by this graph
     */
    public final RelationAttributes supportedRelationAttributes()
    {
        return dataSpecification.relationAttributes();
    }

    /**
     * Returns the vertex attributes supported in this graph
     */
    public final VertexAttributes supportedVertexAttributes()
    {
        return dataSpecification.vertexAttributes();
    }

    /**
     * Returns true if this graph supports the given attribute
     */
    public boolean supports(Attribute<?> attribute)
    {
        return dataSpecification.supports(attribute);
    }

    /**
     * Returns true if this graph contains all PBF node identifier information (both node identifiers and node tags).
     * Normally this information is not available in a graph, but in the case of graph editing by the OpenTerra team, it
     * is necessary for graphs to contain all information in the PBF source file or the data cannot be uploaded to the
     * OSM community. In general this information is voluminous and so it is stored on disk, and it is generally a good
     * idea not to make large graphs with this level of detail.
     */
    public boolean supportsFullPbfNodeInformation()
    {
        return vertexStore.containsAllPbfNodeInformation() && vertexStore.supportsAllNodeTags();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return metadata().toString();
    }

    /**
     * Returns turn restriction {@link EdgeRelation}s in this graph
     */
    public final Set<EdgeRelation> turnRestrictionRelations()
    {
        Set<EdgeRelation> turnRestrictions = new HashSet<>();
        for (var relation : relations())
        {
            if (relation.isTurnRestriction())
            {
                turnRestrictions.add(relation);
            }
        }
        return turnRestrictions;
    }

    /**
     * Unloads data in the graph store for this graph to reduce memory consumption
     */
    public void unload()
    {
        graphStore.unload();
    }

    public Version version()
    {
        if (archive() != null)
        {
            return archive().version();
        }
        return GraphArchive.VERSION;
    }

    /**
     * Returns the number of vertexes in this graph
     */
    public Count vertexCount()
    {
        return vertexStore.retrieveCount();
    }

    /**
     * @param identifier A vertex identifier
     * @return The vertex for the given identifier, or null if the vertex does not exist in this graph
     */
    public Vertex vertexForIdentifier(VertexIdentifier identifier)
    {
        if (contains(identifier))
        {
            return newVertex(identifier);
        }
        problem("Vertex $ not found in vertex store of $ elements", identifier, vertexStore().size());
        return null;
    }

    /**
     * Returns the vertex for the given node identifier, if any
     */
    public final Vertex vertexForNodeIdentifier(MapNodeIdentifier identifier)
    {
        return vertexStore.vertexForNodeIdentifier(identifier.asLong());
    }

    /**
     * Returns any vertex within the given distance of the given location. It's not guaranteed that this is the closest
     * vertex to the location, only that it is within the given distance.
     */
    public Vertex vertexNear(Location location, Distance near)
    {
        for (var vertex : vertexesInside(location.bounds().expanded(near)))
        {
            return vertex;
        }
        return null;
    }

    /**
     * Returns a vertex within the given distance of the given location that is connected to at least one edge with the
     * given road functional class.
     */
    public final Vertex vertexNear(Location location, Distance near,
                                   RoadFunctionalClass minimumFunctionalClass)
    {
        for (var vertex : vertexesInside(location.bounds().expanded(near)))
        {
            if (vertex.maximumRoadFunctionalClass().isMoreImportantThanOrEqual(minimumFunctionalClass))
            {
                return vertex;
            }
        }
        return null;
    }

    /**
     * Returns the vertex closest to the location (but no further than 1km)
     */
    public final Vertex vertexNearest(Location location)
    {
        return vertexNearest(location, Distance.ONE_KILOMETER);
    }

    /**
     * Returns the vertex nearest to the given location, within the maximum distance
     */
    public Vertex vertexNearest(Location location, Distance maximum)
    {
        return vertexNearest(location, maximum, RoadFunctionalClass.FOURTH_CLASS);
    }

    /**
     * Returns the vertex nearest to the given location, within the maximum distance
     */
    @SuppressWarnings("SameParameterValue")
    public Vertex vertexNearest(Location location, Distance maximum,
                                RoadFunctionalClass minimumFunctionalClass)
    {
        // The nearest vertex
        Vertex closest = null;
        var closestDistance = Distance.MAXIMUM;

        // Look in 100 meter increments, so we don't search too wide an area
        for (var near = maximum.minimum(Distance.meters(100)); near
                .isLessThanOrEqualTo(maximum); near = near.add(Distance.meters(100)))
        {
            // Go through candidates at the given (rectangular) distance
            for (var candidate : vertexesInside(location.within(near)))
            {
                // If the candidate vertex' most important edge has the minimum importance
                if (candidate.maximumRoadFunctionalClass().isMoreImportantThanOrEqual(minimumFunctionalClass))
                {
                    // If this is the first candidate or the distance of the candidate is closer
                    var candidateDistance = candidate.location().distanceTo(location);
                    if (closest == null || candidateDistance.isLessThan(closestDistance))
                    {
                        // then this is now the closest
                        closest = candidate;
                        closestDistance = candidateDistance;
                    }
                }
            }

            // If we found the closest vertex
            if (closest != null)
            {
                // then return it (otherwise expand the search area)
                return closest;
            }
        }
        return null;
    }

    /**
     * Returns the vertex sub-store of the graph store for this graph
     */
    public final VertexStore vertexStore()
    {
        return vertexStore;
    }

    /**
     * Returns a sequence of all vertexes in this graph
     */
    public VertexSequence vertexes()
    {
        return new VertexSequence(vertexStore);
    }

    /**
     * Returns the vertexes inside the given bounding rectangle
     */
    public VertexSequence vertexesInside(Rectangle bounds)
    {
        return unsupported();
    }

    /**
     * Returns the vertexes inside the given bounding rectangle that match the given matcher
     */
    public VertexSequence vertexesInside(Rectangle bounds, Matcher<Vertex> matcher)
    {
        return unsupported();
    }

    /**
     * Returns the number of ways in this graph
     */
    public final Count wayCount()
    {
        return metadata.wayCount(REQUIRE_EXACT);
    }

    /**
     * Returns the set of way identifiers in this graph
     */
    public final Set<PbfWayIdentifier> wayIdentifiers()
    {
        Set<PbfWayIdentifier> ways = new HashSet<>();
        for (var edge : forwardEdges())
        {
            ways.add(edge.wayIdentifier());
        }
        return ways;
    }

    /**
     * Returns the closest edge to the given location
     */
    @SuppressWarnings("SameParameterValue")
    private Edge edgeNearest(Location location, Distance near, Heading heading,
                             Edge.TransportMode mode)
    {
        var snap = new GraphSnapper(this, near, null, mode).snap(location, heading);
        if (snap != null)
        {
            return snap.closestEdge();
        }
        return null;
    }

    /**
     * Returns the set of edges matching the given edge
     */
    private EdgeSet edgesMatching(Edge edge)
    {
        var matches = new EdgeSet();
        var from = vertexForNodeIdentifier(edge.fromNodeIdentifier());
        if (from != null)
        {
            for (var out : from.outEdges())
            {
                if (out.toLocation().equals(edge.toLocation()))
                {
                    if (out.roadShape().equals(edge.roadShape()))
                    {
                        matches.add(out);
                    }
                }
            }
        }
        return matches;
    }
}
