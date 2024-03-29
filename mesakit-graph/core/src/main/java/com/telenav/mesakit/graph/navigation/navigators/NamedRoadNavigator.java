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
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;

/**
 * Navigates a section of road with the same name, no branches, no u-turns and no loops.
 *
 * @author jonathanl (shibo)
 */
public class NamedRoadNavigator extends Navigator
{
    private final RoadName name;

    private final EdgeSet visited = new EdgeSet();

    public NamedRoadNavigator(RoadName name)
    {
        this.name = name;
    }

    @Override
    public Edge in(Edge edge)
    {
        var from = edge.from();
        var reversed = edge.reversed();
        for (var in : from.inEdges())
        {
            if (accept(in) && !in.equals(reversed))
            {
                return in;
            }
        }
        return null;
    }

    @Override
    public Edge next(Edge edge, Direction direction)
    {
        var next = super.next(edge, direction);
        if (next != null && !visited.contains(next.forward()))
        {
            visited.add(next.forward());
            return next;
        }
        return null;
    }

    @Override
    public Edge out(Edge edge)
    {
        var to = edge.to();
        var reversed = edge.reversed();
        for (var out : to.outEdges())
        {
            if (accept(out) && !out.equals(reversed))
            {
                return out;
            }
        }
        return null;
    }

    protected boolean accept(Edge edge)
    {
        if (!edge.isRamp())
        {
            if (name().equals(edge.roadName()))
            {
                return true;
            }
            return edge.roadName() == null && edge.roadFunctionalClass().isMoreImportantThanOrEqual(RoadFunctionalClass.MAIN);
        }
        return false;
    }

    private RoadName name()
    {
        return name;
    }
}
