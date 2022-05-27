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

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.count.Range;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.lexakai.DiagramLocation;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.regex.Pattern;

import static com.telenav.kivakit.core.value.count.Range.rangeInclusive;
import static com.telenav.mesakit.map.geography.Precision.DM5;
import static com.telenav.mesakit.map.geography.Precision.DM6;
import static com.telenav.mesakit.map.geography.Precision.DM7;

/**
 * Latitude implementation or a simple angle between -85 and 85.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection") @UmlClassDiagram(diagram = DiagramLocation.class)
public final class Latitude extends Angle
{
    public static final Range<Angle> RANGE;

    public static final Latitude ORIGIN = new Latitude(0);

    public static final Latitude MAXIMUM;

    public static final Latitude MINIMUM;

    public static final double MAXIMUM_DEGREES = 85.0;

    public static final double MINIMUM_DEGREES = -85.0;

    private static final long MAXIMUM_NANODEGREES = (long) (MAXIMUM_DEGREES * 1E9);

    private static final long MINIMUM_NANODEGREES = (long) (MINIMUM_DEGREES * 1E9);

    static
    {
        MAXIMUM = nanodegrees(MAXIMUM_NANODEGREES);
        MINIMUM = nanodegrees(MINIMUM_NANODEGREES);
        RANGE = rangeInclusive(MINIMUM, MAXIMUM);
    }

    public static Latitude angle(Angle angle)
    {
        return new Latitude(angle.asNanodegrees());
    }

    public static Latitude degrees(double degrees)
    {
        return nanodegrees((long) (degrees * 1E9));
    }

    public static Latitude degreesInRange(double degrees)
    {
        return degrees(inRange(degrees));
    }

    public static Latitude degreesMinutesAndSeconds(int degrees, float minutes, float seconds)
    {
        if (degrees < 0)
        {
            return degrees(degrees - minutes / 60.0 - seconds / 3600.0);
        }
        return degrees(degrees + minutes / 60.0 + seconds / 3600.0);
    }

    public static Latitude dm5(int latitudeInDm5)
    {
        return DM5.toLatitude(latitudeInDm5);
    }

    public static Latitude dm6(int latitudeInDm6)
    {
        return DM6.toLatitude(latitudeInDm6);
    }

    public static Latitude dm7(int latitudeInDm7)
    {
        return DM7.toLatitude(latitudeInDm7);
    }

    public static double inRange(double degrees)
    {
        if (degrees < MINIMUM_DEGREES)
        {
            return MINIMUM_DEGREES;
        }
        return Math.min(degrees, MAXIMUM_DEGREES);
    }

    public static boolean isValid(double latitude)
    {
        return latitude >= MINIMUM_DEGREES && latitude <= MAXIMUM_DEGREES;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static Latitude microdegrees(int microdegrees)
    {
        return new Latitude(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Latitude nanodegrees(long nanodegrees)
    {
        return new Latitude(nanodegrees);
    }

    public static class DegreesConverter extends BaseStringConverter<Latitude>
    {
        public DegreesConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onToValue(String value)
        {
            return degrees(Double.parseDouble(value));
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static class DegreesMinutesAndSecondsConverter extends BaseStringConverter<Latitude>
    {
        private static final Pattern pattern = Pattern
                .compile("\\+?(-?\\d+)\\s*°\\s*(\\d+\\.?\\d*)\\s*'\\s*((\\d+\\.?\\d*)\\s*\")?");

        public DegreesMinutesAndSecondsConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onToValue(String string)
        {
            var matcher = pattern.matcher(string);
            if (matcher.matches())
            {
                var degrees = Integer.parseInt(matcher.group(1));
                var minutes = Float.parseFloat(matcher.group(2));
                var seconds = 0F;
                var group4 = matcher.group(4);
                if (!Strings.isEmpty(group4))
                {
                    seconds = Float.parseFloat(group4);
                }
                return degreesMinutesAndSeconds(degrees, minutes, seconds);
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    private Latitude()
    {
    }

    private Latitude(long nanodegrees)
    {
        super(nanodegrees);
        assert nanodegrees >= MINIMUM_NANODEGREES && nanodegrees <= MAXIMUM_NANODEGREES : "Invalid latitude angle:  " + Angle.nanodegrees(nanodegrees);
    }

    public int as(Precision precision)
    {
        return precision.nanodegreesToDecimal(asNanodegrees());
    }

    public Height asHeight()
    {
        return Height.nanodegrees(asNanodegrees());
    }

    @Override
    public Latitude decremented()
    {
        if (equals(MINIMUM))
        {
            return this;
        }
        return nanodegrees(DM7.offsetNanodegrees(asNanodegrees(), -1));
    }

    public Distance distanceTo(Latitude that)
    {
        return new Location(this, Longitude.ORIGIN).distanceTo(new Location(that, Longitude.ORIGIN));
    }

    public Latitude incremented()
    {
        if (equals(MAXIMUM))
        {
            return this;
        }
        return nanodegrees(DM7.offsetNanodegrees(asNanodegrees(), 1));
    }

    @Override
    public Latitude maximum()
    {
        return MAXIMUM;
    }

    public Latitude maximum(Latitude that)
    {
        return asNanodegrees() > that.asNanodegrees() ? this : that;
    }

    @Override
    public Latitude minimum()
    {
        return MINIMUM;
    }

    public Latitude minimum(Latitude that)
    {
        return asNanodegrees() < that.asNanodegrees() ? this : that;
    }

    /**
     * @param that The angle to be subtracted.
     * @return The resulting latitude or -85 if the minimum latitude has been reached.
     */
    @Override
    public Latitude minus(Angle that)
    {
        var result = super.minus(that);

        // Cap the values at the poles.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    /**
     * @param that The angle to be added.
     * @return The resulting latitude or 85 if the maximum latitude has been reached.
     */
    @Override
    public Latitude plus(Angle that)
    {
        var result = super.plus(that);

        // Cap the values at the poles.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    @Override
    public Latitude times(double multiplier)
    {
        var result = (long) (asNanodegrees() * multiplier);

        // Cap the values at the poles.
        if (result < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result > MAXIMUM_NANODEGREES ? MAXIMUM : nanodegrees(result);
    }

    @Override
    protected long maximumInNanoDegrees()
    {
        return MAXIMUM_NANODEGREES;
    }

    @Override
    protected long minimumInNanoDegrees()
    {
        return MINIMUM_NANODEGREES;
    }
}
