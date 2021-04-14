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

package com.telenav.aonia.map.measurements.project;

import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Area;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.motion.Speed;
import com.telenav.kivakit.core.test.random.RandomValueFactory;
import com.telenav.lexakai.annotations.LexakaiJavadoc;

/**
 * Adds the ability to produce random map measurements to {@link RandomValueFactory}.
 *
 * @author jonathanl (shibo)
 */
@LexakaiJavadoc(complete = true)
public class MapMeasurementsRandomValueFactory extends RandomValueFactory
{
    public MapMeasurementsRandomValueFactory()
    {
    }

    public MapMeasurementsRandomValueFactory(final long seed)
    {
        super(seed);
    }

    public Angle newAngle()
    {
        return newAngle(Angle.degrees(-1 * Double.MAX_VALUE), Angle.degrees(Double.MAX_VALUE));
    }

    /**
     * @param minimum The minimum value (inclusive)
     * @param maximum The maximum value (exclusive)
     * @return An angle value greater than or equal to the minimum and less than the maximum.
     */
    public Angle newAngle(final Angle minimum, final Angle maximum)
    {
        final var difference = maximum.minus(minimum);
        return minimum.plus(difference.times(Math.random()));
    }

    public Area newArea()
    {
        return newArea(Area.MINIMUM, Area.MAXIMUM);
    }

    /**
     * @param minimum The minimum value (inclusive)
     * @param maximum The maximum value (exclusive)
     * @return An area value greater than or equal to the minimum and less than the maximum.
     */
    public Area newArea(final Area minimum, final Area maximum)
    {
        final var difference = maximum.minus(minimum);
        return minimum.plus(difference.times(Math.random()));
    }

    public Distance newDistance()
    {
        return newDistance(Distance.MINIMUM, Distance.EARTH_CIRCUMFERENCE);
    }

    /**
     * @param minimum The minimum value (inclusive)
     * @param maximum The maximum value (exclusive)
     * @return A distance value greater than or equal to the minimum and less than the maximum.
     */
    public Distance newDistance(final Distance minimum, final Distance maximum)
    {
        final var difference = maximum.minus(minimum);
        return minimum.add(difference.times(Math.random()));
    }

    public Speed newSpeed()
    {
        return Speed.milesPerHour(newDouble(0, 120));
    }
}
