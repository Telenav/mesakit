////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.coredata.extraction.BaseExtractor;
import com.telenav.kivakit.language.primitive.Ints;
import com.telenav.kivakit.core.language.strings.Strings;
import com.telenav.kivakit.core.language.strings.Strip;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.measurements.motion.Speed;

public class SpeedLimitExtractor extends BaseExtractor<Speed, PbfWay>
{
    public SpeedLimitExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public Speed onExtract(PbfWay way)
    {
        var maxspeed = way.tagValue("maxspeed");
        double speed = -1;
        var kph = true;
        if (maxspeed != null)
        {
            if (maxspeed.endsWith("mph"))
            {
                var value = Strip.trailing(maxspeed, "mph").trim();
                var semicolon = value.lastIndexOf(';');
                if (semicolon > 0)
                {
                    value = value.substring(semicolon + 1).trim();
                }
                var dash = value.lastIndexOf('-');
                if (dash > 0)
                {
                    value = value.substring(dash + 1).trim();
                }
                speed = Double.parseDouble(value);
                kph = false;
            }
            else
            {
                if (Strings.isNaturalNumber(maxspeed))
                {
                    speed = Ints.parseNaturalNumber(maxspeed);
                }
            }
        }

        var speedUnit = way.tagValue("speed_unit");
        if (speedUnit != null && !"K".equals(speedUnit))
        {
            kph = false;
        }

        if (speed >= 0)
        {
            return kph ? Speed.kilometersPerHour(speed) : Speed.milesPerHour(speed);
        }

        return null;
    }
}
