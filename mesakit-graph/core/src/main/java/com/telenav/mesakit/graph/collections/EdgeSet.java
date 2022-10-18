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

package com.telenav.mesakit.graph.collections;

import com.telenav.kivakit.collections.set.LogicalSet;
import com.telenav.kivakit.collections.set.operations.Intersection;
import com.telenav.kivakit.collections.set.operations.Subset;
import com.telenav.kivakit.collections.set.operations.Union;
import com.telenav.kivakit.collections.set.operations.Without;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.messages.status.Warning;
import com.telenav.kivakit.core.string.Differences;
import com.telenav.kivakit.core.string.Join;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.primitive.collections.iteration.IntIterator;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.RoadName;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.core.time.Frequency.EVERY_15_SECONDS;
import static com.telenav.mesakit.graph.GraphLimits.Limit;

/**
 * A set of edges. Supports {@link #union(EdgeSet)} and {@link #without(Set)} operations, that logically combine this
 * set of edges with another set of edges without creating a physical set.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused") public class EdgeSet implements Set<Edge>, StringFormattable
{
    public static final EdgeSet EMPTY = new EdgeSet(Maximum._0, Estimate._0, Collections.emptySet());

    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * Returns an edge set for a collection of edges
     */
    @SuppressWarnings("unchecked")
    public static EdgeSet forCollection(Maximum maximumSize, Collection<? extends Edge> collection)
    {
        if (collection instanceof Set)
        {
            return new EdgeSet(maximumSize, Estimate.estimate(collection), (Set<Edge>) collection);
        }
        else
        {
            var set = new EdgeSet(maximumSize, Estimate.estimate(collection));
            set.addAll(collection);
            return set;
        }
    }

    public static EdgeSet forIdentifierArray(Graph graph, LongArray identifiers)
    {
        var edges = new EdgeSet(Limit.EDGES, Estimate._16);
        var iterator = identifiers.iterator();
        while (iterator.hasNext())
        {
            var identifier = iterator.next();
            edges.add(graph.edgeForIdentifier(new EdgeIdentifier(identifier)));
        }
        return edges;
    }

    /**
     * Returns an edge set for a sequence of edges
     */
    public static EdgeSet forIterable(Estimate estimate, Iterable<? extends Edge> collection)
    {
        var set = new EdgeSet(estimate);
        for (Edge edge : collection)
        {
            set.add(edge);
        }
        return set;
    }

    public static EdgeSet of(Edge one)
    {
        var set = new EdgeSet();
        set.add(one);
        return set;
    }

    public static EdgeSet of(Edge one, Edge... more)
    {
        var set = new EdgeSet();
        set.add(one);
        set.addAll(Arrays.asList(more));
        return set;
    }

    /**
     * Returns an edge set containing a single edge
     */
    public static EdgeSet singleton(Edge edge)
    {
        return new EdgeSet(Maximum._1, Estimate._1, Collections.singleton(edge));
    }

    public static EdgeSet threadSafe(Maximum maximumSize, Estimate initialSize)
    {
        return new EdgeSet(maximumSize, initialSize, Collections.synchronizedSet(new HashSet<>()));
    }

    public static class Converter extends BaseStringConverter<EdgeSet>
    {
        private final Edge.Converter converter;

        private final Separators separators;

        public Converter(Graph graph, Separators separators, Listener listener)
        {
            super(listener);
            this.separators = separators;
            converter = new Edge.Converter(graph, listener);
        }

        @Override
        protected String onToString(EdgeSet value)
        {
            return value.joinedIdentifiers(separators.current());
        }

        @Override
        protected EdgeSet onToValue(String value)
        {
            var edges = new EdgeSet(Limit.EDGES, Estimate._16);
            if (!Strings.isNullOrBlank(value))
            {
                for (var edge : value.split(separators.current()))
                {
                    edges.add(converter.convert(edge));
                }
            }
            return edges;
        }
    }

    /**
     * The underlying set of edges
     */
    private final Set<Edge> edges;

    /**
     * The estimated number of edges in this set
     */
    private final Estimate initialSize;

    /**
     * The maximum number of edges in this set
     */
    private final Maximum maximumSize;

    public EdgeSet()
    {
        this(Estimate._16);
    }

    public EdgeSet(Set<Edge> edges)
    {
        this(Maximum.MAXIMUM, Estimate._16, edges);
    }

    public EdgeSet(Estimate initialSize)
    {
        this(Maximum.MAXIMUM, initialSize);
    }

    public EdgeSet(Maximum maximumSize, Estimate initialSize)
    {
        this(maximumSize, initialSize, new HashSet<>());
    }

    public EdgeSet(Maximum maximumSize, Estimate initialSize, Set<Edge> edges)
    {
        this.maximumSize = maximumSize;
        this.initialSize = initialSize;
        this.edges = edges;
    }

    /**
     * Adds the given edge to this set
     */
    @Override
    public boolean add(Edge edge)
    {
        if (edges.size() == maximumSize.asInt())
        {
            LOGGER.transmit(new Warning( "EdgeSet maximum size of $ elements would be exceeded. Ignoring edge.", maximumSize)
                    .maximumFrequency(EVERY_15_SECONDS));
            return false;
        }
        return edges.add(edge);
    }

    /**
     * Adds each edge in the given route to this set
     */
    public void add(Route route)
    {
        if (route != null)
        {
            for (var edge : route)
            {
                add(edge);
            }
        }
    }

    /**
     * Adds all edges in the given collection to this edge set
     */
    @Override
    public boolean addAll(Collection<? extends Edge> edges)
    {
        var changed = false;
        for (Edge edge : edges)
        {
            changed = add(edge) || changed;
        }
        return changed;
    }

    /**
     * Adds all edges in the given array to this edge set
     */
    public void addAll(Edge[] edges)
    {
        for (var edge : edges)
        {
            add(edge);
        }
    }

    /**
     * Returns this edge set as a primitive {@link LongArray} of edge identifiers
     */
    public LongArray asIdentifierArray()
    {
        var identifiers = new LongArray("temporary");
        for (var edge : edges)
        {
            identifiers.add(edge.identifierAsLong());
        }
        return identifiers;
    }

    public List<Edge> asList()
    {
        return new ArrayList<>(this);
    }

    public Route asRoute()
    {
        return asRouteList().asRoute();
    }

    public RouteList asRouteList()
    {
        var routes = new RouteList();
        for (var edge : this)
        {
            routes.add(Route.fromEdge(edge));
        }
        return routes;
    }

    /**
     * Returns this set of edges as one or more routes, if it can be assembled into one, null if it cannot.
     */
    public List<Route> asRoutes()
    {
        return asRouteList().asRoutes();
    }

    /**
     * Returns this edge set as a sequence
     */
    public EdgeSequence asSequence()
    {
        return new EdgeSequence(this);
    }

    public List<Edge> asSortedList()
    {
        var sorted = asList();
        sorted.sort(Comparator.comparing(Edge::identifier));
        return sorted;
    }

    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public String asString(@NotNull Format format)
    {
        switch (format)
        {
            case USER_LABEL:
            {
                var details = new StringList();
                for (var edge : this)
                {
                    details.add("e${long}", edge.isForward() ? edge.index() : -edge.index());
                }
                return details.join(", ");
            }

            default:
                return toString();
        }
    }

    public PbfWay asWay()
    {
        if (!isEmpty())
        {
            var routes = asRoutes();
            if (routes.size() == 1)
            {
                return routes.get(0).asWay();
            }
            else
            {
                LOGGER.warning("Unable to convert $ to a single route", this);
            }
        }
        return null;
    }

    /**
     * Returns the smallest rectangle that contains all edges in this set
     */
    public Rectangle bounds()
    {
        return Rectangle.fromBoundedObjects(this);
    }

    public Edge center()
    {
        Edge center = null;
        Distance closest = null;
        for (var edge : this)
        {
            var edgeCenter = edge.bounds().center();
            var edgeDistance = edgeCenter.distanceTo(bounds().center());
            if (center == null || edgeDistance.isLessThan(closest))
            {
                center = edge;
                closest = edgeDistance;
            }
        }
        return center;
    }

    /**
     * Clears this set
     */
    @Override
    public void clear()
    {
        edges.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object value)
    {
        if (value instanceof Edge)
        {
            return edges.contains(value);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> edges)
    {
        for (Object object : edges)
        {
            if (!contains(object))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this set contains any edge in the given collection
     */
    public boolean containsAny(Collection<Edge> edges)
    {
        for (var edge : edges)
        {
            if (contains(edge))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of edges in this set
     */
    public Count count()
    {
        return Count.count(size());
    }

    /**
     * Returns the differences that would turn this edge set into that edge set
     */
    public Differences differences(EdgeSet that)
    {
        var differences = new Differences();
        for (var edge : this)
        {
            if (!that.contains(edge))
            {
                differences.add("-" + edge);
            }
        }
        for (var edge : that)
        {
            if (!contains(edge))
            {
                differences.add("+" + edge);
            }
        }
        return differences;
    }

    /**
     * Returns a primitive iterator over the edge indexes in this set (for internal use only)
     */
    public IntIterator edgeIndexIterator()
    {
        return new IntIterator()
        {
            final Iterator<Edge> edges = iterator();

            @Override
            public boolean hasNext()
            {
                return edges.hasNext();
            }

            @Override
            public int next()
            {
                return edges.next().index();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof EdgeSet)
        {
            var that = (EdgeSet) object;
            return edges.equals(that.edges);
        }
        return false;
    }

    /**
     * Returns the first edge in this set (an arbitrary edge since sets are not ordered)
     */
    public Edge first()
    {
        if (!isEmpty())
        {
            return edges.iterator().next();
        }
        return null;
    }

    /**
     * Determines if this EdgeSet has any matching edges
     *
     * @return boolean
     */
    public boolean hasMatch(Matcher<Edge> matcher)
    {
        return !logicalSetMatching(matcher).isEmpty();
    }

    /**
     * Returns true if any edge in this set is one way
     */
    public boolean hasOneWay()
    {
        for (var edge : this)
        {
            if (edge.isOneWay())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any edge in this set is two-way
     */
    public boolean hasTwoWay()
    {
        for (var edge : this)
        {
            if (edge.isTwoWay())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return edges.hashCode();
    }

    /**
     * Returns the set of edges in this set that are in edges to the given vertex
     */
    public EdgeSet inEdges(Vertex vertex)
    {
        return matching(edge -> edge.to().equals(vertex));
    }

    public Estimate initialSize()
    {
        return initialSize;
    }

    /**
     * Returns the set of all edges in this set that are also in the given set
     */
    public EdgeSet intersection(EdgeSet that)
    {
        return logicalSet(new Intersection<>(this, that));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return edges.isEmpty();
    }

    /**
     * Returns true if every edge in this set is one way
     */
    public boolean isOneWay()
    {
        for (var edge : this)
        {
            if (!edge.isOneWay())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if every edge in this set is two-way
     */
    public boolean isTwoWay()
    {
        for (var edge : this)
        {
            if (!edge.isTwoWay())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Edge> iterator()
    {
        return edges.iterator();
    }

    /**
     * Returns all the edge identifiers in this edge set joined into a string using the given separator. The order of
     * identifiers is undefined.
     */
    public String joinedIdentifiers(String separator)
    {
        return Join.join(this, separator, value -> value.identifier().toString());
    }

    /**
     * Returns the total length of all edges in this set
     */
    public Distance length()
    {
        var totalLengthInMillimeters = 0L;
        for (var edge : this)
        {
            totalLengthInMillimeters += edge.length().asMillimeters();
        }
        return Distance.millimeters(totalLengthInMillimeters);
    }

    /**
     * Returns an edge set of the {@link LogicalSet} of all matching edges
     */
    public EdgeSet logicalSetMatching(Matcher<Edge> matcher)
    {
        return logicalSet(new Subset<>(this, matcher));
    }

    /**
     * Returns the set of edges matching the given matcher
     */
    public EdgeSet matching(Matcher<Edge> matcher)
    {
        var matching = new EdgeSet();
        for (var edge : this)
        {
            if (matcher.matches(edge))
            {
                matching.add(edge);
            }
        }
        return matching;
    }

    /**
     * Returns the maximum size of this edge set
     */
    public Maximum maximumSize()
    {
        return maximumSize;
    }

    /**
     * Returns the most important edge in this set, as determined by {@link Edge#isMoreImportantThan(Edge)}
     */
    public Edge mostImportant()
    {
        Edge important = null;
        for (var edge : this)
        {
            if (important == null || edge.isMoreImportantThan(important))
            {
                important = edge;
            }
        }
        return important;
    }

    /**
     * Returns set of all edges in this set that are one-way
     */
    public EdgeSet oneWayEdges()
    {
        return logicalSetMatching(Edge::isOneWay);
    }

    /**
     * Returns the set of all one way edges in this set that lead to the given vertex
     */
    public EdgeSet oneWayInEdges(Vertex vertex)
    {
        return matching(edge -> !edge.isTwoWay() && edge.to().equals(vertex));
    }

    /**
     * Returns the set of all one way edges in this set that depart from the given vertex
     */
    public EdgeSet oneWayOutEdges(Vertex vertex)
    {
        return matching(edge -> !edge.isTwoWay() && edge.from().equals(vertex));
    }

    /**
     * Returns the set of all edges in this set that depart from the given vertex
     */
    public EdgeSet outEdges(Vertex vertex)
    {
        return matching(edge -> edge.from().equals(vertex));
    }

    /**
     * Returns the edge in this set (most) parallel to the given edge (but never the given edge itself) or null if none
     * are considered parallel
     */
    public Edge parallelTo(Edge that)
    {
        return parallelTo(that, Edge.PARALLEL_TOLERANCE);
    }

    /**
     * Returns the edge in this set most parallel to the given edge (but never the given edge itself) or null if none
     * are considered parallel
     */
    public Edge parallelTo(Edge that, Angle tolerance)
    {
        Edge parallel = null;
        var angle = Angle.MAXIMUM;
        for (var edge : this)
        {
            if (!edge.equals(that))
            {
                var pair = new EdgePair(edge, that);
                var difference = pair.smallestAngleBetween();
                if (difference.isLessThan(tolerance) && difference.isLessThan(angle))
                {
                    parallel = edge;
                    angle = difference;
                }
            }
        }
        return parallel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object object)
    {
        if (object instanceof Edge)
        {
            return edges.remove(object);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> edges)
    {
        for (Object edge : edges)
        {
            remove(edge);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean retainAll(Collection<?> edges)
    {
        return unsupported();
    }

    /**
     * Returns the set of all edges in this set reversed. Edges that are not reversible (one way edges) are discarded.
     */
    public EdgeSet reversed()
    {
        var set = new EdgeSet(maximumSize(), initialSize());
        for (var edge : this)
        {
            var reversed = edge.reversed();
            if (reversed != null)
            {
                set.add(reversed);
            }
        }
        return set;
    }

    public Vertex sharedVertex()
    {
        var vertexes = new VertexSet();
        for (var edge : this)
        {
            if (vertexes.contains(edge.from()))
            {
                return edge.from();
            }
            if (vertexes.contains(edge.to()))
            {
                return edge.to();
            }
            vertexes.add(edge.from());
            vertexes.add(edge.to());
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return edges.size();
    }

    @Override
    public Stream<Edge> stream()
    {
        return Streams.stream(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray()
    {
        return edges.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T @NotNull [] a)
    {
        return unsupported();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[" + joinedIdentifiers(", ") + "]";
    }

    /**
     * Returns set of all edges in this set that are two-way
     */
    public EdgeSet twoWayEdges()
    {
        return logicalSetMatching(Edge::isTwoWay);
    }

    /**
     * Returns the union of this edge set with another set of edges
     */
    public EdgeSet union(EdgeSet edges)
    {
        return logicalSet(new Union<>(this, edges));
    }

    /**
     * Returns the set of all {@link Vertex}es referenced by edges in this set
     */
    public VertexSet vertexes()
    {
        var vertexes = new VertexSet(Maximum.maximum(size() * 2L));
        for (var edge : this)
        {
            vertexes.add(edge.from());
            vertexes.add(edge.to());
        }
        return vertexes;
    }

    public Set<PbfWayIdentifier> wayIdentifiers()
    {
        Set<PbfWayIdentifier> identifiers = new HashSet<>();
        for (var edge : this)
        {
            identifiers.add(edge.wayIdentifier());
        }
        return identifiers;
    }

    /**
     * Returns the set of edges with the given road name
     */
    public EdgeSet withRoadName(RoadName name)
    {
        return logicalSetMatching(edge ->
        {
            var edgeName = edge.roadName();
            return edgeName != null && edgeName.equals(name);
        });
    }

    /**
     * Returns the set of edges within the given bounds
     */
    public EdgeSet within(Rectangle bounds)
    {
        return logicalSetMatching(Edge.within(bounds));
    }

    /**
     * Returns this set of edges without the given edge
     */
    public EdgeSet without(Edge exclude)
    {
        return without(Collections.singleton(exclude));
    }

    /**
     * Returns the set of all edges that don't match
     */
    public EdgeSet without(Matcher<Edge> matcher)
    {
        return logicalSet(new Subset<>(this, edge -> !matcher.matches(edge)));
    }

    /**
     * Returns this set of edges without the given set of edges
     */
    public EdgeSet without(Set<Edge> exclude)
    {
        return logicalSet(new Without<>(this, exclude));
    }

    private EdgeSet logicalSet(LogicalSet<Edge> set)
    {
        return new EdgeSet(maximumSize(), initialSize(), set);
    }
}
