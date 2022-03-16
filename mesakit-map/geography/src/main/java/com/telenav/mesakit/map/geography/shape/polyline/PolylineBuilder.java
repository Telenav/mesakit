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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline.Intersection;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
@UmlClassDiagram(diagram = DiagramPolyline.class)
public class PolylineBuilder
{
    private final List<Location> locations = new ArrayList<>();

    private Location last;

    private boolean hasDuplicates;

    @SuppressWarnings("UnusedReturnValue")
    public PolylineBuilder add(double latitude, double longitude)
    {
        return add(Location.degrees(latitude, longitude));
    }

    public PolylineBuilder add(Location location)
    {
        if (location != null)
        {
            if (location.equals(last))
            {
                hasDuplicates = true;
            }
            locations.add(location);
            last = location;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PolylineBuilder addAll(Iterable<Location> locations)
    {
        for (var location : locations)
        {
            add(location);
        }
        return this;
    }

    public PolylineBuilder addAll(Iterator<Location> locations)
    {
        while (locations.hasNext())
        {
            add(locations.next());
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PolylineBuilder addAllButFirst(Iterable<Location> locations)
    {
        var first = true;
        for (var location : locations)
        {
            if (!first)
            {
                add(location);
            }
            first = false;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PolylineBuilder addAllButLast(Iterable<Location> locations)
    {
        Location previous = null;
        for (var location : locations)
        {
            if (previous != null)
            {
                add(previous);
            }
            previous = location;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PolylineBuilder addAllUnique(Iterable<Location> locations)
    {
        for (var location : locations)
        {
            addUnique(location);
        }
        return this;
    }

    /**
     * Adds the point of intersection with the given segment (if any) to this polyline.
     *
     * @param segment The segment that may or may not intersect with this polyline
     * @param maximumSnapDistance The maximum distance over which an intersection will be snapped to a segment endpoint
     * @return The intersection or null. If non-null, the returned {@link Intersection} contains the intersection point
     * and a modified boolean. If the modified boolean is true, a location was added to this polyline. If the modified
     * boolean is false, an existing location on this polyline was reused and no new location was added.
     */
    public Intersection addIntersectionWith(Segment segment, Distance maximumSnapDistance)
    {
        // Go through line segments
        var index = 0;
        for (var current : segments())
        {
            // If there's an intersection between the given segment and the current one
            var intersection = current.intersection(segment);
            if (intersection != null)
            {
                // If the intersection is close enough to the end
                if (current.start().distanceTo(intersection).isLessThan(maximumSnapDistance))
                {
                    // return intersection on the polyline without adding anything to the polyline
                    return new Intersection(current.start(), false);
                }

                // If the intersection is close enough to the end
                if (current.end().distanceTo(intersection).isLessThan(maximumSnapDistance))
                {
                    // return intersection on the polyline without adding anything to the polyline
                    return new Intersection(current.end(), false);
                }

                // otherwise insert the intersection point into the line and return it
                locations.add(index + 1, intersection);
                return new Intersection(intersection, true);
            }
            index++;
        }
        return null;
    }

    /**
     * Adds all locations except the first one
     */
    public PolylineBuilder addTail(Iterable<Location> locations)
    {
        var first = true;
        for (var location : locations)
        {
            if (!first)
            {
                add(location);
            }
            first = false;
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addUnique(Location location)
    {
        if (location != null && !location.equals(last))
        {
            add(location);
            return true;
        }
        return false;
    }

    @UmlRelation(label = "builds")
    public Polyline build()
    {
        if (!isEmpty())
        {
            return Polyline.fromLocations(locations);
        }
        return null;
    }

    public Location end()
    {
        return locations.get(size() - 1);
    }

    public boolean hasDuplicates()
    {
        return hasDuplicates;
    }

    public boolean isEmpty()
    {
        return locations.isEmpty();
    }

    public boolean isValid()
    {
        return size() >= 2;
    }

    public boolean isZeroLength()
    {
        return size() == 2 && locations.get(0).equals(locations.get(1));
    }

    public void prepend(Location location)
    {
        locations.add(0, location);
    }

    public final List<Segment> segments()
    {
        return new AbstractList<>()
        {
            @Override
            public Segment get(int index)
            {
                return segment(index);
            }

            @Override
            public int size()
            {
                return PolylineBuilder.this.size() - 1;
            }
        };
    }

    public void set(int index, Location location)
    {
        locations.set(index, location);
    }

    public int size()
    {
        return locations.size();
    }

    public Location start()
    {
        return locations.get(0);
    }

    protected List<Location> locations()
    {
        return locations;
    }

    private Location get(int index)
    {
        return locations.get(index);
    }

    private Segment segment(int index)
    {
        if (index < size())
        {
            return new Segment(get(index), get(index + 1));
        }
        throw new IndexOutOfBoundsException("No segment at index " + index);
    }
}
