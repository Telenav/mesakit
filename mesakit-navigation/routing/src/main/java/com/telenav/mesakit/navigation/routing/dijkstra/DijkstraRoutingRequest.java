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

package com.telenav.mesakit.navigation.routing.dijkstra;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.navigation.routing.RoutingDebugger;
import com.telenav.mesakit.navigation.routing.RoutingLimiter;
import com.telenav.mesakit.navigation.routing.RoutingRequest;
import com.telenav.mesakit.navigation.routing.RoutingResponse;
import com.telenav.mesakit.navigation.routing.bidijkstra.BiDijkstraRouter;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

public class DijkstraRoutingRequest extends RoutingRequest
{
    /** Direction to route in */
    private Direction direction = Direction.FORWARD;

    /** The state for each vertex */
    private final Map<Vertex, VertexState> state = new HashMap<>();

    /**
     * The priority queue of vertexes to visit, ordered from least cost to most cost. Note that use of a {@link TreeSet}
     * here is purely for performance reasons. Using a tree set as a priority queue is more than twice as fast in
     * practice as using an actual {@link PriorityQueue}.
     */
    private final TreeSet<VertexState> queue = new TreeSet<>();

    /** The most recently settled vertex */
    private VertexState settled;

    public DijkstraRoutingRequest(Vertex start, Vertex end)
    {
        super(start, end);
    }

    protected DijkstraRoutingRequest(DijkstraRoutingRequest request, RoutingLimiter limiter,
                                     RoutingDebugger debugger)
    {
        super(request, limiter, debugger);
        direction = request.direction;
    }

    @Override
    public String description()
    {
        return "Dijkstra " + direction.name();
    }

    public Direction direction()
    {
        return direction;
    }

    public RoutingResponse done(Route route)
    {
        var response = new RoutingResponse(route, elapsed());
        super.onEndRouting(response);
        return response;
    }

    @Override
    public Vertex end()
    {
        return isForward() ? super.end() : super.start();
    }

    public RoutingResponse failed()
    {
        return done(null);
    }

    /**
     * @return Information about the meeting point if this routing request has reached a vertex already settled by that
     * routing request. This is used by the {@link BiDijkstraRouter} to determine when the forward and backward routing
     * requests have met.
     */
    public Meet meet(DijkstraRoutingRequest that)
    {
        // If this request has settled a vertex,
        if (settled != null)
        {
            // get the state of the corresponding vertex in the other request (to see if our
            // request meets any of the vertexes settled by that other request)
            var thatState = that.state.get(settled.vertex());

            // and if there is corresponding vertex state, then the two requests have met.
            if (thatState != null)
            {
                // Get the route to meeting point from the other request,
                var thatRoute = thatState.route(that.direction);
                if (thatRoute != null)
                {
                    // Get the route from this request
                    var thisRoute = route();
                    if (thisRoute != null)
                    {
                        // connect this route to that route
                        var route = thisRoute.connect(thatRoute);

                        // and add the costs together,
                        var cost = settled.cost().add(thatState.cost());

                        // finally returning the meet.
                        return new Meet(route, cost);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void onStartRouting()
    {
        super.onStartRouting();

        // Add starting vertex to priority queue with zero cost
        state(start()).zeroCost();
    }

    public Route route()
    {
        return settled().route(direction);
    }

    public VertexState settled()
    {
        return settled;
    }

    @Override
    public Vertex start()
    {
        return isForward() ? super.start() : super.end();
    }

    public DijkstraRoutingRequest withDebugger(RoutingDebugger debugger)
    {
        return new DijkstraRoutingRequest(this, null, debugger);
    }

    public DijkstraRoutingRequest withDirection(Direction direction)
    {
        var router = new DijkstraRoutingRequest(this, null, null);
        router.direction = direction;
        return router;
    }

    public DijkstraRoutingRequest withLimiter(RoutingLimiter limiter)
    {
        return new DijkstraRoutingRequest(this, limiter, null);
    }

    EdgeSet candidates(Vertex vertex)
    {
        return isForward() ? vertex.outEdges() : vertex.inEdges();
    }

    boolean isDone()
    {
        return queue.isEmpty();
    }

    Vertex nextVertex(Edge edge)
    {
        return isForward() ? edge.to() : edge.from();
    }

    VertexState settle()
    {
        var first = queue.pollFirst();
        assert first != null;
        queue.remove(first);
        settled = first;
        settled.settled();
        onSettled(settled.vertex(), settled.cost());
        return settled;
    }

    VertexState state(Vertex vertex)
    {
        return state.computeIfAbsent(vertex, v ->
        {
            var state = new VertexState(v);
            queue.add(state);
            return state;
        });
    }

    void update(VertexState vertex)
    {
        queue.remove(vertex);
        queue.add(vertex);
    }

    private boolean isForward()
    {
        return direction.isForward();
    }
}
