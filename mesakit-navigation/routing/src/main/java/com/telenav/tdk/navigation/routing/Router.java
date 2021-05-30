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

package com.telenav.tdk.navigation.routing;

import com.telenav.tdk.graph.Vertex;

/**
 * Base interface for something that routes between two vertices in a graph
 *
 * @author jonathanl (shibo)
 */
public interface Router
{
    /**
     * Attempts to satisfy a routing request between two {@link Vertex}es.
     *
     * @param request The routing request
     * @return A non-null {@link RoutingResponse}, including metrics concerning the effort expended during the search
     * process and a route (which is null if no route was found).
     */
    RoutingResponse findRoute(RoutingRequest request);
}
