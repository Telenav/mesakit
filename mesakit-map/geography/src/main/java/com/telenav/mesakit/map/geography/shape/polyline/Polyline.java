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

import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.LatLng;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.iteration.BaseIterator;
import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Arrays;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.string.Split;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.interfaces.collection.Indexable;
import com.telenav.kivakit.interfaces.collection.NextValue;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.GeographyLimits;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.LocatedHeading;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.lexakai.DiagramPolyline;
import com.telenav.mesakit.map.geography.shape.polyline.compression.differential.CompressedPolyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.geography.shape.segment.SegmentPair;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

/**
 * A sequence of two or more locations that are connected, leading from a {@link #start()} to an {@link #end()}
 * location. A polyline is {@link Iterable} so it can be used in an advanced for loop:
 * <pre>
 *     for (var location : polyline) { ... }
 * </pre>
 * Poly-lines also have a bounding rectangle retrieved with {@link Bounded#bounds()}. It can be determined if the
 * polyline intersects a rectangle or segment with {@link Intersectable#intersects(Rectangle)} and {@link
 * #intersects(Segment)}, and whether it intersects itself with {@link #selfIntersection()}. The point of intersection
 * with another polyline or with a segment can be determined with {@link #intersection(Polyline)} and {@link
 * #intersection(Segment)}. Whether two poly-lines cross each other can be determined with {@link #crosses(Polyline)}.
 * <p>
 * Segments of the polyline can be retrieved with:
 * <ul>
 *     <li>{@link #firstSegment()} - The first segment of the polyline</li>
 *     <li>{@link #lastSegment()} - The last segment of the polyline</li>
 *     <li>{@link #secondSegment()} - The second segment (if any)</li>
 *     <li>{@link #middleSegment()} - The middle segment. If there are an even number of segments, the segment prior to the midpoint will be chosen.</li>
 * </ul>
 * <p>
 * Points along the polyline can be located with:
 * <ul>
 *     <li>{@link #size()} - The number of locations in the polyline, guaranteed to be at least 2</li>
 *     <li>{@link #start()} - The starting point of the polyline</li>
 *     <li>{@link #end()} - The ending point of the polyline</li>
 *     <li>{@link #get(int)} - The nth point in the polyline, between 0 and {@link #size()} - 1</li>
 *     <li>{@link #at(Distance)} - The point at the given distance along the polyline (not necessarily a shape point)</li>
 *     <li>{@link #at(Percent)} - The point at the given percentage of the polyline length</li>
 *     <li>{@link #midpoint()} - The point equidistant along the polyline from the start and the end</li>
 * </ul>
 * <p>
 * Various methods help to determine the nature of the polyline:
 * <ul>
 *     <li>{@link #isBent(Distance, Angle)} - Is the polyline bent by more than the given angle within the given distance?</li>
 *     <li>{@link #isStraight(Distance, Angle)} - Is the polyline straight within the given distance and maximum bend angle?</li>
 *     <li>{@link #isLoop()} - Does the polyline form a closed loop?</li>
 *     <li>{@link #isSegment()} - Is the polyline just a single segment?</li>
 *     <li>{@link #isConnectedTo(Polyline)} - Does the start or end of the polyline coincide with the start or end of the given polyline?</li>
 *     <li>{@link #bend()} - The amount that the polyline bends to the left (negative) or right (positive)</li>
 *     <li>{@link #closeness(Polyline, Distance, Angle)} - Closeness of this polyline to that one as a percentage, where 0% is not at all close
 *     and 100% is the same polyline. The closeness is measured by considering segments that are within the given maximum distance apart
 *     and within the given maximum heading deviation</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see Rectangle
 * @see Segment
 * @see Intersectable
 * @see Bounded
 */
@UmlClassDiagram(diagram = DiagramPolyline.class)
@UmlRelation(label = "contains", referent = Location.class, referentCardinality = "2+")
public class Polyline implements
        Indexable<Location>,
        Bounded,
        Intersectable,
        LocationSequence,
        Iterable<Location>
{
    public static final Distance DEFAULT_MAXIMUM_SHAPE_POINT_SPACING = Distance.meters(15);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static Polyline fromGoogleMapsEncoded(String text)
    {
        var line = PolylineEncoding.decode(text);
        var builder = new PolylineBuilder();
        for (var at : line)
        {
            builder.add(at.lat, at.lng);
        }
        return builder.build();
    }

    /**
     * Construct from a convenient argument list of locations
     */
    public static Polyline fromLocations(Location one, Location two, Location... more)
    {
        var builder = new PolylineBuilder();
        builder.add(one);
        builder.add(two);
        for (var location : more)
        {
            builder.add(location);
        }
        return builder.build();
    }

    public static Polyline fromLocations(Iterable<Location> locations)
    {
        if (locations instanceof Polyline)
        {
            return (Polyline) locations;
        }
        if (locations instanceof List)
        {
            return new Polyline((List<Location>) locations);
        }
        return fromLocations(locations.iterator());
    }

    public static Polyline fromLocations(Iterator<Location> locations)
    {
        return new PolylineBuilder().addAll(locations).build();
    }

    public static Polyline fromLocations(long start, long end)
    {
        return new Polyline(new long[] { start, end });
    }

    public static Polyline fromLongArray(LongArray locations)
    {
        var builder = new PolylineBuilder();
        for (var i = 0; i < locations.size(); i++)
        {
            builder.add(Location.dm7(locations.get(i)));
        }
        return builder.build();
    }

    public static Polyline fromLongs(long[] locations)
    {
        return new Polyline(locations);
    }

    public static Polyline parse(String value)
    {
        if (!Strings.isEmpty(value))
        {
            var builder = new PolylineBuilder();
            var converter = new Location.DegreesConverter(LOGGER);
            for (var location : Split.split(value, ":"))
            {
                builder.add(converter.convert(location));
            }
            return builder.build();
        }
        return null;
    }

    public static class Converter extends BaseStringConverter<Polyline>
    {
        private final Location.DegreesConverter locationConverter;

        private final Separators separators;

        public Converter(Listener listener, Separators separators)
        {
            super(listener);
            this.separators = separators;
            locationConverter = new Location.DegreesConverter(listener, separators.child());
            allowEmpty(true);
            allowNull(true);
        }

        @Override
        protected String nullString()
        {
            return "";
        }

        @Override
        protected String onToString(Polyline value)
        {
            var locations = new StringList();
            for (var location : value)
            {
                locations.add(locationConverter.unconvert(location));
            }
            return locations.join(separators.current());
        }

        @Override
        protected Polyline onToValue(String value)
        {
            if (!Strings.isEmpty(value))
            {
                var builder = new PolylineBuilder();
                for (var location : Split.split(value, separators.current()))
                {
                    builder.add(locationConverter.convert(location));
                }
                return builder.build();
            }
            return null;
        }
    }

    /**
     * {@link Polyline} decoding and encoding using the Google method
     *
     * @author matthieun
     */
    public static class GoogleEncodingConverter extends BaseStringConverter<Polyline>
    {
        public GoogleEncodingConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(Polyline value)
        {
            return createPolyline(value);
        }

        @Override
        protected Polyline onToValue(String value)
        {
            return decodePolyline(value);
        }

        /**
         * From <a href="http://statsciolist.blogspot.com/2013/05/java-google-maps-polyline-encoding.html />
         *
         * @param line The {@link Polyline} to encode
         * @return The encoded polyline
         */
        private String createPolyline(Polyline line)
        {
            var oldLatitude = 0D;
            var oldLongitude = 0D;
            var nb = new StringBuilder();
            for (var temp : line)
            {
                var p1 = temp.latitude().asDegrees();
                var p2 = temp.longitude().asDegrees();

                if (Math.abs(p1 - oldLatitude) >= 0.00001)
                {
                    var temp2 = encodePolyline(p1 - oldLatitude);
                    nb.append(temp2);
                }
                else
                {
                    nb.append("?");
                }
                if (Math.abs(p2 - oldLongitude) >= 0.00001)
                {
                    var temp2 = encodePolyline(p2 - oldLongitude);
                    nb.append(temp2);
                }
                else
                {
                    nb.append("?");
                }
                oldLatitude = p1;
                oldLongitude = p2;
            }

            var temp = nb.toString();

            // Ensure temp for "*\*" pattern
            var pattern = Pattern.compile("\".*\\.*\"");
            var matcher = pattern.matcher(temp);

            while (matcher.find())
            {
                // Use matcher.start() and .end() to replace "\" with "\\"
                temp = temp.substring(0, matcher.start())
                        + temp.substring(matcher.start(), matcher.end()).replaceAll("\\\\", "\\\\\\\\")
                        + temp.substring(matcher.end());
            }
            return temp;
        }

        /**
         * {@link Polyline} decoding based on <a href= " http://jeffreysambells.com/2010/05/27/decoding-polylines-from
         * -google-maps-direction-api-with-java" />
         *
         * @param encoded The encoded {@link Polyline}
         * @return The decoded {@link Polyline}
         */
        @SuppressWarnings("DuplicatedCode")
        private Polyline decodePolyline(String encoded)
        {
            var builder = new PolylineBuilder();
            var index = 0;
            var encodedLength = encoded.length();
            int lat = 0, lon = 0;
            while (index < encodedLength)
            {
                int b;
                var shift = 0;
                var result = 0;
                do
                {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                }
                while (b >= 0x20);
                var deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += deltaLat;

                shift = 0;
                result = 0;
                do
                {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                }
                while (b >= 0x20);
                var deltaLon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lon += deltaLon;

                var latitude = Latitude.dm5(lat);
                var longitude = Longitude.dm5(lon);
                var location = new Location(latitude, longitude);
                builder.add(location);
            }
            return builder.build();
        }

        /**
         * From <a href="http://statsciolist.blogspot.com/2013/05/java-google-maps-polyline-encoding.html />
         *
         * @param angleInDegrees The angle in degrees to encode
         * @return The encoded angle
         */
        private String encodePolyline(double angleInDegrees)
        {
            // Google's procedure for encoding polyline data
            // This doesn't cater for backslashes in string literals i.e. the character sequence "\"
            // which should be returned as "\\". Function createPolyline will do this.

            String polyline;
            var signNum = (int) Math.signum(angleInDegrees);

            var b = (int) Math.round(angleInDegrees * 1e5);
            List<Integer> ab = new ArrayList<>();

            // Left shift
            b = b << 1;

            // Invert if negative
            if (signNum < 0)
            {
                b = ~b;
            }

            // Split into 5-bit chunks and reverse order
            while (b > 0)
            {
                ab.add(b % 32);
                b = b >> 5;
            }

            // Convert to ASCII
            var nc4 = new StringBuilder();
            for (var i = 0; i < ab.size() - 1; i++)
            {
                // Or with 0x20 and add 63
                var c = (char) ((ab.get(i) | 0x20) + 63);
                nc4.append(c);
            }
            // Add 63 to last chunk
            nc4.append((char) (ab.get(ab.size() - 1) + 63));
            polyline = nc4.toString();

            return polyline;
        }
    }

    public static class Intersection
    {
        private final Location location;

        private final boolean modified;

        public Intersection(Location location, boolean modified)
        {
            this.location = location;
            this.modified = modified;
        }

        public boolean isModified()
        {
            return modified;
        }

        public Location location()
        {
            return location;
        }
    }

    public static class LongArrayConverter extends BaseStringConverter<Polyline>
    {
        private final LongArray.Converter longArrayConverter;

        public LongArrayConverter(Listener listener, Separators separators)
        {
            super(listener);
            longArrayConverter = new LongArray.Converter(listener, separators);
            allowEmpty(true);
            allowNull(true);
        }

        @Override
        protected String nullString()
        {
            return "";
        }

        @Override
        protected String onToString(Polyline value)
        {
            if (!value.isEmpty())
            {
                return longArrayConverter.unconvert(value.asLongArray());
            }
            return "";
        }

        @Override
        protected Polyline onToValue(String value)
        {
            if (!Strings.isEmpty(value))
            {
                var converted = longArrayConverter.convert(value);
                return converted == null ? null : fromLongArray(converted);
            }
            return null;
        }
    }

    public class Loop
    {
        private int endIndex;

        private Count intersections;

        private int startIndex;

        public Location loopAt;

        public PolylineSection head()
        {
            return startIndex > 0 ? section(0, startIndex) : null;
        }

        public void intersections(Count intersections)
        {
            this.intersections = intersections;
        }

        public Count intersections()
        {
            return intersections;
        }

        public PolylineSection loop()
        {
            return section(startIndex, endIndex);
        }

        public Location loopAt()
        {
            return loopAt;
        }

        public PolylineSection tail()
        {
            return endIndex < size() - 1 ? section(endIndex, size() - 1) : null;
        }
    }

    private int bottomInDecimal = Integer.MAX_VALUE;

    private Integer hashCode;

    private int leftInDecimal = Integer.MAX_VALUE;

    /**
     * Length in millimeters of the polyline
     */
    private long lengthInMillimeters = -1;

    /**
     * Soft-referenced list of locations for convenience and efficiency
     */
    private transient SoftReference<List<Location>> locations;

    /**
     * The actual location data
     */
    private long[] locationsInDecimal;

    private int rightInDecimal = Integer.MIN_VALUE;

    // The bounding rectangle
    private int topInDecimal = Integer.MIN_VALUE;

    public Polyline(List<Location> locations)
    {
        ensure(locations.size() >= 2);

        this.locations = new SoftReference<>(locations);
        locationsInDecimal = new long[locations.size()];
        var i = 0;
        for (var location : locations)
        {
            locationsInDecimal[i++] = expandBounds(location.asDm7Long());
        }
    }

    public Polyline(long[] locationsInDecimal)
    {
        assert locationsInDecimal.length >= 2;

        locations = new SoftReference<>(null);
        this.locationsInDecimal = expandBounds(locationsInDecimal);
    }

    protected Polyline()
    {
    }

    public Polyline append(Polyline that)
    {
        var builder = new PolylineBuilder();
        builder.addAllUnique(locationSequence());
        builder.addAllUnique(that);
        return builder.build();
    }

    public String asGoogleMapsEncoded()
    {
        var positions = new ArrayList<LatLng>();
        for (var at : locations())
        {
            positions.add(new LatLng(at.longitudeInDegrees(), at.latitudeInDegrees()));
        }
        return PolylineEncoding.encode(positions);
    }

    @Override
    public @NotNull
    Iterator<Location> asIterator()
    {
        return new BaseIterator<>()
        {
            int index = 0;

            @Override
            protected Location onNext()
            {
                if (index < size())
                {
                    return get(index++);
                }
                return null;
            }
        };
    }

    @Override
    public @NotNull
    Iterator<Location> asIterator(Matcher<Location> matcher)
    {
        return new BaseIterator<>()
        {
            int index = 0;

            @Override
            protected Location onNext()
            {
                while (index < size())
                {
                    var location = get(index++);
                    if (matcher.matches(location))
                    {
                        return location;
                    }
                }
                return null;
            }
        };
    }

    /**
     * @return This polyline as a set of locations
     */
    public Set<Location> asLocationSet()
    {
        Set<Location> locations = new HashSet<>();
        for (var location : locationSequence())
        {
            locations.add(location);
        }
        return locations;
    }

    public LongArray asLongArray()
    {
        var array = new LongArray("asLongArray");
        array.initialSize(size());
        array.initialize();

        for (var location : locationSequence())
        {
            array.add(location.asLong());
        }
        return array;
    }

    /**
     * @return The location on this polyline at the given distance from the start of the polyline
     */
    public Location at(Distance distance)
    {
        if (distance.isLessThanOrEqualTo(length()))
        {
            for (var segment : segments())
            {
                if (distance.isLessThan(segment.length()))
                {
                    return segment.at(distance);
                }
                distance = distance.minus(segment.length());
            }
        }
        return null;
    }

    /**
     * @return The location on this polyline at the given percentage from the start of the polyline, where 0% is the
     * start of the polyline, 50% is the midpoint and 100% is the end.
     */
    public Location at(Percent parameter)
    {
        return at(length().times(parameter.asUnitValue()));
    }

    /**
     * @return This polyline with the given polyline attached at the closest end
     */
    public Polyline attach(Polyline that)
    {
        var endToStart = end().distanceTo(that.start());
        var endToEnd = end().distanceTo(that.end());
        var startToEnd = start().distanceTo(that.end());
        var startToStart = start().distanceTo(that.start());
        switch (minimum(new Distance[] { endToStart, endToEnd, startToEnd, startToStart }))
        {
            case 0:
                return append(that);
            case 1:
                return append(that.reversed());
            case 2:
                return that.append(this);
            case 3:
                return that.reversed().append(this);
        }
        return null;
    }

    /**
     * @return This polyline augmented with extra interpolated points so that the maximum spacing between locations is
     * {@link #DEFAULT_MAXIMUM_SHAPE_POINT_SPACING}
     */
    public Polyline augmented()
    {
        return augmented(DEFAULT_MAXIMUM_SHAPE_POINT_SPACING);
    }

    /**
     * @return This polyline augmented with extra interpolated points so that the maximum spacing between locations is
     * the given distance
     */
    public Polyline augmented(Distance maximumShapePointSpacing)
    {
        var builder = new PolylineBuilder();
        Location last = null;
        for (var location : locationSequence())
        {
            if (last != null && last.distanceTo(location).isGreaterThan(maximumShapePointSpacing))
            {
                var segment = new Segment(last, location);
                for (var at = maximumShapePointSpacing; at
                        .isLessThan(segment.length()); at = at.add(maximumShapePointSpacing))
                {
                    builder.add(segment.at(at));
                }
            }
            builder.add(location);
            last = location;
        }
        return builder.build();
    }

    public Iterator<Location> backwardLocations()
    {
        return new BaseIterator<>()
        {
            private int index = size() - 1;

            @Override
            protected Location onNext()
            {
                if (index >= 0)
                {
                    return get(index--);
                }
                return null;
            }
        };
    }

    /**
     * @return The amount of bend of the line. If the line bends left, the angle will be less than zero. If it bends
     * right, it will be greater than zero.
     */
    public Angle bend()
    {
        var bend = 0D;
        Segment previous = null;
        for (var segment : segments())
        {
            if (previous != null)
            {
                var left = previous.turnAngleTo(segment, Chirality.COUNTERCLOCKWISE);
                if (left.isLessThan(Angle._180_DEGREES))
                {
                    bend -= left.asDegrees();
                }
                var right = previous.turnAngleTo(segment, Chirality.CLOCKWISE);
                if (right.isLessThan(Angle._180_DEGREES))
                {
                    bend += right.asDegrees();
                }
            }
            previous = segment;
        }
        return Angle.degrees(bend);
    }

    /**
     * @return Two {@link PolylineSection}s for this polyline bisected
     */
    public List<PolylineSection> bisect()
    {
        // Sections to return
        List<PolylineSection> sections = new ArrayList<>();

        // If this polyline is a segment,
        if (isSegment())
        {
            // return the polyline as one section
            sections.add(section(0, size() - 1));
        }
        else
        {
            // otherwise, find the mid-point
            var midpoint = size() / 2;

            // and add two sections
            sections.add(section(0, midpoint));
            sections.add(section(midpoint, size() - 1));
        }
        return sections;
    }

    /**
     * @return The minimum bounding rectangle of all shape-points
     */
    @Override
    public final Rectangle bounds()
    {
        if (leftInDecimal == Integer.MAX_VALUE)
        {
            expandBounds(locationsInDecimal());
        }
        return Rectangle.fromInts(bottomInDecimal, leftInDecimal, topInDecimal, rightInDecimal);
    }

    /**
     * @return The percentage of this polyline that is within the given distance from that polyline. Areas where the
     * headings of the two poly-lines deviate by more than the given maximumHeadingDeviation are not considered close,
     * nor are areas where the poly-lines are more end-to-end than side-by-side.
     */
    public Percent closeness(Polyline that, Distance maximumDistance,
                             Angle maximumHeadingDeviation)
    {
        // Create polyline snapper
        var snapper = new PolylineSnapper();

        // The amount of this polyline that is close to that polyline
        var close = Distance.ZERO;

        // Go through each segment in this polyline
        for (var segment : segments())
        {
            // Snap the midpoint of this segment to that polyline. NOTE: We don't remember the
            // reason for using the mid-point here, but this algorithm gives good results, so don't
            // change this without a good reason
            var point = segment.midpoint();
            var snap = snapper.snap(that, point);

            // If the snap is close enough and the heading is within tolerance,
            if (snap.distanceToSource().isLessThan(maximumDistance)
                    && snap.segmentHeading().isCloseOrReverseIsClose(segment.heading(), maximumHeadingDeviation))
            {
                // and the snap angle is roughly perpendicular (indicating the segments are not end-to-end)
                if (snap.distanceToSource().isLessThan(Distance.meters(1))
                        || snap.angle().isClose(Angle._90_DEGREES, Angle._45_DEGREES))
                {
                    // then add the segment to the amount of this polyline considered close
                    close = close.add(segment.length());
                }
            }
        }
        return close.percentageOf(length());
    }

    public CompressedPolyline compressed()
    {
        return CompressedPolyline.fromLocationSequence(locationSequence());
    }

    public boolean crosses(Polyline that)
    {
        if (bounds().intersects(that.bounds()))
        {
            for (var thisSegment : segments())
            {
                for (var thatSegment : that.segments())
                {
                    if (!thisSegment.start().equals(thatSegment.start())
                            && !thisSegment.start().equals(thatSegment.end())
                            && !thisSegment.end().equals(thatSegment.start())
                            && !thisSegment.end().equals(thatSegment.end())

                            && thisSegment.intersects(thatSegment))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Distance directDistance()
    {
        if (size() > 0)
        {
            return start().distanceTo(end());
        }
        return null;
    }

    public Distance distanceTo(Location location)
    {
        var snapper = new PolylineSnapper();
        var to = snapper.snap(this, location);
        return length().times(to.offset().asZeroToOne());
    }

    public Set<Location> duplicateLocations()
    {
        Set<Location> visited = new HashSet<>();
        Set<Location> duplicates = new HashSet<>();
        for (var location : locationSequence())
        {
            if (visited.contains(location))
            {
                duplicates.add(location);
            }
            visited.add(location);
        }
        return duplicates;
    }

    /**
     * @return Set of duplicated segments
     */
    public Set<Segment> duplicateSegments()
    {
        Set<Segment> duplicates = new HashSet<>();
        Set<Segment> visited = new HashSet<>();
        for (var segment : segments())
        {
            if (visited.contains(segment) || visited.contains(segment.reversed()))
            {
                duplicates.add(segment);
            }
            else
            {
                visited.add(segment);
            }
        }
        return duplicates;
    }

    public Location end()
    {
        if (size() > 0)
        {
            return get(size() - 1);
        }
        return null;
    }

    @Override
    public final boolean equals(Object object)
    {
        if (object instanceof Polyline)
        {
            var that = (Polyline) object;
            return isEqualTo(that);
        }
        return false;
    }

    public Heading finalHeading()
    {
        var last = lastSegment();
        if (last != null)
        {
            return last.heading();
        }
        return null;
    }

    public Segment firstSegment()
    {
        return new Segment(get(0), get(1));
    }

    @Override
    public Location get(int index)
    {
        return Location.dm7(locationsInDecimal()[index]);
    }

    public boolean has(Location location)
    {
        for (var current : locationSequence())
        {
            if (current.equals(location))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public final int hashCode()
    {
        if (hashCode == null)
        {
            hashCode = asHashCode();
        }
        return hashCode;
    }

    /**
     * Provides a hashcode for very large poly-lines, such as those used in country and state borders, to avoid the
     * expense of hashing every location in the polyline (which happens if this method is not called).
     */
    public void hashCode(int hashCode)
    {
        this.hashCode = hashCode;
    }

    public Heading initialHeading()
    {
        var first = firstSegment();
        if (first != null)
        {
            return first.heading();
        }
        return null;
    }

    public Location intersection(Polyline that)
    {
        for (var segment : that.segments())
        {
            var intersection = intersection(segment);
            if (intersection != null)
            {
                return intersection;
            }
        }
        return null;
    }

    public Location intersection(Segment that)
    {
        for (var segment : segments())
        {
            if (segment.intersects(that))
            {
                return segment.intersection(that);
            }
        }
        return null;
    }

    @Override
    public boolean intersects(Rectangle rectangle)
    {
        if (bounds().intersects(rectangle))
        {
            for (var segment : segments())
            {
                if (segment.intersects(rectangle))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean intersects(Segment that)
    {
        for (var segment : segments())
        {
            if (segment.intersects(that))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if any segment is more than the given tolerance out of line with the initial segment
     */
    public boolean isBent(Distance within, Angle tolerance)
    {
        Heading initial = null;
        var length = Distance.ZERO;
        for (var segment : segments())
        {
            if (initial == null)
            {
                initial = segment.heading();
            }
            else if (segment.heading().difference(initial, Chirality.SMALLEST).isGreaterThan(tolerance))
            {
                return true;
            }
            length = length.add(segment.approximateLength());
            if (length.isGreaterThan(within))
            {
                break;
            }
        }
        return false;
    }

    public boolean isConnectedTo(Polyline that)
    {
        return end().equals(that.start()) || start().equals(that.end());
    }

    @Override
    public final boolean isEmpty()
    {
        return size() == 0;
    }

    public boolean isLoop()
    {
        return size() > 2 && start().equals(end());
    }

    public boolean isSegment()
    {
        return size() == 2;
    }

    public boolean isSelfIntersecting()
    {
        Set<Location> visited = new HashSet<>();
        for (var location : locationSequence())
        {
            if (visited.contains(location))
            {
                return true;
            }
            visited.add(location);
        }
        return false;
    }

    public boolean isStraight(Distance within, Angle tolerance)
    {
        return !isBent(within, tolerance);
    }

    public Segment lastSegment()
    {
        ensure(size() >= 2);
        return new Segment(get(size() - 2), get(size() - 1));
    }

    public Distance length()
    {
        if (lengthInMillimeters == -1)
        {
            lengthInMillimeters = computeLengthInMillimeters();
        }
        return Distance.millimeters(lengthInMillimeters);
    }

    @Override
    public Iterable<Location> locationSequence()
    {
        return this;
    }

    public List<Location> locations()
    {
        var referenced = locations == null ? null : locations.get();
        if (referenced == null)
        {
            referenced = new ArrayList<>();
            var locationsInDecimal = locationsInDecimal();
            for (var location : locationsInDecimal)
            {
                referenced.add(Location.dm7(location));
            }
        }
        if (locations == null)
        {
            locations = new SoftReference<>(referenced);
        }
        return referenced;
    }

    /**
     * @return Any loops this polyline forms (or null if it doesn't)
     */
    public List<Loop> loops()
    {
        Map<Location, Integer> locations = new HashMap<>();
        var index = 0;
        Location last = null;
        List<Loop> loops = new ArrayList<>();
        for (var location : locationSequence())
        {
            var startIndex = locations.get(location);
            if (startIndex != null && !location.equals(last))
            {
                var loop = new Loop();
                loop.startIndex = startIndex;
                loop.endIndex = index;
                loop.loopAt = location;
                loops.add(loop);
            }
            locations.put(location, index);
            last = location;
            index++;
        }
        return loops;
    }

    public Location middle()
    {
        return get(size() / 2);
    }

    public Segment middleSegment()
    {
        var first = size() / 2;
        var second = first + 1;
        if (first == 0)
        {
            second = 1;
        }
        else if (first == size() - 1)
        {
            second = size() - 2;
        }
        return new Segment(get(first), get(second));
    }

    public Location midpoint()
    {
        var length = length();
        var midpoint = length.dividedBy(Count._2);
        var from = Distance.ZERO;
        for (var segment : segments())
        {
            var to = from.add(segment.length());
            if (to.isGreaterThanOrEqualTo(midpoint))
            {
                return segment.at(midpoint.minus(from));
            }
            else
            {
                from = to;
            }
        }
        ensure(false);
        return null;
    }

    public Polyline moved(Heading heading, Distance offset)
    {
        var builder = new PolylineBuilder();
        for (var location : locationSequence())
        {
            builder.add(location.moved(heading, offset));
        }
        return builder.build();
    }

    public Segment nextToLastSegment()
    {
        if (!isSegment())
        {
            return new Segment(get(size() - 3), get(size() - 2));
        }
        return null;
    }

    /**
     * @return Set of overlapping (common) segments with the given polyline
     */
    public Set<Segment> overlapping(Polyline that)
    {
        Set<Segment> overlaps = new HashSet<>();
        Set<Segment> visited = new HashSet<>();
        for (var segment : segments())
        {
            visited.add(segment);
            visited.add(segment.reversed());
        }
        for (var segment : that.segments())
        {
            if (visited.contains(segment) || visited.contains(segment.reversed()))
            {
                if (!segment.isPoint())
                {
                    overlaps.add(segment);
                }
            }
            else
            {
                visited.add(segment);
            }
        }
        return overlaps;
    }

    /**
     * @return True if more than one point is shared between the two poly-lines
     */
    public boolean overlaps(Polyline that)
    {
        var overlap = 0;
        var locations = size() < 4 ? that.locations() : that.asLocationSet();
        for (var location : locationSequence())
        {
            if (locations.contains(location))
            {
                overlap++;
                if (overlap > 1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a parallel Polyline at the left/right with distance of offset
     */
    public Polyline parallel(Heading heading, Distance offset)
    {
        List<Location> locations = new ArrayList<>();

        for (var location : locationSequence())
        {
            locations.add(location.moved(heading, offset));
        }

        return fromLocations(locations);
    }

    public Polyline reversed()
    {
        var reversed = locationsInDecimal();
        Arrays.reverse(reversed);
        return new Polyline(reversed);
    }

    public Segment secondSegment()
    {
        if (!isSegment())
        {
            return new Segment(get(1), get(2));
        }
        return null;
    }

    /**
     * @return The section of this polyline between the start and end distances
     */
    public Polyline section(Distance start, Distance end)
    {
        var builder = new PolylineBuilder();
        var distance = Distance.ZERO;
        for (var segment : segments())
        {
            var length = segment.approximateLength();
            if (length.isGreaterThan(Distance.ZERO))
            {
                if (builder.isEmpty())
                {
                    if (distance.add(length).isGreaterThanOrEqualTo(start))
                    {
                        builder.add(segment.at(start.minus(distance)));
                        if (distance.add(length).isGreaterThan(end))
                        {
                            builder.add(segment.at(end.minus(distance)));
                            break;
                        }
                        else
                        {
                            builder.add(segment.end());
                        }
                    }
                }
                else
                {
                    if (distance.add(length).isGreaterThan(end))
                    {
                        builder.add(segment.at(end.minus(distance)));
                        break;
                    }
                    else
                    {
                        builder.add(segment.end());
                        if (distance.add(length).equals(end))
                        {
                            break;
                        }
                    }
                }
                distance = distance.add(length);
            }
        }
        return builder.build();
    }

    /**
     * @return The section of this polyline between the start and end index (inclusive)
     */
    public PolylineSection section(int startIndex, int endIndex)
    {
        return new PolylineSection(this, startIndex, endIndex);
    }

    /**
     * @return The section of this polyline between the start and end locations (inclusive) in polyline order.
     */
    public Polyline section(Location start, Location end)
    {
        var builder = new PolylineBuilder();
        var started = false;
        for (var location : locationSequence())
        {
            if (!started && location.equals(start))
            {
                started = true;
            }
            if (started)
            {
                builder.add(location);
                if (builder.size() > 1 && location.equals(end))
                {
                    break;
                }
            }
        }
        return builder.build();
    }

    /**
     * @return This polyline sectioned into pieces no longer than maximumLength
     */
    public List<PolylineSection> sections(Distance maximumLength)
    {
        // The sections to return
        List<PolylineSection> sections = new ArrayList<>();

        // Builder because we may need to create new locations in the shape
        var builder = new PolylineBuilder();

        // The total length so far
        var total = Distance.ZERO;

        // Go through each segment in this polyline
        var startIndex = 0;
        for (var segment : segments())
        {
            // If the line has no start point yet,
            if (builder.isEmpty())
            {
                // add the segment start
                builder.add(segment.start());
            }

            // Get the segment length
            var length = segment.approximateLength();

            // If the segment pushes us beyond the maximum length
            if (total.add(length).isGreaterThan(maximumLength))
            {
                // the end index is the next location
                var endIndex = startIndex + builder.size();

                // add the final location where we reach the maximum length
                var cutAt = segment.at(maximumLength.minus(total));
                builder.add(cutAt);

                // and add this completed section to the list.
                var line = builder.build();
                sections.add(new PolylineSection(line, startIndex, endIndex));

                // Then, create a new builder with the remainder of the segment we cut
                builder = new PolylineBuilder();
                builder.add(cutAt);
                builder.add(segment.end());

                // and reset our total length.
                total = Distance.ZERO;

                // The start index for the next section is the end index for this one
                startIndex = endIndex;
            }
            else
            {
                // otherwise, add the segment end
                builder.add(segment.end());

                // and increase the total length
                total = total.add(length);
            }
        }

        // If there's a section left over to add,
        if (!builder.isEmpty())
        {
            // then add that final section
            sections.add(new PolylineSection(builder.build(), startIndex, size() - 1));
        }

        return sections;
    }

    public Count segmentCount()
    {
        return Count.count(segmentCountAsInteger());
    }

    public int segmentCountAsInteger()
    {
        return size() - 1;
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
                return segmentCountAsInteger();
            }
        };
    }

    public Location selfIntersection()
    {
        var i = 0;
        for (var a : segments())
        {
            var j = 0;
            for (var b : segments())
            {
                if (Math.abs(j - i) > 1)
                {
                    if (a.intersects(b))
                    {
                        return a.intersection(b);
                    }
                }
                j++;
            }
            i++;
        }
        return null;
    }

    /**
     * @return The sharpest turn angle (difference between headings of two consecutive segments) in this polyline
     */
    public SegmentPair sharpestTurnAngle()
    {
        Segment previous = null;
        SegmentPair sharpest = null;
        for (var segment : segments())
        {
            if (previous != null)
            {
                var pair = new SegmentPair(previous, segment);
                if (sharpest == null || pair.angle().isGreaterThan(sharpest.angle()))
                {
                    sharpest = pair;
                }
            }
            previous = segment;
        }
        return sharpest;
    }

    /**
     * Recursively simplify this {@link Polyline} using the Ramer Douglas Peucker algorithm
     *
     * @param tolerance The distance tolerance for the simplification
     * @return The simplified {@link Polyline}
     * @see <a href= "http://en.wikipedia.org/wiki/Ramer%E2%80%93Douglas%E2%80%93Peucker_algorithm">Ramer Douglas
     * Peucker</a>
     */
    @SuppressWarnings("SpellCheckingInspection")
    public Polyline simplified(Distance tolerance)
    {
        return new PolylineSimplifier().simplify(this, tolerance);
    }

    @Override
    public int size()
    {
        return locationsInDecimal().length;
    }

    public Polyline smooth(Angle tolerance)
    {
        var smoothed = smoothOnce(tolerance);
        for (var iteration = 0; iteration < 2; iteration++)
        {
            var reSmoothed = smoothed.smoothOnce(tolerance);
            if (reSmoothed.size() == smoothed.size())
            {
                break;
            }
            smoothed = reSmoothed;
        }
        return smoothed;
    }

    /**
     * @return The section of this polyline between the origin and destination points
     */
    public Polyline snapAndSection(Location origin, Location destination)
    {
        var snapper = new PolylineSnapper();
        var start = snapper.snap(this, origin);
        var end = snapper.snap(this, destination);
        var builder = new PolylineBuilder();
        builder.add(start);
        for (var index = start.polylineIndex() + 1; index <= end.polylineIndex(); index++)
        {
            builder.add(get(index));
        }
        builder.add(end);
        return builder.build();
    }

    public Location start()
    {
        if (size() > 0)
        {
            return get(0);
        }
        return null;
    }

    @Override
    public String toString()
    {
        return new ObjectList<>(GeographyLimits.LOCATIONS_PER_POLYLINE).appendAll(locationSequence()).join(":");
    }

    public List<PolylineSection> trisect()
    {
        // Sections to return
        List<PolylineSection> sections = new ArrayList<>();

        // If there are at least three segments,
        if (segmentCount().isGreaterThanOrEqualTo(Count._3))
        {
            // find the two split points
            var first = size() / 3;
            var second = first * 2;

            // and then add the three sections
            sections.add(section(0, first));
            sections.add(section(first, second));
            sections.add(section(second, size() - 1));
        }
        else
        {
            // add this polyline as the only section
            sections.add(section(0, size() - 1));
        }

        return sections;
    }

    public final Angle turnAngleTo(Polyline that)
    {
        return finalHeading().difference(that.initialHeading(), Chirality.CLOCKWISE);
    }

    public final Angle turnAngleTo(Polyline that, Chirality chirality)
    {
        return finalHeading().difference(that.initialHeading(), chirality);
    }

    public Iterable<LocatedHeading> vectors()
    {
        return Iterables.iterable(() -> new NextValue<>()
        {
            boolean first = true;

            boolean last;

            boolean done;

            final Iterator<Segment> segments = segments().iterator();

            Segment segment;

            @Override
            public LocatedHeading next()
            {
                if (isEmpty() || done)
                {
                    return null;
                }
                if (first)
                {
                    segment = segments.next();
                    first = false;
                    return new LocatedHeading(segment.start(), segment.heading());
                }
                if (last)
                {
                    done = true;
                    return new LocatedHeading(segment.end(), segment.heading());
                }
                if (segment != null)
                {
                    var next = new LocatedHeading(segment.midpoint(), segment.heading());
                    if (segments.hasNext())
                    {
                        segment = segments.next();
                    }
                    else
                    {
                        last = true;
                    }
                    return next;
                }
                return null;
            }
        });
    }

    public Polyline withAppended(Location location)
    {
        var builder = new PolylineBuilder();
        builder.addAll(locationSequence());
        builder.add(location);
        return builder.build();
    }

    public Polyline withFirstReplaced(Location location)
    {
        var locations = java.util.Arrays.copyOf(locationsInDecimal(), size());
        locations[0] = expandBounds(location.asDm7Long());
        return new Polyline(locations);
    }

    public Polyline withLastReplaced(Location location)
    {
        var locations = java.util.Arrays.copyOf(locationsInDecimal(), size());
        locations[size() - 1] = expandBounds(location.asLong());
        return new Polyline(locations);
    }

    public Polyline withoutDuplicates()
    {
        List<Location> withoutDuplicates = new ArrayList<>();
        Location last = null;
        for (var location : locationSequence())
        {
            // Avoid putting two duplicates in a row.
            if (last == null || !last.equals(location))
            {
                withoutDuplicates.add(location);
            }
            last = location;
        }
        return withoutDuplicates.size() == size() ? this : new Polyline(withoutDuplicates);
    }

    public Polyline withoutFirst()
    {
        return new Polyline(java.util.Arrays.copyOfRange(locationsInDecimal(), 1, size()));
    }

    public Polyline withoutLast()
    {
        return new Polyline(java.util.Arrays.copyOfRange(locationsInDecimal(), 0, size() - 1));
    }

    protected long expandBounds(long location)
    {
        var latitude = Location.latitude(location);
        topInDecimal = Math.max(topInDecimal, latitude);
        bottomInDecimal = Math.min(bottomInDecimal, latitude);

        var longitude = Location.longitude(location);
        leftInDecimal = Math.min(leftInDecimal, longitude);
        rightInDecimal = Math.max(rightInDecimal, longitude);

        return location;
    }

    protected long[] locationsInDecimal()
    {
        assert locationsInDecimal != null;
        return locationsInDecimal;
    }

    /**
     * @return The section of this polyline between the start and end index (inclusive)
     */
    Polyline shape(int startIndex, int endIndex)
    {
        return new Polyline(java.util.Arrays.copyOfRange(locationsInDecimal(), startIndex, endIndex + 1));
    }

    private long computeLengthInMillimeters()
    {
        var lengthInMillimeters = 0L;
        var previousLatitudeInDm7 = Long.MIN_VALUE;
        var previousLongitudeInDm7 = Long.MIN_VALUE;
        var index = 0;
        for (var location : locationsInDecimal())
        {
            var latitudeInDm7 = Location.latitude(location);
            var longitudeInDm7 = Location.longitude(location);
            if (index++ > 0)
            {
                lengthInMillimeters += Location.equirectangularDistanceBetweenInMillimeters(
                        latitudeInDm7, longitudeInDm7, previousLatitudeInDm7, previousLongitudeInDm7);
            }
            previousLatitudeInDm7 = latitudeInDm7;
            previousLongitudeInDm7 = longitudeInDm7;
        }
        return lengthInMillimeters;
    }

    private long[] expandBounds(long[] locations)
    {
        ensure(locations.length >= 2);
        for (var location : locations)
        {
            expandBounds(location);
        }
        return locations;
    }

    private int minimum(Distance[] distances)
    {
        var minimum = Distance.MAXIMUM;
        var index = -1;
        for (var i = 0; i < distances.length; i++)
        {
            if (distances[i].isLessThan(minimum))
            {
                index = i;
            }
        }
        return index;
    }

    private Segment segment(int index)
    {
        if (index < size())
        {
            return new Segment(get(index), get(index + 1));
        }
        throw new IndexOutOfBoundsException("No segment at index " + index);
    }

    private Polyline smoothOnce(Angle tolerance)
    {
        var builder = new PolylineBuilder();
        builder.add(get(0));
        for (var i = 1; i < size() - 1; i++)
        {
            var a = new Segment(get(i - 1), get(i));
            var b = new Segment(get(i), get(i + 1));
            if (a.heading().isClose(b.heading(), tolerance) || a.length().isLessThan(Distance.meters(20))
                    || b.length().isLessThan(Distance.meters(20)))
            {
                builder.add(get(i));
            }
            else
            {
                if (a.length().difference(b.length()).isLessThan(Distance.meters(20)))
                {
                    builder.add(a.midpoint());
                    builder.add(b.midpoint());
                }
                else
                {
                    if (a.length().isGreaterThan(b.length()))
                    {
                        var start = a.at(a.length().minus(b.length()));
                        builder.add(start);
                        builder.add(new Segment(start, a.end()).midpoint());
                        builder.add(b.midpoint());
                    }
                    else
                    {
                        var end = b.at(a.length());
                        builder.add(a.midpoint());
                        builder.add(new Segment(b.start(), end).midpoint());
                        builder.add(end);
                    }
                }
            }
        }
        builder.add(get(size() - 1));
        return builder.build();
    }
}
