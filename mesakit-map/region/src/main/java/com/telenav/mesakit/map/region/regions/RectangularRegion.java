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

package com.telenav.mesakit.map.region.regions;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.lexakai.DiagramRegions;
import com.telenav.mesakit.map.utilities.grid.GridCell;

@UmlClassDiagram(diagram = DiagramRegions.class)
public class RectangularRegion extends Region<RectangularRegion>
{
    private final GridCell cell;

    public RectangularRegion(GridCell cell)
    {
        super(null, new RegionInstance<>(RectangularRegion.class));
        this.cell = cell;
    }

    @Override
    public Rectangle bounds()
    {
        return cell;
    }

    @Override
    public Class<?> subclass()
    {
        return RectangularRegion.class;
    }
}
