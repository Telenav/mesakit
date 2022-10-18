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

package com.telenav.mesakit.graph.specifications.common.shapepoint;

import com.telenav.kivakit.core.language.reflection.property.ExcludeProperty;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.ShapePoint;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;

public class HeavyWeightShapePoint extends ShapePoint
{
    private Location location;

    private PbfTagList pbfTags = PbfTagList.EMPTY;

    private int index;

    public HeavyWeightShapePoint(Graph graph, long identifier)
    {
        super(graph, identifier);
    }

    @Override
    @ExcludeProperty
    public int index()
    {
        return index;
    }

    @Override
    public void index(int index)
    {
        this.index = index;
    }

    @Override
    public boolean isHeavyWeight()
    {
        return true;
    }

    @Override
    public Location location()
    {
        return location;
    }

    public void location(Location location)
    {
        this.location = location;
    }

    public void pbfTags(PbfTagList pbfTags)
    {
        this.pbfTags = pbfTags;
    }

    @Override
    public PbfTagList tagList()
    {
        return pbfTags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[ShapePoint identifier = " + identifier() + "]";
    }
}
