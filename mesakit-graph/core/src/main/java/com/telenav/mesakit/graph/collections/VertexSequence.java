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

import com.telenav.kivakit.core.collections.iteration.FilteredIterable;
import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.mesakit.graph.GraphLimits.Limit;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.RouteBuilder;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.Iterator;
import java.util.stream.Stream;

/**
 * A sequence of vertexes. Wraps {@link Iterable} and adds convenience methods for working with vertex sequences.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public class VertexSequence implements Iterable<Vertex>
{
    /** The vertexes in the sequence */
    private final Iterable<Vertex> vertexes;

    /**
     * Construct from a sequence of vertexes
     */
    public VertexSequence(Iterable<Vertex> vertexes)
    {
        this.vertexes = vertexes;
    }

    /**
     * @return This sequence of vertexes as a route
     */
    public Route asRoute()
    {
        var builder = new RouteBuilder();

        // Get the sequence of vertexes
        var vertexes = iterator();

        // If there's at least one vertex
        if (vertexes.hasNext())
        {
            // make that the last vertex
            var last = vertexes.next();

            // and so long as we have more vertexes
            while (vertexes.hasNext())
            {
                // get the next vertex
                var next = vertexes.next();

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
        return Count.count(this);
    }

    /**
     * @return The set of all edges connected to vertexes in this sequence
     */
    public EdgeSet edges()
    {
        var edges = new EdgeSet(Limit.EDGES, Estimate._16);
        for (var vertex : this)
        {
            edges.addAll(vertex.edges());
        }
        return edges;
    }

    /**
     * @return The sequence of vertexes within the given bounding rectangle
     */
    public VertexSequence inside(Rectangle bounds)
    {
        return matching(Vertex.inside(bounds));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Vertex> iterator()
    {
        return vertexes.iterator();
    }

    /**
     * @return The sequence of matching vertexes
     */
    public VertexSequence matching(Matcher<Vertex> matcher)
    {
        return new VertexSequence(new FilteredIterable<>(vertexes, matcher));
    }

    public Stream<Vertex> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    public Stream<Vertex> stream()
    {
        return Streams.stream(this);
    }

    public Stream<Vertex> stream(Streams.Processing processing)
    {
        return Streams.stream(processing, this);
    }
}
