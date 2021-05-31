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


package com.telenav.kivakit.navigation.routing.cost.functions;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.Route;
import com.telenav.kivakit.navigation.routing.cost.EdgePermissionFunction;
import com.telenav.kivakit.navigation.routing.cost.RoutePermissionFunction;

/**
 * Determines if turns are allowed or not
 *
 * @author jonathanl (shibo)
 */
public class TurnRestrictionFunction implements RoutePermissionFunction, EdgePermissionFunction
{
    @Override
    public Permission allowed(final Edge edge)
    {
        return edge.turnRestrictions().isEmpty() ? Permission.YES : Permission.MAYBE;
    }

    /**
     * Determines if the given route can be extended by the given candidate
     */
    @Override
    public boolean allowed(final Route route)
    {
        // Get the last edge of the route
        final var last = route.last();

        // Go through each turn restriction that the last edge participates in
        for (final var relation : last.turnRestrictions())
        {
            // and if it's a "no turn" restriction
            if (relation.isNoTurnRestriction())
            {
                // and we've traveled the whole restricted route
                final var restricted = relation.asRoute();
                if (restricted != null && route.contains(restricted))
                {
                    // then the route is not legal
                    return false;
                }
            }

            // otherwise, if it's an "only" restriction
            if (relation.isOnlyTurnRestriction())
            {
                // and we have not yet traveled the whole required route
                final var only = relation.asRoute();
                if (only != null && !only.last().equals(last))
                {
                    // then the last edge must be found in the required route to be legal
                    return only.contains(last);
                }
            }
        }

        return true;
    }
}
