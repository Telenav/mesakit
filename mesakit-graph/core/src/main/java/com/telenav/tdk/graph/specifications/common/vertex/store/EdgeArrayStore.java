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

package com.telenav.kivakit.graph.specifications.common.vertex.store;

import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.iteration.IntIterator;
import com.telenav.kivakit.collections.primitive.list.IntList;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.interfaces.collection.Compressible;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.language.iteration.*;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSequence;
import com.telenav.kivakit.graph.specifications.common.edge.store.EdgeStore;

import java.util.List;

import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

/**
 * Stores lists of edge indexes. A new list can be added under a given index with {@link #list(int, IntIterator)}, the
 * list can be retrieved with {@link #list(int)} and the size of the list can be determined with {@link #size()}. A
 * sequence of edges can be retrieved by index with {@link #edgeSequence(EdgeStore, int)}, and the number of edge lists
 * in the store is available by calling {@link #size()}.
 *
 * @author jonathanl (shibo)
 */
public class EdgeArrayStore implements Compressible, NamedObject
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** All edge indexes laid out end-to-end */
    private SplitIntArray indexes;

    /** The offset in the edgeIndexes array where the list of edge indexes begins */
    private SplitIntArray offsets;

    /** The length of each list of edge indexes */
    private SplitByteArray lengths;

    /** The current offset we're writing to in the edgeIndexes array */
    private int offset;

    /** The next list identifier when adding */
    private int nextList = 1;

    /** The name of this object */
    private String objectName;

    /**
     * @param metadata Data for determining initial capacity of collections
     */
    public EdgeArrayStore(final String objectName, final Metadata metadata)
    {
        this.objectName = objectName;

        // Allocate the array of all edge indexes
        final var estimatedEdges = metadata.edgeCount(ALLOW_ESTIMATE).asEstimate();
        indexes = new SplitIntArray(objectName + ".indexes");
        indexes.initialSize(estimatedEdges);
        indexes.initialize();

        // Allocate the array of offsets into the allEdgeIndexes array and initialize the first offset to zero
        offsets = new SplitIntArray(objectName + ".offsets");
        offsets.initialSize(estimatedEdges);
        offsets.initialize();
        offsets.set(offset++, Integer.MIN_VALUE);

        // Allocate the array of lengths and initialize
        lengths = new SplitByteArray(objectName + ".lengths");
        lengths.initialSize(estimatedEdges);
        lengths.initialize();
    }

    protected EdgeArrayStore()
    {
    }

    @Override
    public Method compress(final Method method)
    {
        lengths.compress(method);
        offsets.compress(method);
        indexes.compress(method);
        return Method.RESIZE;
    }

    @Override
    public Method compressionMethod()
    {
        return Method.RESIZE;
    }

    /**
     * @return A sequence of edges from the given edge store created from the list stored at the given index
     */
    public EdgeSequence edgeSequence(final EdgeStore store, final int index)
    {
        return new EdgeSequence(Iterables.of(() -> new Next<>()
        {
            final IntList edges = list(index);

            int at;

            @Override
            public Edge onNext()
            {
                if (at < edges.size())
                {
                    return store.edgeForIndex(at++);
                }
                return null;
            }
        }));
    }

    /**
     * Adds the integer values from the given iterator to the store at the given index.
     *
     * @param index The index at which to store the values
     * @param values The values to store
     */
    public void list(final int index, final IntIterator values)
    {
        // Add each value
        final var start = offset;
        var length = 0;
        DEBUG.trace("Putting values into array $", index);
        while (values.hasNext())
        {
            final var value = values.next();
            DEBUG.trace("  Adding value $", value);
            indexes.set(offset++, value);
            length++;
        }

        // and store the offset (where our data started) and the length of the data added.
        offsets.set(index, start);
        lengths.set(index, (byte) length);
    }

    public int list(final List<Edge> edges)
    {
        final var list = nextList++;
        list(list, new IntIterator()
        {
            private int at;

            @Override
            public boolean hasNext()
            {
                return at < edges.size();
            }

            @Override
            public int next()
            {
                return edges.get(at++).index();
            }
        });
        return list;
    }

    /**
     * @return An {@link IntList} for the given index that is backed by the store (no new object is created aside from
     * the anonymous {@link IntList} subclass).
     */
    public IntList list(final int index)
    {
        final var offset = offsets.get(index);
        final int length = lengths.get(index);
        final var outer = this;
        return new IntList.Adapter()
        {
            @Override
            public int get(final int index)
            {
                return outer.indexes.get(offset + index);
            }

            @Override
            public boolean hasNullInt()
            {
                return outer.indexes.hasNullInt();
            }

            @Override
            public boolean isNull(final int value)
            {
                return outer.indexes.isNull(value);
            }

            @Override
            public int nullInt()
            {
                return outer.indexes.nullInt();
            }

            @Override
            public String objectName()
            {
                return "edge-array-store.get";
            }

            @Override
            public int safeGet(final int index)
            {
                return outer.indexes.safeGet(offset + index);
            }

            @Override
            public int size()
            {
                return length;
            }
        };
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    /**
     * @return The number of lists in this store
     */
    public int size()
    {
        return lengths.size();
    }

    /**
     * @return The size of the list at the given index
     */
    public int size(final int index)
    {
        return lengths.get(index);
    }
}
