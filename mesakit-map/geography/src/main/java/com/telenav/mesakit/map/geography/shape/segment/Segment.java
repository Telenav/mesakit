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

package com.telenav.mesakit.map.geography.shape.segment;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.object.Pair;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.interfaces.collection.NextIterator;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramSegment;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Headed;
import com.telenav.mesakit.map.measurements.geographic.Heading;

import java.awt.geom.Line2D;
import java.io.Serializable;

/**
 * Class representing a line connecting two {@link Location} objects and providing some utilities around their
 * relationship.
 *
 * @author ericg
 * @author matthieun
 */
@SuppressWarnings("DuplicatedCode") @UmlClassDiagram(diagram = DiagramSegment.class)
@UmlExcludeSuperTypes(Serializable.class)
public class Segment implements Bounded, Intersectable, Headed, Serializable, LocationSequence
{
    /**
     * Equator segment
     */
    public static final Segment EQUATOR = new Segment(Location.degrees(0, -180), Location.degrees(0, 180));

    private static final long serialVersionUID = 7197204954808996811L;

    public static class Converter extends BaseStringConverter<Segment>
    {
        private final Location.DegreesConverter locationConverter;

        private final Separators separators;

        public Converter(Listener listener)
        {
            this(listener, new Separators(":", ","));
        }

        public Converter(Listener listener, Separators separators)
        {
            super(listener, Segment.class);
            this.separators = separators;
            locationConverter = new Location.DegreesConverter(listener);
        }

        @Override
        protected String onToString(Segment value)
        {
            return value.start() + ":" + value.end();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Segment onToValue(String value)
        {
            var values = StringList.split(value, separators.current());
            if (values.size() == 2)
            {
                var from = locationConverter.convert(values.get(0).trim());
                var to = locationConverter.convert(values.get(1).trim());
                if (from == null || to == null)
                {
                    problem("Invalid value(s) ${debug}", value);
                    return null;
                }
                return new Segment(from, to);
            }
            else
            {
                // problem("Unable to parse: ${debug}", value);
                return null;
            }
        }
    }

    private Location start;

    private Location end;

    private final long startInDm7;

    private final long endInDm7;

    public Segment(Location start, Location end)
    {
        this.start = start;
        this.end = end;
        startInDm7 = start.asDm7Long();
        endInDm7 = end.asDm7Long();
    }

    public Segment(long startInDm7, long endInDm7)
    {
        assert Precision.DM7.isValidLocation(startInDm7);
        assert Precision.DM7.isValidLocation(endInDm7);
        this.startInDm7 = startInDm7;
        this.endInDm7 = endInDm7;
    }

    /**
     * Returns approximate length of this segment (invalid for long segments)
     */
    public Distance approximateLength()
    {
        return start().distanceTo(end());
    }

    public Polyline asPolyline()
    {
        return Polyline.fromLocations(start(), end());
    }

    /**
     * Returns the location at the given distance from the start along this segment
     */
    public Location at(Distance distance)
    {
        if (length().equals(Distance.ZERO))
        {
            return start();
        }
        var scale = distance.ratio(length());
        return start().moved(heading(), length().times(scale));
    }

    /**
     * Returns a polyline of a backward arrow with direction from end to start
     */
    public Polyline backwardArrow(Angle offset, Distance arrowSize)
    {
        var arrowHeading = heading();
        var line1 = start().moved(arrowHeading.plus(offset), arrowSize);
        var line2 = start().moved(arrowHeading.minus(offset), arrowSize);
        return Polyline.fromLocations(line1, start(), line2);
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromLongs(startInDm7, endInDm7);
    }

    public Location center()
    {
        var latitude = Latitude.degrees((start().latitude().asDegrees() + end().latitude().asDegrees()) / 2.);
        var longitude = Longitude.degrees((start().longitude().asDegrees() + end().longitude().asDegrees()) / 2.);
        return new Location(latitude, longitude);
    }

    public Location end()
    {
        if (end == null)
        {
            end = Location.dm7(endInDm7);
        }
        return end;
    }

    public long endInDm7()
    {
        return endInDm7;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Segment that)
        {
            return start().equals(that.start) && end().equals(that.end());
        }
        return false;
    }

    /**
     * Returns a polyline of a forward arrow with direction from start to end
     */
    public Polyline forwardArrow(Angle offset, Distance arrowSize)
    {
        var arrowHeading = heading().reversed();
        var line1 = end().moved(arrowHeading.plus(offset), arrowSize);
        var line2 = end().moved(arrowHeading.minus(offset), arrowSize);
        return Polyline.fromLocations(line1, end(), line2);
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(start(), end());
    }

    @Override
    public Heading heading()
    {
        return start().headingTo(end());
    }

    public Location intersection(Segment that)
    {
        // Get this segment in degrees
        var this_x1 = start().longitude().asDegrees();
        var this_y1 = start().latitude().asDegrees();
        var this_x2 = end().longitude().asDegrees();
        var this_y2 = end().latitude().asDegrees();

        // Get that segment in degrees
        var that_x1 = that.start().longitude().asDegrees();
        var that_y1 = that.start().latitude().asDegrees();
        var that_x2 = that.end().longitude().asDegrees();
        var that_y2 = that.end().latitude().asDegrees();

        // Compute delta x for both lines
        var this_dx = this_x1 - this_x2;
        var this_dy = this_y1 - this_y2;
        var that_dx = that_x1 - that_x2;
        var that_dy = that_y1 - that_y2;

        // Denominator of determinant
        var denominator = this_dx * that_dy - this_dy * that_dx;

        // If the denominator is zero
        if (denominator == 0)
        {
            // there can be no intersection because the lines are parallel
            return null;
        }

        // If the start of this line is the same as the beginning or start of that line
        if ((this_x1 == that_x1 && this_y1 == that_y1) || (this_x1 == that_x2 && this_y1 == that_y2))
        {
            // then return the start as the intersection point
            return start();
        }

        // If the end of this line is the same as the beginning or start of that line
        if ((this_x2 == that_x1 && this_y2 == that_y1) || (this_x2 == that_x2 && this_y2 == that_y2))
        {
            // then return the end as the intersection point
            return end();
        }

        // Compute intersection
        var xi = (that_dx * (this_x1 * this_y2 - this_y1 * this_x2) - this_dx * (that_x1 * that_y2 - that_y1 * that_x2))
            / denominator;
        var yi = (that_dy * (this_x1 * this_y2 - this_y1 * this_x2) - this_dy * (that_x1 * that_y2 - that_y1 * that_x2))
            / denominator;

        // If this line is vertical
        if (this_x1 == this_x2)
        {
            // round to 7 digits (yes, we know this looks awful, but it works)
            yi = Math.round(yi * 1_000_000_0.0) / 1_000_000_0.0;

            // and the y intersection is out of range on either segment
            if (yi < Math.min(this_y1, this_y2) || yi > Math.max(this_y1, this_y2) || yi < Math.min(that_y1, that_y2)
                || yi > Math.max(that_y1, that_y2))
            {
                // then there is no intersection
                return null;
            }
        }
        else
        {
            // This line is not vertical so there will be a reasonable x intersection

            // round to 7 digits (yes, we know this looks awful, but it works)
            xi = Math.round(xi * 1_000_000_0.0) / 1_000_000_0.0;

            // If the x intersection is out of range on either segment
            if (xi < Math.min(this_x1, this_x2) || xi > Math.max(this_x1, this_x2) || xi < Math.min(that_x1, that_x2)
                || xi > Math.max(that_x1, that_x2))
            {
                // then there is no intersection
                return null;
            }
        }

        // If either coordinate is invalid
        if (!Latitude.isValid(yi) || !Longitude.isValid(xi))
        {
            // there is no intersection
            return null;
        }

        // Return the intersection
        return new Location(Latitude.degrees(yi), Longitude.degrees(xi));
    }

    /**
     * Determines intersection using an adaptation of Liang-Barsky clipping.
     *
     * @see "http://www.skytopia.com/project/articles/compsci/clipping.html"
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public boolean intersects(Rectangle rectangle)
    {
        var bottom = rectangle.bottom().asDegrees();
        var left = rectangle.left().asDegrees();
        var top = rectangle.top().asDegrees();
        var right = rectangle.right().asDegrees();

        var x0 = start().longitude().asDegrees();
        var y0 = start().latitude().asDegrees();
        var x1 = end().longitude().asDegrees();
        var y1 = end().latitude().asDegrees();

        var t0 = 0.0;
        var t1 = 1.0;

        var dx = x1 - x0;
        var dy = y1 - y0;

        // For each side of the rectangle
        for (var side = 0; side < 4; side++)
        {
            double p;
            double q;
            switch (side)
            {
                case 0 ->
                {
                    p = -dx;
                    q = -(left - x0);
                }
                case 1 ->
                {
                    p = dx;
                    q = (right - x0);
                }
                case 2 ->
                {
                    p = -dy;
                    q = -(bottom - y0);
                }
                case 3 ->
                {
                    p = dy;
                    q = (top - y0);
                }
                default ->
                {
                    // Not possible, but compiler wants p and q to be assigned
                    p = 0;
                    q = 0;
                }
            }
            var r = q / p;
            if (p == 0 && q < 0)
            {
                return false;
            }
            if (p < 0)
            {
                if (r > t1)
                {
                    return false;
                }
                else if (r > t0)
                {
                    t0 = r;
                }
            }
            else if (p > 0)
            {
                if (r < t0)
                {
                    return false;
                }
                else if (r < t1)
                {
                    t1 = r;
                }
            }
        }
        return true;
    }

    /**
     * NOTE: This method is only useful on short segments as it does not take into account the curvature of the earth.
     *
     * @return True if this segment intersects that segment.
     */
    public boolean intersects(Segment that)
    {
        var x1 = start().longitude().asNanodegrees();
        var x2 = end().longitude().asNanodegrees();
        var x3 = that.start().longitude().asNanodegrees();
        var x4 = that.end().longitude().asNanodegrees();

        var thisRight = Math.max(x1, x2);
        var thatLeft = Math.min(x3, x4);

        if (thisRight < thatLeft)
        {
            return false;
        }

        var thisLeft = Math.min(x1, x2);
        var thatRight = Math.max(x3, x4);

        if (thisLeft > thatRight)
        {
            return false;
        }

        var y1 = start().latitude().asNanodegrees();
        var y2 = end().latitude().asNanodegrees();
        var y3 = that.start().latitude().asNanodegrees();
        var y4 = that.end().latitude().asNanodegrees();

        var thisTop = Math.max(y1, y2);
        var thatBottom = Math.min(y3, y4);

        if (thisTop < thatBottom)
        {
            return false;
        }

        var thisBottom = Math.min(y1, y2);
        var thatTop = Math.max(y3, y4);

        if (thisBottom > thatTop)
        {
            return false;
        }

        // If any combination of end-points match up,
        if ((x1 == x3 && y1 == y3) || (x1 == x4 && y1 == y4) || (x2 == x3 && y2 == y3) || (x2 == x4 && y2 == y4))
        {
            // that's an intersection
            return true;
        }

        return Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public boolean isConnectedTo(Segment that)
    {
        return start().equals(that.start()) || start().equals(that.end()) || end().equals(that.start())
            || end().equals(that.end());
    }

    public boolean isHorizontal()
    {
        return start.latitudeInDm7() == end.latitudeInDm7();
    }

    public boolean isParallel(Segment that, Angle threshold)
    {
        return heading().difference(that.heading(), Chirality.SMALLEST).isLessThan(threshold);
    }

    public boolean isPoint()
    {
        return start().equals(end());
    }

    public boolean isVertical()
    {
        return start.longitudeInDm7() == end.longitudeInDm7();
    }

    public boolean leadsTo(Segment that)
    {
        return end().equals(that.start());
    }

    /**
     * Returns length of this segment.
     */
    public Distance length()
    {
        return start().distanceTo(end());
    }

    @Override
    public Iterable<Location> locationSequence()
    {
        return new Pair<>(start(), end());
    }

    public Location midpoint()
    {
        return start().moved(heading(), length().times(0.5));
    }

    /**
     * @param left if true, otherwise right
     * @param offset the distance from the new created segment to this segment
     * @return a new created segment that is parallel to this segment with distance of offset and on the left or right
     */
    public Segment parallel(boolean left, Distance offset)
    {
        var heading = heading();
        heading = left ? heading.plus(Angle._270_DEGREES) : heading.plus(Angle._90_DEGREES);

        return new Segment(start().moved(heading, offset), end().moved(heading, offset));
    }

    /**
     * Returns a segment perpendicular to this segment at the given location of the given length on both sides of this
     * segment.
     */
    public Segment perpendicular(Location at, Distance length)
    {
        var start = at.moved(heading().plus(Angle._90_DEGREES), length);
        var end = at.moved(heading().plus(Angle._270_DEGREES), length);
        return new Segment(start, end);
    }

    public Segment reversed()
    {
        return new Segment(end(), start());
    }

    public Iterable<Segment> sections(Distance length)
    {
        return Iterables.iterable(() -> new NextIterator<>()
        {
            private Distance offset = Distance.ZERO;

            @Override
            public Segment next()
            {
                var startOffset = offset;
                var endOffset = offset.add(length);
                if (endOffset.isLessThan(length()))
                {
                    var heading = heading();
                    offset = offset.add(length);
                    return new Segment(start().moved(heading, startOffset), start().moved(heading, endOffset));
                }
                return null;
            }
        });
    }

    public Location start()
    {
        if (start == null)
        {
            start = Location.dm7(startInDm7);
        }
        return start;
    }

    public long startInDm7()
    {
        return startInDm7;
    }

    /**
     * Returns the surrounding bounding box along the segment
     */
    public Polygon surroundingBox(Distance range)
    {
        var heading = heading();
        var reversedHeading = heading().reversed();

        var location1 = start()
            .moved(reversedHeading, range)
            .moved(reversedHeading.plus(Angle._90_DEGREES), range);

        var location2 = start()
            .moved(reversedHeading, range)
            .moved(reversedHeading.minus(Angle._90_DEGREES), range);

        var location3 = end()
            .moved(heading, range)
            .moved(heading.plus(Angle._90_DEGREES), range);

        var location4 = end()
            .moved(heading, range)
            .moved(heading.minus(Angle._90_DEGREES), range);

        return Polygon.fromLocationSequence(location1, location2, location3, location4);
    }

    @Override
    public String toString()
    {
        return start() + ":" + end();
    }

    public Angle turnAngleTo(Segment that, Chirality chirality)
    {
        return heading().difference(that.heading(), chirality);
    }

    public Segment withEndExtended(Distance distance)
    {
        return new Segment(start(), end().moved(heading(), distance));
    }

    public Segment withStartExtended(Distance distance)
    {
        return new Segment(start().moved(heading().reversed(), distance), end());
    }
}
