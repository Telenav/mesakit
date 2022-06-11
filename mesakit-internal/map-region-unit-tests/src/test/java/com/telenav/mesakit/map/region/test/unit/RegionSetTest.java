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

package com.telenav.mesakit.map.region.test.unit;

import com.telenav.mesakit.map.region.RegionSet;
import com.telenav.mesakit.map.region.testing.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import org.junit.Test;

public class RegionSetTest extends RegionUnitTest
{
    @Test
    public void test()
    {
        var regions = new RegionSet();
        regions.addAll(Continent.NORTH_AMERICA.children());
        regions.addAll(Continent.EUROPE.children());
        ensure(regions.contains(Country.RUSSIA));
        regions.remove(Country.RUSSIA);
        ensureFalse(regions.contains(Country.RUSSIA));
    }
}
