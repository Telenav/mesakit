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

package com.telenav.mesakit.map.geography.indexing.rtree;

import com.telenav.kivakit.core.test.SlowTest;
import com.telenav.kivakit.serialization.kryo.types.KryoTypes;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.GeographyUnitTest;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Category({ SlowTest.class })
public class RTreeSpatialIndexTest extends GeographyUnitTest
{
    private static class TestKryoTypes extends KryoTypes
    {
        public TestKryoTypes()
        {
            register(TestSpatialIndex.class, new TestRTreeSpatialIndexSerializer());
        }
    }

    @Test
    public void testClippingBug()
    {
        var bounds = Rectangle.parse("35.1955277,-106.5401421:35.1962294,-106.5388557");
        var index = new RTreeSpatialIndex<Polyline>("test", new RTreeSettings());
        var top = Polyline.parse("35.196277,-106.539661:35.196193,-106.539646:35.195664,-106.539666");
        index.add(top);
        var bottom = Polyline.parse("35.195664,-106.539666:35.195004,-106.539679");
        index.add(bottom);
        int count = 0;
        for (var ignored : index.intersecting(bounds))
        {
            count++;
        }
        ensureEqual(count, 2);
    }

    @Test
    public void testIntersection()
    {
        var unit = Rectangle.fromLocations(Location.ORIGIN,
                new Location(Latitude.degrees(1), Longitude.degrees(1)));

        var fullyInside = polyline(0.5, 0.5, 0.75, 0.75, 0.85, 0.85);
        var outsideLowerLeft = polyline(-1, -1, -0.5, -0.5, -0.85, -0.85);
        var outsideRight = polyline(0.5, 1.5, 0.75, 1.2, 0.85, 1.4);
        var crossingDiagonally = polyline(-0.5, -0.5, 1.5, 1.5, 2.0, 1.5);
        var crossingUpperLeftCorner = polyline(0.5, -.5, 1.5, 0.5);
        var outsideBelow = polyline(-0.5, -0.5, -0.5, 1.5);

        var index = new RTreeSpatialIndex<Polyline>("test", new RTreeSettings());
        // index.setViewer(new SwingViewer());
        index.add(fullyInside);
        // index.dump(System.out);
        index.add(outsideLowerLeft);
        // index.dump(System.out);
        index.add(outsideRight);
        // index.dump(System.out);
        index.add(crossingDiagonally);
        // index.dump(System.out);
        index.add(crossingUpperLeftCorner);
        // index.dump(System.out);
        index.add(outsideBelow);
        // index.dump(System.out);

        Set<Polyline> intersections = new HashSet<>();
        for (Polyline line : index.intersecting(unit))
        {
            intersections.add(line);
        }
        ensure(intersections.contains(fullyInside));
        ensure(intersections.contains(crossingDiagonally));
        ensure(intersections.contains(crossingUpperLeftCorner));
        ensureFalse(intersections.contains(outsideLowerLeft));
        ensureFalse(intersections.contains(outsideRight));
        ensureFalse(intersections.contains(outsideBelow));
    }

    @Test
    public void testRandom()
    {
        testRandom(false);
    }

    @Test
    public void testRandomBulkLoad()
    {
        testRandom(true);
    }

    @Test
    public void testSerialization()
    {
        randomValueFactory().seed(900178094L);

        var index = randomSpatialIndex(500);
        testSerialization(index);
    }

    @Override
    protected KryoTypes kryoTypes()
    {
        return super.kryoTypes().mergedWith(new TestKryoTypes());
    }

    private Polyline polyline(double... values)
    {
        List<Location> locations = new ArrayList<>();
        for (var i = 0; i < values.length; i += 2)
        {
            locations.add(new Location(Latitude.degrees(values[i]), Longitude.degrees(values[i + 1])));
        }
        return Polyline.fromLocations(locations);
    }

    private Location randomLocationNear(Rectangle bounds)
    {
        var expanded = bounds.expanded(bounds.widthAtBase().maximum(bounds.heightAsDistance()));
        return randomValueFactory().newLocation(expanded);
    }

    private Location randomLocationOutside(Rectangle bounds)
    {
        while (true)
        {
            var location = randomLocationNear(bounds);
            if (!bounds.contains(location))
            {
                return location;
            }
        }
    }

    private Polyline randomPolyline(Rectangle bounds, boolean intersects)
    {
        // Create polyline
        var builder = new PolylineBuilder();

        // Add a first point somewhere near or inside the rectangle
        var last = intersects ? randomLocationNear(bounds) : randomLocationOutside(bounds);
        builder.add(last);

        // For some number of segments
        var segments = randomInt(5, 10);
        for (var i = 0; i < segments; i++)
        {
            for (var j = 0; j < 1000; j++)
            {
                // Get a point that's near the rectangle
                var next = randomLocationNear(bounds);

                // and if the segment created by the point intersects (or doesn't intersect) the bounds,
                var segment = new Segment(last, next);
                if (segment.intersects(bounds) == intersects)
                {
                    // we add the point and continue
                    builder.add(last);
                    last = next;
                    break;
                }
            }
        }
        return builder.isValid() ? builder.build() : randomPolyline(bounds, intersects);
    }

    private List<Polyline> randomPolylines(int count)
    {
        List<Polyline> polylines = new ArrayList<>();
        for (var i = 0; i < count; i++)
        {
            var bounds = randomValueFactory().newRectangle();
            if (bounds.area().asSquareMeters() > 1000)
            {
                polylines.add(randomPolyline(bounds, false));
            }
        }
        return polylines;
    }

    private RTreeSpatialIndex<Polyline> randomSpatialIndex(int polylines)
    {
        var index = new TestSpatialIndex(new RTreeSettings());
        var loader = new RTreeBulkLoader<>(index);
        loader.load(randomPolylines(polylines));
        return index;
    }

    private void testRandom(boolean bulkLoad)
    {
        var iterations = 25;
        for (var iteration = 1; iteration <= iterations; iteration++)
        {
            // Create a random rectangle
            var bounds = randomValueFactory().newRectangle();

            // and if it's at least 1000 square meters
            if (bounds.area().asSquareMeters() > 1000)
            {
                // Create spatial index
                var index = new RTreeSpatialIndex<Polyline>("test", new RTreeSettings());

                // Create a set of intersecting lines
                Set<Polyline> intersecting = new HashSet<>();
                {
                    var count = randomInt(1, 1000);
                    for (var i = 0; i < count; i++)
                    {
                        var polyline = randomPolyline(bounds, true);
                        intersecting.add(polyline);
                    }
                }

                // and a set of disjoint lines
                Set<Polyline> disjoint = new HashSet<>();
                {
                    var count = randomInt(1, 1000);
                    for (var i = 0; i < count; i++)
                    {
                        var polyline = randomPolyline(bounds, false);
                        disjoint.add(polyline);
                    }
                }

                List<Polyline> all = new ArrayList<>();
                all.addAll(intersecting);
                all.addAll(disjoint);
                if (bulkLoad)
                {
                    new RTreeBulkLoader<>(index).load(all);
                }
                else
                {
                    for (Polyline line : all)
                    {
                        index.add(line);
                    }
                }

                // index.dump(System.out, DumpDetailLevel.SUMMARY_ONLY);

                // Then check that the index can find only the intersecting lines
                var found = index.intersecting(bounds);
                for (Polyline line : found)
                {
                    ensure(intersecting.contains(line));
                }
                for (Polyline line : disjoint)
                {
                    ensureFalse(intersecting.contains(line));
                }
            }
        }
    }
}
