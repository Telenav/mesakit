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

package com.telenav.aonia.map.geography.shape.segment;

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.geography.project.MapGeographyUnitTest;
import com.telenav.aonia.map.geography.shape.polyline.PolylineSnapper;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.geographic.Heading;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class SegmentTest extends MapGeographyUnitTest
{
    private static final Angle tolerance = Angle.degrees(0.05);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void testHeading()
    {
        final var start = randomValueFactory().newLocation();

        // we need a small rectangle for computations to be accurate
        final var rectangle = Rectangle.fromLocations(start, start).expanded(Distance.ONE_MILE);
        final var eastBound = new Segment(rectangle.bottomLeft(), rectangle.bottomRight());

        assertAngleDegrees(eastBound.heading(), 90);

        final var eastBoundStart = eastBound.start();
        final var eastBoundEnd = eastBound.end();
        ensureEqual(eastBoundStart.latitude(), eastBoundEnd.latitude());

        final var northBound = new Segment(rectangle.bottomRight(), rectangle.topRight());
        assertAngleDegrees(northBound.heading(), 0);

        final var northBoundStart = northBound.start();
        final var northBoundEnd = northBound.end();
        ensureEqual(northBoundStart.longitude(), northBoundEnd.longitude());
    }

    @Test
    public void testIntersection()
    {
        final var a = new Segment(Location.degrees(-0.5, -0.5), Location.degrees(1, -0.5));
        final var b = new Segment(Location.degrees(-0.5, -0.5), Location.degrees(-0.5, -0.446721));
        ensure(a.intersects(b));
        ensure(a.intersects(a));
    }

    @Test
    public void testIntersectionApproximation()
    {
        final var a = new Segment(Location.degrees(6.86E-4, 6.86E-4), Location.degrees(0.0, 6.86E-4));
        final var b = new Segment(Location.degrees(6.8E-5, 6.8E-5), Location.degrees(4.8E-4, 0.001167));

        ensure(a.intersects(b));
        ensureNotNull(a.intersection(b));
    }

    @Test
    public void testIntersectionApproximation2()
    {
        final var a = new Segment(Location.degrees(32.677459, -97.044982), Location.degrees(32.678146, -97.044982));
        final var b = new Segment(Location.degrees(32.677798, -97.044919), Location.degrees(32.67778, -97.04504));

        ensure(a.intersects(b));
        ensureNotNull(a.intersection(b));
    }

    @Test
    public void testIntersectionApproximation3()
    {
        final var a = new Segment(Location.degrees(38.991851, -77.01004), Location.degrees(38.992538, -77.01004));
        final var b = new Segment(Location.degrees(38.991931, -77.010012), Location.degrees(38.99181, -77.010056));

        ensure(a.intersects(b));
        final var intersection = a.intersection(b);
        ensureNotNull(intersection);
        ensureClose(a.start().longitude().asDm7(), intersection.longitude().asDm7(), 6);
    }

    @Test
    public void testIntersectionBoundaryCondition()
    {
        final var a = new Segment(location(36.9489, -106.0), location(34.9489, -106.0));
        final var b = new Segment(location(35.00501, -106.00567), location(35.00501, -105.99999));
        ensure(a.intersects(b));
        ensureNotNull(a.intersection(b));
    }

    @Test
    public void testIntersectionBug()
    {
        final var a = new Segment.Converter(LOGGER).convert("15.776397,-96.089141:15.776194,-96.088763");
        final var b = new Segment.Converter(LOGGER).convert("15.785357,-96.060539:15.785328,-96.060485");
        ensureNull(a.intersection(b));
    }

    @Test
    public void testParallel()
    {
        final var start = new Location(Latitude.degrees(37.385576), Longitude.degrees(-122.005974));
        final var end = new Location(Latitude.degrees(37.386482), Longitude.degrees(-122.004362));

        final var segment = new Segment(start, end);

        final var offset = Distance.meters(5);

        final var parallel = segment.parallel(true, offset);

        ensure(parallel.isParallel(segment, Angle.degrees(1)));
    }

    @Test
    public void testPerpendicularEast()
    {
        final var east = Location.ORIGIN.moved(Heading.EAST, Distance.ONE_MILE);
        final var segment = new Segment(Location.ORIGIN, east);
        final var perpendicularAtOrigin = segment.perpendicular(Location.ORIGIN, Distance.ONE_MILE);
        ensure(perpendicularAtOrigin.start().equals(Location.ORIGIN.moved(Heading.SOUTH, Distance.ONE_MILE)));
        ensure(perpendicularAtOrigin.end().equals(Location.ORIGIN.moved(Heading.NORTH, Distance.ONE_MILE)));
        final var perpendicularAtEast = segment.perpendicular(east, Distance.ONE_MILE);
        ensure(perpendicularAtEast.start().isClose(
                Location.ORIGIN.moved(Heading.SOUTH, Distance.ONE_MILE).moved(Heading.EAST, Distance.ONE_MILE),
                Distance.meters(1)));
        ensure(perpendicularAtEast.end().isClose(
                Location.ORIGIN.moved(Heading.NORTH, Distance.ONE_MILE).moved(Heading.EAST, Distance.ONE_MILE),
                Distance.meters(1)));
    }

    @Test
    public void testProjectionCloseBy()
    {
        final var head = new Location(Latitude.degrees(21.31378), Longitude.degrees(-157.8538));
        final var tail = new Location(Latitude.degrees(21.31403), Longitude.degrees(-157.85394));
        final var point = new Location(Latitude.degrees(21.313837), Longitude.degrees(-157.853931));
        final var segment = new Segment(head, tail);
        final var snapped = new PolylineSnapper().snap(segment, point);
        ensureClose(segment.heading().asDegrees() / 10.0,
                point.headingTo(snapped).plus(Angle.degrees(-90.0)).asDegrees() / 10.0, 0);
    }

    @Test
    public void testSnapping()
    {
        final var start = new Location(Latitude.degrees(37.385576), Longitude.degrees(-122.005974));
        final var end = new Location(Latitude.degrees(37.386482), Longitude.degrees(-122.004362));
        final var segment = new Segment(start, end);

        final var point = new Location(Latitude.degrees(37.386458), Longitude.degrees(-122.006001));
        final var snapped = new PolylineSnapper().snap(segment, point);

        final var toCompare = new Location(Latitude.degrees(37.385860019), Longitude.degrees(-122.00546866));
        ensure(toCompare.isClose(snapped, Angle.degrees(0.0000001)));

        ensureClose(0.31, snapped.offsetOnSegment().asZeroToOne(), 2);

        ensureClose(segment.heading().asDegrees(), point.headingTo(snapped).plus(Angle.degrees(-90.0)).asDegrees(), 0);

        final var point2 = new Location(Latitude.degrees(37.0), Longitude.degrees(-123.0));
        final Location snapped2 = new PolylineSnapper().snap(segment, point2);
        ensureEqual(start, new Location(snapped2.latitude(), snapped2.longitude()));
    }

    private void assertAngleDegrees(final Angle angle, final double degrees)
    {
        ensure(Heading.degrees(degrees).absoluteDifference(angle).isLessThan(tolerance));
    }
}
