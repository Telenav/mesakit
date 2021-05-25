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

package com.telenav.mesakit.map.geography.indexing.quadtree;

import com.telenav.kivakit.kernel.language.collections.list.BaseList;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.threading.KivaKitThread;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class QuadTreeSpatialIndexTest extends MapGeographyUnitTest
{
    public static final boolean PROFILE = false;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void test()
    {
        final var index = new QuadTreeSpatialIndex<Location>(2, Distance.miles(10));
        final var location = location(10, 10);
        final var rectangle = Rectangle.fromLocations(location(0, 0), location(20, 20));
        index.add(location);
        ensureEqual(true, index.inside(rectangle).hasNext());
        index.add(location(11, 11));
        ensureEqual(Count.count(2), Count.count(index.inside(rectangle)));
        index.add(location(21, 21));
        ensureEqual(Count.count(2), Count.count(index.inside(rectangle)));
        index.add(location(-21, -21));
        ensureEqual(Count.count(2), Count.count(index.inside(rectangle)));
        index.add(location(9, 9));
        ensureEqual(Count.count(3), Count.count(index.inside(rectangle)));
        index.add(location(8, 8));
        ensureEqual(Count.count(4), Count.count(index.inside(rectangle)));
        index.add(location(7, 7));
        ensureEqual(Count.count(5), Count.count(index.inside(rectangle)));
        index.add(location(6, 6));
        ensureEqual(Count.count(6), Count.count(index.inside(rectangle)));
        index.add(location(5, 5));
        ensureEqual(Count.count(7), Count.count(index.inside(rectangle)));
        index.add(location(15, 15));
        ensureEqual(Count.count(8), Count.count(index.inside(rectangle)));
        index.add(location(16, 16));
        ensureEqual(Count.count(9), Count.count(index.inside(rectangle)));
        index.add(location(17, 17));
        ensureEqual(Count.count(10), Count.count(index.inside(rectangle)));
    }

    @Test
    public void test4()
    {
        final var index = new QuadTreeSpatialIndex<Location>(2, Distance.miles(10));
        final var rectangle = Rectangle.fromLocations(location(-20, -20), location(20, 20));
        index.add(location(10, -10));
        index.add(location(11, -11));
        index.add(location(12, -12));
        index.add(location(13, -13));
        ensureEqual(true, index.inside(rectangle).hasNext());
        ensureEqual(Count.count(4), Count.count(index.inside(rectangle)));
        index.add(location(10, 10));
        index.add(location(11, 11));
        index.add(location(12, 12));
        index.add(location(13, 13));
        ensureEqual(true, index.inside(rectangle).hasNext());
        ensureEqual(Count.count(8), Count.count(index.inside(rectangle)));
        index.add(location(-10, -10));
        index.add(location(-11, -11));
        index.add(location(-12, -12));
        index.add(location(-13, -13));
        ensureEqual(true, index.inside(rectangle).hasNext());
        ensureEqual(Count.count(12), Count.count(index.inside(rectangle)));
        index.add(location(-10, 10));
        index.add(location(-11, 11));
        index.add(location(-12, 12));
        index.add(location(-13, 13));
        ensureEqual(true, index.inside(rectangle).hasNext());
        ensureEqual(Count.count(16), Count.count(index.inside(rectangle)));
    }

    @Test
    public void testBoundaries()
    {
        final var index = new QuadTreeSpatialIndex<Location>(8, Distance.miles(10));
        for (var latitude = Latitude.MINIMUM_DEGREES; latitude <= Latitude.MAXIMUM_DEGREES; latitude += Latitude.MAXIMUM_DEGREES)
        {
            for (var longitude = Longitude.MINIMUM_DEGREES; longitude <= Longitude.MAXIMUM_DEGREES; longitude += Longitude.MAXIMUM_DEGREES)
            {
                index.add(location(latitude, longitude));
            }
        }

        // index.dump(System.out);

        final var locations = index.inside(Rectangle.MAXIMUM);
        var count = 0;
        while (locations.hasNext())
        {
            locations.next();
            count++;
        }

        ensureEqual(9, count);
    }

    @Test
    public void testRandom()
    {
        for (var iteration = 0; iteration < 3; iteration++)
        {
            testRandomOnce(iteration);
        }
    }

    public void testRandomOnce(final int iteration)
    {
        final var index = new QuadTreeSpatialIndex<Location>(10, Distance.miles(0.1));
        final var rectangle = randomValueFactory().newRectangle();
        final var inside = Collections.synchronizedList(new ArrayList<Location>());
        final var threads = randomInt(5, 10);
        final var total = new AtomicInteger(0);
        final var exited = new CountDownLatch(threads);
        for (var i = 0; i < threads; i++)
        {
            final var thread = new KivaKitThread("test-" + iteration + "-" + i)
            {
                @Override
                protected void onRun()
                {
                    final var count = randomInt(100, 1000);
                    total.addAndGet(count);
                    for (var i = 0; i < count; i++)
                    {
                        final var location = randomValueFactory().newLocation();
                        index.add(location);
                        if (rectangle.contains(location))
                        {
                            inside.add(location);
                        }
                    }
                    exited.countDown();
                }

                {
                    addListener(LOGGER);
                }
            };
            thread.start();
        }
        try
        {
            exited.await();
        }
        catch (final InterruptedException e)
        {
            e.printStackTrace();
        }
        final BaseList<Location> found = new ObjectList<Location>().appendAll(index.inside(rectangle));
        ensureEqual(inside.size(), found.size());
        for (final Location location : found)
        {
            ensure(inside.contains(location));
        }
        // For profiling
        if (PROFILE)
        {
            LOGGER.information("Profiling");
            for (var i = 0; i < 100000; i++)
            {
                final var bounds = randomValueFactory().newRectangle();
                Count.count(index.inside(bounds));
            }
        }
    }
}
