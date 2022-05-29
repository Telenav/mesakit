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

package com.telenav.mesakit.graph.specifications.common.edge.test;

import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.roadname.RoadNameStore;
import org.junit.Test;

import static com.telenav.mesakit.map.road.model.RoadName.Type;
import static com.telenav.mesakit.map.road.model.RoadName.forName;

public class RoadNameStoreTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        var graph = osmGraph();
        var store = new RoadNameStore("test", Estimate._64, Metadata.defaultMetadata());
        var iterations = 1_000;
        for (var i = 0; i < iterations; i++)
        {
            if (i % 3 != 0)
            {
                Edge edge = osmEdge(graph, i, i);
                store.set(edge, Type.OFFICIAL, 0, forName("official" + i));
                store.set(edge, Type.ALTERNATE, 0, forName("alternate" + i));
                store.set(edge, Type.ROUTE, 0, forName("route" + i));
                store.set(edge, Type.EXIT, 0, forName("exit" + i));
            }
        }
        for (var i = 0; i < iterations; i++)
        {
            if (i % 3 != 0)
            {
                Edge edge = osmEdge(graph, i, i);
                ensureEqual(forName("official" + i), store.get(edge, Type.OFFICIAL, 0));
                ensureEqual(forName("alternate" + i), store.get(edge, Type.ALTERNATE, 0));
                ensureEqual(forName("route" + i), store.get(edge, Type.ROUTE, 0));
                ensureEqual(forName("exit" + i), store.get(edge, Type.EXIT, 0));
            }
        }
    }
}
