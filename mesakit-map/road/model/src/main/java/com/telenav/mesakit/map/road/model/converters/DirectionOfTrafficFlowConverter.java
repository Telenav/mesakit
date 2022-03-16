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

package com.telenav.mesakit.map.road.model.converters;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.road.model.DirectionOfTrafficFlow;

public class DirectionOfTrafficFlowConverter extends BaseStringConverter<DirectionOfTrafficFlow>
{
    public DirectionOfTrafficFlowConverter(Listener listener)
    {
        super(listener);
    }

    @Override
    protected DirectionOfTrafficFlow onToValue(String value)
    {
        String direction = null;
        if (value != null)
        {
            // A direction flow(DF) field can contain the below formatted string.
            // DF|DF%VT=xx,xx,xx%VP=xxxxxxxx%AL=x%NP=x%23=x. For OSM data, the format is just
            // "DF|DF". Anyway, we just use "DF|DF" part to get the flow direction.
            var index = value.indexOf("%");
            if (index > 0)
            {
                // In TomTom case
                direction = value.substring(0, index);
            }
            else
            {
                // In OSM case
                direction = value;
            }
        }
        return direction == null ? null : directionOfTrafficFlow(direction);
    }

    /*
     * See TXD spec where direction is specified as a number and is possibly repeated with
     * additional characters after a separator (including "|" or "%").
     */
    private DirectionOfTrafficFlow directionOfTrafficFlow(String direction)
    {
        var index = direction.indexOf('|');
        if (index >= 0)
        {
            direction = direction.substring(0, index);
        }
        index = direction.indexOf('%');
        if (index >= 0)
        {
            direction = direction.substring(0, index);
        }
        switch (direction)
        {
            case "0":
                return DirectionOfTrafficFlow.CLOSED;
            case "1":
                return DirectionOfTrafficFlow.POSITIVE;
            case "2":
                return DirectionOfTrafficFlow.NEGATIVE;
            case "3":
                return DirectionOfTrafficFlow.BOTH;
            default:
                warning("DirectionOfTrafficFlow token from [$] is not recognized", direction);
                return DirectionOfTrafficFlow.UNKNOWN;
        }
    }
}
