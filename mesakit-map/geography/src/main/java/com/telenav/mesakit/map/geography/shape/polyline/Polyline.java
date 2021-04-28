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

import com.telenav.kivakit.core.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.interfaces.collection.Indexable;
import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.core.kernel.language.iteration.Iterables;
import com.telenav.kivakit.core.kernel.language.iteration.Next;
import com.telenav.kivakit.core.kernel.language.strings.Split;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.LocatedHeading;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.MapGeographyLimits;
import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramPolyline;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

/**
 * A sequence of two or more locations that are connected, leading from a {@link #start()} to an {@link #end()}
 * location. A polyline is {@link Iterable} so it can be used in an advanced for loop:
 * <pre>
 *     for (var location : polyline) { ... }
 * </pre>
 * Polylines also have a bounding rectangle retrieved with {@link Bounded#bounds()}. It can be determined if the
 * polyline intersects a rectangle or segment with {@link Intersectable#intersects(Rectangle)} and {@link
 * #intersects(Segment)}, and whether it intersects itself with {@link #selfIntersection()}. The point of intersection
 * with another polyline or with a segment can be determined with {@link #intersection(Polyline)} and {@link
 * #intersection(Segment)}. Whether two polylines cross each other can be determined with {@link #crosses(Polyline)}.
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
public class Polyline implements Indexable<Location>, Bounded, Intersectable, LocationSequence
{
    public static final Distance DEFAULT_MAXIMUM_SHAPE_POINT_SPACING = Distance.meters(15);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static Polyline fromLocations(final long start, final long end)
    {
        return new Polyline(new long[] { start, end });
    }

    /**
     * Construct from a convenient argument list of locations
     */
    public static Polyline fromLocations(final Location one, final Location two, final Location... more)
    {
        final var builder = new PolylineBuilder();
        builder.add(one);
        builder.add(two);
        for (final var location : more)
        {
            builder.add(location);
        }
        return builder.build();
    }

    public static Polyline fromLocations(final Iterable<Location> locations)
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

    public static Polyline fromLocations(final Iterator<Location> locations)
    {
        return new PolylineBuilder().addAll(locations).build();
    }

    public static Polyline fromLongArray(final LongArray locations)
    {
        final var builder = new PolylineBuilder();
        for (var i = 0; i < locations.size(); i++)
        {
            builder.add(Location.dm7(locations.get(i)));
        }
        return builder.build();
    }

    public static Polyline fromLongs(final long[] locations)
    {
        return new Polyline(locations);
    }

    public static Polyline parse(final String value)
    {
        if (!Strings.isEmpty(value))
        {
            final var builder = new PolylineBuilder();
            final var converter = new Location.DegreesConverter(LOGGER);
            for (final var location : Split.split(value, ':'))
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

        public Converter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
            locationConverter = new Location.DegreesConverter(listener, separators.child());
            allowEmpty(true);
            allowNull(true);
        }

        @Override
        protected String onConvertNullToString()
        {
            return "";
        }

        @Override
        protected Polyline onConvertToObject(final String value)
        {
            if (!Strings.isEmpty(value))
            {
                final var builder = new PolylineBuilder();
                for (final var location : Split.split(value, separators.current()))
                {
                    builder.add(locationConverter.convert(location));
                }
                return builder.build();
            }
            return null;
        }

        @Override
        protected String onConvertToString(final Polyline value)
        {
            final var locations = new StringList(MapGeographyLimits.LOCATIONS_PER_POLYLINE);
            for (final var location : value.locationSequence())
            {
                locations.add(locationConverter.toString(location));
            }
            return locations.join(separators.current());
        }
    }

    /**
     * {@link Polyline} decoding and encoding using the Google method
     *
     * @author matthieun
     */
    public static class GoogleEncodingConverter extends BaseStringConverter<Polyline>
    {
        public GoogleEncodingConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Polyline onConvertToObject(final String value)
        {
            return decodePolyline(value);
        }

        @Override
        protected String onConvertToString(final Polyline value)
        {
            return createPolyline(value);
        }

        /**
         * From <a href="http://statsciolist.blogspot.com/2013/05/java-google-maps-polyline-encoding.html />
         *
         * @param line The {@link Polyline} to encode
         * @return The encoded polyline
         */
        private String createPolyline(final Polyline line)
        {
            var oldlat = 0D;
            var oldlon = 0D;
            final var nb = new StringBuilder();
            for (final var temp : line.locationSequence())
            {
                final var p1 = temp.latitude().asDegrees();
                final var p2 = temp.longitude().asDegrees();

                if (Math.abs(p1 - oldlat) >= 0.00001)
                {
                    final var temp2 = encodePolyline(p1 - oldlat);
                    nb.append(temp2);
                }
                else
                {
                    nb.append("?");
                }
                if (Math.abs(p2 - oldlon) >= 0.00001)
                {
                    final var temp2 = encodePolyline(p2 - oldlon);
                    nb.append(temp2);
                }
                else
                {
                    nb.append("?");
                }
                oldlat = p1;
                oldlon = p2;
            }

            var temp = nb.toString();

            // Ensure temp for "*\*" pattern
            final var pattern = Pattern.compile("\".*\\.*\"");
            final var matcher = pattern.matcher(temp);

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
        private Polyline decodePolyline(final String encoded)
        {
            final var builder = new PolylineBuilder();
            var index = 0;
            final var encodedLength = encoded.length();
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
                final var deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
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
                final var deltaLon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lon += deltaLon;

                final var latitude = Latitude.dm5(lat);
                final var longitude = Longitude.dm5(lon);
                final var location = new Location(latitude, longitude);
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
        private String encodePolyline(final double angleInDegrees)
        {
            // Google's procedure for encoding polyline data
            // This doesn't cater for backslashes in string literals i.e. the character sequence "\"
            // which should be returned as "\\". Function createPolyline will do this.

            final String polyline;
            final var signNum = (int) Math.signum(angleInDegrees);

            var b = (int) Math.round(angleInDegrees * 1e5);
            final List<Integer> ab = new ArrayList<>();

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
            final var nc4 = new StringBuilder();
            for (var i = 0; i < ab.size() - 1; i++)
            {
                // Or with 0x20 and add 63
                final var c = (char) ((ab.get(i) | 0x20) + 63);
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
        private final boolean modified;

        private final Location location;

        public Intersection(final Location location, final boolean modified)
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

        public LongArrayConverter(final Listener listener, final Separators separators)
        {
            super(listener);
            longArrayConverter = new LongArray.Converter(listener, separators);
            allowEmpty(true);
            allowNull(true);
        }

        @Override
        protected String onConvertNullToString()
        {
            return "";
        }

        @Override
        protected Polyline onConvertToObject(final String value)
        {
            if (!Strings.isEmpty(value))
            {
                final var converted = longArrayConverter.convert(value);
                return converted == null ? null : fromLongArray(converted);
            }
            return null;
        }

        @Override
        protected String onConvertToString(final Polyline value)
        {
            if (!value.isEmpty())
            {
                return longArrayConverter.toString(value.asLongArray());
            }
            return "";
        }
    }

    public class Loop
    {
        public Location loopAt;

        private int startIndex;

        private int endIndex;

        private Count intersections;

        public PolylineSection head()
        {
            return startIndex > 0 ? section(0, startIndex) : null;
        }

        public void intersections(final Count intersections)
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

    /**
     * Soft-referenced list of locations for convenience and efficiency
     */
    private transient SoftReference<List<Location>> locations;

    /**
     * The actual location data
     */
    private long[] locationsInDecimal;

    /**
     * Length in millimeters of the polyline
     */
    private long lengthInMillimeters = -1;

    // The bounding rectangle
    private int topInDecimal = Integer.MIN_VALUE;

    private int leftInDecimal = Integer.MAX_VALUE;

    private int bottomInDecimal = Integer.MAX_VALUE;

    private int rightInDecimal = Integer.MIN_VALUE;

    private Integer hashCode;

    public Polyline(final List<Location> locations)
    {
        ensure(locations.size() >= 2);

        this.locations = new SoftReference<>(locations);
        locationsInDecimal = new long[locations.size()];
        var i = 0;
        for (final var location : locations)
        {
            locationsInDecimal[i++] = expandBounds(location.asDm7Long());
        }
    }

    public Polyline(final long[] locationsInDecimal)
    {
        assert locationsInDecimal.length >= 2;

        locations = new SoftReference<>(null);
        this.locationsInDecimal = expandBounds(locationsInDecimal);
    }

    protected Polyline()
    {
    }

    public Polyline append(final Polyline that)
    {
        final var builder = new PolylineBuilder();
        builder.addAllUnique(locationSequence());
        builder.addAllUnique(that.locationSequence());
        return builder.build();
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
    Iterator<Location> asIterator(final Matcher<Location> matcher)
    {
        return new BaseIterator<>()
        {
            int index = 0;

            @Override
            protected Location onNext()
            {
                while (index < size())
                {
                    final var location = get(index++);
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
        final Set<Location> locations = new HashSet<>();
        for (final var location : locationSequence())
        {
            locations.add(location);
        }
        return locations;
    }

    public LongArray asLongArray()
    {
        final var array = new LongArray("asLongArray");
        array.initialSize(size());
        array.initialize();

        for (final var location : locationSequence())
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
            for (final var segment : segments())
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
    public Location at(final Percent parameter)
    {
        return at(length().times(parameter.asUnitValue()));
    }

    /**
     * @return This polyline with the given polyline attached at the closest end
     */
    public Polyline attach(final Polyline that)
    {
        final var endToStart = end().distanceTo(that.start());
        final var endToEnd = end().distanceTo(that.end());
        final var startToEnd = start().distanceTo(that.end());
        final var startToStart = start().distanceTo(that.start());
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
    public Polyline augmented(final Distance maximumShapePointSpacing)
    {
        final var builder = new PolylineBuilder();
        Location last = null;
        for (final var location : locationSequence())
        {
            if (last != null && last.distanceTo(location).isGreaterThan(maximumShapePointSpacing))
            {
                final var segment = new Segment(last, location);
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
        for (final var segment : segments())
        {
            if (previous != null)
            {
                final var left = previous.turnAngleTo(segment, Chirality.COUNTERCLOCKWISE);
                if (left.isLessThan(Angle._180_DEGREES))
                {
                    bend -= left.asDegrees();
                }
                final var right = previous.turnAngleTo(segment, Chirality.CLOCKWISE);
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
        final List<PolylineSection> sections = new ArrayList<>();

        // If this polyline is a segment,
        if (isSegment())
        {
            // return the polyline as one section
            sections.add(section(0, size() - 1));
        }
        else
        {
            // otherwise, find the mid point
            final var midpoint = size() / 2;

            // and add two sections
            sections.add(section(0, midpoint));
            sections.add(section(midpoint, size() - 1));
        }
        return sections;
    }

    /**
     * @return The minimum bounding rectangle of all shapepoints
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
     * headings of the two polylines deviate by more than the given maximumHeadingDeviation are not considered close,
     * nor are areas where the polylines are more end-to-end than side-by-side.
     */
    public Percent closeness(final Polyline that, final Distance maximumDistance,
                             final Angle maximumHeadingDeviation)
    {
        // Create polyline snapper
        final var snapper = new PolylineSnapper();

        // The amount of this polyline that is close to that polyline
        var close = Distance.ZERO;

        // Go through each segment in this polyline
        for (final var segment : segments())
        {
            // Snap the midpoint of this segment to that polyline. NOTE: We don't remember the
            // reason for using the mid-point here, but this algorithm gives good results, so don't
            // change this without a good reason
            final var point = segment.midpoint();
            final var snap = snapper.snap(that, point);

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

    public boolean crosses(final Polyline that)
    {
        if (bounds().intersects(that.bounds()))
        {
            for (final var thisSegment : segments())
            {
                for (final var thatSegment : that.segments())
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

    public Distance distanceTo(final Location location)
    {
        final var snapper = new PolylineSnapper();
        final var to = snapper.snap(this, location);
        return length().times(to.offset().asZeroToOne());
    }

    public Set<Location> duplicateLocations()
    {
        final Set<Location> visited = new HashSet<>();
        final Set<Location> duplicates = new HashSet<>();
        for (final var location : locationSequence())
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
        final Set<Segment> duplicates = new HashSet<>();
        final Set<Segment> visited = new HashSet<>();
        for (final var segment : segments())
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
    public final boolean equals(final Object object)
    {
        if (object instanceof Polyline)
        {
            final var that = (Polyline) object;
            if (hashCode != null)
            {
                return hashCode.equals(that.hashCode);
            }
            else
            {
                return isEqualTo(that);
            }
        }
        return false;
    }

    public Heading finalHeading()
    {
        final var last = lastSegment();
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
    public Location get(final int index)
    {
        return Location.dm7(locationsInDecimal()[index]);
    }

    public boolean has(final Location location)
    {
        for (final var current : locationSequence())
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
        return hashCode != null ? hashCode : asHashCode();
    }

    /**
     * Provides a hashcode for very large polylines, such as those used in country and state borders, to avoid the
     * expense of hashing every location in the polyline (which happens if this method is not called).
     */
    public void hashCode(final int hashCode)
    {
        this.hashCode = hashCode;
    }

    public Heading initialHeading()
    {
        final var first = firstSegment();
        if (first != null)
        {
            return first.heading();
        }
        return null;
    }

    public Location intersection(final Polyline that)
    {
        for (final var segment : that.segments())
        {
            final var intersection = intersection(segment);
            if (intersection != null)
            {
                return intersection;
            }
        }
        return null;
    }

    public Location intersection(final Segment that)
    {
        for (final var segment : segments())
        {
            if (segment.intersects(that))
            {
                return segment.intersection(that);
            }
        }
        return null;
    }

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        if (bounds().intersects(rectangle))
        {
            for (final var segment : segments())
            {
                if (segment.intersects(rectangle))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean intersects(final Segment that)
    {
        for (final var segment : segments())
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
    public boolean isBent(final Distance within, final Angle tolerance)
    {
        Heading initial = null;
        var length = Distance.ZERO;
        for (final var segment : segments())
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

    public boolean isConnectedTo(final Polyline that)
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
        final Set<Location> visited = new HashSet<>();
        for (final var location : locationSequence())
        {
            if (visited.contains(location))
            {
                return true;
            }
            visited.add(location);
        }
        return false;
    }

    public boolean isStraight(final Distance within, final Angle tolerance)
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
        return asIterable();
    }

    public List<Location> locations()
    {
        var referenced = locations == null ? null : locations.get();
        if (referenced == null)
        {
            referenced = new ArrayList<>();
            final var locationsInDecimal = locationsInDecimal();
            for (final var location : locationsInDecimal)
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
        final Map<Location, Integer> locations = new HashMap<>();
        var index = 0;
        Location last = null;
        final List<Loop> loops = new ArrayList<>();
        for (final var location : locationSequence())
        {
            final var startIndex = locations.get(location);
            if (startIndex != null && !location.equals(last))
            {
                final var loop = new Loop();
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
        final var first = size() / 2;
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
        final var length = length();
        final var midpoint = length.dividedBy(Count._2);
        var from = Distance.ZERO;
        for (final var segment : segments())
        {
            final var to = from.add(segment.length());
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

    public Polyline moved(final Heading heading, final Distance offset)
    {
        final var builder = new PolylineBuilder();
        for (final var location : locationSequence())
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
    public Set<Segment> overlapping(final Polyline that)
    {
        final Set<Segment> overlaps = new HashSet<>();
        final Set<Segment> visited = new HashSet<>();
        for (final var segment : segments())
        {
            visited.add(segment);
            visited.add(segment.reversed());
        }
        for (final var segment : that.segments())
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
     * @return True if more than one point is shared between the two polylines
     */
    public boolean overlaps(final Polyline that)
    {
        var overlap = 0;
        final var locations = size() < 4 ? that.locations() : that.asLocationSet();
        for (final var location : locationSequence())
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
    public Polyline parallel(final Heading heading, final Distance offset)
    {
        final List<Location> locations = new ArrayList<>();

        for (final var location : locationSequence())
        {
            locations.add(location.moved(heading, offset));
        }

        return fromLocations(locations);
    }

    public Polyline reversed()
    {
        final var reversed = locationsInDecimal();
        com.telenav.kivakit.core.kernel.language.primitives.Arrays.reverse(reversed);
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
    public Polyline section(final Distance start, final Distance end)
    {
        final var builder = new PolylineBuilder();
        var distance = Distance.ZERO;
        for (final var segment : segments())
        {
            final var length = segment.approximateLength();
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
    public PolylineSection section(final int startIndex, final int endIndex)
    {
        return new PolylineSection(this, startIndex, endIndex);
    }

    /**
     * @return The section of this polyline between the start and end locations (inclusive) in polyline order.
     */
    public Polyline section(final Location start, final Location end)
    {
        final var builder = new PolylineBuilder();
        var started = false;
        for (final var location : locationSequence())
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
    public List<PolylineSection> sections(final Distance maximumLength)
    {
        // The sections to return
        final List<PolylineSection> sections = new ArrayList<>();

        // Builder because we may need to create new locations in the shape
        var builder = new PolylineBuilder();

        // The total length so far
        var total = Distance.ZERO;

        // Go through each segment in this polyline
        var startIndex = 0;
        for (final var segment : segments())
        {
            // If the line has no start point yet,
            if (builder.isEmpty())
            {
                // add the segment start
                builder.add(segment.start());
            }

            // Get the segment length
            final var length = segment.approximateLength();

            // If the segment pushes us beyond the maximum length
            if (total.add(length).isGreaterThan(maximumLength))
            {
                // the end index is the next location
                final var endIndex = startIndex + builder.size();

                // add the final location where we reach the maximum length
                final var cutAt = segment.at(maximumLength.minus(total));
                builder.add(cutAt);

                // and add this completed section to the list.
                final var line = builder.build();
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
            public Segment get(final int index)
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
        for (final var a : segments())
        {
            var j = 0;
            for (final var b : segments())
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
        for (final var segment : segments())
        {
            if (previous != null)
            {
                final var pair = new SegmentPair(previous, segment);
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
    public Polyline simplified(final Distance tolerance)
    {
        return new PolylineSimplifier().simplify(this, tolerance);
    }

    @Override
    public int size()
    {
        return locationsInDecimal().length;
    }

    public Polyline smooth(final Angle tolerance)
    {
        var smoothed = smoothOnce(tolerance);
        for (var iteration = 0; iteration < 2; iteration++)
        {
            final var resmoothed = smoothed.smoothOnce(tolerance);
            if (resmoothed.size() == smoothed.size())
            {
                break;
            }
            smoothed = resmoothed;
        }
        return smoothed;
    }

    /**
     * @return The section of this polyline between the origin and destination points
     */
    public Polyline snapAndSection(final Location origin, final Location destination)
    {
        final var snapper = new PolylineSnapper();
        final var start = snapper.snap(this, origin);
        final var end = snapper.snap(this, destination);
        final var builder = new PolylineBuilder();
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
        return new ObjectList<>(MapGeographyLimits.LOCATIONS_PER_POLYLINE).appendAll(locationSequence()).join(":");
    }

    public List<PolylineSection> trisect()
    {
        // Sections to return
        final List<PolylineSection> sections = new ArrayList<>();

        // If there are at least three segments,
        if (segmentCount().isGreaterThanOrEqualTo(Count._3))
        {
            // find the two split points
            final var first = size() / 3;
            final var second = first * 2;

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

    public final Angle turnAngleTo(final Polyline that)
    {
        return finalHeading().difference(that.initialHeading(), Chirality.CLOCKWISE);
    }

    public final Angle turnAngleTo(final Polyline that, final Chirality chirality)
    {
        return finalHeading().difference(that.initialHeading(), chirality);
    }

    public Iterable<LocatedHeading> vectors()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            boolean first = true;

            boolean last;

            boolean done;

            final Iterator<Segment> segments = segments().iterator();

            Segment segment;

            @Override
            public LocatedHeading onNext()
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
                    final var next = new LocatedHeading(segment.midpoint(), segment.heading());
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

    public Polyline withAppended(final Location location)
    {
        final var builder = new PolylineBuilder();
        builder.addAll(locationSequence());
        builder.add(location);
        return builder.build();
    }

    public Polyline withFirstReplaced(final Location location)
    {
        final var locations = Arrays.copyOf(locationsInDecimal(), size());
        locations[0] = expandBounds(location.asDm7Long());
        return new Polyline(locations);
    }

    public Polyline withLastReplaced(final Location location)
    {
        final var locations = Arrays.copyOf(locationsInDecimal(), size());
        locations[size() - 1] = expandBounds(location.asLong());
        return new Polyline(locations);
    }

    public Polyline withoutDuplicates()
    {
        final List<Location> deduplicated = new ArrayList<>();
        Location last = null;
        for (final var location : locationSequence())
        {
            // Avoid putting two duplicates in a row.
            if (last == null || !last.equals(location))
            {
                deduplicated.add(location);
            }
            last = location;
        }
        return deduplicated.size() == size() ? this : new Polyline(deduplicated);
    }

    public Polyline withoutFirst()
    {
        return new Polyline(Arrays.copyOfRange(locationsInDecimal(), 1, size()));
    }

    public Polyline withoutLast()
    {
        return new Polyline(Arrays.copyOfRange(locationsInDecimal(), 0, size() - 1));
    }

    protected long expandBounds(final long location)
    {
        final var latitude = Location.latitude(location);
        topInDecimal = Math.max(topInDecimal, latitude);
        bottomInDecimal = Math.min(bottomInDecimal, latitude);

        final var longitude = Location.longitude(location);
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
    Polyline shape(final int startIndex, final int endIndex)
    {
        return new Polyline(Arrays.copyOfRange(locationsInDecimal(), startIndex, endIndex + 1));
    }

    private long computeLengthInMillimeters()
    {
        var lengthInMillimeters = 0L;
        var previousLatitudeInDm7 = Long.MIN_VALUE;
        var previousLongitudeInDm7 = Long.MIN_VALUE;
        var index = 0;
        for (final var location : locationsInDecimal())
        {
            final var latitudeInDm7 = Location.latitude(location);
            final var longitudeInDm7 = Location.longitude(location);
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

    private long[] expandBounds(final long[] locations)
    {
        ensure(locations.length >= 2);
        for (final var location : locations)
        {
            expandBounds(location);
        }
        return locations;
    }

    private int minimum(final Distance[] distances)
    {
        final var minimum = Distance.MAXIMUM;
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

    private Segment segment(final int index)
    {
        if (index < size())
        {
            return new Segment(get(index), get(index + 1));
        }
        throw new IndexOutOfBoundsException("No segment at index " + index);
    }

    private Polyline smoothOnce(final Angle tolerance)
    {
        final var builder = new PolylineBuilder();
        builder.add(get(0));
        for (var i = 1; i < size() - 1; i++)
        {
            final var a = new Segment(get(i - 1), get(i));
            final var b = new Segment(get(i), get(i + 1));
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
                        final var start = a.at(a.length().minus(b.length()));
                        builder.add(start);
                        builder.add(new Segment(start, a.end()).midpoint());
                        builder.add(b.midpoint());
                    }
                    else
                    {
                        final var end = b.at(a.length());
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
