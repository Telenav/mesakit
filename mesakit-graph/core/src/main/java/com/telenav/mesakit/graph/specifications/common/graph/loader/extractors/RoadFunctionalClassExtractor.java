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
import com.telenav.kivakit.kernel.language.primitives.Ints;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.mesakit.map.road.model.RoadFunctionalClass.FOURTH_CLASS;
import static com.telenav.mesakit.map.road.model.RoadFunctionalClass.UNKNOWN;

public class RoadFunctionalClassExtractor extends BaseExtractor<RoadFunctionalClass, PbfWay>
{
    private static final Map<String, RoadFunctionalClass> roadFunctionalClassForHighway = new HashMap<>();

    static
    {
        roadFunctionalClassForHighway.put("motorway", RoadFunctionalClass.MAIN);
        roadFunctionalClassForHighway.put("motorway_link", RoadFunctionalClass.MAIN);
        roadFunctionalClassForHighway.put("trunk", RoadFunctionalClass.FIRST_CLASS);
        roadFunctionalClassForHighway.put("trunk_link", RoadFunctionalClass.FIRST_CLASS);
        roadFunctionalClassForHighway.put("primary", RoadFunctionalClass.FIRST_CLASS);
        roadFunctionalClassForHighway.put("primary_link", RoadFunctionalClass.FIRST_CLASS);
        roadFunctionalClassForHighway.put("secondary", RoadFunctionalClass.SECOND_CLASS);
        roadFunctionalClassForHighway.put("secondary_link", RoadFunctionalClass.SECOND_CLASS);
        roadFunctionalClassForHighway.put("tertiary", RoadFunctionalClass.THIRD_CLASS);
        roadFunctionalClassForHighway.put("tertiary_link", RoadFunctionalClass.THIRD_CLASS);
        roadFunctionalClassForHighway.put("residential", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("residential_link", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("unclassified", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("service", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("rest_area", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("services", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("road", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("track", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("undefined", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("unknown", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("living_street", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("private", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("driveway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("footway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("pedestrian", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("steps", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("bridleway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("construction", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("path", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("cycleway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("bus_guideway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("minor", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("turning_circle", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("byway", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("unsurfaced", FOURTH_CLASS);
        roadFunctionalClassForHighway.put("proposed", FOURTH_CLASS);
    }

    public RoadFunctionalClassExtractor(Listener listener)
    {
        super(listener);
    }

    @Override
    public RoadFunctionalClass onExtract(PbfWay way)
    {
        var hereFunctionalClass = way.tagValue("functional_class");
        if (hereFunctionalClass != null)
        {
            var value = Ints.parse(hereFunctionalClass);
            if (value != Ints.INVALID)
            {
                return RoadFunctionalClass.forInvertedIdentifier(value);
            }
        }

        var navteqFunctionalClass = way.tagValue("fc");
        if (navteqFunctionalClass != null)
        {
            var value = Ints.parse(navteqFunctionalClass);
            if (value != Ints.INVALID)
            {
                return RoadFunctionalClass.forIdentifier(value);
            }
        }

        for (var highway : way.tagValueSplit("highway"))
        {
            var _class = roadFunctionalClassForHighway.get(highway);
            if (_class != null)
            {
                return _class;
            }
        }

        return way.tagValueIs("route", "ferry") ? FOURTH_CLASS : UNKNOWN;
    }
}
