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

package com.telenav.mesakit.graph.tests.specifications.common.vertex;

import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class ConnectivityStoreTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        var graph = osmGraph();
        var store = new ConnectivityStore("text", graph);

        var edge1 = osmEdge(graph, 1, 1_000_000);
        var edge2 = osmEdge(graph, 2, 2_000_000);

        var vertex1 = osmVertex(graph, 1, 1, Location.TELENAV_HEADQUARTERS);
        var vertex2 = osmVertex(graph, 2, 2, Location.TELENAV_HEADQUARTERS.moved(Heading.WEST, Distance._100_METERS));
        var vertex3 = osmVertex(graph, 3, 3, Location.TELENAV_HEADQUARTERS.moved(Heading.SOUTHWEST, Distance._100_METERS));

        edge1.from(vertex1);
        edge1.to(vertex2);
        edge2.from(vertex2);
        edge2.to(vertex3);

        store.temporaryConnect(edge1, edge1.from().index(), edge1.to().index());
        store.temporaryConnect(edge2, edge2.from().index(), edge2.to().index());

        store.storeTemporaryLists(vertex1.index());
        store.storeTemporaryLists(vertex2.index());
        store.storeTemporaryLists(vertex3.index());

        ensureEqual(1, store.retrieveInEdgeCount(1));
        ensureEqual(1, store.retrieveOutEdgeCount(1));

        ensureEqual(2, store.retrieveInEdgeCount(2));
        ensureEqual(2, store.retrieveOutEdgeCount(2));

        ensureEqual(1, store.retrieveInEdgeCount(3));
        ensureEqual(1, store.retrieveOutEdgeCount(3));
    }
}
