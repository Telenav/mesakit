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

package com.telenav.mesakit.graph.specifications.common.vertex.store;

import com.telenav.kivakit.collections.iteration.iterables.DeduplicatingIterable;
import com.telenav.kivakit.kernel.data.validation.Validation;
import com.telenav.kivakit.kernel.data.validation.Validator;
import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.BitCount;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.primitive.collections.array.packed.SplitPackedArray;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.kivakit.primitive.collections.map.scalars.IntToByteMap;
import com.telenav.kivakit.primitive.collections.set.LongSet;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphNode;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.project.GraphCoreLimits;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.common.node.store.NodeStore;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.common.vertex.VertexAttributes;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.graph.specifications.library.store.ArchivedGraphStore;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfStopProcessingException;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.road.model.GradeSeparation;

import java.util.Collections;
import java.util.Iterator;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.primitive.collections.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;

/**
 * Store of {@link Vertex} information, including edge connectivity (in and out edges, as stored in the {@link
 * ConnectivityStore} associated with this store) and clip state.
 * <p>
 * A vertex store has two phases. In the first phase, information is added to temporary data structures in the {@link
 * ConnectivityStore} that track one-way in, one-way out and two-way edges as they are added to the store. In the second
 * phase, {@link VertexStore#onCommit()} is called and this temporary information is used to create {@link Vertex}
 * objects and add them to the vertex store.
 *
 * @author jonathanl (shibo)
 * @see VertexAttributes
 */
@SuppressWarnings("unused")
public class VertexStore extends NodeStore<Vertex>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** Edge and vertex connectivity information */
    @KivaKitArchivedField
    private ConnectivityStore connectivity;

    private final AttributeReference<ConnectivityStore> CONNECTIVITY =
            new AttributeReference<>(this, EdgeAttributes.get().CONNECTIVITY, "connectivity",
                    () -> new ConnectivityStore("connectivity", graph()))
            {
                @Override
                protected void onLoaded(final ConnectivityStore store)
                {
                    super.onLoaded(store);
                    store.graph(graph());
                }
            };

    private final AttributeReference<SplitPackedArray> IS_CLIPPED =
            new AttributeReference<>(this, VertexAttributes.get().IS_CLIPPED, "isClipped",
                    () -> (SplitPackedArray) new SplitPackedArray("isClipped")
                            .bits(BitCount._1, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    /**
     * The clip state of each vertex
     */
    @KivaKitArchivedField
    private SplitPackedArray isClipped;

    /** Locations where vertexes have been created due to clipping at geographic boundaries */
    private LongSet temporaryClippedLocation;

    /** Cached reference to graph store for performance reasons */
    private final ArchivedGraphStore graphStore;

    private final AttributeReference<IntToByteMap> GRADE_SEPARATION =
            new AttributeReference<>(this, VertexAttributes.get().GRADE_SEPARATION, "gradeSeparation",
                    () -> (IntToByteMap) new IntToByteMap("gradeSeparation")
                            .nullInt(Integer.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private IntToByteMap gradeSeparation;

    private transient boolean vertexesAdded;

    public VertexStore(final Graph graph)
    {
        super(graph);
        graphStore = graph.graphStore();
    }

    /**
     * Uses the index, node identifier and location associations that have been set up to create a vertex with those
     * properties and add it to the store.
     *
     * @see GraphElementStore#adder()
     * @see #onAdd(Vertex)
     */
    public void addVertexes()
    {
        if (!vertexesAdded)
        {
            vertexesAdded = true;

            final var start = Time.now();
            information("Adding vertexes");

            // Add all the vertexes we built up while reading and adding edges
            final var graph = graph();
            final var adder = adder();
            visitElementNodes((index, nodeIdentifier, locationAsLong) ->
            {
                final var vertex = graph.dataSpecification().newHeavyWeightVertex(graph, index);
                vertex.index(index);
                vertex.location(Location.dm7(locationAsLong));
                vertex.nodeIdentifier(new PbfNodeIdentifier(nodeIdentifier));
                adder.add(vertex);
            });

            flush();
            information("Added $ vertexes, discarded $ in $", Count.count(size()), discarded(), start.elapsedSince());
        }
    }

    /**
     * Marks the given location of a vertex where an edge has been clipped at a geographic boundary
     */
    public final void clipped(final Location location)
    {
        temporaryClippedLocation.add(location.asLong());
    }

    /**
     * Marks the given vertex as being clipped at a geographic boundary
     */
    public final void clipped(final Vertex vertex)
    {
        clipped(vertex.location());
    }

    public boolean contains(final MapIdentifier identifier)
    {
        switch (identifier.type())
        {
            case NODE:
                return contains(identifier);

            case WAY:
            case RELATION:
            case INVALID:
            default:
                fail();
                return false;
        }
    }

    /**
     * @return True if this vertex store contains the given vertex
     */
    public boolean contains(final Vertex vertex)
    {
        // The +1 is because there is no identifier 0
        return vertex != null && ((int) vertex.identifierAsLong()) < size() + 1;
    }

    /**
     * @return True if this vertex store contains the given identifier
     */
    public boolean contains(final VertexIdentifier identifier)
    {
        // The +1 is because there is no identifier 0
        return identifier.asInteger() < size() + 1;
    }

    public void freeTemporaryData()
    {
        connectivity().freeTemporaryData();
    }

    /**
     * Creates a new grade separated vertex with a perturbed location so it doesn't precisely overlap any other
     * grade-separated vertex. This method is complex and expensive, but it occurs rarely (mainly at freeway
     * interchanges, bridges and overpasses) so optimization is not a concern. Note that the original vertex will
     * eventually be abandoned (left unconnected to anything) once the process of grade separating the vertex is
     * complete (after separating n levels of vertexes).
     *
     * @param originalVertex The vertex to grade-separate
     * @param grade The grade separation level for the new vertex
     * @param edgesAtGrade The edges at the given grade to connect to the new vertex
     */
    public void gradeSeparate(final Vertex originalVertex, final GradeSeparation grade, final EdgeSet edgesAtGrade)
    {
        // If there are more than two edges its possible that edges at two different grades are
        // connected to the same vertex, so we need to create a new vertex at each grade,
        var vertex = originalVertex;
        if (edgesAtGrade.size() > 2)
        {
            // and if so, we create a new vertex that's a copy of the given vertex,
            final var newVertex = originalVertex.dataSpecification()
                    .newHeavyWeightVertex(graph(), nextIndex());
            newVertex.copy(originalVertex);
            adder().add(newVertex);
            vertex = newVertex;

            // connect the given edges to the new vertex
            gradeSeparateConnectEdgesToNewVertex(vertex, originalVertex, edgesAtGrade);

            // and update the from and to vertexes of the edges so they point to the new vertex
            gradeSeparateUpdateGradeSeparatedEdgeVertexes(newVertex, originalVertex, edgesAtGrade);
        }

        // Next, we perturb the vertex location so it doesn't overlap any other vertex at the grade separation location,
        gradeSeparatePerturbVertexLocation(vertex, originalVertex, grade, edgesAtGrade);

        // and finally, we update the new vertex' grade separation level
        storeGradeSeparation(vertex, grade);
    }

    /**
     * Connects the grade-separated edges from the original vertex to the new vertex
     *
     * @param newVertex The new vertex to connect
     * @param originalVertex The original un-separated vertex
     * @param edgesAtGrade The set of edges at the grade of the new vertex
     */
    public void gradeSeparateConnectEdgesToNewVertex(final Vertex newVertex, final Vertex originalVertex,
                                                     final EdgeSet edgesAtGrade)
    {
        // Get all the in and out edges from the original vertex that are in the collection of edges we were given
        final var inEdges = new EdgeSet(edgesAtGrade.oneWayInEdges(originalVertex));
        final var outEdges = new EdgeSet(edgesAtGrade.oneWayOutEdges(originalVertex));

        // then copy the two-way edge indexes into an int array
        final var twoWayIndexes = new IntArray(objectName() + ".twoWayIndexes");
        twoWayIndexes.initialSize(GraphCoreLimits.Estimated.EDGES_PER_VERTEX);
        twoWayIndexes.initialize();
        final var both = connectivity().retrieveTwoWayEdgeSequence(originalVertex.index());
        for (var edge : both)
        {
            edge = edge.forward();
            if (edgesAtGrade.contains(edge) || edgesAtGrade.contains(edge.reversed()))
            {
                twoWayIndexes.add(edge.index());
            }
        }

        // set the connectivity of the new vertex
        if (!inEdges.isEmpty() || !outEdges.isEmpty() || !twoWayIndexes.isEmpty())
        {
            connectivity().storeLists(newVertex.index(), inEdges.edgeIndexIterator(),
                    outEdges.edgeIndexIterator(), twoWayIndexes.iterator());
        }
    }

    /**
     * Alters the location of a new vertex  slightly so it will not precisely overlap any other grade-separated vertex.
     *
     * @param vertex The new vertex to perturb
     * @param originalVertex The original un-separated vertex
     * @param grade - The grade of the new vertex
     * @param edgesAtGrade - The edges at the given grade
     */
    public void gradeSeparatePerturbVertexLocation(final Vertex vertex, final Vertex originalVertex,
                                                   final GradeSeparation grade, final EdgeSet edgesAtGrade)
    {
        // then use the grade separation level as an offset to the original vertex location in order to
        // give the new vertex a unique, perturbed vertex location so it doesn't overlap the existing vertexes
        final var microdegrees = grade.level() + 5;
        final var offset = new Size(Width.microdegrees(microdegrees), Height.ZERO);
        final var perturbedLocation = originalVertex.location().offsetBy(offset);
        storeNodeLocation(vertex.index(), perturbedLocation.asLong());

        // correct road shape end-points so they are perturbed as well
        for (final var edge : edgesAtGrade)
        {
            final var shape = edge.roadShape();
            if (shape != null)
            {
                if (shape.end().equals(originalVertex.location()))
                {
                    edgeStore().storeRoadShape(edge, shape.withLastReplaced(perturbedLocation));
                }
                if (shape.start().equals(originalVertex.location()))
                {
                    edgeStore().storeRoadShape(edge, shape.withFirstReplaced(perturbedLocation));
                }
            }
        }
    }

    /**
     * Updates the from and to vertexes of the given set of edges so they point to the new vertex
     *
     * @param newVertex The new vertex
     * @param edgesAtGrade The edges to update
     */
    public void gradeSeparateUpdateGradeSeparatedEdgeVertexes(final HeavyWeightVertex newVertex,
                                                              final Vertex originalVertex, final EdgeSet edgesAtGrade)
    {
        // go through the connected edges setting to/from vertexes
        for (var edge : edgesAtGrade)
        {
            edge = edge.forward();
            if (edge.from().equals(originalVertex))
            {
                final var heavyweight = edge.asHeavyWeight();
                heavyweight.from(newVertex);
                edgeStore().storeFromVertexIdentifier(heavyweight);
            }
            if (edge.to().equals(originalVertex))
            {
                final var heavyweight = edge.asHeavyWeight();
                heavyweight.to(newVertex);
                edgeStore().storeToVertexIdentifier(heavyweight);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return metadata().vertexCount(ALLOW_ESTIMATE).asEstimate();
    }

    /**
     * @return True if the two nodes are connected
     */
    public boolean internalIsConnected(final MapNodeIdentifier from, final MapNodeIdentifier to)
    {
        // Get existing indexes for the "from" and "to" node identifiers
        final var fromVertexIndex = nodeIdentifierToIndex(from.identifier());
        final var toVertexIndex = nodeIdentifierToIndex(to.identifier());

        // then ask the connectivity store if they are connected
        return connectivity().temporaryIsConnected(fromVertexIndex, toVertexIndex);
    }

    @Override
    public Iterator<Vertex> iterator()
    {
        return new BaseIterator<>()
        {
            int index = 1;

            @Override
            protected Vertex onNext()
            {
                if (index < size() + 1)
                {
                    return dataSpecification().newVertex(graph(), index++);
                }
                return null;
            }
        };
    }

    /**
     * Once vertexes have been created, a pass is made through the data loading vertex tags. This is a relatively quick
     * process, but it's not possible to do until vertexes have been identified as loading all node tags into memory
     * might be problematic for some regions.
     */
    public void loadVertexTags(final PbfDataSource data, final PbfTagFilter filter)
    {
        metadata().configure(data);
        data.process(new PbfDataProcessor()
        {
            @Override
            public Action onNode(final PbfNode node)
            {
                final var vertex = vertexForNodeIdentifier(node.identifierAsLong());
                if (vertex != null)
                {
                    final var tags = node.tagList(filter);
                    if (!tags.isEmpty())
                    {
                        storeTags(vertex, tags);
                    }
                }
                return ACCEPTED;
            }

            @Override
            public void onStartWays()
            {
                throw new PbfStopProcessingException();
            }
        });
    }

    /**
     * @return The vertex at the given location when full node information is supported
     */
    public GraphNode node(final Location location)
    {
        return new GraphNodeIndex(allNodeIndex(location));
    }

    @Override
    public String objectName()
    {
        return "vertex-store";
    }

    /**
     * @return The total number of edges (in and out) attached to the given vertex
     */
    public final Count retrieveEdgeCount(final Vertex vertex)
    {
        return Count.count(connectivity().retrieveEdgeCount(vertex.index()));
    }

    /**
     * @return The sequence of edges attached to the given vertex
     */
    public final EdgeSequence retrieveEdgeSequence(final Vertex vertex)
    {
        final var edges = connectivity().retrieveEdgeSequence(vertex.index());
        if (edges != null)
        {
            return edges;
        }
        checkVertex(vertex);
        return EdgeSequence.EMPTY;
    }

    public GradeSeparation retrieveGradeSeparation(final Vertex vertex)
    {
        final var grade = GRADE_SEPARATION.retrieveObject(vertex, value -> GradeSeparation.of((int) value));
        return grade == null ? GradeSeparation.GROUND : grade;
    }

    /**
     * @return The number of "in" edges attached to the given vertex
     */
    public final Count retrieveInEdgeCount(final Vertex vertex)
    {
        return Count.count(connectivity().retrieveInEdgeCount(vertex.index()));
    }

    /**
     * @return The sequence of all "in" edges to the given vertex
     */

    public final EdgeSequence retrieveInEdgeSequence(final Vertex vertex)
    {
        final var edges = connectivity().retrieveInEdgeSequence(vertex.index());
        if (edges != null)
        {
            return edges;
        }
        checkVertex(vertex);
        return EdgeSequence.EMPTY;
    }

    /**
     * @return The set of "in" edges to the given vertex
     */
    public final EdgeSet retrieveInEdges(final Vertex vertex)
    {
        final var edges = connectivity().retrieveInEdges(vertex.index());
        if (edges != null)
        {
            return edges;
        }
        checkVertex(vertex);
        return EdgeSet.EMPTY;
    }

    /**
     * @return The index for the given vertex
     */
    @Override
    public final int retrieveIndex(final Vertex vertex)
    {
        // With GraphNodes like Vertex, the identifier is the index
        return (int) (vertex.identifierAsLong() & 0xffff_ffffL);
    }

    /**
     * @return True if the given vertex was added as a result of edge clipping at a region boundary
     */
    public final boolean retrieveIsClipped(final Vertex vertex)
    {
        IS_CLIPPED.load();
        return isClipped.safeGet(vertex.index()) != 0;
    }

    /**
     * @return The number of "out" edges attached to the given vertex
     */
    public final Count retrieveOutEdgeCount(final Vertex vertex)
    {
        return Count.count(connectivity().retrieveOutEdgeCount(vertex.index()));
    }

    /**
     * @return The sequence of all "out" edges attached to the given vertex
     */
    public final EdgeSequence retrieveOutEdgeSequence(final Vertex vertex)
    {
        final var edges = connectivity().retrieveOutEdgeSequence(vertex.index());
        if (edges != null)
        {
            return edges;
        }
        checkVertex(vertex);
        return EdgeSequence.EMPTY;
    }

    /**
     * @return The set of all "out" edges attached to the given vertex
     */
    public final EdgeSet retrieveOutEdges(final Vertex vertex)
    {
        final var edges = connectivity().retrieveOutEdges(vertex.index());
        if (edges != null)
        {
            return edges;
        }
        checkVertex(vertex);
        return EdgeSet.EMPTY;
    }

    /**
     * Stores base attributes and the clip state for the given vertex.
     */
    public void storeAttributes(final Vertex vertex)
    {
        super.storeAttributes(vertex);
        IS_CLIPPED.allocate();
        isClipped.set(vertex.index(), vertex.isClipped() ? (byte) 1 : (byte) 0);
        storeGradeSeparation(vertex, vertex.gradeSeparation());
    }

    public void storeGradeSeparation(final Vertex vertex, final GradeSeparation separation)
    {
        if (separation != null && separation != GradeSeparation.GROUND)
        {
            GRADE_SEPARATION.allocate();
            gradeSeparation.put((int) vertex.identifierAsLong(), (byte) separation.level());
        }
    }

    /**
     * @return True if this vertex store supports all node tags (full node information)
     */
    public boolean supportsAllNodeTags()
    {
        if (allNodeTagStore() != null)
        {
            return allNodeTagStore().isSupported();
        }
        return false;
    }

    /**
     * Connects the given edge to the graph, by adding vertexes to it and storing edge connectivity information.
     * <p>
     * For both the "from" vertex and the "to" vertex of the given edge:
     * <ul>
     *  <li>A mapping is added from node identifier -&gt; location</li>
     *  <li>An index is created for the vertex (this will also serve as the vertex identifier)</li>
     *  <li>A mapping is added from node identifier -&gt; index and from index -&gt; node identifier</li>
     *  <li>A mapping is added from index -&gt; location</li>
     * </ul>
     * Then, if the edge is a {@link HeavyWeightEdge}:
     * <ul>
     *  <li>A {@link Vertex} is created with the index as its {@link VertexIdentifier}</li>
     *  <li>The vertex {@link Location} is added to the {@link Vertex}</li>
     *  <li>The {@link Vertex} is added to the (heavyweight) edge</li>
     * </ul>
     * Finally, the edge and its "from" and "to" vertex identifiers are then added to the {@link ConnectivityStore}
     * via {@link ConnectivityStore#temporaryConnect(Edge, int, int)}
     */
    public void temporaryAddVertexes(final Edge edge)
    {
        // Get "from" and "to" locations
        final var fromLocation = edge.fromLocation();
        final var toLocation = edge.toLocation();

        assert fromLocation != null;
        assert toLocation != null;

        // Get "from" and "to" node identifiers
        final var fromNodeIdentifier = edge.fromNodeIdentifier().asLong();
        final var toNodeIdentifier = edge.toNodeIdentifier().asLong();

        // Store the node's location, both as nodeIdentifier -> location and vertexIdentifier -> location
        final var fromVertexIdentifier = storeNodeLocation(fromNodeIdentifier, fromLocation);
        final var toVertexIdentifier = storeNodeLocation(toNodeIdentifier, toLocation);

        // If this edge is not already in a graph
        if (edge instanceof HeavyWeightEdge)
        {
            // then create new vertex objects
            final var heavyweight = (HeavyWeightEdge) edge;
            final var from = graph().newHeavyWeightVertex(new VertexIdentifier(fromVertexIdentifier));
            final var to = graph().newHeavyWeightVertex(new VertexIdentifier(toVertexIdentifier));
            from.location(fromLocation);
            to.location(toLocation);

            // and add them to the edge
            heavyweight.from(from);
            heavyweight.to(to);
        }

        // Attach this edge to the from and to vertexes, creating in, out and two-way edges for the vertex
        connectivity().temporaryConnect(edge, fromVertexIdentifier, toVertexIdentifier);
    }

    /**
     * Removes the given edge from this store by detaching the "from" and "to" vertexes
     */
    public void temporaryRemove(final Edge edge)
    {
        // Get existing vertex identifiers for the "from" and "to" node identifiers
        final var fromVertexIndex = nodeIdentifierToIndex(edge.fromNodeIdentifier().asLong());
        final var toVertexIndex = nodeIdentifierToIndex(edge.toNodeIdentifier().asLong());
        connectivity().temporaryDisconnect(edge, fromVertexIndex, toVertexIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(final Validation validation)
    {
        return !validation.shouldValidate(getClass()) ? Validator.NULL : new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                // Validate node and element attributes,
                validate(VertexStore.super.validator(validation));

                // then check the "from" and "to" vertexes of each edge in the edge store,
                var count = 0;
                for (final var edge : edgeStore().edges())
                {
                    problemIf(edge.from() == null, "'from' vertex of edge $ is null", edge.identifier());
                    problemIf(edge.to() == null, "'to' vertex of edge $ is null", edge.identifier());

                    // These are just warnings because there may be zero edge counts at the edge of the mapped area
                    warningIf(isZero(edge.from().inEdgeCount()), "'from' vertex $ of edge $ (index $) has no in edges", edge.from().identifier(), edge.identifier(), edge.index());
                    warningIf(isZero(edge.from().outEdgeCount()), "'from' vertex $ of edge $ (index $) has no out edges", edge.from().identifier(), edge.identifier(), edge.index());
                    warningIf(isZero(edge.to().inEdgeCount()), "'to' vertex $ of edge $ (index $) has no in edges", edge.to().identifier(), edge.identifier(), edge.index());
                    warningIf(isZero(edge.to().outEdgeCount()), "'to' vertex $ of edge $ (index $) has no out edges", edge.to().identifier(), edge.identifier(), edge.index());

                    if (isInvalid())
                    {
                        LOGGER.problem("edge $ ($ of $) was invalid", edge.identifier(), count, edgeStore().count());
                        break;
                    }
                    count++;
                }

                // Connectivity is not expected to be valid during raw loading because edges (ways since they are
                // not yet sectioned) can share nodes with other ways without producing a vertex.
                problemIf(!connectivity().isValid(LOGGER), "connectivity is invalid");
            }
        };
    }

    /**
     * @return The vertex, if any, for the given node identifier
     */
    public Vertex vertexForNodeIdentifier(final long nodeIdentifier)
    {
        final var index = nodeIdentifierToIndex(nodeIdentifier);
        if (index > 0)
        {
            return dataSpecification().newVertex(graph(), index);
        }
        return null;
    }

    /**
     * @return The vertexes inside the given bounding rectangle that match the given matcher
     */
    public VertexSequence vertexesInside(final Rectangle bounds, final Matcher<Vertex> matcher)
    {
        if (bounds == null)
        {
            return fail("Bounds is missing");
        }
        if (graph().edgeCount().isGreaterThan(Count._0))
        {
            final var index = edgeStore().spatialIndex();
            if (index == null)
            {
                return fail("Unable to load spatial index");
            }
            final var intersecting = index.intersecting(bounds);
            return new VertexSequence(new DeduplicatingIterable<>(Iterables.iterable(() -> new Next<>()
            {
                final Iterator<Edge> edges = intersecting.iterator();

                int i;

                Edge edge;

                @Override
                public Vertex onNext()
                {
                    while (true)
                    {
                        final var from = i++ % 2 == 0;
                        if (from)
                        {
                            if (edges.hasNext())
                            {
                                edge = edges.next();
                            }
                            else
                            {
                                return null;
                            }
                        }
                        if (edge != null)
                        {
                            final var vertex = from ? edge.from() : edge.to();
                            if (vertex != null && bounds.contains(vertex) && matcher.matches(vertex))
                            {
                                return vertex;
                            }
                        }
                    }
                }
            })));
        }
        else
        {
            return new VertexSequence(Collections.emptyList());
        }
    }

    protected ConnectivityStore connectivity()
    {
        if (!CONNECTIVITY.load())
        {
            CONNECTIVITY.allocate();
        }
        return connectivity;
    }

    @Override
    protected DataSpecification.GraphElementFactory<Vertex> elementFactory()
    {
        return dataSpecification()::newVertex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Vertex> elementType()
    {
        return Vertex.class;
    }

    /**
     * Adds the given vertex to this store. Updates "in" and "out" edge counts, vertex connectivity, increases the
     * bounds of the graph store and stores the vertex's clips state.
     */
    @Override
    protected void onAdd(final Vertex vertex)
    {
        super.onAdd(vertex);

        final var vertexIndex = vertex.index();
        final var locationAsLong = vertex.location().asLong();

        // Update vertex edges from temporary data structures used during loading
        connectivity().storeTemporaryLists(vertex.index());

        // and add the vertex location to bounds of all elements
        graphStore.addToBounds(locationAsLong);

        // and finally, store the vertex attributes
        final var heavyweight = vertex.asHeavyWeight();
        final var clipped = temporaryClippedLocation.contains(locationAsLong);
        heavyweight.clipped(clipped);
        storeAttributes(heavyweight);

        if (DEBUG.isDebugOn())
        {
            final var retrieved = dataSpecification().newVertex(graph(), vertexIndex);
            assert retrieved.validator().validate(LOGGER);
        }
    }

    @Override
    protected void onCommit()
    {
        super.onCommit();
        addVertexes();
    }

    /**
     * Allocate temporary data structures
     */
    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        // Start vertex count at 1 since 0 is the uninitialized value in Java
        resetNextIndex();

        // Temporary vertex information used when loading data
        temporaryClippedLocation = new LongSet(objectName() + ".temporaryClippedLocation");
        temporaryClippedLocation.initialSize(graph().metadata().vertexCount(ALLOW_ESTIMATE).asEstimate());
        temporaryClippedLocation.initialize();
    }

    /**
     * Free temporary data structures
     */
    @Override
    protected void onLoaded(final GraphArchive archive)
    {
        super.onLoaded(archive);

        // Free temporary loading fields
        temporaryClippedLocation = null;
    }

    /**
     * Throws an exception if the given vertex is not in this graph
     */
    private void checkVertex(final Vertex vertex)
    {
        assert contains(vertex.identifier()) : "Invalid vertex " + vertex.identifier();
    }
}
