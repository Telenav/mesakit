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

package com.telenav.mesakit.map.region.border;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndexKryoSerializer;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.kivakit.core.kernel.interfaces.lifecycle.Configured;
import com.telenav.kivakit.core.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.core.serialization.kryo.KryoSerializationSession;

public class BorderSpatialIndexKryoSerializer<T extends Region<T>> extends RTreeSpatialIndexKryoSerializer<Border<T>> implements Configured<BorderCache.Settings<T>>
{
    private BorderCache.Settings<T> settings;

    @Override
    public void configure(final BorderCache.Settings<T> settings)
    {
        this.settings = settings;
    }

    @Override
    protected BorderSpatialIndex<T> newSpatialIndex(final RTreeSettings settings)
    {
        return new BorderSpatialIndex<>("kryo", settings);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Leaf readLeaf(final KryoSerializationSession session,
                            final RTreeSpatialIndex index,
                            final InteriorNode parent)
    {
        final var leaf = new BorderLeaf<T>(index, parent);
        final int size = session.readObject(int.class);
        for (var i = 0; i < size; i++)
        {
            // Populate the region identity and the border polygon.
            final var identity = session.readObject(RegionIdentity.class);
            final var polygon = session.readObject(Polygon.class);
            if (identity.isValid())
            {
                final var region = settings.regionFactory().newInstance(identity);
                if (region != null)
                {
                    final var border = new Border<>(region, polygon);
                    leaf.borders.add(border);
                }
            }
        }
        if (leaf.borders.isEmpty())
        {
            leaf.bounds(Rectangle.fromLocation(Location.ORIGIN));
        }
        else
        {
            leaf.bounds(Rectangle.fromBoundedObjects(leaf.borders));
        }
        return leaf;
    }

    @Override
    protected void writeLeaf(final KryoSerializationSession session,
                             final Leaf<Border<T>> leaf)
    {
        final Iterable<Border<T>> borders = ((BorderLeaf<T>) leaf).borders;
        final var size = new MutableCount();
        for (final var border : borders)
        {
            if (border.isValid())
            {
                size.increment();
            }
        }
        session.writeObject((int) size.asLong());
        for (final var border : borders)
        {
            if (border.region().isValid())
            {
                session.writeObject(border.identity());
                session.writeObject(border.polygon());
            }
        }
    }
}
