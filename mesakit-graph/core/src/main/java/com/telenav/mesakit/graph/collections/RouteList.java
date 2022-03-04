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

package com.telenav.mesakit.graph.collections;

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A list of routes, not necessarily connected in any way.
 *
 * @author jonathanl (shibo)
 */
public class RouteList implements Iterable<Route>
{
    private final List<Route> routes = new LinkedList<>();

    public RouteList()
    {
    }

    public RouteList(Iterable<Route> iterable)
    {
        Iterables.addAll(iterable, routes);
    }

    public void add(Route edge)
    {
        if (edge != null)
        {
            routes.add(edge);
        }
    }

    public EdgeSet asEdgeSet()
    {
        var edges = new EdgeSet();
        for (var route : this)
        {
            edges.add(route);
        }
        return edges;
    }

    /**
     * @return This list of routes as a single route or null if the routes cannot be connected into a single route
     */
    public Route asRoute()
    {
        return asRoutes().first();
    }

    /**
     * @return The smallest list of routes formed by connecting up all the routes in this list that are connected
     */
    public ObjectList<Route> asRoutes()
    {
        var result = new ObjectList<Route>();

        // Create a queue with the routes in this list
        var queue = new LinkedList<>(routes);

        // and if there is at least one route to add
        if (!isEmpty())
        {
            // create maps from vertexes to in and out routes
            Map<Vertex, Route> outRoute = new HashMap<>();
            Map<Vertex, Route> inRoute = new HashMap<>();
            for (var route : queue)
            {
                outRoute.put(route.start(), route);
                inRoute.put(route.end(), route);
            }

            // then, while there are routes that could be connected,
            while (queue.size() > 1)
            {
                // remove the head of the queue
                var head = queue.removeFirst();

                // and get its start and end vertex
                var start = head.start();
                var end = head.end();

                // and if there is an in-route to the start vertex
                var in = inRoute.get(start);
                if (in != null)
                {
                    // then connect it to the head
                    var connected = head.connect(in);
                    if (connected != null)
                    {
                        // remove the in route and add the new head back to the queue
                        queue.remove(in);
                        queue.addFirst(connected);
                        inRoute.remove(start);
                    }
                    continue;
                }

                // and if there is an out-route from the end vertex
                var out = outRoute.get(end);
                if (out != null)
                {
                    // then append it to the head
                    var connected = head.connect(out);
                    if (connected != null)
                    {
                        // remove the out route and add the new head back to the queue
                        queue.remove(out);
                        queue.addFirst(connected);
                        outRoute.remove(end);
                    }
                    continue;
                }

                // but, if there isn't an in-route or an out-route to the head it is an isolated route,
                // so we just add it to the result list
                result.add(head);
            }
        }

        // If there is one route left
        if (queue.size() == 1)
        {
            // add it to the result list
            result.add(queue.removeFirst());
        }

        return result;
    }

    public void clear()
    {
        routes.clear();
    }

    @NotNull
    @Override
    public Iterator<Route> iterator()
    {
        return routes.iterator();
    }

    private boolean isEmpty()
    {
        return routes.isEmpty();
    }
}
