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

import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.matching.All;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.io.PrintStream;
import java.util.Iterator;

import static com.telenav.kivakit.kernel.validation.Validate.fail;

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
    private final int maximumObjectsPerQuadrant;

    private GraphElementQuadrant<T> root;

    private transient Graph graph;

    private final Matcher<T> allElements = new All<>();

    /**
     * Construct
     *
     * @param graph The graph that this index is for
     * @param maximumObjectsPerQuadrant The maximum number of objects in a quadrant before a split is attempted
     */
    protected GraphElementSpatialIndex(final Graph graph, final int maximumObjectsPerQuadrant)
    {
        this.graph = graph;
        this.maximumObjectsPerQuadrant = maximumObjectsPerQuadrant;
        clear();
    }

    /**
     * Adds the given element
     */
    public void add(final T element)
    {
        if (element.graph() != this.graph)
        {
            fail("Element does not belong to the graph for this spatial index");
        }
        this.root.add(element);
    }

    /**
     * Adds the given elements
     */
    public void addAll(final Iterable<T> elements)
    {
        for (final var element : elements)
        {
            add(element);
        }
    }

    /**
     * Clears all elements from this index
     */
    public void clear()
    {
        this.root = new GraphElementQuadrant<>(this, Rectangle.MAXIMUM);
    }

    /**
     * Dumps the index quadrant tree to the given print stream
     */
    public void dump(final PrintStream out)
    {
        this.root.dump(out, 0);
    }

    public void graph(final Graph graph)
    {
        this.graph = graph;
    }

    /**
     * @return All elements completely contained by the given bounding rectangle which match the given matcher
     */
    public Iterable<T> inside(final Rectangle bounds, final Matcher<T> matcher)
    {
        return new Iterable<>()
        {
            @SuppressWarnings("NullableProblems")
            @Override
            public Iterator<T> iterator()
            {
                return GraphElementSpatialIndex.this.root.inside(bounds, matcher);
            }

            @Override
            public String toString()
            {
                /* For debugging purposes */
                var count = 0;
                final var sb = new StringBuilder();
                for (final var element : this)
                {
                    count++;
                    sb.append(element);
                    sb.append(", ");
                }
                return "Iterable<T> [ size=" + count + ", items=[" + sb.toString() + "]]";
            }
        };
    }

    /**
     * @return All elements completely contained by the given bounding rectangle
     */
    public Iterable<T> inside(final Rectangle bounds)
    {
        return inside(bounds, this.allElements);
    }

    /**
     * @return The number of elements in this index
     */
    public int size()
    {
        return this.root.size();
    }

    /**
     * @return The graph element for the given identifier
     */
    protected abstract T forIdentifier(long identifier);

    protected Graph graph()
    {
        return this.graph;
    }

    boolean isFull(final int size)
    {
        return size > this.maximumObjectsPerQuadrant;
    }
}
