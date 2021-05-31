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

import com.telenav.kivakit.collections.iteration.iterators.SingletonIterator;
import com.telenav.kivakit.collections.stack.Stack;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.iteration.Streams;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.primitives.Ints;
import com.telenav.kivakit.kernel.language.strings.Split;
import com.telenav.kivakit.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.graph.Edge.SignPostSupport;
import com.telenav.mesakit.graph.collections.EdgePair;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.collections.VertexSet;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.map.MapEdgeIdentifier;
import com.telenav.mesakit.graph.map.MapRoute;
import com.telenav.mesakit.graph.project.GraphCoreLimits.Limit;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadType;
import com.telenav.mesakit.map.road.name.standardizer.RoadNameStandardizer;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.kernel.language.collections.CompressibleCollection.Method.RESIZE;
import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle._90_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle.degrees;

/**
 * A list of edges that is organized according to connectivity, forming a continuous route. Attempting to add an {@link
 * Edge} to a {@link Route} that is not connected to the edges already in the route will result in a run-time
 * exception.
 *
 * @author jonathanl (shibo)
 */
public abstract class Route implements Iterable<Edge>, Bounded, AsString
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * @return A route for the given edges
     */
    public static Route forEdges(final Edge... edges)
    {
        final var builder = new RouteBuilder();
        for (final var edge : edges)
        {
            builder.append(edge);
        }
        return builder.route();
    }

    /**
     * @return A route for the given edges
     */
    public static Route forEdges(final Iterable<Edge> edges)
    {
        final var builder = new RouteBuilder();
        for (final var edge : edges)
        {
            builder.append(edge);
        }
        return builder.route();
    }

    public static Route forLongArray(final Graph graph, final long[] identifiers)
    {
        final var builder = new RouteBuilder();
        for (final var identifier : identifiers)
        {
            builder.append(graph.edgeForIdentifier(new EdgeIdentifier(identifier)));
        }
        return builder.route();
    }

    /**
     * @return A route for the given vertexes
     */
    public static Route forVertexes(final Iterable<Vertex> vertexes)
    {
        final var edges = new EdgeSet();
        Vertex previous = null;
        for (final var vertex : vertexes)
        {
            if (previous == null)
            {
                previous = vertex;
                continue;
            }
            final var candidates = previous.outEdges();
            for (final var candidate : candidates)
            {
                if (candidate.to().equals(vertex))
                {
                    edges.add(candidate);
                }
            }
            previous = vertex;
        }
        return forEdges(edges);
    }

    /**
     * @return A route for the given edge
     */
    public static Route fromEdge(final Edge edge)
    {
        return new OneEdgeRoute(edge);
    }

    /**
     * Converts routes composed of ordinary {@link EdgeIdentifier}s. To convert routes composed of {@link
     * MapEdgeIdentifier}s (way:from-node:to-node format), see {@link MapIdentifierConverter}
     *
     * @author jonathanl (shibo)
     * @see MapIdentifierConverter
     */
    public static class Converter extends BaseStringConverter<Route>
    {
        private final Separators separators;

        private final Edge.Converter edgeConverter;

        private final Graph graph;

        public Converter(final Graph graph, final Separators separators, final Listener listener)
        {
            this(graph, separators, listener, new Edge.Converter(graph, listener));
        }

        public Converter(final Graph graph, final Separators separators, final Listener listener,
                         final Edge.Converter edgeConverter)
        {
            super(listener);
            this.graph = graph;
            this.separators = separators;
            this.edgeConverter = edgeConverter;
        }

        @Override
        protected Route onConvertToObject(final String value)
        {
            try
            {
                final var edges = new EdgeSet();
                for (final var edge : Split.split(value, separators.current()))
                {
                    edges.add(edgeConverter.convert(edge));
                }
                return edges.asRoute();
            }
            catch (final Exception e)
            {
                problem(problemBroadcastFrequency(), e, "${class}: Problem converting ${debug} with graph ${debug}",
                        subclass(), value, graph.name());
            }
            return null;
        }

        @Override
        protected String onConvertToString(final Route route)
        {
            final var edges = new StringList(Maximum.maximum(route.size()));
            for (final var edge : route)
            {
                edges.add(edgeConverter.toString(edge));
            }
            return edges.join(separators.current());
        }
    }

    /**
     * Converts routes composed of {@link MapEdgeIdentifier}s (way:from-node:to-node). To convert routes composed of
     * ordinary {@link EdgeIdentifier}s, see {@link Route.Converter}
     *
     * @author jonathanl (shibo)
     * @see Route.Converter
     */
    public static class MapIdentifierConverter extends BaseStringConverter<Route>
    {
        private final Separators separators;

        private final MapEdgeIdentifier.EdgeConverter edgeConverter;

        private final Graph graph;

        public MapIdentifierConverter(final Graph graph, final Separators separators, final Listener listener)
        {
            super(listener);
            this.graph = graph;
            this.separators = separators;
            edgeConverter = new MapEdgeIdentifier.EdgeConverter(listener, graph);
        }

        @Override
        protected Route onConvertToObject(final String value)
        {
            final var builder = new RouteBuilder();
            try
            {
                for (final var edge : Split.split(value, separators.current()))
                {
                    final var next = edgeConverter.convert(edge);
                    if (next == null)
                    {
                        problem("Unable to locate edge $ ", edge);
                        return null;
                    }
                    builder.append(next);
                }
            }
            catch (final Exception e)
            {
                problem(problemBroadcastFrequency(), e, "${class}: Problem converting ${debug} with graph ${debug}", subclass(), value,
                        graph.name());
            }
            return builder.route();
        }

        @Override
        protected String onConvertToString(final Route route)
        {
            final var edges = new StringList(Maximum.maximum(route.size()));
            for (final var edge : route)
            {
                edges.add(edgeConverter.toString(edge));
            }
            return edges.join(separators.current());
        }
    }

    /**
     * A route consisting of two sub-routes
     *
     * @author jonathanl (shibo)
     */
    private static class ConcatenatedRoute extends Route
    {
        /** The head route */
        private final Route head;

        /** The tail route */
        private final Route tail;

        /** The last edge in the route */
        private Edge last;

        /** The total distance */
        private long lengthInMillimeters = -1L;

        /** The total travel time for this route */
        private int travelTimeInMilliseconds = -1;

        /** The total number of edges */
        private int size = -1;

        private ConcatenatedRoute(final Route head, final Route tail)
        {
            this.head = head;
            this.tail = tail;
            last = tail.last();
        }

        @Override
        public String asString()
        {
            return "[Route edges = [" + new ObjectList<>().appendAll(this) + "], length = " + length()
                    + ", travelTime = " + travelTime() + "]";
        }

        @Override
        public Vertex end()
        {
            // If we're a concatenated route
            if (tail instanceof ConcatenatedRoute)
            {
                // get the end of the tail route
                return tail.end();
            }

            // If we've hit the end of the route
            else if (tail instanceof OneEdgeRoute)
            {
                // and the last edge in the head is connected to the tail's end vertex
                if (head.last().isConnectedTo(tail.end()))
                {
                    // then that edge must be backwards, so the end is the start
                    return tail.start();
                }
                else
                {
                    // otherwise the start is the start
                    return tail.end();
                }
            }
            else
            {
                return fail("Unsupported route class");
            }
        }

        @Override
        public Edge first()
        {
            return head.first();
        }

        @Override
        public Edge get(final int index)
        {
            if (index < head.size())
            {
                return head.get(index);
            }
            return tail.get(index - head.size());
        }

        @Override
        public Iterator<Edge> iterator()
        {
            return new BaseIterator<>()
            {
                // Keep a stack of routes to iterate through so we can avoid using the Java stack
                // (which can cause stack overflows with long routes)
                private final Stack<Route> stack = new Stack<>(count().asMaximum());

                // The current route object we are at in the iteration process
                private Route current = head;

                @Override
                protected Edge onNext()
                {
                    // While there is some current route object,
                    while (current != null)
                    {
                        // if it's a single edge
                        if (current instanceof OneEdgeRoute)
                        {
                            // return the single edge and advance to the next route
                            final var next = ((OneEdgeRoute) current).edge;
                            current = stack.pop();
                            return next;
                        }

                        // and if it's a ConcatenatedRoute,
                        if (current instanceof ConcatenatedRoute)
                        {
                            // push the tail onto the stack to explore later
                            stack.push(((ConcatenatedRoute) current).tail);

                            // and explore the head now
                            current = ((ConcatenatedRoute) current).head;
                        }
                    }

                    // No more edges
                    return null;
                }

                {
                    // push tail onto the stack for later
                    stack.push(tail);
                }
            };
        }

        @Override
        public Edge last()
        {
            // If there's a cached last reference
            if (last != null)
            {
                // return that
                return last;
            }

            // Use iteration to find the last edge instead of recursion to avoid heavy stack use
            final var iterator = iterator();
            Edge last = null;
            while (iterator.hasNext())
            {
                last = iterator.next();
            }
            this.last = last;
            return last;
        }

        @Override
        public int size()
        {
            if (size < 0)
            {
                size = head.size() + tail.size();
            }
            return size;
        }

        @Override
        public Vertex start()
        {
            // If we're a concatenated route
            if (head instanceof ConcatenatedRoute)
            {
                // get the start of the head route
                return head.start();
            }

            // If we've hit the end of the route
            else if (head instanceof OneEdgeRoute)
            {
                // and the first edge in the tail is connected to the head's start vertex
                if (tail.first().isConnectedTo(head.start()))
                {
                    // then that edge must be backwards, so the end is the start
                    return head.end();
                }
                else
                {
                    // otherwise the start is the start
                    return head.start();
                }
            }
            else
            {
                return fail("Unsupported route class");
            }
        }

        @Override
        public long totalLengthInMillimeters()
        {
            if (lengthInMillimeters < 0)
            {
                lengthInMillimeters = head.lengthInMillimeters() + tail.lengthInMillimeters();
            }
            return lengthInMillimeters;
        }

        @Override
        public Duration travelTime()
        {
            return Duration.milliseconds(travelTimeInMilliseconds());
        }

        @Override
        public int travelTimeInMilliseconds()
        {
            if (travelTimeInMilliseconds < 0)
            {
                travelTimeInMilliseconds = head.travelTimeInMilliseconds()
                        + tail.travelTimeInMilliseconds();
            }
            return travelTimeInMilliseconds;
        }

        @Override
        public Route withoutFirst()
        {
            if (head instanceof OneEdgeRoute)
            {
                return tail;
            }
            else
            {
                final var builder = new RouteBuilder();
                var at = 0;
                for (final var edge : head)
                {
                    if (at++ > 0)
                    {
                        builder.append(edge);
                    }
                }
                builder.append(tail);
                return builder.route();
            }
        }

        @Override
        public Route withoutLast()
        {
            if (tail instanceof OneEdgeRoute)
            {
                return head;
            }
            else
            {
                final var builder = new RouteBuilder();
                builder.append(head);
                final var size = size();
                var at = 0;
                for (final var edge : tail)
                {
                    if (++at < size - 1)
                    {
                        builder.append(edge);
                    }
                }
                return builder.route();
            }
        }
    }

    /**
     * A route consisting of just one edge
     *
     * @author jonathanl (shibo)
     */
    private static class OneEdgeRoute extends Route
    {
        private final Edge edge;

        public OneEdgeRoute(final Edge edge)
        {
            assert edge != null : "Edge cannot be null";

            this.edge = edge;
        }

        @Override
        public String asString()
        {
            return "[Route edges = " + edge + ", length = " + length() + ", travelTime = " + travelTime()
                    + "]";
        }

        @Override
        public Vertex end()
        {
            return edge.to();
        }

        @Override
        public Edge first()
        {
            return edge;
        }

        @Override
        public Edge get(final int index)
        {
            if (index == 0)
            {
                return edge;
            }
            return fail("Index $ is out of bounds", index);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<Edge> iterator()
        {
            return new SingletonIterator<>(edge);
        }

        @Override
        public Edge last()
        {
            return edge;
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public Vertex start()
        {
            return edge.from();
        }

        @Override
        public long totalLengthInMillimeters()
        {
            return edge.lengthInMillimeters();
        }

        @Override
        public Duration travelTime()
        {
            return Duration.milliseconds(travelTimeInMilliseconds());
        }

        @Override
        public int travelTimeInMilliseconds()
        {
            final var ratio = (double) lengthInMillimeters() / (double) totalLengthInMillimeters();
            return (int) (edge.travelTimeInMilliseconds() * ratio);
        }

        @Override
        public Route withoutFirst()
        {
            return fail("One edge route would be invalid without the first edge");
        }

        @Override
        public Route withoutLast()
        {
            return fail("One edge route would be invalid without the last edge");
        }

        @Override
        protected void checkEndOffset(final Distance offset)
        {
            super.checkEndOffset(offset);
            checkLengthWithOffset(startOffset(), offset);
        }

        @Override
        protected void checkStartOffset(final Distance offset)
        {
            super.checkStartOffset(offset);
            checkLengthWithOffset(offset, endOffset());
        }

        private void checkLengthWithOffset(final Distance startOffset, final Distance endOffset)
        {
            if (edge.length().asMillimeters() - offsetInMillimeters(startOffset, endOffset) < 0)
            {
                fail("The offsets' length exceeds the total route length");
            }
        }
    }

    /**
     * True if we have already warned that the maximum was exceeded by a concatenation attempt
     */
    private boolean maximumExceeded;

    /**
     * The amount of the first edge that is NOT included in the route (if the route doesn't include the entire first
     * edge)
     */
    private Distance startOffset = Distance.MINIMUM;

    /**
     * The amount of the last edge that is NOT included in the route (if the route doesn't include the entire last
     * edge)
     */
    private Distance endOffset = Distance.MINIMUM;

    /**
     * Force use of static factory method
     */
    private Route()
    {
    }

    /**
     * @return This route with the given edge appended, if the resulting route is less than the maximum length. If the
     * resulting route would be too long, a warning is logged and the edge is not appended.
     */
    public Route append(final Count maximum, final Edge edge)
    {
        return append(maximum, new OneEdgeRoute(edge));
    }

    /**
     * @return This route with the given route appended, if the resulting route is less than the maximum length. If the
     * resulting route would be too long, a warning is logged and the route is not appended.
     */
    public Route append(final Count maximum, final Route that)
    {
        assert that != null : "Route cannot be null";

        // If we can append that route to this one
        if (canAppend(maximum, that))
        {
            // and return the concatenated route
            return new ConcatenatedRoute(this, that);
        }

        // then try to append the reversed route
        final var reversed = that.reversed();
        if (canAppend(maximum, reversed))
        {
            // and return the concatenated route
            return new ConcatenatedRoute(this, that.reversed());
        }

        if (last().equals(that.first()))
        {
            return fail("Route " + that + " cannot be appended to " + this + " because it starts with the duplicate edge " + last());
        }
        else
        {
            if (that.last().isConnectedTo(first()))
            {
                return fail("Route " + that + " cannot be appended to " + this + ", but the two routes could be appended in the opposite order or if one route were reversed");
            }
            else
            {
                return fail("Route " + that + " cannot be appended because it is not connected to " + this
                                + ". This route's last location at $ is not equal to that route's first location at $",
                        last().toLocation(), that.first().fromLocation());
            }
        }
    }

    /**
     * @return This route with the given edge appended. For convenience the edge can be null and nothing will be
     * appended.
     */
    public Route append(final Edge edge)
    {
        if (edge == null)
        {
            return this;
        }
        else
        {
            return append(Limit.EDGES_PER_ROUTE, edge);
        }
    }

    /**
     * @return This route with the given route appended
     */
    public Route append(final Route route)
    {
        return append(Limit.EDGES_PER_ROUTE, route);
    }

    /**
     * @return The set of all edges in this route
     */
    public EdgeSet asEdgeSet()
    {
        final var edges = new EdgeSet(Estimate.estimate(size()));
        for (final var edge : this)
        {
            edges.add(edge);
        }
        return edges;
    }

    /**
     * @return A primitive array of edge identifiers for this route
     */
    public LongArray asIdentifierArray()
    {
        final var size = Estimate.estimate(this);

        final var array = new LongArray("temporary");
        array.initialSize(size);
        array.initialize();

        for (final var edge : this)
        {
            array.add(edge.identifierAsLong());
        }

        array.compress(RESIZE);
        return array;
    }

    /**
     * @return This route as a list of {@link Edge}s in route order
     */
    public List<Edge> asList()
    {
        final List<Edge> edges = new ArrayList<>(size());
        for (final var edge : this)
        {
            edges.add(edge);
        }
        return edges;
    }

    public MapRoute asMapRoute()
    {
        return new MapRoute(this);
    }

    /**
     * @return Set of all vertexes in this route
     */
    public VertexSet asVertexSet()
    {
        return VertexSet.forIterable(Maximum.maximum(size() + 1), vertexes());
    }

    /**
     * @return This route as a viewable object in the given color with the given label;
     */
    public Viewable asViewable(final Color background, final Color foreground, final String label)
    {
        return new Viewable()
        {
            @Override
            public Rectangle bounds()
            {
                return Route.this.bounds();
            }

            @Override
            public void draw(final DrawingSurface surface)
            {
                surface.draw(polyline(), background, foreground, label);
            }
        };
    }

    public PbfWay asWay()
    {
        final List<WayNode> nodes = new ArrayList<>();
        for (final var point : shapePoints())
        {
            final var node = new WayNode(point.mapIdentifier().asLong());
            nodes.add(node);
        }
        return new PbfWay(new Way(first().commonEntityData(), nodes));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromBoundedObjects(this);
    }

    public boolean canAppend(final Count maximum, final Route that)
    {
        // If the the last edge of this route is connected to the first edge of that route and the
        // maximum size won't be exceeded, we can append.
        if (that != null && last() != null && that.first() != null)
        {
            return last().to().equals(that.first().from()) && checkSize(maximum, that);
        }
        return false;
    }

    public boolean canAppend(final Route that)
    {
        return canAppend(Limit.EDGES_PER_ROUTE, that);
    }

    public Route connect(final Edge edge)
    {
        return connect(Route.fromEdge(edge));
    }

    /**
     * @return Connect two routes, if possible. If either route is two way they may be reversed to form the connection
     */
    public Route connect(final Route that)
    {
        if (leadsTo(that))
        {
            return append(that);
        }
        if (that.leadsTo(this))
        {
            return that.append(this);
        }
        if (isReversible())
        {
            final var thisReversed = reversed();
            if (thisReversed.leadsTo(that))
            {
                return thisReversed.append(that);
            }
            if (that.isReversible())
            {
                final var thatReversed = that.reversed();
                if (thisReversed.leadsTo(thatReversed))
                {
                    return thisReversed.append(thatReversed);
                }
            }
        }
        if (that.isReversible())
        {
            final var toReversed = that.reversed();
            if (leadsTo(toReversed))
            {
                return append(toReversed);
            }
        }
        return null;
    }

    /**
     * @return True if this route connects to the same road
     */
    public boolean connectsToSameRoad()
    {
        final var isRouteStraight = isStraight();
        final var fromEdges = first().fromEdgesWithoutThisEdge();
        final var toEdges = last().toEdgesWithoutThisEdge();
        for (final var toEdge : toEdges)
        {
            for (final var fromEdge : fromEdges)
            {
                // if the route is connecting to same edge
                if (fromEdge.equals(toEdge))
                {
                    return true;
                }

                // if route is not straight, check the perpendicular edges
                if (!isRouteStraight && isAngleValid(fromEdge, toEdge))
                {
                    // check if they are connecting to same road
                    final var nonbranchingRoute = fromEdge.nonBranchingRouteWithSameName(Maximum._6);
                    if (nonbranchingRoute.contains(toEdge))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return True if the route is very short and right between two junctions
     */
    public boolean connectsTwoCloseJunctions()
    {
        // if a route is shorter than 5 meters and it connecting to two junctions, then this route
        // should be junction edges
        if (length().isLessThan(Distance.meters(5)))
        {
            final var inEdges = first().inEdgesWithoutReversed();
            final var outEdges = last().outEdgesWithoutReversed();
            if (inEdges.size() > 1 && outEdges.size() > 1)
            {
                return !inEdges.first().roadType().equals(RoadType.LOW_SPEED_ROAD)
                        || !outEdges.first().roadType().equals(RoadType.LOW_SPEED_ROAD)
                        || !first().roadType().equals(RoadType.LOW_SPEED_ROAD);
            }
        }
        return false;
    }

    /**
     * Note: Not Use isDoubleDigitized attribute of edge
     *
     * @return True if the route is connecting to double digitized roads
     */
    public boolean connectsTwoDoubleDigitizedRoads()
    {
        // routes between two double digitized roads should not connect to same road
        if (connectsToSameRoad())
        {
            return false;
        }

        for (final var edgeFrom : first().fromEdgesWithoutThisEdge())
        {
            for (final var edgeTo : last().toEdgesWithoutThisEdge())
            {
                if (edgeFrom.equals(edgeTo) || edgeFrom.equals(edgeTo.reversed()))
                {
                    continue;
                }

                // check attributes of suspected double digitized roads
                if (hasSameTypeAndFunctionalClass(edgeFrom, edgeTo) && isAttributeValid(edgeFrom)
                        && isAttributeValid(edgeTo) && (hasReasonableLength(edgeFrom) || hasReasonableLength(edgeTo)))
                {
                    RoadName roadNameFrom = null;
                    RoadName roadNameTo = null;
                    if (edgeFrom.supports(EdgeAttributes.get().ROAD_NAMES))
                    {
                        roadNameFrom = edgeFrom.roadName();
                        roadNameTo = edgeTo.roadName();
                    }

                    // if both of the two edges of the suspected double digitized roads have no name
                    if (roadNameFrom == null && roadNameTo == null)
                    {
                        // and they are not bidirectional
                        if (edgeFrom.isTwoWay() || edgeTo.isTwoWay())
                        {
                            continue;
                        }
                        // and they are not low priority roads with route length greater than
                        // MAXIMUM_LENGTH_FOR_LOW_PRIORITY_ROAD
                        if ((isLowPriorityRoad(edgeFrom) || isLowPriorityRoad(edgeTo))
                                && length().isGreaterThan(Distance.meters(12)))
                        {
                            continue;
                        }

                        // then check the angle to see if they are double digitized roads
                        if (isAngleValid(edgeFrom, edgeTo))
                        {
                            return true;
                        }
                    }
                    // if suspected double digitized roads have same road name
                    else if (roadNameFrom != null && roadNameTo != null
                            && roadNameFrom.extractNameOnly().equals(roadNameTo.extractNameOnly()))
                    {
                        // check the angle
                        if (isAngleValid(edgeFrom, edgeTo))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Note: Use isDoubleDigitized attribute of edge
     *
     * @return True if this route connects two double-digitized roads
     */
    public boolean connectsTwoDoubleDigitizedRoads2()
    {
        // A connector can't be bent more than 90 degrees
        if (polyline().isBent(Distance.MAXIMUM, _90_DEGREES))
        {
            return false;
        }

        // If any edge in this route
        for (final var edge : this)
        {
            // is double digitized or not a one-way road,
            if (edge.osmIsDoubleDigitized() || !edge.isOneWay())
            {
                // then this route isn't a connector between two double digitized roads
                return false;
            }
        }

        // Go through all edges that are entering this route
        for (final var in : first().from().inEdges())
        {
            // and if the in edge is a one way road
            if (in.isOneWay())
            {
                // go through all out edges of this route
                for (final var out : last().to().outEdges())
                {
                    // and if the out edge is also a one way road
                    if (out.isOneWay())
                    {
                        // see if the from/to pair is double digitized
                        if (new EdgePair(in, out).isDoubleDigitized())
                        {
                            return true;
                        }
                    }
                }
            }
        }

        // We don't connect two double digitized roads
        return false;
    }

    /**
     * @return True if the given route connects two double digitized roads with different names
     */
    public boolean connectsTwoDoubleDigitizedRoadsWithDifferentNames()
    {
        if (!connectsToSameRoad())
        {
            for (final var edgeFrom : first().inEdgesWithoutReversed())
            {
                for (final var edgeTo : last().outEdgesWithoutReversed())
                {
                    if (!edgeFrom.isForwardOrReverseOf(edgeTo) && !areOnSameLogicalWay(edgeFrom, edgeTo, this)
                            && (hasReasonableLength(edgeFrom) || hasReasonableLength(edgeTo))
                            && isDoubleDigitizedWithDifferentNames(new EdgePair(edgeFrom, edgeTo)))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     */
    public boolean contains(final Edge that)
    {
        for (final var edge : this)
        {
            if (edge.equals(that))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if this route contains every edge in the given route
     */
    public boolean contains(final Route that)
    {
        for (final var edge : that)
        {
            if (!contains(edge))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if this route contains the given vertex
     */
    public boolean contains(final Vertex that)
    {
        for (final var vertex : vertexes())
        {
            if (vertex.equals(that))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if the route contains repeated edges
     */
    public boolean containsRepeatedEdge()
    {
        final Set<Edge> edges = new HashSet<>();
        for (final var edge : this)
        {
            if (edges.contains(edge))
            {
                return true;
            }
            edges.add(edge);
        }
        return false;
    }

    public Count count()
    {
        return Count.count(size());
    }

    /**
     * @return The location of the vertex where the given road name crosses this route
     */
    public Location crossStreet(final RoadNameStandardizer standardizer, final RoadName name)
    {
        final var standardizedName = standardizer.standardize(name).asRoadName();
        for (final var vertex : vertexes())
        {
            for (final var edge : vertex.inEdges())
            {
                for (final var crossName : edge.roadNames())
                {
                    if (standardizer.standardize(crossName).asRoadName().equals(standardizedName))
                    {
                        return vertex.location();
                    }
                }
            }
        }
        return null;
    }

    public Distance distanceToDecisionPoint()
    {
        var distance = Distance.ZERO;
        for (final var edge : this)
        {
            distance = distance.add(edge.length());
            if (edge.to().isDecisionPoint())
            {
                break;
            }
        }
        return distance;
    }

    /**
     * @return One of connected doubly digitized edge, or null
     */
    public Edge doubleDigitizedEdge()
    {
        // routes between two double digitized roads should not connect to same road
        if (connectsToSameRoad())
        {
            return null;
        }

        for (final var edgeFrom : first().fromEdgesWithoutThisEdge())
        {
            for (final var edgeTo : last().toEdgesWithoutThisEdge())
            {
                if (edgeFrom.equals(edgeTo) || edgeFrom.equals(edgeTo.reversed()))
                {
                    continue;
                }

                // check attributes of suspected double digitized roads
                if (isAttributeValid(edgeFrom) && isAttributeValid(edgeTo)
                        && hasSameTypeAndFunctionalClass(edgeFrom, edgeTo) // &&
                        // isRouteLengthValid(edgeFrom,
                        // edgeTo)
                        && (hasReasonableLength(edgeFrom) || hasReasonableLength(edgeTo)))
                {
                    RoadName roadNameFrom = null;
                    RoadName roadNameTo = null;
                    if (edgeFrom.supports(EdgeAttributes.get().ROAD_NAMES))
                    {
                        roadNameFrom = edgeFrom.roadName();
                        roadNameTo = edgeTo.roadName();
                    }

                    // if both of the two edges of the suspected double digitized roads have no name
                    if (roadNameFrom == null && roadNameTo == null)
                    {
                        // and they are not bidirectional
                        if (edgeFrom.isTwoWay() || edgeTo.isTwoWay())
                        {
                            continue;
                        }
                        // and they are not low priority roads with route length greater than
                        // MAXIMUM_LENGTH_FOR_LOW_PRIORITY_ROAD
                        if ((isLowPriorityRoad(edgeFrom) || isLowPriorityRoad(edgeTo))
                                && length().isGreaterThan(Distance.meters(12)))
                        {
                            continue;
                        }

                        // then check the angle to see if they are double digitized roads
                        if (isAngleValid(edgeFrom, edgeTo))
                        {
                            return edgeFrom;
                        }
                    }
                    // if suspected double digitized roads have same road name
                    else if (roadNameFrom != null && roadNameTo != null
                            && roadNameFrom.extractNameOnly().equals(roadNameTo.extractNameOnly()))
                    {
                        // check the angle
                        if (isAngleValid(edgeFrom, edgeTo))
                        {
                            return edgeFrom;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @return The total number of edges in this route
     */
    public Count edgeCount()
    {
        return Count.count(size());
    }

    /**
     * @return The vertex at which this route ends
     */
    public abstract Vertex end();

    /**
     * @return The set of edges attached to the vertex at the end of this route
     */
    public EdgeSet endEdges()
    {
        return end().edges().without(last());
    }

    /**
     * @return The end offset of this route (the amount of the last edge that doesn't count as part of the route)
     */
    public Distance endOffset()
    {
        return endOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Route)
        {
            final var that = (Route) object;
            if (size() == that.size())
            {
                if (this == that)
                {
                    return true;
                }
                if (lengthInMillimeters() == that.lengthInMillimeters())
                {
                    return Iterables.equals(this, that) && Objects.equals(startOffset, that.startOffset)
                            && Objects.equals(endOffset, that.endOffset);
                }
            }
        }
        return false;
    }

    /**
     * @return The first edge in this route
     */
    public abstract Edge first();

    /**
     * @return The travel time for this route based on functional class information
     */
    public Duration functionalClassTravelTime()
    {
        var travelTimeInMilliseconds = 0L;
        for (final var edge : this)
        {
            travelTimeInMilliseconds += edge.travelTimeForFunctionalClass().asMilliseconds();
        }
        return Duration.milliseconds(travelTimeInMilliseconds);
    }

    public abstract Edge get(int index);

    public Graph graph()
    {
        return first().graph();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Hash.many(Iterables.hashCode(this), startOffset, endOffset);
    }

    public Route head()
    {
        return fromEdge(first());
    }

    /**
     * @return The vertexes in this route, not including the end points
     */
    public Iterable<Vertex> interiorVertexes()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            // Edge iterator
            private final Iterator<Edge> edges = iterator();

            @Override
            public Vertex onNext()
            {
                // If we have more edges,
                if (edges.hasNext())
                {
                    // get the next edge
                    final var edge = edges.next();

                    // and if we're not at the very end
                    if (edges.hasNext())
                    {
                        // return the interior vertex
                        return edge.to();
                    }
                }
                return null;
            }
        });
    }

    /**
     * @return True if the given edge is connected to either end of this route
     */
    public boolean isConnectedTo(final Edge edge)
    {
        return first().isConnectedTo(edge) || last().isConnectedTo(edge);
    }

    /**
     * @return True if this route is an in-place u-turn
     */
    public boolean isInPlaceUTurn()
    {
        Edge previous = null;
        for (final var edge : this)
        {
            if (previous != null)
            {
                if (previous.equals(edge.reversed()))
                {
                    return true;
                }
            }

            previous = edge;
        }

        return false;
    }

    /**
     * @return True if all edges in this route are inside the given bounds
     */
    public boolean isInside(final Rectangle bounds)
    {
        for (final var edge : this)
        {
            if (!edge.isInside(bounds))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return True if this route forms a simple closed loop
     */
    public boolean isLoop()
    {
        final var first = first();
        final var last = last();
        return first.from().equals(last.to()) && first.heading().isClose(last.heading(), degrees(20));
    }

    /**
     * @return True if any edge in this route is parallel to the given edge
     */
    public boolean isParallel(final Edge that)
    {
        for (final var edge : this)
        {
            if (edge.isParallelTo(that))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isReversible()
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
     * @return True if every edge in this route is on the same road as the given edge
     */
    public boolean isSameRoadAs(final Edge that)
    {
        for (final var edge : this)
        {
            if (edge.isOnSameRoadAs(that))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isSharpUTurn(final Angle maximumTurnAngle)
    {
        return turnVertexInSharpUTurn(maximumTurnAngle) != null;
    }

    /**
     * @return True if the road shape of this route is straight to within a tolerance of 10 degrees
     */
    public boolean isStraight()
    {
        return polyline().isStraight(Distance.MAXIMUM, degrees(10));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public abstract Iterator<Edge> iterator();

    /**
     * @return The last edge in this route
     */
    public abstract Edge last();

    /**
     * @return True if this route leads to the given edge
     */
    public boolean leadsTo(final Edge edge)
    {
        return last().leadsTo(edge);
    }

    /**
     * @return True if this route leads to that route
     */
    public boolean leadsTo(final Route that)
    {
        return leadsTo(that.first());
    }

    /**
     * @return The length of this route
     */
    public Distance length()
    {
        return Distance.millimeters(lengthInMillimeters());
    }

    /**
     * @return The length of this route in millimeters
     */
    public long lengthInMillimeters()
    {
        return totalLengthInMillimeters() - offsetInMillimeters(startOffset, endOffset);
    }

    /**
     * @return True if the two routes meet at one or more vertexes, false if the routes are completely disjoint
     */
    public boolean meets(final Route that)
    {
        final var thatVertexes = that.asVertexSet();
        for (final var vertex : vertexes())
        {
            if (thatVertexes.contains(vertex))
            {
                return true;
            }
        }
        return false;
    }

    public Edge middleEdge()
    {
        final var middle = size() / 2;
        return get(Ints.inRange(middle, 0, size() - 1));
    }

    /**
     * @return True if this route shares one or more edges with that route
     */
    public boolean overlaps(final Route that)
    {
        for (final var edge : this)
        {
            if (that.contains(edge))
            {
                return true;
            }
        }
        return false;
    }

    public Stream<Edge> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    /**
     * @return The polyline for this route including all shape points in each edge
     */
    public Polyline polyline()
    {
        final var builder = new PolylineBuilder();
        var firstEdge = true;
        for (final var edge : this)
        {
            var firstLocation = true;
            for (final var location : edge.roadShape().locationSequence())
            {
                if (firstEdge || !firstLocation)
                {
                    builder.add(location);
                }
                firstLocation = false;
            }
            firstEdge = false;
        }
        return builder.build();
    }

    /**
     * @return This route with the given edge prepended
     */
    public Route prepend(final Edge edge)
    {
        return fromEdge(edge).append(Limit.EDGES_PER_ROUTE, this);
    }

    /**
     * @return This route with the given edge prepended if the resulting route would be less than the given maximum
     * length
     */
    public Route prepend(final Edge edge, final Distance maximum)
    {
        if (length().add(edge.length()).isGreaterThan(maximum))
        {
            return this;
        }
        return prepend(edge);
    }

    /**
     * @return This route with the given route appended
     */
    public Route prepend(final Route route)
    {
        return route.append(Limit.EDGES_PER_ROUTE, this);
    }

    /**
     * @return This route reversed or null if not all edges are two-way
     */
    public Route reversed()
    {
        final var builder = new RouteBuilder();
        for (final var edge : this)
        {
            if (!edge.isTwoWay())
            {
                return null;
            }
            builder.prepend(edge.reversed());
        }
        return builder.route();
    }

    /**
     * @return A rough polyline for this route, including only vertexes in the route but not the shapepoints of each
     * individual edge
     */
    public Polyline roughPolyline()
    {
        return Polyline.fromLocations(vertexLocations());
    }

    /**
     * @return This route broken at decision points
     */
    public List<Route> sectionedAtNonUTurnDecisionPoints()
    {
        final List<Route> sections = new ArrayList<>();
        var builder = new RouteBuilder();
        for (final var edge : this)
        {
            builder.append(edge);
            if (!edge.to().isThroughVertex())
            {
                sections.add(builder.route());
                builder = new RouteBuilder();
            }
        }
        if (builder.size() > 0)
        {
            sections.add(builder.route());
        }
        return sections;
    }

    public List<ShapePoint> shapePoints()
    {
        return graph().shapePoints(polyline());
    }

    public Set<SignPostSupport> signPostSupport()
    {
        final Set<SignPostSupport> support = new HashSet<>();
        for (final var edge : this)
        {
            support.addAll(edge.signPostSupport());
        }
        return support;
    }

    /**
     * @return The size of this route in edges
     */
    public abstract int size();

    /**
     * @return The vertex at which this route starts
     */
    public abstract Vertex start();

    /**
     * @return The set of edges attached to the start of this route
     */
    public EdgeSet startEdges()
    {
        return start().edges().without(first());
    }

    /**
     * @return The offset at the start of the route that is not included in the route length
     */
    public Distance startOffset()
    {
        return startOffset;
    }

    /**
     * @return The sub-route of this route starting at the given edge (inclusive)
     */
    public Route startingAt(final Edge start)
    {
        return startingAt(start, Integer.MAX_VALUE);
    }

    /**
     * @return The sub-route of this route starting at the given edge (inclusive)
     */
    public Route startingAt(final Edge start, final int limit)
    {
        final var builder = new RouteBuilder();
        var started = false;
        for (final var edge : this)
        {
            started = started || edge.equals(start);
            if (started)
            {
                builder.append(edge);
            }
            if (builder.size() == limit)
            {
                break;
            }
        }
        if (!started)
        {
            fail("Start edge " + start + " not found in route: " + this);
        }
        return builder.route();
    }

    public Stream<Edge> stream()
    {
        return Streams.stream(this);
    }

    public ObjectList<Edge> tail(final Count count)
    {
        final var last = new ObjectList<Edge>(count.asMaximum());
        for (var i = Math.max(0, size() - count.asInt()); i < size(); i++)
        {
            last.add(get(i));
        }
        return last;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return toString(":");
    }

    public String toString(final String separator)
    {
        final var edges = new StringList();
        for (final var edge : this)
        {
            edges.append(edge.identifier().toString());
        }
        return edges.join(separator);
    }

    /**
     * @return This route as a comma-separated list of way identifiers
     */
    public String toWayIdentifierString()
    {
        final var identifiers = new StringList(Maximum._100_000);
        PbfWayIdentifier last = null;
        for (final var edge : this)
        {
            final var identifier = edge.wayIdentifier();
            if (!identifier.equals(last))
            {
                identifiers.add(identifier.toString());
                last = identifier;
            }
        }
        return identifiers.join(",");
    }

    /**
     * @return The travel time for this route, based on free flow speed
     */
    public abstract Duration travelTime();

    /**
     * @return Travel time for this route in milliseconds, based on free flow speed
     */
    public abstract int travelTimeInMilliseconds();

    public Route trimLeadingWay()
    {
        var current = first();
        for (final var edge : this)
        {
            if (!edge.isOnSameWay(current) || edge.isForward() != current.isForward())
            {
                break;
            }
            current = edge;
        }
        return startingAt(current);
    }

    public Route trimTrailingWay()
    {
        final var edges = asList();
        var current = last();
        for (var i = edges.size() - 2; i >= 0; i--)
        {
            final var edge = edges.get(i);
            if (!edge.isOnSameWay(current) || edge.isForward() != current.isForward())
            {
                break;
            }
            current = edge;
        }
        return upTo(current);
    }

    public Vertex turnVertexInSharpUTurn(final Angle maximumTurnAngle)
    {
        Vertex turnVertex = null;
        Edge previous = null;
        for (final var edge : this)
        {
            if (previous != null)
            {
                final var fromHeading = previous.finalHeading();
                final var toHeading = edge.initialHeading();

                if (fromHeading.difference(toHeading, Chirality.SMALLEST).isGreaterThan(maximumTurnAngle))
                {
                    turnVertex = edge.from();
                    break;
                }
            }

            previous = edge;
        }

        return turnVertex;
    }

    /**
     * @return The sub-route of this route from the beginning to the ending edge (inclusive)
     */
    public Route upTo(final Edge end)
    {
        final var builder = new RouteBuilder();
        for (final var edge : this)
        {
            builder.append(edge);
            if (edge.equals(end))
            {
                return builder.route();
            }
        }
        return fail("Edge " + end + " not found");
    }

    /**
     * @return The total number of vertexes in this route
     */
    public Count vertexCount()
    {
        return Count.count(size() + 1);
    }

    /**
     * @return The location of each vertex in this route, in order
     */
    public Iterable<Location> vertexLocations()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            private final Iterator<Vertex> vertexes = vertexes().iterator();

            @Override
            public Location onNext()
            {
                if (vertexes.hasNext())
                {
                    return vertexes.next().location();
                }
                return null;
            }
        });
    }

    /**
     * @return The vertexes in this route, in order
     */
    public Iterable<Vertex> vertexes()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            // Edge iterator
            private final Iterator<Edge> edges = iterator();

            // The most recently returned vertex, or null if we're at the start of the route
            private Vertex last;

            @Override
            public Vertex onNext()
            {
                final Vertex next;

                // If we're just starting iteration,
                if (last == null)
                {
                    // the next vertex is the start of the route
                    next = start();
                }
                else
                {
                    // If we have more edges,
                    if (edges.hasNext())
                    {
                        // get the next edge
                        final var edge = edges.next();

                        // and the next vertex is the vertex on this edge that is at the
                        // opposite end from the last vertex
                        next = edge.oppositeVertex(last);
                    }
                    else
                    {
                        // we're out of edges
                        next = null;
                    }
                }

                // Save the last for next time and return the next
                last = next;
                return next;
            }
        });
    }

    /**
     * @return This route with the given end offset (the amount of the last edge that is not included in this route)
     */
    public Route withEndOffset(final Distance offset)
    {
        checkEndOffset(offset);
        final var route = forEdges(this);
        route.startOffset = startOffset;
        route.endOffset = offset;
        return route;
    }

    /**
     * @return This route with the given start offset (the amount of the first edge that is not included in this route)
     */
    public Route withStartOffset(final Distance offset)
    {
        checkStartOffset(offset);
        final var route = forEdges(this);
        route.startOffset = offset;
        route.endOffset = endOffset;
        return route;
    }

    /**
     * @return This route without the first edge
     */
    public abstract Route withoutFirst();

    /**
     * @return This route without the last edge
     */
    public abstract Route withoutLast();

    protected void checkEndOffset(final Distance offset)
    {
        if (offset.isGreaterThan(last().length()))
        {
            fail("The end offset exceeds the length of the last edge!");
        }
    }

    protected void checkStartOffset(final Distance offset)
    {
        if (offset.isGreaterThan(first().length()))
        {
            fail("The start offset exceeds the length of the first edge!");
        }
    }

    protected long offsetInMillimeters(final Distance startOffset, final Distance endOffset)
    {
        var lengthInMillimeters = 0L;
        if (startOffset != null)
        {
            lengthInMillimeters += startOffset.asMillimeters();
        }
        if (endOffset != null)
        {
            lengthInMillimeters += endOffset.asMillimeters();
        }
        return lengthInMillimeters;
    }

    // the total length of the route without taking account of offsets
    protected abstract long totalLengthInMillimeters();

    /**
     * Verifies if the given edges - connected to this route - and the route are on the same logical way.
     *
     * @param edgeIntoRoute edge entering into the route's first node
     * @param edgeOutOfRoute edge coming out of the route's last node
     * @param route the route between the given edges
     * @return true if the edges and the route are on the same logical way, false otherwise
     */
    private boolean areOnSameLogicalWay(final Edge edgeIntoRoute, final Edge edgeOutOfRoute, final Route route)
    {
        final var routeFirst = route.first();
        final var routeLast = route.last();
        return routeFirst.isOnSameWay(edgeIntoRoute) || routeLast.isOnSameWay(edgeOutOfRoute)
                || new EdgePair(edgeIntoRoute, routeFirst).isStraight(degrees(25D))
                || new EdgePair(routeLast, edgeOutOfRoute).isStraight(degrees(25D));
    }

    /**
     * @return True if the size of this route plus the size of that route
     */
    private boolean checkSize(final Count maximumSize, final Route that)
    {
        if (size() + that.size() > maximumSize.asInt())
        {
            if (!maximumExceeded)
            {
                LOGGER.warning(new Throwable(),
                        "Maximum size of ${debug} elements would have been exceeded. Ignoring operation (this is not an exception, just a warning)",
                        maximumSize);
                maximumExceeded = true;
            }
            return false;
        }
        return true;
    }

    /**
     * @return True if the edge has a reasonable length (at least 5 meters long)
     */
    private boolean hasReasonableLength(final Edge edge)
    {
        return edge.length().isGreaterThan(Distance.meters(5));
    }

    /**
     * @return True if the road type and functional class are same for both edges
     */
    private boolean hasSameTypeAndFunctionalClass(final Edge edgeFrom, final Edge edgeTo)
    {
        return edgeFrom.roadType().equals(edgeTo.roadType())
                && edgeFrom.roadFunctionalClass().equals(edgeTo.roadFunctionalClass());
    }

    /**
     * Verifies if the edges from the given edge pair have the same "ref" tag.
     *
     * @return true if the edges hac=ve the same ref tag value, false otherwise
     */
    private boolean hasTheSameRef(final EdgePair edgePair)
    {
        final var refValueFirst = edgePair.first().tag("ref");
        final var refValueSecond = edgePair.second().tag("ref");

        final var firstRef = refValueFirst != null ? refValueFirst.getValue() : null;
        final var secondRef = refValueSecond != null ? refValueSecond.getValue() : null;

        return firstRef != null && firstRef.equalsIgnoreCase(secondRef);
    }

    /**
     * @return True if the edgeFrom and edgeTo are coming from same side and parallel, and they should be not parallel
     * with the route
     */
    private boolean isAngleValid(final Edge edgeFrom, final Edge edgeTo)
    {
        // angle from 'from vertex of route' to edgeFrom
        final var headingFrom = edgeFrom.from().equals(first().from()) ? edgeFrom.roadShape().initialHeading()
                : edgeFrom.roadShape().reversed().initialHeading();

        // angle from 'to vertex of route' to edgeTo
        final var headingTo = edgeTo.from().equals(last().to()) ? edgeTo.roadShape().initialHeading()
                : edgeTo.roadShape().reversed().initialHeading();

        if (headingFrom.difference(headingTo, Chirality.SMALLEST).isGreaterThan(degrees(30)))
        {
            return false;
        }

        // angle between headingFrom and edgeFrom
        final var intersectionAngleFrom = headingFrom.difference(first().roadShape().initialHeading(),
                Chirality.SMALLEST);

        // angle between headingTo and edgeTo
        final var intersectionAngleTo = headingTo.difference(last().roadShape().reversed().initialHeading(),
                Chirality.SMALLEST);

        return intersectionAngleFrom.isBetween(degrees(40), degrees(140), Chirality.SMALLEST)
                && intersectionAngleTo.isBetween(degrees(40), degrees(140), Chirality.SMALLEST);
    }

    private boolean isAttributeValid(final Edge edge)
    {
        // check road sub type
        if (edge.isRoundabout())
        {
            return false;
        }

        // check road type
        if (edge.roadType().isLessImportantThan(RoadType.LOCAL_ROAD)
                && first().roadType().isLessImportantThan(RoadType.LOCAL_ROAD))
        {
            return false;
        }

        // check road subtype
        if (edge.roadSubType().equals(RoadSubType.CONNECTING_ROAD) || edge.roadSubType().equals(RoadSubType.RAMP))
        {
            return false;
        }

        // check bent
        return isFromVertexConnectedToRoute(edge) ? !edge.roadShape().isBent(Distance.meters(20), degrees(40)) : !edge.roadShape().reversed().isBent(Distance.meters(20), degrees(40));
    }

    /**
     * The edge pair having different names could be double digitized if their {@code ref} tag is the same.
     *
     * @return true if the edges are double digitized, false otherwise
     */
    private boolean isDoubleDigitizedWithDifferentNames(final EdgePair edgePair)
    {
        return hasTheSameRef(edgePair)
                && edgePair.isDoubleDigitized(EdgePair.DoubleDigitizedType.MISMATCHED_NAMES, degrees(35));
    }

    private boolean isFromVertexConnectedToRoute(final Edge edge)
    {
        return edge.from().equals(first().from()) || edge.from().equals(last().to());
    }

    /**
     * @return True if the edge is a low-priority road
     */
    private boolean isLowPriorityRoad(final Edge edge)
    {
        return edge.roadFunctionalClass().isLessImportantThan(RoadFunctionalClass.SECOND_CLASS);
    }
}
