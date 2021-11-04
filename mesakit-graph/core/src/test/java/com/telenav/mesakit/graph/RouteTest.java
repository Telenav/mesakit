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

package com.telenav.mesakit.graph;

import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.ComplexTurnClassifier;
import com.telenav.mesakit.graph.analytics.classification.classifiers.turn.TurnType;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.project.GraphUnitTest;
import com.telenav.mesakit.graph.relations.restrictions.classifiers.TurnRestrictionsTurnClassifier;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tony
 * @author jonathanl (shibo)
 */
public class RouteTest extends GraphUnitTest
{
    // Four sequentially connected edges (edge1 => edge2 => edge3 => edge4) and one unconnected edge
    private Edge edge1, edge2, edge3, edge4, unconnectedEdge;

    // The vertexes from edge1 to edge4 in the order they appear driving that route
    private final List<Vertex> vertexes = new ArrayList<>();

    private Vertex start;

    private Vertex end;

    @Before
    public void initialize()
    {
        // Initialize edge variables from graph data
        edge1 = osmDowntownSeattleTestEdge(6522905000006L);
        edge2 = osmDowntownSeattleTestEdge(428243941000000L);
        edge3 = osmDowntownSeattleTestEdge(428243941000001L);
        edge4 = osmDowntownSeattleTestEdge(428243940000000L);
        unconnectedEdge = osmDowntownSeattleTestEdge(428243942000000L);

        start = edge1.oppositeVertex(edge1.vertexConnecting(edge2));
        end = edge4.oppositeVertex(edge3.vertexConnecting(edge4));
        vertexes.add(start);
        vertexes.add(edge1.vertexConnecting(edge2));
        vertexes.add(edge2.vertexConnecting(edge3));
        vertexes.add(edge3.vertexConnecting(edge4));
        vertexes.add(end);
    }

    @Ignore
    public void testAppend()
    {
        // Both (1 => 2) + 3 and 1 + (2 => 3) should construct 1 => 2 => 3
        Route route1 = Route.forEdges(edge1, edge2).append(edge3);
        Route route2 = Route.fromEdge(edge1).append(Route.forEdges(edge2, edge3));
        ensureEqual(route1, route2);

        // But 2 => 3 should not be equal to 1 => 2 => 3
        Route route3 = Route.fromEdge(edge2).append(edge3);
        ensureNotEqual(route1, route3);

        // And 1 with 2 => 3 should equal 1 => 2 => 3
        Route route4 = Route.fromEdge(edge1).append(route3);
        ensureEqual(route1, route4);

        try
        {
            // We should not be able to make the route 1 => unconnectedEdge
            Route.fromEdge(edge1).append(unconnectedEdge);
            fail("Should have caused an exception");
        }
        catch (AssertionError ignored)
        {
        }
    }

    @Test
    public void testComplexTurnType()
    {
        ComplexTurnClassifier classifier = ComplexTurnClassifier.DEFAULT;
        ensure(classifier.type(osmDowntownSeattleTestRoute(450919738000001L, 450919738000002L, 428243940000000L)) == TurnType.LEFT);
        ensure(classifier.type(osmDowntownSeattleTestRoute(240874273000001L, 240874273000002L, 484863624000000L)) == TurnType.RIGHT);
    }

    @Test
    public void testConcatenation()
    {
        // 1 + (2 => 3 => 4) => 1, 2, 3, 4
        Route route1 = Route.fromEdge(edge1).append(Route.forEdges(edge2, edge3, edge4));

        // (1 => 2 => 3) + 4 => 1, 2, 3, 4
        Route route2 = Route.forEdges(edge1, edge2, edge3).append(Route.fromEdge(edge4));

        ensureEqual(route1, route2);

        // Check start and end
        ensureEqual(start, route1.start());
        ensureEqual(start, route2.start());
        ensureEqual(end, route1.end());
        ensureEqual(end, route2.end());

        // Check first and last
        ensureEqual(edge1, route1.first());
        ensureEqual(edge1, route2.first());
        ensureEqual(edge4, route1.last());
        ensureEqual(edge4, route2.last());

        // Check total length
        Distance length = Distance.ZERO;
        for (Edge edge : route1)
        {
            length = length.add(edge.length());
        }
        ensureEqual(length, route1.length());
        ensureEqual(length, route2.length());

        // Check size
        final int size = 4;
        ensureEqual(size, route1.size());
        ensureEqual(size, route2.size());

        // 1 => 2 => 3 => 4 without the last edge should be 1 => 2 => 3
        ensureEqual(Route.forEdges(edge1, edge2, edge3), route1.withoutLast());
        ensureEqual(Route.forEdges(edge1, edge2, edge3), route2.withoutLast());
    }

    @Ignore
    public void testDisconnected()
    {
        try
        {
            Route.forEdges(osmDowntownSeattleTestEdge(1650860270008L), osmDowntownSeattleTestEdge(-1650860270008L),
                    osmDowntownSeattleTestEdge(65113930001L));
            fail("Should have thrown exception");
        }
        catch (AssertionError ignored)
        {
        }
    }

    @Ignore
    public void testDuplicate()
    {
        try
        {
            Route.forEdges(edge1, edge2).append(Route.fromEdge(edge2));
            fail("Should have thrown exception");
        }
        catch (AssertionError ignored)
        {
        }
    }

    @Ignore
    public void testEquals()
    {
        Graph graph = osmDowntownSeattleTest();

        // We shouldn't be able to look up invalid edge identifiers
        try
        {
            graph.edgeForIdentifier(new EdgeIdentifier(-111111111));
            fail("Should have thrown exception");
        }
        catch (AssertionError ignored)
        {
            // expected
        }

        // 1 == 1
        ensureEqual(Route.fromEdge(edge1), Route.fromEdge(edge1));

        // 1 != 2
        ensureNotEqual(Route.fromEdge(edge1), Route.fromEdge(edge2));

        // (1 => 2) != (2 => 3)
        ensureNotEqual(Route.forEdges(edge1, edge2), Route.forEdges(edge2, edge3));

        // (1 => 2) == (1 => 2)
        ensureEqual(Route.forEdges(edge1, edge2), Route.forEdges(edge1, edge2));
    }

    @Ignore
    public void testGet()
    {
        // 1 => 2 => 3 => 4
        Route route = Route.forEdges(edge1, edge2, edge3, edge4);

        ensureEqual(edge1, route.get(0));
        ensureEqual(edge2, route.get(1));
        ensureEqual(edge3, route.get(2));
        ensureEqual(edge4, route.get(3));
        try
        {
            route.get(-1);
            fail("Should not have worked");
        }
        catch (AssertionError ignored)
        {
        }
        try
        {
            route.get(4);
            fail("Should not have worked");
        }
        catch (AssertionError ignored)
        {
        }
    }

    @Test
    public void testOverlap()
    {

        // 1 => 2
        Route route1 = Route.fromEdge(edge1).append(edge2);

        // 2 => 3
        Route route2 = Route.fromEdge(edge2).append(edge3);

        // 4
        Route route3 = Route.fromEdge(edge4);

        // Overlaps at edge 2
        ensure(route1.overlaps(route2));

        // No overlap
        ensureFalse(route1.overlaps(route3));
    }

    @Test
    public void testPrepend()
    {
        Route route1 = Route.fromEdge(edge2).prepend(edge1);
        Route route2 = Route.fromEdge(edge1).append(edge2);
        ensureEqual(route1, route2);
    }

    @Test
    public void testTurnType()
    {
        TurnRestrictionsTurnClassifier classifier = new TurnRestrictionsTurnClassifier();
        ensure(classifier.type(osmDowntownSeattleTestRoute(450919738000001L, 450919738000002L, 428243940000000L)) == TurnType.LEFT);
        ensure(classifier.type(osmDowntownSeattleTestRoute(240874273000001L, 240874273000002L, 484863624000000L)) == TurnType.RIGHT);
    }

    @Test
    public void testVertexes()
    {
        // 1 => 2 => 3 => 4
        Route route = Route.forEdges(edge1, edge2, edge3, edge4);

        // Should get vertexes in order
        int i = 0;
        for (Vertex vertex : route.vertexes())
        {
            ensureEqual(vertexes.get(i++), vertex);
        }
    }

    @Test
    public void testWithout()
    {
        ensureEqual(Route.forEdges(edge1, edge2),
                Route.forEdges(edge1, edge2, edge3).withoutLast());
        ensureEqual(Route.forEdges(edge2, edge3),
                Route.forEdges(edge1, edge2, edge3).withoutFirst());
    }
}
