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

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

import java.util.stream.Collectors;

public class StateTest extends RegionUnitTest
{
    @Test
    public void testAll()
    {
        var mexicoStates = State.all().stream().filter(state -> state.identity().iso().code().startsWith("MX"))
                .collect(Collectors.toList());
        ensureEqual(32, mexicoStates.size());
        var mexicoAG = mexicoStates.stream().filter(state -> "MX-AG".equals(state.identity().iso().code()))
                .collect(Collectors.toList());
        ensureEqual(1, mexicoAG.size());
        ensureEqual("MX-AG", mexicoAG.get(0).identity().iso().code());
    }

    @Test
    public void testAttributes()
    {
        ensureEqual("New Mexico", Country.UNITED_STATES.NEW_MEXICO.name());
        ensureEqual("United_States-New_Mexico", Country.UNITED_STATES.NEW_MEXICO.identity().mesakit().code());
        ensureEqual("US-NM", Country.UNITED_STATES.NEW_MEXICO.identity().iso().code());
    }

    @Test
    public void testForLocation()
    {
        ensureEqual(Country.UNITED_STATES.WASHINGTON,
                State.forLocation(Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.center()));
        ensureEqual(Country.UNITED_STATES.CALIFORNIA, State.forLocation(Location.TELENAV_HEADQUARTERS));
    }

    @Test
    public void testForString()
    {
        var state = State.forRegionCode(RegionCode.parse("MX-BC")).identity().state();
        ensureEqual("Mexico-Baja_California", state.identity().mesakit().code());
        ensureEqual("MX-BC", state.identity().iso().code());
        ensureEqual(Country.MEXICO.BAJA_CALIFORNIA, State.forRegionCode(code("MX-BC")).identity().state());
    }

    @Test
    public void testIsoCodeLettersAndNumbers()
    {
        var state = State.forLocation(Location.degrees(12.1758692, -68.2484687));
        ensureEqual("NL-BQ1", state.identity().iso().code());
    }

    @Test
    public void testIsoCodeLettersOnly()
    {
        var state = State.forLocation(Location.degrees(8.3453033, 80.3820343));
        ensureEqual("LK-71", state.identity().iso().code());
    }

    @Test
    public void testMetros()
    {
        Region<?> sj = MetropolitanArea.forRegionCode(code("US-CA-METROSANJOSE")).identity()
                .metropolitanArea();
        ensureNotNull(sj);
        Region<?> seattle = City.forRegionCode(code("US-WA-CITYSEATTLE")).identity().city();
        ensureNotNull(seattle);
        var count = 0;
        for (City ignored : Country.UNITED_STATES.WASHINGTON.cities())
        {
            count++;
        }
        ensureEqual(1, count);
        count = 0;
        for (MetropolitanArea ignored : MetropolitanArea.all())
        {
            count++;
        }
        ensure(count > 50);
    }

    @Test
    public void testMexicoAG()
    {
        var state = State.forLocation(Location.degrees(21.896758, -102.283537));
        ensureEqual("MX-AG", state.identity().iso().code());
        ensureEqual("Mexico-Aguascalientes", state.identity().mesakit().code());
        ensureEqual("Aguascalientes", state.name());
    }

    @Test
    public void testSpecialCharacters()
    {
        var state = State.forLocation(Location.degrees(39.0638841, 125.8376773));
        ensureEqual("North_Korea-P_yŏngyang", state.identity().mesakit().code());
        ensureEqual("KP-PYNGYANG", state.identity().iso().code());
        ensureEqual("P'yŏngyang", state.name());
    }

    @Test
    public void testWithCorrectIsoCode()
    {
        var state = State.forLocation(Location.degrees(64.202173, -137.891684));
        ensureEqual("CA-YT", state.identity().iso().code());
        ensureEqual("Canada-Yukon", state.identity().mesakit().code());
    }

    @Test
    public void testWithIsoCodeAsCodeDashNumber()
    {
        var state = State.forLocation(Location.degrees(7.973467, 79.735602));
        ensureEqual("LK-62", state.identity().iso().code());
    }

    @Test
    public void testWithIsoCodeNothingAfterDash()
    {
        var state = State.forLocation(Location.degrees(8.6992333, 106.5968633));
        ensureEqual("VN-SCTRNG", state.identity().iso().code());
    }

    @Test
    // Spratly Islands -99 code in boundary
    public void testWithIsoCodeOnlyANumber()
    {
        var state = State.forLocation(Location.degrees(9.6836461, 114.3740521));
        ensureNull(state);
    }

    @Test
    public void testYetAnotherCode()
    {
        var state = State.forLocation(Location.degrees(40.281435, 49.125561));
        ensureEqual("AZ-ABERON", state.identity().iso().code());
        ensureEqual("Azerbaijan-Abşeron", state.identity().mesakit().code());
        ensureEqual("Abşeron", state.name());
    }
}
