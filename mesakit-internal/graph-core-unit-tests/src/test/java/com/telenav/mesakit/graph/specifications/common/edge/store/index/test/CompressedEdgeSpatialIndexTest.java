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

package com.telenav.mesakit.graph.specifications.common.edge.store.index.test;

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.core.test.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeSpatialIndex;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

/**
 * @author jonathanl (shibo)
 */
public class CompressedEdgeSpatialIndexTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        final Graph graph = osmDowntownSeattleTest();
        final CompressedEdgeSpatialIndex index = new CompressedEdgeSpatialIndex("test", graph, RTreeSettings.DEFAULT);
        index.bulkLoad(graph.edges().asList());
        ensureEqual(graph.edgeCount(), Count.count(index.intersecting(Rectangle.MAXIMUM)));
    }

    @Test
    public void testBug1306()
    {
        osmDowntownSeattle().edgesIntersecting(Rectangle.fromCenterAndRadius(new Location(
                Latitude.degrees(37.36242), Longitude.degrees(-121.88792)), Distance.meters(10)));
    }
}
