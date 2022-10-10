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

package com.telenav.mesakit.navigation.routing.debuggers;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.world.WorldEdge;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.navigation.routing.RoutingDebugger;
import com.telenav.mesakit.navigation.routing.RoutingRequest;
import com.telenav.mesakit.navigation.routing.RoutingResponse;
import com.telenav.mesakit.navigation.routing.cost.Cost;

public class ConsoleRoutingDebugger implements RoutingDebugger
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Override
    public void onEnd(RoutingRequest request, RoutingResponse response)
    {
        ConsoleRoutingDebugger.LOGGER.information("Completed $ from $ to $", request.description(), request.start(), request.end());
    }

    @Override
    public void onRelaxed(Route route, Cost cost)
    {
        ConsoleRoutingDebugger.LOGGER.information("${nowrap}Relaxed (cost $) $ at end of $", cost, route.last(), simplified(route));
    }

    @Override
    public void onSettled(Vertex vertex, Cost cost)
    {
        ConsoleRoutingDebugger.LOGGER.information("${nowrap}Settled (cost $) $", cost, vertex);
    }

    @Override
    public void onStart(RoutingRequest request)
    {
        ConsoleRoutingDebugger.LOGGER.information("Starting $ from $ to $", request.description(), request.start(), request.end());
    }

    /**
     * Returns true if the given route spans more than one world cell
     */
    private boolean oneCell(Route route)
    {
        RegionIdentifier lastCell = null;
        for (var edge : route)
        {
            var worldEdge = (WorldEdge) edge;
            if (lastCell != null && !lastCell.equals(worldEdge.worldCell().identifier()))
            {
                return false;
            }
            lastCell = worldEdge.worldCell().identifier();
        }
        return true;
    }

    private String simplified(Route route)
    {
        // If the route is a world edge route
        if (route.first() instanceof WorldEdge)
        {
            // If the route is all within one cell
            if (oneCell(route))
            {
                // return a simplified route string without the cell identifiers
                var edges = new StringList();
                for (var edge : route)
                {
                    edges.add(Long.toString(edge.identifier().asLong()));
                }
                return edges.join(":");
            }
        }

        // The route spans cells so we must show cell identifiers
        return route.toString();
    }
}
