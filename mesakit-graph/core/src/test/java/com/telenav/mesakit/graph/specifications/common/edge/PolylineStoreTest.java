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

import com.telenav.kivakit.kernel.language.string.formatting.Separators;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.mesakit.graph.project.KivaKitGraphCoreUnitTest;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline.PolylineStore;
import com.telenav.mesakit.map.geography.polyline.Polyline;
import com.telenav.mesakit.map.geography.polyline.compression.differential.CompressedPolyline;
import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class PolylineStoreTest extends KivaKitGraphCoreUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Polyline.Converter polylineConverter = new Polyline.Converter(LOGGER, new Separators(":", ","));

    @Test
    public void test()
    {
        final var a = a();
        final var b = b();
        final var c = c();
        final var d = d();

        ensureEqual(a(), a.decompress());
        ensureEqual(b(), b.decompress());
        ensureEqual(c(), c.decompress());
        ensureEqual(d(), d.decompress());

        ensureEqual(a, a);
        ensureEqual(b, b);
        ensureEqual(c, c);
        ensureEqual(d, d);

        ensureNotEqual(a, b);
        ensureNotEqual(b, c);
        ensureNotEqual(c, d);
        ensureNotEqual(d, a);

        final var store = new PolylineStore("test", Estimate._4);
        ensure(store.add(a) == 1);
        ensure(store.add(b) == 2);
        ensure(store.add(c) == 3);
        ensure(store.add(d) == 4);
        //store.trim();

        ensureEqual(4, store.size());

        final var a2 = store.get(1);
        final var b2 = store.get(2);
        final var c2 = store.get(3);
        final var d2 = store.get(4);

        ensureEqual(a.decompress(), a2);
        ensureEqual(b.decompress(), b2);
        ensureEqual(c.decompress(), c2);
        ensureEqual(d.decompress(), d2);

        serializationTest(store);
    }

    private CompressedPolyline a()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("80.511852,-15.797959:-8.749402,28.202041:8.250598,-6.468993"));
    }

    private CompressedPolyline b()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("-72.226838,-17.942127:4.637036,0.057873:4.637036,0.05789:90.0,-21.879387:90.0,-21.879386"));
    }

    private CompressedPolyline c()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("24.297167,140.272313:24.297203,140.272313:24.136624,166.928427:90.0,180.0:90.0,180.0:0.0,180.0:90.0,180.0"));
    }

    private CompressedPolyline d()
    {
        return CompressedPolyline.fromLocationSequence(
                polylineConverter.convert("37.38686,-121.99797:37.387,-121.99794:37.38773,-121.99851"));
    }
}
