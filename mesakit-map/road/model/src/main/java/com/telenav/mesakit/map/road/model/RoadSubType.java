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

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.interfaces.model.Identifiable;

/**
 * Represents road subtypes (as defined in the TXD specification)
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public enum RoadSubType implements Identifiable
{
    ROUNDABOUT(0),
    MAIN_ROAD(1),
    SEPARATED_MAIN_ROAD(2),
    CONNECTING_ROAD(3),
    INTERSECTION_LINK(4),
    RAMP(5),
    SERVICE_ROAD(6),
    OPEN_TRAFFIC_AREA(7),
    FUNCTIONAL_SPECIAL_ROAD(8),
    @SuppressWarnings("SpellCheckingInspection")
    OVERBRIDGE(9),
    UNDERPASS(10),
    TUNNEL(11),
    BRIDGE(12),
    NULL(15);

    /**
     * @param identifier A road subtype identifier
     * @return The RoadSubType object for the given identifier or NULL if none exists
     */
    public static RoadSubType forIdentifier(int identifier)
    {
        switch (identifier)
        {
            case 0:
                return ROUNDABOUT;

            case 1:
                return MAIN_ROAD;

            case 2:
                return SEPARATED_MAIN_ROAD;

            case 3:
                return CONNECTING_ROAD;

            case 4:
                return INTERSECTION_LINK;

            case 5:
                return RAMP;

            case 6:
                return SERVICE_ROAD;

            case 7:
                return OPEN_TRAFFIC_AREA;

            case 8:
                return FUNCTIONAL_SPECIAL_ROAD;

            case 9:
                return OVERBRIDGE;

            case 10:
                return UNDERPASS;

            case 11:
                return TUNNEL;

            case 12:
                return BRIDGE;

            default:
                return NULL;
        }
    }

    /**
     * Returns the identifier for the given road subtype or NULL.identifier if the subtype is null
     */
    public static int identifierFor(RoadSubType subType)
    {
        if (subType == null)
        {
            return NULL.identifier;
        }
        return (int) subType.identifier();
    }

    private final int identifier;

    RoadSubType(int identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public long identifier()
    {
        return identifier;
    }

    @Override
    public long longValue()
    {
        return identifier;
    }
}
