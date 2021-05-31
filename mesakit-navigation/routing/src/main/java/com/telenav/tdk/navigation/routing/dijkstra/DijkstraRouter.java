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

package com.telenav.kivakit.navigation.routing.dijkstra;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.navigation.routing.BaseRouter;
import com.telenav.kivakit.navigation.routing.LevelPromoter;
import com.telenav.kivakit.navigation.routing.RoutingRequest;
import com.telenav.kivakit.navigation.routing.RoutingResponse;
import com.telenav.kivakit.navigation.routing.cost.Cost;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;
import com.telenav.kivakit.navigation.routing.cost.EdgePermissionFunction;
import com.telenav.kivakit.navigation.routing.cost.EdgePermissionFunction.Permission;
import com.telenav.kivakit.navigation.routing.cost.RoutePermissionFunction;

/**
 * Implementation of Dijkstra routing.
 *
 * @author jonathanl (shibo)
 */
public class DijkstraRouter extends BaseRouter
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /** The cost function for edges in route */
    private final CostFunction costFunction;

    /** The heuristic cost function for */
    private CostFunction heuristicCostFunction;

    /** Any route permission function for determining turn restrictions, for example */
    private RoutePermissionFunction routePermissionFunction = RoutePermissionFunction.NULL;

    /** Any edge permission function for determining if an edge could be restricted */
    private EdgePermissionFunction edgePermissionFunction = EdgePermissionFunction.NULL;

    /** Any level promoter */
    private LevelPromoter levelPromoter = LevelPromoter.NULL;

    public DijkstraRouter(final CostFunction costFunction)
    {
        this.costFunction = costFunction;
    }

    private DijkstraRouter(final DijkstraRouter that)
    {
        costFunction = that.costFunction;
        routePermissionFunction = that.routePermissionFunction;
        edgePermissionFunction = that.edgePermissionFunction;
        levelPromoter = that.levelPromoter;
        heuristicCostFunction = that.heuristicCostFunction;
    }

    public RoutingResponse execute(final DijkstraRoutingRequest request, int iterations)
    {
        // If iterations is 1, we're doing bi-dijkstra
        final var bidirectionalDijkstra = iterations == 1;

        // While we have vertexes to process
        while (!request.isDone() && iterations-- > 0)
        {
            // get the next vertex and mark it as visited or "settled"
            final var at = request.settle();

            // update level promoter
            levelPromoter.onSettle(at.edge(request.direction()));

            // If we reached the goal
            if (request.isEnd(at.vertex()))
            {
                // we're done, so call the debugger and return the result
                return request.done(at.route(request.direction()));
            }

            // Go through each candidate edge leaving the vertex we're at
            for (final var candidate : request.candidates(at.vertex()))
            {
                // Get candidate cost
                final var candidateCost = costFunction.cost(candidate);

                // and if the cost is maximum (for example, if the edge is a toll road and we're
                // avoiding toll roads, the cost function will return Cost.MAXIMUM)
                if (candidateCost.isMaximum())
                {
                    // we can't go this way
                    continue;
                }

                // and if the level promoter says we should explore this candidate
                if (levelPromoter.shouldExplore(candidate))
                {
                    // then get the state for the next vertex we can reach via this edge
                    final var next = request.state(request.nextVertex(candidate));

                    // and if the to vertex is not already settled,
                    if (!next.isSettled())
                    {
                        // ask the limiter what we should do with the edge
                        final var instruction = request.limiter().instruction(candidate);
                        switch (instruction.meaning())
                        {
                            case STOP_ROUTING:
                                DijkstraRouter.LOGGER.warning("Routing halted by ${class}: $", request.limiter().getClass(), instruction.message());
                                return request.failed();

                            case EXPLORE_EDGE:

                                // Get the route so far. Note that this call side-effects the chain
                                // of vertexes that lead to the 'at' vertex so we cannot skip this
                                // call in the YES case below even though we would not actually test
                                // the route with the route permission function.
                                final var route = at.route(request.direction());

                                // If the candidate edge needs to be checked
                                if (edgePermissionFunction.allowed(candidate) == Permission.MAYBE)
                                {
                                    // check if adding the candidate forms an allowed route
                                    if (route != null && !routePermissionFunction
                                            .allowed(request.direction().concatenate(route, candidate)))
                                    {
                                        // and this candidate forms a bad route, skip it
                                        continue;
                                    }
                                }

                                // Relax the edge if it's a cheaper way to get to next
                                maybeRelax(request, at, next, candidate, candidateCost);
                                break;

                            case IGNORE_EDGE:
                                break;
                        }
                    }
                }
            }
        }

        // If we're being called by bi-dijkstra,
        if (bidirectionalDijkstra)
        {
            // If we failed or ran out of places to go return failure, otherwise return an
            // indication that the step succeeded
            return request.isDone() ? request.failed() : RoutingResponse.STEP_SUCCEEDED;
        }
        else
        {
            // otherwise, no route was found so return routing result failure
            return request.failed();
        }
    }

    @Override
    public RoutingResponse onFindRoute(final RoutingRequest request)
    {
        // Start routing
        request.onStartRouting();

        // execute the request by settling as many vertexes as necessary
        return execute((DijkstraRoutingRequest) request, Integer.MAX_VALUE);
    }

    public DijkstraRouter withEdgePermissionFunction(final EdgePermissionFunction edgePermissionFunction)
    {
        final var router = new DijkstraRouter(this);
        router.edgePermissionFunction = edgePermissionFunction;
        return router;
    }

    public DijkstraRouter withHeuristicCostFunction(final CostFunction heuristicCostFunction)
    {
        if (heuristicCostFunction != null)
        {
            final var router = new DijkstraRouter(this);
            router.heuristicCostFunction = heuristicCostFunction;
            return router;
        }
        return this;
    }

    public DijkstraRouter withLevelPromoter(final LevelPromoter levelPromoter)
    {
        final var router = new DijkstraRouter(this);
        router.levelPromoter = levelPromoter;
        return router;
    }

    public DijkstraRouter withRoutePermissionFunction(final RoutePermissionFunction routePermissionFunction)
    {
        final var router = new DijkstraRouter(this);
        router.routePermissionFunction = routePermissionFunction;
        return router;
    }

    private Cost heuristicCost(final Edge edge)
    {
        if (heuristicCostFunction != null)
        {
            return heuristicCostFunction.cost(edge);
        }
        return costFunction.cost(edge);
    }

    private void maybeRelax(final DijkstraRoutingRequest request, final VertexState at, final VertexState next,
                            final Edge candidate, final Cost candidateCost)
    {
        // Tell the level promoter we are relaxing a new candidate
        levelPromoter.onRelax(candidate);

        // Get the total cost of where we're at, plus the cost of candidate
        final var cost = at.cost().add(candidateCost);

        // If the cost is lower,
        if (cost.isLessThan(next.cost()))
        {
            // then relax the vertex
            next.relax(at, cost, at.cost().add(heuristicCost(candidate)));

            // and update it's queue position
            request.update(next);

            // If we're debugging
            if (request.isDebugging())
            {
                // call onRelaxed
                final var relaxed = next.route(request.direction());
                if (relaxed != null)
                {
                    request.onRelaxed(relaxed, next.cost());
                }
            }
        }
    }
}
