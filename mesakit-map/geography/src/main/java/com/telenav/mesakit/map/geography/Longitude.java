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
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.values.count.Range;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.regex.Pattern;

import static com.telenav.mesakit.map.geography.Precision.DM7;

/**
 * Longitude implementation or a simple angle between -180 and 180.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramLocation.class)
public final class Longitude extends Angle
{
    public static final Range<Angle> RANGE;

    public static final Longitude MAXIMUM;

    public static final Longitude MINIMUM;

    public static final Longitude ORIGIN = new Longitude(0);

    private static final long MAXIMUM_NANODEGREES = 180_000_000_000L;

    private static final long MINIMUM_NANODEGREES = -180_000_000_000L;

    static
    {
        MAXIMUM = nanodegrees(MAXIMUM_NANODEGREES);
        MINIMUM = nanodegrees(MINIMUM_NANODEGREES);
        RANGE = new Range<>(MINIMUM, MAXIMUM);
    }

    public static Longitude angle(final Angle angle)
    {
        return new Longitude(angle.asNanodegrees());
    }

    public static Longitude degrees(final double degrees)
    {
        return new Longitude((long) (degrees * 1_000_000_000));
    }

    public static Longitude degreesInRange(final double degrees)
    {
        return degrees(inRange(degrees));
    }

    public static Longitude degreesMinutesAndSeconds(final int degrees, final float minutes, final float seconds)
    {
        if (degrees < 0)
        {
            return degrees(degrees - minutes / 60.0 - seconds / 3600.0);
        }
        return degrees(degrees + minutes / 60.0 + seconds / 3600.0);
    }

    public static Longitude dm5(final int longitudeInDm5)
    {
        return Precision.DM5.toLongitude(longitudeInDm5);
    }

    public static Longitude dm6(final int longitudeInDm6)
    {
        return Precision.DM6.toLongitude(longitudeInDm6);
    }

    public static Longitude dm7(final int longitudeInDm7)
    {
        return DM7.toLongitude(longitudeInDm7);
    }

    public static double inRange(final double degrees)
    {
        return degrees % 180.0;
    }

    public static boolean isValid(final double longitude)
    {
        return longitude >= -180 && longitude <= 180;
    }

    public static Longitude microdegrees(final int microdegrees)
    {
        return new Longitude(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Longitude nanodegrees(final long nanodegrees)
    {
        return new Longitude(nanodegrees);
    }

    public static class DegreesConverter extends BaseStringConverter<Longitude>
    {
        public DegreesConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Longitude onConvertToObject(final String value)
        {
            return degrees(Double.parseDouble(value));
        }
    }

    public static class DegreesMinutesAndSecondsConverter extends BaseStringConverter<Longitude>
    {
        private static final Pattern pattern = Pattern
                .compile("\\+?(-?\\d+)\\s*°\\s*(\\d+\\.?\\d*)\\s*'\\s*((\\d+\\.?\\d*)\\s*\")?");

        public DegreesMinutesAndSecondsConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Longitude onConvertToObject(final String string)
        {
            final var matcher = pattern.matcher(string);
            if (matcher.matches())
            {
                final var degrees = Integer.parseInt(matcher.group(1));
                final var minutes = Float.parseFloat(matcher.group(2));
                var seconds = 0F;
                final var group4 = matcher.group(4);
                if (!Strings.isEmpty(group4))
                {
                    seconds = Float.parseFloat(group4);
                }
                return degreesMinutesAndSeconds(degrees, minutes, seconds);
            }
            return null;
        }
    }

    protected Longitude()
    {
    }

    private Longitude(final long nanodegrees)
    {
        super(nanodegrees);
        assert nanodegrees >= MINIMUM_NANODEGREES && nanodegrees <= MAXIMUM_NANODEGREES : "Invalid longitude angle:  " + Angle.nanodegrees(nanodegrees);
    }

    public int as(final Precision precision)
    {
        return precision.nanodegreesToDecimal(asNanodegrees());
    }

    public Width asWidth()
    {
        return Width.nanodegrees(asNanodegrees());
    }

    @Override
    public Longitude decremented()
    {
        if (equals(MINIMUM))
        {
            return this;
        }
        return nanodegrees(DM7.offsetNanodegrees(asNanodegrees(), -1));
    }

    public Distance distanceTo(final Longitude that, final Latitude at)
    {
        return new Location(at, this).distanceTo(new Location(at, that));
    }

    public Longitude incremented()
    {
        if (equals(MAXIMUM))
        {
            return this;
        }
        return nanodegrees(DM7.offsetNanodegrees(asNanodegrees(), 1));
    }

    @Override
    public Longitude maximum()
    {
        return MAXIMUM;
    }

    public Longitude maximum(final Longitude that)
    {
        return asNanodegrees() > that.asNanodegrees() ? this : that;
    }

    @Override
    public Longitude minimum()
    {
        return MINIMUM;
    }

    public Longitude minimum(final Longitude that)
    {
        return asNanodegrees() < that.asNanodegrees() ? this : that;
    }

    /**
     * @param that The angle to be subtracted
     * @return The resulting longitude. Note that if the longitude goes out of bounds it will be capped.
     */
    @Override
    public Longitude minus(final Angle that)
    {
        final var result = super.minus(that);

        // Cap the result at the minimum and maximum.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    /**
     * @param that The angle to be added.
     * @return The resulting longitude. Note that if the longitude goes out of bounds it will be capped.
     */
    @Override
    public Longitude plus(final Angle that)
    {
        // Add the values. Note that this caps between -360 and 360.
        final var result = super.plus(that);

        // Cap the result at the minimum and maximum.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    @Override
    public Longitude times(final double multiplier)
    {
        final var result = (long) (asNanodegrees() * multiplier);

        // Cap the result at the minimum and maximum.
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
