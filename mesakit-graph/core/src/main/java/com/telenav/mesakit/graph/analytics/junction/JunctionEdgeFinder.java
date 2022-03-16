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

package com.telenav.mesakit.graph.analytics.junction;

import com.telenav.kivakit.core.thread.KivaKitThread;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.core.value.count.MutableCount;
import com.telenav.kivakit.core.vm.JavaVirtualMachine;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle.degrees;

/**
 * Finds junction edges in the given edge sequence.
 *
 * @author tuom
 * @author jonathanl (shibo)
 */
public abstract class JunctionEdgeFinder extends BaseRepeater
{
    private static final Distance MAXIMUM_JUNCTION_EDGE_LENGTH = Distance.meters(150);

    /** Decoded of edges to look through */
    private final EdgeSequence edges;

    private Iterator<Edge> edgeIterator;

    private final MutableCount junctionEdges = new MutableCount();

    private final MutableCount connectors = new MutableCount();

    protected JunctionEdgeFinder(EdgeSequence edges)
    {
        this.edges = edges;
    }

    public void find()
    {
        var threads = JavaVirtualMachine.local().processors();
        var completion = new CountDownLatch(threads.asInt());
        edgeIterator = edges.iterator();
        var outer = this;
        for (var thread = 0; thread < threads.asInt(); thread++)
        {
            new KivaKitThread("JunctionFinder-" + thread)
            {
                @Override
                protected void onRun()
                {
                    try
                    {
                        while (true)
                        {
                            var next = nextEdges(10_000);
                            if (next.isEmpty())
                            {
                                break;
                            }
                            for (var edge : next)
                            {
                                try
                                {
                                    processEdge(edge);
                                }
                                catch (Exception e)
                                {
                                    outer.warning(e, "Edge processing threw exception");
                                }
                            }
                        }
                    }
                    finally
                    {
                        completion.countDown();
                    }
                }

                {
                    addListener(outer);
                }
            }.start();
        }
        try
        {
            completion.await();
        }
        catch (InterruptedException ignored)
        {
        }
    }

    /**
     * Called for each connector edge found
     *
     * @param edge The connector edge
     */
    protected abstract void onConnector(Edge edge);

    /**
     * Called for each junction edge found
     *
     * @param edge The junction edge
     */
    protected abstract void onJunction(Edge edge);

    /**
     * @return true if any end of the route is connecting to more than two edges besides the reversed edge, or all the
     * connected roads have more than one road name
     */
    private boolean atIntersection(Route route)
    {
        var inEdges = route.first().fromEdgesWithoutThisEdge();
        var outEdges = route.last().toEdgesWithoutThisEdge();

        // check connected roads number, if any end of the route is connecting to more than two
        // edges, then the route is at intersection
        if (inEdges.size() > 2 || outEdges.size() > 2)
        {
            return true;
        }

        // check road name
        Set<RoadName> roadNames = new HashSet<>();
        for (var edge : inEdges)
        {
            roadNames.add(edge.roadName());
        }
        for (var edge : outEdges)
        {
            roadNames.add(edge.roadName());
        }
        // If all the connected roads have more than one road name, then the route is at
        // intersection
        return roadNames.size() > 1;
    }

    /**
     * @return true if both ends of the route only connect to two parallel edges
     */
    private boolean isConnectingRoute(Route route)
    {
        var inEdges = route.first().fromEdgesWithoutThisEdge();
        var outEdges = route.last().toEdgesWithoutThisEdge();

        if (inEdges.size() == 2 && outEdges.size() == 2)
        {
            return isParallel(inEdges, route.first().from()) && isParallel(outEdges, route.last().to());
        }
        return false;
    }

    private boolean isParallel(EdgeSet twoEdges, Vertex shared)
    {
        var iterator = twoEdges.iterator();
        var firstEdge = iterator.next();
        var secondEdge = iterator.next();

        var first = firstEdge.from().equals(shared) ? firstEdge.heading()
                : firstEdge.roadShape().reversed().initialHeading();
        var second = secondEdge.from().equals(shared) ? secondEdge.heading()
                : secondEdge.roadShape().reversed().initialHeading();

        var difference = first.difference(second, Chirality.SMALLEST);
        return difference.isGreaterThan(degrees(135));
    }

    private boolean isTooComplex(Route route)
    {
        var fromEdges = new EdgeSet();
        for (var edge : route.first().fromEdgesWithoutThisEdge())
        {
            if (!fromEdges.contains(edge.reversed()))
            {
                fromEdges.add(edge);
                if (fromEdges.size() > 4)
                {
                    return true;
                }
            }
        }
        var toEdges = new EdgeSet();
        for (var edge : route.last().toEdgesWithoutThisEdge())
        {
            if (!toEdges.contains(edge.reversed()))
            {
                toEdges.add(edge);
                if (toEdges.size() > 4)
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Some filters for junction edge, including road type, road sub type and if the edge is bent, if the edge is ramp,
     * then the edge can be bent
     */
    private boolean isValidCandidate(Edge edge)
    {
        return edge.roadType().isEqualOrMoreImportantThan(RoadType.LOW_SPEED_ROAD) && !edge.isRoundabout()
                && !edge.isConnector() && edge.length().isLessThan(MAXIMUM_JUNCTION_EDGE_LENGTH);
    }

    /**
     * @return true if the route has appropriate length
     */
    private boolean isValidConnectionOrBentJunction(Route route)
    {
        return route.length().isLessThanOrEqualTo(Distance.meters(150));
    }

    /**
     * @return true if the route has appropriate length
     */
    private boolean isValidStraightJunction(Route route)
    {
        // If the route has only one edge,
        if (route.size() == 1)
        {
            // the length should be <= 60 meters
            return route.length().isLessThanOrEqualTo(Distance.meters(60));
        }
        else
        {
            // otherwise, the route length should be <= 90 meters
            return route.length().isLessThanOrEqualTo(Distance.meters(90));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private synchronized List<Edge> nextEdges(int count)
    {
        List<Edge> edges = new ArrayList<>();
        while (edgeIterator.hasNext() && count-- > 0)
        {
            edges.add(edgeIterator.next());
        }
        return edges;
    }

    private synchronized void notifyConnectorRoute(Route route)
    {
        for (var connector : route)
        {
            connectors.increment();
            onConnector(connector);
        }
    }

    private synchronized void notifyJunctionRoute(Route route)
    {
        for (var edge : route)
        {
            junctionEdges.increment();
            onJunction(edge);
        }
    }

    private void processEdge(Edge edge)
    {
        // check road type, road sub type
        if (!isValidCandidate(edge))
        {
            return;
        }

        // the junction edge should have less than or equal to five edges
        var route = edge.nonBranchingRoute(Maximum._5);
        if (route == null)
        {
            return;
        }

        // junction route should not contain repeated edges
        if (route.containsRepeatedEdge() || isTooComplex(route))
        {
            return;
        }

        // if the route is connecting to two close junctions, then all the edges in this route
        // are junction edges
        if (route.connectsTwoCloseJunctions())
        {
            notifyJunctionRoute(route);
        }

        // if the route is connecting to double digitized roads,
        else if (route.connectsTwoDoubleDigitizedRoads())
        {
            // if the route is straight,
            if (route.isStraight())
            {
                // and is a valid straight junction route
                if (isValidStraightJunction(route))
                {
                    notifyJunctionRoute(route);
                }
            }
            // Or if the route is bent
            else
            {
                // and the route is a valid connection or bent junction
                if (isValidConnectionOrBentJunction(route))
                {
                    // and the route is at an intersection, the route is junction edges
                    if (atIntersection(route))
                    {
                        notifyJunctionRoute(route);
                    }
                    // or the route is not at an intersection, and connecting to two parallel
                    // edges, the route is connection edges
                    else if (isConnectingRoute(route))
                    {
                        notifyConnectorRoute(route);
                    }
                }
            }
        }
    }
}
