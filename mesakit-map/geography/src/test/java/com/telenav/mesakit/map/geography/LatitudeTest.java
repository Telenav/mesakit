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

import com.telenav.mesakit.map.measurements.geographic.Angle;
import org.junit.Test;

public class LatitudeTest extends GeographyUnitTest
{
    @Test
    public void testAdd()
    {
        var latitude1 = randomValueFactory().newLatitude();
        var latitude2 = randomValueFactory().newLatitude();

        // Calculate the expected value capping the poles.
        var expected = latitude1.asMicrodegrees() + latitude2.asMicrodegrees();
        if (expected < Latitude.MINIMUM.asMicrodegrees())
        {
            expected = Latitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Latitude.MAXIMUM.asMicrodegrees())
        {
            expected = Latitude.MAXIMUM.asMicrodegrees();
        }

        ensureEqual(expected, latitude1.plus(latitude2).asMicrodegrees());
    }

    @Test
    public void testMaximum()
    {
        var smallLatitude = randomValueFactory().newLatitude(Latitude.degrees(Latitude.MINIMUM_DEGREES), Latitude.degrees(0));
        var bigLatitude = randomValueFactory().newLatitude(Latitude.degrees(0), Latitude.degrees(Latitude.MAXIMUM_DEGREES));

        ensureEqual(bigLatitude, smallLatitude.maximum(bigLatitude));
        ensureEqual(bigLatitude, bigLatitude.maximum(smallLatitude));
        ensureEqual(bigLatitude, bigLatitude.maximum(bigLatitude));
    }

    @Test
    public void testMinimum()
    {
        var smallLatitude = randomValueFactory().newLatitude(Latitude.degrees(Latitude.MINIMUM_DEGREES), Latitude.degrees(0));
        var bigLatitude = randomValueFactory().newLatitude(Latitude.degrees(0), Latitude.degrees(Latitude.MAXIMUM_DEGREES));

        ensureEqual(smallLatitude, smallLatitude.minimum(bigLatitude));
        ensureEqual(smallLatitude, bigLatitude.minimum(smallLatitude));
        ensureEqual(smallLatitude, smallLatitude.minimum(smallLatitude));
    }

    @Test
    public void testPrecision()
    {
        ensure(Latitude.degrees(34.123456).isClose(Precision.DM6.toLatitude(34_123_456), Angle.degrees(0.0000001)));
        ensureEqual(Latitude.degrees(34.1234567), Precision.DM7.toLatitude(34_123_456_7));
    }

    @Test
    public void testScaleBy()
    {
        var latitude = randomValueFactory().newLatitude();
        var multiplier = randomValueFactory().newDouble(-10000, 10000);

        // Calculate the expected value capping the poles.
        var expected = latitude.asMicrodegrees() * multiplier;
        if (expected < Latitude.MINIMUM.asMicrodegrees())
        {
            expected = Latitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Latitude.MAXIMUM.asMicrodegrees())
        {
            expected = Latitude.MAXIMUM.asMicrodegrees();
        }
        ensureEqual((int) expected, latitude.times(multiplier).asMicrodegrees());
    }

    @Test
    public void testSubtract()
    {
        var latitude1 = randomValueFactory().newLatitude();
        var latitude2 = randomValueFactory().newLatitude();

        // Calculate the expected value capping the poles.
        var expected = latitude1.asMicrodegrees() - latitude2.asMicrodegrees();
        if (expected < Latitude.MINIMUM.asMicrodegrees())
        {
            expected = Latitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Latitude.MAXIMUM.asMicrodegrees())
        {
            expected = Latitude.MAXIMUM.asMicrodegrees();
        }

        ensureEqual(expected, latitude1.minus(latitude2).asMicrodegrees());
    }
}
