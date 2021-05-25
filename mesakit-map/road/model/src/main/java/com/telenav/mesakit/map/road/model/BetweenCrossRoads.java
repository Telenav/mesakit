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

import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.objects.Objects;

/**
 * Identifies a segment of a given road between two cross streets. For example, I-5 between Mercer and Stewart.
 *
 * @author jonathanl (shibo)
 */
public class BetweenCrossRoads
{
    public static BetweenCrossRoads newInstance(final RoadName mainRoad, final RoadName firstCrossStreet,
                                                final RoadName secondCrossStreet)
    {
        if (mainRoad != null)
        {
            return new BetweenCrossRoads(mainRoad, firstCrossStreet, secondCrossStreet);
        }
        return null;
    }

    private final RoadName mainRoad;

    private final RoadName firstCrossStreet;

    private final RoadName secondCrossStreet;

    private BetweenCrossRoads(final RoadName mainRoad, final RoadName firstCrossStreet,
                              final RoadName secondCrossStreet)
    {
        this.mainRoad = mainRoad;
        this.firstCrossStreet = firstCrossStreet;
        this.secondCrossStreet = secondCrossStreet;
    }

    public Intersection asIntersection()
    {
        if (mainRoad != null)
        {
            if (firstCrossStreet != null)
            {
                return Intersection.forStreets(mainRoad, firstCrossStreet);
            }
            if (secondCrossStreet != null)
            {
                return Intersection.forStreets(mainRoad, secondCrossStreet);
            }
        }
        return null;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof BetweenCrossRoads)
        {
            final var that = (BetweenCrossRoads) object;
            return Objects.equal(mainRoad, that.mainRoad)
                    && Objects.equal(firstCrossStreet, that.firstCrossStreet)
                    && Objects.equal(secondCrossStreet, that.secondCrossStreet);
        }
        return false;
    }

    public RoadName getFirstCrossStreet()
    {
        return firstCrossStreet;
    }

    public RoadName getMainRoad()
    {
        return mainRoad;
    }

    public RoadName getSecondCrossStreet()
    {
        return secondCrossStreet;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(mainRoad, firstCrossStreet, secondCrossStreet);
    }
}
