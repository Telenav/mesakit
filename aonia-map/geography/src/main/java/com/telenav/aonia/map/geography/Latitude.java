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

package com.telenav.aonia.map.geography;

import com.telenav.aonia.map.geography.project.lexakai.diagrams.DiagramLocation;
import com.telenav.aonia.map.geography.shape.rectangle.Height;
import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.language.strings.Strings;
import com.telenav.kivakit.core.kernel.language.values.count.Range;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.regex.Pattern;

import static com.telenav.aonia.map.geography.Precision.DM5;
import static com.telenav.aonia.map.geography.Precision.DM6;
import static com.telenav.aonia.map.geography.Precision.DM7;

/**
 * Latitude implementation or a simple angle between -90 and 90.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramLocation.class)
public final class Latitude extends Angle
{
    public static final Range<Angle> RANGE;

    public static final Latitude ORIGIN = new Latitude(0);

    // See http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#X_and_Y
    // to understand why the OSM maximum latitude is 85.0511
    public static final Latitude OSM_MAXIMUM = degrees(85.0511);

    public static final Latitude MAXIMUM;

    public static final Latitude MINIMUM;

    private static final long MAXIMUM_NANODEGREES = 90_000_000_000L;

    private static final long MINIMUM_NANODEGREES = -90_000_000_000L;

    static
    {
        MAXIMUM = nanodegrees(MAXIMUM_NANODEGREES);
        MINIMUM = nanodegrees(MINIMUM_NANODEGREES);
        RANGE = new Range<>(MINIMUM, MAXIMUM);
    }

    public static Latitude angle(final Angle angle)
    {
        return new Latitude(angle.asNanodegrees());
    }

    public static Latitude degrees(final double degrees)
    {
        return nanodegrees((long) (degrees * 1_000_000_000));
    }

    public static Latitude degreesInRange(final double degrees)
    {
        return degrees(inRange(degrees));
    }

    public static Latitude degreesMinutesAndSeconds(final int degrees, final float minutes, final float seconds)
    {
        if (degrees < 0)
        {
            return degrees(degrees - minutes / 60.0 - seconds / 3600.0);
        }
        return degrees(degrees + minutes / 60.0 + seconds / 3600.0);
    }

    public static Latitude dm5(final int latitudeInDm5)
    {
        return DM5.toLatitude(latitudeInDm5);
    }

    public static Latitude dm6(final int latitudeInDm6)
    {
        return DM6.toLatitude(latitudeInDm6);
    }

    public static Latitude dm7(final int latitudeInDm7)
    {
        return DM7.toLatitude(latitudeInDm7);
    }

    public static double inRange(final double degrees)
    {
        return degrees % 90.0;
    }

    public static boolean isValid(final double latitude)
    {
        return latitude >= -90 && latitude <= 90;
    }

    public static Latitude microdegrees(final int microdegrees)
    {
        return new Latitude(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Latitude nanodegrees(final long nanodegrees)
    {
        return new Latitude(nanodegrees);
    }

    public static class DegreesConverter extends BaseStringConverter<Latitude>
    {
        public DegreesConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onConvertToObject(final String value)
        {
            return degrees(Double.parseDouble(value));
        }
    }

    public static class DegreesMinutesAndSecondsConverter extends BaseStringConverter<Latitude>
    {
        private static final Pattern pattern = Pattern
                .compile("\\+?(-?\\d+)\\s*°\\s*(\\d+\\.?\\d*)\\s*'\\s*((\\d+\\.?\\d*)\\s*\")?");

        public DegreesMinutesAndSecondsConverter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected Latitude onConvertToObject(final String string)
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

    protected Latitude()
    {
    }

    private Latitude(final long nanodegrees)
    {
        super(nanodegrees);
        assert nanodegrees >= MINIMUM_NANODEGREES && nanodegrees <= MAXIMUM_NANODEGREES : "Invalid latitude angle:  " + Angle.nanodegrees(nanodegrees);
    }

    public int as(final Precision precision)
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

    public Distance distanceTo(final Latitude that)
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

    public Latitude maximum(final Latitude that)
    {
        return asNanodegrees() > that.asNanodegrees() ? this : that;
    }

    @Override
    public Latitude minimum()
    {
        return MINIMUM;
    }

    public Latitude minimum(final Latitude that)
    {
        return asNanodegrees() < that.asNanodegrees() ? this : that;
    }

    /**
     * @param that The angle to be subtracted.
     * @return The resulting latitude or -90 if the minimum latitude has been reached.
     */
    @Override
    public Latitude minus(final Angle that)
    {
        final var result = super.minus(that);

        // Cap the values at the poles.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    /**
     * @param that The angle to be added.
     * @return The resulting latitude or 90 if the maximum latitude has been reached.
     */
    @Override
    public Latitude plus(final Angle that)
    {
        final var result = super.plus(that);

        // Cap the values at the poles.
        if (result.asNanodegrees() < MINIMUM_NANODEGREES)
        {
            return MINIMUM;
        }
        return result.asNanodegrees() > MAXIMUM_NANODEGREES ? MAXIMUM : angle(result);
    }

    @Override
    public Latitude times(final double multiplier)
    {
        final var result = (long) (asNanodegrees() * multiplier);

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
