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

import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.map.geography.Location;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class VertexTest extends GraphUnitTest
{
    @Test
    public void testInEdges()
    {
        final Vertex firstAndVirginia = osmDowntownSeattleTest().vertexNearest(Location.degrees(47.611069, -122.3426071));
        ensureEqual(firstAndVirginia.inEdgeCount(), Count._3);
        ensure(firstAndVirginia.inEdges().contains(osmDowntownSeattleTestEdge(206880348000003L)));
        ensure(firstAndVirginia.inEdges().contains(osmDowntownSeattleTestEdge(693449123000003L)));
        ensure(firstAndVirginia.inEdges().contains(osmDowntownSeattleTestEdge(206877624000002L)));
    }

    @Test
    public void testOutEdges()
    {
        final Vertex firstAndVirginia = osmDowntownSeattleTest().vertexNearest(Location.degrees(47.611069, -122.3426071));
        ensureEqual(firstAndVirginia.outEdgeCount(), Count._3);
        ensure(firstAndVirginia.outEdges().contains(osmDowntownSeattleTestEdge(192005425000000L)));
        ensure(firstAndVirginia.outEdges().contains(osmDowntownSeattleTestEdge(-693449123000003L)));
        ensure(firstAndVirginia.outEdges().contains(osmDowntownSeattleTestEdge(-206877624000002L)));
    }
}
