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

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.region.project.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

public class CountyTest extends RegionUnitTest
{
    @Test
    public void testAttributes()
    {
        ensureEqual("San Mateo", Country.UNITED_STATES.CALIFORNIA.SAN_MATEO_COUNTY.name());
        ensureEqual("United_States-California-County_San_Mateo",
                Country.UNITED_STATES.CALIFORNIA.SAN_MATEO_COUNTY.identity().mesakit().code());
    }

    @Test
    public void testCountySameAsMetro()
    {
        final County county = County.forLocation(Location.degrees(39.5615881, -77.7744467));
        ensureEqual("US-MD-COUNTYWASHINGTON", county.identity().iso().code());
        ensureEqual("United_States-Maryland-County_Washington", county.identity().mesakit().code());
        ensureEqual("Washington", county.name());

        var metro = MetropolitanArea.forLocation(Location.degrees(38.9083212, -77.0395367));
        ensureEqual("US-MD-METROWASHINGTON", metro.identity().iso().code());
        ensureEqual("United_States-Maryland-Metro_Washington", metro.identity().mesakit().code());
    }

    @Test
    public void testDC()
    {
        var location = new Location(Latitude.degrees(38.910861), Longitude.degrees(-77.039704));
        var state = State.forLocation(location);
        ensureEqual(Country.UNITED_STATES.DISTRICT_OF_COLUMBIA, state);
        ensureEqual("United_States-District_of_Columbia", state.identity().mesakit().code());
    }

    @Test
    public void testForLocation()
    {
        ensureEqual(Country.UNITED_STATES.CALIFORNIA.SANTA_CLARA_COUNTY,
                County.forLocation(Country.UNITED_STATES.CALIFORNIA.MOUNTAIN_VIEW.center()));
    }

    @Test
    public void testHierarchy()
    {
        ensureEqual(Country.UNITED_STATES.CALIFORNIA, Country.UNITED_STATES.CALIFORNIA.SAN_MATEO_COUNTY.state());
        ensureEqual(Country.UNITED_STATES.CALIFORNIA, Country.UNITED_STATES.CALIFORNIA.SANTA_CLARA_COUNTY.state());
        ensure(Country.UNITED_STATES.CALIFORNIA.counties().contains(Country.UNITED_STATES.CALIFORNIA.SAN_MATEO_COUNTY));
        ensure(Country.UNITED_STATES.CALIFORNIA.counties().contains(Country.UNITED_STATES.CALIFORNIA.SANTA_CLARA_COUNTY));
    }

    @Test
    public void testSpecialCharacters()
    {
        var location = Location.degrees(18.3616778, -66.1814385);
        final County county = County.forLocation(location);
        ensureEqual("US-PR-COUNTYBAYAMN", county.identity().iso().code());
        ensureEqual("United_States-Puerto_Rico-County_Bayamón", county.identity().mesakit().code());
    }
}
