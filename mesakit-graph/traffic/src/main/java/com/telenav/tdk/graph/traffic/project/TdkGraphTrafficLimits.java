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

package com.telenav.kivakit.graph.traffic.project;

import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.kernel.scalars.counts.Maximum;

public class KivaKitGraphTrafficLimits
{
    public static final Maximum MAXIMUM_TRAFFIC_EDGES = Maximum.parse("1,000,000,000");

    public static final Maximum MAXIMUM_TMC_CODES = Maximum.parse("2,500,000");

    public static final Maximum MAXIMUM_TMC_CODES_PER_EDGE = Maximum.parse("100");

    public static final Maximum MAXIMUM_TTL_CODES = Maximum.parse("2,500,000");

    public static final Maximum MAXIMUM_ROAD_SECTIONS = Maximum.parse("4,000,000");

    public static final Maximum MAXIMUM_UPSTREAM_ROAD_SECTIONS = Maximum.parse("100");

    public static final Maximum MAXIMUM_DOWNSTREAM_ROAD_SECTIONS = Maximum.parse("100");

    public static final Maximum MAXIMUM_MARKETS = Maximum.parse("5,000");

    public static final Maximum MAXIMUM_MARKETS_PER_DATA_SOURCE = Maximum.parse("500");

    public static final Maximum MAXIMUM_ROAD_SECTIONS_PER_ROUTE = Maximum.parse("10,000");

    public static final Estimate ESTIMATED_TRAFFIC_EDGES = Estimate.parse("1,000,000,000");
}
