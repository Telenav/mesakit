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

package com.telenav.mesakit.map.geography.indexing.rtree.test;

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSession;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndexKryoSerializer;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TestRTreeSpatialIndexSerializer extends RTreeSpatialIndexKryoSerializer<Polyline>
{
    @Override
    protected TestSpatialIndex newSpatialIndex(RTreeSettings settings)
    {
        return new TestSpatialIndex(settings);
    }

    @Override
    protected Leaf readLeaf(KryoSerializationSession session,
                            RTreeSpatialIndex index,
                            InteriorNode parent)
    {
        var leaf = new TestLeaf(index, parent);
        leaf.polylines = session.read(ObjectList.class);
        return leaf;
    }

    @Override
    protected void writeLeaf(KryoSerializationSession session,
                             Leaf leaf)
    {
        var compressedLeaf = (TestLeaf) leaf;
        session.write(compressedLeaf.polylines);
    }
}
