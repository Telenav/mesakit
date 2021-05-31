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

package com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors;

import com.telenav.kivakit.data.extraction.BaseExtractor;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfWay;
import com.telenav.kivakit.data.formats.pbf.osm.OsmHighwayTag;
import com.telenav.kivakit.kernel.language.primitive.Ints;
import com.telenav.mesakit.map.road.model.RoadType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RoadTypeExtractor extends BaseExtractor<RoadType, PbfWay>
{
    public static final Map<String, RoadType> roadTypeForHighway = new HashMap<>();

    private static final Map<RoadType, Integer> priorityForRoadType = new HashMap<>();

    static
    {
        roadTypeForHighway.put(OsmHighwayTag.MOTORWAY, RoadType.FREEWAY);
        roadTypeForHighway.put(OsmHighwayTag.MOTORWAY_LINK, RoadType.FREEWAY);
        roadTypeForHighway.put(OsmHighwayTag.TRUNK, RoadType.HIGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.TRUNK_LINK, RoadType.HIGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.PRIMARY, RoadType.THROUGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.PRIMARY_LINK, RoadType.THROUGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.SECONDARY, RoadType.THROUGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.SECONDARY_LINK, RoadType.THROUGHWAY);
        roadTypeForHighway.put(OsmHighwayTag.RESIDENTIAL, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.RESIDENTIAL_LINK, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.TERTIARY, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.TERTIARY_LINK, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.UNCLASSIFIED, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.SERVICE, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.REST_AREA, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.SERVICES, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.ROAD, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.TRACK, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.UNDEFINED, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.UNKNOWN, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.LIVING_STREET, RoadType.PRIVATE_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.PRIVATE, RoadType.PRIVATE_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.DRIVEWAY, RoadType.PRIVATE_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.FOOTWAY, RoadType.WALKWAY);
        roadTypeForHighway.put(OsmHighwayTag.PEDESTRIAN, RoadType.WALKWAY);
        roadTypeForHighway.put(OsmHighwayTag.STEPS, RoadType.WALKWAY);
        roadTypeForHighway.put(OsmHighwayTag.BRIDLEWAY, RoadType.NON_NAVIGABLE);
        roadTypeForHighway.put(OsmHighwayTag.CONSTRUCTION, RoadType.NON_NAVIGABLE);
        roadTypeForHighway.put(OsmHighwayTag.PATH, RoadType.NON_NAVIGABLE);
        roadTypeForHighway.put(OsmHighwayTag.CYCLEWAY, RoadType.RESERVED_1);
        roadTypeForHighway.put(OsmHighwayTag.BUS_GUIDEWAY, RoadType.PUBLIC_VEHICLE_ONLY);
        roadTypeForHighway.put(OsmHighwayTag.MINOR, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.TURNING_CIRCLE, RoadType.LOCAL_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.BYWAY, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.UNSURFACED, RoadType.LOW_SPEED_ROAD);
        roadTypeForHighway.put(OsmHighwayTag.PROPOSED, RoadType.NON_NAVIGABLE);
    }

    static
    {
        priorityForRoadType.put(RoadType.FERRY, 1);
        priorityForRoadType.put(RoadType.TRAIN, 1);
        priorityForRoadType.put(RoadType.PUBLIC_VEHICLE_ONLY, 2);
        priorityForRoadType.put(RoadType.PRIVATE_ROAD, 3);
        priorityForRoadType.put(RoadType.URBAN_HIGHWAY, 4);
        priorityForRoadType.put(RoadType.FREEWAY, 5);
        priorityForRoadType.put(RoadType.FRONTAGE_ROAD, 5);
        priorityForRoadType.put(RoadType.WALKWAY, 5);
        priorityForRoadType.put(RoadType.NON_NAVIGABLE, 6);
        priorityForRoadType.put(RoadType.HIGHWAY, 7);
        priorityForRoadType.put(RoadType.THROUGHWAY, 7);
        priorityForRoadType.put(RoadType.LOCAL_ROAD, 7);
        priorityForRoadType.put(RoadType.LOW_SPEED_ROAD, 7);
        priorityForRoadType.put(RoadType.LAYOUT, 8);
        priorityForRoadType.put(RoadType.RESERVED_1, 9);
        priorityForRoadType.put(RoadType.RESERVED_2, 9);
    }

    public RoadTypeExtractor(final Listener listener)
    {
        super(listener);
    }

    @Override
    public RoadType onExtract(final PbfWay way)
    {
        if (way.hasKey("rt"))
        {
            final var type = way.tagValueAsInt("rt");
            return type == Ints.INVALID ? null : RoadType.forIdentifier(type);
        }

        final Set<RoadType> types = new HashSet<>();
        if (way.tagValueIs("route", "ferry"))
        {
            types.add(RoadType.FERRY);
        }
        if (way.tagValueIs("access", "private"))
        {
            types.add(RoadType.PRIVATE_ROAD);
        }
        if (way.tagValueIs("access", "no"))
        {
            types.add(RoadType.NON_NAVIGABLE);
        }
        for (final var highway : way.tagValueSplit("highway"))
        {
            final var type = roadTypeForHighway.get(highway);
            if (type != null)
            {
                types.add(type);
            }
        }
        return choose(types);
    }

    private RoadType choose(final Set<RoadType> types)
    {
        RoadType chosen = null;
        var highestPriority = Integer.MAX_VALUE;
        for (final var type : types)
        {
            final var priority = priorityForRoadType.get(type);
            if (priority != null && (chosen == null || priority < highestPriority))
            {
                chosen = type;
                highestPriority = priority;
            }
        }
        return chosen;
    }
}
