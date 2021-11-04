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

package com.telenav.mesakit.navigation.routing.cost.functions;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.navigation.routing.cost.Cost;
import com.telenav.mesakit.navigation.routing.cost.CostFunction;

/**
 * A cost function that uses or avoids toll roads
 *
 * @author jonathanl (shibo)
 */
public class TollRoadCostFunction implements CostFunction
{
    public enum TollRoadMode
    {
        USE_TOLL_ROADS,
        AVOID_TOLL_ROADS
    }

    private final TollRoadMode mode;

    public TollRoadCostFunction(TollRoadMode mode)
    {
        this.mode = mode;
    }

    @Override
    public Cost cost(Edge edge)
    {
        if (mode == TollRoadMode.AVOID_TOLL_ROADS && edge.isTollRoad())
        {
            return Cost.MAXIMUM;
        }
        return Cost.ZERO;
    }
}
