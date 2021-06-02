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
import com.telenav.mesakit.navigation.routing.cost.Cost;

public class VertexState implements Comparable<VertexState>
{
    /** The vertex */
    private final Vertex vertex;

    /** The previous vertex in the path to this vertex */
    private VertexState previous;

    /** The cost of getting to this vertex */
    private Cost cost = Cost.MAXIMUM;

    /** The heuristic cost of getting to this vertex */
    private Cost heuristicCost = Cost.MAXIMUM;

    /** True if this vertex is settled */
    private boolean settled;

    /** The route to this vertex */
    private Route route;

    VertexState(final Vertex vertex)
    {
        this.vertex = vertex;
    }

    @Override
    @SuppressWarnings("UseCompareMethod")
    public int compareTo(final VertexState that)
    {
        final var thisCost = heuristicCost.asDouble();
        final var thatCost = that.heuristicCost.asDouble();
        if (thisCost < thatCost)
        {
            return -1;
        }
        if (thisCost > thatCost)
        {
            return 1;
        }
        return 0;
    }

    public Cost cost()
    {
        return cost;
    }

    /**
     * @return Any edge from the previous vertex to this vertex (if we're going forward) or from this vertex to the
     * previous vertex (if we're going backward) or null if there's no such edge (either because there's no previous, or
     * because there's no edge, as in the case of a one-way street).
     */
    public Edge edge(final Direction direction)
    {
        if (previous != null)
        {
            // If routing direction is forward
            if (direction.isForward())
            {
                // return any edge from the previous vertex to this vertex
                return previous.vertex().edgeTo(vertex());
            }
            else
            {
                // otherwise, return any edge from this vertex to the previous vertex
                return vertex().edgeTo(previous.vertex());
            }
        }

        return null;
    }

    public boolean isSettled()
    {
        return settled;
    }

    public void relax(final VertexState previous, final Cost cost, final Cost heuristicCost)
    {
        this.previous = previous;
        this.cost = cost;
        this.heuristicCost = heuristicCost;
    }

    public Route route(final Direction direction)
    {
        // If we don't have the route to this vertex yet
        if (route == null)
        {
            // there is no route to the first vertex
            if (previous == null)
            {
                return null;
            }

            // If there is an edge in the given direction from this vertex
            final var edge = edge(direction);
            if (edge != null)
            {
                // If there's no previous route,
                if (previous.route == null)
                {
                    // we're at the second vertex forming the first edge
                    route = Route.fromEdge(edge);
                }
                else
                {
                    if (direction.isForward())
                    {
                        // this route is the previous route plus the new edge
                        route = previous.route.append(edge);
                    }
                    else
                    {
                        // this route is the previous route with the new edge prepended
                        route = previous.route.prepend(edge);
                    }
                }
            }
        }

        return route;
    }

    public void settled()
    {
        settled = true;
    }

    @Override
    public String toString()
    {
        return "[VertexState vertex = " + vertex() + ", cost = " + cost() + "]";
    }

    public Vertex vertex()
    {
        return vertex;
    }

    @SuppressWarnings("UnusedReturnValue")
    public VertexState zeroCost()
    {
        cost = Cost.ZERO;
        return this;
    }
}
