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

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.geography.indexing.rtree.InteriorNode;
import com.telenav.mesakit.map.geography.indexing.rtree.Leaf;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.List;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * Leaf of a compressed edge spatial index
 *
 * @author jonathanl (shibo)
 */
public class CompressedLeaf extends Leaf<Edge>
{
    int list;

    CompressedLeaf(RTreeSpatialIndex<Edge> index, InteriorNode<Edge> parent)
    {
        super(index, parent);
    }

    private CompressedLeaf()
    {
    }

    @Override
    public void addAll(List<Edge> list)
    {
        this.list = edges().add(list);
        bounds(Rectangle.fromBoundedObjects(list));
    }

    @Override
    protected void addElement(Edge element)
    {
        unsupported();
    }

    @Override
    protected List<Edge> elements()
    {
        return edges().get(list);
    }

    @Override
    protected int size()
    {
        return elements().size();
    }

    private CompressedEdgeListStore edges()
    {
        return ((CompressedEdgeSpatialIndex) index()).edges;
    }
}
