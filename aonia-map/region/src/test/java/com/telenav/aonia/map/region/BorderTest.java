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

package com.telenav.aonia.map.region;

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.geography.shape.polyline.Polygon;
import com.telenav.aonia.map.region.border.Border;
import com.telenav.aonia.map.region.project.MapRegionUnitTest;
import com.telenav.aonia.map.region.regions.Continent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BorderTest extends MapRegionUnitTest
{
    @Test
    public void testSerialization()
    {
        final var polygon = polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85);
        final var border = new Border<>(Continent.ASIA, polygon);
        border.identity(new RegionIdentity("x-y")
                .withIdentifier(new RegionIdentifier(1))
                .withIsoCode("X")
                .withAoniaCode("X"));
        serializationTest(border);
    }

    private Polygon polygon(final double... values)
    {
        final List<Location> locations = new ArrayList<>();
        for (var i = 0; i < values.length; i += 2)
        {
            locations.add(new Location(Latitude.degrees(values[i]), Longitude.degrees(values[i + 1])));
        }
        return Polygon.fromLocationSequence(locations);
    }
}
