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

import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.navigation.routing.cost.Cost;

public class Meet
{
    private final Route route;

    private final Cost cost;

    public Meet(Route route, Cost cost)
    {
        this.route = route;
        this.cost = cost;
    }

    public Cost cost()
    {
        return cost;
    }

    public boolean equals(Meet that)
    {
        return that != null && route.equals(that.route);
    }

    public boolean isCheaperThan(Meet that)
    {
        return cost.isLessThan(that.cost);
    }

    public Route route()
    {
        return route;
    }
}
