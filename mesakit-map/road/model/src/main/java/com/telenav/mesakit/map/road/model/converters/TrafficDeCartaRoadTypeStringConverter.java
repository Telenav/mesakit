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

import com.telenav.mesakit.map.road.model.DeCartaRoadType;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.messaging.Listener;

import java.util.HashMap;
import java.util.Map;

// TODO Temporary version only to build Robert CallStack Mar 2, 2013
public class TrafficDeCartaRoadTypeStringConverter extends BaseStringConverter<DeCartaRoadType>
{
    public static final Map<String, DeCartaRoadType> roadTypeMap = new HashMap<>();

    static
    {
        // key: functional class, road type, road sub type
        roadTypeMap.put(("*,0,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,0,1"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,0,2"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,0,3"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,0,4"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,0,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,0,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,0,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,0,9"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,0,10"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,0,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,0,12"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,1,1"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,2"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,3"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,1,4"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,1,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,1,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,1,9"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,10"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,1,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,1,12"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,2,*"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,2,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,2,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,2,2"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("*,2,3"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,2,3"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,2,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,2,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,2,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,2,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("1,2,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,2,12"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,2,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,3,*"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,3,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,3,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,3,3"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,3,3"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,3,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,3,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,3,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,3,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("1,3,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,3,12"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,3,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,4,*"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,4,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,4,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,4,3"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,4,3"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,4,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,4,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,4,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,4,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("1,4,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,4,12"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,4,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("1,5,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,5,3"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,4"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,5,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,5,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,5,9"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,5,9"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,10"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,5,10"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,5,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("1,5,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,5,12"), DeCartaRoadType.ROAD_ARTERIAL);
        roadTypeMap.put(("1,5,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,6,1"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,2"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,3"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,4"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,6,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,6,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,6,9"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,10"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,6,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,6,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("1,6,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("1,6,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("1,6,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("1,6,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("1,6,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,7,*"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,7,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,7,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,7,12"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,8,*"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,8,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,8,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,8,12"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,9,*"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,9,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,9,12"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,10,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,11,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,12,*"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("5,12,2"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,12,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,12,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,12,12"), DeCartaRoadType.ROAD_LOCAL_STREET);
        roadTypeMap.put(("*,13,0"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,13,1"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,13,2"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,13,3"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,13,4"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,13,5"), DeCartaRoadType.ROAD_RAMP);
        roadTypeMap.put(("*,13,6"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,13,7"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,13,9"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,13,10"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,13,11"), DeCartaRoadType.ROAD_TUNNEL);
        roadTypeMap.put(("*,13,12"), DeCartaRoadType.ROAD_LIMITED_ACCESS);
        roadTypeMap.put(("*,14,*"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,15,*"), DeCartaRoadType.ROAD_TERMINAL);
        roadTypeMap.put(("*,0,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,1,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,2,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,3,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,4,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,5,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,6,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,7,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,8,8"), DeCartaRoadType.ROAD_ROTARY);
        roadTypeMap.put(("*,13,8"), DeCartaRoadType.ROAD_ROTARY);
    }

    public TrafficDeCartaRoadTypeStringConverter(final Listener listener)
    {
        super(listener);
    }

    @Override
    protected DeCartaRoadType onToValue(final String value)
    {
        var indexOfFirstSeparator = 0;
        var indexOfLastSeparator = 0;
        var roadType = roadTypeMap.get(value);
        if (roadType == null)
        {
            indexOfFirstSeparator = value.indexOf(",");
            roadType = roadTypeMap.get("*" + value.substring(indexOfFirstSeparator));
        }
        if (roadType == null)
        {
            indexOfLastSeparator = value.lastIndexOf(",");
            roadType = roadTypeMap.get(value.substring(0, indexOfLastSeparator + 1) + "*");
        }
        if (roadType == null)
        {
            roadType = roadTypeMap.get("*" + value.substring(indexOfFirstSeparator, indexOfLastSeparator + 1) + "*");
        }
        return roadType;
    }
}
