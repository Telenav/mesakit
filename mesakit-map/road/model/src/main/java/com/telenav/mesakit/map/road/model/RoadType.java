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

import com.telenav.kivakit.primitive.collections.Quantizable;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Road type (as defined in the TXD specification)
 *
 * @author jonathanl (shibo)
 */
public enum RoadType implements Quantizable
{
    FREEWAY(0),
    URBAN_HIGHWAY(1),
    HIGHWAY(2),
    THROUGHWAY(3),
    LOCAL_ROAD(4),
    FRONTAGE_ROAD(5),
    LOW_SPEED_ROAD(6),
    PRIVATE_ROAD(7),
    WALKWAY(8),
    NON_NAVIGABLE(9),
    FERRY(10),
    TRAIN(11),
    PUBLIC_VEHICLE_ONLY(12),
    RESERVED_1(13),
    LAYOUT(14),
    RESERVED_2(15),
    NULL(31);

    /**
     * @return The road type for the given identifier
     */
    public static RoadType forIdentifier(int identifier)
    {
        switch (identifier)
        {
            case 0:
                return FREEWAY;

            case 1:
                return URBAN_HIGHWAY;

            case 2:
                return HIGHWAY;

            case 3:
                return THROUGHWAY;

            case 4:
                return LOCAL_ROAD;

            case 5:
                return FRONTAGE_ROAD;

            case 6:
                return LOW_SPEED_ROAD;

            case 7:
                return PRIVATE_ROAD;

            case 8:
                return WALKWAY;

            case 9:
                return NON_NAVIGABLE;

            case 10:
                return FERRY;

            case 11:
                return TRAIN;

            case 12:
                return PUBLIC_VEHICLE_ONLY;

            case 13:
                return RESERVED_1;

            case 14:
                return LAYOUT;

            case 15:
                return RESERVED_2;

            default:
                return NULL;
        }
    }

    /**
     * @return The identifier for the given road type or NULL.identifier if the type is null
     */
    public static int identifierFor(RoadType type)
    {
        if (type == null)
        {
            return NULL.identifier;
        }
        return type.identifier();
    }

    private final int identifier;

    RoadType(int identifier)
    {
        this.identifier = identifier;
    }

    public int identifier()
    {
        return identifier;
    }

    public boolean isEqualOrLessImportantThan(RoadType that)
    {
        return identifier >= that.identifier;
    }

    public boolean isEqualOrMoreImportantThan(RoadType that)
    {
        return identifier <= that.identifier;
    }

    public boolean isLessImportantThan(RoadType that)
    {
        return identifier > that.identifier;
    }

    public boolean isMoreImportantThan(RoadType that)
    {
        return identifier < that.identifier;
    }

    public RoadType maximum(RoadType that)
    {
        if (isMoreImportantThan(that))
        {
            return this;
        }
        else
        {
            return that;
        }
    }

    public Distance maximumDoubleDigitizationSeparation()
    {
        return isEqualOrMoreImportantThan(HIGHWAY) ? Distance.meters(250) : Distance.meters(100);
    }

    public RoadType nextLeastImportant()
    {
        return this == WALKWAY ? null : forIdentifier(identifier + 1);
    }

    public RoadType nextMostImportant()
    {
        return this == FREEWAY ? null : forIdentifier(identifier - 1);
    }

    @Override
    public long quantum()
    {
        return identifier;
    }
}
