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

package com.telenav.mesakit.map.geography.shape.rectangle;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.junit.Test;

public class RectangleTest extends MapGeographyUnitTest
{
    @Test
    public void testArea()
    {
        final var locationA = new Location(Latitude.degrees(37.385576), Longitude.degrees(-122.005974));
        final var locationB = new Location(Latitude.degrees(37.386482), Longitude.degrees(-122.004362));
        final var locationC = new Location(Latitude.degrees(37.385576), Longitude.degrees(-122.004362));
        final var horizontal = locationC.preciseDistanceTo(locationA);
        final var vertical = locationC.preciseDistanceTo(locationB);
        final var expected = Area.squareMeters((long) (horizontal.asMeters() * vertical.asMeters()));
        trace("Area expected: ${debug} square meters.", expected.asSquareMeters());
        final var telenav = Rectangle.fromLocations(locationA, locationB);
        final var telenavArea = telenav.area();
        trace("Area found: ${debug} square meters.", telenavArea.asSquareMeters());
        ensureClose(expected.asSquareMeters(), telenavArea.asSquareMeters(), 0);
        final var rect2 = Rectangle.fromCenterAndRadius(Location.degrees(47.601765, -122.332335), Distance.miles(100));
        final var rectArea2 = rect2.area();
        trace("Area found: ${debug} square miles.", rectArea2.asSquareMiles());
        ensureWithin(Area.squareMiles(200 * 200).asSquareMiles(), rectArea2.asSquareMiles(), 1);
    }

    @Test
    public void testCenter()
    {
        final var rectangle = randomValueFactory().newRectangle();

        // Determine the width and height.
        final var height = rectangle.topRight().latitude().asNanodegrees()
                - rectangle.bottomLeft().latitude().asNanodegrees();
        final var width = rectangle.topRight().longitude().asNanodegrees()
                - rectangle.bottomLeft().longitude().asNanodegrees();

        // Determine the center point.
        final var size = new Size(Width.nanodegrees(width / 2), Height.nanodegrees(height / 2));
        final var expected = rectangle.bottomLeft().offset(size);

        ensureEqual(expected, rectangle.center());
    }

    @Test
    public void testConsistency()
    {
        final var a = location(35.00501, -106.00567);
        final var b = location(35.00501, -105.99999);
        final var bounds = rectangle("34.9489,-108.0:36.9489,-106.0");
        ensureFalse(bounds.contains(b));
        ensure(bounds.contains(a));
        ensure(new Segment(a, b).intersects(bounds));
    }

    @Test
    public void testContains_Location()
    {
        final var rectangle = randomValueFactory().newRectangle();

        // Generate a location clearly within the box.
        ensure(rectangle.contains(rectangle.center()));

        // The bottom left corners should be inside
        ensure(rectangle.contains(rectangle.bottomLeft()));

        // The top right corner should be excluded
        ensureFalse(rectangle.contains(rectangle.topRight()));

        // Generate a location on the line.
        var latitude = rectangle.bottom();
        var longitude = rectangle.center().longitude();
        ensure(rectangle.contains(new Location(latitude, longitude)));

        // Generate a location outside of the box.
        latitude = rectangle.center().latitude();
        longitude = rectangle.right().plus(Longitude.MAXIMUM.minus(rectangle.right()).times(.5));
        ensureFalse(rectangle.contains(new Location(latitude, longitude)));

        // A zero size rectangle does not contain the lower left
        final var x = Location.degrees(36.55511, -121.92965);
        final var r = rectangle("36.55511,-121.92965:36.55511,-121.92965");
        ensureFalse(r.contains(x));

        // A non-zero size rectangle does not contain the lower left
        final var x2 = Location.degrees(36.55511, -121.92965);
        final var r2 = rectangle("36.55511,-121.92965:36.55512,-121.92964");
        ensure(r2.contains(x2));

        final var maximum = Rectangle.MAXIMUM;
        ensure(maximum.contains(Location.degrees(90, 180)));
        ensure(maximum.contains(Location.degrees(90, -180)));
        ensure(maximum.contains(Location.degrees(-90, 180)));
        ensure(maximum.contains(Location.degrees(-90, -180)));
    }

    @Test
    public void testContains_Rectangle()
    {
        final var rectangle = randomValueFactory().newRectangle();

        // A rectangle contains itself
        ensure(rectangle.contains(rectangle));

        // A rectangle does not contain itself incremented
        ensureFalse(rectangle.contains(rectangle.incremented()));
    }

    @Test
    public void testExpand()
    {
        final var rectangle = randomValueFactory().newRectangle();
        final var distance = randomValueFactory().newDistance(Distance.MINIMUM, Distance.EARTH_RADIUS);
        final var expansion = Angle.degrees(distance.asDegrees()).asNanodegrees();

        // Grow the lower left down and to the left.
        var llLatitude = rectangle.bottom().asNanodegrees() - expansion;
        var llLongitude = rectangle.left().asNanodegrees() - expansion;

        // Grow the upper right up and to the right.
        var urLatitude = rectangle.top().asNanodegrees() + expansion;
        var urLongitude = rectangle.right().asNanodegrees() + expansion;

        // Cap the values.
        llLatitude = Math.max(llLatitude, Latitude.MINIMUM.asNanodegrees());
        llLongitude = Math.max(llLongitude, Longitude.MINIMUM.asNanodegrees());
        urLatitude = Math.min(urLatitude, Latitude.MAXIMUM.asNanodegrees());
        urLongitude = Math.min(urLongitude, Longitude.MAXIMUM.asNanodegrees());

        // Create the rectangle.
        final var lowerLeft = new Location(Latitude.nanodegrees(llLatitude), Longitude.nanodegrees(llLongitude));
        final var upperRight = new Location(Latitude.nanodegrees(urLatitude), Longitude.nanodegrees(urLongitude));
        final var expected = Rectangle.fromLocations(lowerLeft, upperRight);

        ensureEqual(expected, rectangle.expanded(distance));
    }

    @Test
    public void testForLocations()
    {
        final var locationA = randomValueFactory().newLocation();
        final var locationB = randomValueFactory().newLocation();

        // Determine the lower left corner.
        final var lowerLeftLatitude = locationA.latitude().minimum(locationB.latitude());
        final var lowerLeftLongitude = locationA.longitude().minimum(locationB.longitude());
        final var lowerLeft = new Location(lowerLeftLatitude, lowerLeftLongitude);

        // Determine the upper right corner.
        final var upperRightLatitude = locationA.latitude().maximum(locationB.latitude());
        final var upperRightLongitude = locationA.longitude().maximum(locationB.longitude());
        final var upperRight = new Location(upperRightLatitude, upperRightLongitude);

        ensureEqual(Rectangle.fromLocations(lowerLeft, upperRight), Rectangle.fromLocations(locationA, locationB));
    }

    @Test
    public void testFromCenter()
    {
        final var location = randomValueFactory().newLocation();
        final var shift = Distance.miles(10.0);
        final var rectangle = Rectangle.fromCenterAndRadius(location, shift);
        final var orthogonalDistance = rectangle.bottomLeft().moved(Heading.degrees(0), shift).preciseDistanceTo(location);
        final var tolerance = shift.times(0.01);
        trace("Orthogonal Distance: ${debug}", orthogonalDistance.asMiles());
        trace("Tolerance: ${debug}", tolerance.asMiles());
        ensureWithin(shift.asMiles(), orthogonalDistance.asMiles(), tolerance.asMiles());
    }

    @Test
    public void testHeight()
    {
        final var rectangle = randomValueFactory().newRectangle();
        final var height = rectangle.topRight().latitude().asMicrodegrees()
                - rectangle.bottomLeft().latitude().asMicrodegrees();
        ensureEqual(height, rectangle.height().asMicrodegrees());
    }

    @Test
    public void testIntersection()
    {
        final var rectangle = randomValueFactory().newRectangle();
        ensure(rectangle.intersects(rectangle));

        {
            final var a = rectangle("0,0:10,10");
            final var b = rectangle("5,5:10,10");
            ensureEqual(rectangle("5,5:10,10"), a.intersect(b));
        }
        {
            final var a = rectangle("0,0:10,10");
            final var b = rectangle("0,0:10,10");
            ensureEqual(rectangle("0,0:10,10"), a.intersect(b));
        }
        {
            final var a = rectangle("0,0:10,10");
            final var b = rectangle("5,5:6,6");
            ensureEqual(rectangle("5,5:6,6"), a.intersect(b));
        }
        {
            final var a = rectangle("0,0:10,10");
            final var b = rectangle("5,5:20,20");
            ensureEqual(rectangle("5,5:10,10"), a.intersect(b));
        }
        {
            final var a = rectangle("0,0:10,10");
            final var b = rectangle("15,15:20,20");
            ensureNull(a.intersect(b));
        }
    }

    @Test
    public void testIntersects()
    {
        final var loc = randomValueFactory().newLocation();

        final var rect1 = Rectangle.fromLocations(loc, loc).expanded(Distance.ONE_MILE);

        final var size = new Size(Width.degrees(0), Height.degrees(1));
        final var loc2 = loc.offset(size);
        final var rect2 = Rectangle.fromLocations(loc2, loc2).expanded(Distance.ONE_MILE);

        ensure(!rect1.intersects(rect2));
        ensure(rect1.intersects(rect1));

        final var o1 = Location.ORIGIN.bounds().expanded(Distance.ONE_METER);
        final var o2 = Location.ORIGIN.bounds().expanded(Distance.TEN_METERS);
        ensure(o1.intersects(o2));
        ensure(o2.intersects(o1));
    }

    @Test
    public void testMaximumRectangleCenter()
    {
        final var rectangle = Rectangle.MAXIMUM;

        // Determine the width and height.
        final var height = rectangle.topRight().latitude().asMicrodegrees()
                - rectangle.bottomLeft().latitude().asMicrodegrees();
        final var width = rectangle.topRight().longitude().asMicrodegrees()
                - rectangle.bottomLeft().longitude().asMicrodegrees();

        // Determine the center point.
        final var size = new Size(Width.microdegrees(width / 2), Height.microdegrees(height / 2));
        final var expected = rectangle.bottomLeft().offset(size);

        ensureEqual(expected, rectangle.center());
    }

    @Test
    public void testStrips()
    {
        var i = 0;
        final var width = Distance.miles(1000);
        final var overlap = Distance.ZERO;
        for (final Rectangle strip : Rectangle.MAXIMUM.verticalStrips(width, overlap))
        {
            trace(++i + ". " + strip);
            if (i < 25)
            {
                ensureClose(0.0,
                        Math.abs(width.add(overlap).asMiles() - strip.width().projectionOnEarthSurface().asMiles()), 1);
            }
            else
            {
                ensureClose(880.6, strip.width().projectionOnEarthSurface().asMiles(), 1);
            }
        }
        ensureEqual(25, i);
    }

    @Test
    public void testStripsByCount()
    {
        var i = 0;
        final var stripCount = 10;
        final var overlap = Width.ZERO;
        for (final Rectangle strip : Rectangle.MAXIMUM.verticalStrips(stripCount, overlap))
        {
            trace(++i + ". " + strip);
            ensureClose(0.0,
                    Math.abs(Rectangle.MAXIMUM.width().dividedBy(10.0).asDegrees() - strip.width().asDegrees()), 1);
        }
        ensureEqual(10, i);
    }

    @Test
    public void testStripsByCountWithOverlap()
    {
        var i = 0;
        final var stripCount = 10;
        final var overlap = Width.angle(Distance.miles(200).asAngle());
        for (final Rectangle strip : Rectangle.MAXIMUM.verticalStrips(stripCount, overlap))
        {
            trace(++i + ". " + strip);
            if (i < 10)
            {
                ensureClose(0.0, Math.abs(Rectangle.MAXIMUM.width().dividedBy(10.0).asDegrees()
                        - strip.width().minus(overlap).asDegrees()), 1);
            }
            else
            {
                ensureClose(0.0,
                        Math.abs(Rectangle.MAXIMUM.width().dividedBy(10.0).asDegrees() - strip.width().asDegrees()), 1);
            }
        }
        ensureEqual(10, i);
    }

    @Test
    public void testStripsWithOverlap()
    {
        var i = 0;
        final var width = Distance.miles(1000);
        final var overlap = Distance.miles(200);
        for (final Rectangle strip : Rectangle.MAXIMUM.verticalStrips(width, overlap))
        {
            trace(++i + ". " + strip);
            if (i < 25)
            {
                ensureClose(0.0,
                        Math.abs(width.add(overlap).asMiles() - strip.width().projectionOnEarthSurface().asMiles()), 1);
            }
            else
            {
                ensureClose(880.6, strip.width().projectionOnEarthSurface().asMiles(), 1);
            }
        }
        ensureEqual(25, i);
    }

    @Test
    public void testUpperRight()
    {
        final var rectangle = randomValueFactory().newRectangle();
        ensureFalse(rectangle.contains(rectangle.topRight().incremented()));
        ensureFalse(rectangle.contains(rectangle.topRight()));
        ensure(rectangle.contains(rectangle.topRight().decremented()));
    }
}
