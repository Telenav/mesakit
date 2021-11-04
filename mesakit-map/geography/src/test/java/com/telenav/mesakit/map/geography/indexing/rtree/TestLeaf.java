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

import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.List;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

class TestLeaf extends Leaf<Polyline>
{
    ObjectList<Polyline> polylines = new ObjectList<>();

    public TestLeaf(RTreeSpatialIndex<Polyline> index, InteriorNode<Polyline> parent)
    {
        super(index, parent);
    }

    private TestLeaf()
    {
        super(null, null);
    }

    @Override
    public void addAll(List<Polyline> elements)
    {
        polylines.addAll(elements);
        bounds(Rectangle.fromBoundedObjects(elements));
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof TestLeaf)
        {
            var that = (TestLeaf) object;
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
    protected void addElement(Polyline element)
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
