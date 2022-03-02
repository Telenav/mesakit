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

import com.telenav.kivakit.language.time.PreciseDuration;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.navigation.routing.RoutingInstruction;
import com.telenav.mesakit.navigation.routing.RoutingLimiter;
import com.telenav.mesakit.navigation.routing.StopRoutingInstruction;

public class CpuTimeRoutingLimiter implements RoutingLimiter
{
    private final PreciseDuration maximum;

    private int iteration;

    private final PreciseDuration start = PreciseDuration.cpuTime();

    public CpuTimeRoutingLimiter(PreciseDuration maximum)
    {
        this.maximum = maximum;
    }

    @Override
    public RoutingInstruction instruction(Edge edge)
    {
        if (++iteration % 100 == 0)
        {
            var duration = PreciseDuration.cpuTime().minus(start);
            if (duration.isGreaterThan(maximum))
            {
                return new StopRoutingInstruction("Exceeded $ CPU time", duration);
            }
        }
        return RoutingInstruction.EXPLORE_EDGE;
    }
}
