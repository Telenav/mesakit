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

package com.telenav.aonia.map.region.border;

import com.telenav.aonia.map.geography.indexing.rtree.InteriorNode;
import com.telenav.aonia.map.geography.indexing.rtree.Leaf;
import com.telenav.aonia.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.region.Region;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public class BorderLeaf<T extends Region<T>> extends Leaf<Border<T>>
{
    final ArrayList<Border<T>> borders;

    public BorderLeaf(final RTreeSpatialIndex<Border<T>> index, final InteriorNode<Border<T>> parent)
    {
        super(index, parent);
        borders = new ArrayList<>(index.settings().estimatedChildrenPerLeaf().asInt());
    }

    @Override
    public void addAll(final List<Border<T>> elements)
    {
        if (!elements.isEmpty())
        {
            for (final var border : elements)
            {
                assert border != null;
                assert border.identity() != null;
                assert border.region() != null;
                borders.add(border);
            }
            bounds(Rectangle.fromBoundedObjects(elements));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof BorderLeaf)
        {
            final BorderLeaf<T> that = (BorderLeaf<T>) object;
            assert borders.equals(that.borders);
            return borders.equals(that.borders);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return borders.hashCode();
    }

    @Override
    protected void addElement(final Border<T> element)
    {
        ensure(element != null);
        borders.add(element);
        bounds(Rectangle.fromBoundedObjects(borders));
    }

    @Override
    protected Iterable<Border<T>> elements()
    {
        return borders;
    }

    @Override
    protected int size()
    {
        return borders.size();
    }
}
