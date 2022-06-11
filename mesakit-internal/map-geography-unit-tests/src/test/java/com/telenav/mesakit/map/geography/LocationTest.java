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

package com.telenav.mesakit.map.geography;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.testing.GeographyUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import org.junit.Test;

@SuppressWarnings({ "ConstantConditions", "SpellCheckingInspection" })
public class LocationTest extends GeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void testAdd()
    {
        // Assume that the unit test for Latitude and Longitude provide sufficient coverage for
        // their add methods.
        var location = newRandomValueFactory().newLocation();
        var size = newRandomValueFactory().newSize(Size.MAXIMUM);

        var expectedLatitude = location.latitude().plus(size.height());
        var expectedLongitude = location.longitude().plus(size.width());
        var expectedLocation = new Location(expectedLatitude, expectedLongitude);

        ensureEqual(expectedLocation, location.offsetBy(size));
    }

    @Test
    public void testDegreesMinutesAndSeconds()
    {
        var converter = new Location.DegreesMinutesAndSecondsConverter(LOGGER);
        var location = converter.convert("+47°40'46.92\", -122°20'42.96\"");

        var decimalConverter = new Location.DegreesConverter(LOGGER);
        var decimalLocation = decimalConverter.convert("47.679699999,-122.345266666");

        ensureEqual(decimalLocation, location);
    }

    @Test
    public void testDistance()
    {
        for (double latitude = 0; latitude < Latitude.MAXIMUM_DEGREES; latitude += 1.0)
        {
            var north = Location.degrees(latitude, 0.0);
            var location = north.moved(Heading.EAST, Distance.meters(100));
            ensureBetween(north.equirectangularDistanceToInMillimeters(location), 99_000, 100_000);
            ensureBetween(north.lawOfCosinesDistanceTo(location).asMeters(), 99.9, 100);
            ensureBetween(north.haversineDistanceTo(location).asMeters(), 99.9, 100);
        }

        random().loop(() ->
        {
            var from = newRandomValueFactory().newLocation(Rectangle.MINIMUM.expanded(Distance.kilometers(5_000)));
            var to = newRandomValueFactory().newLocation(from.bounds().expanded(Distance.kilometers(1)));
            var haversine = from.haversineDistanceTo(to);
            var cosines = from.lawOfCosinesDistanceTo(to);
            var rectangular = from.equirectangularDistanceTo(to);
            ensure(haversine.difference(cosines).isLessThan(Distance.meters(1)));
            if (haversine.difference(rectangular).percentageOf(haversine).isGreaterThan(Percent.percent(3)))
            {
                fail("The distance between " + from + " and " + to + " is " + haversine.asMeters() + " meters by haversine and " + rectangular.asMeters() + " meters by equirectangular");
            }
            if (cosines.difference(rectangular).percentageOf(haversine).isGreaterThan(Percent.percent(3)))
            {
                fail("The distance between " + from + " and " + to + " is " + haversine.asMeters() + " meters by cosines and " + rectangular.asMeters() + " meters by equirectangular");
            }
        });
    }

    @Test
    public void testForDm7()
    {
        Location.dm7(0, 0);

        // 160 degrees is valid for a longitude
        Location.dm7(0, 1600000000);

        // but not for a latitude
        ensureThrows(() -> Location.dm7(160_000_000_0, 0));
    }

    @Test
    public void testHeadingTo()
    {
        // Test for a "point" segment
        var start = new Location(Latitude.degrees(37.0), Longitude.degrees(-122.0));
        var end = new Location(Latitude.degrees(37.0), Longitude.degrees(-122.0));
        var heading = start.headingTo(end);
        ensureEqual(Heading.degrees(0.0), heading);

        ensureWithin(Heading.NORTHEAST.asDegrees(), Location.degrees(0, 0)
                .headingTo(Location.degrees(1, 1)).asDegrees(), 0.1);

        ensureWithin(Heading.NORTHEAST.asDegrees(), Location.degrees(0, 0)
                .headingTo(Location.degrees(0.1, 0.1)).asDegrees(), 0.1);
    }

    @Test
    public void testLong6()
    {
        testLongConversion6(Location.MAXIMUM);
        testLongConversion6(Location.MINIMUM);
        testLongConversion6(Location.ORIGIN);
        for (var i = 0; i < 1000; i++)
        {
            testLongConversion6(newRandomValueFactory().newLocation());
        }
    }

    @Test
    public void testLong7()
    {
        testLongConversion7(Location.MAXIMUM);
        testLongConversion7(Location.MINIMUM);
        testLongConversion7(Location.ORIGIN);
        for (var i = 0; i < 1000; i++)
        {
            testLongConversion7(newRandomValueFactory().newLocation());
        }
    }

    @Test
    public void testPrecision()
    {
        var locationDm6 = Location.degrees(34.123456, 34.123456);
        var dm6 = locationDm6.asDm6Long();
        ensure(locationDm6.isClose(Location.dm6(dm6), Angle.degrees(0.000_001)));

        var locationDm7 = Location.degrees(34.1234567, 34.1234567);
        var dm7 = locationDm7.asDm7Long();
        ensureEqual(locationDm7, Location.dm7(dm7));
    }

    @Test
    public void testQuantize()
    {
        // 100 meters is about 900 microdegrees
        ensureEqual(Location.degrees(0, 0), Location.degrees(0.000_400, 0.000_400).quantize(Distance.meters(100)));
        ensureEqual(Location.degrees(0.000_800, 0.000_800).quantize(Distance.meters(100)),
                Location.degrees(0.000_600, 0.000_600).quantize(Distance.meters(100)));
        ensureEqual(Location.degrees(0.000_400, 0.000_400).quantize(Distance.meters(100)),
                Location.degrees(0.000_300, 0.000_300).quantize(Distance.meters(100)));
    }

    @Test
    public void testRectangle()
    {
        // Assume that the add methods for Latitude and Longitude are sufficiently tested.
        var location = newRandomValueFactory().newLocation();
        var size = newRandomValueFactory().newSize(Size.MAXIMUM);

        var expectedLatitude = location.latitude().plus(size.height());
        var expectedLongitude = location.longitude().plus(size.width());
        var expectedCorner = new Location(expectedLatitude, expectedLongitude);
        var expectedRectangle = Rectangle.fromLocations(location, expectedCorner);

        ensureEqual(expectedRectangle, location.rectangle(size));
    }

    @Test
    public void testScaleBy()
    {
        // We are going to have to go by the assumption that the unit tests for scale by for the
        // latitude and longitude work.
        var location = newRandomValueFactory().newLocation();
        var latitudeMultiplier = newRandomValueFactory().randomDouble(-10000, 10000);
        var longitudeMultiplier = newRandomValueFactory().randomDouble(-10000, 10000);

        var scaledLatitude = location.latitude().times(latitudeMultiplier);
        var scaledLongitude = location.longitude().times(longitudeMultiplier);
        var expectedLocation = new Location(scaledLatitude, scaledLongitude);

        ensureEqual(expectedLocation, location.scaledBy(latitudeMultiplier, longitudeMultiplier));
    }

    @Test
    public void testSmallAngleApproximation()
    {
        var location = new Location(Latitude.degrees(37.38582), Longitude.degrees(-122.007173));
        var otherLocation = new Location(Latitude.degrees(37.386960), Longitude.degrees(-122.005478));
        var real = location.distanceTo(otherLocation);
        var approx = location.distanceTo(otherLocation);
        var difference = Math.abs(real.asMeters() - approx.asMeters());
        ensureEqual(true, difference / real.asMeters() < 0.01);
    }

    private void testLongConversion6(Location location)
    {
        var before = new Location(Latitude.dm6(location.latitude().asDm6()),
                Longitude.dm6(location.longitude().asDm6()));
        var value = location.asDm6Long();
        ensureEqual(before, Location.dm6(value));
    }

    private void testLongConversion7(Location location)
    {
        var value = location.asDm7Long();
        ensureEqual(location, Location.dm7(value));
    }
}
