////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.road.model;

import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Direction;
import org.junit.Test;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureEqual;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNull;

@SuppressWarnings("ConstantConditions")
public class RoadNameTest extends MapGeographyUnitTest
{
    @Test
    public void testExtractDirection()
    {
        ensureEqual(Direction.NORTH, RoadName.forName("I-5 N").extractDirection());
        ensureEqual(Direction.WEST, RoadName.forName("I-405 WB").extractDirection());
        ensureEqual(Direction.SOUTH, RoadName.forName("North Street South").extractDirection());
        ensureEqual(Direction.NORTH, RoadName.forName("I-5//N").extractDirection());
        ensureNull(RoadName.forName("I-405").extractDirection());
    }

    @Test
    public void testExtractName()
    {
        ensureEqual(RoadName.forName("I-5"), RoadName.forName("I-5 N").extractNameOnly());
        ensureEqual(RoadName.forName("I-5"), RoadName.forName("I-5//N").extractNameOnly());
        ensureEqual(RoadName.forName("I-405"), RoadName.forName("I-405 WB").extractNameOnly());
        ensureEqual(RoadName.forName("North Street"), RoadName.forName("North Street South").extractNameOnly());
        ensureEqual(RoadName.forName("I-405"), RoadName.forName("I-405").extractNameOnly());
        ensureEqual(RoadName.forName("I-405 Express Ln"), RoadName.forName("I-405 Express Ln").extractNameOnly());
        ensureEqual(RoadName.forName("5th Ave"), RoadName.forName("5th Ave NW").extractNameOnly());

        ensureEqual(RoadName.forName("Oak St"), RoadName.forName("N Oak St").extractNameOnly());
        ensureEqual(RoadName.forName("Oak St"), RoadName.forName("Oak St N").extractNameOnly());
    }
}
