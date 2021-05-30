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

package com.telenav.tdk.graph.specifications.common.element.store.index;

import com.telenav.tdk.core.collections.iteration.iterators.EmptyIterator;
import com.telenav.tdk.core.collections.primitive.array.scalars.LongArray;
import com.telenav.tdk.core.collections.primitive.iteration.LongIterator;
import com.telenav.tdk.core.kernel.interfaces.object.Matcher;
import com.telenav.tdk.core.kernel.language.iteration.BaseIterator;
import com.telenav.tdk.core.kernel.scalars.counts.Estimate;
import com.telenav.tdk.graph.GraphElement;
import com.telenav.tdk.map.geography.rectangle.Rectangle;

import java.util.Iterator;

final class GraphElementList<T extends GraphElement> implements Iterable<T>
{
    private final GraphElementSpatialIndex<T> index;

    private final Estimate initialSize;

    private LongArray identifiers;

    public GraphElementList(final GraphElementSpatialIndex<T> index, final Estimate initialSize)
    {
        this.index = index;
        this.initialSize = initialSize;
    }

    public void add(final T element)
    {
        identifiers().add(element.identifierAsLong());
    }

    public boolean isEmpty()
    {
        return this.identifiers == null || this.identifiers.isEmpty();
    }

    @Override
    public Iterator<T> iterator()
    {
        return iterator(null, null);
    }

    public Iterator<T> iterator(final Rectangle bounds, final Matcher<T> matcher)
    {
        if (isEmpty())
        {
            return new EmptyIterator<>();
        }
        else
        {
            final var outer = this;
            return new BaseIterator<>()
            {
                private final LongIterator identifiers = outer.identifiers.iterator();

                @Override
                protected T onNext()
                {
                    while (this.identifiers.hasNext())
                    {
                        final var next = outer.index.forIdentifier(this.identifiers.next());
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
        return this.identifiers == null ? 0 : this.identifiers.size();
    }

    private LongArray identifiers()
    {
        if (this.identifiers == null)
        {
            this.identifiers = new LongArray("identifiers");
            this.identifiers.initialSize(this.initialSize);
            this.identifiers.initialize();
        }
        return this.identifiers;
    }
}
