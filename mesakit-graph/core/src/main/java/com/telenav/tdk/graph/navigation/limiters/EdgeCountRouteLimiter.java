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

package com.telenav.tdk.graph.navigation.limiters;

import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.navigation.RouteLimiter;

/**
 * Limiter to ensure a route doesn't exceed a maximum number of edges
 *
 * @author jonathanl (shibo)
 */
public class EdgeCountRouteLimiter implements RouteLimiter
{
    private final int maximumEdges;

    public EdgeCountRouteLimiter(final Count maximumEdges)
    {
        this.maximumEdges = maximumEdges.asInt();
    }

    @Override
    public boolean canExtendRoute(final Route route, final Edge edge)
    {
        return route.size() + 1 < this.maximumEdges;
    }
}
