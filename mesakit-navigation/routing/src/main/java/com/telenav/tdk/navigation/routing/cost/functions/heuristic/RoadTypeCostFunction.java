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

package com.telenav.kivakit.navigation.routing.cost.functions.heuristic;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.map.road.model.RoadFunctionalClass;
import com.telenav.kivakit.navigation.routing.cost.Cost;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;

/**
 * A routing cost function which is based on functional class
 *
 * @author jonathanl (shibo)
 */
public class RoadTypeCostFunction implements CostFunction
{
    private final double maximum = RoadFunctionalClass.MAIN.identifier();

    @Override
    public Cost cost(final Edge edge)
    {
        switch (edge.roadType())
        {
            case FREEWAY:
            case URBAN_HIGHWAY: return Cost.of(0.0);

            case HIGHWAY: return Cost.of(0.1);
            case FRONTAGE_ROAD: return Cost.of(0.2);
            case THROUGHWAY: return Cost.of(0.5);

            case LOCAL_ROAD: return Cost.of(0.6);
            case LOW_SPEED_ROAD: return Cost.of(0.8);
            case PRIVATE_ROAD: return Cost.of(0.9);

            case FERRY:
                return Cost.of(1.5);

            default:
                return Cost.of(2.0);
        }
    }
}
