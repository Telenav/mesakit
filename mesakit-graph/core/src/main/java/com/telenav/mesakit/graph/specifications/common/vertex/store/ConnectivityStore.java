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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.telenav.kivakit.collections.set.logical.operations.Intersection;
import com.telenav.kivakit.kernel.data.validation.BaseValidator;
import com.telenav.kivakit.kernel.data.validation.Validatable;
import com.telenav.kivakit.kernel.data.validation.ValidationType;
import com.telenav.kivakit.kernel.data.validation.Validator;
import com.telenav.kivakit.kernel.interfaces.lifecycle.Initializable;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.language.collections.CompressibleCollection;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitIntArray;
import com.telenav.kivakit.primitive.collections.iteration.IntIterator;
import com.telenav.kivakit.primitive.collections.list.store.IntLinkedListStore;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.map.geography.Location;

import java.util.HashSet;
import java.util.Set;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore.Sequence.ALL;
import static com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore.Sequence.IN;
import static com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore.Sequence.OUT;
import static com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore.Sequence.TWO_WAY;

/**
 * Stores lists of "in", "out" and two-way edges are added during loading with temporary* methods and then stored by
 * vertex index with {@link #storeTemporaryLists(int)}. Information can be retrieved in several ways:
 * <p>
 * <b>Counts</b>
 * <ul>
 *     <li>{@link #retrieveEdgeCount(int)}</li>
 *     <li>{@link #retrieveInEdgeCount(int)}</li>
 *     <li>{@link #retrieveOutEdgeCount(int)} </li>
 *     <li>{@link #retrieveTwoWayEdgeCount(int)}</li>
 * </ul>
 * <p>
 * <b>Edge Sets</b>
 * <ul>
 *     <li>{@link #retrieveInEdges(int)}</li>
 *     <li>{@link #retrieveOutEdges(int)}</li>
 * </ul>
 * <p>
 * <b>Sequences</b>
 * <ul>
 *     <li>{@link #retrieveEdgeSequence(int)}</li>
 *     <li>{@link #retrieveInEdgeSequence(int)}</li>
 *     <li>{@link #retrieveOutEdgeSequence(int)}</li>
 *     <li>{@link #retrieveTwoWayEdgeSequence(int)}</li>
 * </ul>
 * <p>
 * A variety of methods starting with "temporary" are used to temporarily store connectivity information while
 * loading data. These collections are discarded once the store is populated with edge lists.
 *
 * @author jonathanl (shibo)
 */
public class ConnectivityStore implements Validatable, NamedObject, KryoSerializable, CompressibleCollection, Initializable<ConnectivityStore>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    enum Sequence
    {
        OUT,
        IN,
        TWO_WAY,
        ALL,
    }

    /** The graph being accessed */
    private transient volatile Graph graph;

    /** Indexes of edge sets stored in the store */
    @KivaKitArchivedField
    private EdgeArrayStore inEdges;

    /** Indexes of edge sets stored in the store */
    @KivaKitArchivedField
    private EdgeArrayStore outEdges;

    /** Indexes of edge sets stored in the store */
    @KivaKitArchivedField
    private EdgeArrayStore twoWayEdges;

    /** Temporary "in" edge lists in the temporaryVertexEdgeStore while loading a graph */
    private transient SplitIntArray temporaryVertexInEdges;

    /** Temporary "out" edge lists in the temporaryVertexEdgeStore while loading a graph */
    private transient SplitIntArray temporaryVertexOutEdges;

    /** Temporary two-way edge lists in the temporaryVertexEdgeStore while loading a graph */
    private transient SplitIntArray temporaryVertexTwoWayEdges;

    /** Temporary linked list store for "in", "out" and two-way edge lists */
    private transient IntLinkedListStore temporaryVertexEdgeListStore;

    /** The name of this object for debugging purposes */
    private String objectName;

    public ConnectivityStore(final String objectName, final Graph graph)
    {
        assert objectName != null;

        this.objectName = objectName;
        this.graph = ensureNotNull(graph);
        initialize();
    }

    protected ConnectivityStore()
    {
    }

    @Override
    public Method compress(final Method method)
    {
        inEdges.compress(method);
        outEdges.compress(method);
        twoWayEdges.compress(method);

        return compressionMethod();
    }

    @Override
    public Method compressionMethod()
    {
        return Method.RESIZE;
    }

    public void freeTemporaryData()
    {
        temporaryVertexEdgeListStore = null;
        temporaryVertexInEdges = null;
        temporaryVertexOutEdges = null;
        temporaryVertexTwoWayEdges = null;
    }

    /**
     * The graph that owns this store (this method will need to be called when an EdgeStore is deserialized since the
     * graph field is transient)
     */
    public void graph(final Graph graph)
    {
        this.graph = ensureNotNull(graph);
    }

    @Override
    public ConnectivityStore initialize()
    {
        final var vertexCount = graph.metadata().vertexCount(ALLOW_ESTIMATE).asEstimate();

        // Allocate "in", "out" and two-way edge list stores
        inEdges = new EdgeArrayStore(objectName() + "." + "in-edges", graph.metadata());
        outEdges = new EdgeArrayStore(objectName() + "." + "out-edges", graph.metadata());
        twoWayEdges = new EdgeArrayStore(objectName() + "." + "two-way-edges", graph.metadata());

        // Allocate temporary store of "in" and "out" and two-way edges for each vertex
        temporaryVertexInEdges = new SplitIntArray(objectName() + ".temporaryVertexInEdges");
        temporaryVertexInEdges.initialSize(vertexCount);
        temporaryVertexInEdges.initialize();

        temporaryVertexOutEdges = new SplitIntArray(objectName() + ".temporaryVertexOutEdges");
        temporaryVertexOutEdges.initialSize(vertexCount);
        temporaryVertexOutEdges.initialize();

        temporaryVertexTwoWayEdges = new SplitIntArray(objectName() + ".temporaryVertexTwoWayEdges");
        temporaryVertexTwoWayEdges.initialSize(vertexCount);
        temporaryVertexTwoWayEdges.initialize();

        temporaryVertexEdgeListStore = new IntLinkedListStore(objectName() + ".temporaryVertexEdgeListStore");
        temporaryVertexEdgeListStore.initialSize(vertexCount);
        temporaryVertexEdgeListStore.initialize();

        return this;
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void objectName(final String objectName)
    {
        this.objectName = objectName;
    }

    @Override
    public void read(final Kryo kryo, final Input input)
    {
        inEdges = kryo.readObject(input, EdgeArrayStore.class);
        outEdges = kryo.readObject(input, EdgeArrayStore.class);
        twoWayEdges = kryo.readObject(input, EdgeArrayStore.class);
    }

    /**
     * @return The total number of edges attached to the given vertex
     */
    public final int retrieveEdgeCount(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return inEdges.size(vertexIndex)
                + outEdges.size(vertexIndex)
                + twoWayEdges.size(vertexIndex) * 2;
    }

    public EdgeSequence retrieveEdgeSequence(final int vertexIndex)
    {
        return retrieveEdgeSequence(vertexIndex, ALL);
    }

    /**
     * @return The total number of "in" edges, including two-way roads
     */
    public final int retrieveInEdgeCount(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return inEdges.size(vertexIndex) + twoWayEdges.size(vertexIndex);
    }

    /**
     * @return A sequences of "in" edges to the given vertex
     */
    public EdgeSequence retrieveInEdgeSequence(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return retrieveEdgeSequence(vertexIndex, IN);
    }

    /**
     * @return The edge set for the given index
     */
    public EdgeSet retrieveInEdges(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return retrieveInEdgeSequence(vertexIndex).asSet(Estimate._4);
    }

    /**
     * @return The total number of "out" edges, including two-way roads
     */
    public final int retrieveOutEdgeCount(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return outEdges.size(vertexIndex) + twoWayEdges.size(vertexIndex);
    }

    /**
     * @return The sequence of "out" edges for the given vertex
     */
    public EdgeSequence retrieveOutEdgeSequence(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return retrieveEdgeSequence(vertexIndex, OUT);
    }

    /**
     * @return The edge set for the given index
     */
    public EdgeSet retrieveOutEdges(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return retrieveOutEdgeSequence(vertexIndex).asSet(Estimate._4);
    }

    /**
     * @return The number of two-way edges attached to the given vertex
     */
    public final int retrieveTwoWayEdgeCount(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return twoWayEdges.size(vertexIndex);
    }

    /**
     * @return An iterator over the two-way edges for the given vertex
     */
    public EdgeSequence retrieveTwoWayEdgeSequence(final int vertexIndex)
    {
        assert vertexIndex > 0;
        return retrieveEdgeSequence(vertexIndex, TWO_WAY);
    }

    public void storeLists(final int vertexIndex, final IntIterator in, final IntIterator out, final IntIterator twoWay)
    {
        assert vertexIndex > 0;

        inEdges.list(vertexIndex, in);
        outEdges.list(vertexIndex, out);
        twoWayEdges.list(vertexIndex, twoWay);
    }

    public void storeTemporaryLists(final int vertexIndex)
    {
        DEBUG.trace("Storing temporary lists for vertex $", vertexIndex);
        assert vertexIndex > 0;

        final var inList = temporaryVertexInEdges.safeGet(vertexIndex);
        final var outList = temporaryVertexOutEdges.safeGet(vertexIndex);
        final var twoWayList = temporaryVertexTwoWayEdges.safeGet(vertexIndex);

        storeLists(vertexIndex,
                temporaryVertexEdgeListStore.list(inList),
                temporaryVertexEdgeListStore.list(outList),
                temporaryVertexEdgeListStore.list(twoWayList));
    }

    /**
     * Assigns a connection from the "from" vertex to the "to" vertex for the given edge, taking into account its road
     * state (one-way or two-way). This connection is temporary during edge loading and is discarded once connectivity
     * has been finalized.
     */
    public void temporaryConnect(final Edge edge, final int fromVertexIndex, final int toVertexIndex)
    {
        DEBUG.trace("Storing connection between vertex $ and vertex $ for edge index $", fromVertexIndex, toVertexIndex, edge.index());

        final var edgeIndex = edge.index();

        switch (edge.roadState())
        {
            case ONE_WAY:
                temporaryAddInEdge(toVertexIndex, edgeIndex);
                temporaryAddOutEdge(fromVertexIndex, edgeIndex);
                break;

            case TWO_WAY:
                temporaryAddTwoWayEdge(fromVertexIndex, edgeIndex);
                temporaryAddTwoWayEdge(toVertexIndex, -edgeIndex);
                break;

            case CLOSED:
            case NULL:
                break;
        }
    }

    /**
     * Detaches "in" and "out" and two-way edges from the "from" and "to" vertexes in the appropriate way, depending on
     * the road state. This method is called when an edge is removed.
     */
    public void temporaryDisconnect(final Edge edge, final int fromVertexIndex, final int toVertexIndex)
    {
        DEBUG.trace("Removing connection between vertex $ and vertex $ for edge index $", fromVertexIndex, toVertexIndex, edge.index());

        final var edgeIndex = edge.index();

        switch (edge.roadState())
        {
            case ONE_WAY:
                temporaryRemoveOutEdge(fromVertexIndex, edgeIndex);
                temporaryRemoveInEdge(toVertexIndex, edgeIndex);
                break;

            case TWO_WAY:
                temporaryRemoveTwoWayEdge(fromVertexIndex, edgeIndex);
                temporaryRemoveTwoWayEdge(toVertexIndex, -edgeIndex);
                break;

            case CLOSED:
            case NULL:
                break;
        }
    }

    /**
     * @return True if the given "from" and "to" vertex identifiers are connected
     */
    public boolean temporaryIsConnected(final int fromVertexIndex, final int toVertexIndex)
    {
        // If the set intersection of the outbound and inbound edges isn't empty then there must be a shared edge
        return !new Intersection<>(temporaryOutEdges(fromVertexIndex), temporaryInEdges(toVertexIndex)).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(final ValidationType type)
    {
        return new BaseValidator()
        {
            @Override
            protected void onValidate()
            {
                for (var vertexIndex = 1; vertexIndex < vertexCount() && !isInvalid(); vertexIndex++)
                {
                    // We can only check that the edge count is at least one because of one-way edges that have been clipped at the
                    // edges of the source map data (they will only have either a "from" or a "to" edge count depending on the clipping).
                    problemIf(retrieveEdgeCount(vertexIndex) < 1, "Vertex with index $ at $ isn't connected to any edges",
                            vertexIndex, Location.dm7(vertexStore().retrieveLocationAsLong(vertexIndex)));

                    DEBUG.trace("Vertex $ in = $, out = $, two-way = $", vertexIndex,
                            retrieveInEdgeCount(vertexIndex),
                            retrieveOutEdgeCount(vertexIndex),
                            retrieveTwoWayEdgeCount(vertexIndex));
                }
            }
        };
    }

    /**
     * @return The number of vertexes in this connectivity store
     */
    public int vertexCount()
    {
        return inEdges.size();
    }

    @Override
    public void write(final Kryo kryo, final Output output)
    {
        kryo.writeObject(output, inEdges);
        kryo.writeObject(output, outEdges);
        kryo.writeObject(output, twoWayEdges);
    }

    /**
     * @return The edge store associated with the graph associated with this store
     */
    private EdgeStore edgeStore()
    {
        return graph.edgeStore();
    }

    /**
     * @return The sequence of all edges for the given vertex
     */
    private EdgeSequence retrieveEdgeSequence(final int vertexIndex, final Sequence sequence)
    {
        assert vertexIndex > 0;
        final var outer = this;
        return new EdgeSequence(Iterables.iterable(() -> new Next<>()
        {
            final IntIterator in = sequence == ALL || sequence == IN ? outer.inEdges.list(vertexIndex).iterator() : IntIterator.NULL;

            final IntIterator out = sequence == ALL || sequence == OUT ? outer.outEdges.list(vertexIndex).iterator() : IntIterator.NULL;

            final IntIterator twoWayEdges = outer.twoWayEdges.list(vertexIndex).iterator();

            int reverseEdge;

            @Override
            public Edge onNext()
            {
                if (sequence == ALL && reverseEdge != 0)
                {
                    final var edge = edgeStore().edgeForIndex(reverseEdge);
                    reverseEdge = 0;
                    return edge;
                }
                if (twoWayEdges.hasNext())
                {
                    final var next = (sequence == IN) ? -twoWayEdges.next() : twoWayEdges.next();
                    reverseEdge = -next;
                    return edgeStore().edgeForIndex(next);
                }
                if (in.hasNext())
                {
                    return edgeStore().edgeForIndex(in.next());
                }
                if (out.hasNext())
                {
                    return edgeStore().edgeForIndex(out.next());
                }
                return null;
            }
        }));
    }

    /**
     * Adds the given edge index to the list of edges for the given vertex index
     */
    private void temporaryAddEdgeTo(final SplitIntArray vertexEdges, final int vertexIndex, final int edgeIndex)
    {
        // Get any existing list of edges,
        var list = vertexEdges.safeGet(vertexIndex);

        // then add the given edge index to the list of edges (if edges is the null value 0, a new list will be created)
        list = temporaryVertexEdgeListStore.add(list, edgeIndex);

        // and store the new list
        vertexEdges.set(vertexIndex, list);
        assert vertexEdges.get(vertexIndex) == list;
    }

    /**
     * Adds the given edge index as an "in" edge to the given vertex identifier
     */
    private void temporaryAddInEdge(final int vertexIndex, final int edgeIndex)
    {
        temporaryAddEdgeTo(temporaryVertexInEdges, vertexIndex, edgeIndex);
    }

    /**
     * Adds the given edge index as an "out" edge to the given vertex identifier
     */
    private void temporaryAddOutEdge(final int vertexIndex, final int edgeIndex)
    {
        temporaryAddEdgeTo(temporaryVertexOutEdges, vertexIndex, edgeIndex);
    }

    /**
     * Adds the given edge index as a two-way edge to the given vertex identifier
     */
    private void temporaryAddTwoWayEdge(final int vertexIndex, final int edgeIndex)
    {
        temporaryAddEdgeTo(temporaryVertexTwoWayEdges, vertexIndex, edgeIndex);
    }

    /**
     * @return The set of all inbound edge indexes connected to the given vertex index
     */
    private Set<Integer> temporaryInEdges(final int vertexIndex)
    {
        final Set<Integer> inbound = new HashSet<>();
        var index = temporaryVertexInEdges.safeGet(vertexIndex);
        if (!temporaryVertexInEdges.isNull(index))
        {
            final var in = temporaryVertexEdgeListStore.list(index);
            while (in.hasNext())
            {
                inbound.add(Math.abs(in.next()));
            }
        }
        index = temporaryVertexTwoWayEdges.safeGet(vertexIndex);
        if (!temporaryVertexTwoWayEdges.isNull(index))
        {
            final var twoWay = temporaryVertexEdgeListStore.list(index);
            while (twoWay.hasNext())
            {
                inbound.add((Math.abs(twoWay.next())));
            }
        }
        return inbound;
    }

    /**
     * @return The set of "out" edges for the given vertex index
     */
    private Set<Integer> temporaryOutEdges(final int vertexIndex)
    {
        final Set<Integer> outbound = new HashSet<>();
        var index = temporaryVertexOutEdges.safeGet(vertexIndex);
        if (!temporaryVertexOutEdges.isNull(index))
        {
            final var out = temporaryVertexEdgeListStore.list(index);
            while (out.hasNext())
            {
                outbound.add(Math.abs(out.next()));
            }
        }
        index = temporaryVertexTwoWayEdges.safeGet(vertexIndex);
        if (!temporaryVertexTwoWayEdges.isNull(index))
        {
            final var twoWay = temporaryVertexEdgeListStore.list(index);
            while (twoWay.hasNext())
            {
                outbound.add((Math.abs(twoWay.next())));
            }
        }
        return outbound;
    }

    /**
     * Removes the given "in" edge from the given vertex
     */
    private void temporaryRemoveInEdge(final int vertexIndex, final int edgeIndex)
    {
        // Get the list of "in" edges for the vertex
        final var edges = temporaryVertexInEdges.get(vertexIndex);

        // then store a new list without the given edge
        temporaryVertexInEdges.set(vertexIndex, temporaryVertexEdgeListStore.remove(edges, edgeIndex));
    }

    /**
     * Removes the given "out" edge from the given vertex
     */
    private void temporaryRemoveOutEdge(final int vertexIndex, final int edgeIndex)
    {
        // Get the list of "in" edges for the vertex
        final var edges = temporaryVertexOutEdges.get(vertexIndex);

        // then store a new list without the given edge
        temporaryVertexOutEdges.set(vertexIndex, temporaryVertexEdgeListStore.remove(edges, edgeIndex));
    }

    /**
     * Removes the given two-way edge from the given vertex
     */
    private void temporaryRemoveTwoWayEdge(final int vertexIndex, final int edgeIndex)
    {
        // Get the list of two-way edges for the vertex
        final var edges = temporaryVertexTwoWayEdges.get(vertexIndex);

        // then store a new list without the given edge
        temporaryVertexTwoWayEdges.set(vertexIndex, temporaryVertexEdgeListStore.remove(edges, edgeIndex));
    }

    /**
     * @return The vertex store associated with the graph associated with this store
     */
    private VertexStore vertexStore()
    {
        return graph.vertexStore();
    }
}
