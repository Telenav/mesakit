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

package com.telenav.mesakit.graph.specifications.common.edge;

import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.project.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline.SplitPolylineStore;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.compression.differential.CompressedPolyline;
import org.junit.Test;

public class SplitPolylineStoreTest extends GraphUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Polyline.Converter polylineConverter = new Polyline.Converter(LOGGER, new Separators(":", ","));

    @Test
    public void test()
    {
        var a = a();
        var b = b();
        var c = c();

        ensureEqual(a, a);
        ensureEqual(b, b);
        ensureEqual(c, c);

        var graph = osmGraph();
        var store = new SplitPolylineStore("test",
                Maximum._1024, Maximum._1024, Estimate._16, Estimate._16);

        Edge edge3 = osmEdge(graph, 3, 3);
        Edge edge777 = osmEdge(graph, 777, 777);
        Edge edge55 = osmEdge(graph, 55, 55);
        Edge edge333 = osmEdge(graph, 333, 333);
        Edge edge666 = osmEdge(graph, 666, 666);

        store.set(edge3, a);
        store.set(edge777, b);
        store.set(edge333, c);

        Polyline a2 = store.get(edge3);
        Polyline b2 = store.get(edge777);
        Polyline c2 = store.get(edge55);
        Polyline d2 = store.get(edge333);

        ensureEqual(null, store.get(edge666));

        ensureEqual(a.decompress(), a2);
        ensureEqual(b.decompress(), b2);
        ensureEqual(c.decompress(), d2);
    }

    private CompressedPolyline a()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("80.511852,-151.797959:81.749402,-28.202041:8.250598,-20.468993"));
    }

    private CompressedPolyline b()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("-7.226838,-17.942127:4.637036,0.057873:4.637036,0.05789:9.0,-21.879387:9.0,-21.879386"));
    }

    private CompressedPolyline c()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("37.38686,-121.99797:37.387,-121.99794:37.38773,-121.99851"));
    }
}
