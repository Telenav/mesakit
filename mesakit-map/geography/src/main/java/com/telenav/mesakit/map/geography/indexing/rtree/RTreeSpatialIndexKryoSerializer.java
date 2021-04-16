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

import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.kivakit.core.serialization.kryo.KryoSerializationSession;
import com.telenav.kivakit.core.serialization.kryo.KryoSerializer;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class RTreeSpatialIndexKryoSerializer<T extends Bounded & Intersectable> extends KryoSerializer<RTreeSpatialIndex>
{
    public enum NodeType
    {
        INTERIOR_NODE,
        LEAF
    }

    protected RTreeSpatialIndexKryoSerializer()
    {
        super(RTreeSpatialIndex.class);
    }

    @Override
    public RTreeSpatialIndex<T> onRead(final KryoSerializationSession session)
    {
        final var settings = session.readObject(RTreeSettings.class);
        final RTreeSpatialIndex<T> index = newSpatialIndex(settings);
        index.root = readNode(session, index, null);
        return index;
    }

    @Override
    public void onWrite(final KryoSerializationSession session, final RTreeSpatialIndex index)
    {
        session.writeObject(index.settings());
        writeNode(session, index.root);
    }

    protected abstract RTreeSpatialIndex newSpatialIndex(RTreeSettings settings);

    protected Leaf readLeaf(final KryoSerializationSession session,
                            final RTreeSpatialIndex index,
                            final InteriorNode parent)
    {
        final var leaf = new UncompressedLeaf(index, parent);
        leaf.elements = session.readList(type());
        return leaf;
    }

    protected void writeLeaf(final KryoSerializationSession session, final Leaf<T> leaf)
    {
        session.writeList(((UncompressedLeaf) leaf).elements, type());
    }

    private InteriorNode readInteriorNode(final KryoSerializationSession session,
                                          final RTreeSpatialIndex index,
                                          final InteriorNode parent)
    {
        final var node = new InteriorNode(index, parent);
        final int size = session.readObject(int.class);
        for (var i = 0; i < size; i++)
        {
            node.children.add(readNode(session, index, node));
        }
        return node;
    }

    private Node readNode(final KryoSerializationSession session,
                          final RTreeSpatialIndex index,
                          final InteriorNode parent)
    {
        final var type = session.readObject(NodeType.class);
        final long bottomLeft = session.readObject(long.class);
        final long topRight = session.readObject(long.class);
        final Node node;
        switch (type)
        {
            case INTERIOR_NODE:
                node = readInteriorNode(session, index, parent);
                break;

            case LEAF:
                node = readLeaf(session, index, parent);
                break;

            default:
                return fail("Unsupported node type");
        }
        node.bottomLeft = bottomLeft;
        node.topRight = topRight;
        return node;
    }

    private void writeInteriorNode(final KryoSerializationSession session, final InteriorNode node)
    {
        final var size = node.children.size();
        session.writeObject(size);
        for (var i = 0; i < size; i++)
        {
            writeNode(session, (Node) node.children.get(i));
        }
    }

    private void writeNode(final KryoSerializationSession session, final Node node)
    {
        if (node instanceof InteriorNode)
        {
            session.writeObject(NodeType.INTERIOR_NODE);
            session.writeObject(node.bottomLeft);
            session.writeObject(node.topRight);
            writeInteriorNode(session, (InteriorNode) node);
        }
        else if (node instanceof Leaf)
        {
            session.writeObject(NodeType.LEAF);
            session.writeObject(node.bottomLeft);
            session.writeObject(node.topRight);
            writeLeaf(session, (Leaf) node);
        }
    }
}
