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

package com.telenav.mesakit.map.geography;

import com.telenav.mesakit.map.geography.project.lexakai.diagrams.DiagramLocation;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Dimensioned;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Offset;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.data.validation.Validatable;
import com.telenav.kivakit.core.kernel.data.validation.Validation;
import com.telenav.kivakit.core.kernel.data.validation.Validator;
import com.telenav.kivakit.core.kernel.data.validation.validators.BaseValidator;
import com.telenav.kivakit.core.kernel.interfaces.model.Identifiable;
import com.telenav.kivakit.core.kernel.language.collections.list.StringList;
import com.telenav.kivakit.core.kernel.language.primitives.Longs;
import com.telenav.kivakit.core.kernel.language.reflection.populator.KivaKitPropertyConverter;
import com.telenav.kivakit.core.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat;
import com.telenav.kivakit.core.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.core.kernel.language.values.count.BitCount;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.kivakit.math.trigonometry.Trigonometry;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.awt.Point;
import java.io.Serializable;

import static com.telenav.mesakit.map.geography.Precision.DM7;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.core.kernel.language.strings.conversion.StringFormat.USER_LABEL_IDENTIFIER;

/**
 * Representation of a location on the globe in terms of latitude and longitude. Locations are stored internally in
 * {@link Precision#DM7}. Locations can be created in several different ways:
 * <ul>
 *    <li>{@link Location#degrees(double, double)}</li>
 *    <li>{@link Location#dm5(int, int)}</li>
 *    <li>{@link Location#dm5(long)}</li>
 *    <li>{@link Location#dm6(int, int)}</li>
 *    <li>{@link Location#dm6(long)}</li>
 *    <li>{@link Location#dm7(int, int)}</li>
 *    <li>{@link Location#dm7(long)}</li>
 * </ul>
 * <p>
 * The methods that construct from different decimal precisions come in two forms, the first taking two integers
 * representing latitude and longitude in the given precision, the second taking a single long. This long value
 * is composed of a high int and a low int storing the latitude and longitude in DM7 precision. The two values
 * can be retrieved with {@link Location#latitude(long)} and {@link Location#longitude(long)}.
 * <p>
 * A location value can be converted in the other direction from two DM7 values into a single long value with
 * {@link Location#toLong(int, int)}, or a location object can convert itself into a long value with {@link #asLong()}
 * or {@link #asLong(Precision)} if a particular precision is desired. There are also convenience methods for each
 * precision: {@link #asDm5Long()}, {@link #asDm6Long()} and {@link #asDm7Long()}.
 * <p>
 * New locations can also be derived from an existing location object in several ways:
 * <ul>
 *     <li>{@link #moved(Heading, Distance)} - This location moved the given distance at the given heading</li>
 *     <li>{@link #scaledBy(double, double)} - This location multiplied by the given scale factors</li>
 *     <li>{@link #offset(Width)} - This location offset by the given {@link Width}</li>
 *     <li>{@link #offset(Height)} - This location offset by the given {@link Height}</li>
 *     <li>{@link #minus(Width)} - This location offset by the given {@link Width}</li>
 *     <li>{@link #minus(Height)} - This location offset by the given {@link Height}</li>
 * </ul>
 * <p>
 * The distance between two locations can be computed in three different ways
 * <ul>
 *     <li>{@link #haversineDistanceTo(Location)}</li>
 *     <li>{@link #lawOfCosinesDistanceTo(Location)}</li>
 *     <li>{@link #equirectangularDistanceTo(Location)}</li>
 * </ul>
 * The haversine and cosines methods are both fairly accurate at any latitude for any distance. The equirectangular
 * distance formula is accurate enough only for locations that span a relatively "small" amount of latitude distance
 * (less than a few tens of kilometers). However, the equirectangular distance formula is much faster than the more
 * accurate methods, so it is the method used in {@link #distanceTo(Location)} when the latitude separation is small
 * and when it is large {@link #distanceTo(Location)} uses {@link #lawOfCosinesDistanceTo(Location)}. The method
 * {@link #preciseDistanceTo(Location)} can be used to find a precise distance without selecting haversine or law of
 * cosines specifically. It defaults to law of cosines at the present time.
 * <p>
 * Relationships between locations can be determined with these methods:
 * <ul>
 *     <li>{@link #headingTo(Location)} - The {@link Heading} from this location to the given location</li>
 *     <li>{@link #offsetTo(Location)} - The {@link Offset} from this location to the given location</li>
 *     <li>{@link #isClose(Location, Distance)} - True if this location is within the given distance of the given location</li>
 *     <li>{@link #isClose(Location, Angle)} - True if the given location's latitude and longitude are within the given angle of this location</li>
 * </ul>
 * Locations support validation via {@link Validatable}, and they can be intersected with a rectangle using
 * {@link Intersectable#intersects(com.telenav.mesakit.map.geography.shape.rectangle.Rectangle)}. Although a location is a zero-dimensional point, it also supports
 * retrieval of a bounding rectangle of zero width and height with {@link Bounded#bounds()}. Naturally, a location is
 * also {@link Located} for interoperation with other objects that are also {@link Located}.
 *
 * @author jonathanl (shibo)
 * @see Precision
 * @see Validatable
 * @see Located
 * @see Bounded
 * @see Intersectable
 */
@SuppressWarnings("SwitchStatementWithTooFewBranches")
@UmlClassDiagram(diagram = DiagramLocation.class)
@UmlExcludeSuperTypes({ Validatable.class, Identifiable.class, AsString.class, Serializable.class })
@UmlRelation(label = "represented at", referent = Precision.class)
public class Location implements Validatable, Located, Identifiable, Bounded, Intersectable, AsString, Serializable
{
    public static final Location TELENAV_HEADQUARTERS = new Location(Latitude.degrees(37.3859),
            Longitude.degrees(-122.0046));

    public static final Location ORIGIN = new Location(Latitude.ORIGIN, Longitude.ORIGIN);

    public static final Location MINIMUM = new Location(Latitude.MINIMUM, Longitude.MINIMUM);

    public static final Location MAXIMUM = new Location(Latitude.MAXIMUM, Longitude.MAXIMUM);

    public static final long NULL = Long.MIN_VALUE;

    public static final BitCount SIZE_IN_BITS = BitCount.bitCount(64);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final long EARTH_RADIUS_IN_METERS = (long) Distance.EARTH_RADIUS.asMeters();

    private static final double[] latitudeCosine = { 1.0,
            0.9998476951563913, 0.9993908270190958, 0.9986295347545738, 0.9975640502598242, 0.9961946980917455,
            0.9945218953682733, 0.992546151641322, 0.9902680687415704, 0.9876883405951378, 0.984807753012208,
            0.981627183447664, 0.9781476007338057, 0.9743700647852352, 0.9702957262759965, 0.9659258262890683,
            0.9612616959383189, 0.9563047559630354, 0.9510565162951535, 0.9455185755993168, 0.9396926207859084,
            0.9335804264972017, 0.9271838545667874, 0.9205048534524404, 0.9135454576426009, 0.9063077870366499,
            0.898794046299167, 0.8910065241883679, 0.882947592858927, 0.8746197071393957, 0.8660254037844387,
            0.8571673007021123, 0.848048096156426, 0.838670567945424, 0.8290375725550416, 0.8191520442889918,
            0.8090169943749475, 0.7986355100472928, 0.7880107536067219, 0.7771459614569709, 0.766044443118978,
            0.754709580222772, 0.7431448254773942, 0.7313537016191705, 0.7193398003386512, 0.7071067811865476,
            0.6946583704589973, 0.6819983600624985, 0.6691306063588582, 0.6560590289905073, 0.6427876096865394,
            0.6293203910498375, 0.6156614753256583, 0.6018150231520484, 0.5877852522924731, 0.5735764363510462,
            0.5591929034707468, 0.5446390350150271, 0.5299192642332049, 0.5150380749100542, 0.5000000000000001,
            0.4848096202463371, 0.46947156278589086, 0.4539904997395468, 0.43837114678907746, 0.42261826174069944,
            0.4067366430758002, 0.3907311284892737, 0.37460659341591196, 0.3583679495453004, 0.3420201433256688,
            0.32556815445715676, 0.30901699437494745, 0.29237170472273677, 0.27563735581699916, 0.25881904510252074,
            0.24192189559966767, 0.22495105434386492, 0.20791169081775945, 0.19080899537654492, 0.17364817766693041,
            0.15643446504023092, 0.13917310096006547, 0.12186934340514749, 0.10452846326765346, 0.08715574274765814,
            0.06975647374412523, 0.052335956242943966, 0.03489949670250108, 0.0174524064372836, 0.0
    };

    public static Location degrees(final double latitude, final double longitude)
    {
        final var latitudeRound = latitude < 0 ? -0.000_000_05 : 0.000_000_05;
        final var longitudeRound = longitude < 0 ? -0.000_000_05 : 0.000_000_05;
        return new Location((int) ((latitude + latitudeRound) * 1_000_000_0),
                (int) ((longitude + longitudeRound) * 1_000_000_0));
    }

    public static Location dm5(final int latitudeInDm5, final int longitudeInDm5)
    {
        return new Location(latitudeInDm5 * 100, longitudeInDm5 * 100);
    }

    /**
     * Constructs a location from a long value with latitude in the high 32 bits and longitude in the low 32 bits.
     *
     * @param location The combined latitude/longitude value
     * @return A new location object with the given latitude and longitude
     */
    public static Location dm5(final long location)
    {
        if (location == NULL)
        {
            return null;
        }
        else
        {
            return dm5(latitude(location), longitude(location));
        }
    }

    public static Location dm6(final int latitudeInDm6, final int longitudeInDm6)
    {
        return new Location(latitudeInDm6 * 10, longitudeInDm6 * 10);
    }

    /**
     * Constructs a location from a long value with latitude in the high 32 bits and longitude in the low 32 bits.
     *
     * @param location The combined latitude/longitude value
     * @return A new location object with the given latitude and longitude
     */
    public static Location dm6(final long location)
    {
        if (location == NULL)
        {
            return null;
        }
        else
        {
            return dm6(latitude(location), longitude(location));
        }
    }

    public static Location dm7(final int latitudeInDm7, final int longitudeInDm7)
    {
        return new Location(latitudeInDm7, longitudeInDm7);
    }

    /**
     * Constructs a location from a long value with latitude in the high 32 bits and longitude in the low 32 bits.
     *
     * @param location The combined latitude/longitude value
     * @return A new location object with the given latitude and longitude
     */
    public static Location dm7(final long location)
    {
        if (location == NULL)
        {
            return null;
        }
        else
        {
            return dm7(latitude(location), longitude(location));
        }
    }

    /**
     * A fast way to compute the distance between two points when the latitude difference is not that much. The formula
     * is essentially the euclidean distance formula (Pythagoras) adjusted by multiplying by cos(latitude), yielding the
     * distance in meters:
     * <pre>
     * var x = (lon2 - lon1) * Math.cos(lat1);
     * var y = (lat2 - lat1);
     * var d = Math.sqrt(x * x + y * y) * EARTH_RADIUS
     * </pre>
     * The implementation here uses a look-up table for cos(latitude) to improve performance further and returns the
     * result as a long in millimeters instead of as a double in meters.
     *
     * @return The distance in millimeters
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public static long equirectangularDistanceBetweenInMillimeters(
            final long fromLatitudeInDm7, final long fromLongitudeInDm7,
            final long toLatitudeInDm7, final long toLongitudeInDm7)
    {
        final var fromLatitude = fromLatitudeInDm7 / 1E7;
        final var fromLongitude = fromLongitudeInDm7 / 1E7;
        final var toLatitude = toLatitudeInDm7 / 1E7;
        final var toLongitude = toLongitudeInDm7 / 1E7;

        final var x = Math.toRadians(toLongitude - fromLongitude) * (latitudeCosine[(int) Math.abs(fromLatitude)] * (fromLatitude < 0 ? -1 : 1));
        final var y = Math.toRadians(toLatitude - fromLatitude);

        return (long) (Math.sqrt(x * x + y * y) * EARTH_RADIUS_IN_METERS * 1_000.0);
    }

    /**
     * @return A location for the given long value in DM7
     */
    public static Location fromLong(final long locationInDm7)
    {
        return dm7(locationInDm7);
    }

    public static int latitude(final long latitudeAndLongitude)
    {
        return Longs.high(latitudeAndLongitude);
    }

    public static int longitude(final long latitudeAndLongitude)
    {
        return Longs.low(latitudeAndLongitude);
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(final String[] args)
    {
        // Generate latitudeCosine table
        for (var latitude = 0.0; latitude <= 90; latitude += 1.0)
        {
            final var cosine = Math.cos(Math.toRadians(latitude));
            System.out.print(cosine + ", ");
            if (latitude % 10 == 0.0)
            {
                System.out.println();
            }
        }
    }

    public static SwitchParser.Builder<Location> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Location.class).name(name).converter(new DegreesConverter(LOGGER))
                .description(description);
    }

    /**
     * @return The given latitude and longitude as a single long value, with latitude in the high 32 bits and longitude
     * in the low 32 bits. The latitude and longitude may be in any {@link Precision}.
     */
    public static long toLong(final int latitude, final int longitude)
    {
        return Longs.forHighLow(latitude, longitude);
    }

    public static class Converter extends BaseStringConverter<Location>
    {
        public enum Type
        {
            DM5,
            DEGREES
        }

        private Type type = Type.DM5;

        public Converter(final Listener listener)
        {
            super(listener);
        }

        public void type(final Type type)
        {
            this.type = type;
        }

        @Override
        protected Location onConvertToObject(final String value)
        {
            final Latitude latitude;
            final Longitude longitude;

            final var coordinates = StringList.split(value, ',');

            if (type == Type.DM5)
            {
                latitude = Latitude.dm5(Integer.parseInt(coordinates.first()));
            }
            else
            {
                latitude = Latitude.degrees(Double.parseDouble(coordinates.first()));
            }

            if (type == Type.DM5)
            {
                longitude = Longitude.dm5(Integer.parseInt(coordinates.last()));
            }
            else
            {
                longitude = Longitude.degrees(Double.parseDouble(coordinates.last()));
            }

            return new Location(latitude, longitude);
        }
    }

    public static class DegreesConverter extends BaseStringConverter<Location>
    {
        private final Latitude.DegreesConverter latitudeConverter;

        private final Longitude.DegreesConverter longitudeConverter;

        private final Separators separators;

        public DegreesConverter(final Listener listener)
        {
            this(listener, new Separators(","));
        }

        public DegreesConverter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
            latitudeConverter = new Latitude.DegreesConverter(listener);
            longitudeConverter = new Longitude.DegreesConverter(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Location onConvertToObject(final String value)
        {
            final var values = StringList.split(value, separators.current());
            if (values.size() == 2)
            {
                final var latitude = latitudeConverter.convert(values.get(0).trim());
                final var longitude = longitudeConverter.convert(values.get(1).trim());

                if (latitude == null || longitude == null)
                {
                    problem("Invalid value(s) ${debug}", value);
                    return null;
                }
                return new Location(latitude, longitude);
            }
            else
            {
                // problem("Unable to parse: ${debug}", value);
                return null;
            }
        }

        @Override
        protected String onConvertToString(final Location value)
        {
            return value.latitude() + separators.current() + value.longitude();
        }
    }

    public static class DegreesMinutesAndSecondsConverter extends BaseStringConverter<Location>
    {

        private final Latitude.DegreesMinutesAndSecondsConverter latitudeConverter;

        private final Longitude.DegreesMinutesAndSecondsConverter longitudeConverter;

        private final Separators separators;

        public DegreesMinutesAndSecondsConverter(final Listener listener)
        {
            this(listener, new Separators(","));
        }

        public DegreesMinutesAndSecondsConverter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
            latitudeConverter = new Latitude.DegreesMinutesAndSecondsConverter(listener);
            longitudeConverter = new Longitude.DegreesMinutesAndSecondsConverter(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Location onConvertToObject(final String value)
        {
            final var values = StringList.split(value, separators.current());
            if (values.size() == 2)
            {
                final var latitude = latitudeConverter.convert(values.get(0).trim());
                final var longitude = longitudeConverter.convert(values.get(1).trim());

                if (latitude == null || longitude == null)
                {
                    problem("Invalid value(s) ${debug}", value);
                    return null;
                }
                return new Location(latitude, longitude);
            }
            else
            {
                // problem("Unable to parse: ${debug}", value);
                return null;
            }
        }

        @Override
        protected String onConvertToString(final Location value)
        {
            return value.latitude() + separators.current() + value.longitude();
        }
    }

    public static class Dm5Converter extends BaseStringConverter<Location>
    {
        private final Precision.Dm5LatitudeConverter latitudeConverter;

        private final Precision.Dm5LongitudeConverter longitudeConverter;

        private final Separators separators;

        public Dm5Converter(final Listener listener)
        {
            this(listener, new Separators(","));
        }

        public Dm5Converter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
            latitudeConverter = new Precision.Dm5LatitudeConverter(listener);
            longitudeConverter = new Precision.Dm5LongitudeConverter(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Location onConvertToObject(final String value)
        {
            final var values = StringList.split(value, separators.current());
            if (values.size() == 2)
            {
                final var latitude = latitudeConverter.convert(values.get(0).trim());
                final var longitude = longitudeConverter.convert(values.get(1).trim());

                if (latitude == null || longitude == null)
                {
                    problem("Invalid value(s) ${debug}", value);
                    return null;
                }
                return new Location(latitude, longitude);
            }
            else
            {
                problem("Unable to parse: ${debug}", value);
                return null;
            }
        }

        @Override
        protected String onConvertToString(final Location value)
        {
            return latitudeConverter.toString(value.latitude()) + separators.current()
                    + longitudeConverter.toString(value.longitude());
        }
    }

    @KivaKitPropertyConverter(Latitude.DegreesConverter.class)
    @UmlAggregation
    private Latitude latitude;

    @KivaKitPropertyConverter(Longitude.DegreesConverter.class)
    @UmlAggregation
    private Longitude longitude;

    private final int latitudeInDm7;

    private final int longitudeInDm7;

    public Location()
    {
        latitudeInDm7 = Integer.MAX_VALUE;
        longitudeInDm7 = Integer.MAX_VALUE;
    }

    public Location(final Latitude latitude, final Longitude longitude)
    {
        assert latitude != null;
        assert longitude != null;
        this.latitude = latitude;
        this.longitude = longitude;
        latitudeInDm7 = latitude.asDm7();
        longitudeInDm7 = longitude.asDm7();

        assert latitudeInDm7 <= 90_000_000_0;
        assert longitudeInDm7 <= 180_000_000_0;

        assert DM7.isValidLatitude(latitudeInDm7) : "DM7 latitude " + latitudeInDm7 + " is out of range";
        assert DM7.isValidLongitude(longitudeInDm7) : "DM7 longitude " + longitudeInDm7 + " is out of range";
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public Location(final Location location)
    {
        this(location.latitude(), location.longitude());
    }

    private Location(final int latitudeInDm7, final int longitudeInDm7)
    {
        this.latitudeInDm7 = latitudeInDm7;
        this.longitudeInDm7 = longitudeInDm7;

        assert this.latitudeInDm7 <= 90_000_000_0;
        assert this.longitudeInDm7 <= 180_000_000_0;

        assert DM7.isValidLatitude(latitudeInDm7) : "DM7 latitude " + latitudeInDm7 + " is out of range";
        assert DM7.isValidLongitude(longitudeInDm7) : "DM7 longitude " + longitudeInDm7 + " is out of range";
    }

    public Point asAwtPointInMicroDegrees()
    {
        return new Point(longitude().asMicrodegrees(), latitude().asMicrodegrees());
    }

    public long asDecimal(final Precision precision)
    {
        return DM7.to(precision, asDm7Long());
    }

    public long asDecimalLong(final Precision precision)
    {
        return precision.toLong(this);
    }

    public long asDm5Long()
    {
        return toLong(latitudeInDm7 / 100, longitudeInDm7 / 100);
    }

    public long asDm6Long()
    {
        return toLong(latitudeInDm7 / 10, longitudeInDm7 / 10);
    }

    public long asDm7Long()
    {
        return toLong(latitudeInDm7, longitudeInDm7);
    }

    public long asLong()
    {
        return asLong(Precision.DEFAULT);
    }

    /**
     * @return This location as a single long value, with latitude integer in the high 32 bits and longitude integer
     * value in the low 32 bits. The high and low values are either 6 or 7 decimals. If decimals is 0 (probably due to
     * an uninitialized serialized field), then 6 is assumed to allow for easy defaulting of old 6 decimal graph api
     * values.
     */
    public long asLong(final Precision precision)
    {
        assert precision != null;

        switch (precision)
        {
            case DM6:
                return asDm6Long();

            case DM7:
                return asDm7Long();

            default:
                fail("Precision $ not supported", precision);
                return -1;
        }
    }

    @Override
    public String asString(final StringFormat format)
    {
        switch (format.identifier())
        {
            case USER_LABEL_IDENTIFIER:
                return String.format("latitude = %.07f, longitude = %.07f", latitude().asDegrees(), longitude().asDegrees());

            default:
                return latitude() + "," + longitude();
        }
    }

    /**
     * @return A zero-area bounding rectangle at this location, which can be expanded with {@link
     * com.telenav.mesakit.map.geography.shape.rectangle.Rectangle#expanded(Distance)}
     */
    @Override
    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle bounds()
    {
        return com.telenav.mesakit.map.geography.shape.rectangle.Rectangle.fromLocations(this, this);
    }

    public Location decremented()
    {
        return dm7(latitudeInDm7 - 1, longitudeInDm7 - 1);
    }

    /**
     * @return The distance from this location to the given {@link Located} object
     */
    public Distance distanceTo(final Located that)
    {
        return distanceTo(that.location());
    }

    /**
     * Computes the distance from this location to the given location. If the latitudinal difference between the two
     * points is close (less than 0.05 degrees, or about 5.5 kilometers), then the faster {@link
     * #equirectangularDistanceTo(Location)} method is used. When the distance is more than this, the slower but more
     * accurate {@link #lawOfCosinesDistanceTo(Location)} is used. This gives a nice balance between efficiency and
     * accuracy.
     *
     * @return The distance from this location to the given location
     */
    public Distance distanceTo(final Location that)
    {
        // If the latitudes are within about 0.05 degrees (10_000_000 / 20)
        if (Math.abs(that.latitudeInDm7 - latitudeInDm7) < 500_000)
        {
            return equirectangularDistanceTo(that);
        }
        else
        {
            return lawOfCosinesDistanceTo(that);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Location)
        {
            final var that = (Location) object;
            return latitudeInDm7 == that.latitudeInDm7 && longitudeInDm7 == that.longitudeInDm7;
        }
        return false;
    }

    /**
     * @return The distance to the given location using an efficient formula consistent with an equirectangular
     * projection. NOTE: This formula is accurate enough for "short" distances (up to a few kilometers), but gets less
     * and less accurate as the latitude difference increases.
     * @see #equirectangularDistanceToInMillimeters(Location)
     * @see #equirectangularDistanceBetweenInMillimeters(long, long, long, long)
     */
    public final Distance equirectangularDistanceTo(final Location that)
    {
        return Distance.millimeters(equirectangularDistanceToInMillimeters(that));
    }

    public final long equirectangularDistanceToInMillimeters(final Location that)
    {
        // HOTSPOT: This method has been determined to be a hotspot by YourKit profiling

        return equirectangularDistanceBetweenInMillimeters(latitudeInDm7, longitudeInDm7,
                that.latitudeInDm7, that.longitudeInDm7);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return Integer.hashCode(latitudeInDm7) ^ Integer.hashCode(longitudeInDm7);
    }

    /**
     * @return The distance to the given location using the Haversine formula. Adapted from:
     * http://www.movable-type.co.uk/scripts/latlong.html
     */
    public Distance haversineDistanceTo(final Location that)
    {
        /*
          (lat2 - lat1) -> Vertical Angle
         */
        final Angle latitudeDifference = that.latitude().minus(latitude());
        /*
          (lon2 - lon1) -> Horizontal Angle
         */
        final Angle longitudeDifference = that.longitude().minus(longitude());
        final var hav = Math.pow(Math.sin(latitudeDifference.asRadians() / 2.0), 2.0)
                + Math.pow(Math.sin(longitudeDifference.asRadians() / 2.0), 2.0) * Math.cos(latitude().asRadians())
                * Math.cos(that.latitude().asRadians());
        final var resultAngle = 2.0 * Math.atan2(Math.sqrt(hav), Math.sqrt(1.0 - hav));
        return Distance.meters(Distance.EARTH_RADIUS.asMeters() * resultAngle);
    }

    /***
     * Computes the heading from start to end. See a sample algorithm here:
     * http://www.movable-type.co.uk/scripts/latlong.html
     */
    public Heading headingTo(final Location that)
    {
        final var longitudeDifference = DM7.toRadians(that.longitudeInDm7 - longitudeInDm7);
        final var startLatitude = DM7.toRadians(latitudeInDm7);
        final var endLatitude = DM7.toRadians(that.latitudeInDm7);
        final var y = Math.sin(longitudeDifference) * Math.cos(endLatitude);
        final var x = Math.cos(startLatitude) * Math.sin(endLatitude)
                - Math.sin(startLatitude) * Math.cos(endLatitude) * Math.cos(longitudeDifference);
        return Heading.radians(Trigonometry.arcTangent2(y, x));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long identifier()
    {
        return asLong();
    }

    public Location incremented()
    {
        return dm7(latitudeInDm7 + 1, longitudeInDm7 + 1);
    }

    /**
     * {@inheritDoc}
     *
     * @return True if the given rectangle contains this location
     */
    @Override
    public boolean intersects(final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle rectangle)
    {
        return rectangle.contains(this);
    }

    public boolean isClose(final Location that, final Angle tolerance)
    {
        return latitude().isClose(that.latitude(), tolerance) && longitude().isClose(that.longitude(), tolerance);
    }

    /**
     * @return True if the given location is as close or closer than the given distance from this location
     */
    public boolean isClose(final Location that, final Distance distance)
    {
        return distanceTo(that).isLessThanOrEqualTo(distance);
    }

    /**
     * @return True if the location is 0.0, 0.0
     */
    public boolean isOrigin()
    {
        return equals(ORIGIN);
    }

    /**
     * @return The latitude of this location
     */
    public Latitude latitude()
    {
        if (latitude == null)
        {
            latitude = DM7.toLatitude(latitudeInDm7);
        }
        return latitude;
    }

    public int latitudeInDm7()
    {
        return latitudeInDm7;
    }

    /**
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Distance lawOfCosinesDistanceTo(final Location that)
    {
        if (equals(that))
        {
            return Distance.ZERO;
        }
        final var lat1 = DM7.toRadians(latitudeInDm7);
        final var lon1 = DM7.toRadians(longitudeInDm7);
        final var lat2 = DM7.toRadians(that.latitudeInDm7);
        final var lon2 = DM7.toRadians(that.longitudeInDm7);
        return Distance.meters(Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * EARTH_RADIUS_IN_METERS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location location()
    {
        return this;
    }

    /**
     * @return The longitude of this location
     */
    public Longitude longitude()
    {
        if (longitude == null)
        {
            longitude = Longitude.dm7(longitudeInDm7);
        }
        return longitude;
    }

    public int longitudeInDm7()
    {
        return longitudeInDm7;
    }

    public Location minus(final Dimensioned sized)
    {
        return new Location(latitude().minus(sized.height()), longitude().minus(sized.width()));
    }

    public Location minus(final Height height)
    {
        return new Location(latitude().minus(height), longitude());
    }

    public Location minus(final Latitude latitude, final Longitude longitude)
    {
        return new Location(latitude().minus(latitude), longitude().minus(longitude));
    }

    public Location minus(final Width width)
    {
        return new Location(latitude(), longitude().minus(width));
    }

    /**
     * Given a Location, provide a new Location that is offset a given Distance and Heading. Adapted from:
     * http://www.movable-type.co.uk/scripts/latlong.html See section: "Destination point given distance and bearing
     * from start point"
     *
     * @param heading Angle based on the direction of a compass, with 0degree/360degree = North, 90degrees = East, 180
     * degrees = South, 270 degrees = West.
     * @param offset Distance from the source point
     * @return The new Location. If this location is on the other side of the -180/180 day separation line, then the
     * Longitudes will be adjusted to be between -180 and 180.
     */
    public Location moved(final Heading heading, final Distance offset)
    {
        final var latitude = latitude().asRadians();
        final var longitude = longitude().asRadians();
        final var angularDistance = offset.ratio(Distance.EARTH_RADIUS);

        final var movedLatitude = Math.asin(Math.sin(latitude) * Math.cos(angularDistance)
                + Math.cos(latitude) * Math.sin(angularDistance) * Math.cos(heading.asRadians()));
        final var movedLongitude = longitude
                + Math.atan2(Math.sin(heading.asRadians()) * Math.sin(angularDistance) * Math.cos(latitude),
                Math.cos(angularDistance) - Math.sin(latitude) * Math.sin(movedLatitude));
        var newLongitude = Angle.radians(movedLongitude);
        if (newLongitude.isLessThan(Longitude.MINIMUM))
        {
            newLongitude = newLongitude.plus(Angle.MAXIMUM);
        }
        if (newLongitude.isGreaterThan(Longitude.MAXIMUM))
        {
            newLongitude = newLongitude.minus(Angle.MAXIMUM);
        }
        return new Location(Latitude.angle(Angle.radians(movedLatitude)), Longitude.angle(newLongitude));
    }

    public Location offset(final Dimensioned sized)
    {
        return new Location(latitude().plus(sized.height()), longitude().plus(sized.width()));
    }

    public Location offset(final Height height)
    {
        return new Location(latitude().plus(height), longitude());
    }

    public Location offset(final Latitude latitude, final Longitude longitude)
    {
        return new Location(latitude().plus(latitude), longitude().plus(longitude));
    }

    public Location offset(final Width width)
    {
        return new Location(latitude(), longitude().plus(width));
    }

    public Offset offsetTo(final Location at)
    {
        return new Offset(at.longitude().minus(longitude()).asWidth(),
                at.latitude().minus(latitude()).asHeight());
    }

    public Distance preciseDistanceTo(final Location that)
    {
        return lawOfCosinesDistanceTo(that);
    }

    public Location quantize(final Distance quantum)
    {
        final var quantumMicrodegrees = quantum.asAngle().asMicrodegrees();
        final var latitude = Latitude.microdegrees(
                (latitude().asMicrodegrees() + (quantumMicrodegrees / 2)) / quantumMicrodegrees * quantumMicrodegrees);
        final var longitude = Longitude.microdegrees(
                (longitude().asMicrodegrees() + (quantumMicrodegrees / 2)) / quantumMicrodegrees * quantumMicrodegrees);
        return new Location(latitude, longitude);
    }

    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle rectangle(final Dimensioned sized)
    {
        return Rectangle.fromLocations(this, offset(sized));
    }

    public Location scaledBy(final double latitudeScale, final double longitudeScale)
    {
        return new Location(latitude().times(latitudeScale), longitude().times(longitudeScale));
    }

    public Segment to(final Location to)
    {
        return new Segment(this, to);
    }

    @Override
    public String toString()
    {
        return latitude() + "," + longitude();
    }

    @Override
    public Validator validator(final Validation type)
    {
        return new BaseValidator()
        {

            @Override
            protected void onValidate()
            {
                problemIf(!latitude().isValid(), "latitude is invalid");
                problemIf(!longitude().isValid(), "longitude is invalid");
            }
        };
    }

    public Location withLatitude(final Latitude latitude)
    {
        return new Location(latitude, longitude());
    }

    public Location withLongitude(final Longitude longitude)
    {
        return new Location(latitude(), longitude);
    }

    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle within(final Distance distance)
    {
        return Rectangle.fromLocations(this, this).expanded(distance);
    }
}