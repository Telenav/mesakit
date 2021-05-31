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

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.navigation.routing.cost.Cost;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;

/**
 * Adds two cost functions together
 *
 * @author jonathanl (shibo)
 */
public class Plus implements CostFunction
{
    private final CostFunction a;

    private final CostFunction b;

    /**
     * @param a The first cost function
     * @param b The second cost function
     */
    public Plus(final CostFunction a, final CostFunction b)
    {
        this.a = a;
        this.b = b;
    }

    @Override
    public Cost cost(final Edge edge)
    {
        final var costA = this.a.cost(edge);
        final var costB = this.b.cost(edge);
        return costA.add(costB);
    }
}
