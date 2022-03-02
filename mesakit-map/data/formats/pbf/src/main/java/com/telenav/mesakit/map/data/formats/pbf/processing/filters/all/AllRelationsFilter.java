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

package com.telenav.mesakit.map.data.formats.pbf.processing.filters.all;

import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfProcessingFilters;

/**
 * All ways
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfProcessingFilters.class)
public class AllRelationsFilter extends RelationFilter
{
    public AllRelationsFilter()
    {
        super("all", "includes all relations");
    }

    @Override
    public boolean accepts(PbfRelation relation)
    {
        return true;
    }
}
