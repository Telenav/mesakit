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

package com.telenav.mesakit.navigation.routing;

import com.telenav.kivakit.language.time.Duration;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.navigation.routing.bidijkstra.BiDijkstraRouter;
import com.telenav.mesakit.navigation.routing.dijkstra.DijkstraRouter;

/**
 * OperationLifecycleMessage of a routing engine's run.
 *
 * @author jonathanl (shibo)
 */
public class RoutingResponse
{
    /**
     * Value used by {@link DijkstraRouter} when it's being called by {@link BiDijkstraRouter} to execute a single
     * forward or backward step.
     */
    public static final RoutingResponse STEP_SUCCEEDED = new RoutingResponse(null, null);

    private final Route route;

    private final Duration elapsed;

    public RoutingResponse(Route route, Duration elapsed)
    {
        this.route = route;
        this.elapsed = elapsed;
    }

    public Duration elapsed()
    {
        return elapsed;
    }

    public boolean failed()
    {
        return this != RoutingResponse.STEP_SUCCEEDED && route == null;
    }

    public Route route()
    {
        return route;
    }

    @Override
    public String toString()
    {
        return "[RoutingResponse elapsed = " + elapsed() +
                ", route = " + route() + "]";
    }
}
