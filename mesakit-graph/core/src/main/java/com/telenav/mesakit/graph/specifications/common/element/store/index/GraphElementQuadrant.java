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

import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.collections.iteration.BaseIterator;
import com.telenav.kivakit.core.language.strings.AsciiArt;
import com.telenav.kivakit.language.count.Estimate;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.io.PrintStream;
import java.util.Iterator;

final class GraphElementQuadrant<T extends GraphElement>
{
    private final class QuadrantIterator extends BaseIterator<T>
    {
        private final Rectangle bounds;

        private int childIndex = -1;

        private Iterator<T> iterator;

        private QuadrantIterator(Rectangle bounds, Matcher<T> matcher)
        {
            this.bounds = bounds;
            filter(matcher);
        }

        @Override
        protected T onNext()
        {
            do
            {
                if (iterator == null || !iterator.hasNext())
                {
                    iterator = nextIterator();
                }
                if (iterator != null)
                {
                    if (iterator.hasNext())
                    {
                        return iterator.next();
                    }
                }
            }
            while (iterator != null);
            return null;
        }

        private Iterator<T> nextIterator()
        {
            // If we're at index -1
            if (childIndex == -1)
            {
                // advance to first child index
                childIndex = 0;

                // and if we have edges in this quadrant,
                if (!edges.isEmpty())
                {
                    // and there are matches,
                    var iterator = edges.iterator(bounds, filter());
                    if (iterator.hasNext())
                    {
                        // return the iterator
                        return iterator;
                    }
                }
            }

            if (!isLeaf())
            {
                // Look through each child quadrant from 0 to 3
                while (childIndex < 4)
                {
                    var child = children[childIndex++];
                    if (!child.isEmpty() && bounds.intersects(child.bounds))
                    {
                        var iterator = child.inside(bounds, filter());
                        if (iterator.hasNext())
                        {
                            return iterator;
                        }
                    }
                }
            }
            return null;
        }
    }

    private final GraphElementSpatialIndex<T> index;

    private GraphElementList<T> edges;

    private final Rectangle bounds;

    private GraphElementQuadrant<T>[] children;

    private int size;

    GraphElementQuadrant(GraphElementSpatialIndex<T> index, Rectangle bounds)
    {
        this.index = index;
        this.bounds = bounds;
        edges = new GraphElementList<>(index, Estimate._1024);
    }

    @Override
    public String toString()
    {
        return "[" + (isLeaf() ? "Leaf" : "Quadrant") + " " + bounds.toString() + ", " + size + " ("
                + edges.size() + ") edges]";
    }

    boolean add(T element)
    {
        // If the edge is fully contained in this quadrant
        if (element.isInside(bounds))
        {
            // If this quadrant is a leaf,
            if (isLeaf())
            {
                // Simply store the edge identifier
                edges.add(element);
                size++;

                // Then, if this quadrant is full,
                if (index.isFull(edges.size()))
                {
                    // split the quadrant into 4 children
                    split();
                }
            }
            else
            {
                // Try to add the edge to all four child quadrants
                var added = false;
                for (var child : children)
                {
                    // If it was added to the child,
                    if (child.add(element))
                    {
                        // we're done
                        size++;
                        added = true;
                        break;
                    }
                }

                // If we couldn't add it anywhere,
                if (!added)
                {
                    // we will just retain it in this quadrant
                    edges.add(element);
                    size++;
                }
            }

            // The element was successfully added to this quadrant
            return true;
        }
        else
        {
            // The element is not fully contained and so cannot be added, which means the parent
            // quadrant is doing a split and will wind up retaining it.
            return false;
        }
    }

    void dump(PrintStream out, int level)
    {
        out.println(AsciiArt.repeat(level, " ") + this);
        if (!isLeaf())
        {
            for (var child : children)
            {
                if (!child.isEmpty())
                {
                    child.dump(out, level + 1);
                }
            }
        }
    }

    Iterator<T> inside(Rectangle rectangle, Matcher<T> matcher)
    {
        return new QuadrantIterator(rectangle, matcher);
    }

    int size()
    {
        return size;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isEmpty()
    {
        return size == 0;
    }

    private boolean isLeaf()
    {
        return children == null;
    }

    @SuppressWarnings("unchecked")
    private void split()
    {
        // Create four child quadrants
        children = new GraphElementQuadrant[4];
        children[0] = new GraphElementQuadrant<>(index, bounds.northWestQuadrant());
        children[1] = new GraphElementQuadrant<>(index, bounds.northEastQuadrant());
        children[2] = new GraphElementQuadrant<>(index, bounds.southWestQuadrant());
        children[3] = new GraphElementQuadrant<>(index, bounds.southEastQuadrant());

        // Keep a list of the edges to add
        var toAdd = edges;

        // Then clear the element list
        edges = new GraphElementList<>(index, Estimate._1024);
        size = 0;

        // and finally, add each edge
        for (var element : toAdd)
        {
            add(element);
        }
    }
}
