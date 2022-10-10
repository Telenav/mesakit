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

import com.telenav.mesakit.graph.Edge;

/**
 * Limits a routing effort by determining if a vertex should be explored
 *
 * @author jonathanl (shibo)
 */
public interface RoutingLimiter
{
    RoutingLimiter UNLIMITED = edge -> RoutingInstruction.EXPLORE_EDGE;

    /**
     * Returns the routing instruction to follow for the given edge
     */
    RoutingInstruction instruction(Edge edge);

    default void start(RoutingRequest request)
    {
    }
}
