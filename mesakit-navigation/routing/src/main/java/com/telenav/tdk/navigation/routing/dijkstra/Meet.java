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

package com.telenav.tdk.navigation.routing.dijkstra;

import com.telenav.tdk.graph.Route;
import com.telenav.tdk.navigation.routing.cost.Cost;

public class Meet
{
    private final Route route;

    private final Cost cost;

    public Meet(final Route route, final Cost cost)
    {
        this.route = route;
        this.cost = cost;
    }

    public Cost cost()
    {
        return this.cost;
    }

    public boolean equals(final Meet that)
    {
        return that != null && this.route.equals(that.route);
    }

    public boolean isCheaperThan(final Meet that)
    {
        return this.cost.isLessThan(that.cost);
    }

    public Route route()
    {
        return this.route;
    }
}
