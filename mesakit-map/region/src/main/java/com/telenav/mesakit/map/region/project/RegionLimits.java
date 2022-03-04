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

package com.telenav.mesakit.map.region.project;

import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.messaging.Listener;

/**
 * Limits of ways nodes and relations in a single PBF or graph
 *
 * @author jonathanl (shibo)
 */
public class RegionLimits
{
    public static final Maximum MAXIMUM_WAYS = Maximum.maximum(Limit.WAYS);

    public static final Maximum MAXIMUM_NODES = Maximum.maximum(Limit.NODES);

    public static final Estimate ESTIMATED_WAYS = Estimate.estimate(Estimated.WAYS);

    public static final Estimate ESTIMATED_NODES = Estimate.estimate(Estimated.NODES);

    public static final Maximum CITIES = Maximum.parseMaximum(Listener.console(), "10,000");

    // Regions
    public static final Maximum CONTINENTS = Maximum._7;

    public static final Maximum DISTRICTS_PER_CITY = Maximum.parseMaximum(Listener.console(), "100");

    public static final Maximum COUNTIES = Maximum._10_000;

    public static final Maximum COUNTRIES = Maximum.maximum(300);

    public static final Maximum METROPOLITAN_AREAS = Maximum._10_000;

    // Border Polygons
    public static final Maximum POLYGONS_PER_CONTINENT = Maximum._100;

    public static final Maximum POLYGONS_PER_COUNTRY = Maximum._10_000;

    public static final Maximum POLYGONS_PER_COUNTY = Maximum._10_000;

    public static final Maximum POLYGONS_PER_METROPOLITAN_AREA = Maximum._10_000;

    public static final Maximum POLYGONS_PER_STATE = Maximum._10_000;

    public static final Maximum POLYGONS_PER_TIME_ZONE = Maximum._10_000;

    public static final Maximum STATES = Maximum._10_000;

    public static final Maximum TIME_ZONES = Maximum.parseMaximum(Listener.console(), "30,000");

    static class Estimated
    {
        public static final int WAYS = 65_536;

        public static final int NODES = 65_536;
    }

    static class Limit
    {
        public static final int WAYS = 1_000_000;

        public static final int NODES = 4_000_000;
    }
}
