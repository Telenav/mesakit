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

package com.telenav.mesakit.graph.traffic;

import com.telenav.kivakit.test.annotations.SlowTests;
import com.telenav.mesakit.graph.traffic.project.GraphTrafficUnitTest;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSection;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.motion.Speed;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ SlowTests.class })
public class RoadSectionTest extends GraphTrafficUnitTest
{
    public RoadSectionTest()
    {
        loadBayAreaRoadSectionDatabase();
    }

    @Test
    public void testFreeFlowSpeed()
    {
        final RoadSection roadSection = randomValueFactory().newRoadSection();

        // Free flow speeds are stored in RoadSection as hundredths of miles per hour; verify the
        // resulting values. Note that Speed.toString() floors to 0.1 miles per hour.
        roadSection.freeFlowSpeed(Speed.milesPerHour(60));
        ensureEqual(6000, roadSection.freeFlowSpeed().asHundredthsOfAMilePerHour());
        ensureEqual(60, (int) roadSection.freeFlowSpeed().asMilesPerHour());
        ensureEqual("60.0 mph", roadSection.freeFlowSpeed().toString());

        roadSection.freeFlowSpeed(Speed.milesPerHour(60.5));
        ensureEqual(6050, roadSection.freeFlowSpeed().asHundredthsOfAMilePerHour());
        ensureEqual(60, (int) roadSection.freeFlowSpeed().asMilesPerHour());
        ensureEqual("60.5 mph", roadSection.freeFlowSpeed().toString());

        roadSection.freeFlowSpeed(Speed.milesPerHour(60.54));
        ensureEqual(6054, roadSection.freeFlowSpeed().asHundredthsOfAMilePerHour());
        ensureEqual(60, (int) roadSection.freeFlowSpeed().asMilesPerHour());
        ensureEqual("60.5 mph", roadSection.freeFlowSpeed().toString());

        roadSection.freeFlowSpeed(Speed.milesPerHour(60.56));
        ensureEqual(6056, roadSection.freeFlowSpeed().asHundredthsOfAMilePerHour());
        ensureEqual(60, (int) roadSection.freeFlowSpeed().asMilesPerHour());
        ensureEqual("60.6 mph", roadSection.freeFlowSpeed().toString());
    }

    @Test
    public void testIntersects()
    {
        final RoadSection roadSection = randomValueFactory().newRoadSection();

        // Test the case where the road section is clearly in the rectangle.
        ensure(roadSection.intersects(roadSection.bounds().expanded(Distance.miles(1))));

        // Test the case where the road section is exactly within the rectangle.
        ensure(roadSection.intersects(roadSection.bounds()));

        // Create a rectangle the contains the start, but not the end.
        var rectangle = Rectangle.fromLocations(roadSection.start(), roadSection.start())
                .expanded(roadSection.start().distanceTo(roadSection.end()).times(.5));
        ensure(roadSection.intersects(rectangle));

        // Create a rectangle the contains the end, but not the start.
        rectangle = Rectangle.fromLocations(roadSection.end(), roadSection.end())
                .expanded(roadSection.start().distanceTo(roadSection.end()).times(.5));
        ensure(roadSection.intersects(rectangle));
    }
}
