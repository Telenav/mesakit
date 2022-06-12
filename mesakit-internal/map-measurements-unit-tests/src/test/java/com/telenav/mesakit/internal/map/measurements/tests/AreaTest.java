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

package com.telenav.mesakit.internal.map.measurements.tests;

import com.telenav.mesakit.map.measurements.testing.MeasurementsUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Area;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Copied from DistanceTest
 *
 * @author matthieun
 */
public class AreaTest extends MeasurementsUnitTest
{
    @Ignore
    public void testConversion()
    {
        var area = random().newArea();
        var feet = area.asSquareFeet();
        var converted = Area.squareFeet(feet);

        ensureClose(converted.asSquareMeters(), area.asSquareMeters(), 1);
    }

    @Test
    public void testDifference()
    {
        var area1 = random().newArea();
        var area2 = random().newArea();
        var sum = area1.difference(area2);

        var totalMeters = Math.abs(area1.asSquareMeters() - area2.asSquareMeters());

        ensureEqual(totalMeters, sum.asSquareMeters());
    }

    @Test
    public void testEquals()
    {
        var area = random().newArea();
        var larger = area.plus(Area.squareMeters(1));
        var smaller = area.minus(Area.squareMeters(1));

        ensure(area.equals(area));
        ensureFalse(smaller.equals(area));
        ensureFalse(larger.equals(area));
    }

    @Test
    public void testIsGreaterThan()
    {
        var area = random().newArea();
        var larger = area.plus(Area.squareMeters(1));
        var smaller = area.minus(Area.squareMeters(1));

        ensureFalse(area.isGreaterThan(area));
        ensureFalse(smaller.isGreaterThan(area));
        ensure(larger.isGreaterThan(area));
    }

    @Test
    public void testIsGreaterThanOrEqualTo()
    {
        var area = random().newArea();
        var larger = area.plus(Area.squareMeters(1));
        var smaller = area.minus(Area.squareMeters(1));

        ensure(area.isGreaterThanOrEqualTo(area));
        ensureFalse(smaller.isGreaterThanOrEqualTo(area));
        ensure(larger.isGreaterThanOrEqualTo(area));
    }

    @Test
    public void testIsLessThan()
    {
        var area = random().newArea();
        var larger = area.plus(Area.squareMeters(1));
        var smaller = area.minus(Area.squareMeters(1));

        ensureFalse(area.isLessThan(area));
        ensure(smaller.isLessThan(area));
        ensureFalse(larger.isLessThan(area));
    }

    @Test
    public void testIsLessThanOrEqualTo()
    {
        var area = random().newArea();
        var larger = area.plus(Area.squareMeters(1));
        var smaller = area.minus(Area.squareMeters(1));

        ensure(area.isLessThanOrEqualTo(area));
        ensure(smaller.isLessThanOrEqualTo(area));
        ensureFalse(larger.isLessThan(area));
    }

    @Test
    public void testMinus()
    {
        var area1 = random().newArea();
        var area2 = random().newArea();
        var difference = area1.minus(area2);

        var totalSquareMeters = area1.asSquareMeters() - area2.asSquareMeters();
        if (totalSquareMeters < 0)
        {
            totalSquareMeters = 0;
        }

        ensureEqual(totalSquareMeters, difference.asSquareMeters());
    }

    @Test
    public void testPlus()
    {
        var area1 = random().newArea();
        var area2 = random().newArea();
        var sum = area1.plus(area2);

        var totalSquareMeters = area1.asSquareMeters() + area2.asSquareMeters();

        ensureEqual(totalSquareMeters, sum.asSquareMeters());
    }

    @Test
    public void testRatio()
    {
        var area1 = random().newArea();
        var area2 = random().newArea(Area.squareMeters(1), Area.ONE_SQUARE_MILE);

        var ratio = area1.asSquareMeters() / area2.asSquareMeters();

        ensureClose(ratio, area1.ratio(area2), 3);
    }

    @Test
    public void testTimes()
    {
        var area = random().newArea();
        var factor = random().randomDouble(0.1, 10);

        var scaled = area.asSquareMeters() * factor;

        ensureEqual((int) scaled, (int) area.times(factor).asSquareMeters());
    }
}
