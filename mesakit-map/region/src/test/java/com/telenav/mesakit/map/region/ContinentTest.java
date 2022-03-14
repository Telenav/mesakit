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

package com.telenav.mesakit.map.region;

import com.telenav.kivakit.core.test.SlowTest;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ SlowTest.class })
public class ContinentTest extends RegionUnitTest
{
    @Test
    public void test()
    {
        var count = 0;
        for (var ignored : Continent.all())
        {
            count++;
        }
        ensureEqual(7, count);
    }

    @Test
    public void testAttributes()
    {
        ensureEqual(Continent.NORTH_AMERICA, Country.UNITED_STATES.WASHINGTON.country().continent());
        ensureEqual("North America", Continent.NORTH_AMERICA.name());
        ensureEqual("NA", Continent.NORTH_AMERICA.identity().iso().code());
    }

    @Test
    public void testForLocation()
    {
        ensureEqual(Continent.NORTH_AMERICA,
                Continent.forLocation(Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.center()));
        ensureEqual(Continent.NORTH_AMERICA, Continent.forLocation(Country.UNITED_STATES.TEXAS.center()));
        ensureEqual(Continent.EUROPE, Continent.forLocation(Country.ROMANIA.center()));
        ensureEqual(Continent.NORTH_AMERICA,
                Continent.forLocation(Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.center()));
        ensureEqual(Continent.EUROPE, Continent.forLocation(Country.BELGIUM.center()));
        ensureEqual(Continent.AFRICA, Continent.forLocation(Country.KENYA.center()));
        ensureEqual(Continent.OCEANIA, Continent.forLocation(Country.AUSTRALIA.center()));
        ensureEqual(Continent.SOUTH_AMERICA, Continent.forLocation(Country.VENEZUELA.center()));
    }
}
