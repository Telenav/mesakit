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
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegions;

/**
 * NOTE: Full district support is still in the future.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramRegions.class)
public class District extends Region<District>
{
    public District(City city, RegionInstance<District> instance)
    {
        super(city, instance.prefix(city));
    }

    public City city()
    {
        return (City) parent();
    }

    @Override
    public Class<?> subclass()
    {
        return District.class;
    }
}
