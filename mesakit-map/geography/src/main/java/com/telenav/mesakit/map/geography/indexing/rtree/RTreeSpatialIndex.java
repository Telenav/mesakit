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
import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.core.kernel.language.iteration.Iterables;
import com.telenav.kivakit.core.kernel.language.iteration.Next;
import com.telenav.kivakit.core.kernel.language.objects.Objects;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.messaging.filters.operators.All;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

/**
 * An r-tree spatial index with a simple linear splitting algorithm that sorts elements by latitude and longitude of
 * their center and picks the two most distant as the beginning of the split. Otherwise, this is a linear r-tree as
 * described in http://en.wikipedia.org/wiki/R-tree.
 *
 * @param <Element> An element must be {@link Bounded}, and {@link Intersectable} in order to be spatially indexed by an
 * r-tree.
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramSpatialIndex.class)
@UmlExcludeSuperTypes(NamedObject.class)
public class RTreeSpatialIndex<Element extends Bounded & Intersectable> implements Bounded, NamedObject
{
    public enum DumpDetailLevel
    {
        SUMMARY_ONLY,
        SHOW_OBJECTS;

        public static final DumpDetailLevel DEFAULT = SHOW_OBJECTS;
    }

    /** The root of the tree, initially just a root node with a single leaf */
    public Node<Element> root;

    /** Settings that determine how the tree is laid out */
    @KivaKitIncludeProperty
    private RTreeSettings settings;

    /** Matcher that matches all elements */
    private final Matcher<Element> allElements = new All<>();

    private String objectName;

    /** Debugger interface */
    private RTreeSpatialIndexDebugger<Element> debugger = RTreeSpatialIndexDebugger.nullDebugger();

    /**
     * Construct with good defaults
     */
    public RTreeSpatialIndex(final String objectName, final RTreeSettings settings)
    {
        assert objectName != null;

        this.objectName = objectName;
        this.settings = settings;
        root = newLeaf(null);
    }

    protected RTreeSpatialIndex()
    {
    }

    /**
     * Adds the given element to this spatial index
     */
    public synchronized void add(final Element element)
    {
        root().add(element);
    }

    public synchronized Iterable<Element> all()
    {
        return intersecting(Rectangle.MAXIMUM);
    }

    @Override
    public Rectangle bounds()
    {
        return root.bounds();
    }

    public void bulkLoad(final List<Element> elements)
    {
        new RTreeBulkLoader<>(this).load(elements);
    }

    @KivaKitIncludeProperty
    public Count count()
    {
        return Count.count(all());
    }

    public void debugger(final RTreeSpatialIndexDebugger<Element> debugger)
    {
        this.debugger = debugger;
    }

    public RTreeSpatialIndexDebugger<Element> debugger()
    {
        return debugger;
    }

    public void dump(final PrintStream out)
    {
        dump(out, DumpDetailLevel.SHOW_OBJECTS);
    }

    /**
     * Dumps the r-tree to the given print stream
     */
    public void dump(final PrintStream out, final DumpDetailLevel detail)
    {
        out.println(statistics());
        root().dump(out, 1, detail);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RTreeSpatialIndex)
        {
            final var that = (RTreeSpatialIndex) object;
            return Objects.equalPairs(root, that.root);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return root.hashCode();
    }

    /**
     * @return An iterator of elements that intersect with the given bounding rectangle
     */
    public synchronized Iterable<Element> intersecting(final Rectangle bounds)
    {
        return intersecting(bounds, allElements);
    }

    /**
     * @return An iterator of elements that intersect with the given bounding rectangle and match the given matcher
     */
    public synchronized Iterable<Element> intersecting(final Rectangle bounds, final Matcher<Element> matcher)
    {
        return Iterables.iterable(() -> new Next<>()
        {
            final Iterator<Element> elements = root().intersecting(bounds, matcher);

            @Override
            public Element onNext()
            {
                if (elements.hasNext())
                {
                    return elements.next();
                }
                return null;
            }
        });
    }

    public Leaf<Element> newLeaf(final InteriorNode<Element> parent)
    {
        return new UncompressedLeaf<>(this, parent);
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void objectName(final String objectName)
    {
        this.objectName = objectName;
    }

    public void root(final Node<Element> root)
    {
        if (this.root != null)
        {
            debugger.remove(this.root);
        }
        this.root = root;
    }

    public RTreeSettings settings()
    {
        return settings;
    }

    public Statistics statistics()
    {
        final var statistics = new Statistics();
        root().statistics(1, statistics);
        return statistics;
    }

    @Override
    public String toString()
    {
        return "[RTreeSpatialIndex settings = " + settings() + "]";
    }

    private Node<Element> root()
    {
        return root;
    }
}