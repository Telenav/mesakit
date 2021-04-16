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

package com.telenav.mesakit.map.measurements.project;

import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.measurements.geographic.AverageDistance;
import com.telenav.mesakit.map.measurements.geographic.Direction;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.mesakit.map.measurements.geographic.Slope;
import com.telenav.mesakit.map.measurements.geographic.Span;
import com.telenav.mesakit.map.measurements.motion.Acceleration;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.measurements.motion.speeds.AverageSpeed;
import com.telenav.mesakit.map.measurements.motion.speeds.WeightedAverageSpeed;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

/**
 * @author jonathanl (shibo)
 */
public class MapMeasurementsKryoTypes extends KryoTypes
{
    public MapMeasurementsKryoTypes()
    {
        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility of serialization, registration groups and the types
        // in each registration group must remain in the same order.
        //----------------------------------------------------------------------------------------------

        group("measurements", () ->
        {
            register(Acceleration.class);
            register(Angle.class);
            register(Area.class);
            register(AverageDistance.class);
            register(AverageSpeed.class);
            register(Direction.class);
            register(Heading.class);
            register(Slope.class);
            register(Span.class);
            register(Speed.class);
            register(WeightedAverageSpeed.class);
        });
    }
}
