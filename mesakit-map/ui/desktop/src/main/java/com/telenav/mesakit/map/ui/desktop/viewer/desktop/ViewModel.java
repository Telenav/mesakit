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

package com.telenav.mesakit.map.ui.desktop.viewer.desktop;

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapDrawable;
import com.telenav.mesakit.map.ui.desktop.viewer.DrawableIdentifier;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ViewModel
{
    private static class Entry
    {
        private final DrawableIdentifier identifier;

        private MapDrawable viewable;

        private Shape shape;

        public Entry(DrawableIdentifier identifier, MapDrawable viewable)
        {
            this.identifier = identifier;
            this.viewable = viewable;
        }

        @Override
        public boolean equals(Object object)
        {
            if (object instanceof Entry)
            {
                var that = (Entry) object;
                return identifier.equals(that.identifier);
            }
            return false;
        }

        @Override
        public int hashCode()
        {
            return identifier.hashCode();
        }

        public void shape(Shape shape)
        {
            this.shape = shape;
        }

        @Override
        public String toString()
        {
            return identifier.toString();
        }

        public void viewable(MapDrawable viewable)
        {
            this.viewable = viewable;
        }
    }

    /**
     * Currently selected viewable
     */
    private Entry selected;

    /**
     * Ordered list of viewables and their drawn outlines
     */
    private final LinkedList<Entry> entries = new LinkedList<>();

    /**
     * Map from viewable identifier to entry
     */
    private final Map<DrawableIdentifier, Entry> entryForIdentifier = new HashMap<>();

    /**
     * Next unique identifier
     */
    private final AtomicInteger nextIdentifier = new AtomicInteger(1);

    /**
     * The most recent set of entries involved in a select(Point) operation
     */
    private Set<Entry> lastEntriesForPoint;

    /**
     * Adds the given viewable
     */
    public void add(MapDrawable viewable)
    {
        update(nextIdentifier(), viewable);
    }

    public synchronized Rectangle bounds()
    {
        var builder = new BoundingBoxBuilder();
        for (var entry : entries)
        {
            builder.add(entry.viewable.bounds());
        }
        return builder.build();
    }

    /**
     * Clears the map
     */
    public synchronized void clear()
    {
        entries.clear();
    }

    /**
     * Draws the {@link MapDrawable} objects in this map on the given drawing surface, keeping outlines for hit-testing
     * in the entry structure.
     */
    public void draw(DrawingSurface surface)
    {
        // Get a copy of the entries list
        List<Entry> copy;
        synchronized (this)
        {
            copy = new ArrayList<>(entries);
        }

        // For each entry
        for (var entry : copy)
        {
            // if the entry isn't the selected one
            if (entry != selected)
            {
                // draw it
                draw(surface, entry);
            }
        }

        // Draw any selected viewable on top
        if (selected != null)
        {
            draw(surface, selected);
        }
    }

    public void map(Function<MapDrawable, MapDrawable> function)
    {
        entries.forEach(entry -> entry.viewable = function.apply(entry.viewable));
    }

    /**
     * Moves the given viewable to the top of the view
     */
    public synchronized void pullToFront(DrawableIdentifier identifier)
    {
        var entry = entry(identifier);
        entries.remove(new Entry(identifier, null));
        entries.add(entry);
    }

    /**
     * Moves the given viewable to the back of the view
     */
    public synchronized void pushToBack(DrawableIdentifier identifier)
    {
        var entry = entry(identifier);
        entries.remove(new Entry(identifier, null));
        entries.add(0, entry);
    }

    /**
     * Remove the identified viewable
     */
    public synchronized void remove(DrawableIdentifier identifier)
    {
        entries.remove(new Entry(identifier, null));
        entryForIdentifier.remove(identifier);
    }

    /**
     * Changes the current selection and drawing order based on the mouse click location
     */
    public void select(Point point)
    {
        // Get all entries that are selected by the given point
        var entries = entriesForPoint(point);

        // If there is at least one entry to select from
        if (!entries.isEmpty())
        {
            // If there is no selection yet,
            if (selected == null)
            {
                // pick the top of the list
                selected = entries.get(0);
            }
            else
            {
                // If there is more than one entry and the click is on the same set of entries as
                // the last click was,
                if (entries.size() > 1 && new HashSet<>(entries).equals(lastEntriesForPoint))
                {
                    // then we rotate the selected entry to the back of the list, allowing users to
                    // "drill-down" through overlapping entries by clicking multiple times
                    moveToBack(selected);
                }

                // We must re-query the entries here since moveToBack may have changed the order
                selected = entriesForPoint(point).get(0);
            }
        }

        // Save set of entries for next select call
        lastEntriesForPoint = new HashSet<>(entries);
    }

    /**
     * Update the viewable with the given unique identifier
     */
    public synchronized void update(DrawableIdentifier identifier, MapDrawable viewable)
    {
        var entry = entry(identifier);
        if (entry != null)
        {
            entry.viewable(viewable);
        }
        else
        {
            entry = new Entry(identifier, viewable);
            entries.add(entry);
            entryForIdentifier.put(identifier, entry);
        }
    }

    private void draw(DrawingSurface surface, Entry entry)
    {
        entry.viewable.draw(surface);
    }

    /**
     * Returns the list of entries whose outlines contain the given point, in front-to-back order
     */
    private List<Entry> entriesForPoint(Point2D point)
    {
        List<Entry> entries = new ArrayList<>();
        synchronized (this)
        {
            for (var entry : this.entries)
            {
                if (entry.shape != null && entry.shape.contains(point))
                {
                    entries.add(entry);
                }
            }
        }
        Collections.reverse(entries);
        return entries;
    }

    private synchronized Entry entry(DrawableIdentifier identifier)
    {
        return entryForIdentifier.get(identifier);
    }

    private void moveToBack(Entry entry)
    {
        synchronized (this)
        {
            entries.remove(entry);
            entries.addFirst(entry);
        }
    }

    @SuppressWarnings("unused")
    private synchronized void moveToTop(Entry entry)
    {
        entries.remove(entry);
        entries.addLast(entry);
    }

    private DrawableIdentifier nextIdentifier()
    {
        return new DrawableIdentifier("auto::" + nextIdentifier.getAndIncrement());
    }
}
