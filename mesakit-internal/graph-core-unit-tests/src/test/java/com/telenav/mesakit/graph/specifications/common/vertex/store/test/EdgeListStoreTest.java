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

package com.telenav.mesakit.graph.specifications.common.vertex.store.test;

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.internal.testing.Repeats;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.specifications.common.vertex.store.EdgeArrayStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class EdgeListStoreTest extends GraphUnitTest
{
    @Test
    public void testAddRetrieve()
    {
        var store = new EdgeArrayStore("test", Metadata.defaultMetadata().withEdgeCount(Count._128));

        var indexes = new ArrayList<Integer>();
        var lists = new HashMap<Integer, IntArray>();
        random().indexes(Repeats.NO_REPEATS, 65_536, index ->
        {
            indexes.add(index);

            var values = new IntArray("values");
            values.initialize();
            for (var value : random().list(Repeats.ALLOW_REPEATS, Count.count(random().randomIntExclusive(0, 20)),1, 500,Integer.class))
            {
                values.add(value);
            }

            lists.put(index, values);
            store.list(index, values.iterator());
        });

        for (var index : indexes)
        {
            var list = store.list(index);
            ensure(lists.get(index).iterator().identical(list.iterator()));
        }
    }
}
