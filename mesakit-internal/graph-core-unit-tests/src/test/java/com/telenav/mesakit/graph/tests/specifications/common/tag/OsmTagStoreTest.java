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

package com.telenav.mesakit.graph.tests.specifications.common.tag;

import com.telenav.kivakit.data.compression.codecs.huffman.character.CharacterFrequencies;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.list.HuffmanStringListCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.StringFrequencies;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.element.store.TagStore;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

public class OsmTagStoreTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        // Create tag store with the codec
        var graph = osmGraph();
        var store = new TagStore("test", Metadata.defaultMetadata().tagCodec());

        // Store three tag collections
        Edge edge1 = nextOsmEdge(graph);
        Edge edge2 = nextOsmEdge(graph);
        Edge edge3 = nextOsmEdge(graph);
        store.set(edge1, tags("a", "one", "b", "two"));
        store.set(edge2, tags("a", "one", "a", "two"));
        store.set(edge3, tags("a", "three", "c", "one"));

        // Retrieve and check each tag collection
        PbfTagList tags = store.tagList(edge1);
        ensureEqual(tags.get(0).getKey(), "a");
        ensureEqual(tags.get(0).getValue(), "one");
        ensureEqual(tags.get(1).getKey(), "b");
        ensureEqual(tags.get(1).getValue(), "two");

        PbfTagList tags2 = store.tagList(edge2);
        ensureEqual(tags2.get(0).getKey(), "a");
        ensureEqual(tags2.get(0).getValue(), "one");
        ensureEqual(tags2.get(1).getKey(), "a");
        ensureEqual(tags2.get(1).getValue(), "two");

        PbfTagList tags3 = store.tagList(edge3);
        ensureEqual(tags3.get(0).getKey(), "a");
        ensureEqual(tags3.get(0).getValue(), "three");
        ensureEqual(tags3.get(1).getKey(), "c");
        ensureEqual(tags3.get(1).getValue(), "one");
    }

    @Test
    public void testBug()
    {
        // Create tag store with the codec
        var graph = osmGraph();
        var store = new TagStore("test", Metadata.defaultMetadata().tagCodec());

        Edge edge = nextOsmEdge(graph);
        store.set(edge, tags("J", "J"));

        // Retrieve and check each tag collection
        PbfTagList tags = store.tagList(edge);
        ensureEqual(tags.get(0).getKey(), "J");
        ensureEqual(tags.get(0).getValue(), "J");
    }

    @Test
    public void testCodec()
    {
        var graph = osmGraph();
        PbfStringListTagCodec codec = codec();
        var store = new TagStore("test", codec);

        Edge edge2 = nextOsmEdge(graph);
        store.set(edge2, tags("x", "XXX"));
        ensureEqual("XXX", store.tagList(edge2).get(0).getValue());

        Edge edge1 = nextOsmEdge(graph);
        store.set(edge1, tags("a", "b"));
        PbfTagList tags = store.tagList(edge1);
        ensureEqual("a", tags.get(0).getKey());
        ensureEqual("b", tags.get(0).getValue());
    }

    @Test
    public void testOsm()
    {
        var graph = osmGraph();
        PbfStringListTagCodec codec = codec();
        var store = new TagStore("test", codec);
        Edge edge = nextOsmEdge(graph);
        store.set(edge, tags("a", "b"));
        PbfTagList tags = store.tagList(edge);
        ensureEqual("a", tags.get(0).getKey());
        ensureEqual("b", tags.get(0).getValue());
    }

    private PbfStringListTagCodec codec()
    {
        var characterFrequencies = new CharacterFrequencies().add("abc").add("def");
        characterFrequencies.frequencies().add(HuffmanCharacterCodec.ESCAPE, Count._16);

        var keyCharacterCodec = HuffmanCharacterCodec.from(
                characterFrequencies.symbols());

        var valueFrequencies = new CharacterFrequencies().add("bcd").add("ghi");
        valueFrequencies.frequencies().add(HuffmanCharacterCodec.ESCAPE, Count._16);
        var valueCharacterCodec = HuffmanCharacterCodec.from(
                valueFrequencies.symbols());

        var keyStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("abc")
                        .add("def")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi")
                        .add("ghi").symbols());

        var valueStringCodec = HuffmanStringCodec.from(
                new StringFrequencies(Count._128, Maximum.MAXIMUM)
                        .add("jkl")
                        .add("mno")
                        .add("mno")
                        .add("mno")
                        .add("pqr")
                        .add("stu").symbols());

        var keyStringListCodec = new HuffmanStringListCodec(keyStringCodec, keyCharacterCodec);
        var valueStringListCodec = new HuffmanStringListCodec(valueStringCodec, valueCharacterCodec);

        return new PbfStringListTagCodec(keyStringListCodec, valueStringListCodec);
    }

    private PbfTagList tags(String... pairs)
    {
        var tags = PbfTagList.create();
        for (var i = 0; i < pairs.length; i += 2)
        {
            tags.add(new Tag(pairs[i], pairs[i + 1]));
        }
        return tags;
    }
}
