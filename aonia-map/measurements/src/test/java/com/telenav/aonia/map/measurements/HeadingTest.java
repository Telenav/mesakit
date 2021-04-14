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

package com.telenav.aonia.map.measurements;

import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Direction;
import com.telenav.aonia.map.measurements.geographic.Heading;
import com.telenav.aonia.map.measurements.project.MapMeasurementsUnitTest;
import org.junit.Test;

public class HeadingTest extends MapMeasurementsUnitTest
{
    @Test
    public void testDirections()
    {
        // Verify that headings outside the standard range of 0 to 360 degrees are converted to
        // direction correctly
        final var headingNorth = Heading.degrees(0);
        final var headingEast = Heading.degrees(90);
        final var headingSouth = Heading.degrees(180);
        final var headingWest = Heading.degrees(270);
        final var headingNorth2 = Heading.degrees(360);
        final var headingEast2 = Heading.degrees(450);
        final var headingSouth2 = Heading.degrees(540);
        final var headingWest2 = Heading.degrees(630);
        final var headingNorth3 = Heading.degrees(-360);
        final var headingEast3 = Heading.degrees(-270);
        final var headingSouth3 = Heading.degrees(-180);
        final var headingWest3 = Heading.degrees(-90);

        ensureEqual(headingNorth.asApproximateDirection(), Direction.NORTH);
        ensureEqual(headingEast.asApproximateDirection(), Direction.EAST);
        ensureEqual(headingSouth.asApproximateDirection(), Direction.SOUTH);
        ensureEqual(headingWest.asApproximateDirection(), Direction.WEST);
        ensureEqual(headingNorth2.asApproximateDirection(), Direction.NORTH);
        ensureEqual(headingEast2.asApproximateDirection(), Direction.EAST);
        ensureEqual(headingSouth2.asApproximateDirection(), Direction.SOUTH);
        ensureEqual(headingWest2.asApproximateDirection(), Direction.WEST);
        ensureEqual(headingNorth3.asApproximateDirection(), Direction.NORTH);
        ensureEqual(headingEast3.asApproximateDirection(), Direction.EAST);
        ensureEqual(headingSouth3.asApproximateDirection(), Direction.SOUTH);
        ensureEqual(headingWest3.asApproximateDirection(), Direction.WEST);
    }

    @Test
    public void testRound()
    {
        ensureEqual(Heading.degrees(10), Heading.degrees(9).quantize(Angle.degrees(10)));
        ensureEqual(Heading.degrees(10), Heading.degrees(11).quantize(Angle.degrees(10)));
        ensureEqual(Heading.degrees(10), Heading.degrees(9.5).quantize(Angle.degrees(10)));
        ensureEqual(Heading.degrees(10), Heading.degrees(10.5).quantize(Angle.degrees(10)));
    }
}
