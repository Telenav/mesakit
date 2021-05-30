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

package com.telenav.tdk.graph.collections;

import com.telenav.tdk.core.kernel.interfaces.code.Callback;
import com.telenav.tdk.core.kernel.interfaces.object.Matcher;
import com.telenav.tdk.core.kernel.language.iteration.Iterables;
import com.telenav.tdk.core.kernel.language.iteration.Next;
import com.telenav.tdk.core.kernel.language.iteration.Streams;
import com.telenav.tdk.core.kernel.language.iteration.Streams.Processing;
import com.telenav.tdk.core.kernel.language.matching.Matching;
import com.telenav.tdk.core.kernel.language.string.StringList;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.core.kernel.messaging.messages.status.Problem;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.scalars.counts.Estimate;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.io.load.GraphConstraints;
import com.telenav.tdk.map.geography.rectangle.Bounded;
import com.telenav.tdk.map.geography.rectangle.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * An arbitrary sequence of edges. Wraps {@link Iterable} and adds convenience methods for working with edge sequences.
 *
 * @author jonathanl (shibo)
 */
public class EdgeSequence implements Iterable<Edge>, Bounded
{
    /**
     * An empty edge sequence
     */
    public static final EdgeSequence EMPTY = new EdgeSequence(EdgeSet.EMPTY);

    public enum Type
    {
        EDGES,
        FORWARD_EDGES
    }

    /** The edges in this sequence */
    protected final Iterable<Edge> edges;

    /** The bounding rectangle of this sequence */
    private Rectangle bounds;

    /**
     * Construct edge sequence
     */
    public EdgeSequence(final Iterable<Edge> edges)
    {
        this.edges = edges;
    }

    /**
     * @return This forward edge sequence where edges are also returned as reverse edges if the edge is two way
     */
    public EdgeSequence asDirectional()
    {
        return new EdgeSequence(Iterables.of(() -> new Next<>()
        {
            private final Iterator<Edge> edges = iterator();

            private Edge last;

            @Override
            public Edge onNext()
            {
                // If out last edge was forward and it's a two-way road,
                if (last != null && last.isForward() && last.isTwoWay())
                {
                    // return the reverse edge next
                    last = last.reversed();
                    return last;
                }

                // If there's a next edge,
                if (edges.hasNext())
                {
                    // return it
                    return last = edges.next();
                }
                return null;
            }
        }));
    }

    /**
     * @return This edge sequence as a list
     */
    public List<Edge> asList()
    {
        final var edges = new ArrayList<Edge>();
        for (final var edge : this)
        {
            edges.add(edge);
        }
        return edges;
    }

    /**
     * @return This edge sequence as a route. If the edge sequence doesn't form a valid route, an exception will be
     * thrown.
     */
    public Route asRoute()
    {
        return Route.forEdges(this);
    }

    public EdgeSet asSet()
    {
        final var set = new EdgeSet();
        for (final var edge : this)
        {
            set.add(edge);
        }
        return set;
    }

    /**
     * @return This sequence as an {@link EdgeSet}
     */
    public EdgeSet asSet(final Estimate estimate)
    {
        return EdgeSet.forIterable(estimate, this);
    }

    /**
     * @return The bounding rectangle for this sequence of edges
     */
    @Override
    public Rectangle bounds()
    {
        if (bounds == null)
        {
            bounds = Rectangle.fromBoundedObjects(edges);
        }
        return bounds;
    }

    /**
     * @return The number of edges in this sequence
     */
    public Count count()
    {
        return Count.of(this);
    }

    /**
     * @param maximum A count after which the program will stop iterating the iterable and return stopAfter.
     * @return The count is smaller than stopAfter, or stopAfter otherwise.
     */
    public Count count(final Count maximum)
    {
        return Count.of(this, maximum);
    }

    /**
     * @return True if any edge in this sequence crosses the given edge
     */
    public boolean crosses(final Edge that)
    {
        for (final var edge : this)
        {
            if (!edge.equals(that) && edge.crosses(that))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The edges in this sequence within the given bounds
     */
    public EdgeSequence intersecting(final Rectangle bounds)
    {
        return matching(Edge.intersecting(bounds));
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
     * @return The sequence of edges matching the given graph constraints
     */
    public EdgeSequence matching(final GraphConstraints constraints)
    {
        return constraints.edges(edges);
    }

    /**
     * @return The edges in this sequence that match the given matcher
     */
    public EdgeSequence matching(final Matcher<Edge> matcher)
    {
        return new EdgeSequence(new Matching<>(matcher)
        {
            @Override
            protected Iterator<Edge> values()
            {
                return edges.iterator();
            }
        });
    }

    /**
     * @return The edges in this sequence of the given type
     */
    public EdgeSequence ofType(final Edge.Type type)
    {
        return matching(edge -> edge.type() == type);
    }

    public Stream<Edge> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    /**
     * Calls the given call back with each edge in this sequence. If an exception is thrown, the sequence is not
     * interrupted, but the listener is called.
     */
    public void process(final Listener<Message> listener, final Callback<Edge> callback)
    {
        for (final var edge : this)
        {
            try
            {
                callback.onCallback(edge);
            }
            catch (final Exception e)
            {
                listener.receive(new Problem(e, "Unable to process edge $", edge));
            }
        }
    }

    public Stream<Edge> stream()
    {
        return Streams.stream(this);
    }

    public Stream<Edge> stream(final Processing processing)
    {
        return Streams.stream(processing, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final var edges = new StringList();
        for (final var edge : this.edges)
        {
            edges.add(edge.identifier().toString());
        }
        return "[EdgeSequence edges = [" + edges.join(", ") + "], bounds = " + bounds() + "]";
    }

    /**
     * @return The edges in this sequence within the given bounds
     */
    public EdgeSequence within(final Rectangle bounds)
    {
        return matching(Edge.within(bounds));
    }
}
