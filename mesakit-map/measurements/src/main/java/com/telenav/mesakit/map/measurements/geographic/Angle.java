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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.validation.BaseValidator;
import com.telenav.kivakit.kernel.data.validation.Validatable;
import com.telenav.kivakit.kernel.data.validation.ValidationType;
import com.telenav.kivakit.kernel.data.validation.Validator;
import com.telenav.kivakit.kernel.interfaces.numeric.Maximizable;
import com.telenav.kivakit.kernel.interfaces.numeric.Minimizable;
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.kernel.language.strings.conversion.StringFormat;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementGeographic;

import java.io.Serializable;
import java.util.regex.Pattern;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;
import static com.telenav.kivakit.kernel.language.strings.conversion.StringFormat.USER_LABEL_IDENTIFIER;

/**
 * An angle value represented between -360 and 360 degrees. Note that an angle of -45 degrees is seen as being less than
 * 15 degrees. In other words the values are not normalized for comparison.
 *
 * <p><b>DM5, DM6 and DM7</b></p>
 * <p>
 * DM5, DM6 and DM7 are measurements of accuracy (of angles, including latitudes and longitudes). DM5 values (DM =
 * Decimal) have 5 places after the decimal, and DM6 and DM7 have 6 and 7 places, respectively. For example:
 *
 * <ul>
 *     <li>DM5 - 47.12389</li>
 *     <li>DM6 - 47.123895</li>
 *     <li>DM7 - 47.1238951</li>
 * </ul>
 *
 * <p><b>Creating Angles</b></p>
 *
 * <p>
 * Several useful constants are provided, and factory methods create angles for different units (subclasses of {@link
 * Angle} will have their own construction methods):
 * </p>
 *
 * <ul>
 *     <li>{@link #degrees(double)}</li>
 *     <li>{@link #microdegrees(int)}</li>
 *     <li>{@link #nanodegrees(long)}</li>
 *     <li>{@link #radians(double)}</li>
 * </ul>
 *
 * <p><b>Operations</b></p>
 *
 * <ul>
 *     <li>{@link #absoluteDifference(Angle)}</li>
 *     <li>{@link #bisect(Angle, Chirality)}</li>
 *     <li>{@link #dividedBy(double)}</li>
 *     <li>{@link #dividedBy(Angle)}</li>
 *     <li>{@link #times(double)}</li>
 *     <li>{@link #plus(Angle)}</li>
 *     <li>{@link #maximum(Angle)}</li>
 *     <li>{@link #minimum(Angle)}</li>
 *     <li>{@link #projectionOnEarthSurface()}</li>
 *     <li>{@link #reversed()}</li>
 * </ul>
 *
 * <p><b>Checks</b></p>
 *
 * <ul>
 *     <li>{@link #isAcute()}</li>
 *     <li>{@link #isBetween(Angle, Angle, Chirality)}</li>
 *     <li>{@link #isClose(Angle, Angle)}</li>
 *     <li>{@link #isCloseOrReverseIsClose(Angle, Angle)}</li>
 *     <li>{@link #isMaximum()}</li>
 *     <li>{@link #isMinimum()}</li>
 *     <li>{@link #isOppositeDirection(Angle, Angle)}</li>
 * </ul>
 *
 * <p><b>Conversions</b></p>
 *
 * <ul>
 *     <li>{@link #asDegrees()}</li>
 *     <li>{@link #asMicrodegrees()}</li>
 *     <li>{@link #asNanodegrees()}</li>
 *     <li>{@link #asRadians()}</li>
 *     <li>{@link #asDm5()}</li>
 *     <li>{@link #asDm6()}</li>
 *     <li>{@link #asDm7()}</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SwitchStatementWithTooFewBranches")
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@UmlExcludeSuperTypes({ AsString.class, Serializable.class, Quantizable.class })
@LexakaiJavadoc(complete = true)
public class Angle implements
        Validatable,
        Comparable<Angle>,
        Minimizable<Angle>,
        Maximizable<Angle>,
        AsString,
        Quantizable,
        Serializable
{
    public static final long NANODEGREES_PER_DEGREE = 1_000_000_000;

    public static final Angle _0_DEGREES = nanodegrees(0);

    public static final Angle _45_DEGREES = degrees(45);

    public static final Angle _90_DEGREES = degrees(90);

    public static final Angle _180_DEGREES = degrees(180);

    public static final Angle _270_DEGREES = degrees(270);

    public static final Angle _MINUS_180_DEGREES = degrees(-180);

    protected static final double PI = 3.1415926535897932;

    protected static final double DEGREES_PER_RADIAN = 180.0 / PI;

    public static final double NANODEGREES_PER_RADIAN = NANODEGREES_PER_DEGREE * DEGREES_PER_RADIAN;

    protected static final double RADIANS_PER_DEGREE = PI / 180.0;

    protected static final long MAXIMUM_NANODEGREES = 360_000_000_000L;

    public static final Angle MAXIMUM = nanodegrees(MAXIMUM_NANODEGREES);

    protected static final long MINIMUM_NANODEGREES = -360_000_000_000L;

    public static final Angle MINIMUM = nanodegrees(MINIMUM_NANODEGREES);

    protected static final long NANODEGREES_PER_MICRODEGREE = 1_000;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final long NANODEGREES_PER_DM5 = 10_000;

    private static final long NANODEGREES_PER_DM6 = 1_000;

    private static final long NANODEGREES_PER_DM7 = 100;

    public static Angle degrees(final double degrees)
    {
        return nanodegrees((long) (degrees * NANODEGREES_PER_DEGREE));
    }

    public static double degreesPerRadian()
    {
        return DEGREES_PER_RADIAN;
    }

    public static Angle microdegrees(final int microdegrees)
    {
        return new Angle(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Angle nanodegrees(final long nanodegrees)
    {
        return new Angle(nanodegrees);
    }

    public static Angle radians(final double radians)
    {
        return nanodegrees((long) (radians * NANODEGREES_PER_RADIAN));
    }

    public static SwitchParser.Builder<Angle> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Angle.class).name(name).converter(new DegreesConverter(LOGGER))
                .description(description);
    }

    /**
     * Chirality is the generic term for clockwise or counterclockwise (the term comes from from the Greek for "hand"
     * and relates to the "hand rule" in physics)
     *
     * @author jonathanl (shibo)
     */
    public enum Chirality
    {
        CLOCKWISE,
        COUNTERCLOCKWISE,
        SMALLEST
    }

    /**
     * Converts the given <code>String</code> to a new <code>Angle</code> object. The input string is expected to be of
     * the format of a floating point number followed by a units identifier (just "degrees" for right now). Examples
     * would include '1 degree', '180.3 degrees'.
     *
     * @author jonathanl (shibo)
     */
    public static class Converter extends BaseStringConverter<Angle>
    {
        /** Pattern to match strings */
        private static final Pattern PATTERN = Pattern.compile("([0-9]+([.,][0-9]+)?)\\s+(degree)s?",
                Pattern.CASE_INSENSITIVE);

        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(final Angle value)
        {
            return value.asDegrees() + " degrees";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Angle onToValue(final String value)
        {
            final var matcher = PATTERN.matcher(value);
            if (matcher.matches())
            {
                final var scalar = Double.parseDouble(matcher.group(1));
                final var units = matcher.group(3);
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

    public static class DegreesConverter extends BaseStringConverter<Angle>
    {
        public DegreesConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Angle onToValue(final String value)
        {
            return degrees(Double.parseDouble(value));
        }
    }

    /**
     * The earth is approximately 24,901 miles around at the equator.
     * <p>
     * There are 360,000,000,000 nanodegrees in that distance.
     * <p>
     * So there are about 14,457,251 nanodegrees per mile (at the equator).
     * <p>
     * Since a mile is roughly 5,280 feet, one nanodegree is then about 0.0003 feet or 1/10th of 1mm.
     */
    long nanodegrees;

    protected Angle()
    {
    }

    protected Angle(final long nanodegrees)
    {
        // Get the valid range for the type of Angle
        final var maximum = maximumInNanoDegrees();
        final var minimum = minimumInNanoDegrees();

        // If we have a maximum or a minimum value precisely,
        if (nanodegrees == maximum || nanodegrees == minimum)
        {
            // just store it as is. This avoids turning a maximum or minimum value like 360 or -360
            // into 0 with the modulus arithmetic used to normalize values in the else clause
            this.nanodegrees = nanodegrees;
        }
        else
        {
            // Normalize the angle between the minimum and the maximum angle.
            this.nanodegrees = nanodegrees % maximum;
        }
    }

    /**
     * Returns the simple absolute difference between the two angles in either the clockwise or counterclockwise
     * direction, without handling the case of crossing 360.
     */
    public Angle absoluteDifference(final Angle that)
    {
        return nanodegrees(Math.abs(nanodegrees - that.nanodegrees));
    }

    public double asDegrees()
    {
        return (double) nanodegrees / (double) NANODEGREES_PER_DEGREE;
    }

    public final int asDm5()
    {
        return (int) (nanodegrees / NANODEGREES_PER_DM5);
    }

    public final int asDm6()
    {
        return (int) (nanodegrees / NANODEGREES_PER_DM6);
    }

    public final int asDm7()
    {
        return (int) (nanodegrees / NANODEGREES_PER_DM7);
    }

    public final int asMicrodegrees()
    {
        return (int) (nanodegrees / NANODEGREES_PER_MICRODEGREE);
    }

    public final long asNanodegrees()
    {
        return nanodegrees;
    }

    public double asRadians()
    {
        return nanodegrees / NANODEGREES_PER_RADIAN;
    }

    @Override
    public String asString(final StringFormat format)
    {
        switch (format.identifier())
        {
            case USER_LABEL_IDENTIFIER:
                return this + " degrees";

            default:
                return Double.valueOf(asDegrees()).toString();
        }
    }

    public Angle bisect(final Angle that, final Chirality chirality)
    {
        final var delta = difference(that, chirality);
        switch (chirality)
        {
            case CLOCKWISE:
                return plus(delta.dividedBy(2));

            case COUNTERCLOCKWISE:
                return minus(delta.dividedBy(2));

            case SMALLEST:
                if (plus(delta).equals(that))
                {
                    return plus(delta.dividedBy(2));
                }
                else
                {
                    return minus(delta.dividedBy(2));
                }

            default:
                return unsupported();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Angle that)
    {
        if (nanodegrees == that.nanodegrees)
        {
            return 0;
        }
        return nanodegrees < that.nanodegrees ? -1 : 1;
    }

    /**
     * @return This angle reduced by one nanodegree
     */
    public Angle decremented()
    {
        return nanodegrees(nanodegrees - 1);
    }

    /**
     * Computes the difference between this angle and another angle in various different ways.
     * <p>
     * Chirality.CLOCKWISE - Computes the difference from this angle to that angle in the clockwise direction, possibly
     * crossing the 360 degree line. For example, the difference between 10 degrees and 20 degrees is 10 degrees. The
     * difference between 355 degrees and 5 degrees in the clockwise direction is also 10 degrees.
     * <p>
     * Chirality.COUNTERCLOCKWISE - Computes the difference from this angle to that angle in the counterclockwise
     * direction, possibly crossing the 360 degree line. For example, the counterclockwise difference between 20 degrees
     * and 10 degrees is 10 degrees. The counterclockwise difference between 5 degrees and 355 degrees is also 10
     * degrees.
     * <p>
     * Chirality.SMALLEST - Returns the smaller of the clockwise and the counterclockwise differences
     *
     * @param that The other angle
     * @return The difference between the two angles as an angle
     */
    public Angle difference(final Angle that, final Chirality type)
    {
        switch (type)
        {
            case CLOCKWISE:
                // If our first angle is greater than the second one
                if (isGreaterThan(that))
                {
                    // we cross the 360 degree line, so we must make an adjustment
                    return nanodegrees(that.nanodegrees + MAXIMUM.nanodegrees - nanodegrees);
                }
                else
                {
                    // otherwise, we don't cross 360 degrees and the difference is simple
                    return nanodegrees(that.nanodegrees - nanodegrees);
                }

            case COUNTERCLOCKWISE:
                // If our first angle is less than the second one
                if (isLessThan(that))
                {
                    // we cross the 360 degree line, so we must make an adjustment
                    return nanodegrees(nanodegrees + MAXIMUM.nanodegrees - that.nanodegrees);
                }
                else
                {
                    // otherwise, we don't cross 360 degrees and the difference is simple
                    return nanodegrees(nanodegrees - that.nanodegrees);
                }

            case SMALLEST:
                return difference(that, Chirality.CLOCKWISE).minimum(difference(that, Chirality.COUNTERCLOCKWISE));
        }
        throw new IllegalArgumentException("Unsupported difference type " + type);
    }

    public Angle dividedBy(final Angle that)
    {
        return nanodegrees(nanodegrees / that.nanodegrees);
    }

    public Angle dividedBy(final double by)
    {
        return nanodegrees((long) (nanodegrees / by));
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof Angle)
        {
            final var that = (Angle) obj;

            // Two angles are considered to be equal if they are pointing in the
            // same direction. In other words, -1 and 359 are equal.
            return asNanodegrees() == that.asNanodegrees();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(asNanodegrees());
    }

    public boolean isAcute()
    {
        return isLessThan(_90_DEGREES);
    }

    /**
     * @param a The first angle defining the range.
     * @param b The second angle defining the range.
     * @return True indicates that the angle is greater than or equal to the minimum bound and less than or equal to the
     * upper bound.
     */
    public boolean isBetween(final Angle a, final Angle b, final Chirality type)
    {
        final var aToThis = a.difference(this, type);
        final var aToB = a.difference(b, type);
        return aToThis.isLessThanOrEqualTo(aToB);
    }

    public boolean isClose(final Angle that, final Angle tolerance)
    {
        return difference(that, Chirality.SMALLEST).isLessThanOrEqualTo(tolerance);
    }

    public boolean isCloseOrReverseIsClose(final Angle that, final Angle tolerance)
    {
        return isClose(that, tolerance) || isClose(that.reversed(), tolerance);
    }

    public boolean isMaximum()
    {
        return equals(maximum());
    }

    public boolean isMinimum()
    {
        return equals(minimum());
    }

    public boolean isOppositeDirection(final Angle that, final Angle tolerance)
    {
        return difference(that, Chirality.SMALLEST).isClose(_180_DEGREES, tolerance);
    }

    public Angle maximum()
    {
        return MAXIMUM;
    }

    @Override
    public Angle maximum(final Angle that)
    {
        return nanodegrees > that.nanodegrees ? this : that;
    }

    public Angle minimum()
    {
        return MINIMUM;
    }

    @Override
    public Angle minimum(final Angle that)
    {
        return nanodegrees < that.nanodegrees ? this : that;
    }

    public Angle minus(final Angle that)
    {
        return nanodegrees(nanodegrees - that.nanodegrees);
    }

    public Angle plus(final Angle that)
    {
        return nanodegrees(nanodegrees + that.nanodegrees);
    }

    /**
     * @return The distance on earth's surface of this angle from the center of the earth, assuming the earth is a
     * perfect sphere.
     */
    public Distance projectionOnEarthSurface()
    {
        return Distance.EARTH_RADIUS_MINOR.times(asRadians());
    }

    @Override
    public long quantum()
    {
        return nanodegrees;
    }

    public Angle reversed()
    {
        return plus(degrees(180));
    }

    public Angle times(final double multiplier)
    {
        return nanodegrees((long) (asNanodegrees() * multiplier));
    }

    @Override
    public String toString()
    {
        return asString(StringFormat.TEXT);
    }

    @Override
    public Validator validator(final ValidationType type)
    {
        return new BaseValidator()
        {
            @Override
            protected void onValidate()
            {
                problemIf(nanodegrees > MAXIMUM_NANODEGREES, "Angle of $ nanodegrees is too large", nanodegrees);
                problemIf(nanodegrees < -MAXIMUM_NANODEGREES, "Angle of $ nanodegrees is too small", nanodegrees);
            }
        };
    }

    protected long maximumInNanoDegrees()
    {
        return MAXIMUM_NANODEGREES;
    }

    protected long minimumInNanoDegrees()
    {
        return -MAXIMUM_NANODEGREES;
    }
}
