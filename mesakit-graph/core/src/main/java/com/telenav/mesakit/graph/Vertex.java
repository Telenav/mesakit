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

import com.telenav.kivakit.collections.set.logical.operations.Intersection;
import com.telenav.kivakit.kernel.data.comparison.Differences;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.reflection.property.KivaKitExcludeProperty;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.library.matchers.Matchers;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.node.NodeAttributes;
import com.telenav.mesakit.graph.specifications.common.vertex.HeavyWeightVertex;
import com.telenav.mesakit.graph.specifications.common.vertex.VertexAttributes;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.road.model.GradeSeparation;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;

/**
 * A vertex in a {@link Graph}, representing one end of an {@link Edge} (either the "from" vertex or the "to" vertex),
 * and possibly defining an intersection with other edges if the vertex is not a dead-end.
 * <p>
 * Note that a Graph API {@link Vertex} is NOT ALWAYS a map node as Graph API {@link Vertex}es only occur at {@link
 * Edge} end-points (as opposed to map nodes which are defined for every shape point of every way). You can also get all
 * the shape points along an {@link Edge} via {@link Edge#shapePoints}.
 * <p>
 * Note that a {@link Vertex} is a small "flyweight" object, having only three fields: a {@link Graph} reference, an
 * integer vertex identifier and a cached integer index. All other attributes are stored in a compressed form in the
 * graph's {@link GraphStore} implementation. This makes it very efficient for Java to work with large numbers of {@link
 * Vertex}es.
 *
 * @author jonathanl (shibo)
 */
public class Vertex extends GraphNode
{
    /**
     * @return A matcher for vertexes inside the given bounds
     */
    public static Matcher<Vertex> inside(Rectangle bounds)
    {
        return vertex -> vertex.isInside(bounds);
    }

    public static class Converter extends BaseStringConverter<Vertex>
    {
        private final Graph graph;

        public Converter(Graph graph, Listener listener)
        {
            super(listener);
            this.graph = graph;
        }

        @Override
        protected String onToString(Vertex vertex)
        {
            return Integer.toString((int) vertex.identifierAsLong());
        }

        @Override
        protected Vertex onToValue(String value)
        {
            return graph.vertexForIdentifier(new VertexIdentifier(Integer.parseInt(value)));
        }
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public Vertex(Graph graph, VertexIdentifier identifier)
    {
        this(graph, identifier.asLong());
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public Vertex(Graph graph, long identifier)
    {
        graph(graph);
        identifier(identifier);
        index((int) identifier);
    }

    @Override
    public HeavyWeightVertex asHeavyWeight()
    {
        if (this instanceof HeavyWeightVertex)
        {
            return (HeavyWeightVertex) this;
        }
        var heavyweight = dataSpecification().newHeavyWeightVertex(graph(), identifierAsLong());
        heavyweight.copy(this);
        return heavyweight;
    }

    public HeavyWeightVertex asHeavyWeightCopy()
    {
        var copy = graph().newHeavyWeightVertex(identifier());
        copy.copy(this);
        return copy;
    }

    @Override
    public GraphElementAttributes<?> attributes()
    {
        return VertexAttributes.get();
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromLocation(location());
    }

    /**
     * @return The differences between this vertex and that vertex
     */
    public Differences differences(Vertex that, Rectangle bounds)
    {
        var differences = new Differences();
        differences.compare("identifier", identifier(), that.identifier());
        differences.compare("location", location(), that.location());
        differences.compare("inEdges", inEdges().within(bounds), that.inEdges().within(bounds));
        differences.compare("outEdges", outEdges().within(bounds), that.outEdges().within(bounds));
        return differences;
    }

    /**
     * @return The edge between this vertex and the given vertex (in either direction)
     */
    public Edge edgeBetween(Vertex vertex)
    {
        var sharedEdges = new Intersection<>(edges(), vertex.edges()).iterator();
        if (sharedEdges.hasNext())
        {
            return sharedEdges.next();
        }
        return null;
    }

    /**
     * @return The number of edges attached to this vertex, regardless of their direction.
     */
    public Count edgeCount()
    {
        return store().retrieveEdgeCount(this);
    }

    /**
     * @return The set of all edges connected to this vertex
     */
    public EdgeSequence edgeSequence()
    {
        return store().retrieveEdgeSequence(this);
    }

    public Edge edgeTo(Vertex that)
    {
        for (var edge : outEdgeSequence())
        {
            if (edge.to().equals(that))
            {
                return edge;
            }
        }
        return null;
    }

    /**
     * @return The set of all edges (both in edges and out edges) attached to this vertex
     */
    public EdgeSet edges()
    {
        return inEdges().union(outEdges());
    }

    /**
     * @return The set of edges connecting this vertex to the given vertex
     */
    public EdgeSet edgesBetween(Vertex that)
    {
        var edges = new EdgeSet();
        for (var edge : outEdges())
        {
            if (edge.to().equals(that))
            {
                edges.add(edge);
            }
        }
        for (var edge : inEdges())
        {
            if (edge.from().equals(that))
            {
                edges.add(edge);
            }
        }
        return edges;
    }

    /**
     * @return The set of edges connecting this vertex to the given vertex
     */
    public EdgeSet edgesTo(Vertex vertex)
    {
        var edges = new EdgeSet();
        for (var edge : outEdges())
        {
            if (edge.to().equals(vertex))
            {
                edges.add(edge);
            }
        }
        return edges;
    }

    /**
     * @return The grade separation level of this vertex
     */
    public GradeSeparation gradeSeparation()
    {
        return store().retrieveGradeSeparation(this);
    }

    /**
     * @return The identifier for this vertex
     */
    @Override
    public VertexIdentifier identifier()
    {
        return new VertexIdentifier((int) identifierAsLong());
    }

    /**
     * @return The number of in edges
     */
    public Count inEdgeCount()
    {
        return store().retrieveInEdgeCount(this);
    }

    /**
     * @return The set of all edges connected to this vertex that have in-bound traffic (meaning traffic headed towards
     * this vertex)
     */
    public EdgeSequence inEdgeSequence()
    {
        return store().retrieveInEdgeSequence(this);
    }

    /**
     * @return The set of all edges connected to this vertex that have in-bound traffic (meaning traffic headed towards
     * this vertex)
     */
    public EdgeSet inEdges()
    {
        return store().retrieveInEdges(this);
    }

    /**
     * @return True if this vertex had one or more edges clipped from it due to proximity to the edge of the graph
     */
    @KivaKitExcludeProperty
    public boolean isClipped()
    {
        return store().retrieveIsClipped(this);
    }

    /**
     * @return True if this vertex is connected to that vertex
     */
    public boolean isConnectedTo(Vertex that)
    {
        return edgeBetween(that) != null;
    }

    /**
     * @return True if this vertex is a dead end
     */
    public boolean isDeadEnd()
    {
        if (!isClipped() && inEdgeCount().equals(Count._1) && outEdgeCount().equals(Count._1))
        {
            var out = outEdges();
            var in = inEdges();
            return out != null && in != null && !out.isEmpty() && !in.isEmpty()
                    && out.first().equals(in.first().reversed());
        }
        return false;
    }

    /**
     * @return True if there is more than one out-bound edge connected to this vertex. Note that for two-way roads, all
     * vertexes will be decision points because u-turn is a possibility. If you want to know if the road forks, call
     * {@link Edge#leadsToFork()}.
     */
    public boolean isDecisionPoint()
    {
        return outEdgeCount().isGreaterThan(Count._1);
    }

    public boolean isIntersection()
    {
        for (var edge : edges())
        {
            if (edge.isIntersectionEdge())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if there is more than one way to get to this vertex
     */
    public boolean isMergeVertex()
    {
        // If there is only one in edge,
        if (inEdgeCount().equals(Count._1))
        {
            // it's a one way through-vertex and there is no merge.
            return false;
        }

        // If there are two in edges but also two out edges and both edges are two-way roads,
        // it's also a through vertex and there is no merge
        return !inEdgeCount().equals(Count._2) || !outEdgeCount().equals(Count._2) || !edges().isTwoWay();

        // In every other case, some edge merges in at this vertex
    }

    public boolean isOnFreeway()
    {
        return !outEdges().logicalSetMatching(Matchers.FREEWAYS_WITHOUT_RAMPS).isEmpty();
    }

    public boolean isSynthetic()
    {
        if (store().supports(NodeAttributes.get().NODE_IDENTIFIER))
        {
            return store().retrieveIsNodeSynthetic(this);
        }
        return ((PbfNodeIdentifier) mapIdentifier()).isSynthetic();
    }

    /**
     * Validate if the vertex is marked as dead end in OSM, i.e. it has tag "noexit:yes".
     * <p>
     * Note: method {@link Vertex#isDeadEnd()} does not check for this tag; it only checks the graph geometry.
     *
     * @return True if this vertex is tagged as a dead end and false otherwise
     */
    public boolean isTaggedAsDeadEnd()
    {
        return "yes".equals(tagValue("noexit"));
    }

    /**
     * @return True if there is exactly one in-bound and one out-bound edge connected to this vertex.
     */
    public boolean isThroughVertex()
    {
        // One way street case
        if (inEdgeCount().equals(Count._1) && outEdgeCount().equals(Count._1))
        {
            return !isDeadEnd();
        }

        // Two way street case
        if (inEdgeCount().isLessThanOrEqualTo(Count._2) && outEdgeCount().isLessThanOrEqualTo(Count._2))
        {
            for (var in : inEdges())
            {
                if (!outEdges().contains(in.reversed()))
                {
                    return false;
                }
            }
            return true;
        }

        // Other intersection
        return false;
    }

    @Override
    public Time lastModificationTime()
    {
        return null;
    }

    @Override
    public Location location()
    {
        return Location.dm7(store().retrieveLocationAsLong(this));
    }

    @Override
    public long locationAsLong()
    {
        return store().retrieveLocationAsLong(this);
    }

    @Override
    public MapNodeIdentifier mapIdentifier()
    {
        return nodeIdentifier();
    }

    public MapNodeIdentifier nodeIdentifier()
    {
        if (store().supports(NodeAttributes.get().NODE_IDENTIFIER))
        {
            return store().retrieveNodeIdentifier(this);
        }
        var out = outEdges().first();
        if (out != null)
        {
            return out.fromNodeIdentifier();
        }
        var in = inEdges().first();
        if (in != null)
        {
            return in.toNodeIdentifier();
        }
        return null;
    }

    /**
     * @return The number of out edges
     */
    public Count outEdgeCount()
    {
        return store().retrieveOutEdgeCount(this);
    }

    /**
     * @return The set of all edges connected to this vertex that have out-bound traffic (meaning traffic headed away
     * from this vertex)
     */
    public EdgeSequence outEdgeSequence()
    {
        return store().retrieveOutEdgeSequence(this);
    }

    /**
     * @return The set of all edges connected to this vertex that have out-bound traffic (meaning traffic headed away
     * from this vertex)
     */
    public EdgeSet outEdges()
    {
        return store().retrieveOutEdges(this);
    }

    /**
     * @return The properties of this element from its {@link DataSpecification},
     * @see GraphElementPropertySet
     */
    @Override
    public GraphElementPropertySet<Vertex> properties()
    {
        return dataSpecification().vertexProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[Vertex " + identifier() + " at " + location() + ", in = " + inEdges() + ", out = " + outEdges() + "]";
    }

    @Override
    protected VertexStore store()
    {
        return subgraph().vertexStore();
    }

    RoadFunctionalClass maximumRoadFunctionalClass()
    {
        var maximum = RoadFunctionalClass.UNKNOWN;
        for (var edge : edges())
        {
            if (edge.roadFunctionalClass().isMoreImportantThan(maximum))
            {
                maximum = edge.roadFunctionalClass();
            }
        }
        return maximum;
    }
}
