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

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex.DumpDetailLevel;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * Base class for interior nodes and leaf nodes.
 */
@SuppressWarnings("rawtypes")
public abstract class Node<T extends Bounded & Intersectable> implements Bounded, Intersectable
{
    long bottomLeft = Location.NULL;

    long topRight = Location.NULL;

    private RTreeSpatialIndex<T> index;

    private InteriorNode<T> parent;

    protected Node(final RTreeSpatialIndex<T> index, final InteriorNode<T> parent)
    {
        this.index = index;
        this.parent = parent;
    }

    protected Node()
    {
    }

    public abstract void add(final T element);

    @Override
    public Rectangle bounds()
    {
        if (bottomLeft != Location.NULL && topRight != Location.NULL)
        {
            return Rectangle.fromLongs(bottomLeft, topRight);
        }
        return null;
    }

    public void bounds(final Rectangle bounds)
    {
        // Get bounds long values
        final var bottomLeft = bounds.bottomLeft().asLong();
        final var topRight = bounds.topRight().asLong();

        // If the bounds are changing
        final var changed = bottomLeft != this.bottomLeft || topRight != this.topRight;

        // Change the bounds
        this.bottomLeft = bottomLeft;
        this.topRight = topRight;

        // If the bounds changed,
        if (changed)
        {
            // then update the debug view
            index().debugger().update(this);
        }
    }

    public void dump(final PrintStream out, final int level, final DumpDetailLevel detail)
    {
        out.println(AsciiArt.repeat(level, " ") + toString(detail));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Node)
        {
            final var that = (Node<T>) object;
            return bottomLeft == that.bottomLeft && topRight == that.topRight;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(bottomLeft, topRight);
    }

    public void index(final RTreeSpatialIndex<T> index)
    {
        this.index = index;
    }

    public RTreeSpatialIndex<T> index()
    {
        return index;
    }

    public abstract Iterator<T> intersecting(final Rectangle that, final Matcher<T> matcher);

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        return bounds().intersects(rectangle);
    }

    public abstract boolean isLeaf();

    public abstract void statistics(int depth, final Statistics statistics);

    protected abstract void add(Node<T> child);

    /**
     * @return The node from the given list whose area would be least increased by the addition of the given element
     */
    protected <N extends Node<T>> Node<T> bestFit(final List<N> nodes, final Bounded element)
    {
        // Find the node whose area will increase the least by adding the given element
        Area minimumIncrease = null;
        N minimum = null;
        for (final var node : nodes)
        {
            final var before = node.bounds().area();
            final var after = node.bounds().union(element.bounds()).area();
            final var increase = after.minus(before);
            if (minimum == null || increase.isLessThan(minimumIncrease))
            {
                minimum = node;
                minimumIncrease = increase;
            }
        }
        return minimum;
    }

    protected void expandToInclude(final Rectangle bounds)
    {
        // Walk up the parent tree
        for (var at = this; at != null; at = at.parent())
        {
            final var current = at.bounds();
            if (current == null)
            {
                at.bounds(bounds);
            }
            else
            {
                // If the parent fully contains the bounded object,
                if (current.contains(bounds))
                {
                    // we're done
                    break;
                }

                // otherwise, expand the current bounds to include the bounds
                at.bounds(current.union(bounds));
            }
        }
    }

    protected abstract boolean isFull();

    protected InteriorNode<T> parent()
    {
        return parent;
    }

    protected void parent(final InteriorNode<T> parent)
    {
        this.parent = parent;
    }

    /**
     * Replaces the given node with the two sibling nodes as the result of a split operation, returning the parent of
     * the new nodes
     *
     * @return A new interior node that is the parent of the two given nodes
     */
    @SuppressWarnings({ "unchecked" })
    protected InteriorNode<T> replace(final Node<T> siblingA, final Node<T> siblingB)
    {
        // If there is no parent
        if (parent == null)
        {
            // create a new root node that we can add to
            parent = new InteriorNode(index, null);
            index.root(parent);
        }
        else
        {
            // otherwise, remove the node from the parent
            parent.remove(this);
        }

        // Add a and b to the parent (which might cause splitting)
        parent.add(siblingA);
        parent.add(siblingB);

        // The parent that can be added to now
        return parent;
    }

    protected RTreeSettings settings()
    {
        return index.settings();
    }

    protected abstract String toString(DumpDetailLevel detail);
}
