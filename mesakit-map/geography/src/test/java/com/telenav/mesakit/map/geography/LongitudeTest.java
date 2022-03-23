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

public class LongitudeTest extends GeographyUnitTest
{
    @Test
    public void testAdd()
    {
        var longitude1 = newRandomValueFactory().newLongitude();
        var longitude2 = newRandomValueFactory().newLongitude();

        // Calculate the expected value capping where needed
        var expected = longitude1.asMicrodegrees() + longitude2.asMicrodegrees();
        if (expected < Longitude.MINIMUM.asMicrodegrees())
        {
            expected = Longitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Longitude.MAXIMUM.asMicrodegrees())
        {
            expected = Longitude.MAXIMUM.asMicrodegrees();
        }
        ensureEqual(expected, longitude1.plus(longitude2).asMicrodegrees());
    }

    @Test
    public void testMaximum()
    {
        var smallLongitude = newRandomValueFactory().newLongitude(Longitude.degrees(-180), Longitude.degrees(0));
        var bigLongitude = newRandomValueFactory().newLongitude(Longitude.degrees(0), Longitude.degrees(180));

        ensureEqual(bigLongitude, smallLongitude.maximum(bigLongitude));
        ensureEqual(bigLongitude, bigLongitude.maximum(smallLongitude));
        ensureEqual(bigLongitude, bigLongitude.maximum(bigLongitude));
    }

    @Test
    public void testMinimum()
    {
        var smallLongitude = newRandomValueFactory().newLongitude(Longitude.degrees(-180), Longitude.degrees(0));
        var bigLongitude = newRandomValueFactory().newLongitude(Longitude.degrees(0), Longitude.degrees(180));

        ensureEqual(smallLongitude, smallLongitude.minimum(bigLongitude));
        ensureEqual(smallLongitude, bigLongitude.minimum(smallLongitude));
        ensureEqual(smallLongitude, smallLongitude.minimum(smallLongitude));
    }

    @Test
    public void testPrecision()
    {
        ensure(
                Longitude.degrees(34.123456).isClose(Precision.DM6.toLongitude(34_123_456), Angle.degrees(0.0000001)));
        ensureEqual(Longitude.degrees(34.1234567), Precision.DM7.toLongitude(34_123_456_7));
    }

    @Test
    public void testScaleBy()
    {
        var longitude = newRandomValueFactory().newLongitude();
        var multiplier = newRandomValueFactory().randomDouble(-10000, 10000);

        // Calculate the expected value
        var expected = longitude.asMicrodegrees() * multiplier;

        // Cap the values where needed.
        if (expected < Longitude.MINIMUM.asMicrodegrees())
        {
            expected = Longitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Longitude.MAXIMUM.asMicrodegrees())
        {
            expected = Longitude.MAXIMUM.asMicrodegrees();
        }
        ensureEqual((int) expected, longitude.times(multiplier).asMicrodegrees());
    }

    @Test
    public void testSubtract()
    {
        var longitude1 = newRandomValueFactory().newLongitude();
        var longitude2 = newRandomValueFactory().newLongitude();

        // Calculate the expected value capping where needed.
        var expected = longitude1.asMicrodegrees() - longitude2.asMicrodegrees();
        if (expected < Longitude.MINIMUM.asMicrodegrees())
        {
            expected = Longitude.MINIMUM.asMicrodegrees();
        }
        else if (expected > Longitude.MAXIMUM.asMicrodegrees())
        {
            expected = Longitude.MAXIMUM.asMicrodegrees();
        }
        ensureEqual(expected, longitude1.minus(longitude2).asMicrodegrees());
    }
}
