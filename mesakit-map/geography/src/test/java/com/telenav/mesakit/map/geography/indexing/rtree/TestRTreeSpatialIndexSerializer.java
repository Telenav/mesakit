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

package com.telenav.mesakit.map.geography.indexing.rtree;

import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.kivakit.core.serialization.kryo.KryoSerializationSession;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestRTreeSpatialIndexSerializer extends RTreeSpatialIndexKryoSerializer<Polyline>
{
    @Override
    protected TestSpatialIndex newSpatialIndex(final RTreeSettings settings)
    {
        return new TestSpatialIndex(settings);
    }

    @Override
    protected Leaf readLeaf(final KryoSerializationSession session,
                            final RTreeSpatialIndex index,
                            final InteriorNode parent)
    {
        final var leaf = new TestLeaf(index, parent);
        leaf.polylines = session.readList(Polyline.class);
        return leaf;
    }

    @Override
    protected void writeLeaf(final KryoSerializationSession session,
                             final Leaf leaf)
    {
        final var compressedLeaf = (TestLeaf) leaf;
        session.writeList(compressedLeaf.polylines, Polyline.class);
    }
}
