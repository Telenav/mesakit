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

package com.telenav.mesakit.graph.navigation;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;

/**
 * Defines a general way to limit routes that are being created through some assembly process. An implementation might
 * limit a route by total length, distance from a start point, total number of edges or some other metric or property of
 * the route or the edge being added.
 *
 * @author jonathanl (shibo)
 */
public interface RouteLimiter
{
    /**
     * Returns true if the given route can be extended by the given edge
     */
    boolean canExtendRoute(Route route, Edge edge);
}
