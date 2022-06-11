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

package com.telenav.mesakit.map.region.test.unit;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.testing.RegionUnitTest;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.regions.Continent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BorderTest extends RegionUnitTest
{
    @Test
    public void testSerialization()
    {
        var polygon = polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85);
        var border = new Border<>(Continent.ASIA, polygon);
        border.identity(new RegionIdentity("x-y")
                .withIdentifier(new RegionIdentifier(1))
                .withIsoCode("X")
                .withMesaKitCode("X"));
        testSerialization(border);
    }

    private Polygon polygon(double... values)
    {
        List<Location> locations = new ArrayList<>();
        for (var i = 0; i < values.length; i += 2)
        {
            locations.add(new Location(Latitude.degrees(values[i]), Longitude.degrees(values[i + 1])));
        }
        return Polygon.fromLocationSequence(locations);
    }
}
