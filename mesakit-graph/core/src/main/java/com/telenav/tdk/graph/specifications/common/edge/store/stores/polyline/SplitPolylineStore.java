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

package com.telenav.kivakit.graph.specifications.common.edge.store.stores.polyline;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.io.*;
import com.telenav.kivakit.collections.primitive.array.scalars.IntArray;
import com.telenav.kivakit.kernel.interfaces.collection.*;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.interfaces.operation.Initializable;
import com.telenav.kivakit.kernel.language.object.*;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.map.geography.polyline.Polyline;
import com.telenav.kivakit.map.geography.polyline.compression.differential.CompressedPolyline;

import java.util.ArrayList;

import static com.telenav.kivakit.kernel.validation.Validate.*;

public class SplitPolylineStore implements KryoSerializable, NamedObject, Initializable<SplitPolylineStore>, Compressible
{
    /** The underlying polyline stores */
    private ArrayList<PolylineStore> stores;

    /** The current store we're adding to */
    private PolylineStore polylines;

    /** Map from client index to store */
    private IntArray indexToStore;

    /** Map from client index to store index */
    private IntArray indexToStoreIndex;

    /** The name of this store */
    private String objectName;

    /** Sizes of child polyline stores */
    private Maximum maximumChildSize;

    /** Estimated size of child polyline stores */
    private Estimate initialChildSize;

    public SplitPolylineStore(final String objectName, final Maximum maximumSize, final Maximum maximumChildSize,
                              final Estimate initialSize, final Estimate initialChildSize)
    {
        ensure(objectName != null);
        ensure(!initialSize.isMaximum());
        ensure(initialSize.isLessThanOrEqualTo(maximumSize));

        this.objectName = objectName;

        this.maximumChildSize = maximumChildSize;
        this.initialChildSize = initialChildSize;

        indexToStore = new IntArray(objectName + ".indexToStore");
        indexToStore.nullInt(Integer.MIN_VALUE);
        indexToStore.initialSize(initialSize);
        indexToStore.initialize();

        indexToStoreIndex = new IntArray(objectName + ".indexToStoreIndex");
        indexToStore.nullInt(Integer.MIN_VALUE);
        indexToStoreIndex.initialSize(initialSize);
        indexToStoreIndex.initialize();

        stores = new ArrayList<>(16);

        addStore();
    }

    protected SplitPolylineStore()
    {
    }

    @Override
    public Method compress(final Method method)
    {
        for (final var store : stores)
        {
            store.compress(method);
        }

        polylines.compress(method);
        indexToStore.compress(method);
        indexToStoreIndex.compress(method);

        return Method.MIXED;
    }

    @Override
    public Method compressionMethod()
    {
        return Method.MIXED;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof SplitPolylineStore)
        {
            final var that = (SplitPolylineStore) object;
            return Objects.equalPairs(stores, that.stores, indexToStore, that.indexToStore,
                    indexToStoreIndex, that.indexToStoreIndex);
        }
        return false;
    }

    /**
     * @return The polyline for the given index
     */
    public Polyline get(final Indexed indexed)
    {
        return get(indexed.index());
    }

    /**
     * @return The polyline for the given index
     */
    public Polyline get(final int index)
    {
        ensure(index > 0);

        // If the store for the given index is not null,
        final var which = indexToStore.safeGet(index);
        if (!indexToStore.isNull(which))
        {
            // then get the store
            final var store = stores.get(which);

            // and the index within the store,
            final var storeIndex = indexToStoreIndex.safeGet(index);

            // and if that index is not null,
            if (!store.isNull(storeIndex))
            {
                // the the polyline from the store
                return store.get(storeIndex);
            }
        }
        return null;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(stores, indexToStore, indexToStoreIndex);
    }

    @Override
    public SplitPolylineStore initialize()
    {
        return this;
    }

    @Override
    public void objectName(final String objectName)
    {
        this.objectName = objectName;
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void read(final Kryo kryo, final Input input)
    {
        objectName = kryo.readObject(input, String.class);
        maximumChildSize = kryo.readObject(input, Maximum.class);
        initialChildSize = kryo.readObject(input, Estimate.class);
        indexToStore = kryo.readObject(input, IntArray.class);
        indexToStoreIndex = kryo.readObject(input, IntArray.class);
        final int storeCount = kryo.readObject(input, int.class);
        stores = new ArrayList<>();
        for (var at = 0; at < storeCount; at++)
        {
            stores.add(kryo.readObject(input, PolylineStore.class));
        }
    }

    /**
     * Stores the given polyline at the given index.
     */
    public void set(final Indexed indexed, final Polyline line)
    {
        unsupported();
    }

    public void set(final Indexed indexed, final CompressedPolyline line)
    {
        // Add to store
        var storeIndex = polylines.add(line);

        // and if it doesn't fit
        while (storeIndex == PolylineStore.INVALID_INDEX)
        {
            // create a new store
            addStore();

            // and add to that one
            storeIndex = polylines.add(line);
        }

        // Save which store we put the polyline in and what index in that store
        final var index = indexed.index();
        indexToStore.set(index, stores.size() - 1);
        indexToStoreIndex.set(index, storeIndex);
    }

    public int size()
    {
        var size = 0;
        for (final var store : stores)
        {
            size += store.size();
        }
        return size;
    }

    @Override
    public void write(final Kryo kryo, final Output output)
    {
        kryo.writeObject(output, objectName);
        kryo.writeObject(output, maximumChildSize);
        kryo.writeObject(output, initialChildSize);
        kryo.writeObject(output, indexToStore);
        kryo.writeObject(output, indexToStoreIndex);
        kryo.writeObject(output, stores.size());
        for (final var store : stores)
        {
            kryo.writeObject(output, store);
        }
    }

    private void addStore()
    {
        polylines = new PolylineStore(objectName + ".store[" + stores.size() + "]", initialChildSize);
        stores.add(polylines);
    }
}
