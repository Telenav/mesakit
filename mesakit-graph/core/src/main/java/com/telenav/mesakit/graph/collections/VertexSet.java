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

import com.telenav.kivakit.collections.set.operations.Intersection;
import com.telenav.kivakit.collections.set.operations.Subset;
import com.telenav.kivakit.collections.set.operations.Union;
import com.telenav.kivakit.collections.set.operations.Without;
import com.telenav.kivakit.core.collections.set.ObjectSet;
import com.telenav.kivakit.core.string.Join;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A set of vertexes. Supports {@link #union(Set)}, {@link #intersection(Set)} and {@link #without(Set)} operations,
 * that logically combine this set of vertexes with another set of vertexes without creating a physical one.
 *
 * @author jonathanl (shibo)
 */
public class VertexSet extends ObjectSet<Vertex>
{
    /**
     * Returns a set of vertexes for the given vertex collection
     */
    public static VertexSet forCollection(Maximum maximum, Collection<? extends Vertex> collection)
    {
        if (collection instanceof VertexSet)
        {
            return (VertexSet) collection;
        }
        else
        {
            var set = new VertexSet(maximum);
            set.addAll(collection);
            return set;
        }
    }

    /**
     * A set of vertexes for a sequence of vertexes
     */
    public static VertexSet forIterable(Maximum maximum, Iterable<? extends Vertex> collection)
    {
        var set = new VertexSet(maximum);
        set.addAll(collection);
        return set;
    }

    /**
     * Returns a single vertex set
     */
    public static VertexSet singleton(Vertex vertex)
    {
        var set = new VertexSet(Maximum._1);
        set.add(vertex);
        return set;
    }

    /**
     * Construct a vertex set with a given maximum
     */
    public VertexSet()
    {
        super(Maximum.maximum(64));
    }

    /**
     * Construct a vertex set with a given maximum
     */
    public VertexSet(Maximum maximum)
    {
        super(maximum);
    }

    /**
     * Construct from an underlying vertex set
     */
    public VertexSet(Set<Vertex> vertexes)
    {
        super(Maximum.MAXIMUM, vertexes);
    }

    /**
     * Returns the set of vertexes in this set within the bounding rectangle
     */
    public VertexSet inside(Rectangle bounds)
    {
        return matchingAsIterable(Vertex.inside(bounds));
    }

    /**
     * Returns the intersection of this vertex set with the given set of vertexes
     */
    public VertexSet intersection(Set<Vertex> vertexes)
    {
        return new VertexSet(new Intersection<>(this, vertexes));
    }

    /**
     * Returns all the vertexes identifiers in this set joined into a string using the given separator. The order of
     * identifiers is undefined.
     */
    public String joinedIdentifiers(String separator)
    {
        return Join.join(this, separator, value -> Long.toString(value.identifierAsLong()));
    }

    /**
     * Returns matching vertexes
     */
    @Override
    public VertexSet matchingAsIterable(Matcher<Vertex> matcher)
    {
        return new VertexSet(new Subset<>(this, matcher));
    }

    /**
     * Returns the union of this set of vertexes with that set of vertexes
     */
    public VertexSet union(Set<Vertex> that)
    {
        return new VertexSet(new Union<>(this, that));
    }

    /**
     * Returns this set of vertexes without the given vertexes
     */
    public VertexSet without(Set<Vertex> exclude)
    {
        return new VertexSet(new Without<>(this, exclude));
    }

    /**
     * Returns this set of vertexes excluding the given vertex
     */
    public VertexSet without(Vertex exclude)
    {
        return without(Collections.singleton(exclude));
    }
}
