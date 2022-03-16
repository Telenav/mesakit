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

import com.telenav.mesakit.map.geography.GeographyUnitTest;
import org.junit.Test;

public class IntersectionTest extends GeographyUnitTest
{
    @Test
    public void testCompare()
    {
        // Test identical street names, transposed names, 1 of 2 matching names, and
        // 2 different names
        var intersection1a = Intersection.forStreets(RoadName.forName("1st St"), RoadName.forName("1st Av"));
        var intersection1b = Intersection.forStreets(RoadName.forName("1st St"), RoadName.forName("1st Av"));
        var intersection2 = Intersection.forStreets(RoadName.forName("1st Av"), RoadName.forName("1st St"));
        var intersection3 = Intersection.forStreets(RoadName.forName("1st St"), RoadName.forName("3rd Av"));
        var intersection4 = Intersection.forStreets(RoadName.forName("2nd St"), RoadName.forName("1st Av"));
        var intersection5 = Intersection.forStreets(RoadName.forName("4th St"), RoadName.forName("4th Av"));

        ensure(intersection1a.equals(intersection1a));
        ensure(intersection1a.equals(intersection1b));
        ensureFalse(intersection1a.equals(intersection2));
        ensureFalse(intersection1a.equals(intersection3));
        ensureFalse(intersection1a.equals(intersection4));
        ensureFalse(intersection1a.equals(intersection5));
        ensureFalse(intersection1a.equals(null));

        // Only test hashCode equivalence for identical values; Intersections with
        // different values may offer up identical hashcodes.
        ensure(intersection1a.hashCode() == intersection1a.hashCode());
        ensure(intersection1a.hashCode() == intersection1b.hashCode());
    }
}
