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

package com.telenav.mesakit.graph.specifications.common.vertex.store;

import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.project.GraphUnitTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

public class EdgeListStoreTest extends GraphUnitTest
{
    @Test
    public void testAddRetrieve()
    {
        final var store = new EdgeArrayStore("test", Metadata.defaultMetadata().withEdgeCount(Count._128));

        final var indexes = new ArrayList<Integer>();
        final var lists = new HashMap<Integer, IntArray>();
        randomIndexes(Repeats.NO_REPEATS, Count._1024, 65_536, index ->
        {
            indexes.add(index);

            final var values = new IntArray("values");
            values.initialize();
            for (final var value : randomIntList(Repeats.ALLOW_REPEATS, Count.count(randomInt(0, 20)), 1, 500))
            {
                values.add(value);
            }

            lists.put(index, values);
            store.list(index, values.iterator());
        });

        for (final var index : indexes)
        {
            final var list = store.list(index);
            ensure(lists.get(index).iterator().identical(list.iterator()));
        }
    }
}
