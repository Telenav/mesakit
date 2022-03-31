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

package com.telenav.mesakit.map.data.formats.pbf.model.tags.compression;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.test.UnitTest;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.list.HuffmanStringListCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.tree.Symbols;
import com.telenav.kivakit.primitive.collections.array.scalars.ByteArray;
import com.telenav.kivakit.properties.PropertyMap;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import org.junit.Test;

import static com.telenav.kivakit.core.value.count.Count._1_000;
import static com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec.ESCAPE;

/**
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection")
public class StringListTagCodecTest extends UnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void test()
    {
        var keyCharacterCodec = HuffmanCharacterCodec.from(characterSymbols(("test-key-character.codec")));
        var keyStringCodec = HuffmanStringCodec.from(stringSymbols("test-key-string.codec"));
        var keyStringListCodec = new HuffmanStringListCodec(keyStringCodec, keyCharacterCodec);

        var valueCharacterCodec = HuffmanCharacterCodec.from(characterSymbols("test-value-character.codec"));
        var valueStringCodec = HuffmanStringCodec.from(stringSymbols("test-value-string.codec"));
        var valueStringListCodec = new HuffmanStringListCodec(valueStringCodec, valueCharacterCodec);

        var codec = new PbfStringListTagCodec(keyStringListCodec, valueStringListCodec);

        test(codec, PbfTagList.create().add("x", "XXX"));
        test(codec, PbfTagList.create().add("jdlficxqxbjlzen", "mmjpqcbor").add("iyvxmttgmgl", "path"));
        test(codec, PbfTagList.create().add("zdcyaq", "h").add("name", "fwljgohinvxs"));
        test(codec, PbfTagList.create().add("z", "z"));
        test(codec, tags());

        var progress = BroadcastingProgressReporter.create();
        _1_000.loop(() ->
        {
            test(codec, randomTags());
            progress.next();
        });
    }

    private Symbols<Character> characterSymbols(String fileName)
    {
        return Symbols.load(frequencies(fileName), ESCAPE, new HuffmanCharacterCodec.Converter(LOGGER));
    }

    private PropertyMap frequencies(String name)
    {
        return PropertyMap.load(this, thisPackage(), "codecs/" + name);
    }

    private PbfTagList randomTags()
    {
        var tags = PbfTagList.create();
        random().rangeInclusive(1, 4).loop(() -> tags.add(random().letters(), random().letters()));
        return tags;
    }

    private Symbols<String> stringSymbols(String fileName)
    {
        return Symbols.load(frequencies(fileName), new HuffmanStringCodec.Converter(LOGGER));
    }

    private PbfTagList tags()
    {
        var tags = PbfTagList.create();
        tags.add("highway", "track");
        tags.add("highway", "motorway");
        tags.add("highway", "secondary");
        tags.add("a", "b");
        return tags;
    }

    private void test(PbfStringListTagCodec codec, PbfTagList tags)
    {
        var data = new ByteArray("data");
        data.initialize();

        codec.encode(data, tags);

        data.reset();
        var decoded = codec.decode(data);
        ensureEqual(decoded, tags);
    }
}
