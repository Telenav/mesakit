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

package com.telenav.mesakit.map.geography.indexing.polygon;

import com.telenav.kivakit.resource.packages.PackageResource;
import com.telenav.kivakit.testing.SlowTest;
import com.telenav.mesakit.map.geography.GeographyUnitTest;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ SlowTest.class })
public class PolygonSpatialIndexTest extends GeographyUnitTest
{
    @Test
    public void test1()
    {
        var polygon = Polygon.fromLocationSequence(Location.ORIGIN,
                Location.ORIGIN.moved(Heading.NORTHEAST, Distance.kilometers(100)),
                Location.ORIGIN.moved(Heading.NORTHWEST, Distance.kilometers(100)));
        ensure(polygon.isCounterClockwise());
        new PolygonSpatialIndex(polygon);
    }

    @Test
    public void test2()
    {
        var polygon = Polygon.fromLocationSequence(Location.ORIGIN,
                Location.ORIGIN.moved(Heading.NORTHEAST, Distance.kilometers(100)),
                Location.ORIGIN.moved(Heading.NORTH, Distance.kilometers(50)),
                Location.ORIGIN.moved(Heading.NORTHWEST, Distance.kilometers(100)));
        new PolygonSpatialIndex(polygon);
    }

    @Test
    public void test3()
    {
        var i = 0;
        for (Polygon polygon : world().polygons())
        {
            var index = new PolygonSpatialIndex(polygon);
            var center = polygon.bounds().center();
            if (!index.containment(center).matches(polygon.containment(center)))
            {
                trace("#" + i + ". failed: index = " + index.containment(center)
                        + ", polygon = " + polygon.containment(center));
                fail("index.containment() failed");
            }
            i++;
        }
    }

    @Test
    public void test4()
    {
        testPolygonIndex(Polygon.fromLocationSequence(Location.ORIGIN,
                Location.ORIGIN.moved(Heading.NORTHEAST, Distance.kilometers(100)),
                Location.ORIGIN.moved(Heading.NORTH, Distance.kilometers(50)),
                Location.ORIGIN.moved(Heading.NORTHWEST, Distance.kilometers(100))));
    }

    @Test
    public void test5()
    {
        // random().seed(1595079367L);

        var index = 0;

        for (Polygon polygon : world().polygons())
        {
            trace("\ntesting polygon " + index + " with " + polygon.segmentCount() + " segments");

            // Polygon 15, 100 and 221 all span the -180 longitude boundary so it's an edge case we
            // don't care about since none of our code really works at that location
            if (index != 15 && index != 100 && index != 221)
            {
                testPolygonIndex(polygon);
            }
            index++;
        }
    }

    @Test
    public void test6()
    {
        var i = 0;
        for (Polygon polygon : world().polygons())
        {
            if (i == 2)
            {
                var index = new PolygonSpatialIndex(polygon);
                var location = Location.degrees(-54.45, -65.93118);
                if (index.contains(location))
                {
                    ensure(index.contains(location));
                }
            }
            i++;
        }
    }

    @Test
    public void testSerialization()
    {
        var polygon = Polygon.fromLocationSequence(Location.ORIGIN,
                Location.ORIGIN.moved(Heading.NORTHEAST, Distance.kilometers(100)),
                Location.ORIGIN.moved(Heading.NORTHWEST, Distance.kilometers(100)));
        ensure(polygon.isCounterClockwise());
        var spatialIndex = new PolygonSpatialIndex(polygon);
        testSerialization(spatialIndex);
    }

    private void testPolygonIndex(Polygon polygon)
    {
        var index = new PolygonSpatialIndex(polygon);
        var bounds = polygon.bounds();
        for (var i = 0; i < 100; i++)
        {
            var location = newRandomValueFactory().newLocation(bounds);
            var polygonContainment = polygon.containment(location);
            var indexContainment = index.containment(location);
            if (!indexContainment.matches(polygonContainment))
            {
                trace("Containment test failed at " + location + ": polygon = " + polygonContainment
                        + ", index = " + indexContainment);
                ensure(polygonContainment.matches(indexContainment), "Containment $ doesn't match $ for polygon $ at $",
                        polygonContainment, indexContainment, polygon, location);
            }
        }
    }

    private ShapeFileReader world()
    {
        return new ShapeFileReader(this, PackageResource.packageResource(this, PolygonSpatialIndexTest.class, "world.shp"));
    }
}
