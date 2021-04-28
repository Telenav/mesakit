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

package com.telenav.mesakit.map.geography.indexing.quadtree;

import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.language.collections.list.LinkedObjectList;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramSpatialIndex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.illegalState;

/**
 * Stores {@link Located} objects in a tree of quadrants so they can be quickly located spatially.
 *
 * @param <Element> The {@link Located} type
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramSpatialIndex.class)
public class QuadTreeSpatialIndex<Element extends Located>
{
    private static final int QUADRANTS = 4;

    public final class Quadrant
    {
        private final class BoundedLeafIterator extends BaseIterator<Element>
        {
            private final Rectangle bounds;

            private final Iterator<Element> iterator = objects.iterator();

            private BoundedLeafIterator(final Rectangle bounds, final Matcher<Element> matcher)
            {
                this.bounds = bounds;
                filter(matcher);
            }

            @Override
            protected final Element onNext()
            {
                while (iterator.hasNext())
                {
                    final var next = iterator.next();
                    if (bounds.contains(next.location()))
                    {
                        return next;
                    }
                }
                return null;
            }
        }

        private final class QuadrantIterator extends BaseIterator<Element>
        {
            private final Rectangle bounds;

            private int index = -1;

            private Iterator<Element> iterator;

            private QuadrantIterator(final Rectangle bounds, final Matcher<Element> matcher)
            {
                this.bounds = bounds;
                filter(matcher);
            }

            @Override
            protected Element onNext()
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

            private Iterator<Element> nextIterator()
            {
                while (++index < QUADRANTS)
                {
                    final var child = children[index];
                    if (bounds.intersects(child.bounds))
                    {
                        iterator = child.inside(bounds);
                        if (iterator.hasNext())
                        {
                            return iterator;
                        }
                    }
                }
                return null;
            }
        }

        private Rectangle bounds;

        private Quadrant[] children;

        private LinkedObjectList<Element> objects = new LinkedObjectList<>();

        Quadrant(final Rectangle bounds)
        {
            this.bounds = bounds;
        }

        protected Quadrant()
        {
        }

        @Override
        public String toString()
        {
            return "[Quadrant " + bounds.toString() + "]";
        }

        boolean add(final Element object)
        {
            // If the object is inside this quadrant
            if (bounds.contains(object.location()))
            {
                synchronized (this)
                {
                    // and the quadrant is a leaf
                    if (isLeaf())
                    {
                        // then add the object to the lead
                        objects.add(0, object);

                        // If this quadrant is full,
                        if (isFull())
                        {
                            // split the quadrant into 4 children
                            split();
                        }
                    }
                    else
                    {
                        // Add the object to the first quadrant that takes it
                        for (final var child : children)
                        {
                            if (child.add(object))
                            {
                                break;
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        void dump(final PrintStream out)
        {
            if (isLeaf())
            {
                out.println(this + " contains " + new StringList(objects.iterator()));
            }
            else
            {
                for (final var child : children)
                {
                    child.dump(out);
                }
            }
        }

        Iterator<Element> inside(final Rectangle bounds)
        {
            return inside(bounds, null);
        }

        Iterator<Element> inside(final Rectangle bounds, final Matcher<Element> matcher)
        {
            if (isLeaf())
            {
                if (bounds == null)
                {
                    return objects.matching(matcher);
                }
                else
                {
                    return new BoundedLeafIterator(bounds, matcher);
                }
            }
            else
            {
                return new QuadrantIterator(bounds, matcher);
            }
        }

        boolean remove(final Element object)
        {
            if (isLeaf())
            {
                // NOTE: We don't "un-split" if our four sibling quadrants are also empty.
                // We could do this to conserve memory for data that varies in location a lot.
                return objects.remove(object);
            }
            else
            {
                // Go through each child quadrant
                for (final var child : children)
                {
                    // If the object we're looking for intersects with the quadrant bounds
                    if (child.bounds.contains(object))
                    {
                        // and the object can be removed from the child quadrant
                        if (child.remove(object))
                        {
                            // then we removed it
                            return true;
                        }
                    }
                }
                return false;
            }
        }

        Collection<Element> removeAll(final Rectangle bounds, final Matcher<Element> matcher)
        {
            if (isLeaf())
            {
                // NOTE: We don't "un-split" if our four sibling quadrants are also empty.
                // We could do this to conserve memory for data that varies in location a lot.
                return new HashSet<>(objects.remove(matcher));
            }
            else
            {
                final Collection<Element> removed = new HashSet<>();
                for (final var child : children)
                {
                    if (child.bounds.contains(bounds))
                    {
                        removed.addAll(child.removeAll(bounds, matcher));
                    }
                }
                return removed;
            }
        }

        boolean replace(final Element object, final Element replacement)
        {
            if (bounds.contains(object))
            {
                if (isLeaf())
                {
                    return objects.replace(object, replacement);
                }
                else
                {
                    // Go through each child quadrant
                    for (final var child : children)
                    {
                        // If the object we're looking for intersects with the quadrant bounds
                        if (child.bounds.contains(object))
                        {
                            // and the object can be replaced within the child quadrant
                            if (child.replace(object, replacement))
                            {
                                // we've replaced it
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
            return false;
        }

        private boolean canBeSplit()
        {
            return bounds.width().times(0.5).isGreaterThan(minimumWidth)
                    && bounds.height().times(0.5).isGreaterThan(minimumHeight);
        }

        private boolean isFull()
        {
            return objects.size() > maximumObjectsPerQuadrant;
        }

        private boolean isLeaf()
        {
            return children == null;
        }

        private void split()
        {
            if (canBeSplit())
            {
                // Create four child quadrants
                @SuppressWarnings("unchecked") final Quadrant[] children = new QuadTreeSpatialIndex.Quadrant[QUADRANTS];
                children[0] = new Quadrant(bounds.northWestQuadrant());
                children[1] = new Quadrant(bounds.northEastQuadrant());
                children[2] = new Quadrant(bounds.southWestQuadrant());
                children[3] = new Quadrant(bounds.southEastQuadrant());

                // Insert each object from this quadrant into each child quadrant (the object
                // addition will be ignored by quadrants that don't contain the object's location)
                for (final var object : objects)
                {
                    // Go through quadrants
                    for (final var quadrant : children)
                    {
                        // until one adds it
                        if (quadrant.add(object))
                        {
                            break;
                        }
                    }
                }

                // There are no objects in this quadrant now, only child quadrants
                objects = new LinkedObjectList<>();
                this.children = children;
            }
        }
    }

    private final int maximumObjectsPerQuadrant;

    private Quadrant root;

    private Rectangle bounds;

    private boolean elementRemoved;

    private final Latitude minimumHeight;

    private final Longitude minimumWidth;

    private final AtomicInteger size = new AtomicInteger();

    public QuadTreeSpatialIndex()
    {
        this(100, Distance.miles(0.25));
    }

    public QuadTreeSpatialIndex(final int maximumObjectsPerQuadrant, final Distance minimumQuadrantSize)
    {
        this.maximumObjectsPerQuadrant = maximumObjectsPerQuadrant;
        minimumHeight = Latitude.degrees(minimumQuadrantSize.asDegrees());
        minimumWidth = Longitude.degrees(minimumQuadrantSize.asDegrees());
        clear();
    }

    public void add(final Element object)
    {
        if (bounds == null)
        {
            bounds = object.location().bounds();
        }
        else
        {
            bounds = bounds.expandedToInclude(object.location());
        }
        if (root.add(object))
        {
            size.incrementAndGet();
        }
    }

    public void addAll(final Iterable<Element> objects)
    {
        for (final var object : objects)
        {
            add(object);
        }
    }

    public Rectangle bounds()
    {
        // TODO implement dynamic bounds calculation in Quadrant so bounds shrink after element removal
        if (elementRemoved)
        {
            illegalState("Bounds unavailable after element removal");
        }
        return bounds;
    }

    public void clear()
    {
        root = new Quadrant(Rectangle.MAXIMUM);
        size.set(0);
    }

    public Count count()
    {
        return Count.count(size());
    }

    public void dump(final PrintStream out)
    {
        root.dump(out);
    }

    public Iterator<Element> inside(final Rectangle bounds)
    {
        return inside(bounds, null);
    }

    public Iterator<Element> inside(final Rectangle bounds, final Matcher<Element> matcher)
    {
        return root.inside(bounds, matcher);
    }

    public void remove(final Element object)
    {
        if (root.remove(object))
        {
            size.decrementAndGet();
            elementRemoved = true;
        }
    }

    public void removeAll(final Rectangle bounds, final Matcher<Element> matcher)
    {
        final var removed = root.removeAll(bounds, matcher);
        size.addAndGet(-1 * removed.size());
        elementRemoved = true;
    }

    public boolean replace(final Element oldValue, final Element newValue)
    {
        return root.replace(oldValue, newValue);
    }

    public int size()
    {
        return size.get();
    }
}
