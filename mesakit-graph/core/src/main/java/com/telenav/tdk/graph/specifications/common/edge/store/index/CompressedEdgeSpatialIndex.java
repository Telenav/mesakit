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

package com.telenav.kivakit.graph.specifications.common.edge.store.index;

import com.telenav.kivakit.kernel.interfaces.collection.Compressible;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.map.geography.indexing.rtree.*;

import static com.telenav.kivakit.kernel.validation.Validate.unsupported;

/**
 * Compressed spatial index for edges.
 *
 * @author jonathanl (shibo)
 */
public class CompressedEdgeSpatialIndex extends RTreeSpatialIndex<Edge> implements Compressible
{
    CompressedEdgeListStore edges;

    public CompressedEdgeSpatialIndex(final String objectName, final Graph graph, final RTreeSettings settings)
    {
        super(objectName, settings);

        edges = new CompressedEdgeListStore(objectName() + ".edges", graph);
    }

    protected CompressedEdgeSpatialIndex()
    {
    }

    @Override
    public void add(final Edge element)
    {
        unsupported("CompressedEdgeSpatialIndex only supports bulk loading of elements");
    }

    @Override
    public Method compress(final Method method)
    {
        return edges.compress(method);
    }

    @Override
    public Method compressionMethod()
    {
        return edges.compressionMethod();
    }

    public void graph(final Graph graph)
    {
        edges.graph(graph);
    }

    @Override
    public Leaf<Edge> newLeaf(final InteriorNode<Edge> parent)
    {
        return new CompressedLeaf(this, parent);
    }
}
