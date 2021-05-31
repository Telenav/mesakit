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

package com.telenav.tdk.navigation.routing.cost.functions.heuristic;

import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.segment.Segment;
import com.telenav.mesakit.map.measurements.Heading;
import com.telenav.kivakit.navigation.routing.cost.Cost;
import com.telenav.kivakit.navigation.routing.cost.CostFunction;
import com.telenav.mesakit.graph.Edge;

import static com.telenav.kivakit.map.measurements.Angle.Chirality.SMALLEST;

/**
 * A routing cost function which is based on travel time
 *
 * @author jonathanl (shibo)
 */
public class HeadingDeviationCostFunction implements CostFunction
{
    private final Heading heading;

    public HeadingDeviationCostFunction(final Located from, final Located to)
    {
        heading = new Segment(from.location(), to.location()).heading();
    }

    @Override
    public Cost cost(final Edge candidate)
    {
        final var deviation = candidate.heading().difference(heading, SMALLEST);
        return Cost.of(deviation.asDegrees() / 180.0);
    }
}
