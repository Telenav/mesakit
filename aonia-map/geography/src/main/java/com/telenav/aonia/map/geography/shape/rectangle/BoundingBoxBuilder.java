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

package com.telenav.aonia.map.geography.shape.rectangle;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.project.lexakai.diagrams.DiagramRectangle;
import com.telenav.aonia.map.geography.shape.segment.Segment;

import static com.telenav.aonia.map.geography.Precision.DM7;

@UmlClassDiagram(diagram = DiagramRectangle.class)
public final class BoundingBoxBuilder
{
    private int minimumLatitudeInDm7 = Integer.MAX_VALUE;

    private int maximumLatitudeInDm7 = Integer.MIN_VALUE;

    private int minimumLongitudeInDm7 = Integer.MAX_VALUE;

    private int maximumLongitudeInDm7 = Integer.MIN_VALUE;

    public void add(final long locationInDm7)
    {
        // We don't include the origin (0,0) because there's nothing there
        if (locationInDm7 != 0)
        {
            final var latitudeInDm7 = Location.latitude(locationInDm7);
            final var longitudeInDm7 = Location.longitude(locationInDm7);
            add(latitudeInDm7, longitudeInDm7);
        }
    }

    public void add(final int latitudeInDm7, final int longitudeDm7)
    {
        if (latitudeInDm7 < minimumLatitudeInDm7)
        {
            minimumLatitudeInDm7 = latitudeInDm7;
        }
        if (latitudeInDm7 > maximumLatitudeInDm7)
        {
            maximumLatitudeInDm7 = latitudeInDm7;
        }
        if (longitudeDm7 < minimumLongitudeInDm7)
        {
            minimumLongitudeInDm7 = longitudeDm7;
        }
        if (longitudeDm7 > maximumLongitudeInDm7)
        {
            maximumLongitudeInDm7 = longitudeDm7;
        }
    }

    public void add(final Iterable<Location> locations)
    {
        for (final var location : locations)
        {
            add(location);
        }
    }

    public void add(final double latitude, final double longitude)
    {
        if (latitude != 0 || longitude != 0)
        {
            add(DM7.toDecimal(latitude), DM7.toDecimal(longitude));
        }
    }

    public void add(final Location location)
    {
        if (location != null)
        {
            add(location.latitudeInDm7(), location.longitudeInDm7());
        }
    }

    public void add(final Rectangle rectangle)
    {
        add(rectangle.bottomInDm7(), rectangle.leftInDm7());
        add(rectangle.topInDm7(), rectangle.rightInDm7());
    }

    public void add(final Segment segment)
    {
        final var start = segment.startInDm7();
        final var end = segment.endInDm7();
        add(Location.latitude(start), Location.longitude(start));
        add(Location.latitude(end), Location.longitude(end));
    }

    @UmlRelation(label = "builds")
    public Rectangle build()
    {
        if (minimumLatitudeInDm7 == Integer.MAX_VALUE || maximumLatitudeInDm7 == Integer.MIN_VALUE
                || minimumLongitudeInDm7 == Integer.MAX_VALUE || maximumLongitudeInDm7 == Integer.MIN_VALUE)
        {
            return null;
        }
        return Rectangle.fromInts(minimumLatitudeInDm7, minimumLongitudeInDm7, maximumLatitudeInDm7, maximumLongitudeInDm7);
    }

    public boolean isValid()
    {
        return minimumLatitudeInDm7 != Integer.MAX_VALUE && maximumLatitudeInDm7 != Integer.MIN_VALUE
                && minimumLongitudeInDm7 != Integer.MAX_VALUE && maximumLongitudeInDm7 != Integer.MIN_VALUE;
    }
}
