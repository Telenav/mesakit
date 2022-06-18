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

import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.map.geography.Location;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
public class EdgeSetTest extends GraphUnitTest
{
    // Four sequentially connected edges (edge1 => edge2 => edge3 => edge4) and one unconnected edge
    private Edge edge1, edge2, edge3, parallelEdge, nonparallelEdge;

    @Before
    public void initialize()
    {
        // Initialize edge variables from graph data
        edge1 = osmDowntownSeattleTestEdge(371427803000000L);
        edge2 = osmDowntownSeattleTestEdge(371427803000001L);
        edge3 = osmDowntownSeattleTestEdge(371427803000002L);
        parallelEdge = osmDowntownSeattleTestEdge(550940543000003L);
        nonparallelEdge = osmDowntownSeattleTestEdge(636926809000001L);
    }

    @Test
    public void test()
    {
        final Graph graph = osmDowntownSeattleTest();
        final Iterator<Edge> edges = graph.edges().iterator();

        final EdgeSet a = new EdgeSet();
        a.add(edges.next());
        a.add(edges.next());
        a.add(edges.next());

        final EdgeSet b = new EdgeSet();
        b.addAll(a);

        ensureEqual(a, b);
    }

    @Test
    public void testMatching()
    {
        final Graph graph = osmDowntownSeattle();
        final Vertex center = graph.vertexNearest(Location.degrees(47.611069, -122.3426071));
        final EdgeSet allEdges = center.edges();
        ensureEqual(6, allEdges.size());
        ensureEqual(3, allEdges.inEdges(center).size());
        ensureEqual(3, allEdges.outEdges(center).size());
        ensureEqual(1, allEdges.oneWayInEdges(center).size());
        ensureEqual(1, allEdges.oneWayOutEdges(center).size());
    }

    @Test
    public void testParallel()
    {
        final EdgeSet set1 = EdgeSet.singleton(edge1);
        ensureEqual(edge1, set1.parallelTo(parallelEdge));
        ensureEqual(null, set1.parallelTo(nonparallelEdge));

        // should return edge with smaller diff angle
        final List<Edge> edgeList = new ArrayList<>();
        edgeList.add(edge3);
        edgeList.add(edge2);
        final EdgeSet set2 = EdgeSet.forCollection(Maximum._3, edgeList);

        ensureNotEqual(null, set2.parallelTo(parallelEdge));
        ensureEqual(null, set2.parallelTo(nonparallelEdge));
    }

    @Test
    public void testSort()
    {
        final EdgeSet set = new EdgeSet();
        set.add(edge1);
        set.add(edge3);
        set.add(edge2);
        final List<Edge> sorted = set.asSortedList();
        ensureEqual(edge1, sorted.get(0));
        ensureEqual(edge2, sorted.get(1));
        ensureEqual(edge3, sorted.get(2));
    }

    @Test
    public void testUnion()
    {
        final EdgeSet set1 = EdgeSet.singleton(edge1).union(EdgeSet.singleton(edge2));
        final List<Edge> edgeList = new ArrayList<>();
        edgeList.add(edge1);
        edgeList.add(edge2);
        final EdgeSet set2 = EdgeSet.forCollection(Maximum._2, edgeList);

        // we can't use assertEquals(set1,set2) here, why?
        ensure(set1.containsAll(set2));
        ensure(set2.containsAll(set1));

        // same as above
        final EdgeSet set3 = set1.union(EdgeSet.singleton(edge1));
        ensure(set1.containsAll(set3));
        ensure(set3.containsAll(set1));
    }

    @Test
    public void testWithout()
    {
        final EdgeSet set1 = EdgeSet.singleton(edge1);
        final EdgeSet set2 = EdgeSet.singleton(edge2);
        final List<Edge> edgeList = new ArrayList<>();
        edgeList.add(edge1);
        edgeList.add(edge2);
        final EdgeSet set3 = EdgeSet.forCollection(Maximum._2, edgeList);

        // we can't use assertEquals(set1,set2) here, why?
        ensure(set3.without(set1).containsAll(set2));
        ensure(set2.containsAll(set3.without(set1)));
    }
}
