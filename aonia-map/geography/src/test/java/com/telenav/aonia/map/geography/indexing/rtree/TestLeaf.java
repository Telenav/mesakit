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

package com.telenav.aonia.map.geography.indexing.rtree;

import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.core.kernel.language.objects.Hash;

import java.util.List;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

class TestLeaf extends Leaf<Polyline>
{
    ObjectList<Polyline> polylines = new ObjectList<>();

    public TestLeaf(final RTreeSpatialIndex<Polyline> index, final InteriorNode<Polyline> parent)
    {
        super(index, parent);
    }

    private TestLeaf()
    {
        super(null, null);
    }

    @Override
    public void addAll(final List<Polyline> elements)
    {
        polylines.addAll(elements);
        bounds(Rectangle.fromBoundedObjects(elements));
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof TestLeaf)
        {
            final var that = (TestLeaf) object;
            return super.equals(that) && polylines.equals(that.polylines);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(super.hashCode(), polylines);
    }

    @Override
    protected void addElement(final Polyline element)
    {
        unsupported();
    }

    @Override
    protected List<Polyline> elements()
    {
        return polylines;
    }

    @Override
    protected int size()
    {
        return polylines.size();
    }
}
