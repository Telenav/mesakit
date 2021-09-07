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
import com.telenav.mesakit.map.region.project.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

import java.util.stream.Collectors;

public class MetropolitanAreaTest extends RegionUnitTest
{
    @Test
    public void testAll()
    {
        final var metropolitanAreas = MetropolitanArea.all()
                .stream()
                .filter(state -> state.identity().iso().code().startsWith("MX"))
                .collect(Collectors.toList());

        ensureEqual(25, metropolitanAreas.size());

        final var mexicoAG = metropolitanAreas
                .stream()
                .filter(metro ->
                {
                    final String code = metro.identity().iso().code();
                    return "MX-AG-METROZONAMETROPOLITANADEAGUASCALIENTES".equals(code);
                })
                .collect(Collectors.toList());

        ensureEqual(1, mexicoAG.size());
        ensureEqual("MX-AG-METROZONAMETROPOLITANADEAGUASCALIENTES", mexicoAG.get(0).identity().iso().code());
    }

    @Test
    public void testArizona()
    {
        final var areas = Country.UNITED_STATES.ARIZONA.metropolitanAreas();
        ensureEqual(1, areas.size());
    }

    @Test
    public void testAttributes()
    {
        ensureEqual("San Francisco—Oakland", Country.UNITED_STATES.CALIFORNIA.SAN_FRANCISCO_OAKLAND.name());
        ensureEqual("United_States-California-Metro_San_Francisco—Oakland",
                Country.UNITED_STATES.CALIFORNIA.SAN_FRANCISCO_OAKLAND.identity().mesakit().code());
    }

    @Test
    public void testForLocation()
    {
        ensureEqual(Country.UNITED_STATES.CALIFORNIA.SAN_JOSE,
                MetropolitanArea.forLocation(Country.UNITED_STATES.CALIFORNIA.MOUNTAIN_VIEW.center()));
    }

    @Test
    public void testForRegionCode()
    {
        ensureEqual(Country.MEXICO.AGUASCALIENTES, State.forRegionCode(code("MX-AG")));
    }

    @Test
    public void testMexicoAG()
    {
        final var metro = MetropolitanArea.forLocation(Location.degrees(21.896758, -102.283537));
        ensureEqual("MX-AG", metro.state().identity().iso().code());
        ensureEqual("MX-AG-METROZONAMETROPOLITANADEAGUASCALIENTES", metro.identity().iso().code());
        ensureEqual("Mexico-Aguascalientes-Metro_Zona_metropolitana_de_Aguascalientes", metro.identity().mesakit().code());
        ensureEqual("Zona metropolitana de Aguascalientes", metro.name());
    }

    @Test
    public void testSpecialChars()
    {
        final var location = Location.degrees(47.436471, -52.7749647);
        final var county = MetropolitanArea.forLocation(location);
        ensureEqual("CA-NL-METROSTJOHNS", county.identity().iso().code());
        ensureEqual("Canada-Newfoundland_and_Labrador-Metro_St._John_s", county.identity().mesakit().code());
        ensureEqual("St. John's", county.name());
    }
}
