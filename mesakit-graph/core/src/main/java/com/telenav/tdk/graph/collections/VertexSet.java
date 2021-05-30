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

import com.telenav.tdk.core.collections.set.BoundedSet;
import com.telenav.tdk.core.collections.set.WrappedSet;
import com.telenav.tdk.core.collections.set.operations.Intersection;
import com.telenav.tdk.core.collections.set.operations.Subset;
import com.telenav.tdk.core.collections.set.operations.Union;
import com.telenav.tdk.core.collections.set.operations.Without;
import com.telenav.tdk.core.kernel.conversion.BaseConverter;
import com.telenav.tdk.core.kernel.conversion.collection.BaseSetConverter;
import com.telenav.tdk.core.kernel.interfaces.object.Matcher;
import com.telenav.tdk.core.kernel.language.string.Strings;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.core.kernel.messaging.listeners.FailureThrower;
import com.telenav.tdk.core.kernel.scalars.counts.Estimate;
import com.telenav.tdk.graph.Graph;
import com.telenav.tdk.graph.Vertex;
import com.telenav.tdk.map.geography.rectangle.Rectangle;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A set of vertexes. Supports {@link #union(Set)}, {@link #intersection(Set)} and {@link #without(Set)} operations,
 * that logically combine this set of vertexes with another set of vertexes without creating a physical one.
 *
 * @author jonathanl (shibo)
 */
public class VertexSet extends WrappedSet<Vertex>
{
    /**
     * @return A set of vertexes for the given vertex collection
     */
    public static VertexSet forCollection(final Estimate estimate, final Collection<? extends Vertex> collection)
    {
        if (collection instanceof VertexSet)
        {
            return (VertexSet) collection;
        }
        else
        {
            final var set = new VertexSet(estimate);
            set.addAll(collection);
            return set;
        }
    }

    /**
     * A set of vertexes for a sequence of vertexes
     */
    public static VertexSet forIterable(final Estimate estimate, final Iterable<? extends Vertex> collection)
    {
        final var set = new VertexSet(estimate);
        set.addAll(collection);
        return set;
    }

    /**
     * @return A single vertex set
     */
    public static VertexSet singleton(final Vertex vertex)
    {
        final var set = new VertexSet(Estimate._1);
        set.add(vertex);
        return set;
    }

    public static class Converter extends BaseSetConverter<Vertex>
    {
        public Converter(final Graph graph, final Listener<Message> listener)
        {
            super(listener, new Vertex.Converter(graph, listener), ",");
        }
    }

    /**
     * Construct a vertex set with a given maximum
     */
    public VertexSet()
    {
        super(new BoundedSet<>(Estimate._16));
    }

    /**
     * Construct a vertex set with a given maximum
     */
    public VertexSet(final Estimate estimate)
    {
        super(new BoundedSet<>(estimate));
    }

    /**
     * Construct from an underlying vertex set
     */
    public VertexSet(final Set<Vertex> vertexes)
    {
        super(vertexes);
    }

    /**
     * @return The set of vertexes in this set within the bounding rectangle
     */
    public VertexSet inside(final Rectangle bounds)
    {
        return matching(Vertex.inside(bounds));
    }

    /**
     * @return The intersection of this vertex set with the given set of vertexes
     */
    public VertexSet intersection(final Set<Vertex> vertexes)
    {
        return new VertexSet(new Intersection<>(this, vertexes));
    }

    /**
     * @return All the vertexes identifiers in this set joined into a string using the given separator. The order of
     * identifiers is undefined.
     */
    public String joinedIdentifiers(final String separator)
    {
        return Strings.join(this, separator, new BaseConverter<>(new FailureThrower<>())
        {
            @Override
            protected String onConvert(final Vertex value)
            {
                return Long.toString(value.identifierAsLong());
            }
        });
    }

    /**
     * @return Matching vertexes
     */
    public VertexSet matching(final Matcher<Vertex> matcher)
    {
        return new VertexSet(new Subset<>(this, matcher));
    }

    /**
     * @return The union of this set of vertexes with that set of vertexes
     */
    public VertexSet union(final Set<Vertex> that)
    {
        return new VertexSet(new Union<>(this, that));
    }

    /**
     * @return This set of vertexes without the given vertexes
     */
    public VertexSet without(final Set<Vertex> exclude)
    {
        return new VertexSet(new Without<>(this, exclude));
    }

    /**
     * @return This set of vertexes excluding the given vertex
     */
    public VertexSet without(final Vertex exclude)
    {
        return without(Collections.singleton(exclude));
    }
}
