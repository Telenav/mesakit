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

import com.telenav.kivakit.kernel.language.time.PreciseDuration;
import com.telenav.kivakit.kernel.language.values.level.Weight;
import com.telenav.mesakit.graph.world.project.WorldGraphUnitTest;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.navigation.routing.cost.functions.heuristic.RemainingDistanceToEndCostFunction;
import com.telenav.mesakit.navigation.routing.cost.functions.heuristic.SpeedCostFunction;
import com.telenav.mesakit.navigation.routing.debuggers.SwingRoutingDebugger;
import com.telenav.mesakit.navigation.routing.dijkstra.DijkstraRouter;
import com.telenav.mesakit.navigation.routing.dijkstra.DijkstraRoutingRequest;
import com.telenav.mesakit.navigation.routing.limiters.CpuTimeRoutingLimiter;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class WorldGraphRoutingTest extends WorldGraphUnitTest
{
    private static final boolean SWING_DEBUG = false;

    @Test
    public void testRouting()
    {
        final var from = osmGraph().vertexNearest(Location.degrees(34.9559601, -79.9980919));
        final var to = osmGraph().vertexNearest(Location.degrees(34.9453363, -80.0146624));
        final var router = new DijkstraRouter(new RemainingDistanceToEndCostFunction(from.location(), to.location())
                .weightedSum(Weight.weight(0.75), new SpeedCostFunction()));
        final RoutingRequest request = new DijkstraRoutingRequest(from, to)
                .withLimiter(new CpuTimeRoutingLimiter(PreciseDuration.seconds(3)))
                .withDebugger(SWING_DEBUG ? new SwingRoutingDebugger("test routing") : RoutingDebugger.NULL);
        try
        {
            final var result = router.findRoute(request);
            ensureEqual(
                    "cell-24-20--16398033000000:cell-24-20--16399176000002:cell-24-20--16399176000001:cell-24-20--16399176000000:cell-24-19--16399176000000:cell-24-19--16399995000001:cell-24-19--16399995000000:cell-23-19--16399995000002:cell-23-19--16399995000001:cell-23-19-16399369000000",
                    result.route().toString());
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            fail("failed");
        }
    }
}
