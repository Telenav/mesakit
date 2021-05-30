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

package com.telenav.tdk.graph.specifications.common.graph.loader.extractors;

import com.telenav.tdk.core.data.extraction.BaseExtractor;
import com.telenav.tdk.core.kernel.language.primitive.Ints;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfWay;
import com.telenav.tdk.map.road.model.RoadFunctionalClass;

import java.util.*;

import static com.telenav.tdk.map.road.model.RoadFunctionalClass.*;

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

    public RoadFunctionalClassExtractor(final Listener<Message> listener)
    {
        super(listener);
    }

    @Override
    public RoadFunctionalClass onExtract(final PbfWay way)
    {
        final var hereFunctionalClass = way.tagValue("functional_class");
        if (hereFunctionalClass != null)
        {
            final var value = Ints.parse(hereFunctionalClass);
            if (value != Ints.INVALID)
            {
                return RoadFunctionalClass.forInvertedIdentifier(value);
            }
        }

        final var navteqFunctionalClass = way.tagValue("fc");
        if (navteqFunctionalClass != null)
        {
            final var value = Ints.parse(navteqFunctionalClass);
            if (value != Ints.INVALID)
            {
                return RoadFunctionalClass.forIdentifier(value);
            }
        }

        for (final var highway : way.tagValueSplit("highway"))
        {
            final var _class = roadFunctionalClassForHighway.get(highway);
            if (_class != null)
            {
                return _class;
            }
        }

        return way.tagValueIs("route", "ferry") ? FOURTH_CLASS : UNKNOWN;
    }
}
