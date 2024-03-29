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

package com.telenav.mesakit.navigation.routing.limiters;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.navigation.routing.RoutingInstruction;
import com.telenav.mesakit.navigation.routing.RoutingLimiter;
import com.telenav.mesakit.navigation.routing.RoutingRequest;

public class DistanceRoutingLimiter implements RoutingLimiter
{
    private final Distance maximum;

    private Location start;

    public DistanceRoutingLimiter(Distance maximum)
    {
        this.maximum = maximum;
    }

    @Override
    public RoutingInstruction instruction(Edge edge)
    {
        // Get the distance that this edge puts us from the starting point
        var distance = edge.toLocation().distanceTo(start);

        // If this edge is too far away, ignore it
        return distance.isGreaterThan(maximum) ? RoutingInstruction.IGNORE_EDGE : RoutingInstruction.EXPLORE_EDGE;
    }

    @Override
    public void start(RoutingRequest request)
    {
        start = request.start().location();
    }
}
