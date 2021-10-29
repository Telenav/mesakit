////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.osm.OsmHighwayTag;
import com.telenav.mesakit.map.road.model.RoadSubType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoadSubTypeExtractor extends BaseExtractor<RoadSubType, PbfWay>
{
    public static final Map<String, RoadSubType> roadSubTypeForHighway = new HashMap<>();

    private static final Map<RoadSubType, Integer> priorityForRoadSubType = new HashMap<>();

    static
    {
        roadSubTypeForHighway.put(OsmHighwayTag.MOTORWAY, RoadSubType.SEPARATED_MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.MOTORWAY_LINK, RoadSubType.RAMP);
        roadSubTypeForHighway.put(OsmHighwayTag.TRUNK, RoadSubType.SEPARATED_MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.TRUNK_LINK, RoadSubType.CONNECTING_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PRIMARY_LINK, RoadSubType.CONNECTING_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.SECONDARY_LINK, RoadSubType.CONNECTING_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.RESIDENTIAL_LINK, RoadSubType.CONNECTING_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.TERTIARY_LINK, RoadSubType.CONNECTING_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PRIMARY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.SECONDARY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.RESIDENTIAL, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.TERTIARY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.UNCLASSIFIED, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.SERVICE, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.REST_AREA, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.SERVICES, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.ROAD, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.TRACK, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.UNDEFINED, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.UNKNOWN, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.LIVING_STREET, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PRIVATE, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.DRIVEWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.FOOTWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PEDESTRIAN, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.STEPS, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.BRIDLEWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.CONSTRUCTION, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PATH, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.CYCLEWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.BUS_GUIDEWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.MINOR, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.TURNING_CIRCLE, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.BYWAY, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.UNSURFACED, RoadSubType.MAIN_ROAD);
        roadSubTypeForHighway.put(OsmHighwayTag.PROPOSED, RoadSubType.MAIN_ROAD);
    }

    static
    {
        priorityForRoadSubType.put(RoadSubType.RAMP, 1);
        priorityForRoadSubType.put(RoadSubType.SERVICE_ROAD, 2);
        priorityForRoadSubType.put(RoadSubType.OPEN_TRAFFIC_AREA, 2);
        priorityForRoadSubType.put(RoadSubType.INTERSECTION_LINK, 3);
        priorityForRoadSubType.put(RoadSubType.CONNECTING_ROAD, 4);
        priorityForRoadSubType.put(RoadSubType.ROUNDABOUT, 5);
        priorityForRoadSubType.put(RoadSubType.OVERBRIDGE, 6);
        priorityForRoadSubType.put(RoadSubType.UNDERPASS, 6);
        priorityForRoadSubType.put(RoadSubType.TUNNEL, 6);
        priorityForRoadSubType.put(RoadSubType.BRIDGE, 6);
        priorityForRoadSubType.put(RoadSubType.MAIN_ROAD, 7);
        priorityForRoadSubType.put(RoadSubType.SEPARATED_MAIN_ROAD, 7);
        priorityForRoadSubType.put(RoadSubType.FUNCTIONAL_SPECIAL_ROAD, 7);
    }

    public RoadSubTypeExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public RoadSubType onExtract(PbfWay way)
    {
        var roadSubType = way.tagValueAsNaturalNumber("rst");
        if (roadSubType >= 0)
        {
            return RoadSubType.forIdentifier(roadSubType);
        }

        Set<RoadSubType> subtypes = new HashSet<>();
        if (way.tagValueIs("route", "ferry"))
        {
            subtypes.add(RoadSubType.MAIN_ROAD);
        }
        if (way.tagValueIs("junction", "roundabout"))
        {
            subtypes.add(RoadSubType.ROUNDABOUT);
        }
        if (way.tagValueIsYes("tunnel"))
        {
            subtypes.add(RoadSubType.TUNNEL);
        }
        if (way.tagValueIsYes("bridge"))
        {
            subtypes.add(RoadSubType.BRIDGE);
        }
        for (var highway : way.tagValueSplit("highway"))
        {
            highway = highway.trim();
            if ("trunk_link".equals(highway) || "primary_link".equals(highway))
            {
                subtypes.add(RoadSubType.CONNECTING_ROAD);
            }
            var subtype = roadSubTypeForHighway.get(highway);
            if (subtype == null)
            {
                glitch("No road subtype inferred for highway tag '$'", highway);
            }
            else
            {
                subtypes.add(roadSubTypeForHighway.get(highway));
            }
        }
        return choose(subtypes);
    }

    private RoadSubType choose(Set<RoadSubType> subtypes)
    {
        RoadSubType chosen = null;
        var minimumPriority = 9;
        for (var subtype : subtypes)
        {
            var priority = priorityForRoadSubType.get(subtype);
            if (priority == null)
            {
                glitch("No priority for $", subtype);
            }
            else if (priority < minimumPriority)
            {
                chosen = subtype;
                minimumPriority = priority;
            }
        }
        return chosen;
    }
}
