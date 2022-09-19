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

import com.telenav.kivakit.interfaces.comparison.Filter;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.io.PrintStream;
import java.util.Iterator;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * A space-efficient read-only spatial index of graph elements (edges or nodes).
 * <p>
 * Elements are stored in quadrants such that they never span more than one quadrant (in the case of edges, both end
 * points must be within a quadrant for it to be stored in that quadrant). When a quadrant is split into sub-quadrants,
 * elements that fit in each sub-quadrant are populated into those quadrants, while elements that don't fit into any
 * sub-quadrant (those which span the newly created sub-quadrants) are retained in the parent quadrant.
 *
 * @author jonathanl (shibo)
 */
public abstract class GraphElementSpatialIndex<T extends GraphElement>
{
    private final Matcher<T> allElements = Filter.acceptingAll();

    private transient Graph graph;

    private final int maximumObjectsPerQuadrant;

    private GraphElementQuadrant<T> root;

    /**
     * Construct
     *
     * @param graph The graph that this index is for
     * @param maximumObjectsPerQuadrant The maximum number of objects in a quadrant before a split is attempted
     */
    protected GraphElementSpatialIndex(Graph graph, int maximumObjectsPerQuadrant)
    {
        this.graph = graph;
        this.maximumObjectsPerQuadrant = maximumObjectsPerQuadrant;
        clear();
    }

    /**
     * Adds the given element
     */
    public void add(T element)
    {
        if (element.graph() != graph)
        {
            fail("Element does not belong to the graph for this spatial index");
        }
        root.add(element);
    }

    /**
     * Adds the given elements
     */
    public void addAll(Iterable<T> elements)
    {
        for (var element : elements)
        {
            add(element);
        }
    }

    /**
     * Clears all elements from this index
     */
    public void clear()
    {
        root = new GraphElementQuadrant<>(this, Rectangle.MAXIMUM);
    }

    /**
     * Dumps the index quadrant tree to the given print stream
     */
    public void dump(PrintStream out)
    {
        root.dump(out, 0);
    }

    public void graph(Graph graph)
    {
        this.graph = graph;
    }

    /**
     * @return All elements completely contained by the given bounding rectangle which match the given matcher
     */
    public Iterable<T> inside(Rectangle bounds, Matcher<T> matcher)
    {
        return new Iterable<>()
        {
            @Override
            public Iterator<T> iterator()
            {
                return root.inside(bounds, matcher);
            }

            @Override
            public String toString()
            {
                /* For debugging purposes */
                var count = 0;
                var builder = new StringBuilder();
                for (var element : this)
                {
                    count++;
                    builder.append(element);
                    builder.append(", ");
                }
                return "Iterable<T> [ size=" + count + ", items=[" + builder + "]]";
            }
        };
    }

    /**
     * @return All elements completely contained by the given bounding rectangle
     */
    public Iterable<T> inside(Rectangle bounds)
    {
        return inside(bounds, allElements);
    }

    /**
     * @return The number of elements in this index
     */
    public int size()
    {
        return root.size();
    }

    /**
     * @return The graph element for the given identifier
     */
    protected abstract T forIdentifier(long identifier);

    protected Graph graph()
    {
        return graph;
    }

    boolean isFull(int size)
    {
        return size > maximumObjectsPerQuadrant;
    }
}
