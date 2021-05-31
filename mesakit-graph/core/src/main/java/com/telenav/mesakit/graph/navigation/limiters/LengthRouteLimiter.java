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

package com.telenav.mesakit.graph.navigation.limiters;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.navigation.RouteLimiter;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Limiter to ensure a route doesn't exceed a maximum total length
 *
 * @author jonathanl (shibo)
 */
public class LengthRouteLimiter implements RouteLimiter
{
    public enum Type
    {
        /**
         * Only allow route extensions that are strictly less than the maximum distance
         */
        STRICT,

        /**
         * Allow the final route extension edge to cross the maximum distance threshold
         */
        LENIENT
    }

    private final Type type;

    private final Distance maximum;

    public LengthRouteLimiter(final Distance maximum, final Type type)
    {
        this.maximum = maximum;
        this.type = type;
    }

    @Override
    public boolean canExtendRoute(final Route route, final Edge edge)
    {
        switch (this.type)
        {
            case STRICT:
                return route.length().add(edge.length()).isLessThan(this.maximum);

            case LENIENT:
                return route.length().isLessThan(this.maximum);
        }
        return false;
    }
}
