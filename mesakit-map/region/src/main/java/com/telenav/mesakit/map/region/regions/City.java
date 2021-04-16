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

import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegions;
import com.telenav.lexakai.annotations.UmlClassDiagram;

/**
 * @author Jianbo Chen
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramRegions.class)
public class City extends Region<City>
{
    public static City forIdentity(final RegionIdentity identity)
    {
        return type(City.class).forIdentity(identity);
    }

    public static City forRegionCode(final RegionCode code)
    {
        return type(City.class).forRegionCode(code);
    }

    public City(final State state, final RegionInstance<City> instance)
    {
        super(state, instance.prefix("City").prefix(state));
    }

    @Override
    public Class<?> subclass()
    {
        return City.class;
    }
}
