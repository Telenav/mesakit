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

import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public class TagListTest
{
    @Test
    public void addGetValueForKey()
    {
        {
            final PbfTagList tags = PbfTagList.create();
            ensure(tags.isEmpty());
            tags.add("a", "b");
            tags.add(new Tag("b", "x"));
            ensure(!tags.isEmpty());
            ensure(tags.size() == 2);
            ensure("x".equalsIgnoreCase(tags.get("b").getValue()));
            ensure("x".equalsIgnoreCase(tags.valueForKey("b")));
        }
        {
            final PbfTagList tags = PbfTagList.create();
            tags.add(new Tag("1", "b"));
            tags.add("2", "b");
            tags.add(new Tag("3", "b"));
            tags.add(new Tag("a", "x"));
            ensure(!tags.isEmpty());
            ensure(tags.size() == 4);
            ensure("x".equalsIgnoreCase(tags.get("a").getValue()));
            ensure("x".equalsIgnoreCase(tags.valueForKey("a")));
        }
    }

    @Test
    public void containsKey()
    {
        {
            final PbfTagList tags = PbfTagList.create();
            tags.add(new Tag("a", "b"));
            ensure(tags.containsKey("a"));
            ensure(!tags.containsKey("b"));
        }
        {
            final PbfTagList tags = PbfTagList.create();
            tags.add(new Tag("1", "b"));
            tags.add(new Tag("2", "b"));
            tags.add(new Tag("3", "b"));
            tags.add(new Tag("a", "b"));
            ensure(tags.containsKey("a"));
            ensure(!tags.containsKey("b"));
        }
    }

    @Test
    public void copyEquals()
    {
        for (int i = 0; i < 20; i++)
        {
            final PbfTagList tags = PbfTagList.create();
            for (int j = 0; j < i; j++)
            {
                tags.add(new Tag("key-" + j, "b"));
            }
            ensure(tags.copy().equals(tags));
            ensure(tags.size() == i);
        }
    }

    @Test
    public void testHashEquals()
    {
        final PbfTagList ab = PbfTagList.create();
        ab.add(new Tag("a", "b"));

        final PbfTagList cd = PbfTagList.create();
        cd.add(new Tag("c", "d"));

        final PbfTagList ef = PbfTagList.create();
        ef.add(new Tag("e", "f"));

        final Map<PbfTagList, String> map = new HashMap<>();
        map.put(ab, "ab");
        map.put(cd, "cd");
        map.put(ef, "ef");

        ensure("ab".equals(map.get(ab)));
        ensure("cd".equals(map.get(cd)));
        ensure("ef".equals(map.get(ef)));
    }
}
