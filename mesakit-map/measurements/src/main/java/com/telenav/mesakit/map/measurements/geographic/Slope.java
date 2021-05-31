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

import com.telenav.lexakai.annotations.LexakaiJavadoc;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.measurements.project.lexakai.diagrams.DiagramMapMeasurementGeographic;

/**
 * A slope angle, such as for the steepness of the rise or fall of a roadway. Supports basic mathematical operations and
 * supplies a {@link #quantum()} in integral degrees.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramMapMeasurementGeographic.class)
@LexakaiJavadoc(complete = true)
public class Slope extends Angle
{
    /**
     * @return The slope for the number of degrees
     */
    public static Slope degrees(final double degrees)
    {
        return new Slope((long) (degrees * NANODEGREES_PER_DEGREE));
    }

    /**
     * @return A slope of the given angle of steepness
     */
    public static Slope of(final Angle angle)
    {
        return new Slope(angle);
    }

    private Slope(final Angle angle)
    {
        super(angle.nanodegrees);
    }

    private Slope(final long nanoDegrees)
    {
        super(nanoDegrees);
    }

    public Slope difference(final Slope that)
    {
        return of(super.difference(that, Chirality.SMALLEST));
    }

    @Override
    public Slope reversed()
    {
        return (Slope) super.reversed();
    }

    @Override
    public Slope minus(final Angle that)
    {
        return of(super.minus(that));
    }

    @Override
    public Slope plus(final Angle that)
    {
        return of(super.plus(that));
    }

    /**
     * @return The quantum of a slope is integral degrees
     */
    @Override
    public long quantum()
    {
        return (long) asDegrees();
    }

    @Override
    public Slope times(final double multiplier)
    {
        return of(super.times(multiplier));
    }
}
