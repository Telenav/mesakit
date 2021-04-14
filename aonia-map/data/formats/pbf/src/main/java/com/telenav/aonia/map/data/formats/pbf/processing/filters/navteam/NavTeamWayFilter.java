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

package com.telenav.aonia.map.data.formats.pbf.processing.filters.navteam;

import com.telenav.aonia.map.data.formats.pbf.osm.OsmHighwayTag;
import com.telenav.aonia.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfUniDb;
import com.telenav.lexakai.annotations.UmlClassDiagram;

/**
 * Important drivable roads
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfUniDb.class)
public class NavTeamWayFilter extends WayFilter
{
    public NavTeamWayFilter()
    {
        super("navteam-ways", "includes only motorway, primary, secondary, tertiary, trunk, residential, road, service and unclassified ways");

        include(OsmHighwayTag.MOTORWAY);
        include(OsmHighwayTag.MOTORWAY_LINK);
        include(OsmHighwayTag.PRIMARY);
        include(OsmHighwayTag.PRIMARY_LINK);
        include(OsmHighwayTag.SECONDARY);
        include(OsmHighwayTag.SECONDARY_LINK);
        include(OsmHighwayTag.TERTIARY);
        include(OsmHighwayTag.TERTIARY_LINK);
        include(OsmHighwayTag.TRUNK);
        include(OsmHighwayTag.TRUNK_LINK);
        include(OsmHighwayTag.RESIDENTIAL);
        include(OsmHighwayTag.ROAD);
        include(OsmHighwayTag.SERVICE);
        include(OsmHighwayTag.UNCLASSIFIED);
    }
}
