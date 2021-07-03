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

package com.telenav.mesakit.map.measurements.motion;

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.conversion.string.primitive.IntegerConverter;
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.kivakit.kernel.language.values.level.Level;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementMotion;

import java.util.regex.Pattern;

/**
 * A unitless speed, as the {@link Distance} traveled over a given {@link Duration}, both of which are also unitless.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementMotion.class)
@UmlExcludeSuperTypes(Quantizable.class)
public class Speed implements Comparable<Speed>, Quantizable
{
    public static final Speed INVALID = new Speed(Distance.MAXIMUM, Duration.milliseconds(1));

    /** Cache of speeds from 0 to 99.99 miles per hour */
    private static final Speed[] hundredthsOfMilesPerHourCache = new Speed[100 * 100];

    public static final Speed NONE = milesPerHour(0);

    public static final Speed SIXTY_FIVE_MILES_PER_HOUR = milesPerHour(65);

    /** Default highway speed */
    public static final Speed HIGHWAY_SPEED = SIXTY_FIVE_MILES_PER_HOUR;

    public static final Speed SIXTY_MILES_PER_HOUR = milesPerHour(60);

    public static final Speed FIFTY_FIVE_MILES_PER_HOUR = milesPerHour(55);

    public static final Speed FIFTY_MILES_PER_HOUR = milesPerHour(50);

    public static final Speed FORTY_FIVE_MILES_PER_HOUR = milesPerHour(45);

    public static final Speed FORTY_MILES_PER_HOUR = milesPerHour(40);

    public static final Speed THIRTY_FIVE_MILES_PER_HOUR = milesPerHour(35);

    public static final Speed THIRTY_MILES_PER_HOUR = milesPerHour(30);

    public static final Speed TWENTY_FIVE_MILES_PER_HOUR = milesPerHour(25);

    public static final Speed TWENTY_MILES_PER_HOUR = milesPerHour(20);

    public static final Speed FIFTEEN_MILES_PER_HOUR = milesPerHour(15);

    public static final Speed TEN_MILES_PER_HOUR = milesPerHour(10);

    public static final Speed FIVE_MILES_PER_HOUR = milesPerHour(5);

    public static final Speed MAXIMUM = milesPerHour(300);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static Speed distancePerDuration(final Distance distance, final Duration duration)
    {
        return new Speed(distance, duration);
    }

    public static Speed hundredthsOfAMilePerHour(final int speedInHundredthsOfAMilePerHour)
    {
        return milesPerHour(speedInHundredthsOfAMilePerHour / 100.0);
    }

    public static Speed kilometersPerHour(final double kph)
    {
        return new Speed(Distance.kilometers(kph), Duration.ONE_HOUR);
    }

    public static Speed metersPerHour(final double metersPerHour)
    {
        return new Speed(Distance.meters(metersPerHour), Duration.ONE_HOUR);
    }

    public static Speed metersPerSecond(final double metersPerSecond)
    {
        return new Speed(Distance.meters(metersPerSecond), Duration.ONE_SECOND);
    }

    public static Speed microDegreesPerSecond(final int value)
    {
        return milesPerHour(Duration.ONE_HOUR.asSeconds() * value / Distance.ONE_MILE.asDm6());
    }

    public static Speed milesPerHour(final double mph)
    {
        final var index = round(mph * 100);
        if (index < 0)
        {
            return INVALID;
        }
        //noinspection ConditionCoveredByFurtherCondition
        if (hundredthsOfMilesPerHourCache != null && index < hundredthsOfMilesPerHourCache.length)
        {
            if (hundredthsOfMilesPerHourCache[index] == null)
            {
                hundredthsOfMilesPerHourCache[index] = new Speed(Distance.miles(mph), Duration.ONE_HOUR);
            }
            return hundredthsOfMilesPerHourCache[index];
        }
        return new Speed(Distance.miles(mph), Duration.ONE_HOUR);
    }

    public static Speed millimetersPerHour(final long millimetersPerHour)
    {
        return new Speed(Distance.millimeters(millimetersPerHour), Duration.ONE_HOUR);
    }

    public static Speed parse(final String text)
    {
        return new Converter(LOGGER).convert(text);
    }

    public static SwitchParser.Builder<Speed> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Speed.class).name(name).converter(new Speed.Converter(LOGGER))
                .description(description);
    }

    /**
     * Converts the given <code>String</code> to a new <code>Speed</code> object. The input string is expected to be of
     * the format of a floating point number followed by a units identifier (mph, msec, kph, etc.). Examples would
     * include '55.4 mph', '13 msec', or '65 kph'.
     *
     * @author ericg
     */
    public static class Converter extends BaseStringConverter<Speed>
    {
        /** Pattern to match strings */
        private static final Pattern PATTERN = Pattern.compile("([0-9]+([.,][0-9]+)?)\\s+(mph|msec|kph)",
                Pattern.CASE_INSENSITIVE);

        public Converter(final Listener listener)
        {
            super(listener);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Speed onToValue(final String value)
        {
            final var matcher = PATTERN.matcher(value);
            if (matcher.matches())
            {
                final var scalar = Double.parseDouble(matcher.group(1));
                final var units = matcher.group(3);
                if ("mph".equalsIgnoreCase(units))
                {
                    return milesPerHour(scalar);
                }
                else if ("kph".equalsIgnoreCase(units))
                {
                    return kilometersPerHour(scalar);
                }
                else if ("msec".equalsIgnoreCase(units))
                {
                    return metersPerSecond(scalar);
                }
                else
                {
                    problem("Unrecognized units: ${debug}", value);
                    return null;
                }
            }
            else
            {
                problem("Unable to parse: ${debug}", value);
                return null;
            }
        }
    }

    public static class KilometersPerHourConverter extends BaseStringConverter<Speed>
    {
        public KilometersPerHourConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Speed onToValue(final String value)
        {
            return kilometersPerHour(Double.parseDouble(value));
        }
    }

    public static class MicroDegreesPerSecondConverter extends BaseStringConverter<Speed>
    {
        private final IntegerConverter integerConverter;

        public MicroDegreesPerSecondConverter(final Listener listener)
        {
            super(listener);
            integerConverter = new IntegerConverter(listener);
        }

        @Override
        protected Speed onToValue(final String value)
        {
            final var kilometersPerHour = integerConverter.convert(value);
            if (kilometersPerHour == null)
            {
                return null;
            }
            return microDegreesPerSecond(kilometersPerHour);
        }
    }

    public static class MilesPerHourConverter extends BaseStringConverter<Speed>
    {
        public MilesPerHourConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Speed onToValue(final String value)
        {
            return milesPerHour(Double.parseDouble(value));
        }
    }

    @UmlAggregation(label = "change in")
    private final Distance distance;

    @UmlAggregation(label = "per")
    private final Duration duration;

    private Speed(final Distance distance, final Duration duration)
    {
        this.distance = distance;
        this.duration = duration;
    }

    public int asHundredthsOfAMilePerHour()
    {
        return round(asMilesPerHour() * 100.0);
    }

    public double asKilometersPerHour()
    {
        return distance.asKilometers() / duration.asHours();
    }

    public double asMetersPerHour()
    {
        return distance.asMeters() / duration.asHours();
    }

    public double asMetersPerSecond()
    {
        return distance.asMeters() / duration.asSeconds();
    }

    public int asMicroDegreesPerSecond()
    {
        return round(asMilesPerHour() * Distance.ONE_MILE.asDm6() / Duration.ONE_HOUR.asSeconds());
    }

    public double asMilesPerHour()
    {
        return distance.asMiles() / duration.asHours();
    }

    public double asMillimetersPerHour()
    {
        return distance.asMillimeters() / duration.asHours();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final Speed that)
    {
        return isLessThan(that) ? -1 : (equals(that) ? 0 : 1);
    }

    public Speed difference(final Speed that)
    {
        final var difference = Math.abs(that.asMetersPerHour() - asMetersPerHour());
        return metersPerHour(difference);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Speed)
        {
            final var that = (Speed) object;

            // Two speeds that are within 0.1 mm/hour are considered equal
            return Math.abs(asMillimetersPerHour() - that.asMillimetersPerHour()) < 0.1;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(asMillimetersPerHour());
    }

    public boolean isBetween(final Speed a, final Speed b)
    {
        return isGreaterThan(a.minimum(b)) && isLessThan(a.maximum(b));
    }

    public boolean isGreaterThan(final Speed that)
    {
        return asMetersPerSecond() > that.asMetersPerSecond();
    }

    public boolean isGreaterThanOrEqualTo(final Speed that)
    {
        return asMetersPerSecond() >= that.asMetersPerSecond();
    }

    public boolean isInvalid()
    {
        return this == INVALID;
    }

    public boolean isLessThan(final Speed that)
    {
        return asMetersPerSecond() < that.asMetersPerSecond();
    }

    public boolean isLessThanOrEqualTo(final Speed that)
    {
        return asMetersPerSecond() <= that.asMetersPerSecond();
    }

    public boolean isNone()
    {
        return equals(NONE);
    }

    public Speed maximum(final Speed that)
    {
        return isGreaterThan(that) ? this : that;
    }

    public Speed minimum(final Speed that)
    {
        return isLessThan(that) ? this : that;
    }

    @Override
    public long quantum()
    {
        return (long) asKilometersPerHour();
    }

    public Speed scale(final Level coefficient)
    {
        return metersPerHour(asMetersPerHour() * coefficient.asZeroToOne());
    }

    public Duration timeToTravel(final Distance length)
    {
        return Duration.milliseconds(timeToTravelInMilliseconds(length));
    }

    public long timeToTravelInMilliseconds(final Distance length)
    {
        return length.asMillimeters() * duration.asMilliseconds() / distance.asMillimeters();
    }

    @Override
    public String toString()
    {
        return String.format("%.1f mph", asMilesPerHour());
    }

    /**
     * This is a faster alternative to Math.round, which is (as of JVM 6) painfully slow.
     */
    private static int round(final double value)
    {
        return (int) (value + .5);
    }
}
