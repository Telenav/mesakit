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

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.List;

public class UncompressedLeaf<T extends Bounded & Intersectable> extends Leaf<T>
{
    List<T> elements;

    public UncompressedLeaf(RTreeSpatialIndex<T> index, InteriorNode<T> parent)
    {
        super(index, parent);
    }

    @Override
    public void addAll(List<T> elements)
    {
        elements().addAll(elements);
        bounds(Rectangle.fromBoundedObjects(elements()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object object)
    {
        if (object instanceof UncompressedLeaf)
        {
            var that = (UncompressedLeaf) object;
            return super.equals(that) && elements().equals(that.elements());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(super.hashCode(), elements);
    }

    @Override
    protected void addElement(T element)
    {
        elements().add(element);
        bounds(Rectangle.fromBoundedObjects(elements));
    }

    @Override
    protected List<T> elements()
    {
        if (elements == null)
        {
            elements = new ObjectList<>();
        }
        return elements;
    }

    @Override
    protected int size()
    {
        return elements().size();
    }
}
