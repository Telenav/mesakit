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

import com.telenav.kivakit.kernel.language.time.Duration;
import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementMotion;

/**
 * Change in {@link Speed} per {@link Duration}, or distance per time squared. An acceleration can be negative if the
 * object is slowing down ever more rapidly.
 *
 * @author matthieun
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementMotion.class)
@LexakaiJavadoc(complete = true)
public class Acceleration
{
    public static Acceleration metersPerSecondSquared(double metersPerSecondSquared)
    {
        return new Acceleration(
                Speed.metersPerSecond((metersPerSecondSquared < 0 ? -1.0 : 1.0) * metersPerSecondSquared),
                Duration.ONE_SECOND, metersPerSecondSquared < 0);
    }

    public static Acceleration milesPerHourSquared(double milesPerHourSquared)
    {
        return new Acceleration(Speed.milesPerHour((milesPerHourSquared < 0 ? -1.0 : 1.0) * milesPerHourSquared),
                Duration.ONE_HOUR, milesPerHourSquared < 0);
    }

    @UmlAggregation(label = "change in")
    private final Speed speed;

    @UmlAggregation(label = "per")
    private final Duration duration;

    private final boolean negative;

    private Acceleration(Speed speed, Duration duration, boolean negative)
    {
        this.speed = speed;
        this.duration = duration;
        this.negative = negative;
    }

    public double asMetersPerSecondSquared()
    {
        return (negative ? -1.0 : 1.0) * speed.asMetersPerSecond() / duration.asSeconds();
    }

    public double asMilesPerHourSquared()
    {
        return (negative ? -1.0 : 1.0) * speed.asMilesPerHour() / duration.asHours();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Acceleration)
        {
            var that = (Acceleration) object;
            return speed.equals(that.speed) && duration.equals(that.duration)
                    && negative == that.negative;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return String.format("%.1f mph^2", asMilesPerHourSquared());
    }
}
