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

import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.data.compression.SymbolProducer;
import com.telenav.kivakit.data.compression.codecs.StringListCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.primitive.collections.array.bits.io.BitReader;
import com.telenav.kivakit.primitive.collections.array.bits.io.BitWriter;
import com.telenav.kivakit.primitive.collections.list.ByteList;
import com.telenav.kivakit.properties.PropertyMap;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.UmlNote;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelCompression;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;

import java.util.ArrayList;

import static com.telenav.kivakit.data.compression.SymbolConsumer.Directive.CONTINUE;
import static com.telenav.kivakit.data.compression.SymbolConsumer.Directive.STOP;

/**
 * An implementation of {@link PbfTagCodec} that uses {@link StringListCodec}s to encode and decode keys and values both
 * by character and by string. This design allows very short codes to be used for frequently occurring keys and values
 * like "highway" and "residential", but with fallback to the more typical huffman character encoding for values that
 * aren't in the set of most frequently used strings.
 * <p>
 * To create {@link StringListCodec}s tuned to the key and value character and string frequencies of a given data set,
 * the <i>CodecGeneratorApplication</i> application will create these four codec properties files:
 * <ul>
 *     <li>default-key-character.codec</li>
 *     <li>default-value-character.codec</li>
 *     <li>default-key-string.codec</li>
 *     <li>default-value-string.codec</li>
 * </ul>
 * The properties files can be used to construct the codecs with {@link HuffmanCharacterCodec#characterCodec(Listener, PropertyMap, Character)}
 * for character based compression or {@link HuffmanStringCodec#stringCodec(PropertyMap)}, and these two codecs can be passed
 * to the constructor of this class.
 *
 * @author jonathanl (shibo)
 * @see PbfTagCodec
 */
@UmlClassDiagram(diagram = DiagramPbfModelCompression.class)
@UmlNote(text = "Created from metadata")
public class PbfStringListTagCodec implements PbfTagCodec
{
    /** The codec for keys */
    private StringListCodec keyCodec;

    /** The codec for values */
    private StringListCodec valueCodec;

    public PbfStringListTagCodec(StringListCodec keyCodec, StringListCodec valueCodec)
    {
        this.keyCodec = keyCodec;
        this.valueCodec = valueCodec;
    }

    protected PbfStringListTagCodec()
    {
    }

    /**
     * @return A {@link PbfTagList} decompressed from the given {@link BitReader} using the trained Huffman codec
     */
    @Override
    public PbfTagList decode(ByteList input)
    {
        var tags = PbfTagList.create();

        keyCodec.decode(input, (index, key) ->
        {
            if (key != null)
            {
                tags.set(index, key, null);
            }
            return key != null ? CONTINUE : STOP;
        });

        valueCodec.decode(input, (index, value) ->
        {
            if (value != null)
            {
                tags.set(index, null, value);
            }
            return value != null ? CONTINUE : STOP;
        });

        return tags;
    }

    /**
     * @return A {@link PbfTagList} decompressed from the given {@link BitReader} using the trained Huffman codec
     */
    @Override
    public PbfTagMap decodeMap(ByteList input)
    {
        var tags = PbfTagMap.create();

        var keys = new ArrayList<String>();
        keyCodec.decode(input, (index, key) ->
        {
            if (key != null)
            {
                keys.add(key);
            }
            return key != null ? CONTINUE : STOP;
        });

        valueCodec.decode(input, (index, value) ->
        {
            if (value != null)
            {
                tags.put(keys.get(index), value);
            }
            return value != null ? CONTINUE : STOP;
        });

        return tags;
    }

    /**
     * Encodes the tag list using the Huffman codec and writes it to the given {@link BitWriter}.
     */
    @Override
    public void encode(ByteList output, PbfTagList tags)
    {
        keyCodec.encode(output, new SymbolProducer<>()
        {
            @Override
            public String get(int ordinal)
            {
                return ordinal < tags.size() ? tags.get(ordinal).getKey() : null;
            }

            @Override
            public int size()
            {
                return tags.size();
            }
        });

        valueCodec.encode(output, new SymbolProducer<>()
        {
            @Override
            public String get(int ordinal)
            {
                return ordinal < tags.size() ? tags.get(ordinal).getValue() : null;
            }

            @Override
            public int size()
            {
                return tags.size();
            }
        });
    }
}
