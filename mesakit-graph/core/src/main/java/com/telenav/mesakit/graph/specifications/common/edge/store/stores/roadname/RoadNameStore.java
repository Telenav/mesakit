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

package com.telenav.mesakit.graph.specifications.common.edge.store.stores.roadname;

import com.telenav.kivakit.core.bits.BitDiagram;
import com.telenav.kivakit.core.collections.map.CacheMap;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.value.mutable.MutableValue;
import com.telenav.kivakit.data.compression.SymbolProducer;
import com.telenav.kivakit.data.compression.codecs.CharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.interfaces.lifecycle.Initializable;
import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitByteArray;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToIntMap;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.map.road.model.RoadName;

import java.util.WeakHashMap;

import static com.telenav.kivakit.data.compression.SymbolConsumer.Directive.STOP;

public class RoadNameStore implements NamedObject, Initializable
{
    @SuppressWarnings("SpellCheckingInspection")
    private static final BitDiagram KEY = new BitDiagram("IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIITTTT OOOOOOOO");

    private static final BitDiagram.BitField INDEX = KEY.field('I');

    private static final BitDiagram.BitField TYPE = KEY.field('T');

    private static final BitDiagram.BitField ORDINAL = KEY.field('O');

    /** Efficient store of road names */
    private SplitByteArray names;

    /** The index in the name array for a given edge index */
    private LongToIntMap keyToNameIndex;

    /** The codec for decoding compressed data in the 'names' byte array */
    private transient CharacterCodec codec;

    private Estimate initialSize;

    /** The name of this object */
    private String objectName;

    /**
     * String pooling map used while loading to avoid duplicating frequently occurring strings. This effectively
     * compresses the input ala LZW.
     */
    private final transient CacheMap<String, Integer> pool = new CacheMap<>(Maximum._8192);

    private final transient WeakHashMap<Long, String> cache = new WeakHashMap<>();

    public RoadNameStore(String objectName, Estimate initialSize, Metadata metadata)
    {
        codec = metadata.roadNameCharacterCodec();

        this.initialSize = initialSize;
        this.objectName = objectName;

        initialize();
    }

    protected RoadNameStore()
    {
    }

    public void codec(HuffmanCharacterCodec codec)
    {
        this.codec = codec;
    }

    public RoadName get(Edge edge, RoadName.Type type, int roadNameOrdinal)
    {
        var edgeIndex = edge.index();
        var key = keyFor(edgeIndex, type, roadNameOrdinal);
        var nameIndex = keyToNameIndex.get(key);
        if (!keyToNameIndex.isNull(nameIndex))
        {
            synchronized (cache)
            {
                var cached = cache.get(key);
                if (cached == null)
                {
                    var name = new MutableValue<String>();
                    codec.decode(names.sublist(nameIndex, 256), (ordinal, decoded) ->
                    {
                        name.set(decoded);
                        return STOP;
                    });
                    cached = name.get();
                    cache.put(key, cached);
                }
                return RoadName.forName(cached);
            }
        }
        return null;
    }

    @Override
    public void objectName(String objectName)
    {
        this.objectName = objectName;
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void onInitialize()
    {
        names = new SplitByteArray(objectName() + ".names");
        names.initialSize(initialSize.times(Count._4));
        names.initialize();
        names.write(Byte.MIN_VALUE);

        keyToNameIndex = new LongToIntMap(objectName() + ".keyToNameIndex");
        keyToNameIndex.initialSize(initialSize);
        keyToNameIndex.initialize();
    }

    public void set(Edge edge, RoadName.Type type, int roadNameOrdinal, RoadName roadName)
    {
        if (roadName != null)
        {
            var name = roadName.name();
            if (name != null)
            {
                var edgeIndex = edge.index();
                var key = keyFor(edgeIndex, type, roadNameOrdinal);

                // Look in pool for an already-stored index
                var pooledIndex = pool.get(name);
                if (pooledIndex != null)
                {
                    keyToNameIndex.put(key, pooledIndex);
                }
                else
                {
                    var index = names.cursor();
                    codec.encode(names, SymbolProducer.symbolProducer(name));
                    keyToNameIndex.put(key, index);
                    pool.put(name, index);
                }

                assert roadName.equals(get(edge, type, roadNameOrdinal)) :
                        "Road name " + roadName + " != " + get(edge, type, roadNameOrdinal)
                                + " (edge.id = " + edge.identifierAsLong()
                                + ", edge.index = " + edge.index()
                                + ", type = " + type
                                + ", ordinal = " + roadNameOrdinal + ")";
            }
        }
    }

    public int size(Edge edge, RoadName.Type type)
    {
        var count = 0;
        var edgeIndex = edge.index();
        for (var roadNameOrdinal = 0; roadNameOrdinal < 256; roadNameOrdinal++)
        {
            var key = keyFor(edgeIndex, type, roadNameOrdinal);
            var nameIndex = keyToNameIndex.get(key);
            if (keyToNameIndex.isNull(nameIndex))
            {
                break;
            }
            count++;
        }
        return count;
    }

    /**
     * Returns a pseudo-index for a given edgeIndex, type and road-name ordinal
     */
    private long keyFor(int edgeIndex, RoadName.Type type, int roadNameOrdinal)
    {
        assert roadNameOrdinal < 256;

        var key = INDEX.set(0L, edgeIndex);
        key = TYPE.set(key, type.ordinal());
        key = ORDINAL.set(key, roadNameOrdinal);
        return key;
    }
}
