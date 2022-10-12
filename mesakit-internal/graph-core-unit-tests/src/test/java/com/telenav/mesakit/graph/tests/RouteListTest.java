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

package com.telenav.mesakit.graph.tests;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.collections.RouteList;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import org.junit.Before;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class RouteListTest extends GraphUnitTest
{
    // Four sequentially connected edges (edge1 => edge2 => edge3 => edge4) and one unconnected edge
    private Edge edge1, edge2, edge3, edge4, unconnectedEdge;

    @Before
    public void initialize()
    {
        // Initialize edge variables from graph data
        edge1 = osmDowntownSeattleTestEdge(6522905000006L);
        edge2 = osmDowntownSeattleTestEdge(428243941000000L);
        edge3 = osmDowntownSeattleTestEdge(428243941000001L);
        edge4 = osmDowntownSeattleTestEdge(428243940000000L);
        unconnectedEdge = osmDowntownSeattleTestEdge(428243942000000L);
    }

    @Test
    public void testAsRoutes()
    {
        RouteList routes = new RouteList();
        routes.add(Route.forEdges(edge3, edge4));
        routes.add(Route.forEdges(edge1, edge2));

        Route route = routes.asRoute();
        ensureNotNull(route);
        ensureEqual(4, route.size());
        ensureEqual(edge1, route.first());
        ensureEqual(edge4, route.last());

        routes.clear();
        routes.add(Route.fromEdge(edge4));
        routes.add(Route.fromEdge(edge2));
        routes.add(Route.fromEdge(edge1));
        routes.add(Route.fromEdge(edge3));

        route = routes.asRoute();
        ensureNotNull(route);
        ensureEqual(4, route.size());
        ensureEqual(edge1, route.first());
        ensureEqual(edge4, route.last());

        routes.clear();
        routes.add(Route.forEdges(edge1, edge2, edge3));
        routes.add(Route.forEdges(unconnectedEdge));

        route = routes.asRoute();
        ensureNotNull(route);
        ensureEqual(3, route.size());
        ensureEqual(edge1, route.first());
        ensureEqual(edge3, route.last());
    }
}
