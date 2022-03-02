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

import com.telenav.kivakit.data.compression.codecs.huffman.HuffmanCodec;
import com.telenav.kivakit.primitive.collections.array.bits.io.BitReader;
import com.telenav.kivakit.primitive.collections.array.bits.io.BitWriter;
import com.telenav.kivakit.primitive.collections.list.ByteList;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfModelCompression;

/**
 * A tag codec is used to compress the key / value pairs found in tags. Different codecs can be tuned to different data
 * sets. For example, keys and values may have different distributions of letter frequencies in any data source, and
 * keys in OSM data may have different frequencies than keys in other data sources. Individual data suppliers may have
 * different characteristics as well. See {@link PbfStringListTagCodec} for information on how tag codecs can be tuned
 * to different data sets.
 * <p>
 * Although {@link HuffmanCodec}s are currently used to compress data in {@link PbfStringListTagCodec}, the particular
 * compression scheme used to compress tags is an implementation detail and might be changed in the future.
 * <p>
 * The {@link #encode(ByteList, PbfTagList)} method encodes a list of tags into the given byte array. The {@link
 * #decode(ByteList)} method can later be used to decompress the tag list from the same data.
 *
 * @author jonathanl (shibo)
 * @see PbfTagList
 * @see PbfStringListTagCodec
 * @see BitWriter
 * @see BitReader
 */
@UmlClassDiagram(diagram = DiagramPbfModelCompression.class)
public interface PbfTagCodec
{
    /**
     * @return A {@link PbfTagList} decompressed from the given {@link BitReader} using the trained Huffman codec
     */
    PbfTagList decode(ByteList input);

    /**
     * @return A {@link PbfTagList} decompressed from the given {@link BitReader} using the trained Huffman codec
     */
    PbfTagMap decodeMap(ByteList input);

    /**
     * Encodes the tag list using the Huffman codec and writes it to the given {@link BitWriter}.
     */
    void encode(ByteList output, PbfTagList tags);
}
