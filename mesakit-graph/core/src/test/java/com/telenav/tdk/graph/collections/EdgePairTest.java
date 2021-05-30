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

package com.telenav.tdk.graph.collections;

import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.identifiers.EdgeIdentifier;
import com.telenav.tdk.graph.navigation.Navigator;
import com.telenav.tdk.graph.project.TdkGraphCoreUnitTest;
import com.telenav.tdk.map.measurements.Distance;
import org.junit.*;

/**
 * @author tony
 * @author jonathanl (shibo)
 */
public class EdgePairTest extends TdkGraphCoreUnitTest
{
    private EdgePair connectedEdgePair1, connectedEdgePair2, unconnectedEdgePair, nonBranchingUnconnectedEdgePair;

    @Before
    public void initialize()
    {
        final Graph graph = osmDowntownSeattleTest();

        final Edge edge1 = graph.edgeForIdentifier(new EdgeIdentifier(-759056694000002L));
        ensureNotNull(edge1);
        final Edge edge2 = graph.edgeForIdentifier(new EdgeIdentifier(-759056694000001L));
        ensureNotNull(edge2);
        final Edge edge3 = graph.edgeForIdentifier(new EdgeIdentifier(293753570000012L));
        ensureNotNull(edge3);

        connectedEdgePair1 = new EdgePair(edge1, edge2);
        connectedEdgePair2 = new EdgePair(edge1, edge3);
        unconnectedEdgePair = new EdgePair(edge2, edge3);

        final Edge i5North1 = graph.edgeForIdentifier(new EdgeIdentifier(4712853000000L));
        ensureNotNull(i5North1);
        final Edge i5North2 = graph.edgeForIdentifier(new EdgeIdentifier(15257843000000L));
        ensureNotNull(i5North2);
        nonBranchingUnconnectedEdgePair = new EdgePair(i5North1, i5North2);
    }

    @Test
    public void testAngles()
    {
        // isParallel
        ensure(connectedEdgePair1.isParallel());
        ensure(!connectedEdgePair2.isParallel());
        ensure(!unconnectedEdgePair.isParallel());

        // isPerpendicular
        ensure(!connectedEdgePair1.isPerpendicular());
        ensure(connectedEdgePair2.isPerpendicular());
        ensure(unconnectedEdgePair.isPerpendicular());
    }

    @Test
    public void testConnect()
    {
        // isConnected
        ensure(connectedEdgePair1.isConnected());
        ensure(connectedEdgePair2.isConnected());
        ensureFalse(unconnectedEdgePair.isConnected());
        ensureFalse(nonBranchingUnconnectedEdgePair.isConnected());

        // non-branching route
        final Distance distance = Distance.miles(1);
        ensure(nonBranchingUnconnectedEdgePair.isConnectedByRoute(Navigator.NON_BRANCHING, distance));
        ensureFalse(unconnectedEdgePair.isConnectedByRoute(Navigator.NON_BRANCHING, distance));
        ensure(nonBranchingUnconnectedEdgePair.isConnectedByRoute(Navigator.NON_BRANCHING, distance));
    }

    @Test
    public void testIsSameRoad()
    {
        ensure(connectedEdgePair1.isSameRoad());
        ensureFalse(connectedEdgePair2.isSameRoad());
        ensure(nonBranchingUnconnectedEdgePair.isSameRoad());
    }
}
