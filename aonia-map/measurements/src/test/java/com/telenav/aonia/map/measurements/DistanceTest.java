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

package com.telenav.aonia.map.measurements;

import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.project.MapMeasurementsUnitTest;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import org.junit.Test;

public class DistanceTest extends MapMeasurementsUnitTest
{
    @Test
    public void testAdd()
    {
        final var distance1 = random().newDistance();
        final var distance2 = random().newDistance();
        final var sum = distance1.add(distance2);

        final var totalMillimeters = distance1.asMillimeters() + distance2.asMillimeters();

        ensureEqual(totalMillimeters, sum.asMillimeters());
    }

    @Test
    public void testConversion()
    {
        final var distance = random().newDistance();
        final var feet = distance.asFeet();
        final var converted = Distance.feet(feet);

        ensureClose(converted.asMillimeters(), distance.asMillimeters(), 1);
    }

    @Test
    public void testDifference()
    {
        final var distance1 = random().newDistance();
        final var distance2 = random().newDistance();
        final var sum = distance1.difference(distance2);

        final var totalMillimeters = Math.abs(distance1.asMillimeters() - distance2.asMillimeters());

        ensureEqual(totalMillimeters, sum.asMillimeters());
    }

    @Test
    public void testEquals()
    {
        final var distance = random().newDistance();
        final var larger = distance.add(Distance.millimeters(1));
        final var smaller = distance.minus(Distance.millimeters(1));

        //noinspection EqualsWithItself
        ensure(distance.equals(distance));
        ensureFalse(smaller.equals(distance));
        ensureFalse(larger.equals(distance));
    }

    @Test
    public void testIsGreaterThan()
    {
        final var distance = random().newDistance();
        final var larger = distance.add(Distance.millimeters(1));
        final var smaller = distance.minus(Distance.millimeters(1));

        ensureFalse(distance.isGreaterThan(distance));
        ensureFalse(smaller.isGreaterThan(distance));
        ensure(larger.isGreaterThan(distance));
    }

    @Test
    public void testIsGreaterThanOrEqualTo()
    {
        final var distance = random().newDistance();
        final var larger = distance.add(Distance.millimeters(1));
        final var smaller = distance.minus(Distance.millimeters(1));

        ensure(distance.isGreaterThanOrEqualTo(distance));
        ensureFalse(smaller.isGreaterThanOrEqualTo(distance));
        ensure(larger.isGreaterThanOrEqualTo(distance));
    }

    @Test
    public void testIsLessThan()
    {
        final var distance = random().newDistance();
        final var larger = distance.add(Distance.millimeters(1));
        final var smaller = distance.minus(Distance.millimeters(1));

        ensureFalse(distance.isLessThan(distance));
        ensure(smaller.isLessThan(distance));
        ensureFalse(larger.isLessThan(distance));
    }

    @Test
    public void testIsLessThanOrEqualTo()
    {
        final var distance = random().newDistance();
        final var larger = distance.add(Distance.millimeters(1));
        final var smaller = distance.minus(Distance.millimeters(1));

        ensure(distance.isLessThanOrEqualTo(distance));
        ensure(smaller.isLessThanOrEqualTo(distance));
        ensureFalse(larger.isLessThan(distance));
    }

    @Test
    public void testRatio()
    {
        final var distance1 = random().newDistance();
        final var distance2 = random().newDistance(Distance.millimeters(1), Distance.ONE_MILE);

        final double ratio = (double) distance1.asMillimeters() / distance2.asMillimeters();

        ensureClose(ratio, distance1.ratio(distance2), 3);
    }

    @Test
    public void testScaleBy()
    {
        final var distance = random().newDistance();
        final var factor = random().newDouble(0.1, 2.0);

        final var scaled = distance.asMillimeters() * factor;

        ensureEqual((long) scaled, distance.times(factor).asMillimeters());

        final var tenMeters = Distance.meters(10);
        ensureClose(tenMeters.times(Percent.of(50)).asMeters(), 5, 2);
    }

    @Test
    public void testSubtract()
    {
        final var distance1 = random().newDistance();
        final var distance2 = random().newDistance();
        final var difference = distance1.minus(distance2);

        var totalMillimeters = distance1.asMillimeters() - distance2.asMillimeters();
        if (totalMillimeters < 0)
        {
            totalMillimeters = 0;
        }

        ensureEqual(totalMillimeters, difference.asMillimeters());
    }
}
