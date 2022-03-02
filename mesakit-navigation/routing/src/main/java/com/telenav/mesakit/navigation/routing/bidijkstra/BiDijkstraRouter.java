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

import com.telenav.kivakit.language.count.MutableCount;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.mesakit.navigation.routing.BaseRouter;
import com.telenav.mesakit.navigation.routing.LevelPromoter;
import com.telenav.mesakit.navigation.routing.RoutingRequest;
import com.telenav.mesakit.navigation.routing.RoutingResponse;
import com.telenav.mesakit.navigation.routing.cost.CostFunction;
import com.telenav.mesakit.navigation.routing.cost.EdgePermissionFunction;
import com.telenav.mesakit.navigation.routing.cost.RoutePermissionFunction;
import com.telenav.mesakit.navigation.routing.dijkstra.DijkstraRouter;
import com.telenav.mesakit.navigation.routing.dijkstra.DijkstraRoutingRequest;
import com.telenav.mesakit.navigation.routing.dijkstra.Direction;
import com.telenav.mesakit.navigation.routing.dijkstra.Meet;

public class BiDijkstraRouter extends BaseRouter
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static final Debug DEBUG = new Debug(BiDijkstraRouter.LOGGER);

    /** The cost function for routing */
    private final CostFunction costFunction;

    /** Any route permission function */
    private RoutePermissionFunction routePermissionFunction = RoutePermissionFunction.NULL;

    /** Any edge permission function */
    private EdgePermissionFunction edgePermissionFunction = EdgePermissionFunction.NULL;

    /** Any level promoter */
    private LevelPromoter levelPromoter = LevelPromoter.NULL;

    public BiDijkstraRouter(CostFunction costFunction)
    {
        this.costFunction = costFunction;
    }

    private BiDijkstraRouter(BiDijkstraRouter that)
    {
        costFunction = that.costFunction;
        routePermissionFunction = that.routePermissionFunction;
        edgePermissionFunction = that.edgePermissionFunction;
        levelPromoter = that.levelPromoter;
    }

    @Override
    public RoutingResponse onFindRoute(RoutingRequest request)
    {
        // Get request as BiDijkstraRoutingRequest
        var birequest = (BiDijkstraRoutingRequest) request;

        // Construct forward and backwards routing requests
        var forward = new DijkstraRoutingRequest(request.start(), request.end())
                .withDirection(Direction.FORWARD)
                .withDebugger(request.debugger())
                .withLimiter(request.limiter());

        var backward = new DijkstraRoutingRequest(request.start(), request.end())
                .withDirection(Direction.BACKWARD)
                .withDebugger(request.debugger())
                .withLimiter(request.limiter());

        // Create router
        var forwardRouter = router().withHeuristicCostFunction(birequest.forwardHeuristicCostFunction());
        var backwardRouter = router().withHeuristicCostFunction(birequest.backwardHeuristicCostFunction());

        // Start routing
        forward.onStartRouting();
        backward.onStartRouting();

        // The best meet we've found
        Meet best = null;

        // The number of meets we've found
        var meets = new MutableCount();

        // Flip route direction back and forth forever
        for (var direction = Direction.FORWARD; ; direction = direction.reversed())
        {
            // execute the next step in either the forward or backward request
            RoutingResponse result;
            if (direction.isForward())
            {
                result = forwardRouter.execute(forward, 1);
            }
            else
            {
                result = backwardRouter.execute(backward, 1);
            }

            // If the forward request has just met the backward request
            var meet = direction.isForward() ? forward.meet(backward) : backward.meet(forward);
            if (meet != null)
            {
                // If we found the same route twice in a row
                var sameRoute = meet.equals(best);

                // If we have no best meet yet or this meet is cheaper than the best
                if (best == null || meet.isCheaperThan(best))
                {
                    // then we have a new best meet
                    best = meet;
                }

                // If (1) we've found the same route or (2) the number of meets so far is greater
                // than the maximum allowed or (3) the meet cost is greater than the first meet's
                // cost times the maximum first meet cost multiple
                meets.increment();
                if (sameRoute || meets.count().isGreaterThan(birequest.meets())
                        || meet.cost().isGreaterThan(best.cost().times(birequest.maximumFirstMeetCostMultiple())))
                {
                    // then we stop because we're not likely to get a better meet
                    return forward.done(best.route());
                }
            }

            // If we didn't meet because of this step forward or backward and we've never met at all
            // and our step failed
            if (meet == null && meets.isZero() && result.failed())
            {
                // then it's time to give up because there's no way for us to succeed
                return forward.failed();
            }
        }
    }

    public BiDijkstraRouter withEdgePermissionFunction(EdgePermissionFunction edgePermissionFunction)
    {
        var router = new BiDijkstraRouter(this);
        router.edgePermissionFunction = edgePermissionFunction;
        return router;
    }

    public BiDijkstraRouter withLevelPromoter(LevelPromoter levelPromoter)
    {
        var router = new BiDijkstraRouter(this);
        router.levelPromoter = levelPromoter;
        return router;
    }

    public BiDijkstraRouter withRoutePermissionFunction(RoutePermissionFunction routePermissionFunction)
    {
        var router = new BiDijkstraRouter(this);
        router.routePermissionFunction = routePermissionFunction;
        return router;
    }

    private DijkstraRouter router()
    {
        return new DijkstraRouter(costFunction)
                .withLevelPromoter(levelPromoter)
                .withRoutePermissionFunction(routePermissionFunction)
                .withEdgePermissionFunction(edgePermissionFunction);
    }
}
