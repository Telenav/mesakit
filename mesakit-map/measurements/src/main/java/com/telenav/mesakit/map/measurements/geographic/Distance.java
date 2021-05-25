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

package com.telenav.mesakit.map.measurements.geographic;

import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.primitives.Doubles;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.level.Percent;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementGeographic;

import java.util.regex.Pattern;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * Abstract representation of the spatial separation of two points.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public final class Distance implements Quantizable, Comparable<Distance>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    // Conversion ratios
    private static final long FEET_PER_MILE = 5_280;

    private static final double METERS_PER_FOOT = 0.3048;

    private static final double METERS_PER_KILOMETER = 1_000;

    private static final long MILLIMETERS_PER_METER = 1_000;

    /** The largest distance we can represent */
    public static final Distance MAXIMUM = millimeters(Long.MAX_VALUE);

    public static final Distance EARTH_RADIUS_MINOR = meters(6_372_797);

    public static final Distance EARTH_RADIUS_MAJOR = meters(6_378_137);

    private static final double DM5_PER_DEGREE = 100_000;

    private static final double DM7_PER_DEGREE = 10_000_000;

    private static final double DM5_PER_KILOMETER = 1000.0 / EARTH_RADIUS_MINOR.asMeters() * Angle.degreesPerRadian() * DM5_PER_DEGREE;

    private static final double DM7_PER_KILOMETER = 1000.0 / EARTH_RADIUS_MINOR.asMeters() * Angle.degreesPerRadian() * DM7_PER_DEGREE;

    private static final double KILOMETERS_PER_DM7 = 1 / DM7_PER_KILOMETER;

    /**
     * Distance around the earth
     */
    public static final Distance EARTH_CIRCUMFERENCE = kilometers(40_008);

    /**
     * Distance of five meters
     */
    public static final Distance FIVE_METERS = meters(5);

    /**
     * Distance of one kilometer
     */
    public static final Distance ONE_KILOMETER = kilometers(1);

    /**
     * Distance of one meter
     */
    public static final Distance ONE_METER = meters(1);

    /**
     * Distance of one mile
     */
    public static final Distance ONE_MILE = miles(1);

    /**
     * Distance of _10 meters
     */
    public static final Distance TEN_METERS = meters(10);

    /**
     * Zero distance
     */
    public static final Distance ZERO = millimeters(0);

    /**
     * The smallest distance we can represent (NONE)
     */
    public static final Distance MINIMUM = ZERO;

    /**
     * Distance of one hundred meters
     */
    public static final Distance _100_METERS = meters(100);

    public static ArgumentParser.Builder<Distance> argumentParser(final String description)
    {
        return ArgumentParser.builder(Distance.class).converter(new Converter(LOGGER)).description(description);
    }

    public static Distance centimeters(final long centimeters)
    {
        return millimeters(centimeters * 10);
    }

    public static Distance degrees(final double degrees)
    {
        return Distance.meters(degrees * DM7_PER_DEGREE * KILOMETERS_PER_DM7);
    }

    public static Distance feet(final double feet)
    {
        return meters(feet * METERS_PER_FOOT);
    }

    public static Distance kilometers(final double kilometers)
    {
        return meters(kilometers * METERS_PER_KILOMETER);
    }

    public static Distance meters(final double meters)
    {
        return millimeters((long) (meters * MILLIMETERS_PER_METER));
    }

    public static Distance miles(final double miles)
    {
        return feet(miles * FEET_PER_MILE);
    }

    public static Distance millimeters(final long millimeters)
    {
        return new Distance(millimeters);
    }

    public static Distance of(final double value, final Unit unit)
    {
        switch (unit)
        {
            case FEET:
                return feet(value);

            case METERS:
                return meters(value);

            case MILES:
                return miles(value);

            case MILLIMETERS:
                return millimeters((long) value);

            case KILOMETERS:
                return kilometers(value);

            case CENTIMETERS:
                return centimeters((long) value);

            default:
                return fail();
        }
    }

    public static Distance parse(final String value)
    {
        return new Converter(LOGGER).convert(value);
    }

    public static SwitchParser.Builder<Distance> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Distance.class).name(name).converter(new Converter(LOGGER))
                .description(description);
    }

    /** Units that can be converted to */
    public enum Unit
    {
        MILLIMETERS,
        CENTIMETERS,
        METERS,
        KILOMETERS,
        FEET,
        MILES,
    }

    /**
     * Converts the given <code>String</code> to a new <code>Distance</code> object. The input string is expected to be
     * of the format of a floating point number followed by a units identifier (miles, kilometers, meters, feet, etc.).
     * Examples would include '1 mile', '2.3 kilometers', or '62.3 meters'.
     *
     * @author jonathanl (shibo)
     * @author ericg
     */
    public static class Converter extends BaseStringConverter<Distance>
    {
        /** Pattern to match strings */
        private static final Pattern PATTERN = Pattern
                .compile("([0-9]+([.,][0-9]+)?)(\\s+|-)(mile|kilometer|km|meter|feet)s?", Pattern.CASE_INSENSITIVE);

        public Converter(final Listener listener)
        {
            super(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Distance onConvertToObject(final String value)
        {
            final var matcher = PATTERN.matcher(value);
            if (matcher.matches())
            {
                final var scalar = Double.parseDouble(matcher.group(1));
                final var units = matcher.group(4);
                if ("mile".equalsIgnoreCase(units))
                {
                    return miles(scalar);
                }
                else if ("kilometer".equalsIgnoreCase(units) || "km".equalsIgnoreCase(units))
                {
                    return kilometers(scalar);
                }
                else if ("meter".equalsIgnoreCase(units))
                {
                    return meters(scalar);
                }
                else if ("feet".equalsIgnoreCase(units))
                {
                    return feet(scalar);
                }
                else
                {
                    problem("Unrecognized units: ${debug}", value);
                    return null;
                }
            }
            else
            {
                problem("Unable to parse distance: ${debug}", value);
                return null;
            }
        }

        @Override
        protected String onConvertToString(final Distance value)
        {
            if (value.isLessThan(meters(500)))
            {
                return value.asMeters() + " meters";
            }
            return value.asKilometers() + " km";
        }
    }

    public static class KilometersConverter extends BaseStringConverter<Distance>
    {
        public KilometersConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Distance onConvertToObject(final String value)
        {
            return kilometers(Double.parseDouble(value));
        }
    }

    public static class MetersConverter extends BaseStringConverter<Distance>
    {
        public MetersConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Distance onConvertToObject(final String value)
        {
            return meters(Double.parseDouble(value));
        }
    }

    public static class MilesConverter extends BaseStringConverter<Distance>
    {
        public MilesConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Distance onConvertToObject(final String value)
        {
            return miles(Double.parseDouble(value));
        }
    }

    public static class MillimetersConverter extends BaseStringConverter<Distance>
    {
        public MillimetersConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Distance onConvertToObject(final String value)
        {
            return millimeters(Long.parseLong(value));
        }
    }

    /**
     * The number of millimeters in this {@link Distance}
     */
    private final long millimeters;

    /**
     * This constructor should not be used outside of this class. It is made protected to allow for other classes to
     * extend it.
     *
     * @param millimeters The distance in millimeters
     */
    private Distance(final long millimeters)
    {
        // Ensure that the distance is either the special value MAXIMUM or it is < 100_000 km
        assert millimeters == Long.MAX_VALUE || millimeters < (100_000L * METERS_PER_KILOMETER * MILLIMETERS_PER_METER);

        this.millimeters = millimeters;
    }

    public Distance add(final Distance that)
    {
        return millimeters(asMillimeters() + that.asMillimeters());
    }

    public double as(final Unit unit)
    {
        switch (unit)
        {
            case FEET:
                return asFeet();

            case METERS:
                return asMeters();

            case MILES:
                return asMiles();

            case MILLIMETERS:
                return asMillimeters();

            case KILOMETERS:
                return asKilometers();

            case CENTIMETERS:
                return asCentimeters();

            default:
                return fail();
        }
    }

    public Angle asAngle()
    {
        return Angle.degrees(asDegrees());
    }

    public long asCentimeters()
    {
        return millimeters / 10;
    }

    public double asDegrees()
    {
        return asDm5() / DM5_PER_DEGREE;
    }

    public double asDm5()
    {
        return asKilometers() * DM5_PER_KILOMETER;
    }

    public double asDm6()
    {
        return asDm5() * 10;
    }

    public double asFeet()
    {
        return asMeters() / METERS_PER_FOOT;
    }

    public int asInt(final Unit unit)
    {
        return (int) (as(unit) + 0.5);
    }

    public double asKilometers()
    {
        return asMeters() / METERS_PER_KILOMETER;
    }

    public double asMeters()
    {
        return (double) asMillimeters() / MILLIMETERS_PER_METER;
    }

    public double asMiles()
    {
        return asFeet() / FEET_PER_MILE;
    }

    public long asMillimeters()
    {
        return millimeters;
    }

    public Area by(final Distance that)
    {
        return Area.of(this, that);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final Distance that)
    {
        if (isLessThan(that))
        {
            return -1;
        }
        if (isGreaterThan(that))
        {
            return 1;
        }
        return 0;
    }

    /**
     * @param that The other distance to compare with.
     * @return The absolute value difference between the two distances.
     */
    public Distance difference(final Distance that)
    {
        return millimeters(Math.abs(asMillimeters() - that.asMillimeters()));
    }

    public Distance dividedBy(final Count divisor)
    {
        return millimeters(asMillimeters() / divisor.asInt());
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof Distance)
        {
            return asMillimeters() == ((Distance) obj).asMillimeters();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(asMillimeters());
    }

    public boolean isGreaterThan(final Distance that)
    {
        return asMillimeters() > that.asMillimeters();
    }

    public boolean isGreaterThanOrEqualTo(final Distance that)
    {
        return asMillimeters() >= that.asMillimeters();
    }

    public boolean isLessThan(final Distance that)
    {
        return asMillimeters() < that.asMillimeters();
    }

    public boolean isLessThanOrEqualTo(final Distance that)
    {
        return asMillimeters() <= that.asMillimeters();
    }

    @Override
    public boolean isZero()
    {
        return equals(ZERO);
    }

    public Distance maximum(final Distance that)
    {
        return isGreaterThan(that) ? this : that;
    }

    public Distance minimum(final Distance that)
    {
        return isLessThan(that) ? this : that;
    }

    /**
     * @param that The distance to subtract from this one.
     * @return The newly calculated distance. Note that if the passed in value is greater than this value 0 is returned.
     * There are no negative distances.
     */
    public Distance minus(final Distance that)
    {
        final var difference = asMillimeters() - that.asMillimeters();
        return millimeters(difference < 0 ? 0 : difference);
    }

    public Percent percentageOf(final Distance that)
    {
        if (that.asMeters() == 0.0)
        {
            return Percent._100;
        }
        return Percent.of(100.0 * Doubles.inRange(asMeters() / that.asMeters(), 0.0, 1.0));
    }

    @Override
    public long quantum()
    {
        return millimeters;
    }

    public double ratio(final Distance divisor)
    {
        assert divisor.asMillimeters() > 0 : "Unable to divide by zero or a negative value " + divisor;

        return (double) asMillimeters() / (double) divisor.asMillimeters();
    }

    public Area squared()
    {
        return Area.squareMeters(asMeters() * asMeters());
    }

    public Distance times(final Percent percent)
    {
        return millimeters(percent.scale(asMillimeters()));
    }

    public Distance times(final double multiplier)
    {
        if (multiplier < 0)
        {
            return fail("Unable to scale by a negative value");
        }
        return millimeters((long) (asMillimeters() * multiplier));
    }

    public String toCommaSeparatedString()
    {
        if (asMeters() < 1000)
        {
            return String.format("%,.1f meters (%,.1f feet)", asMeters(), asFeet());
        }
        return String.format("%,.1f km (%,.1f miles)", asKilometers(), asMiles());
    }

    @Override
    public String toString()
    {
        if (asMeters() < 1000)
        {
            return String.format("%.1f meters (%.1f feet)", asMeters(), asFeet());
        }
        return String.format("%.1f km (%.1f miles)", asKilometers(), asMiles());
    }
}
