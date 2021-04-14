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

package com.telenav.aonia.map.data.formats.pbf.osm;

import com.telenav.aonia.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfOsm;
import com.telenav.lexakai.annotations.UmlClassDiagram;

@UmlClassDiagram(diagram = DiagramPbfOsm.class)
public class Osm
{
    public static boolean isNavigable(final PbfWay way)
    {
        for (final var tag : way)
        {
            final var key = tag.getKey();
            if ("area".equalsIgnoreCase(key) || "landuse".equalsIgnoreCase(key))
            {
                return false;
            }
            if ("highway".equalsIgnoreCase(key))
            {
                return true;
            }
            if ("route".equalsIgnoreCase(key))
            {
                if ("ferry".equalsIgnoreCase(tag.getValue()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
