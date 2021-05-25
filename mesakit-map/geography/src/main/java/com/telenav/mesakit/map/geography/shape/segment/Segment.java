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

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.objects.Pair;
import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramSegment;
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
@UmlClassDiagram(diagram = DiagramSegment.class)
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

        public Converter(final Listener listener)
        {
            this(listener, new Separators(":", ","));
        }

        public Converter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
            locationConverter = new Location.DegreesConverter(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Segment onConvertToObject(final String value)
        {
            final var values = StringList.split(value, separators.current());
            if (values.size() == 2)
            {
                final var from = locationConverter.convert(values.get(0).trim());
                final var to = locationConverter.convert(values.get(1).trim());
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

        @Override
        protected String onConvertToString(final Segment value)
        {
            return value.start() + ":" + value.end();
        }
    }

    private Location start;

    private Location end;

    private final long startInDm7;

    private final long endInDm7;

    public Segment(final Location start, final Location end)
    {
        this.start = start;
        this.end = end;
        startInDm7 = start.asDm7Long();
        endInDm7 = end.asDm7Long();
    }

    public Segment(final long startInDm7, final long endInDm7)
    {
        assert Precision.DM7.isValidLocation(startInDm7);
        assert Precision.DM7.isValidLocation(endInDm7);
        this.startInDm7 = startInDm7;
        this.endInDm7 = endInDm7;
    }

    /**
     * @return Approximate length of this segment (invalid for long segments)
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
     * @return The location at the given distance from the start along this segment
     */
    public Location at(final Distance distance)
    {
        if (length().equals(Distance.ZERO))
        {
            return start();
        }
        final var scale = distance.ratio(length());
        return start().moved(heading(), length().times(scale));
    }

    /**
     * @return A polyline of a backward arrow with direction from end to start
     */
    public Polyline backwardArrow(final Angle offset, final Distance arrowSize)
    {
        final var arrowHeading = heading();
        final var line1 = start().moved(arrowHeading.plus(offset), arrowSize);
        final var line2 = start().moved(arrowHeading.minus(offset), arrowSize);
        return Polyline.fromLocations(line1, start(), line2);
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromLongs(startInDm7, endInDm7);
    }

    public Location center()
    {
        final var latitude = Latitude.degrees((start().latitude().asDegrees() + end().latitude().asDegrees()) / 2.);
        final var longitude = Longitude.degrees((start().longitude().asDegrees() + end().longitude().asDegrees()) / 2.);
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
    public boolean equals(final Object object)
    {
        if (object instanceof Segment)
        {
            final var that = (Segment) object;
            return start().equals(that.start) && end().equals(that.end());
        }
        return false;
    }

    /**
     * @return A polyline of a forward arrow with direction from start to end
     */
    public Polyline forwardArrow(final Angle offset, final Distance arrowSize)
    {
        final var arrowHeading = heading().reversed();
        final var line1 = end().moved(arrowHeading.plus(offset), arrowSize);
        final var line2 = end().moved(arrowHeading.minus(offset), arrowSize);
        return Polyline.fromLocations(line1, end(), line2);
    }

    @Override
    public int hashCode()
    {
        return Hash.many(start(), end());
    }

    @Override
    public Heading heading()
    {
        return start().headingTo(end());
    }

    public Location intersection(final Segment that)
    {
        // Get this segment in degrees
        final var this_x1 = start().longitude().asDegrees();
        final var this_y1 = start().latitude().asDegrees();
        final var this_x2 = end().longitude().asDegrees();
        final var this_y2 = end().latitude().asDegrees();

        // Get that segment in degrees
        final var that_x1 = that.start().longitude().asDegrees();
        final var that_y1 = that.start().latitude().asDegrees();
        final var that_x2 = that.end().longitude().asDegrees();
        final var that_y2 = that.end().latitude().asDegrees();

        // Compute delta x for both lines
        final var this_dx = this_x1 - this_x2;
        final var this_dy = this_y1 - this_y2;
        final var that_dx = that_x1 - that_x2;
        final var that_dy = that_y1 - that_y2;

        // Denominator of determinant
        final var denominator = this_dx * that_dy - this_dy * that_dx;

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
    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        final var bottom = rectangle.bottom().asDegrees();
        final var left = rectangle.left().asDegrees();
        final var top = rectangle.top().asDegrees();
        final var right = rectangle.right().asDegrees();

        final var x0 = start().longitude().asDegrees();
        final var y0 = start().latitude().asDegrees();
        final var x1 = end().longitude().asDegrees();
        final var y1 = end().latitude().asDegrees();

        var t0 = 0.0;
        var t1 = 1.0;

        final var dx = x1 - x0;
        final var dy = y1 - y0;

        // For each side of the rectangle
        for (var side = 0; side < 4; side++)
        {
            final double p;
            final double q;
            switch (side)
            {
                case 0:
                    p = -dx;
                    q = -(left - x0);
                    break;
                case 1:
                    p = dx;
                    q = (right - x0);
                    break;
                case 2:
                    p = -dy;
                    q = -(bottom - y0);
                    break;
                case 3:
                    p = dy;
                    q = (top - y0);
                    break;
                default:
                    // Not possible, but compiler wants p and q to be assigned
                    p = 0;
                    q = 0;
            }
            final var r = q / p;
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
    public boolean intersects(final Segment that)
    {
        final var x1 = start().longitude().asNanodegrees();
        final var x2 = end().longitude().asNanodegrees();
        final var x3 = that.start().longitude().asNanodegrees();
        final var x4 = that.end().longitude().asNanodegrees();

        final var thisRight = Math.max(x1, x2);
        final var thatLeft = Math.min(x3, x4);

        if (thisRight < thatLeft)
        {
            return false;
        }

        final var thisLeft = Math.min(x1, x2);
        final var thatRight = Math.max(x3, x4);

        if (thisLeft > thatRight)
        {
            return false;
        }

        final var y1 = start().latitude().asNanodegrees();
        final var y2 = end().latitude().asNanodegrees();
        final var y3 = that.start().latitude().asNanodegrees();
        final var y4 = that.end().latitude().asNanodegrees();

        final var thisTop = Math.max(y1, y2);
        final var thatBottom = Math.min(y3, y4);

        if (thisTop < thatBottom)
        {
            return false;
        }

        final var thisBottom = Math.min(y1, y2);
        final var thatTop = Math.max(y3, y4);

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

    public boolean isConnectedTo(final Segment that)
    {
        return start().equals(that.start()) || start().equals(that.end()) || end().equals(that.start())
                || end().equals(that.end());
    }

    public boolean isHorizontal()
    {
        return start.latitudeInDm7() == end.latitudeInDm7();
    }

    public boolean isParallel(final Segment that, final Angle threshold)
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

    public boolean leadsTo(final Segment that)
    {
        return end().equals(that.start());
    }

    /**
     * @return Length of this segment.
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
    public Segment parallel(final boolean left, final Distance offset)
    {
        var heading = heading();
        heading = left ? heading.plus(Angle._270_DEGREES) : heading.plus(Angle._90_DEGREES);

        return new Segment(start().moved(heading, offset), end().moved(heading, offset));
    }

    /**
     * @return A segment perpendicular to this segment at the given location of the given length on both sides of this
     * segment.
     */
    public Segment perpendicular(final Location at, final Distance length)
    {
        final var start = at.moved(heading().plus(Angle._90_DEGREES), length);
        final var end = at.moved(heading().plus(Angle._270_DEGREES), length);
        return new Segment(start, end);
    }

    public Segment reversed()
    {
        return new Segment(end(), start());
    }

    public Iterable<Segment> sections(final Distance length)
    {
        return Iterables.iterable(() -> new Next<>()
        {
            private Distance offset = Distance.ZERO;

            @Override
            public Segment onNext()
            {
                final var startOffset = offset;
                final var endOffset = offset.add(length);
                if (endOffset.isLessThan(length()))
                {
                    final var heading = heading();
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
     * @return The surrounding bounding box along the segment
     */
    public Polygon surroundingBox(final Distance range)
    {
        final var heading = heading();
        final var reversedHeading = heading().reversed();

        final var location1 = start()
                .moved(reversedHeading, range)
                .moved(reversedHeading.plus(Angle._90_DEGREES), range);

        final var location2 = start()
                .moved(reversedHeading, range)
                .moved(reversedHeading.minus(Angle._90_DEGREES), range);

        final var location3 = end()
                .moved(heading, range)
                .moved(heading.plus(Angle._90_DEGREES), range);

        final var location4 = end()
                .moved(heading, range)
                .moved(heading.minus(Angle._90_DEGREES), range);

        return Polygon.fromLocationSequence(location1, location2, location3, location4);
    }

    @Override
    public String toString()
    {
        return start() + ":" + end();
    }

    public Angle turnAngleTo(final Segment that, final Chirality chirality)
    {
        return heading().difference(that.heading(), chirality);
    }

    public Segment withEndExtended(final Distance distance)
    {
        return new Segment(start(), end().moved(heading(), distance));
    }

    public Segment withStartExtended(final Distance distance)
    {
        return new Segment(start().moved(heading().reversed(), distance), end());
    }
}
