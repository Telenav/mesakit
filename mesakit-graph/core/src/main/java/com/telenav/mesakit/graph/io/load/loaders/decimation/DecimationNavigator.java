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

package com.telenav.mesakit.graph.io.load.loaders.decimation;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.map.measurements.geographic.Angle;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;

public class DecimationNavigator extends Navigator
{
    private final EdgeSet decimated;

    private final Angle maximumDeviation;

    private final Edge start;

    /**
     * @param start The starting edge
     * @param decimated The set of edges that have already been decimated
     * @param maximumDeviation The maximum deviation to allow
     */
    public DecimationNavigator(final Edge start, final EdgeSet decimated, final Angle maximumDeviation)
    {
        this.start = start;
        this.decimated = decimated;
        this.maximumDeviation = maximumDeviation;
    }

    @Override
    public Edge in(final Edge at)
    {
        return to(at.inEdgesWithoutReversed());
    }

    @Override
    public Edge out(final Edge at)
    {
        return to(at.outEdgesWithoutReversed());
    }

    private Edge to(final EdgeSet toSet)
    {
        if (toSet.size() == 1)
        {
            final var to = toSet.first();
            if (!this.decimated.contains(to) && this.start.turnAngleTo(to, Chirality.SMALLEST).isLessThan(this.maximumDeviation))
            {
                this.decimated.add(to);
                return to;
            }
        }
        return null;
    }
}
