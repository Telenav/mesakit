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

package com.telenav.tdk.graph.specifications.unidb.store.tag;

import com.telenav.tdk.core.kernel.scalars.counts.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.tdk.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.project.TdkGraphCoreUnitTest;
import com.telenav.tdk.graph.specifications.common.element.store.TagStore;
import com.telenav.tdk.utilities.compression.codecs.huffman.character.*;
import com.telenav.tdk.utilities.compression.codecs.huffman.list.HuffmanStringListCodec;
import com.telenav.tdk.utilities.compression.codecs.huffman.string.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

public class UniDbTagStoreTest extends TdkGraphCoreUnitTest
{
    @Test
    public void testOsm()
    {
        final var graph = osmGraph();
        final var store = new TagStore("test", codec());

        final Edge edge = nextOsmEdge(graph);
        store.set(edge, tags("a", "b"));
        final var tags = store.tagList(edge);
        ensureEqual("a", tags.get(0).getKey());
        ensureEqual("b", tags.get(0).getValue());
    }

    @Test
    public void testUniDb()
    {
        // Train a tag codec
        final var codec = codec2();
        final var store = new TagStore("test", codec);

        // Store three tag collections
        final Edge edge1 = nextUniDbEdge();
        final Edge edge2 = nextUniDbEdge();
        final Edge edge3 = nextUniDbEdge();

        store.set(edge1, tags("a", "one", "b", "two"));
        store.set(edge2, tags("a", "one", "a", "two"));
        store.set(edge3, tags("a", "three", "c", "one"));

        // Retrieve and check each tag collection
        final var tags = store.tagList(edge1);
        ensureEqual(tags.get(0).getKey(), "a");
        ensureEqual(tags.get(0).getValue(), "one");
        ensureEqual(tags.get(1).getKey(), "b");
        ensureEqual(tags.get(1).getValue(), "two");

        final var tags2 = store.tagList(edge2);
        ensureEqual(tags2.get(0).getKey(), "a");
        ensureEqual(tags2.get(0).getValue(), "one");
        ensureEqual(tags2.get(1).getKey(), "a");
        ensureEqual(tags2.get(1).getValue(), "two");

        final var tags3 = store.tagList(edge3);
        ensureEqual(tags3.get(0).getKey(), "a");
        ensureEqual(tags3.get(0).getValue(), "three");
        ensureEqual(tags3.get(1).getKey(), "c");
        ensureEqual(tags3.get(1).getValue(), "one");
    }

    @NotNull
    private PbfStringListTagCodec codec()
    {
        final var keyCharacterCodec = HuffmanCharacterCodec.from(
                new CharacterFrequencies()
                        .add("abc")
                        .add("def").symbols());

        final var valueCharacterCodec = HuffmanCharacterCodec.from(
                new CharacterFrequencies()
                        .add("bcd")
                        .add("ghi").symbols());

        final var keyStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("abc")
                        .add("def")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi").symbols());

        final var valueStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("jkl")
                        .add("mno")
                        .add("mno")
                        .add("mno")
                        .add("pqr")
                        .add("stu").symbols());

        final var keyStringListCodec = new HuffmanStringListCodec(keyStringCodec, keyCharacterCodec);
        final var valueStringListCodec = new HuffmanStringListCodec(valueStringCodec, valueCharacterCodec);

        return new PbfStringListTagCodec(keyStringListCodec, valueStringListCodec);
    }

    @NotNull
    private PbfStringListTagCodec codec2()
    {
        final var keyCharacterCodec = HuffmanCharacterCodec.from(
                new CharacterFrequencies()
                        .add("a")
                        .add("b")
                        .add("c")
                        .add("a")
                        .add("a").symbols());

        final var valueCharacterCodec = HuffmanCharacterCodec.from(
                new CharacterFrequencies()
                        .add("one")
                        .add("two")
                        .add("three")
                        .add("one")
                        .add("two").symbols());

        final var keyStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("abc")
                        .add("def")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi").symbols());

        final var valueStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("jkl")
                        .add("mno")
                        .add("mno")
                        .add("mno")
                        .add("pqr")
                        .add("stu").symbols());

        final var keyStringListCodec = new HuffmanStringListCodec(keyStringCodec, keyCharacterCodec);
        final var valueStringListCodec = new HuffmanStringListCodec(valueStringCodec, valueCharacterCodec);

        return new PbfStringListTagCodec(keyStringListCodec, valueStringListCodec);
    }

    private PbfTagList tags(final String... pairs)
    {
        final var tags = PbfTagList.create();
        for (var i = 0; i < pairs.length; i += 2)
        {
            tags.add(new Tag(pairs[i], pairs[i + 1]));
        }
        return tags;
    }
}
