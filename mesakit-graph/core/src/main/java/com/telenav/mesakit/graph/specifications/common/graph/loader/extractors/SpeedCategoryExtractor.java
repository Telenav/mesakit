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

import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.road.model.SpeedCategory;

import java.util.HashMap;
import java.util.Map;

public class SpeedCategoryExtractor extends BaseExtractor<SpeedCategory, PbfWay>
{
    private static final Map<String, SpeedCategory> speedCategoryForHighway = new HashMap<>();

    static
    {
        speedCategoryForHighway.put("motorway", SpeedCategory.forIdentifier(2));
        speedCategoryForHighway.put("motorway_link", SpeedCategory.forIdentifier(7));
        speedCategoryForHighway.put("trunk", SpeedCategory.forIdentifier(3));
        speedCategoryForHighway.put("trunk_link", SpeedCategory.forIdentifier(8));
        speedCategoryForHighway.put("primary", SpeedCategory.forIdentifier(5));
        speedCategoryForHighway.put("primary_link", SpeedCategory.forIdentifier(9));
        speedCategoryForHighway.put("secondary", SpeedCategory.forIdentifier(7));
        speedCategoryForHighway.put("secondary_link", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("residential", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("residential_link", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("service", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("rest_area", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("services", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("tertiary", SpeedCategory.forIdentifier(9));
        speedCategoryForHighway.put("tertiary_link", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("road", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("track", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("unclassified", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("undefined", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("unknown", SpeedCategory.forIdentifier(13));
        speedCategoryForHighway.put("living_street", SpeedCategory.forIdentifier(15));
        speedCategoryForHighway.put("private", SpeedCategory.forIdentifier(15));
        speedCategoryForHighway.put("driveway", SpeedCategory.forIdentifier(15));
        speedCategoryForHighway.put("footway", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("pedestrian", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("steps", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("bridleway", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("construction", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("path", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("cycleway", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("bus_guideway", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("minor", SpeedCategory.forIdentifier(16));
        speedCategoryForHighway.put("turning_circle", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("byway", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("unsurfaced", SpeedCategory.forIdentifier(10));
        speedCategoryForHighway.put("proposed", SpeedCategory.forIdentifier(10));
    }

    public SpeedCategoryExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public SpeedCategory onExtract(PbfWay way)
    {
        {
            var category = way.tagValueAsNaturalNumber("sc");
            if (category != Ints.INVALID)
            {
                return SpeedCategory.forIdentifier(category);
            }
        }

        for (var highway : way.highways())
        {
            var category = speedCategoryForHighway.get(highway);
            if (category != null)
            {
                return category;
            }
        }

        return way.tagValueIs("route", "ferry") ? SpeedCategory.forIdentifier(16) : SpeedCategory.forSpeed(Speed.milesPerHour(35));
    }
}
