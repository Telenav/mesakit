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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.internal.lexakai.DiagramRectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;

/**
 * A latitudinal geographic height from -180 to 180
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramRectangle.class)
public class Height extends Angle
{
    public static final Height MAXIMUM = degrees(180);

    public static final Height MINIMUM = degrees(-180);

    public static final Height ZERO = nanodegrees(0);

    public static Height angle(Angle difference)
    {
        return nanodegrees(difference.asNanodegrees());
    }

    public static Height degrees(double degrees)
    {
        return nanodegrees((long) (degrees * NANODEGREES_PER_DEGREE));
    }

    public static Height microdegrees(int microdegrees)
    {
        return new Height(microdegrees * NANODEGREES_PER_MICRODEGREE);
    }

    public static Height nanodegrees(long nanodegrees)
    {
        return new Height(nanodegrees);
    }

    protected Height(long nanodegrees)
    {
        super(nanodegrees);
    }

    public Location asLocation()
    {
        return Location.degrees(asDegrees(), 0);
    }

    public Size asSize()
    {
        return Size.of(Width.ZERO, this);
    }

    @Override
    public Height maximum()
    {
        return MAXIMUM;
    }

    @Override
    public Height minimum()
    {
        return MINIMUM;
    }

    @Override
    public Height times(double multiplier)
    {
        return nanodegrees((long) (asNanodegrees() * multiplier));
    }

    @Override
    protected long maximumInNanoDegrees()
    {
        return 180_000_000_000L;
    }

    @Override
    protected long minimumInNanoDegrees()
    {
        return -180_000_000_000L;
    }
}
