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

import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.test.UnitTest;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.mesakit.map.measurements.geographic.Distance.ZERO;
import static com.telenav.mesakit.map.measurements.geographic.Distance.meters;
import static com.telenav.mesakit.map.measurements.geographic.Heading.EAST;
import static java.util.Arrays.asList;

@SuppressWarnings("ConstantConditions")
public class PolylineTest extends UnitTest
{
    @Test
    public void testBisect()
    {
        ensureEqual(1, polyline(0, 0, 1, 1).bisect().size());
        ensureEqual(2, polyline(0, 0, 1, 1, 2, 2).bisect().size());
        {
            var sections = polyline(0, 0, 1, 1, 2, 2).bisect();
            ensureEqual(section(polyline(0, 0, 1, 1, 2, 2), 0, 1), sections.get(0));
            ensureEqual(section(polyline(0, 0, 1, 1, 2, 2), 1, 2), sections.get(1));
        }
        {
            var sections = polyline(0, 0, 1, 1, 2, 2, 3, 3).bisect();
            ensureEqual(section(polyline(0, 0, 1, 1, 2, 2, 3, 3), 0, 2), sections.get(0));
            ensureEqual(section(polyline(0, 0, 1, 1, 2, 2, 3, 3), 2, 3), sections.get(1));
        }
    }

    @Test
    public void testIntersection()
    {
        var unit = Rectangle.fromLocations(Location.ORIGIN, new Location(Latitude.degrees(1), Longitude.degrees(1)));

        // Fully inside
        ensure(polyline(0.5, 0.5, 0.75, 0.75, 0.85, 0.85).intersects(unit));

        // Outside lower left
        ensure(!polyline(-1, -1, -.5, -.5, -0.85, -0.85).intersects(unit));

        // Outside right
        ensure(!polyline(0.5, 1.5, 0.75, 1.2, 0.85, 1.4).intersects(unit));

        // Crossing diagonally
        ensure(polyline(-0.5, -0.5, 1.5, 1.5, 2.0, 1.5).intersects(unit));

        // Crossing upper left corner
        ensure(polyline(.5, -.5, 1.5, .5).intersects(unit));
    }

    @Test
    public void testIntersectionBug()
    {
        var a = segment(32.9489, -82.0, 0.9489, -82.0);
        var b = segment(30.952673, -82.001432, 30.952329, -81.999931);
        ensure(a.intersects(b));
        ensureNotNull(a.intersection(b));

        var bounds = new Rectangle.Converter(Listener.none()).convert("30.9489,-84.0:32.9489,-82.0");
        ensure(bounds.asPolyline().intersects(b));
        ensureNotNull(bounds.asPolyline().intersection(b));
    }

    @Test
    public void testParallel()
    {
        var value = "37.82088,-122.29884 37.82185,-122.30077 37.82139,-122.30125 37.82138,-122.30128 37.82137,-122.30131 37.82139,-122.30146 37.8215,-122.30165";
        List<Location> locations = new ArrayList<>();

        for (String item : StringList.split(value, " "))
        {
            var items = StringList.split(item, ",");
            var latitude = Double.parseDouble(items.first());
            var longitude = Double.parseDouble(items.last());
            var location = new Location(Latitude.degrees(latitude), Longitude.degrees(longitude));

            locations.add(location);
        }

        var polyline = Polyline.fromLocations(locations);
        var heading = Heading.degrees(45 + 180);
        var offset = meters(20);
        var parallel = polyline.parallel(heading, offset);
        for (var i = 0; i < polyline.segments().size(); i++)
        {
            var segment1 = polyline.segments().get(i);
            var segment2 = parallel.segments().get(i);

            ensure(segment1.isParallel(segment2, Angle.degrees(1)));
        }
    }

    @Test
    public void testSectionFromFirstSegment()
    {
        var polyline = Polyline.fromLocations(asList(Location.ORIGIN, Location.ORIGIN.moved(EAST, meters(100))));
        var sectionLength = meters(10);
        var section = polyline.section(ZERO, sectionLength);
        ensure(sectionLength.difference(section.length()).isLessThan(meters(1)));
    }

    @Test
    public void testSimplification()
    {
        // Complex
        var complex = polyline(0, 0, 1, 1, 2, 0, 3, 1, 4, 0);
        var simple1 = complex.simplified(Distance.ONE_METER);
        ensure(simple1.size() == complex.size());

        // Loop
        var loop = polyline(0, 0, 1, 1, 2, 2, 3, 1, 2, 0, 0, 0);
        var simple2 = loop.simplified(Distance.EARTH_RADIUS_MINOR);
        ensure(simple2.size() == 3);

        // Easy
        var easy = polyline(0, 0, 0, 1, 0, 2, 0, 3, 1, 3, 2, 3, 3, 3);
        var simple3 = easy.simplified(Distance.ONE_MILE);
        ensure(simple3.size() == 3);
        ensure(new Location(Latitude.degrees(0), Longitude.degrees(3)).equals(simple3.get(1)));
    }

    @Test
    public void testTrisect()
    {
        ensureEqual(1, polyline(0, 0, 1, 1).trisect().size());
        ensureEqual(1, polyline(0, 0, 1, 1, 2, 2).trisect().size());

        var fourPointLine = polyline(0, 0, 1, 1, 2, 2, 3, 3);

        ensureEqual(3, fourPointLine.trisect().size());

        {
            var sections = fourPointLine.trisect();
            ensureEqual(section(fourPointLine, 0, 1), sections.get(0));
            ensureEqual(section(fourPointLine, 1, 2), sections.get(1));
            ensureEqual(section(fourPointLine, 2, 3), sections.get(2));
        }
        {
            var fivePointLine = polyline(0, 0, 1, 1, 2, 2, 3, 3, 4, 4);
            var sections = fivePointLine.trisect();
            ensureEqual(section(fivePointLine, 0, 1), sections.get(0));
            ensureEqual(section(fivePointLine, 1, 2), sections.get(1));
            ensureEqual(section(fivePointLine, 2, 4), sections.get(2));
        }
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

    private PolylineSection section(Polyline line, int start, int end)
    {
        return new PolylineSection(line, start, end);
    }

    private Segment segment(double latitude1, double longitude1, double latitude2,
                            double longitude2)
    {
        return new Segment(Location.degrees(latitude1, longitude1), Location.degrees(latitude2, longitude2));
    }
}
