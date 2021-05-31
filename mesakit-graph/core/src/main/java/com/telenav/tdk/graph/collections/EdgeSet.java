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

package com.telenav.kivakit.graph.collections;

import com.telenav.kivakit.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.collections.primitive.iteration.IntIterator;
import com.telenav.kivakit.collections.set.LogicalSet;
import com.telenav.kivakit.collections.set.operations.*;
import com.telenav.kivakit.kernel.comparison.Differences;
import com.telenav.kivakit.kernel.conversion.BaseConverter;
import com.telenav.kivakit.kernel.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.interfaces.object.Matcher;
import com.telenav.kivakit.kernel.language.iteration.Streams;
import com.telenav.kivakit.kernel.language.string.*;
import com.telenav.kivakit.kernel.language.string.conversion.*;
import com.telenav.kivakit.kernel.language.string.formatting.Separators;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.kernel.messaging.listeners.FailureThrower;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.kernel.time.Frequency;
import com.telenav.kivakit.data.formats.library.map.identifiers.WayIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.identifiers.EdgeIdentifier;
import com.telenav.kivakit.graph.project.KivaKitGraphCoreLimits.Limit;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;
import com.telenav.kivakit.map.measurements.*;
import com.telenav.kivakit.map.road.model.RoadName;

import java.util.*;
import java.util.stream.Stream;

import static com.telenav.kivakit.kernel.validation.Validate.unsupported;

/**
 * A set of edges. Supports {@link #union(EdgeSet)} and {@link #without(Set)} operations, that logically combine this
 * set of edges with another set of edges without creating a physical set.
 *
 * @author jonathanl (shibo)
 */
public class EdgeSet implements Set<Edge>, AsString
{
    public static final EdgeSet EMPTY = new EdgeSet(Maximum._0, Estimate._0, Collections.emptySet());

    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * @return An edge set for a collection of edges
     */
    @SuppressWarnings("unchecked")
    public static EdgeSet forCollection(final Maximum maximumSize, final Collection<? extends Edge> collection)
    {
        if (collection instanceof Set)
        {
            return new EdgeSet(maximumSize, Estimate.of(collection), (Set<Edge>) collection);
        }
        else
        {
            final var set = new EdgeSet(maximumSize, Estimate.of(collection));
            set.addAll(collection);
            return set;
        }
    }

    public static EdgeSet forIdentifierArray(final Graph graph, final LongArray identifiers)
    {
        final var edges = new EdgeSet(Limit.EDGES, Estimate._16);
        final var iterator = identifiers.iterator();
        while (iterator.hasNext())
        {
            final var identifier = iterator.next();
            edges.add(graph.edgeForIdentifier(new EdgeIdentifier(identifier)));
        }
        return edges;
    }

    /**
     * @return An edge set for a sequence of edges
     */
    public static EdgeSet forIterable(final Estimate estimate, final Iterable<? extends Edge> collection)
    {
        final var set = new EdgeSet(estimate);
        for (final Edge edge : collection)
        {
            set.add(edge);
        }
        return set;
    }

    public static EdgeSet of(final Edge one)
    {
        final var set = new EdgeSet();
        set.add(one);
        return set;
    }

    public static EdgeSet of(final Edge one, final Edge... more)
    {
        final var set = new EdgeSet();
        set.add(one);
        set.addAll(Arrays.asList(more));
        return set;
    }

    /**
     * @return An edge set containing a single edge
     */
    public static EdgeSet singleton(final Edge edge)
    {
        return new EdgeSet(Maximum._1, Estimate._1, Collections.singleton(edge));
    }

    public static EdgeSet threadSafe(final Maximum maximumSize, final Estimate initialSize)
    {
        return new EdgeSet(maximumSize, initialSize, Collections.synchronizedSet(new HashSet<>()));
    }

    public static class Converter extends BaseStringConverter<EdgeSet>
    {
        private final Edge.Converter converter;

        private final Separators separators;

        public Converter(final Graph graph, final Separators separators, final Listener<Message> listener)
        {
            super(listener);
            this.separators = separators;
            converter = new Edge.Converter(graph, listener);
        }

        @Override
        protected EdgeSet onConvertToObject(final String value)
        {
            final var edges = new EdgeSet(Limit.EDGES, Estimate._16);
            if (!Strings.isEmpty(value))
            {
                for (final var edge : value.split(separators.current()))
                {
                    edges.add(converter.convert(edge));
                }
            }
            return edges;
        }

        @Override
        protected String onConvertToString(final EdgeSet value)
        {
            return value.joinedIdentifiers(separators.current());
        }
    }

    /**
     * The underlying set of edges
     */
    private final Set<Edge> edges;

    /**
     * The maximum number of edges in this set
     */
    private final Maximum maximumSize;

    /**
     * The estimated number of edges in this set
     */
    private final Estimate initialSize;

    public EdgeSet()
    {
        this(Estimate._16);
    }

    public EdgeSet(final Set<Edge> edges)
    {
        this(Maximum.MAXIMUM, Estimate._16, edges);
    }

    public EdgeSet(final Estimate initialSize)
    {
        this(Maximum.MAXIMUM, initialSize);
    }

    public EdgeSet(final Maximum maximumSize, final Estimate initialSize)
    {
        this(maximumSize, initialSize, new HashSet<>());
    }

    public EdgeSet(final Maximum maximumSize, final Estimate initialSize, final Set<Edge> edges)
    {
        this.maximumSize = maximumSize;
        this.initialSize = initialSize;
        this.edges = edges;
    }

    /**
     * Adds the given edge to this set
     */
    @Override
    public boolean add(final Edge edge)
    {
        if (edges.size() == maximumSize.asInt())
        {
            LOGGER.warning("EdgeSet maximum size of $ elements would be exceeded. Ignoring edge.", maximumSize)
                    .maximumFrequency(Frequency.EVERY_MINUTE);
            return false;
        }
        return edges.add(edge);
    }

    /**
     * Adds each edge in the given route to this set
     */
    public void add(final Route route)
    {
        if (route != null)
        {
            for (final var edge : route)
            {
                add(edge);
            }
        }
    }

    /**
     * Adds all edges in the given collection to this edge set
     */
    @Override
    public boolean addAll(final Collection<? extends Edge> edges)
    {
        var changed = false;
        for (final Edge edge : edges)
        {
            changed = add(edge) || changed;
        }
        return changed;
    }

    /**
     * Adds all edges in the given array to this edge set
     */
    public void addAll(final Edge[] edges)
    {
        for (final var edge : edges)
        {
            add(edge);
        }
    }

    /**
     * @return This edge set as a primitive {@link LongArray} of edge identifiers
     */
    public LongArray asIdentifierArray()
    {
        final var identifiers = new LongArray("temporary");
        for (final var edge : edges)
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
        final var routes = new RouteList();
        for (final var edge : this)
        {
            routes.add(Route.fromEdge(edge));
        }
        return routes;
    }

    /**
     * @return This set of edges as one or more routes, if it can be assembled into one, null if it cannot.
     */
    public List<Route> asRoutes()
    {
        return asRouteList().asRoutes();
    }

    /**
     * @return This edge set as a sequence
     */
    public EdgeSequence asSequence()
    {
        return new EdgeSequence(this);
    }

    public List<Edge> asSortedList()
    {
        final var sorted = asList();
        sorted.sort(Comparator.comparing(Edge::identifier));
        return sorted;
    }

    @Override
    public String asString(final StringFormat format)
    {
        switch (format.identifier())
        {
            case "USER_LABEL":
            {
                final var details = new StringList();
                for (final var edge : this)
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
            final var routes = asRoutes();
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
     * @return The smallest rectangle that contains all edges in this set
     */
    public Rectangle bounds()
    {
        return Rectangle.fromBoundedObjects(this);
    }

    public Edge center()
    {
        Edge center = null;
        Distance closest = null;
        for (final var edge : this)
        {
            final var edgeCenter = edge.bounds().center();
            final var edgeDistance = edgeCenter.distanceTo(bounds().center());
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
    public boolean contains(final Object value)
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
    public boolean containsAll(final Collection<?> edges)
    {
        for (final Object object : edges)
        {
            if (!contains(object))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if this set contains any edge in the given collection
     */
    public boolean containsAny(final Collection<Edge> edges)
    {
        for (final var edge : edges)
        {
            if (contains(edge))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The number of edges in this set
     */
    public Count count()
    {
        return Count.of(size());
    }

    /**
     * @return The differences that would turn this edge set into that edge set
     */
    public Differences differences(final EdgeSet that)
    {
        final var differences = new Differences();
        for (final var edge : this)
        {
            if (!that.contains(edge))
            {
                differences.add("-" + edge);
            }
        }
        for (final var edge : that)
        {
            if (!contains(edge))
            {
                differences.add("+" + edge);
            }
        }
        return differences;
    }

    /**
     * @return A primitive iterator over the edge indexes in this set (for internal use only)
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
    public boolean equals(final Object object)
    {
        if (object instanceof EdgeSet)
        {
            final var that = (EdgeSet) object;
            return edges.equals(that.edges);
        }
        return false;
    }

    /**
     * @return The first edge in this set (an arbitrary edge since sets are not ordered)
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
    public boolean hasMatch(final Matcher<Edge> matcher)
    {
        return !logicalSetMatching(matcher).isEmpty();
    }

    /**
     * @return True if any edge in this set is one way
     */
    public boolean hasOneWay()
    {
        for (final var edge : this)
        {
            if (edge.isOneWay())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if any edge in this set is two way
     */
    public boolean hasTwoWay()
    {
        for (final var edge : this)
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
     * @return The set of edges in this set that are in edges to the given vertex
     */
    public EdgeSet inEdges(final Vertex vertex)
    {
        return matching(edge -> edge.to().equals(vertex));
    }

    public Estimate initialSize()
    {
        return initialSize;
    }

    /**
     * @return The set of all edges in this set that are also in the given set
     */
    public EdgeSet intersection(final EdgeSet that)
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
     * @return True if every edge in this set is one way
     */
    public boolean isOneWay()
    {
        for (final var edge : this)
        {
            if (!edge.isOneWay())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if every edge in this set is two way
     */
    public boolean isTwoWay()
    {
        for (final var edge : this)
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
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Edge> iterator()
    {
        return edges.iterator();
    }

    /**
     * @return All the edge identifiers in this edge set joined into a string using the given separator. The order of
     * identifiers is undefined.
     */
    public String joinedIdentifiers(final String separator)
    {
        return Strings.join(this, separator, new BaseConverter<>(new FailureThrower<>())
        {
            @Override
            protected String onConvert(final Edge value)
            {
                return value.identifier().toString();
            }
        });
    }

    /**
     * @return The total length of all edges in this set
     */
    public Distance length()
    {
        var totalLengthInMillimeters = 0L;
        for (final var edge : this)
        {
            totalLengthInMillimeters += edge.length().asMillimeters();
        }
        return Distance.millimeters(totalLengthInMillimeters);
    }

    /**
     * @return An edge set of the {@link LogicalSet} of all matching edges
     */
    public EdgeSet logicalSetMatching(final Matcher<Edge> matcher)
    {
        return logicalSet(new Subset<>(this, matcher));
    }

    /**
     * @return The set of edges matching the given matcher
     */
    public EdgeSet matching(final Matcher<Edge> matcher)
    {
        final var matching = new EdgeSet();
        for (final var edge : this)
        {
            if (matcher.matches(edge))
            {
                matching.add(edge);
            }
        }
        return matching;
    }

    /**
     * @return The maximum size of this edge set
     */
    public Maximum maximumSize()
    {
        return maximumSize;
    }

    /**
     * @return The most important edge in this set, as determined by {@link Edge#isMoreImportantThan(Edge)}
     */
    public Edge mostImportant()
    {
        Edge important = null;
        for (final var edge : this)
        {
            if (important == null || edge.isMoreImportantThan(important))
            {
                important = edge;
            }
        }
        return important;
    }

    /**
     * @return Set of all edges in this set that are one-way
     */
    public EdgeSet oneWayEdges()
    {
        return logicalSetMatching(Edge::isOneWay);
    }

    /**
     * @return The set of all one way edges in this set that lead to the given vertex
     */
    public EdgeSet oneWayInEdges(final Vertex vertex)
    {
        return matching(edge -> !edge.isTwoWay() && edge.to().equals(vertex));
    }

    /**
     * @return The set of all one way edges in this set that depart from the given vertex
     */
    public EdgeSet oneWayOutEdges(final Vertex vertex)
    {
        return matching(edge -> !edge.isTwoWay() && edge.from().equals(vertex));
    }

    /**
     * @return The set of all edges in this set that depart from the given vertex
     */
    public EdgeSet outEdges(final Vertex vertex)
    {
        return matching(edge -> edge.from().equals(vertex));
    }

    /**
     * @return The edge in this set (most) parallel to the given edge (but never the given edge itself) or null if none
     * are considered parallel
     */
    public Edge parallelTo(final Edge that)
    {
        return parallelTo(that, Edge.PARALLEL_TOLERANCE);
    }

    /**
     * @return The edge in this set most parallel to the given edge (but never the given edge itself) or null if none
     * are considered parallel
     */
    public Edge parallelTo(final Edge that, final Angle tolerance)
    {
        Edge parallel = null;
        var angle = Angle.MAXIMUM;
        for (final var edge : this)
        {
            if (!edge.equals(that))
            {
                final var pair = new EdgePair(edge, that);
                final var difference = pair.smallestAngleBetween();
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
    public boolean remove(final Object object)
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
    public boolean removeAll(final Collection<?> edges)
    {
        for (final Object edge : edges)
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
    public boolean retainAll(final Collection<?> edges)
    {
        return unsupported();
    }

    /**
     * @return The set of all edges in this set reversed. Edges that are not reversible (one way edges) are discarded.
     */
    public EdgeSet reversed()
    {
        final var set = new EdgeSet(maximumSize(), initialSize());
        for (final var edge : this)
        {
            final var reversed = edge.reversed();
            if (reversed != null)
            {
                set.add(reversed);
            }
        }
        return set;
    }

    public Vertex sharedVertex()
    {
        final var vertexes = new VertexSet();
        for (final var edge : this)
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
    public <T> T[] toArray(final T[] a)
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
     * @return Set of all edges in this set that are two-way
     */
    public EdgeSet twoWayEdges()
    {
        return logicalSetMatching(Edge::isTwoWay);
    }

    /**
     * @return The union of this edge set with another set of edges
     */
    public EdgeSet union(final EdgeSet edges)
    {
        return logicalSet(new Union<>(this, edges));
    }

    /**
     * @return The set of all {@link Vertex}es referenced by edges in this set
     */
    public VertexSet vertexes()
    {
        final var vertexes = new VertexSet(Estimate.of(size() * 2L));
        for (final var edge : this)
        {
            vertexes.add(edge.from());
            vertexes.add(edge.to());
        }
        return vertexes;
    }

    public Set<WayIdentifier> wayIdentifiers()
    {
        final Set<WayIdentifier> identifiers = new HashSet<>();
        for (final var edge : this)
        {
            identifiers.add(edge.wayIdentifier());
        }
        return identifiers;
    }

    /**
     * @return The set of edges with the given road name
     */
    public EdgeSet withRoadName(final RoadName name)
    {
        return logicalSetMatching(edge ->
        {
            final var edgeName = edge.roadName();
            return edgeName != null && edgeName.equals(name);
        });
    }

    /**
     * @return The set of edges within the given bounds
     */
    public EdgeSet within(final Rectangle bounds)
    {
        return logicalSetMatching(Edge.within(bounds));
    }

    /**
     * @return This set of edges without the given edge
     */
    public EdgeSet without(final Edge exclude)
    {
        return without(Collections.singleton(exclude));
    }

    /**
     * @return The set of all edges that don't match
     */
    public EdgeSet without(final Matcher<Edge> matcher)
    {
        return logicalSet(new Subset<>(this, edge -> !matcher.matches(edge)));
    }

    /**
     * @return This set of edges without the given set of edges
     */
    public EdgeSet without(final Set<Edge> exclude)
    {
        return logicalSet(new Without<>(this, exclude));
    }

    private EdgeSet logicalSet(final LogicalSet<Edge> set)
    {
        return new EdgeSet(maximumSize(), initialSize(), set);
    }
}
