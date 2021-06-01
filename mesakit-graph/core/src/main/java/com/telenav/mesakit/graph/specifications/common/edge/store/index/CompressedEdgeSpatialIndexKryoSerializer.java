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

import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
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
    private final Graph graph;

    public CompressedEdgeSpatialIndexKryoSerializer(final Graph graph)
    {
        this.graph = graph;
    }

    @Override
    public RTreeSpatialIndex<Edge> onRead(final KryoSerializationSession kryo)
    {
        final var index = (CompressedEdgeSpatialIndex) super.onRead(kryo);
        index.edges = kryo.readObject(CompressedEdgeListStore.class);
        return index;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public void onWrite(final KryoSerializationSession kryo, final RTreeSpatialIndex index)
    {
        super.onWrite(kryo, index);
        final var edgeSpatialIndex = (CompressedEdgeSpatialIndex) index;
        final var edges = edgeSpatialIndex.edges;
        kryo.writeObject(edges);
    }

    @Override
    protected CompressedEdgeSpatialIndex newSpatialIndex(final RTreeSettings settings)
    {
        return new CompressedEdgeSpatialIndex(objectName(), this.graph, settings);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected Leaf readLeaf(final KryoSerializationSession kryo,
                            final RTreeSpatialIndex index,
                            final InteriorNode parent)
    {
        final var leaf = new CompressedLeaf(index, parent);
        leaf.list = kryo.readObject(int.class);
        return leaf;
    }

    @Override
    protected void writeLeaf(final KryoSerializationSession kryo,
                             final Leaf<Edge> leaf)
    {
        final var kryoLeaf = (CompressedLeaf) leaf;
        kryo.writeObject(kryoLeaf.list);
    }
}
