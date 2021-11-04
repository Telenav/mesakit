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

package com.telenav.mesakit.navigation.routing;

import com.telenav.kivakit.kernel.language.values.level.Weight;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.project.GraphUnitTest;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.navigation.routing.bidijkstra.BiDijkstraRouter;
import com.telenav.mesakit.navigation.routing.bidijkstra.BiDijkstraRoutingRequest;
import com.telenav.mesakit.navigation.routing.cost.CostFunction;
import com.telenav.mesakit.navigation.routing.cost.functions.TravelTimeCostFunction;
import com.telenav.mesakit.navigation.routing.cost.functions.heuristic.RemainingDistanceToEndCostFunction;
import com.telenav.mesakit.navigation.routing.cost.functions.heuristic.RoadTypeCostFunction;
import org.junit.Before;
import org.junit.Test;

public class BiDijkstraRouterTest extends GraphUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    // private static RoutingDebugger DEBUGGER = new ConsoleRoutingDebugger();
    // private static RoutingDebugger DEBUGGER = new SwingRoutingDebugger();
    private static final RoutingDebugger DEBUGGER = RoutingDebugger.NULL;

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setup()
    {
    }

    @Test
    public void testDowntownSeattleAStarRouting()
    {
        for (var i = 0; i < 3; i++)
        {
            var start = osmDowntownSeattleTestEdge(6348968000003L);
            var end = osmDowntownSeattleTestEdge(6415868000005L);
            var weight = Weight.weight(i / 3.0);
            weightedRoute(weight, start.to(), end.to());
        }
    }

    @Test
    public void testDowntownSeattleBackwardsRouting()
    {
        for (var i = 0; i < 3; i++)
        {
            var start = osmDowntownSeattleTestEdge(6348968000003L);
            var end = osmDowntownSeattleTestEdge(6415868000005L);
            fixedSpeedRoute(start.to(), end.to());
        }
    }

    @Test
    public void testDowntownSeattleTravelTimeRouting()
    {
        var start = osmDowntownSeattleTestEdge(6348968000003L);
        var end = osmDowntownSeattleTestEdge(6415868000005L);
        fixedSpeedRoute(start.to(), end.to());
    }

    @Test
    public void testGreenLakeAStarRouting()
    {
        var start = osmGreenLakeSeattleEdge(6366507000001L);
        var end = osmGreenLakeSeattleEdge(4794181000017L);
        var weight = Weight.weight(99 / 100.0);
        weightedRoute(weight, start.to(), end.from());
    }

    @Test
    public void testGreenLakeTravelTimeRouting()
    {
        var start = osmGreenLakeSeattleEdge(6366507000001L);
        var end = osmGreenLakeSeattleEdge(4794181000017L);
        fixedSpeedRoute(start.to(), end.from());
    }

    @Test
    public void testOneEdgeRouting()
    {
        var edge = osmGreenLakeSeattleEdge(6366507000001L);
        ensure(edge.isTwoWay());
        ensureEqual(route(edge), fixedSpeedRoute(edge.from(), edge.to()));
        ensureEqual(route(edge.reversed()), fixedSpeedRoute(edge.to(), edge.from()));
    }

    private Route fixedSpeedRoute(Vertex start, Vertex end)
    {
        return route(start, end, new TravelTimeCostFunction(Speed.SIXTY_FIVE_MILES_PER_HOUR,
                start.location(), end.location()));
    }

    private Route route(Vertex start, Vertex end, CostFunction costFunction)
    {
        RoutingRequest request = new BiDijkstraRoutingRequest(start, end).withDebugger(DEBUGGER);
        var routeResult = new BiDijkstraRouter(costFunction).findRoute(request);
        DEBUG.trace("Found route ${debug}", routeResult);
        return routeResult.route();
    }

    private void weightedRoute(Weight weight, Vertex start, Vertex end)
    {
        route(start, end, new RemainingDistanceToEndCostFunction(start.location(), end.location())
                .weightedSum(weight, new RoadTypeCostFunction()));
    }
}
