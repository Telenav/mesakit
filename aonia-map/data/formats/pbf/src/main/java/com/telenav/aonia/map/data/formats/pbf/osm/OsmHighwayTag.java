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

import com.telenav.aonia.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfOsm;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.Arrays;
import java.util.List;

/**
 * Recognized osm highway tags
 *
 * @author matthieun
 */
@UmlClassDiagram(diagram = DiagramPbfOsm.class)
public class OsmHighwayTag
{
    public static final String KEY = "highway";

    public static final String MOTORWAY = "motorway";

    public static final String MOTORWAY_LINK = "motorway_link";

    public static final String TRUNK = "trunk";

    public static final String TRUNK_LINK = "trunk_link";

    public static final String PRIMARY = "primary";

    public static final String PRIMARY_LINK = "primary_link";

    public static final String SECONDARY = "secondary";

    public static final String SECONDARY_LINK = "secondary_link";

    public static final String TERTIARY = "tertiary";

    public static final String TERTIARY_LINK = "tertiary_link";

    public static final String RESIDENTIAL = "residential";

    public static final String RESIDENTIAL_LINK = "residential_link";

    public static final String UNCLASSIFIED = "unclassified";

    public static final String SERVICE = "service";

    public static final String REST_AREA = "rest_area";

    public static final String SERVICES = "services";

    public static final String ROAD = "road";

    public static final String TRACK = "track";

    public static final String UNDEFINED = "undefined";

    public static final String UNKNOWN = "unknown";

    public static final String LIVING_STREET = "living_street";

    public static final String PRIVATE = "private";

    public static final String DRIVEWAY = "driveway";

    public static final String FOOTWAY = "footway";

    public static final String PEDESTRIAN = "pedestrian";

    public static final String STEPS = "steps";

    public static final String BRIDLEWAY = "bridleway";

    public static final String CONSTRUCTION = "construction";

    public static final String PATH = "path";

    public static final String CYCLEWAY = "cycleway";

    public static final String BUS_GUIDEWAY = "bus_guideway";

    public static final String MINOR = "minor";

    public static final String TURNING_CIRCLE = "turning_circle";

    public static final String BYWAY = "byway";

    public static final String UNSURFACED = "unsurfaced";

    public static final String PLATFORM = "platform";

    public static final String ABANDONED = "abandoned";

    public static final String RAZED = "razed";

    public static final String RACEWAY = "raceway";

    public static final String PLANNED = "planned";

    public static final String PROPOSED = "proposed";

    public static final String PROPOSAL = "proposal";

    public static final String HISTORIC = "historic";

    public static final String ESCALATOR = "escalator";

    public static final String ELEVATOR = "elevator";

    public static final String DISMANTLED = "dismantled";

    public static final String DISUSED = "disused";

    public static final String BUS_STOP = "bus_stop";

    public static final String HALLWAY = "hallway";

    public static final String FORD = "ford";

    public static final String CONVEYOR = "conveyor";

    public static final String CROSSING = "crossing";

    public static final String PUBLIC_TRANSPORT = "public_transport";

    public static final String TRAIL = "trail";

    public static final String CLOSED = "closed";

    public static final String WALKWAY = "walkway";

    public static final String OLD = "old";

    public static final String STREET_LAMP = "street_lamp";

    public static final String STEPPING_STONES = "stepping_stones";

    public static final String KERB = "kerb";

    public static final String LAYBY = "layby";

    public static final String GIVE_WAY = "give_way";

    public static final String ESCAPE = "escape";

    public static final String GALLOP = "gallop";

    public static final String PASSING_PLACE = "passing_place";

    public static final String TOWPATH = "towpath";

    public static final String SIDEWALK = "sidewalk";

    public static final String CORRIDOR = "corridor";

    public static final String ACCESS = "access";

    public static final String NO = "no";

    public static final String DEPOT = "depot";

    public static final String VERGE = "verge";

    public static final String MINI_ROUNDABOUT = "mini_roundabout";

    public static final String MOTORWAY_JUNCTION = "motorway_junction";

    public static final String[] CORE_WAYS = { MOTORWAY, MOTORWAY_LINK, TRUNK, TRUNK_LINK, PRIMARY,
            PRIMARY_LINK, SECONDARY, SECONDARY_LINK, TERTIARY, TERTIARY_LINK, UNCLASSIFIED, RESIDENTIAL, SERVICE,
            SERVICES,
            LIVING_STREET, PEDESTRIAN, TRACK, BUS_GUIDEWAY, RACEWAY, ROAD, STEPS, FOOTWAY, CONSTRUCTION, PROPOSED,
            CYCLEWAY,
            BRIDLEWAY, REST_AREA };

    public static final String[] CORE_NODES = { BUS_STOP, CROSSING, ESCAPE, GIVE_WAY, MINI_ROUNDABOUT,
            TURNING_CIRCLE, MOTORWAY_JUNCTION, PASSING_PLACE, REST_AREA };

    public static final List<String> NAVIGABLE_WAYS = Arrays.asList(MOTORWAY, MOTORWAY_LINK, TRUNK, TRUNK_LINK, PRIMARY,
            PRIMARY_LINK, SECONDARY, SECONDARY_LINK, RESIDENTIAL, RESIDENTIAL_LINK, TERTIARY, TERTIARY_LINK, ROAD, TRACK,
            UNCLASSIFIED, UNDEFINED, UNKNOWN, LIVING_STREET, PRIVATE);

    public static final List<String> METRICS_WAYS = Arrays.asList(MOTORWAY, MOTORWAY_LINK, TRUNK, TRUNK_LINK, PRIMARY,
            PRIMARY_LINK, SECONDARY, SECONDARY_LINK, TERTIARY, TERTIARY_LINK, RESIDENTIAL, RESIDENTIAL_LINK, UNCLASSIFIED,
            SERVICE, TRACK, FOOTWAY, PEDESTRIAN, STEPS, BRIDLEWAY, CONSTRUCTION, CYCLEWAY, PATH, BUS_GUIDEWAY);

    public static boolean isNavigableHighway(final Tag tag)
    {
        return KEY.equalsIgnoreCase(tag.getKey()) && NAVIGABLE_WAYS.contains(tag.getValue());
    }

    public static boolean isNullServiceWay(final Tag tag)
    {
        return SERVICE.equalsIgnoreCase(tag.getKey()) && null == tag.getValue();
    }

    public static boolean isPrivateAccess(final Tag tag)
    {
        return ACCESS.equalsIgnoreCase(tag.getKey()) && PRIVATE.equalsIgnoreCase(tag.getValue());
    }

    public static boolean isServiceWay(final Tag tag)
    {
        return KEY.equalsIgnoreCase(tag.getKey()) && SERVICE.equalsIgnoreCase(tag.getValue());
    }
}
