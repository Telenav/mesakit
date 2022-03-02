////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.model.tags;

import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagMapTest extends UnitTest
{
    @Test
    public void test()
    {
        var tags = tags("a", "b", "c", "d", "e", "1");
        var map = PbfTagMap.create();
        ensure(map.isEmpty());
        map.putAll(tags);
        ensure(!map.isEmpty());
        ensure(map.size() == 3);
        var keys = map.keys();
        ensure(keys.hasNext());
        ensure(keys.next().equals("a"));
        ensure(keys.hasNext());
        ensure(keys.next().equals("c"));
        ensure(keys.hasNext());
        ensure(keys.next().equals("e"));
        ensure(!keys.hasNext());

        ensure("y".equals(map.get("x", "y")));
        ensure(1 == map.valueAsInt("e"));
        map.put("g", "z");
        ensure(map.size() == 4);
        ensure(map.containsKey("c"));
        ensure(map.containsKey("e"));
        ensure(map.containsKey("g"));
        ensure(map.containsKey("a"));
        map.put("a", "!");
        ensure("!".equals(map.get("a")));
    }

    @Test
    public void testSplit()
    {
        var tags = PbfTagMap.from(tags("x", "a:bar;c:"));
        var values = tags.valueSplit("x");
        ensureEqual("a", values.get(0));
        ensureEqual("bar", values.get(1));
        ensureEqual("c", values.get(2));
        ensureEqual("", values.get(3));
    }

    private List<Tag> tags(String... pairs)
    {
        List<Tag> tags = new ArrayList<>();
        for (var i = 0; i < pairs.length; i += 2)
        {
            tags.add(new Tag(pairs[i], pairs[i + 1]));
        }
        return tags;
    }
}
