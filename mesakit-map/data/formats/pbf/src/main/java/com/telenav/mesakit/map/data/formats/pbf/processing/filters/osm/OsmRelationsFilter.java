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

package com.telenav.mesakit.map.data.formats.pbf.processing.filters.osm;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfOsm;

import java.util.HashSet;
import java.util.Set;

@UmlClassDiagram(diagram = DiagramPbfOsm.class)
public class OsmRelationsFilter extends RelationFilter
{
    private static final Set<String> acceptableRelationTypes = new HashSet<>();

    static
    {
        acceptableRelationTypes.add("restriction");
        acceptableRelationTypes.add("route");
    }

    public OsmRelationsFilter()
    {
        super("osm-relations", "relations that are useful in OSM data processing");
    }

    @Override
    public boolean accepts(PbfRelation relation)
    {
        if (!relation.hasTags())
        {
            return false;
        }
        for (var tag : relation)
        {
            if (acceptableRelationTypes.contains(tag.getValue()))
            {
                return true;
            }
        }
        return false;
    }
}
