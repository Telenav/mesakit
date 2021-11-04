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

package com.telenav.mesakit.navigation.routing.bidijkstra;

import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.navigation.routing.RoutingDebugger;
import com.telenav.mesakit.navigation.routing.RoutingLimiter;
import com.telenav.mesakit.navigation.routing.RoutingRequest;
import com.telenav.mesakit.navigation.routing.cost.CostFunction;

public class BiDijkstraRoutingRequest extends RoutingRequest
{
    /** The number of meets that should be found */
    private Count meets;

    /** Maximum multiple of the first meet for future meets before search stops */
    private double maximumFirstMeetCostMultiple = 1.5;

    /** The forward heuristic cost function */
    private CostFunction forwardHeuristicCostFunction;

    /** The backward heuristic cost function */
    private CostFunction backwardHeuristicCostFunction;

    public BiDijkstraRoutingRequest(Vertex start, Vertex end)
    {
        super(start, end);
    }

    protected BiDijkstraRoutingRequest(BiDijkstraRoutingRequest that, RoutingLimiter limiter,
                                       RoutingDebugger debugger)
    {
        super(that, limiter, debugger);
        meets = that.meets;
        forwardHeuristicCostFunction = that.forwardHeuristicCostFunction;
        backwardHeuristicCostFunction = that.backwardHeuristicCostFunction;
    }

    public CostFunction backwardHeuristicCostFunction()
    {
        return backwardHeuristicCostFunction;
    }

    @Override
    public String description()
    {
        return "BiDijkstra";
    }

    public CostFunction forwardHeuristicCostFunction()
    {
        return forwardHeuristicCostFunction;
    }

    public double maximumFirstMeetCostMultiple()
    {
        return maximumFirstMeetCostMultiple;
    }

    public Count meets()
    {
        if (meets == null)
        {
            var distance = distance();
            if (distance.isLessThan(Distance.miles(50)))
            {
                meets = Count.count(200);
            }
            else if (distance.isLessThan(Distance.miles(300)))
            {
                meets = Count.count(500);
            }
            else
            {
                meets = Count.count(1_000);
            }
        }
        return meets;
    }

    public BiDijkstraRoutingRequest withBackwardHeuristicCostFunction(CostFunction backwardHeuristicCostFunction)
    {
        if (backwardHeuristicCostFunction != null)
        {
            var request = new BiDijkstraRoutingRequest(this, null, null);
            request.backwardHeuristicCostFunction = backwardHeuristicCostFunction;
            return request;
        }
        return this;
    }

    public BiDijkstraRoutingRequest withDebugger(RoutingDebugger debugger)
    {
        return new BiDijkstraRoutingRequest(this, null, debugger);
    }

    public BiDijkstraRoutingRequest withForwardHeuristicCostFunction(CostFunction forwardHeuristicCostFunction)
    {
        if (forwardHeuristicCostFunction != null)
        {
            var router = new BiDijkstraRoutingRequest(this, null, null);
            router.forwardHeuristicCostFunction = forwardHeuristicCostFunction;
            return router;
        }
        return this;
    }

    public BiDijkstraRoutingRequest withLimiter(RoutingLimiter limiter)
    {
        return new BiDijkstraRoutingRequest(this, limiter, null);
    }

    public BiDijkstraRoutingRequest withMaximumFirstMeetCostMultiple(double maximumFirstMeetCostMultiple)
    {
        var router = new BiDijkstraRoutingRequest(this, null, null);
        router.maximumFirstMeetCostMultiple = this.maximumFirstMeetCostMultiple;
        return router;
    }

    public BiDijkstraRoutingRequest withMeets(Count meets)
    {
        var router = new BiDijkstraRoutingRequest(this, null, null);
        router.meets = meets;
        return router;
    }
}
