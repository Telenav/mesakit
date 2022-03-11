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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.GeographyUnitTest;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PolygonTest extends GeographyUnitTest
{
    @Test
    public void testContains()
    {
        var polygon = polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85);
        ensure(polygon.contains(Location.degrees(-0.4, -0.4)));
        ensure(polygon.contains(Location.degrees(0, 0)));
    }

    @Test
    public void testContainsRandom()
    {
        var polygon = polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85);
        NEXT:
        for (var i = 0; i < 100000; i++)
        {
            var location = randomValueFactory().newLocation(polygon.bounds());
            for (Segment segment : polygon.segments())
            {
                if (segment.isHorizontal() && segment.start().latitude().equals(location.latitude()))
                {
                    continue NEXT;
                }
            }
            var graph = polygon.contains(location);
            var awt = polygon.asAwtPolygonInMicroDegrees().contains(location.asAwtPointInMicroDegrees());
            var equal = graph == awt;
            if (!equal)
            {
                // Ensure distance of location from polygon edge
                var snapper = new PolylineSnapper();
                var snap = snapper.snap(polygon, location);

                // and if the distance is not too close (which might be a precision error)
                if (snap.distanceToSource().isGreaterThan(Distance.TEN_METERS))
                {
                    // then we truly failed
                    fail("Location " + location + " failed! ((awt = " + awt + ") != (graph = " + graph + "))");
                }
            }
        }
    }

    @Test
    public void testIntersection()
    {
        var line = Polyline.fromLocations(Location.ORIGIN,
                new Location(Latitude.degrees(0.5), Longitude.degrees(0.5)));

        // Fully inside
        ensure(polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85).intersectsOrContains(line));

        // Crossing
        ensure(polygon(-0.5, -0.5, 1, -0.5, 1, 0.25, -0.5, 0.25).intersectsOrContains(line));

        // Crossing
        ensure(polygon(-0.5, -0.5, 0.1, -0.5, 0.1, 0.25, -0.5, 0.25).intersectsOrContains(line));

        // Outside
        ensureFalse(polygon(-0.5, -0.5, -1, -0.5, -1, 0.25, -0.5, 0.25).intersectsOrContains(line));

        // Touching the edge of polygon
        // assertTrue(polygon(-0.5, -0.5, 0, -0.5, 0, 0.25, -0.5, 0.25).intersectsOrContains(line));
    }

    @Test
    public void testSerialization()
    {
        var polygon = polygon(-0.5, -0.5, 1, -0.5, 1, 0.85, -0.5, 0.85);
        testSerialization(polygon);
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
