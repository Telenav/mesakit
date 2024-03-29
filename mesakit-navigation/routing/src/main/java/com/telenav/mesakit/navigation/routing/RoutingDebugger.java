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

import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.navigation.routing.cost.Cost;

/**
 * A debug strategy for routing
 *
 * @author jonathanl (shibo)
 */
public interface RoutingDebugger
{
    /**
     * The null {@link RoutingDebugger}
     */
    RoutingDebugger NULL = new RoutingDebugger()
    {
        @Override
        public void onEnd(RoutingRequest request, RoutingResponse response)
        {
        }

        @Override
        public void onRelaxed(Route route, Cost cost)
        {
        }

        @Override
        public void onSettled(Vertex vertex, Cost cost)
        {
        }

        @Override
        public void onStart(RoutingRequest request)
        {
        }
    };

    void onEnd(RoutingRequest request, RoutingResponse response);

    /**
     * @param route The relaxed route
     * @param cost The cost of the relaxed route
     */
    void onRelaxed(Route route, Cost cost);

    /**
     * @param vertex The new vertex
     * @param cost Initial cost of the new vertex
     */
    void onSettled(Vertex vertex, Cost cost);

    /**
     * Called by the router at start of routing
     */
    void onStart(RoutingRequest request);
}
