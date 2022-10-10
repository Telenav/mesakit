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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.conversion.core.language.EnumConverter;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.language.primitive.Longs;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramLocation;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

/**
 * Decimal precision for {@link Angle}, {@link Latitude}, {@link Longitude}, {@link Width}, {@link Height} and {@link
 * Heading} values when they are stored as integral primitives.
 * <p>
 * DM{N} is a value that has N digits after the decimal.
 * <p>
 * For example, 85 degrees in DM6 is 85.000000 which is 85,000,000 as an integer.
 * <p>
 * In DM7, 85 degrees is 850,000,000.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramLocation.class)
public enum Precision
{
    NONE(0), // No precision
    DM5(5), // 5 places right of decimal (1 degree -> 100_000 DM5)
    DM6(6), // 6 places right of decimal (1 degree -> 1_000_000 DM6)
    DM7(7), // 7 places right of decimal (1 degree -> 10_000_000 DM7)
    ;

    public static final Precision DEFAULT = DM7;

    private static final long[] NANODEGREES_PER_DECIMAL = {
            0, // NONE
            1_0000L, // DM5
            1_000L, // DM6
            1_00L // DM7
    };

    private static final long[] MAXIMUM = {
            0, // NONE
            360_000_00L, // DM5
            360_000_000L, // DM6
            360_000_000_0L // DM7
    };

    private static final long[] MAXIMUM_LATITUDE_OFFSET = {
            0, // NONE
            180_000_00L, // DM5
            180_000_000L, // DM6
            180_000_000_0L // DM7
    };

    private static final long[] MAXIMUM_LONGITUDE_OFFSET = {
            0, // NONE
            360_000_00L, // DM5
            360_000_000L, // DM6
            360_000_000_0L // DM7
    };

    private static final long[] MAXIMUM_LATITUDE = {
            0, // NONE
            90_000_00L, // DM5
            90_000_000L, // DM6
            90_000_000_0L // DM7
    };

    private static final long[] MAXIMUM_LONGITUDE = {
            0, // NONE
            180_000_00L, // DM5
            180_000_000L, // DM6
            180_000_000_0L // DM7
    };

    // DM7 values have an accuracy of seven decimal places.

    // For example, the angle 110.1234567 is a DM7 value.

    // At the equator, one degree of latitude is 110,574 meters (110.6 kilometers).

    // This means that 110,574 meters (1 degree) / 10,000,000 DM7 units per degree =
    // 0.0110574 meters, which is about 1.1 centimeters.

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<Precision> precisionSwitchParser()
    {
        return precisionSwitchParser("data-precision", "The data precision (DM5, DM6 or DM7)");
    }

    public static SwitchParser.Builder<Precision> precisionSwitchParser(String name, String description)
    {
        return SwitchParser.switchParserBuilder(Precision.class)
                .name(name)
                .converter(new EnumConverter<>(LOGGER, Precision.class))
                .description(description);
    }

    public static class Dm5LatitudeConverter extends BaseStringConverter<Latitude>
    {
        public Dm5LatitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onToValue(String value)
        {
            return DM5.toLatitude(Integer.parseInt(value));
        }
    }

    public static class Dm5LongitudeConverter extends BaseStringConverter<Longitude>
    {
        public Dm5LongitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Longitude onToValue(String value)
        {
            return DM5.toLongitude(Integer.parseInt(value));
        }
    }

    public static class Dm6LatitudeConverter extends BaseStringConverter<Latitude>
    {
        public Dm6LatitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onToValue(String value)
        {
            return DM6.toLatitude(Integer.parseInt(value));
        }
    }

    public static class Dm6LongitudeConverter extends BaseStringConverter<Longitude>
    {
        public Dm6LongitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Longitude onToValue(String value)
        {
            return DM6.toLongitude(Integer.parseInt(value));
        }
    }

    public static class Dm7LatitudeConverter extends BaseStringConverter<Latitude>
    {
        public Dm7LatitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onToValue(String value)
        {
            return DM7.toLatitude(Integer.parseInt(value));
        }
    }

    public static class Dm7LongitudeConverter extends BaseStringConverter<Longitude>
    {
        public Dm7LongitudeConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Longitude onToValue(String value)
        {
            return DM7.toLongitude(Integer.parseInt(value));
        }
    }

    private int places;

    @SuppressWarnings("unused")
    Precision()
    {
    }

    Precision(int decimalPlaces)
    {
        places = decimalPlaces;
    }

    public boolean hasCorrectLatitudePlaces(int latitude)
    {
        if (latitude != 0)
        {
            latitude = latitude / Ints.intPowerOfTen(places());
            return latitude != 0 && Ints.intIsBetweenInclusive(latitude, (int) Latitude.MINIMUM_DEGREES, (int) Latitude.MAXIMUM_DEGREES);
        }
        return true;
    }

    public boolean hasCorrectLongitudePlaces(int longitude)
    {
        if (longitude != 0)
        {
            longitude = longitude / Ints.intPowerOfTen(places());
            return longitude != 0 && Ints.intIsBetweenInclusive(longitude, (int) Longitude.MINIMUM_DEGREES, (int) Longitude.MAXIMUM_DEGREES);
        }
        return true;
    }

    public long inRangeLatitude(int decimal)
    {
        return Longs.longInRangeInclusive(decimal, minimumLatitude(), maximumLatitude());
    }

    public long inRangeLatitudeOffset(long decimal)
    {
        return Longs.longInRangeInclusive(decimal, minimumLatitudeOffset(), maximumLatitudeOffset());
    }

    public long inRangeLongitude(int decimal)
    {
        return Longs.longInRangeInclusive(decimal, minimumLongitude(), maximumLongitude());
    }

    public long inRangeLongitudeOffset(long decimal)
    {
        return Longs.longInRangeInclusive(decimal, minimumLongitudeOffset(), maximumLongitudeOffset());
    }

    public final boolean isMaximum(int decimal)
    {
        return maximum() == decimal;
    }

    public final boolean isMaximumLatitude(int decimal)
    {
        return maximumLatitude() == decimal;
    }

    public final boolean isMaximumLongitude(int decimal)
    {
        return maximumLongitude() == decimal;
    }

    public final boolean isMinimum(int decimal)
    {
        return maximum() == decimal;
    }

    public final boolean isValidLatitude(int latitude)
    {
        return latitude >= minimumLatitude() && latitude <= maximumLatitude();
    }

    public final boolean isValidLatitudeOffset(int latitude)
    {
        return latitude >= minimumLatitudeOffset() && latitude <= maximumLatitudeOffset();
    }

    public boolean isValidLocation(long latitudeAndLongitude)
    {
        return isValidLatitude(Location.latitude(latitudeAndLongitude)) &&
                isValidLongitude(Location.longitude(latitudeAndLongitude));
    }

    public final boolean isValidLongitude(int longitude)
    {
        return longitude >= minimumLongitude() && longitude <= maximumLongitude();
    }

    public final boolean isValidLongitudeOffset(int longitude)
    {
        return longitude >= minimumLongitudeOffset() && longitude <= maximumLongitudeOffset();
    }

    public final long maximum()
    {
        return MAXIMUM[ordinal()];
    }

    public final long maximumLatitude()
    {
        return MAXIMUM_LATITUDE[ordinal()];
    }

    public long maximumLatitudeOffset()
    {
        return MAXIMUM_LATITUDE_OFFSET[ordinal()];
    }

    public final long maximumLongitude()
    {
        return MAXIMUM_LONGITUDE[ordinal()];
    }

    public long maximumLongitudeOffset()
    {
        return MAXIMUM_LONGITUDE_OFFSET[ordinal()];
    }

    public final long minimum()
    {
        return -MAXIMUM[ordinal()];
    }

    public final long minimumLatitude()
    {
        return -MAXIMUM_LATITUDE[ordinal()];
    }

    public long minimumLatitudeOffset()
    {
        return -MAXIMUM_LATITUDE_OFFSET[ordinal()];
    }

    public final long minimumLongitude()
    {
        return -MAXIMUM_LONGITUDE[ordinal()];
    }

    public long minimumLongitudeOffset()
    {
        return -MAXIMUM_LONGITUDE_OFFSET[ordinal()];
    }

    public final int nanodegreesToDecimal(long nanodegrees)
    {
        return (int) (nanodegrees / NANODEGREES_PER_DECIMAL[ordinal()]);
    }

    public final long offsetNanodegrees(long nanodegrees, int decimal)
    {
        return toNanodegrees(nanodegreesToDecimal(nanodegrees) + decimal);
    }

    public final int places()
    {
        return places;
    }

    /**
     * Converts the decimal value in the given precision to a decimal value in this precision
     */
    public final int to(Precision precision, int decimal)
    {
        var fromPlaces = places();
        var toPlaces = precision.places();
        var difference = toPlaces - fromPlaces;
        if (difference > 0)
        {
            return decimal * Ints.intPowerOfTen(difference);
        }
        if (difference < 0)
        {
            return decimal / Ints.intPowerOfTen(-difference);
        }
        return decimal;
    }

    public final long to(Precision precision, int latitude, int longitude)
    {
        return Location.toLong(to(precision, latitude),
                to(precision, longitude));
    }

    /**
     * Converts the decimal value in the given precision to a decimal value in this precision
     */
    public final long to(Precision precision, long decimal)
    {
        return to(precision, Location.latitude(decimal), Location.longitude(decimal));
    }

    /**
     * Convert degrees to decimal in this precision. Note that this method is here to avoid allocation for efficiency
     * purposes only and should ONLY be used when a hot spot is noticed in a profiler. See {@link
     * BoundingBoxBuilder#add(double, double)} for an example of such a hot spot that was found in
     * PbfResourceAnalysis.expandBounds (which is called for every node, way and relation).
     *
     * @param degrees Degrees to convert
     * @return The decimal value in this precision
     */
    public int toDecimal(double degrees)
    {
        return (int) (degrees * Angle.NANODEGREES_PER_DEGREE / NANODEGREES_PER_DECIMAL[ordinal()]);
    }

    public final int toDecimal(Angle angle)
    {
        return (int) (angle.asNanodegrees() / NANODEGREES_PER_DECIMAL[ordinal()]);
    }

    public double toDegrees(int decimal)
    {
        return (double) toNanodegrees(decimal) / Angle.NANODEGREES_PER_DEGREE;
    }

    public final int toDm5(int decimal)
    {
        assert decimal >= minimum();
        assert decimal <= maximum();
        return to(DM5, decimal);
    }

    public final int toDm6(int decimal)
    {
        assert decimal >= minimum();
        assert decimal <= maximum();
        return to(DM6, decimal);
    }

    public final int toDm7(int decimal)
    {
        assert decimal >= minimum();
        assert decimal <= maximum();
        return to(DM7, decimal);
    }

    public final Latitude toLatitude(int decimal)
    {
        assert decimal >= minimumLatitude();
        assert decimal <= maximumLatitude();
        return Latitude.nanodegrees(toNanodegrees(decimal));
    }

    public Distance toLatitudinalDistance(int from, int to)
    {
        return toLatitude(from).distanceTo(toLatitude(to));
    }

    public final Location toLocation(long decimal)
    {
        var latitude = Location.latitude(decimal);
        var longitude = Location.longitude(decimal);
        return Location.dm7(toDm7(latitude), toDm7(longitude));
    }

    public final Location toLocation(int latitude, int longitude)
    {
        return Location.dm7(toDm7(latitude), toDm7(longitude));
    }

    public final long toLong(Location location)
    {
        var latitudeInDm7 = location.latitudeInDm7();
        var longitudeInDm7 = location.longitudeInDm7();
        long value;
        if (this == DM7)
        {
            value = Location.toLong(latitudeInDm7, longitudeInDm7);
        }
        else
        {
            value = Location.toLong(DM7.to(this, latitudeInDm7),
                    DM7.to(this, longitudeInDm7));
        }
        assert toLocation(value).equals(location);
        return value;
    }

    public final Longitude toLongitude(int decimal)
    {
        assert decimal >= minimumLongitude();
        assert decimal <= maximumLongitude();
        return Longitude.nanodegrees(toNanodegrees(decimal));
    }

    public Distance toLongitudinalDistance(int from, int to, int at)
    {
        return toLongitude(from).distanceTo(toLongitude(to), toLatitude(at));
    }

    public final long toNanodegrees(int decimal)
    {
        assert decimal >= minimum();
        assert decimal <= maximum();
        return decimal * NANODEGREES_PER_DECIMAL[ordinal()];
    }

    public double toRadians(int decimal)
    {
        return (double) toNanodegrees(decimal) / Angle.NANODEGREES_PER_RADIAN;
    }

    public final Rectangle toRectangle(
            int bottomInDecimal, int leftInDecimal, int topInDecimal,
            int rightInDecimal)
    {
        return Rectangle.fromInts(toDm7(bottomInDecimal), toDm7(leftInDecimal),
                toDm7(topInDecimal), toDm7(rightInDecimal));
    }
}
