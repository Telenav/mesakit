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

package com.telenav.aonia.map.road.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The definition of deCarta road types, which are used by client and converted from txd road types by vectormap or
 * minimap.
 *
 * <pre>
 * RoadType Definition:
 * -------------------------------------------------------------------------------
 *   Value |   Description
 * -------------------------------------------------------------------------------
 * 	   1   |  Freeway
 * -------------------------------------------------------------------------------
 *     2   |  Arterial Road
 * -------------------------------------------------------------------------------
 *     3   |  Local Street
 * -------------------------------------------------------------------------------
 *     4   |  Terminal
 * -------------------------------------------------------------------------------
 *     5   |  Tunnel
 * -------------------------------------------------------------------------------
 *     8   |  Rotary Road
 * -------------------------------------------------------------------------------
 *     9   |  Ramp
 * -------------------------------------------------------------------------------
 *     10  |  Via
 * -------------------------------------------------------------------------------
 *     99  |  Sign
 * -------------------------------------------------------------------------------
 *     101 |  Intersection
 * -------------------------------------------------------------------------------
 * </pre>
 *
 * @author Jianbo chen
 */
public class DeCartaRoadType
{
    public static final DeCartaRoadType ROAD_LIMITED_ACCESS = new DeCartaRoadType(1);

    public static final DeCartaRoadType ROAD_ARTERIAL = new DeCartaRoadType(2);

    public static final DeCartaRoadType ROAD_LOCAL_STREET = new DeCartaRoadType(3);

    public static final DeCartaRoadType ROAD_TERMINAL = new DeCartaRoadType(4);

    public static final DeCartaRoadType ROAD_TUNNEL = new DeCartaRoadType(5);

    public static final DeCartaRoadType ROAD_ROTARY = new DeCartaRoadType(8);

    public static final DeCartaRoadType ROAD_RAMP = new DeCartaRoadType(9);

    public static final DeCartaRoadType ROAD_VIA = new DeCartaRoadType(10);

    public static final DeCartaRoadType ROAD_SIGN = new DeCartaRoadType(99);

    public static final DeCartaRoadType ROAD_INTERSECTION = new DeCartaRoadType(101);

    private static final Map<Integer, DeCartaRoadType> roadTypes = new HashMap<>();

    public static DeCartaRoadType forType(final int type)
    {
        return roadTypes.get(type);
    }

    private final int type;

    private DeCartaRoadType(final int type)
    {
        this.type = type;
        roadTypes.put(type, this);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (this == object)
        {
            return true;
        }

        if (object instanceof DeCartaRoadType)
        {
            return type() == ((DeCartaRoadType) object).type();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return type;
    }

    public boolean isLeastImportant()
    {
        return type == ROAD_LOCAL_STREET.type;
    }

    public boolean isMoreImportantThan(final DeCartaRoadType that)
    {
        return type < that.type;
    }

    public int type()
    {
        return type;
    }
}
