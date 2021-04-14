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

package com.telenav.aonia.map.geography;

import com.telenav.aonia.map.geography.project.MapGeographyUnitTest;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.geography.shape.rectangle.Size;
import com.telenav.aonia.map.measurements.geographic.Angle;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.geographic.Heading;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import org.junit.Test;

@SuppressWarnings("ConstantConditions")
public class LocationTest extends MapGeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void testAdd()
    {
        // Assume that the unit test for Latitude and Longitude provide sufficient coverage for
        // their add methods.
        final var location = randomValueFactory().newLocation();
        final var size = randomValueFactory().newSize(Size.MAXIMUM);

        final var expectedLatitude = location.latitude().plus(size.height());
        final var expectedLongitude = location.longitude().plus(size.width());
        final var expectedLocation = new Location(expectedLatitude, expectedLongitude);

        ensureEqual(expectedLocation, location.offset(size));
    }

    @Test
    public void testDegreesMinutesAndSeconds()
    {
        final var converter = new Location.DegreesMinutesAndSecondsConverter(LOGGER);
        final var location = converter.convert("+47°40'46.92\", -122°20'42.96\"");

        final var decimalConverter = new Location.DegreesConverter(LOGGER);
        final var decimalLocation = decimalConverter.convert("47.679699999,-122.345266666");

        ensureEqual(decimalLocation, location);
    }

    @Test
    public void testDistance()
    {
        for (double latitude = 0; latitude < 90; latitude += 1.0)
        {
            final var north = Location.degrees(latitude, 0.0);
            final var location = north.moved(Heading.EAST, Distance.meters(100));
            ensureBetween(north.equirectangularDistanceToInMillimeters(location), 99_000, 100_000);
            ensureBetween(north.lawOfCosinesDistanceTo(location).asMeters(), 99.9, 100);
            ensureBetween(north.haversineDistanceTo(location).asMeters(), 99.9, 100);
        }

        loop(() ->
        {
            final var from = randomValueFactory().newLocation(Rectangle.MINIMUM.expanded(Distance.kilometers(5_000)));
            final var to = randomValueFactory().newLocation(from.bounds().expanded(Distance.kilometers(1)));
            final var haversine = from.haversineDistanceTo(to);
            final var cosines = from.lawOfCosinesDistanceTo(to);
            final var rectangular = from.equirectangularDistanceTo(to);
            ensure(haversine.difference(cosines).isLessThan(Distance.meters(1)));
            if (haversine.difference(rectangular).percentageOf(haversine).isGreaterThan(Percent.of(3)))
            {
                fail("The distance between " + from + " and " + to + " is " + haversine.asMeters() + " meters by haversine and " + rectangular.asMeters() + " meters by equirectangular");
            }
            if (cosines.difference(rectangular).percentageOf(haversine).isGreaterThan(Percent.of(3)))
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
        try
        {
            // but not for a latitude
            Location.dm7(160_000_000_0, 0);
            fail("Should have thrown");
        }
        catch (final AssertionError ignored)
        {
        }
    }

    @Test
    public void testHeadingTo()
    {
        // Test for a "point" segment
        final var start = new Location(Latitude.degrees(37.0), Longitude.degrees(-122.0));
        final var end = new Location(Latitude.degrees(37.0), Longitude.degrees(-122.0));
        final var heading = start.headingTo(end);
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
            testLongConversion6(randomValueFactory().newLocation());
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
            testLongConversion7(randomValueFactory().newLocation());
        }
    }

    @Test
    public void testPrecision()
    {
        final var locationDm6 = Location.degrees(34.123456, 34.123456);
        final var dm6 = locationDm6.asDm6Long();
        ensure(locationDm6.isClose(Location.dm6(dm6), Angle.degrees(0.000_001)));

        final var locationDm7 = Location.degrees(34.1234567, 34.1234567);
        final var dm7 = locationDm7.asDm7Long();
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
        final var location = randomValueFactory().newLocation();
        final var size = randomValueFactory().newSize(Size.MAXIMUM);

        final var expectedLatitude = location.latitude().plus(size.height());
        final var expectedLongitude = location.longitude().plus(size.width());
        final var expectedCorner = new Location(expectedLatitude, expectedLongitude);
        final var expectedRectangle = Rectangle.fromLocations(location, expectedCorner);

        ensureEqual(expectedRectangle, location.rectangle(size));
    }

    @Test
    public void testScaleBy()
    {
        // We are going to have to go by the assumption that the unit tests for scale by for the
        // latitude and longitude work.
        final var location = randomValueFactory().newLocation();
        final var latitudeMultiplier = randomValueFactory().newDouble(-10000, 10000);
        final var longitudeMultiplier = randomValueFactory().newDouble(-10000, 10000);

        final var scaledLatitude = location.latitude().times(latitudeMultiplier);
        final var scaledLongitude = location.longitude().times(longitudeMultiplier);
        final var expectedLocation = new Location(scaledLatitude, scaledLongitude);

        ensureEqual(expectedLocation, location.scaledBy(latitudeMultiplier, longitudeMultiplier));
    }

    @Test
    public void testSmallAngleApproximation()
    {
        final var location = new Location(Latitude.degrees(37.38582), Longitude.degrees(-122.007173));
        final var otherLocation = new Location(Latitude.degrees(37.386960), Longitude.degrees(-122.005478));
        final var real = location.distanceTo(otherLocation);
        final var approx = location.distanceTo(otherLocation);
        final var difference = Math.abs(real.asMeters() - approx.asMeters());
        ensureEqual(true, difference / real.asMeters() < 0.01);
    }

    private void testLongConversion6(final Location location)
    {
        final var before = new Location(Latitude.dm6(location.latitude().asDm6()),
                Longitude.dm6(location.longitude().asDm6()));
        final var value = location.asDm6Long();
        ensureEqual(before, Location.dm6(value));
    }

    private void testLongConversion7(final Location location)
    {
        final var value = location.asDm7Long();
        ensureEqual(location, Location.dm7(value));
    }
}
