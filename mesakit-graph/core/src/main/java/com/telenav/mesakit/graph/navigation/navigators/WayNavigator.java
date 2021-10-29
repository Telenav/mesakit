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

package com.telenav.mesakit.graph.navigation.navigators;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Navigates an PBF way starting at the given edge
 *
 * @author jonathanl (shibo)
 */
public class WayNavigator extends Navigator
{
    private final PbfWayIdentifier identifier;

    private final Set<Edge> visited = new HashSet<>();

    public WayNavigator(Edge edge)
    {
        identifier = edge.wayIdentifier();
        visited.add(edge);
    }

    @Override
    public Edge in(Edge edge)
    {
        var reversed = edge.reversed();
        for (var in : edge.inEdges())
        {
            if (!in.equals(reversed))
            {
                if (in.wayIdentifier().equals(identifier))
                {
                    if (!visited.contains(in) && !visited.contains(in.reversed()))
                    {
                        visited.add(in);
                        return in;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Edge out(Edge edge)
    {
        var reversed = edge.reversed();
        for (var out : edge.outEdges())
        {
            if (!out.equals(reversed))
            {
                if (out.wayIdentifier().equals(identifier))
                {
                    if (!visited.contains(out) && !visited.contains(out.reversed()))
                    {
                        visited.add(out);
                        return out;
                    }
                }
            }
        }
        return null;
    }
}
