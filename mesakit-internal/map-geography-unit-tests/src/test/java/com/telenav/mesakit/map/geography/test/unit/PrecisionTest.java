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

package com.telenav.mesakit.map.geography.test.unit;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.test.GeographyUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import org.junit.Test;

import static com.telenav.mesakit.map.geography.Precision.DM5;
import static com.telenav.mesakit.map.geography.Precision.DM6;
import static com.telenav.mesakit.map.geography.Precision.DM7;

public class PrecisionTest extends GeographyUnitTest
{
    @Test
    public void testAngleToDecimal()
    {
        ensureEqual(45_000_00, DM5.toDecimal(Angle.degrees(45.0)));
        ensureEqual(45_000_000, DM6.toDecimal(Angle.degrees(45.0)));
        ensureEqual(45_000_000_0, DM7.toDecimal(Angle.degrees(45.0)));
    }

    @Test
    public void testDecimalLongToLocation()
    {
        ensureEqual(Location.degrees(45, 45), DM5.toLocation(Location.toLong(45_000_00, 45_000_00)));
        ensureEqual(Location.degrees(45, 45), DM6.toLocation(Location.toLong(45_000_000, 45_000_000)));
        ensureEqual(Location.degrees(45, 45), DM7.toLocation(Location.toLong(45_000_000_0, 45_000_000_0)));
    }

    @Test
    public void testDecimalPlaces()
    {
        ensureEqual(5, DM5.places());
        ensureEqual(6, DM6.places());
        ensureEqual(7, DM7.places());
    }

    @Test
    public void testDecimalToDm5()
    {
        ensureEqual(45_000_00, DM5.toDm5(45_000_00));
        ensureEqual(45_000_00, DM6.toDm5(45_000_000));
        ensureEqual(45_000_00, DM7.toDm5(45_000_000_0));
    }

    @Test
    public void testDecimalToDm6()
    {
        ensureEqual(45_000_000, DM5.toDm6(45_000_00));
        ensureEqual(45_000_000, DM6.toDm6(45_000_000));
        ensureEqual(45_000_000, DM7.toDm6(45_000_000_0));
    }

    @Test
    public void testDecimalToDm7()
    {
        ensureEqual(45_000_000_0, DM5.toDm7(45_000_00));
        ensureEqual(45_000_000_0, DM6.toDm7(45_000_000));
        ensureEqual(45_000_000_0, DM7.toDm7(45_000_000_0));
    }

    @Test
    public void testDecimalToLatitude()
    {
        ensureEqual(Latitude.degrees(45), DM5.toLatitude(45_000_00));
        ensureEqual(Latitude.degrees(45), DM6.toLatitude(45_000_000));
        ensureEqual(Latitude.degrees(45), DM7.toLatitude(45_000_000_0));
    }

    @Test
    public void testDecimalToLongitude()
    {
        ensureEqual(Longitude.degrees(45), DM5.toLongitude(45_000_00));
        ensureEqual(Longitude.degrees(45), DM6.toLongitude(45_000_000));
        ensureEqual(Longitude.degrees(45), DM7.toLongitude(45_000_000_0));
    }

    @Test
    public void testDecimalToNanodegrees()
    {
        ensureEqual(45_000_000_000L, DM5.toNanodegrees(45_000_00));
        ensureEqual(45_000_000_000L, DM6.toNanodegrees(45_000_000));
        ensureEqual(45_000_000_000L, DM7.toNanodegrees(45_000_000_0));
    }

    @Test
    public void testHasCorrectPlaces()
    {
        ensure(DM6.hasCorrectLatitudePlaces(84_000_000));
        ensure(DM6.hasCorrectLatitudePlaces(8_000_000));
        ensure(DM6.hasCorrectLatitudePlaces(1_000_000));

        ensureFalse(DM6.hasCorrectLatitudePlaces(84_000_000_0));
        ensureFalse(DM6.hasCorrectLatitudePlaces(8_000_00));
        ensureFalse(DM6.hasCorrectLatitudePlaces(1_000_0));

        ensure(DM7.hasCorrectLongitudePlaces(150_000_000_0));
        ensure(DM7.hasCorrectLongitudePlaces(8_000_000_0));
        ensure(DM7.hasCorrectLongitudePlaces(1_000_000_0));

        ensureFalse(DM7.hasCorrectLongitudePlaces(15_000_0));
        ensureFalse(DM7.hasCorrectLongitudePlaces(8_000_000));
        ensureFalse(DM7.hasCorrectLongitudePlaces(1_000_00));
    }

    @Test
    public void testNanodegreesToDecimal()
    {
        ensureEqual(45_000_00, DM5.nanodegreesToDecimal(45_000_000_000L));
        ensureEqual(45_000_000, DM6.nanodegreesToDecimal(45_000_000_000L));
        ensureEqual(45_000_000_0, DM7.nanodegreesToDecimal(45_000_000_000L));
    }

    @Test
    public void testOffsetNanodegrees()
    {
        ensureEqual(45_000_010_000L, DM5.offsetNanodegrees(45_000_000_000L, 1));
        ensureEqual(45_000_001_000L, DM6.offsetNanodegrees(45_000_000_000L, 1));
        ensureEqual(45_000_000_100L, DM7.offsetNanodegrees(45_000_000_000L, 1));
    }

    @Test
    public void testToDecimalLong()
    {
        ensureEqual((45_000_00L << 32) | 45_000_00L, DM5.toLong(Location.degrees(45, 45)));
        ensureEqual((45_000_000L << 32) | 45_000_000L, DM6.toLong(Location.degrees(45, 45)));
        ensureEqual((45_000_000_0L << 32) | 45_000_000_0L, DM7.toLong(Location.degrees(45, 45)));
    }
}
