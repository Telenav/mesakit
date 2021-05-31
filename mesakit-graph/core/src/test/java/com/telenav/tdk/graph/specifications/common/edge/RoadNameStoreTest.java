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

package com.telenav.kivakit.graph.specifications.common.edge;

import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.project.KivaKitGraphCoreUnitTest;
import com.telenav.kivakit.graph.specifications.common.edge.store.stores.roadname.RoadNameStore;
import com.telenav.kivakit.map.road.model.RoadName;
import com.telenav.kivakit.map.road.model.RoadName.Type;
import org.junit.Test;

public class RoadNameStoreTest extends KivaKitGraphCoreUnitTest
{
    @Test
    public void test()
    {
        final var graph = osmGraph();
        final var store = new RoadNameStore("test", Estimate._64, Metadata.defaultMetadata());
        final var iterations = 1_000;
        for (var i = 0; i < iterations; i++)
        {
            if (i % 3 != 0)
            {
                final Edge edge = osmEdge(graph, i, i);
                store.set(edge, Type.OFFICIAL, 0, RoadName.forName("official" + i));
                store.set(edge, Type.ALTERNATE, 0, RoadName.forName("alternate" + i));
                store.set(edge, Type.ROUTE, 0, RoadName.forName("route" + i));
                store.set(edge, Type.EXIT, 0, RoadName.forName("exit" + i));
            }
        }
        for (var i = 0; i < iterations; i++)
        {
            if (i % 3 != 0)
            {
                final Edge edge = osmEdge(graph, i, i);
                ensureEqual(RoadName.forName("official" + i), store.get(edge, Type.OFFICIAL, 0));
                ensureEqual(RoadName.forName("alternate" + i), store.get(edge, Type.ALTERNATE, 0));
                ensureEqual(RoadName.forName("route" + i), store.get(edge, Type.ROUTE, 0));
                ensureEqual(RoadName.forName("exit" + i), store.get(edge, Type.EXIT, 0));
            }
        }
    }
}
