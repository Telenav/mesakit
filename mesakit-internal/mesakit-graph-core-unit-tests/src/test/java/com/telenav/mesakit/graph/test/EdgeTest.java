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

package com.telenav.mesakit.graph.test;

import com.telenav.mesakit.graph.core.test.GraphUnitTest;
import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.graph.navigation.RouteLimiter;
import com.telenav.mesakit.graph.navigation.limiters.LengthRouteLimiter;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.BridgeType;
import com.telenav.mesakit.map.road.model.RoadName;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class EdgeTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        final Graph graph = osmDowntownSeattleTest();
        final Edge edge = graph.edges().iterator().next();
        ensureNotNull(edge);
    }

    @Test
    public void testAsString()
    {
        final Graph graph = osmDowntownSeattleTest();
        final Edge edge = graph.edges().iterator().next();
        ensure(edge.asString(Stringable.Format.TEXT).length() > 100);
    }

    @Test
    public void testBridgeType()
    {
        ensureEqual(osmBellevueWashington().edgeForIdentifier(365781618000000L).bridgeType(), BridgeType.BRIDGE);
    }

    @Test
    public void testContains()
    {
        ensure(osmGreenLakeSeattle().contains(new EdgeIdentifier(48003053000001L)));
        ensure(!osmGreenLakeSeattle().contains(new EdgeIdentifier(-48003053000001L)));
    }

    @Test
    public void testDeadEnd()
    {
        ensure(!osmGreenLakeSeattleEdge(457542640000005L).leadsToDeadEnd());
        ensure(osmGreenLakeSeattleEdge(737221017000000L).leadsToDeadEnd());
    }

    @Test
    public void testDoubleDigitized()
    {
        ensure(osmGreenLakeSeattleEdge(332085450000002L).osmIsDoubleDigitized());
        ensure(!osmGreenLakeSeattleEdge(784738538000000L).osmIsDoubleDigitized());
        ensure(!osmGreenLakeSeattleEdge(37644017000000L).osmIsDoubleDigitized());
    }

    @Test
    public void testFork()
    {
        ensure(osmGreenLakeSeattleEdge(457542640000005L).leadsToFork());
        ensure(!osmGreenLakeSeattleEdge(737221017000000L).leadsToFork());
    }

    @Test
    public void testInRoute()
    {
        final RouteLimiter limiter = new LengthRouteLimiter(Distance.miles(1), LengthRouteLimiter.Type.LENIENT);
        {
            final Route route = osmGreenLakeSeattleEdge(743150397000003L).inRoute(Navigator.NON_BRANCHING_NO_UTURN, limiter);
            ensureEqual(route, Route.forEdges(osmGreenLakeSeattleEdge(743150397000002L), osmGreenLakeSeattleEdge(743150397000003L)));
        }
    }

    @Test
    public void testOneWay()
    {
        ensure(osmGreenLakeSeattleEdge(52847322000000L).isOneWay());
        ensure(!osmGreenLakeSeattleEdge(52847322000000L).isTwoWay());
        ensure(!osmGreenLakeSeattleEdge(336155511000002L).isOneWay());
        ensure(osmGreenLakeSeattleEdge(336155511000002L).isTwoWay());
    }

    @Test
    public void testOutRoute()
    {
        final RouteLimiter limiter = new LengthRouteLimiter(Distance.miles(1), LengthRouteLimiter.Type.LENIENT);
        {
            final Route route = osmGreenLakeSeattleEdge(743150397000002L).outRoute(Navigator.NON_BRANCHING_NO_UTURN, limiter);
            ensureEqual(route, Route.forEdges(osmGreenLakeSeattleEdge(743150397000002L), osmGreenLakeSeattleEdge(743150397000003L)));
        }
    }

    @Test
    public void testRoadNames()
    {
        final Edge edge = osmGreenLakeSeattleEdge(48003053000001L);
        ensureEqual("Aurora Ave N", edge.roadName().toString());
        ensureEqual("WA-99", edge.roadName(RoadName.Type.ROUTE).toString());
    }

    @Test
    public void testShaped()
    {
        ensure(osmGreenLakeSeattleEdge(241540085000006L).isShaped());
        ensure(!osmGreenLakeSeattleEdge(395675862000000L).isShaped());
    }
}
