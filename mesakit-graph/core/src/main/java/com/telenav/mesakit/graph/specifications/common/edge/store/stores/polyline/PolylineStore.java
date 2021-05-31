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

package com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline;

import com.telenav.kivakit.collections.primitive.array.bits.BitArray;
import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.interfaces.collection.Compressible;
import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.object.*;
import com.telenav.kivakit.kernel.scalars.bytes.Bytes;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.mesakit.map.geography.polyline.Polyline;
import com.telenav.mesakit.map.geography.polyline.compression.differential.CompressedPolyline;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

/**
 * A compressed store of polyline data
 *
 * @author jonathanl (shibo)
 */
public class PolylineStore implements Named, Compressible
{
    /** An invalid polyline index */
    public static final int INVALID_INDEX = -1;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** The data for all compressed polylines */
    private ByteArray data;

    /** Index of the end of each byte array */
    private IntArray ends;

    /** Name for debugging purposes */
    private String name;

    public PolylineStore(final String name, final Estimate initialSize)
    {
        ensure(name != null);
        ensure(initialSize != Estimate.MAXIMUM);

        this.name = name;

        ends = new IntArray(name + ".ends");
        ends.initialSize(initialSize);
        ends.initialize();

        // The start of each byte array is found at index - 1, so for the first byte array, we point the zeroeth index
        // to the start of the first byte array at index 1 in the data array (since index 0 is invalid).
        ends.add(0);

        data = new ByteArray(name + ".data");
        data.hasNullByte(false);
        data.initialSize(Bytes.megabytes(8).asEstimate());
        data.maximumSize(Bytes.megabytes(8).asMaximum());
        data.initialize();
    }

    protected PolylineStore()
    {
    }

    /**
     * @return The store index if the given polyline was added and INVALID_INDEX if it wouldn't fit
     */
    public int add(final CompressedPolyline polyline)
    {
        // Get polyline data as a byte array
        final var bytes = polyline.asBytes();

        // and if we can fit another polyline in the data store,
        if (data.size() + bytes.size() + 1 < data.maximumSize().asInt())
        {
            // get the index of the data we are adding,
            final var index = ends.size();

            // add the data,
            data.addAll(bytes);

            // store the end of the data,
            ends.add(data.size());

            // and return the index to the data
            return index;
        }

        // We cannot fit the polyline
        return INVALID_INDEX;
    }

    @Override
    public Method compress(final Method method)
    {
        data.compress(method);
        ends.compress(method);

        return compressionMethod();
    }

    @Override
    public Method compressionMethod()
    {
        return Method.RESIZE;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof PolylineStore)
        {
            final var that = (PolylineStore) object;
            return Objects.equalPairs(size(), that.size(), data, that.data, ends, that.ends);
        }
        return false;
    }

    /**
     * Gets the polyline stored at the given index
     */
    public Polyline get(final int index)
    {
        final var start = ends.get(index - 1);
        final var end = ends.get(index);
        final var data = this.data.sublist(start, end - start);
        if (DEBUG.isDebugOn())
        {
            DEBUG.trace("Retrieved data: $", data);
        }
        final var bits = new BitArray("bits", data);
        bits.initialize();
        return CompressedPolyline.fromBitArray(bits);
    }

    @Override
    public int hashCode()
    {
        return Hash.many(size(), data.hashCode(), ends.hashCode());
    }

    public boolean isNull(final int index)
    {
        return ends.isNull(index);
    }

    @Override
    public String name()
    {
        return name;
    }

    public int size()
    {
        return ends.size() - 1;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
