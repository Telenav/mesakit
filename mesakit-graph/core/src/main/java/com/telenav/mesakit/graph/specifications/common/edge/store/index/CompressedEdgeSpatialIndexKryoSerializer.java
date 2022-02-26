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

package com.telenav.mesakit.graph.specifications.common.edge.store.index;

import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSession;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndexKryoSerializer;

/**
 * Kryo serializer for compressed edge spatial index
 *
 * @author jonathanl (shibo)
 */
public class CompressedEdgeSpatialIndexKryoSerializer extends RTreeSpatialIndexKryoSerializer<Edge> implements NamedObject
{
    public static final int IDENTIFIER = 333;

    private final Graph graph;

    public CompressedEdgeSpatialIndexKryoSerializer(Graph graph)
    {
        this.graph = graph;
    }

    @Override
    public RTreeSpatialIndex<Edge> onRead(KryoSerializationSession kryo)
    {
        var edgeSpatialIndex = (CompressedEdgeSpatialIndex) super.onRead(kryo);
        edgeSpatialIndex.edges = kryo.readObject(CompressedEdgeListStore.class);
        return edgeSpatialIndex;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void onWrite(KryoSerializationSession kryo, RTreeSpatialIndex index)
    {
        super.onWrite(kryo, index);
        var edgeSpatialIndex = (CompressedEdgeSpatialIndex) index;
        var edges = edgeSpatialIndex.edges;
        kryo.writeObject(edges);
    }

    @Override
    protected CompressedEdgeSpatialIndex newSpatialIndex(RTreeSettings settings)
    {
        return new CompressedEdgeSpatialIndex(objectName(), graph, settings);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Leaf readLeaf(KryoSerializationSession kryo,
                            RTreeSpatialIndex index,
                            InteriorNode parent)
    {
        var leaf = new CompressedLeaf(index, parent);
        leaf.list = kryo.readObject(int.class);
        return leaf;
    }

    @Override
    protected void writeLeaf(KryoSerializationSession kryo,
                             Leaf<Edge> leaf)
    {
        var kryoLeaf = (CompressedLeaf) leaf;
        kryo.writeObject(kryoLeaf.list);
    }
}
