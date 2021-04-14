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

package com.telenav.aonia.map.geography.shape.rectangle;

import com.telenav.aonia.map.geography.project.lexakai.diagrams.DiagramRectangle;
import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.lexakai.annotations.UmlClassDiagram;

/**
 * A longitudinal geographic width from -360 to 360
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramRectangle.class)
public class Width extends Angle
{
    public static final Width MAXIMUM = degrees(360);

    public static final Width MINIMUM = degrees(-360);

    public static final Width ZERO = nanodegrees(0);

    public static Width angle(final Angle angle)
    {
        return nanodegrees(angle.asNanodegrees());
    }

    public static Width degrees(final double degrees)
    {
        return nanodegrees((long) (degrees * NANODEGREES_PER_DEGREE));
    }

    public static Width microdegrees(final int microdegrees)
    {
        return new Width(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Width nanodegrees(final long nanodegrees)
    {
        return new Width(nanodegrees);
    }

    protected Width(final long nanodegrees)
    {
        super(nanodegrees);
    }

    @Override
    public Width maximum()
    {
        return MAXIMUM;
    }

    @Override
    public Width minimum()
    {
        return MINIMUM;
    }

    @Override
    public Width times(final double multiplier)
    {
        return nanodegrees((long) (asNanodegrees() * multiplier));
    }

    @Override
    protected long maximumInNanoDegrees()
    {
        return 360_000_000_000L;
    }

    @Override
    protected long minimumInNanoDegrees()
    {
        return -360_000_000_000L;
    }
}
