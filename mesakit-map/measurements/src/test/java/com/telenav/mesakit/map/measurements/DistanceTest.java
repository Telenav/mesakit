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

import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

public class DistanceTest extends MeasurementsUnitTest
{
    @Test
    public void testAdd()
    {
        var distance1 = random().newDistance();
        var distance2 = random().newDistance();
        var sum = distance1.add(distance2);

        var totalMillimeters = distance1.asMillimeters() + distance2.asMillimeters();

        ensureEqual(totalMillimeters, sum.asMillimeters());
    }

    @Test
    public void testConversion()
    {
        var distance = random().newDistance();
        var feet = distance.asFeet();
        var converted = Distance.feet(feet);

        ensureClose(converted.asMillimeters(), distance.asMillimeters(), 1);
    }

    @Test
    public void testDifference()
    {
        var distance1 = random().newDistance();
        var distance2 = random().newDistance();
        var sum = distance1.difference(distance2);

        var totalMillimeters = Math.abs(distance1.asMillimeters() - distance2.asMillimeters());

        ensureEqual(totalMillimeters, sum.asMillimeters());
    }

    @Test
    public void testEquals()
    {
        var distance = random().newDistance();
        var larger = distance.add(Distance.millimeters(1));
        var smaller = distance.minus(Distance.millimeters(1));

        ensure(distance.equals(distance));
        ensureFalse(smaller.equals(distance));
        ensureFalse(larger.equals(distance));
    }

    @Test
    public void testIsGreaterThan()
    {
        var distance = random().newDistance();
        var larger = distance.add(Distance.millimeters(1));
        var smaller = distance.minus(Distance.millimeters(1));

        ensureFalse(distance.isGreaterThan(distance));
        ensureFalse(smaller.isGreaterThan(distance));
        ensure(larger.isGreaterThan(distance));
    }

    @Test
    public void testIsGreaterThanOrEqualTo()
    {
        var distance = random().newDistance();
        var larger = distance.add(Distance.millimeters(1));
        var smaller = distance.minus(Distance.millimeters(1));

        ensure(distance.isGreaterThanOrEqualTo(distance));
        ensureFalse(smaller.isGreaterThanOrEqualTo(distance));
        ensure(larger.isGreaterThanOrEqualTo(distance));
    }

    @Test
    public void testIsLessThan()
    {
        var distance = random().newDistance();
        var larger = distance.add(Distance.millimeters(1));
        var smaller = distance.minus(Distance.millimeters(1));

        ensureFalse(distance.isLessThan(distance));
        ensure(smaller.isLessThan(distance));
        ensureFalse(larger.isLessThan(distance));
    }

    @Test
    public void testIsLessThanOrEqualTo()
    {
        var distance = random().newDistance();
        var larger = distance.add(Distance.millimeters(1));
        var smaller = distance.minus(Distance.millimeters(1));

        ensure(distance.isLessThanOrEqualTo(distance));
        ensure(smaller.isLessThanOrEqualTo(distance));
        ensureFalse(larger.isLessThan(distance));
    }

    @Test
    public void testRatio()
    {
        var distance1 = random().newDistance();
        var distance2 = random().newDistance(Distance.millimeters(1), Distance.ONE_MILE);

        double ratio = (double) distance1.asMillimeters() / distance2.asMillimeters();

        ensureClose(ratio, distance1.ratio(distance2), 3);
    }

    @Test
    public void testScaleBy()
    {
        var distance = random().newDistance();
        var factor = random().randomDouble(0.1, 2.0);

        var scaled = distance.asMillimeters() * factor;

        ensureEqual((long) scaled, distance.times(factor).asMillimeters());

        var tenMeters = Distance.meters(10);
        ensureClose(tenMeters.times(Percent.of(50)).asMeters(), 5, 2);
    }

    @Test
    public void testSubtract()
    {
        var distance1 = random().newDistance();
        var distance2 = random().newDistance();
        var difference = distance1.minus(distance2);

        var totalMillimeters = distance1.asMillimeters() - distance2.asMillimeters();
        if (totalMillimeters < 0)
        {
            totalMillimeters = 0;
        }

        ensureEqual(totalMillimeters, difference.asMillimeters());
    }
}
