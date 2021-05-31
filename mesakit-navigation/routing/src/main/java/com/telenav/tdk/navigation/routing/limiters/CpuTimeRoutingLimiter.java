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

package com.telenav.kivakit.navigation.routing.limiters;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.navigation.routing.RoutingInstruction;
import com.telenav.kivakit.navigation.routing.RoutingLimiter;
import com.telenav.kivakit.navigation.routing.StopRoutingInstruction;
import com.telenav.kivakit.utilities.time.PreciseDuration;

public class CpuTimeRoutingLimiter implements RoutingLimiter
{
    private final PreciseDuration maximum;

    private int iteration;

    private final PreciseDuration start = PreciseDuration.cpuTime();

    public CpuTimeRoutingLimiter(final PreciseDuration maximum)
    {
        this.maximum = maximum;
    }

    @Override
    public RoutingInstruction instruction(final Edge edge)
    {
        if (++this.iteration % 100 == 0)
        {
            final var duration = PreciseDuration.cpuTime().subtract(this.start);
            if (duration.isGreaterThan(this.maximum))
            {
                return new StopRoutingInstruction("Exceeded $ CPU time", duration);
            }
        }
        return RoutingInstruction.EXPLORE_EDGE;
    }
}
