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

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.measurements.internal.lexakai.DiagramMapMeasurementGeographic;

import java.util.regex.Pattern;

/**
 * A compass heading from 0 to 360 degrees. Several useful constants are provided, a heading can be parsed with {@link
 * #parse(String)}, or a heading can be constructed with several factory methods:
 *
 * <ul>
 *     <li>{@link #degrees(double)}</li>
 *     <li>{@link #radians(double)}</li>
 *     <li>{@link #microdegrees(int)}</li>
 *     <li>{@link #nanodegrees(long)}</li>
 * </ul>
 * <p>
 * Basic mathematical operations are provided and a heading can be converted to an approximate compass direction with {@link #asApproximateDirection()}.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings({ "SpellCheckingInspection", "unused" }) @UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public class Heading extends Angle implements Headed
{
    public static final int NULL = 361;

    public static final Heading NORTH = degrees(0);

    public static final Heading EAST = degrees(90);

    public static final Heading SOUTH = degrees(180);

    public static final Heading WEST = degrees(270);

    public static final Heading NORTHEAST = degrees(45);

    public static final Heading NORTHWEST = degrees(270 + 45);

    public static final Heading SOUTHEAST = degrees(180 - 45);

    public static final Heading SOUTHWEST = degrees(180 + 45);

    public static Heading degrees(double degrees)
    {
        return nanodegrees((long) (degrees * NANODEGREES_PER_DEGREE));
    }

    public static Heading microdegrees(int microdegrees)
    {
        return nanodegrees(microdegrees * 1_000L);
    }

    public static Heading nanodegrees(long nanodegrees)
    {
        // Make sure it is a positive value.
        return new Heading((nanodegrees % MAXIMUM_NANODEGREES) + MAXIMUM_NANODEGREES);
    }

    public static Heading parse(String text)
    {
        return new Converter(Listener.nullListener()).convert(text);
    }

    public static Heading radians(double radians)
    {
        return nanodegrees((long) (radians * NANODEGREES_PER_RADIAN));
    }

    /**
     * Converts the given <code>String</code> to a new <code>Heading</code> object. The input string is expected to be
     * of the format of a floating point number followed by a unit identifier (just "degrees" for right now). Examples
     * would include '1 degree', '180.3 degrees'.
     *
     * @author jonathanl (shibo)
     */
    @SuppressWarnings("DuplicatedCode")
    public static class Converter extends BaseStringConverter<Heading>
    {
        /** Pattern to match strings */
        private static final Pattern PATTERN = Pattern.compile("([0-9]+([.,][0-9]+)?)\\s+(degree)s?",
                Pattern.CASE_INSENSITIVE);

        public Converter(Listener listener)
        {
            super(listener, Heading.class);
        }

        @Override
        protected String onToString(Heading value)
        {
            return value.asDegrees() + " degrees";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Heading onToValue(String value)
        {
            var matcher = PATTERN.matcher(value);
            if (matcher.matches())
            {
                var scalar = Double.parseDouble(matcher.group(1));
                var units = matcher.group(3);
                if ("degree".equalsIgnoreCase(units))
                {
                    return degrees(scalar);
                }
                else
                {
                    problem("Unrecognized units: ${debug}", value);
                    return null;
                }
            }
            else
            {
                problem("Unable to parse heading: ${debug}", value);
                return null;
            }
        }
    }

    /**
     * Converts degrees to and from {@link Heading}
     *
     * @author jonathanl (shibo)
     */
    @LexakaiJavadoc(complete = true)
    public static class DegreesConverter extends BaseStringConverter<Heading>
    {
        public DegreesConverter(Listener listener)
        {
            super(listener, Heading.class);
        }

        @Override
        protected Heading onToValue(String value)
        {
            return degrees(Double.parseDouble(value));
        }
    }

    private Heading(long nanodegrees)
    {
        super(nanodegrees);
    }

    public Direction asApproximateDirection()
    {
        if (isBetween(Angle.degrees(45), Angle.degrees(135), Chirality.CLOCKWISE))
        {
            return Direction.EAST;
        }
        if (isBetween(Angle.degrees(135), Angle.degrees(225), Chirality.CLOCKWISE))
        {
            return Direction.SOUTH;
        }
        if (isBetween(Angle.degrees(225), Angle.degrees(315), Chirality.CLOCKWISE))
        {
            return Direction.WEST;
        }
        return Direction.NORTH;
    }

    public Heading average(Heading that)
    {
        var smaller = isLessThan(that) ? this : that;
        var larger = isGreaterThan(that) ? this : that;

        var smallerClockwiseDifference = smaller.difference(larger, Chirality.CLOCKWISE);
        var largerClockwiseDifference = larger.difference(smaller, Chirality.CLOCKWISE);

        if (smallerClockwiseDifference.isLessThan(largerClockwiseDifference))
        {
            return smaller.plus(smallerClockwiseDifference.times(0.5));
        }
        else
        {
            return larger.plus(largerClockwiseDifference.times(0.5));
        }
    }

    public Heading bisect(Heading that, Chirality chirality)
    {
        return degrees(super.bisect(that, chirality).asDegrees());
    }

    @Override
    public Heading heading()
    {
        return this;
    }

    public Heading minimum(Heading that)
    {
        return isLessThan(that) ? this : that;
    }

    @Override
    public Heading minus(Angle angle)
    {
        return nanodegrees(super.minus(angle).asNanodegrees());
    }

    @Override
    public Heading plus(Angle angle)
    {
        return nanodegrees(super.plus(angle).asNanodegrees());
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    public Heading quantize(Angle quantum)
    {
        var quantumDegrees = (int) quantum.asDegrees();
        return degrees(((int) (asDegrees() + (quantumDegrees / 2))) / quantumDegrees * quantumDegrees);
    }

    @Override
    public long longValue()
    {
        return (long) asDegrees();
    }

    @Override
    public Heading reversed()
    {
        return plus(Angle.degrees(180));
    }
}
