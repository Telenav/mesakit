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

import com.telenav.kivakit.core.collections.iteration.CompoundIterator;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.language.count.Count;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An interior node in the tree, holding only child nodes
 */
public class InteriorNode<T extends Bounded & Intersectable> extends Node<T>
{
    /** The children of this interior node */
    List<Node<T>> children;

    public InteriorNode(RTreeSpatialIndex<T> index, InteriorNode<T> parent)
    {
        super(index, parent);
        children = new ArrayList<>(index.settings().maximumChildrenPerInteriorNode().asInt());
    }

    protected InteriorNode()
    {
    }

    /**
     * Adds the given element to the child node which would be least expanded by the addition
     */
    @Override
    public void add(T element)
    {
        // Add the element to the appropriate child
        bestFit(children(), element).add(element);
    }

    public void children(List<Node<T>> children)
    {
        this.children = children;
    }

    @Override
    public void dump(PrintStream out, int level, RTreeSpatialIndex.DumpDetailLevel detail)
    {
        super.dump(out, level, detail);
        for (var child : children())
        {
            child.dump(out, level + 1, detail);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof InteriorNode)
        {
            var that = (InteriorNode) object;
            return super.equals(that) && children.equals(that.children);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(super.hashCode(), children.hashCode());
    }

    /**
     * @return Any elements under this interior node that intersect the given rectangle
     */
    @Override
    public Iterator<T> intersecting(Rectangle that, Matcher<T> matcher)
    {
        // Go through each child node,
        var matches = new CompoundIterator<T>();
        for (var child : children())
        {
            // and if the child's bounds intersects the given rectangle,
            if (that.intersects(child.bounds()))
            {
                // add any intersecting elements from the child.
                matches.add(child.intersecting(that, matcher));
            }
        }
        return matches;
    }

    @Override
    public boolean isLeaf()
    {
        return false;
    }

    @Override
    public void statistics(int depth, Statistics statistics)
    {
        statistics.interiorNodes++;
        statistics.maximumDepth = Math.max(depth, statistics.maximumDepth);
        for (var child : children())
        {
            child.statistics(depth + 1, statistics);
        }
    }

    @Override
    public String toString()
    {
        return toString(RTreeSpatialIndex.DumpDetailLevel.DEFAULT);
    }

    /**
     * Adds the given node to this interior node. If any parent nodes need to be expanded to encompass the new node,
     * they are expanded. If this results in too many nodes, a split is performed.
     */
    @Override
    protected void add(Node<T> child)
    {
        // If there are too many children,
        if (isFull())
        {
            // split this node and add the given node to the parent
            // the split and add are done as a single operation (as opposed to leaf nodes)
            // because the child has to be added to one of the split results (not to their
            // parent)
            splitAndAdd(child);
        }
        else
        {
            // Add the new child and update debug view
            child.parent(this);
            children().add(child);
            index().debugger().update(child);

            // Expand the parents of this node if need be
            expandToInclude(child.bounds());
        }
    }

    @Override
    protected boolean isFull()
    {
        return settings().isInteriorNodeFull(Count.count(children));
    }

    protected void remove(Node<T> node)
    {
        children().remove(node);
        index().debugger().remove(node);
    }

    @Override
    protected String toString(RTreeSpatialIndex.DumpDetailLevel detail)
    {
        return "[InteriorNode bounds = " + bounds() + ", children = " + children().size() + "]";
    }

    List<Node<T>> children()
    {
        return children;
    }

    /**
     * Splits this interior node in two and adds to one of them the given node.
     *
     * @return the parent of the two resulting nodes
     */
    @SuppressWarnings("UnusedReturnValue")
    private InteriorNode<Node<T>> splitAndAdd(Node<T> reasonForSplit)
    {
        // Split the children into two
        return new LinearSplit<Node<T>>()
        {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            protected InteriorNode<Node<T>> onSplit(Node<T> childA, Node<T> childB)
            {
                // Create two buckets for the most distant elements
                var bucketA = new InteriorNode(index(), null);
                var bucketB = new InteriorNode(index(), null);
                bucketA.add(childA);
                bucketB.add(childB);

                // Go through all elements that need to be split
                for (var child : children())
                {
                    // and if it's not one of the two we started with,
                    if (!child.equals(childA) && !child.equals(childB))
                    {
                        addChildToBestBucket(child, bucketA, bucketB);
                    }
                }
                addChildToBestBucket(reasonForSplit, bucketA, bucketB);

                // Remove this node and add the two buckets
                return replace(bucketA, bucketB);
            }

            private void addChildToBestBucket(Node<T> child, InteriorNode<T> bucketA,
                                              InteriorNode<T> bucketB)
            {
                var center = child.bounds().center();
                if (center.preciseDistanceTo(bucketA.bounds().center())
                        .isLessThan(center.preciseDistanceTo(bucketB.bounds().center())))
                {
                    bucketA.add(child);
                }
                else
                {
                    bucketB.add(child);
                }
            }
        }.split(children());
    }
}
