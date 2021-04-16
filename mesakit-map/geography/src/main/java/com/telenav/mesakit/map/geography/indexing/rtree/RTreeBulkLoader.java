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

import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramSpatialIndex;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * This is a top-down bulk loader that minimizes overlap. It's roughly based on a very sketchy research paper that was
 * found by a Google search due to an intuition that this would be the best approach to solve the problem.
 *
 * @author jonathanl (shibo)
 * @see "http://ftp.informatik.rwth-aachen.de/Publications/CEUR-WS/Vol-74/files/FORUM_18.pdf"
 */
@UmlClassDiagram(diagram = DiagramSpatialIndex.class)
@UmlRelation(label = "loads", referent = RTreeSpatialIndex.class)
public class RTreeBulkLoader<Element extends Bounded & Intersectable>
{
    private enum Sort
    {
        HORIZONTAL,
        VERTICAL,
        NONE
    }

    private final RTreeSpatialIndex<Element> index;

    public RTreeBulkLoader(final RTreeSpatialIndex<Element> index)
    {
        this.index = index;
    }

    public void load(final List<Element> elements)
    {
        assert !elements.isEmpty();

        var index = 0;
        for (final var element : elements)
        {
            ensureNotNull(element, "Null element at index " + index);
            index++;
        }

        this.index.root(tree(null, Rectangle.MAXIMUM, elements, Sort.NONE));
    }

    public double log(final int base, final int num)
    {
        return Math.log(num) / Math.log(base);
    }

    protected int compareHorizontal(final Element a, final Element b)
    {
        return a.bounds().compareHorizontal(b.bounds());
    }

    protected int compareVertical(final Element a, final Element b)
    {
        return a.bounds().compareVertical(b.bounds());
    }

    private Node<Element> tree(final InteriorNode<Element> parent,
                               final Rectangle bounds,
                               final List<Element> elements,
                               Sort sort)
    {
        // If the elements will all fit in a leaf,
        if (!index.settings().isLeafFull(Count.count(elements)))
        {
            // return a leaf
            final var leaf = index.newLeaf(parent);
            leaf.index(index);
            leaf.addAll(elements);
            return leaf;
        }
        else
        {
            // The height of the tree is log base M of N, where M is the maximum children per
            // interior node and N is the number of elements to be loaded into this subtree
            final var height = ((int) Math
                    .ceil(log(index.settings().maximumChildrenPerInteriorNode().asInt(), elements.size())));

            // The number of elements in each child, N(s), is M ^ height - 1
            final var elementsPerChild = (int) Math
                    .pow(index.settings().maximumChildrenPerInteriorNode().asInt(), height - 1);

            // The number of children is ceiling(N / N(s))
            final var childCount = ((elements.size() + elementsPerChild) / elementsPerChild);

            // Sort elements by longitude or latitude, depending
            final var horizontal = bounds.isHorizontal();
            if (horizontal)
            {
                if (sort != Sort.HORIZONTAL)
                {
                    elements.sort(this::compareHorizontal);
                    sort = Sort.HORIZONTAL;
                }
            }
            else
            {
                if (sort != Sort.VERTICAL)
                {
                    elements.sort(this::compareVertical);
                    sort = Sort.VERTICAL;
                }
            }

            // Build an interior node with the given number of children
            final var node = new InteriorNode<>(index, parent);

            // Create children
            final List<Node<Element>> children = new ArrayList<>();
            var first = 0;
            final var countPerChild = elements.size() / childCount;
            for (var i = 0; i < childCount; i++)
            {
                // Get the child elements to assign to this node
                final var last = i == childCount - 1 ? elements.size() : (i + 1) * countPerChild;
                final List<Element> childElements = new ArrayList<>(elements.subList(first, last));
                first = last;

                // Get the bounding rectangle around the child elements
                final var childBounds = Rectangle.fromBoundedObjects(childElements);

                // Add a child node with the given bounds and elements
                children.add(tree(node, childBounds, childElements, sort));
            }

            // Assign children of this node and bounds
            node.children(children);
            node.bounds(bounds);
            return node;
        }
    }
}
