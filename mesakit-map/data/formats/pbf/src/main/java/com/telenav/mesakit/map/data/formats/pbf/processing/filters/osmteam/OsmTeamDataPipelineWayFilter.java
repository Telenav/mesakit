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

package com.telenav.mesakit.map.data.formats.pbf.processing.filters.osmteam;

import com.telenav.mesakit.map.data.formats.pbf.osm.OsmHighwayTag;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfOsm;
import com.telenav.lexakai.annotations.UmlClassDiagram;

/**
 * PbfFilters out highway types that we never want to consider.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfOsm.class)
public class OsmTeamDataPipelineWayFilter extends WayFilter
{
    public OsmTeamDataPipelineWayFilter()
    {
        super("osm-team-data-pipeline", "the way filter used by the data pipeline to process OSM data");
        include(OsmHighwayTag.MOTORWAY);
        include(OsmHighwayTag.MOTORWAY_LINK);
        include(OsmHighwayTag.TRUNK);
        include(OsmHighwayTag.TRUNK_LINK);
        include(OsmHighwayTag.PRIMARY);
        include(OsmHighwayTag.PRIMARY_LINK);
        include(OsmHighwayTag.SECONDARY);
        include(OsmHighwayTag.SECONDARY_LINK);
        include(OsmHighwayTag.RESIDENTIAL);
        include(OsmHighwayTag.RESIDENTIAL_LINK);
        include(OsmHighwayTag.SERVICE);
        include(OsmHighwayTag.TERTIARY);
        include(OsmHighwayTag.TERTIARY_LINK);
        include(OsmHighwayTag.ROAD);
        include(OsmHighwayTag.TRACK);
        include(OsmHighwayTag.UNCLASSIFIED);
        include(OsmHighwayTag.UNDEFINED);
        include(OsmHighwayTag.UNKNOWN);
        include(OsmHighwayTag.LIVING_STREET);
        include(OsmHighwayTag.PRIVATE);
        include(OsmHighwayTag.FOOTWAY);
        include(OsmHighwayTag.PEDESTRIAN);
        include(OsmHighwayTag.STEPS);
        include(OsmHighwayTag.BRIDLEWAY);
        include(OsmHighwayTag.CONSTRUCTION);
        include(OsmHighwayTag.CYCLEWAY);
        include(OsmHighwayTag.PATH);
        include(OsmHighwayTag.BUS_GUIDEWAY);
    }
}
