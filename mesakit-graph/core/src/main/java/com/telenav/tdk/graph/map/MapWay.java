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

package com.telenav.tdk.graph.map;

import com.telenav.tdk.data.formats.library.map.identifiers.WayIdentifier;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.Graph;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.navigation.navigators.WayNavigator;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;

/**
 * This class represents a way in a map.
 *
 * @author jonathanl (shibo)
 */
public class MapWay extends Way
{
    public static MapWay forEdge(final Edge edge)
    {
        return forRoute(edge.forward().route(new WayNavigator(edge), Distance.miles(1000)));
    }

    public static MapWay forRoute(final Route route)
    {
        return new MapWay(route);
    }

    public static MapWay forWayIdentifier(final Graph graph, final Rectangle bounds,
                                          final WayIdentifier identifier)
    {
        for (final var edge : graph.edgesIntersecting(bounds))
        {
            if (edge.wayIdentifier().equals(identifier))
            {
                return forEdge(edge);
            }
        }
        return null;
    }

    private MapWay(final Route route)
    {
        super(route);
    }

    @Override
    public WayIdentifier wayIdentifier()
    {
        return asRoute().first().wayIdentifier();
    }
}
