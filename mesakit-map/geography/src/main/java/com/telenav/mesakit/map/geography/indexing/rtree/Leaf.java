////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.geography.indexing.rtree;

import com.telenav.kivakit.core.collections.iteration.FilteredIterator;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.Iterator;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * A leaf node holds only elements and has no children.
 */
public abstract class Leaf<T extends Bounded & Intersectable> extends Node<T>
{
    protected Leaf(RTreeSpatialIndex<T> index, InteriorNode<T> parent)
    {
        super(index, parent);
    }

    protected Leaf()
    {
    }

    /**
     * Adds the given element to this leaf node. If any parent nodes need to be expanded to encompass the element, they
     * are expanded. If the node is too full, a split is performed.
     */
    @Override
    public void add(T element)
    {
        // If we're out of room in this leaf,
        if (isFull())
        {
            // split this leaf and add to the parent which will now have room
            split().add(element);
        }
        else
        {
            // Add the element and update debug view
            addElement(element);
            index().debugger().update(this, element);

            // Expand the parents of this element if need be
            expandToInclude(element.bounds());
        }
    }

    public abstract void addAll(List<T> elements);

    /**
     * @return An iterator over all elements in this leaf node intersecting the given rectangle. It is up to the element
     * to implement this efficiently.
     */
    @Override
    public Iterator<T> intersecting(Rectangle that, Matcher<T> matcher)
    {
        return new FilteredIterator<>(elements().iterator(), value -> value.intersects(that) && matcher.matches(value));
    }

    @Override
    public boolean isLeaf()
    {
        return true;
    }

    @Override
    public void statistics(int depth, Statistics statistics)
    {
        statistics.leaves++;
        statistics.elements += size();
        statistics.maximumDepth = Math.max(depth, statistics.maximumDepth);
    }

    @Override
    public String toString()
    {
        return toString(RTreeSpatialIndex.DumpDetailLevel.DEFAULT);
    }

    @Override
    protected void add(Node<T> child)
    {
        unsupported("Leaf node cannot add child $", child);
    }

    protected abstract void addElement(T element);

    protected final Count count()
    {
        return Count.count(size());
    }

    protected abstract Iterable<T> elements();

    @Override
    protected boolean isFull()
    {
        return settings().isLeafFull(Count.count(size()));
    }

    protected abstract int size();

    @Override
    protected String toString(RTreeSpatialIndex.DumpDetailLevel detail)
    {
        return "[Leaf bounds = " + bounds() + ", size = " + size() + ", elements = "
                + (detail == RTreeSpatialIndex.DumpDetailLevel.SUMMARY_ONLY
                ? "" + size()
                : new ObjectList<>().appendThen(elements()).join())
                + "]";
    }

    /**
     * Splits this leaf node into two leaves.
     *
     * @return The parent of the new leaves
     */
    private InteriorNode<T> split()
    {
        // Split the elements into two
        return new LinearSplit<T>()
        {
            @Override
            protected InteriorNode<T> onSplit(T a, T b)
            {
                // Create two un-parented leaves for the most distant elements
                var leafA = index().newLeaf(null);
                var leafB = index().newLeaf(null);
                leafA.add(a);
                leafB.add(b);

                // Go through all elements that need to be split
                for (var element : elements())
                {
                    // and if it's not one of the two we started with,
                    if (!element.equals(a) && !element.equals(b))
                    {
                        var center = element.bounds().center();
                        if (center.preciseDistanceTo(a.bounds().center())
                                .isLessThan(center.preciseDistanceTo(b.bounds().center())))
                        {
                            leafA.add(element);
                        }
                        else
                        {
                            leafB.add(element);
                        }
                    }
                }

                // Add leaves to parent (which might cause an internal node split)
                return replace(leafA, leafB);
            }
        }.split(elements());
    }
}
