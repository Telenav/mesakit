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

package com.telenav.kivakit.navigation.routing.cost.operators;

import com.telenav.kivakit.kernel.scalars.levels.Weight;
import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.navigation.routing.cost.Cost;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;

/**
 * Weights a cost function
 *
 * @author jonathanl (shibo)
 */
public class Weighted implements CostFunction
{
    private final CostFunction costFunction;

    private final Weight weight;

    /**
     * @param costFunction The cost function
     * @param weight The weight to apply to the cost function
     */
    public Weighted(final CostFunction costFunction, final Weight weight)
    {
        this.costFunction = costFunction;
        this.weight = weight;
    }

    @Override
    public Cost cost(final Edge edge)
    {
        return this.costFunction.cost(edge).weighted(this.weight);
    }
}
