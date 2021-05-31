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

package com.telenav.kivakit.graph.specifications.common.element.store;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.telenav.kivakit.collections.map.CacheMap;
import com.telenav.kivakit.collections.primitive.array.packed.SplitPackedArray;
import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.list.ByteList;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.interfaces.operation.Initializable;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.kivakit.graph.GraphElement;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.telenav.kivakit.collections.primitive.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.kivakit.kernel.validation.Validate.ensureNotNull;

/**
 * Stores and retrieves tag lists for graph elements. The store can be constructed with {@link #TagStore(String,
 * PbfTagCodec)}, passing in the tag codec used to compress and decompress tag lists. The method {@link
 * #set(GraphElement, PbfTagList)} stores a tag list for the given graph element and the method {@link
 * #tagList(GraphElement)} retrieves it again. The number of tag lists can be retrieved with {@link #size()}.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public class TagStore implements KryoSerializable, NamedObject, Initializable<TagStore>
{
    private static class Entry
    {
        int offset;

        int length;

        PbfTagList tags;
    }

    /** The name of this object */
    private String objectName;

    /** The codec for compressing and decompressing tags */
    private transient volatile PbfTagCodec codec;

    /** The {@link ByteList} containing all of the compressed tags */
    private ByteList tags;

    /** Indexes into the tags byte array where the tags for each element are found */
    private SplitIntArray offset;

    /** The length of each byte sub-array */
    private SplitPackedArray length;

    /** Map from hashcode to entry for compressing tag entries in the tag store */
    private transient final Map<Integer, Entry> hashToEntry = new CacheMap<>(Maximum._8192);

    /**
     * @param objectName The name of this object
     * @param codec The codec to compress tags with
     */
    public TagStore(final String objectName, final PbfTagCodec codec)
    {
        this.objectName = objectName;
        this.codec = ensureNotNull(codec);
        initialize();
    }

    protected TagStore()
    {
    }

    public PbfTagCodec codec()
    {
        return codec;
    }

    public void codec(final PbfTagCodec codec)
    {
        this.codec = ensureNotNull(codec);
    }

    @Override
    public TagStore initialize()
    {
        offset = new SplitIntArray(objectName() + ".offset");
        offset.nullInt(Integer.MIN_VALUE);
        offset.initialize();

        length = new SplitPackedArray(objectName() + ".length");
        length.bits(Bits._16, NO_OVERFLOW);
        length.nullLong(65_535);
        length.initialize();

        final var tags = new SplitByteArray(objectName() + ".tags");
        tags.hasNullByte(false);
        tags.initialize();
        this.tags = tags;

        return this;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void objectName(final String objectName)
    {
        this.objectName = objectName;
    }

    @Override
    public void read(final Kryo kryo, final Input input)
    {
        objectName = kryo.readObject(input, String.class);
        offset = kryo.readObject(input, SplitIntArray.class);
        length = kryo.readObject(input, SplitPackedArray.class);
        tags = kryo.readObject(input, ByteArray.class);
        kryo.setReferences(true);
        codec = (PbfTagCodec) kryo.readClassAndObject(input);
    }

    public void set(final GraphElement element, final Entry entry)
    {
        offset.set(element.index(), entry.offset);
        length.set(element.index(), entry.length);
    }

    public void set(final GraphElement element, final PbfTagList tags)
    {
        if (tags != null && !tags.isEmpty())
        {
            // Get the hashcode of the tags we would like to store
            final var hash = tags.hashCode();

            // and from that get any recently created entry in the tag store with the same hash code
            final var existingEntry = hashToEntry.get(hash);

            // and if the entry is non-null and the actual tag lists are the same (the hash is not enough)
            if (existingEntry != null && existingEntry.tags.equals(tags))
            {
                // then reference the already existing entry's tags
                set(element, existingEntry);
            }
            else
            {
                // otherwise, get the element's index
                final var index = element.index();

                // then get the output writer and record the position before encoding
                final var start = this.tags.cursor();
                offset.set(index, start);

                // write the tag list
                codec.encode(this.tags, tags);

                // and then mark the end of the output,
                final var length = this.tags.cursor() - start;
                this.length.set(index, length);

                // and finally we check that we can get the element's tag list back
                assert tags.equals(tagList(element)) : "Tag list stored at index " + index + " should have been:\n" + tags + "\nbut was:\n" + tagList(element);

                final var entry = new Entry();
                entry.offset = start;
                entry.length = length;
                entry.tags = tags;

                hashToEntry.put(hash, entry);
            }
        }
    }

    public int size()
    {
        return offset.size();
    }

    public PbfTagList tagList(final GraphElement element)
    {
        // Get offset of tag list
        final var index = element.index();
        final var offset = this.offset.safeGet(index);

        // and if we have tags,
        if (!this.offset.isNull(offset))
        {
            // decode them
            final var length = (int) this.length.safeGet(index);
            return codec.decode(tags.sublist(offset, length));
        }

        return PbfTagList.EMPTY;
    }

    public PbfTagMap tagMap(final GraphElement element)
    {
        // Get offset of tag list
        final var index = element.index();
        final var offset = this.offset.safeGet(index);

        // and if we have tags,
        if (!this.offset.isNull(offset))
        {
            // decode them
            final var length = (int) this.length.safeGet(index);
            return codec.decodeMap(tags.sublist(offset, length));
        }

        return PbfTagMap.EMPTY;
    }

    @Override
    public String toString()
    {
        return objectName;
    }

    @Override
    public void write(final Kryo kryo, final Output output)
    {
        kryo.writeObject(output, objectName);
        kryo.writeObject(output, offset);
        kryo.writeObject(output, length);
        tags = unsplit();
        kryo.writeObject(output, tags);
        kryo.setReferences(true);
        kryo.writeClassAndObject(output, codec);
    }

    @NotNull
    private ByteArray unsplit()
    {
        final var bytes = new ByteArray("bytes");
        bytes.hasNullByte(false);
        bytes.initialize();
        bytes.addAll(tags);
        return bytes;
    }
}
