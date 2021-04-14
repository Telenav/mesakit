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

package com.telenav.aonia.map.data.formats.pbf.processing.filters.unidb;

import com.telenav.aonia.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.aonia.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfUniDb;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.HashSet;
import java.util.Set;

@UmlClassDiagram(diagram = DiagramPbfUniDb.class)
public class UniDbRelationsFilter extends RelationFilter
{
    private static final Set<String> acceptableRelationTypes = new HashSet<>();

    static
    {
        acceptableRelationTypes.add("restriction");
        acceptableRelationTypes.add("traffic_signals");
        acceptableRelationTypes.add("traffic_sign");
        acceptableRelationTypes.add("lane_connectivity");
        acceptableRelationTypes.add("adas:maxspeed");
        acceptableRelationTypes.add("oneway");
        acceptableRelationTypes.add("construction");
        acceptableRelationTypes.add("adas_node");
        acceptableRelationTypes.add("bifurcation");
        acceptableRelationTypes.add("divided_junction");
        acceptableRelationTypes.add("junction_view");
        acceptableRelationTypes.add("gjv");
        acceptableRelationTypes.add("natural_guidance");
        acceptableRelationTypes.add("safety_camera");
        acceptableRelationTypes.add("signpost");
        acceptableRelationTypes.add("blackspot");
        acceptableRelationTypes.add("barrier");
    }

    public UniDbRelationsFilter()
    {
        super("unidb-relations", "relations that are useful in UniDb data processing");
    }

    @Override
    public boolean accepts(final PbfRelation relation)
    {
        if (!relation.hasTags())
        {
            return false;
        }
        for (final var tag : relation)
        {
            if (acceptableRelationTypes.contains(tag.getValue()))
            {
                return true;
            }
        }
        return false;
    }
}
