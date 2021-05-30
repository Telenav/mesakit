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

import com.telenav.tdk.core.kernel.interfaces.object.Matcher;
import com.telenav.tdk.core.kernel.language.iteration.Streams;
import com.telenav.tdk.core.kernel.language.iteration.Streams.Processing;
import com.telenav.tdk.core.kernel.language.matching.Matching;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.scalars.counts.Estimate;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.RouteBuilder;
import com.telenav.tdk.graph.Vertex;
import com.telenav.tdk.graph.project.TdkGraphCoreLimits.Limit;
import com.telenav.tdk.map.geography.rectangle.Rectangle;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A sequence of vertexes. Wraps {@link Iterable} and adds convenience methods for working with vertex sequences.
 *
 * @author jonathanl (shibo)
 */
public class VertexSequence implements Iterable<Vertex>
{
    /** The vertexes in the sequence */
    private final Iterable<Vertex> vertexes;

    /**
     * Construct from a sequence of vertexes
     */
    public VertexSequence(final Iterable<Vertex> vertexes)
    {
        this.vertexes = vertexes;
    }

    /**
     * @return This sequence of vertexes as a route
     */
    public Route asRoute()
    {
        final var builder = new RouteBuilder();

        // Get the sequence of vertexes
        final var vertexes = iterator();

        // If there's at least one vertex
        if (vertexes.hasNext())
        {
            // make that the last vertex
            var last = vertexes.next();

            // and so long as we have more vertexes
            while (vertexes.hasNext())
            {
                // get the next vertex
                final var next = vertexes.next();

                // and add the edge between the last vertex and the next vertex
                builder.append(last.edgeBetween(next));

                // the next vertex is now the last vertex
                last = next;
            }
        }
        return builder.route();
    }

    /**
     * @return The number of vertexes in this vertex sequence
     */
    public Count count()
    {
        return Count.of(this);
    }

    /**
     * @return The set of all edges connected to vertexes in this sequence
     */
    public EdgeSet edges()
    {
        final var edges = new EdgeSet(Limit.EDGES, Estimate._16);
        for (final var vertex : this)
        {
            edges.addAll(vertex.edges());
        }
        return edges;
    }

    /**
     * @return The sequence of vertexes within the given bounding rectangle
     */
    public VertexSequence inside(final Rectangle bounds)
    {
        return matching(Vertex.inside(bounds));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Vertex> iterator()
    {
        return vertexes.iterator();
    }

    /**
     * @return The sequence of matching vertexes
     */
    public VertexSequence matching(final Matcher<Vertex> matcher)
    {
        return new VertexSequence(new Matching<>(matcher)
        {
            @Override
            protected Iterator<Vertex> values()
            {
                return vertexes.iterator();
            }
        });
    }

    public Stream<Vertex> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    public Stream<Vertex> stream()
    {
        return Streams.stream(this);
    }

    public Stream<Vertex> stream(final Processing processing)
    {
        return Streams.stream(processing, this);
    }
}
