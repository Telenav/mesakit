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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.interfaces.collection.NextIterator;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramRectangle;
import com.telenav.mesakit.map.geography.shape.Outline;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.telenav.mesakit.map.geography.Precision.DM7;

/**
 * NOTE: Rectangles cannot cross the -180/180 longitude line.
 *
 * @author jonathanl (shibo)
 * @author matthieun
 */
@SuppressWarnings({ "unused", "SpellCheckingInspection" })
@UmlClassDiagram(diagram = DiagramRectangle.class)
public class Rectangle implements Intersectable, LocationSequence, Bounded, Outline, Dimensioned
{
    public static final Rectangle ALASKA_US = new Rectangle(new Location(Latitude.degrees(52), Longitude.degrees(-172)),
            new Location(Latitude.degrees(72), Longitude.degrees(-141)));

    public static final Rectangle BAY_AREA = fromLocations(
            new Location(Latitude.degrees(37.84016), Longitude.degrees(-122.60468)),
            new Location(Latitude.degrees(37.20627), Longitude.degrees(-121.70654)));

    public static final Rectangle BERLIN_DE_EU = new Rectangle(
            new Location(Latitude.degrees(52.2202), Longitude.degrees(12.8925)),
            new Location(Latitude.degrees(52.7621), Longitude.degrees(13.9993)));

    public static final Rectangle CALIFORNIA_AND_NEVADA = fromLocations(
            new Location(Latitude.degrees(32.13841), Longitude.degrees(-124.73877)),
            new Location(Latitude.degrees(42.0), Longitude.degrees(-114.19189)));

    public static final Rectangle CENTRAL_NA = new Rectangle(
            new Location(Latitude.degrees(12.72608), Longitude.degrees(-108.36914)),
            new Location(Latitude.degrees(63.54855), Longitude.degrees(-87.45117)));

    public static final Rectangle CONTINENTAL_US = new Rectangle(
            new Location(Latitude.degrees(24), Longitude.degrees(-128)),
            new Location(Latitude.degrees(52), Longitude.degrees(-64)));

    public static final Rectangle EAST_NA = new Rectangle(
            new Location(Latitude.degrees(7.9722), Longitude.degrees(-93.69141)),
            new Location(Latitude.degrees(58.44773), Longitude.degrees(-48.33984)));

    public static final Rectangle EU = new Rectangle(
            new Location(Latitude.degrees(32.84267), Longitude.degrees(-14.58984)),
            new Location(Latitude.degrees(60.67318), Longitude.degrees(43.24219)));

    public static final Rectangle HAWAII_US = new Rectangle(new Location(Latitude.degrees(18), Longitude.degrees(-161)),
            new Location(Latitude.degrees(24), Longitude.degrees(-153)));

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Location.DegreesConverter LOCATION_CONVERTER = new Location.DegreesConverter(LOGGER);

    public static final Rectangle LOS_ANGLES_US = new Rectangle(
            new Location(Latitude.degrees(33.20192), Longitude.degrees(-119.11377)),
            new Location(Latitude.degrees(35.37114), Longitude.degrees(-116.36719)));

    public static final Rectangle MAXIMUM = new Rectangle(
            new Location(Latitude.MINIMUM, Longitude.MINIMUM),
            new Location(Latitude.MAXIMUM, Longitude.MAXIMUM));

    public static final Rectangle MINIMUM = new Rectangle(Location.ORIGIN, Location.ORIGIN);

    public static final Rectangle PUERTO_RICO_US = new Rectangle(
            new Location(Latitude.degrees(17), Longitude.degrees(-68)),
            new Location(Latitude.degrees(19), Longitude.degrees(-65)));

    public static final Rectangle SUNNYVALE_CA_US = new Rectangle(
            new Location(Latitude.degrees(37.352147), Longitude.degrees(-122.059492)),
            new Location(Latitude.degrees(37.386893), Longitude.degrees(-122.004238)));

    public static final Rectangle TELENAV_CA_US = new Rectangle(
            new Location(Latitude.degrees(37.385572), Longitude.degrees(-122.006137)),
            new Location(Latitude.degrees(37.416664), Longitude.degrees(-121.987064)));

    public static final Rectangle WEST_NA = new Rectangle(
            new Location(Latitude.degrees(18.39623), Longitude.degrees(-126.12305)),
            new Location(Latitude.degrees(60.06484), Longitude.degrees(-106.69922)));

    public static ArgumentParser.Builder<Rectangle> argumentParser(String description)
    {
        return ArgumentParser.builder(Rectangle.class).converter(new Rectangle.Converter(LOGGER))
                .description(description);
    }

    /**
     * @param objects The bounded objects
     * @return The smallest bounds that contains all the bounded objects
     */
    public static Rectangle fromBoundedObjects(Bounded[] objects)
    {
        var builder = new BoundingBoxBuilder();
        for (var object : objects)
        {
            builder.add(object.bounds());
        }
        return builder.build();
    }

    /**
     * @param objects The bounded objects
     * @return The smallest bounds that contains all the bounded objects
     */
    public static Rectangle fromBoundedObjects(Iterable<? extends Bounded> objects)
    {
        var builder = new BoundingBoxBuilder();
        for (Bounded object : objects)
        {
            builder.add(object.bounds());
        }
        return builder.build();
    }

    /**
     * Constructs a rectangle from a center and an approximate orthogonal distance from the center
     *
     * @param center The center
     * @param approximateOrthogonalRadius The orthogonal distance from the center to the edges
     * @return The rectangle
     */
    public static Rectangle fromCenterAndRadius(Location center, Distance approximateOrthogonalRadius)
    {
        var bottomLeft = center
                .moved(Heading.degrees(270), approximateOrthogonalRadius)
                .moved(Heading.degrees(180), approximateOrthogonalRadius);

        var topRight = center
                .moved(Heading.degrees(90), approximateOrthogonalRadius)
                .moved(Heading.degrees(0), approximateOrthogonalRadius);

        return fromLocations(bottomLeft, topRight);
    }

    /**
     * @return A rectangle for the given DM7 ints
     */
    public static Rectangle fromInts(int bottom, int left, int top, int right)
    {
        return new Rectangle(bottom, left, top, right);
    }

    /**
     * @return A zero size rectangle at the given location
     */
    public static Rectangle fromLocation(Location location)
    {
        return fromLocations(location, location);
    }

    /**
     * @return A rectangle of the given width and height with the bottom left at the given location
     */
    public static Rectangle fromLocationWidthAndHeight(Location location, Width width, Height height)
    {
        return new Rectangle(location, location.offsetBy(width).offsetBy(height));
    }

    /**
     * Constructs a rectangle from two unordered locations. The top right location is not included in the rectangle.
     *
     * @param a One corner of the rectangle
     * @param b The other corner of the rectangle.
     * @return The rectangle
     */
    public static Rectangle fromLocations(Location a, Location b)
    {
        return new Rectangle(new Location(a.latitude().minimum(b.latitude()), a.longitude().minimum(b.longitude())),
                new Location(a.latitude().maximum(b.latitude()), a.longitude().maximum(b.longitude())));
    }

    /**
     * Constructs a rectangle from two unordered locations. The top right location is included in the rectangle. This is
     * best when determining the bounds around two points.
     *
     * @param a One corner of the rectangle
     * @param b The other corner of the rectangle.
     * @return The rectangle
     */
    public static Rectangle fromLocationsInclusive(Location a, Location b)
    {
        return fromLocations(a, b).incremented();
    }

    /**
     * @return A rectangle for the given DM7 longs
     */
    public static Rectangle fromLongs(long bottomLeft, long topRight)
    {
        return new Rectangle(Location.latitude(bottomLeft),
                Location.longitude(bottomLeft),
                Location.latitude(topRight),
                Location.longitude(topRight));
    }

    /**
     * @return A rectangle from the given string of the form: [latitude-1],[longitude-1]:[latitude-2],[latitude-2]
     */
    public static Rectangle parse(String string)
    {
        var parts = StringList.split(string, ":");
        if (parts.size() == 2)
        {
            var a = LOCATION_CONVERTER.convert(parts.get(0));
            var b = LOCATION_CONVERTER.convert(parts.get(1));
            return (a == null || b == null) ? null : fromLocations(a, b);
        }
        return null;
    }

    public static SwitchParser.Builder<Rectangle> rectangleSwitchParser(Listener listener,
                                                                        String name,
                                                                        String description)
    {
        return SwitchParser.builder(Rectangle.class)
                .name(name)
                .converter(new Rectangle.Converter(listener))
                .description(description);
    }

    /**
     * File name compatible converter for a rectangle using this format:
     *
     * <pre>
     *     [bottomLeftLatitude],[bottomLeftLongitude]:[topRightLatitude],[topRightLongitude]
     * </pre>
     *
     * @author ericg
     * @author jonathanl (shibo)
     */
    public static class Converter extends BaseStringConverter<Rectangle>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Rectangle onToValue(String value)
        {
            return Rectangle.parse(value);
        }
    }

    /**
     * File name compatible converter for a rectangle using this format:
     *
     * <pre>
     * [bottomLeftLatitude],[bottomLeftLongitude]:[topRightLatitude],[topRightLongitude]
     * </pre>
     *
     * @author jonathanl (shibo)
     */
    public static class FileNameConverter extends BaseStringConverter<Rectangle>
    {
        /**
         * String used when there's no bounds specified
         */
        public static final String NO_BOUNDS = "null";

        private final Latitude.DegreesConverter latitudeConverter;

        private final Longitude.DegreesConverter longitudeConverter;

        public FileNameConverter(Listener listener)
        {
            super(listener);
            latitudeConverter = new Latitude.DegreesConverter(listener);
            longitudeConverter = new Longitude.DegreesConverter(listener);
        }

        @Override
        protected String onToString(Rectangle value)
        {
            return value.toFileString();
        }

        @Override
        protected Rectangle onToValue(String value)
        {
            if (value.equals(NO_BOUNDS))
            {
                return MAXIMUM;
            }
            var parts = StringList.split(value, "_");
            var bottomLeftLatitude = latitudeConverter.convert(parts.get(0));
            var bottomLeftLongitude = longitudeConverter.convert(parts.get(1));
            var topRightLatitude = latitudeConverter.convert(parts.get(2));
            var topRightLongitude = longitudeConverter.convert(parts.get(3));
            return (bottomLeftLatitude == null || bottomLeftLongitude == null || topRightLatitude == null || topRightLongitude == null)
                    ? null : fromLocations(new Location(bottomLeftLatitude, bottomLeftLongitude),
                    new Location(topRightLatitude, topRightLongitude));
        }
    }

    /**
     * {@link Rectangle}s are in DM7.
     */
    private int bottomInDm7;

    private int leftInDm7;

    private int topInDm7;

    private int rightInDm7;

    private transient Location bottomLeft;

    private transient Location topRight;

    protected Rectangle()
    {
    }

    /**
     * Constructs a rectangle. The bottom left corner is included in the rectangle while the top right corner is NOT
     * included. This means that contains(bottomLeft) == true while contains(topRight) == false. To include the top
     * right location *inside* the rectangle so that contains(topRight) == true, either call {@link #incremented()} to
     * expand the rectangle's top right bounds by one dm7 value or call {@link Location#decremented()} with the
     * top-right corner location to move it inside the rectangle.
     *
     * @param bottomLeft The bottom left corner of the rectangle (inclusive)
     * @param topRight The top right corner of the rectangle (exclusive)
     */
    protected Rectangle(Location bottomLeft, Location topRight)
    {
        bottomInDm7 = bottomLeft.latitude().asDm7();
        leftInDm7 = bottomLeft.longitude().asDm7();
        topInDm7 = topRight.latitude().asDm7();
        rightInDm7 = topRight.longitude().asDm7();

        if (bottomInDm7 > topInDm7 || leftInDm7 > rightInDm7)
        {
            throw new IllegalArgumentException("Invalid rectangle: " + bottomLeft + " to " + topRight);
        }
    }

    protected Rectangle(int bottomLeftInDm7, int topRightInDm7)
    {
        bottomInDm7 = Location.latitude(bottomLeftInDm7);
        leftInDm7 = Location.longitude(bottomLeftInDm7);
        topInDm7 = Location.latitude(topRightInDm7);
        rightInDm7 = Location.longitude(topRightInDm7);
    }

    private Rectangle(int bottomInDm7, int leftInDm7, int topInDm7, int rightInDm7)
    {
        this.bottomInDm7 = bottomInDm7;
        this.leftInDm7 = leftInDm7;
        this.topInDm7 = topInDm7;
        this.rightInDm7 = rightInDm7;
    }

    /**
     * Area = EarthRadius^2 * (right - left) * (sin(top) - sin(bottom))
     */
    public Area area()
    {
        var earthRadius = Distance.EARTH_RADIUS_MINOR.asMeters();
        var earthSquare = earthRadius * earthRadius;
        var radiansTop = top().asRadians();
        var radiansBottom = bottom().asRadians();
        var radiansLeft = left().asRadians();
        var radiansRight = right().asRadians();
        var latSinDifference = Math.sin(radiansTop) - Math.sin(radiansBottom);
        var lonDifference = radiansLeft - radiansRight;
        var squareMeters = earthSquare * Math.abs(lonDifference) * Math.abs(latSinDifference);
        return Area.squareMeters((long) squareMeters);
    }

    public Polygon asPolygon()
    {
        return Polygon.fromLocationSequence(locationSequence());
    }

    public Polyline asPolyline()
    {
        var builder = new PolylineBuilder();
        builder.add(topLeft());
        builder.add(topRight());
        builder.add(bottomRight());
        builder.add(bottomLeft());
        builder.add(topLeft());
        return builder.build();
    }

    public Latitude bottom()
    {
        return Latitude.dm7(bottomInDm7);
    }

    public double bottomInDegrees()
    {
        return DM7.toDegrees(bottomInDm7);
    }

    public int bottomInDm7()
    {
        return bottomInDm7;
    }

    public Location bottomLeft()
    {
        if (bottomLeft == null)
        {
            bottomLeft = Location.dm7(bottomInDm7, leftInDm7);
        }
        return bottomLeft;
    }

    public Location bottomRight()
    {
        return new Location(bottomLeft().latitude(), topRight().longitude());
    }

    @Override
    public Rectangle bounds()
    {
        return this;
    }

    public Iterable<Rectangle> cells(Distance size)
    {
        return Iterables.iterable(() -> new NextIterator<>()
        {
            private Latitude bottom = bottom();

            private Longitude left = left();

            @Override
            public Rectangle next()
            {
                if (left.isGreaterThan(right()))
                {
                    left = left();
                    bottom = bottom.plus(size.asAngle());
                }
                if (bottom.isLessThan(top()))
                {
                    var top = bottom.plus(size.asAngle());
                    var right = left.plus(size.asAngle());
                    var next = fromLocations(new Location(bottom, left),
                            new Location(top.minimum(top()), right.minimum(right())));
                    left = right;
                    return next;
                }
                return null;
            }
        });
    }

    /**
     * @return The center of this rectangle
     */
    public Location center()
    {
        var centerLatitude = bottomInDm7 + Math.abs(topInDm7 - bottomInDm7) / 2;
        var centerLongitude = (int) (leftInDm7 + ((Math.abs((long) rightInDm7 - (long) leftInDm7) / 2L)));
        return Location.dm7(centerLatitude, centerLongitude);
    }

    /**
     * @return A value for use in a {@link Comparator} that sorts rectangles by longitude
     */
    public int compareHorizontal(Rectangle that)
    {
        var thisCenter = leftInDm7 + ((rightInDm7 - leftInDm7) / 2);
        var thatCenter = that.leftInDm7 + ((that.rightInDm7 - that.leftInDm7) / 2);
        return Integer.compare(thisCenter, thatCenter);
    }

    /**
     * @return A value for use in a {@link Comparator} that sorts rectangles by longitude
     */
    public int compareVertical(Rectangle that)
    {
        var thisCenter = bottomInDm7 + ((topInDm7 - bottomInDm7) / 2);
        var thatCenter = that.bottomInDm7 + ((that.topInDm7 - that.bottomInDm7) / 2);
        return Integer.compare(thisCenter, thatCenter);
    }

    public int compareVerticalThenHorizontal(Rectangle that)
    {
        var compareVertical = compareVertical(that);
        if (compareVertical == 0)
        {
            return compareHorizontal(that);
        }
        return compareVertical;
    }

    @Override
    public final Outline.Containment containment(Location location)
    {
        var latitude = location.latitude().asDm7();
        var longitude = location.longitude().asDm7();

        // All locations are inside the maximum rectangle
        if (this == MAXIMUM)
        {
            return Containment.INSIDE;
        }

        // If the point is inside the bottom, left, top and right, exclusive
        if (latitude > bottomInDm7
                && latitude < topInDm7
                && longitude > leftInDm7
                && longitude < rightInDm7)
        {
            // then it's inside
            return Containment.INSIDE;
        }

        // If the point is exactly at the top or bottom
        if (latitude == bottomInDm7 || latitude == topInDm7)
        {
            // and it's between left and right, inclusive
            if (longitude >= leftInDm7 && longitude <= rightInDm7)
            {
                // then it's on the top or bottom border
                return Containment.ON_BORDER;
            }
        }

        // If the point is exactly on the left or right
        if (longitude == leftInDm7 || longitude == rightInDm7)
        {
            // and it's between the bottom and the top, inclusive
            if (latitude >= bottomInDm7 && longitude <= topInDm7)
            {
                // then it's on the left or right border
                return Containment.ON_BORDER;
            }
        }

        // The point is outside this rectangle
        return Containment.OUTSIDE;
    }

    /**
     * @param location The location
     * @return True if this rectangle contains the location (if the location is &gt;= the lower left corner AND &lt; the
     * top right corner).
     */
    @Override
    public boolean contains(Location location)
    {
        if (this == MAXIMUM)
        {
            return true;
        }
        else
        {
            var latitude = location.latitudeInDm7();
            var longitude = location.longitudeInDm7();
            if (latitude >= bottomInDm7)
            {
                if (latitude < topInDm7 || DM7.isMaximumLatitude(topInDm7))
                {
                    if (longitude >= leftInDm7)
                    {
                        return longitude < rightInDm7 || DM7.isMaximumLongitude(rightInDm7);
                    }
                }
            }
            return false;
        }
    }

    /**
     * Returns true if the given rectangle is inside this rectangle. Because the top right corner of a rectangle is
     * excluded from a rectangle, the point just inside the given rectangle is tested with {@link
     * Rectangle#containment(Location)}, which results in the expected behavior that a rectangle contains itself.
     *
     * @param that The rectangle to test
     * @return True if the given rectangle is inside this rectangle
     */
    public boolean contains(Rectangle that)
    {
        if (this == MAXIMUM)
        {
            return true;
        }
        else
        {
            return bottomInDm7 <= that.bottomInDm7
                    && leftInDm7 <= that.leftInDm7
                    && topInDm7 >= that.topInDm7
                    && rightInDm7 >= that.rightInDm7;
        }
    }

    /**
     * @return True if this segment crosses the equator
     */
    public boolean crossesEquator()
    {
        return Segment.EQUATOR.intersects(this);
    }

    /**
     * @return A new rectangle with the top right corner decremented by one Dm7 unit.
     */
    public Rectangle decremented()
    {
        return fromInts(bottomInDm7, leftInDm7, topInDm7 - 1, rightInDm7 - 1);
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
        {
            return true;
        }
        if (object instanceof Rectangle)
        {
            var that = (Rectangle) object;
            return bottomInDm7 == that.bottomInDm7
                    && leftInDm7 == that.leftInDm7
                    && topInDm7 == that.topInDm7
                    && rightInDm7 == that.rightInDm7;
        }
        return false;
    }

    public Rectangle expanded(Distance distance)
    {
        var expansion = Angle.degrees(distance.asDegrees());
        var newBottomLeft = new Location(bottomLeft().latitude().minus(expansion),
                bottomLeft().longitude().minus(expansion));
        var newTopRight = new Location(topRight().latitude().plus(expansion),
                topRight().longitude().plus(expansion));
        return fromLocations(newBottomLeft, newTopRight);
    }

    public Rectangle expanded(Percent scaleFactor)
    {
        var latitude = Latitude.degrees(scaleFactor.scale(height().asDegrees()));
        var longitude = Longitude.degrees(scaleFactor.scale(width().asDegrees()));
        return fromLocations(bottomLeft().relativeTo(latitude, longitude), topRight().offsetBy(latitude, longitude));
    }

    public Rectangle expandedBottom(Distance distance)
    {
        var expansion = Angle.degrees(distance.asDegrees());
        var newBottomLeft = new Location(bottomLeft().latitude().minus(expansion), bottomLeft().longitude());
        return fromLocations(newBottomLeft, topRight());
    }

    public Rectangle expandedLeft(Distance distance)
    {
        var expansion = Angle.degrees(distance.asDegrees());
        var newBottomLeft = new Location(bottomLeft().latitude(), bottomLeft().longitude().minus(expansion));
        return fromLocations(newBottomLeft, topRight());
    }

    public Rectangle expandedRight(Distance distance)
    {
        var expansion = Angle.degrees(distance.asDegrees());
        var newTopRight = new Location(topRight().latitude(), topRight().longitude().plus(expansion));
        return fromLocations(bottomLeft(), newTopRight);
    }

    public Rectangle expandedToInclude(Location location)
    {
        if (!containment(location).isOutside())
        {
            return this;
        }
        else
        {
            var latitudeInDm7 = location.latitudeInDm7();
            var longitudeInDm7 = location.longitudeInDm7();

            var bottomInDm7 = Math.min(bottomInDm7(), latitudeInDm7);
            var leftInDm7 = Math.min(leftInDm7(), longitudeInDm7);
            var topInDm7 = Math.max(topInDm7(), latitudeInDm7);
            var rightInDm7 = Math.max(rightInDm7(), longitudeInDm7);

            return fromInts(bottomInDm7, leftInDm7, topInDm7, rightInDm7);
        }
    }

    public Rectangle expandedTop(Distance distance)
    {
        var expansion = Angle.degrees(distance.asDegrees());
        var newTopRight = new Location(topRight().latitude().plus(expansion), topRight().longitude());
        return fromLocations(bottomLeft(), newTopRight);
    }

    @Override
    public int hashCode()
    {
        var hashCode = Hash.SEED + bottomInDm7;
        hashCode = hashCode * Hash.SEED + leftInDm7;
        hashCode = hashCode * Hash.SEED + topInDm7;
        hashCode = hashCode * Hash.SEED + rightInDm7;
        return hashCode;
    }

    /**
     * Computes the height of this rectangle in latitudinal degrees. Note that the return value is an {@link Angle}
     * because not all latitudinal distances are valid {@link Latitude}s (for example, the distance from the north to
     * the South Pole is 180, which is not a valid {@link Latitude}).
     *
     * @return The height of this rectangle
     */
    @Override
    public Height height()
    {
        return Height.angle(topRight().latitude().absoluteDifference(bottomLeft().latitude()));
    }

    /**
     * @return The height of this rectangle as a distance
     */
    public Distance heightAsDistance()
    {
        return bottomLeft().preciseDistanceTo(topLeft());
    }

    public double heightInDegrees()
    {
        return Math.abs(topInDegrees() - bottomInDegrees());
    }

    /**
     * @return A new rectangle with the top right corner incremented
     */
    public Rectangle incremented()
    {
        return fromInts(bottomInDm7, leftInDm7, topInDm7 + 1, rightInDm7 + 1);
    }

    public Rectangle intersect(Rectangle that)
    {
        if (intersects(that))
        {
            return new Rectangle(
                    new Location(bottomLeft().latitude().maximum(that.bottomLeft().latitude()),
                            bottomLeft().longitude().maximum(that.bottomLeft().longitude())),
                    new Location(topRight().latitude().minimum(that.topRight().latitude()),
                            topRight().longitude().minimum(that.topRight().longitude())));
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean intersects(Rectangle that)
    {
        if (this != MAXIMUM)
        {
            if (rightInDm7 < that.leftInDm7
                    || leftInDm7 > that.rightInDm7
                    || topInDm7 < that.bottomInDm7
                    || bottomInDm7 > that.topInDm7)
            {
                return false;
            }
        }
        return true;
    }

    public boolean isEmpty()
    {
        return height().isZero() || width().isZero();
    }

    /**
     * @return True if this rectangle is wider than it is tall
     */
    public boolean isHorizontal()
    {
        return width().isGreaterThan(height());
    }

    public boolean isMaximum()
    {
        return equals(MAXIMUM);
    }

    /**
     * @return True if this rectangle is taller than it is wide
     */
    public boolean isVertical()
    {
        return height().isGreaterThan(width());
    }

    public Longitude left()
    {
        return Longitude.dm7(leftInDm7);
    }

    public double leftInDegrees()
    {
        return DM7.toDegrees(leftInDm7);
    }

    public int leftInDm7()
    {
        return leftInDm7;
    }

    @Override
    public Iterable<Location> locationSequence()
    {
        List<Location> locations = new ArrayList<>();
        locations.add(bottomLeft());
        locations.add(bottomRight());
        locations.add(topRight());
        locations.add(topLeft());
        return locations;
    }

    public Rectangle moved(Offset offset)
    {
        return fromLocations(topLeft().offsetBy(offset), bottomRight().offsetBy(offset));
    }

    public Rectangle northEastQuadrant()
    {
        return fromLocations(center(), topRight());
    }

    public Rectangle northWestQuadrant()
    {
        return fromLocations(center(), topLeft());
    }

    public Longitude right()
    {
        return Longitude.dm7(rightInDm7);
    }

    public double rightInDegrees()
    {
        return DM7.toDegrees(rightInDm7);
    }

    public int rightInDm7()
    {
        return rightInDm7;
    }

    public Rectangle shrunken()
    {
        return shrunken(Latitude.dm7(1), Longitude.dm7(1));
    }

    public Rectangle shrunken(Angle angle)
    {
        return fromLocations(bottomLeft().offsetBy(Width.angle(angle)).offsetBy(Height.angle(angle)),
                topRight().minus(Width.angle(angle)).minus(Height.angle(angle)));
    }

    public Rectangle shrunken(Distance distance)
    {
        return fromLocations(bottomLeft().moved(Heading.EAST, distance).moved(Heading.NORTH, distance),
                topRight().moved(Heading.WEST, distance).moved(Heading.SOUTH, distance));
    }

    public Rectangle shrunken(Latitude latitude, Longitude longitude)
    {
        return fromLocations(bottomLeft().offsetBy(latitude, longitude), topRight().relativeTo(latitude, longitude));
    }

    public Rectangle shrunken(Percent scaleFactor)
    {
        var latitude = Latitude.degrees(scaleFactor.scale(height().asDegrees()));
        var longitude = Longitude.degrees(scaleFactor.scale(width().asDegrees()));
        return shrunken(latitude, longitude);
    }

    @Override
    public Size size()
    {
        return new Size(width(), height());
    }

    public Rectangle southEastQuadrant()
    {
        return fromLocations(center(), bottomRight());
    }

    public Rectangle southWestQuadrant()
    {
        return fromLocations(center(), bottomLeft());
    }

    public String toCommaSeparatedString()
    {
        return bottomLeft() + "," + topRight();
    }

    public String toFileString()
    {
        return bottomLeft().latitude() + "_" + bottomLeft().longitude() + "_" + topRight().latitude() + "_"
                + topRight().longitude();
    }

    @Override
    public String toString()
    {
        return bottomLeft() + ":" + topRight();
    }

    public Latitude top()
    {
        return Latitude.dm7(topInDm7);
    }

    public double topInDegrees()
    {
        return DM7.toDegrees(topInDm7);
    }

    public int topInDm7()
    {
        return topInDm7;
    }

    public Location topLeft()
    {
        return new Location(topRight().latitude(), bottomLeft().longitude());
    }

    public Location topRight()
    {
        if (topRight == null)
        {
            topRight = Location.dm7(topInDm7, rightInDm7);
        }
        return topRight;
    }

    public Rectangle union(Rectangle that)
    {
        return expandedToInclude(that.bottomLeft()).expandedToInclude(that.topRight());
    }

    public Iterable<Rectangle> verticalStrips(Distance width, Distance overlap)
    {
        return Iterables.iterable(() -> new NextIterator<>()
        {
            private Longitude left = left();

            @Override
            public Rectangle next()
            {
                if (left.isLessThan(right().minus(overlap.asAngle())))
                {
                    var right = left.plus(width.add(overlap).asAngle());
                    var next = fromLocations(new Location(bottom(), left),
                            new Location(top(), right.minimum(right())));
                    left = right.minus(overlap.asAngle());
                    return next;
                }
                return null;
            }
        });
    }

    public Iterable<Rectangle> verticalStrips(int stripCount, Width overlap)
    {
        var difference = right().asWidth().minus(left().asWidth());
        var stripWidth = difference.dividedBy(stripCount);

        return Iterables.iterable(() -> new NextIterator<>()
        {
            private Longitude left = left();

            @Override
            public Rectangle next()
            {
                if (left.isLessThan(right().minus(overlap)))
                {
                    var right = left.plus(stripWidth).plus(overlap);
                    if (right().isLessThan(right)
                            || right().minus(right).isLessThan(stripWidth.dividedBy(2.0)))
                    {
                        right = right();
                    }
                    var next = fromLocations(new Location(bottom(), left),
                            new Location(top(), right));
                    left = right.minus(overlap);
                    return next;
                }
                return null;
            }
        });
    }

    /**
     * @return The widest width of this rectangle
     */
    public Distance widestWidth()
    {
        // If the rectangle crosses the equator
        if (crossesEquator())
        {
            // and it's the maximum rectangle
            if (equals(MAXIMUM))
            {
                // then the width is the whole circumference
                return Distance.EARTH_CIRCUMFERENCE;
            }

            // otherwise, find the distance from left to right at the equator
            return left().distanceTo(right(), Latitude.ORIGIN);
        }

        // The rectangle is on one side of the equator or the other
        return widthAtBase();
    }

    /**
     * Computes the width of this rectangle in longitudinal degrees. Note that the return value is an {@link Angle}
     * because not all longitudinal distances are valid {@link Longitude}s (for example, the distance from -180 to +180
     * is 360 degrees, which is not a valid {@link Longitude}).
     *
     * @return The width of this rectangle
     */
    @Override
    public Width width()
    {
        return Width.angle(topRight().longitude().absoluteDifference(bottomLeft().longitude()));
    }

    /**
     * Compute the distance between the two points (left and right) that are the closest to the equator. For example, a
     * rectangle in the northern hemisphere will use bottomLeft and bottomRight, whereas a rectangle in the southern
     * hemisphere will use topLeft and topRight.
     *
     * @return The base width of this rectangle
     */
    public Distance widthAtBase()
    {
        Location point1;
        Location point2;

        // Select the side that is closest to the equator
        if (Math.abs(topRight().latitude().asDegrees()) > Math.abs(bottomLeft().latitude().asDegrees()))
        {
            point1 = bottomLeft();
            point2 = bottomRight();
        }
        else
        {
            point1 = topLeft();
            point2 = topRight();
        }
        return point1.preciseDistanceTo(point2);
    }

    public double widthInDegrees()
    {
        return Math.abs(rightInDegrees() - leftInDegrees());
    }

    public Rectangle withBottom(Latitude latitude)
    {
        return fromLocations(bottomLeft().withLatitude(latitude), topRight());
    }

    public Rectangle withLeft(Longitude longitude)
    {
        return fromLocations(bottomLeft().withLongitude(longitude), topRight());
    }

    public Rectangle withRight(Longitude longitude)
    {
        return fromLocations(bottomLeft(), topRight().withLongitude(longitude));
    }

    public Rectangle withTop(Latitude latitude)
    {
        return fromLocations(bottomLeft(), topRight().withLatitude(latitude));
    }

    java.awt.Rectangle asAwtRectangleInDm7()
    {
        return new java.awt.Rectangle(leftInDm7, bottomInDm7, widthInDm7(), heightInDm7());
    }

    private int heightInDm7()
    {
        return topInDm7 - bottomInDm7;
    }

    private int widthInDm7()
    {
        return rightInDm7 - leftInDm7;
    }
}
