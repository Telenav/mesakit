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
import com.telenav.mesakit.graph.world.WorldVertex;

import static com.telenav.kivakit.ensure.Ensure.fail;

/**
 * Router base class that handles trivial cases
 *
 * @author jonathanl (shibo)
 */
public abstract class BaseRouter implements Router
{
    @Override
    public final RoutingResponse findRoute(RoutingRequest request)
    {
        // Handle trivial case if the edges are the same, or if they are directly connected
        var result = trivialRoute(request);
        if (result != null)
        {
            return result;
        }

        // Non-trivial case
        return onFindRoute(request);
    }

    /**
     * @return The route from start to end
     */
    public abstract RoutingResponse onFindRoute(RoutingRequest request);

    /**
     * @return A routing result if trivial routing is possible, Null otherwise.
     */
    private RoutingResponse trivialRoute(RoutingRequest request)
    {
        // Get start and end
        var start = request.start();
        var end = request.end();

        // Make sure two edges are in the same Graph (unless they are both in a world graph)
        if (!(start instanceof WorldVertex && end instanceof WorldVertex))
        {
            if (!start.graph().equals(end.graph()))
            {
                return fail("Cannot route between vertexes in two different graphs");
            }
        }

        // If we have the same start and end vertex
        if (start.equals(end))
        {
            // then no route is needed
            return new RoutingResponse(null, Duration.NONE);
        }

        // If the start vertex is connected to the end vertex
        if (start.isConnectedTo(end))
        {
            // and there exists an edge from the start to the end edge
            if (start.edgeTo(end) != null)
            {
                // then we have a trivial route from the start vertex to the end vertex
                return new RoutingResponse(Route.fromEdge(start.edgeTo(end)), Duration.NONE);
            }
            else
            {
                // the vertexes are connected, but the direction must be wrong
                return new RoutingResponse(null, Duration.NONE);
            }
        }

        // No trivial route
        return null;
    }
}
