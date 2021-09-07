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

package com.telenav.mesakit.map.measurements;

import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.measurements.project.MeasurementsUnitTest;
import org.junit.Test;

/**
 * Copied from DistanceTest
 *
 * @author matthieun
 */
public class AreaTest extends MeasurementsUnitTest
{
    @Test
    public void testConversion()
    {
        final var area = random().newArea();
        final var feet = area.asSquareFeet();
        final var converted = Area.squareFeet(feet);

        ensureClose(converted.asSquareMeters(), area.asSquareMeters(), 1);
    }

    @Test
    public void testDifference()
    {
        final var area1 = random().newArea();
        final var area2 = random().newArea();
        final var sum = area1.difference(area2);

        final var totalMeters = Math.abs(area1.asSquareMeters() - area2.asSquareMeters());

        ensureEqual(totalMeters, sum.asSquareMeters());
    }

    @Test
    public void testEquals()
    {
        final var area = random().newArea();
        final var larger = area.plus(Area.squareMeters(1));
        final var smaller = area.minus(Area.squareMeters(1));

        ensure(area.equals(area));
        ensureFalse(smaller.equals(area));
        ensureFalse(larger.equals(area));
    }

    @Test
    public void testIsGreaterThan()
    {
        final var area = random().newArea();
        final var larger = area.plus(Area.squareMeters(1));
        final var smaller = area.minus(Area.squareMeters(1));

        ensureFalse(area.isGreaterThan(area));
        ensureFalse(smaller.isGreaterThan(area));
        ensure(larger.isGreaterThan(area));
    }

    @Test
    public void testIsGreaterThanOrEqualTo()
    {
        final var area = random().newArea();
        final var larger = area.plus(Area.squareMeters(1));
        final var smaller = area.minus(Area.squareMeters(1));

        ensure(area.isGreaterThanOrEqualTo(area));
        ensureFalse(smaller.isGreaterThanOrEqualTo(area));
        ensure(larger.isGreaterThanOrEqualTo(area));
    }

    @Test
    public void testIsLessThan()
    {
        final var area = random().newArea();
        final var larger = area.plus(Area.squareMeters(1));
        final var smaller = area.minus(Area.squareMeters(1));

        ensureFalse(area.isLessThan(area));
        ensure(smaller.isLessThan(area));
        ensureFalse(larger.isLessThan(area));
    }

    @Test
    public void testIsLessThanOrEqualTo()
    {
        final var area = random().newArea();
        final var larger = area.plus(Area.squareMeters(1));
        final var smaller = area.minus(Area.squareMeters(1));

        ensure(area.isLessThanOrEqualTo(area));
        ensure(smaller.isLessThanOrEqualTo(area));
        ensureFalse(larger.isLessThan(area));
    }

    @Test
    public void testMinus()
    {
        final var area1 = random().newArea();
        final var area2 = random().newArea();
        final var difference = area1.minus(area2);

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
        final var area1 = random().newArea();
        final var area2 = random().newArea();
        final var sum = area1.plus(area2);

        final var totalSquareMeters = area1.asSquareMeters() + area2.asSquareMeters();

        ensureEqual(totalSquareMeters, sum.asSquareMeters());
    }

    @Test
    public void testRatio()
    {
        final var area1 = random().newArea();
        final var area2 = random().newArea(Area.squareMeters(1), Area.ONE_SQUARE_MILE);

        final var ratio = area1.asSquareMeters() / area2.asSquareMeters();

        ensureClose(ratio, area1.ratio(area2), 3);
    }

    @Test
    public void testTimes()
    {
        final var area = random().newArea();
        final var factor = random().newDouble(0.1, 10);

        final var scaled = area.asSquareMeters() * factor;

        ensureEqual((int) scaled, (int) area.times(factor).asSquareMeters());
    }
}
