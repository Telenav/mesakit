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

import com.telenav.kivakit.kernel.language.collections.CompressibleCollection;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * Compressed spatial index for edges.
 *
 * @author jonathanl (shibo)
 */
public class CompressedEdgeSpatialIndex extends RTreeSpatialIndex<Edge> implements CompressibleCollection
{
    CompressedEdgeListStore edges;

    public CompressedEdgeSpatialIndex(String objectName, Graph graph, RTreeSettings settings)
    {
        super(objectName, settings);

        edges = new CompressedEdgeListStore(objectName() + ".edges", graph);
    }

    protected CompressedEdgeSpatialIndex()
    {
    }

    @Override
    public void add(Edge element)
    {
        unsupported("CompressedEdgeSpatialIndex only supports bulk loading of elements");
    }

    @Override
    public Method compress(Method method)
    {
        return edges.compress(method);
    }

    @Override
    public Method compressionMethod()
    {
        return edges.compressionMethod();
    }

    public void graph(Graph graph)
    {
        edges.graph(graph);
    }

    @Override
    public Leaf<Edge> newLeaf(InteriorNode<Edge> parent)
    {
        return new CompressedLeaf(this, parent);
    }
}
