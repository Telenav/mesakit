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


package com.telenav.tdk.graph;

import com.telenav.tdk.map.measurements.Distance;

/**
 * Builds a route by adding edges to the ends before calling {@link #route()} to build the {@link Route}.
 * <p>
 * <b>Adding Edges</b>
 * <ul>
 *     <li>{@link #append(Edge)}</li>
 *     <li>{@link #append(Route)}</li>
 *     <li>{@link #prepend(Edge)}</li>
 *     <li>{@link #prepend(Route)}</li>
 *     <li>{@link #canAppend(Edge)}</li>
 *     <li>{@link #canAppend(Route)}</li>
 *     <li>{@link #safeAppend(Edge)}</li>
 *     <li>{@link #safeAppend(Route)}</li>
 *     <li>{@link #smartAdd(Edge)}</li>
 *     <li>{@link #safeSmartAdd(Edge)}</li>
 *     <li>{@link #safeSmartAdd(Route)}</li>
 * </ul>
 * <p>
 * <b>Building a Route</b>
 * <ul>
 *     <li>{@link #route()}</li>
 *     <li>{@link #isValid()}</li>
 *     <li>{@link #length()}</li>
 *     <li>{@link #size()}</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see Edge
 * @see Route
 * @see Distance
 */
public class RouteBuilder
{
    private Route route;

    /**
     * Adds an edge, which must be connected to the route that has been built so far
     */
    public void append(final Edge edge)
    {
        if (edge != null)
        {
            append(Route.fromEdge(edge));
        }
    }

    /**
     * Adds a route, which must be connected to the route that has been built so far
     */
    public void append(final Route that)
    {
        if (route == null)
        {
            route = that;
        }
        else
        {
            route = route.append(that);
        }
    }

    /**
     * @return True if the given edge can be appended to the route being built
     */
    public boolean canAppend(final Edge edge)
    {
        return route == null || route.canAppend(Route.fromEdge(edge));
    }

    /**
     * @return True if the given route can be appended to the route being built
     */
    public boolean canAppend(final Route route)
    {
        return this.route == null || this.route.canAppend(route);
    }

    /**
     * Adds the given edge to this route, appending, prepending or flipping the edge as need be to make it connect.
     */
    public boolean canSafeSmartAdd(final Edge edge)
    {
        if (route != null)
        {
            return route.leadsTo(edge) || edge.leadsTo(route)
                    || edge.isTwoWay() && route.leadsTo(edge.reversed())
                    || edge.isTwoWay() && edge.reversed().leadsTo(route);
        }
        return true;
    }

    /**
     * @return True if there is a valid route that can be constructed by calling {@link #route()}
     */
    public boolean isValid()
    {
        return size() > 0;
    }

    /**
     * @return The total length of the route built so far
     */
    public Distance length()
    {
        if (route == null)
        {
            return Distance.ZERO;
        }
        return route.length();
    }

    /**
     * Prepends an edge, which must be connected to the route that has been built so far
     */
    public void prepend(final Edge edge)
    {
        prepend(Route.fromEdge(edge));
    }

    /**
     * Prepends a route, which must be connected to the route that has been built so far
     */
    public void prepend(final Route that)
    {
        if (route == null)
        {
            route = that;
        }
        else
        {
            route = route.prepend(that);
        }
    }

    /**
     * @return The route that has been built
     */
    public Route route()
    {
        return route;
    }

    /**
     * Appends the given edge if the operation will succeed
     */
    public void safeAppend(final Edge edge)
    {
        if (canAppend(edge))
        {
            append(edge);
        }
    }

    /**
     * Appends the given route if the operation will succeed
     */
    public void safeAppend(final Route route)
    {
        if (canAppend(route))
        {
            append(route);
        }
    }

    /**
     * Adds the given edge to this route, appending, prepending or flipping the edge as need be to make it connect.
     */
    public boolean safeSmartAdd(final Edge edge)
    {
        if (route == null)
        {
            append(edge);
        }
        else
        {
            if (route.leadsTo(edge))
            {
                append(edge);
            }
            else if (edge.leadsTo(route))
            {
                prepend(edge);
            }
            else if (edge.isTwoWay() && route.leadsTo(edge.reversed()))
            {
                append(edge.reversed());
            }
            else if (edge.isTwoWay() && edge.reversed().leadsTo(route))
            {
                prepend(edge.reversed());
            }
            else
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds the given route to this route, appending, prepending or flipping it as need be to make it connect.
     */
    public boolean safeSmartAdd(final Route route)
    {
        for (final var edge : route)
        {
            if (!safeSmartAdd(edge))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The number of edges in this route so far
     */
    public int size()
    {
        if (route == null)
        {
            return 0;
        }
        return route.size();
    }

    /**
     * Adds the given edge to this route, appending, prepending or flipping the edge as need be to make it connect.
     */
    public void smartAdd(final Edge edge)
    {
        if (!safeSmartAdd(edge))
        {
            throw new IllegalArgumentException(
                    "Neither edge " + edge.identifier() + " nor its reverse can be appended or prepended to " + route);
        }
    }
}
