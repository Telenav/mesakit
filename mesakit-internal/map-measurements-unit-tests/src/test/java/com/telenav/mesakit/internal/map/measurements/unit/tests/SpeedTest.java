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

package com.telenav.mesakit.internal.map.measurements.unit.tests;

import com.telenav.mesakit.map.measurements.testing.MeasurementsUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.motion.Speed;
import org.junit.Test;

public class SpeedTest extends MeasurementsUnitTest
{
    @Test
    public void test()
    {
        var miles = Distance.miles(5);
        ensureWithin(5, miles.asMiles(), 0.001);

        var speed = Speed.milesPerHour(5);
        ensureWithin(5, speed.asMilesPerHour(), 0.001);
    }
}
