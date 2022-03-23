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

import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import org.junit.Test;

public class AngleTest extends MeasurementsUnitTest
{
    @Test
    public void testAdd()
    {
        var angle1 = random().newAngle();
        var angle2 = random().newAngle();

        // Retrieve the nanodegrees and then make sure that it doesn't wrap around 360.
        var totalNanodegrees = angle1.asNanodegrees() + angle2.asNanodegrees();
        var normalizedNanodegrees = totalNanodegrees % 360_000_000_000L;

        ensureEqual(normalizedNanodegrees, angle1.plus(angle2).asNanodegrees());
    }

    @Test
    public void testBetween()
    {
        // Ensure the case where everything is positive.
        var minimum = random().newAngle(Angle.degrees(45), Angle.degrees(90));
        var maximum = random().newAngle(Angle.degrees(180), Angle.degrees(225));
        var isBetween = random().newAngle(minimum, maximum);
        var notBetween = random().newAngle(Angle.degrees(0), Angle.degrees(45));

        ensure(isBetween.isBetween(minimum, maximum, Chirality.CLOCKWISE));
        ensureFalse(notBetween.isBetween(minimum, maximum, Chirality.CLOCKWISE));

        // Ensure the contract conditions where the angle falls on the minimum and maximums.
        ensure(minimum.isBetween(minimum, maximum, Chirality.CLOCKWISE));
        ensure(maximum.isBetween(minimum, maximum, Chirality.CLOCKWISE));

        // Now check the conditions where minimum and maximum span 0.
        minimum = random().newAngle(Angle.degrees(-90), Angle.degrees(-45));
        maximum = random().newAngle(Angle.degrees(45), Angle.degrees(90));
        isBetween = random().newAngle(minimum, maximum);
        notBetween = random().newAngle(Angle.degrees(-180), Angle.degrees(-90));

        ensure(isBetween.isBetween(minimum, maximum, Chirality.CLOCKWISE));
        ensureFalse(notBetween.isBetween(minimum, maximum, Chirality.CLOCKWISE));

        ensure(Angle.degrees(45).isBetween(Angle.degrees(10), Angle.degrees(50), Chirality.CLOCKWISE));
        ensure(Angle.degrees(45).isBetween(Angle.degrees(55), Angle.degrees(50), Chirality.CLOCKWISE));
        ensureFalse(Angle.degrees(45).isBetween(Angle.degrees(50), Angle.degrees(55), Chirality.CLOCKWISE));

        ensure(Angle.degrees(45).isBetween(Angle.degrees(50), Angle.degrees(10), Chirality.COUNTERCLOCKWISE));
        ensure(Angle.degrees(45).isBetween(Angle.degrees(50), Angle.degrees(55), Chirality.COUNTERCLOCKWISE));
        ensureFalse(Angle.degrees(45).isBetween(Angle.degrees(55), Angle.degrees(50), Chirality.COUNTERCLOCKWISE));
    }

    @Test
    public void testDifference()
    {
        var angle1 = random().newAngle();
        var angle2 = random().newAngle();

        var difference = Math.abs(angle1.asNanodegrees() - angle2.asNanodegrees());

        ensureEqual(difference, angle1.absoluteDifference(angle2).asNanodegrees());
        ensureEqual(Angle.degrees(10), Angle.degrees(10).difference(Angle.degrees(20), Chirality.CLOCKWISE));
        ensureEqual(Angle.degrees(10), Angle.degrees(355).difference(Angle.degrees(5), Chirality.CLOCKWISE));
        ensureEqual(Angle.degrees(10), Angle.degrees(20).difference(Angle.degrees(10), Chirality.COUNTERCLOCKWISE));
        ensureEqual(Angle.degrees(10), Angle.degrees(5).difference(Angle.degrees(355), Chirality.COUNTERCLOCKWISE));
        ensureEqual(Angle.degrees(10), Angle.degrees(5).difference(Angle.degrees(15), Chirality.SMALLEST));
        ensureEqual(Angle.degrees(10), Angle.degrees(355).difference(Angle.degrees(5), Chirality.SMALLEST));
    }

    @Test
    public void testGreaterThan()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensure(bigAngle.isGreaterThan(smallAngle));
        ensureFalse(smallAngle.isGreaterThan(bigAngle));
        ensureFalse(bigAngle.isGreaterThan(bigAngle));
    }

    @Test
    public void testGreaterThanOrEqualTo()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensure(bigAngle.isGreaterThanOrEqualTo(smallAngle));
        ensureFalse(smallAngle.isGreaterThanOrEqualTo(bigAngle));
        ensure(bigAngle.isGreaterThanOrEqualTo(bigAngle));
    }

    @Test
    public void testIsClose()
    {
        ensure(Angle.degrees(45).isClose(Angle.degrees(50), Angle.degrees(15)));
        ensureFalse(Angle.degrees(45).isClose(Angle.degrees(50), Angle.degrees(1)));
        ensure(Angle.degrees(355).isClose(Angle.degrees(5), Angle.degrees(15)));
        ensureFalse(Angle.degrees(355).isClose(Angle.degrees(5), Angle.degrees(1)));
    }

    @Test
    public void testLessThan()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensure(smallAngle.isLessThan(bigAngle));
        ensureFalse(bigAngle.isLessThan(smallAngle));
        ensureFalse(bigAngle.isLessThan(bigAngle));
    }

    @Test
    public void testLessThanOrEqualTo()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensure(smallAngle.isLessThanOrEqualTo(bigAngle));
        ensureFalse(bigAngle.isLessThanOrEqualTo(smallAngle));
        ensure(bigAngle.isLessThanOrEqualTo(bigAngle));
    }

    @Test
    public void testMaximum()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensureEqual(bigAngle, smallAngle.maximum(bigAngle));
        ensureEqual(bigAngle, bigAngle.maximum(smallAngle));
        ensureEqual(bigAngle, bigAngle.maximum(bigAngle));
    }

    @Test
    public void testMinimum()
    {
        var smallAngle = random().newAngle(Angle.degrees(-180), Angle.degrees(180));
        var bigAngle = random().newAngle(Angle.degrees(180), Angle.degrees(360));

        ensureEqual(smallAngle, smallAngle.minimum(bigAngle));
        ensureEqual(smallAngle, bigAngle.minimum(smallAngle));
        ensureEqual(smallAngle, smallAngle.minimum(smallAngle));
    }

    @Test
    public void testScaleBy()
    {
        var angle = random().newAngle();
        var multiplier = random().randomDouble(0, 10000);

        // Test the standard case.
        var expected = (angle.asNanodegrees() * multiplier) % 360_000_000_000L;
        ensureEqual(angle.times(multiplier).asNanodegrees(), (long) expected);
    }

    @Test
    public void testSubtract()
    {
        var angle1 = random().newAngle();
        var angle2 = random().newAngle();

        // Retrieve the nanodegrees and then make sure that it doesn't wrap around 360.
        var nanodegreesDifference = angle1.asNanodegrees() - angle2.asNanodegrees();
        var normalizedNanodegrees = nanodegreesDifference % 360_000_000_000L;

        ensureEqual(normalizedNanodegrees, angle1.minus(angle2).asNanodegrees());

        // Verify validity of negative angle
        var angle3 = Angle.degrees(10.0).minus(Angle.degrees(20.0));
        ensureWithin(-10.0, angle3.asDegrees(), 0.0);
    }
}
