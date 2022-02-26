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

package com.telenav.mesakit.graph.specifications.common.element.store.index;

import com.telenav.kivakit.collections.iteration.iterators.EmptyIterator;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.primitive.collections.iteration.LongIterator;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.Iterator;

final class GraphElementList<T extends GraphElement> implements Iterable<T>
{
    private final GraphElementSpatialIndex<T> index;

    private final Estimate initialSize;

    private LongArray identifiers;

    public GraphElementList(GraphElementSpatialIndex<T> index, Estimate initialSize)
    {
        this.index = index;
        this.initialSize = initialSize;
    }

    public void add(T element)
    {
        identifiers().add(element.identifierAsLong());
    }

    public boolean isEmpty()
    {
        return identifiers == null || identifiers.isEmpty();
    }

    @Override
    public Iterator<T> iterator()
    {
        return iterator(null, null);
    }

    public Iterator<T> iterator(Rectangle bounds, Matcher<T> matcher)
    {
        if (isEmpty())
        {
            return new EmptyIterator<>();
        }
        else
        {
            var outer = this;
            return new BaseIterator<>()
            {
                private final LongIterator identifiers = outer.identifiers.iterator();

                @Override
                protected T onNext()
                {
                    while (identifiers.hasNext())
                    {
                        var next = outer.index.forIdentifier(identifiers.next());
                        if ((bounds == null || next.isInside(bounds)) && (matcher == null || matcher.matches(next)))
                        {
                            return next;
                        }
                    }
                    return null;
                }
            };
        }
    }

    public int size()
    {
        return identifiers == null ? 0 : identifiers.size();
    }

    private LongArray identifiers()
    {
        if (identifiers == null)
        {
            identifiers = new LongArray("identifiers");
            identifiers.initialSize(initialSize);
            identifiers.initialize();
        }
        return identifiers;
    }
}
