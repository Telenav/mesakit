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

package com.telenav.tdk.navigation.routing.cost.functions.heuristic;

import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.map.geography.Located;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.navigation.routing.cost.Cost;
import com.telenav.tdk.navigation.routing.cost.CostFunction;

/**
 * Cost of the remaining distance to the goal "as the crow flies"
 *
 * @author jonathanl (shibo)
 */
public class RemainingDistanceToStartCostFunction implements CostFunction
{
    private final Location start;

    private final double totalDistanceInMeters;

    public RemainingDistanceToStartCostFunction(final Located start, final Located end)
    {
        this.start = start.location();
        totalDistanceInMeters = end.location().distanceTo(start).asMeters();
    }

    @Override
    public Cost cost(final Edge edge)
    {
        return Cost.inverse(Math.min(1.0, edge.fromLocation().distanceTo(start).asMeters() / totalDistanceInMeters));
    }
}
