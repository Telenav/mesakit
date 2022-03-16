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
import com.telenav.mesakit.map.data.formats.pbf.osm.OsmHighwayTag;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfOsm;

/**
 * PbfFilters out highway types that we never want to consider.
 *
 * @author jonathanl (shibo)
 */
@UmlClassDiagram(diagram = DiagramPbfOsm.class)
public class OsmExcludeNonDrivableWaysFilter extends WayFilter
{
    public OsmExcludeNonDrivableWaysFilter()
    {
        super("osm-exclude-non-drivable-ways",
                "excludes 30+ types of non-drivable ways including crossings, raceways, walkways, sidewalks and trails");
        exclude(OsmHighwayTag.PLATFORM);
        exclude(OsmHighwayTag.ABANDONED);
        exclude(OsmHighwayTag.RAZED);
        exclude(OsmHighwayTag.RACEWAY);
        exclude(OsmHighwayTag.PLANNED);
        exclude(OsmHighwayTag.PROPOSED);
        exclude(OsmHighwayTag.PROPOSAL);
        exclude(OsmHighwayTag.HISTORIC);
        exclude(OsmHighwayTag.ESCALATOR);
        exclude(OsmHighwayTag.ELEVATOR);
        exclude(OsmHighwayTag.DISMANTLED);
        exclude(OsmHighwayTag.DISUSED);
        exclude(OsmHighwayTag.BUS_STOP);
        exclude(OsmHighwayTag.HALLWAY);
        exclude(OsmHighwayTag.FORD);
        exclude(OsmHighwayTag.CONVEYOR);
        exclude(OsmHighwayTag.CROSSING);
        exclude(OsmHighwayTag.PUBLIC_TRANSPORT);
        exclude(OsmHighwayTag.TRAIL);
        exclude(OsmHighwayTag.CLOSED);
        exclude(OsmHighwayTag.WALKWAY);
        exclude(OsmHighwayTag.OLD);
        exclude(OsmHighwayTag.STREET_LAMP);
        exclude(OsmHighwayTag.STEPPING_STONES);
        exclude(OsmHighwayTag.KERB);
        exclude(OsmHighwayTag.LAYBY);
        exclude(OsmHighwayTag.GIVE_WAY);
        exclude(OsmHighwayTag.ESCAPE);
        exclude(OsmHighwayTag.GALLOP);
        exclude(OsmHighwayTag.PASSING_PLACE);
        exclude(OsmHighwayTag.TOWPATH);
        exclude(OsmHighwayTag.SIDEWALK);
        exclude(OsmHighwayTag.CORRIDOR);
        exclude(OsmHighwayTag.ACCESS);
        exclude(OsmHighwayTag.NO);
        exclude(OsmHighwayTag.DEPOT);
        exclude(OsmHighwayTag.VERGE);
    }
}
