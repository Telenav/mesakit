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

package com.telenav.aonia.map.region;

import com.telenav.aonia.map.region.project.MapRegionUnitTest;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class RegionCodeTest extends MapRegionUnitTest
{
    @Test
    public void testAoniaCity()
    {
        final var code = code("United_States-Washington-City_San_Jose");
        ensureEqual(3, code.size());
        ensureEqual("United_States", code.first().code());
        ensureEqual("Washington", code.second().code());
        ensureEqual("City_San_Jose", code.third().code());
        ensureEqual("United_States", code.first(1).code());
        ensureEqual("United_States-Washington", code.first(2).code());
        ensureEqual("United_States-Washington-City_San_Jose", code.first(3).code());
        ensureEqual("United_States-Washington-City_San_Jose", code.code());
        ensure(code.hasState());
        ensure(code.hasCity());
        ensure(code.isAonian());
        ensure(code.isCity());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaContinent()
    {
        final var code = code("North_America");
        ensureEqual(1, code.size());
        ensureEqual("North_America", code.first().code());
        ensure(code.isAonian());
        ensure(code.isContinent());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaCountry()
    {
        final var code = code("United_States");
        ensureEqual(1, code.size());
        ensureEqual("United_States", code.first().code());
        ensure(code.isAonian());
        ensure(code.isCountry());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaCounty()
    {
        final var code = code("United_States-Washington-County_King");
        ensureEqual(3, code.size());
        ensureEqual("United_States", code.first().code());
        ensureEqual("Washington", code.second().code());
        ensureEqual("County_King", code.third().code());
        ensureEqual("United_States", code.first(1).code());
        ensureEqual("United_States-Washington", code.first(2).code());
        ensureEqual("United_States-Washington-County_King", code.first(3).code());
        ensureEqual("United_States-Washington-County_King", code.code());
        ensure(code.hasCounty());
        ensure(code.hasState());
        ensure(code.hasCounty());
        ensure(code.isAonian());
        ensure(code.isCounty());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaDistrict()
    {
        final var code = code("United_States-Washington-City_San_Jose-District_Telenav");
        ensureEqual(4, code.size());
        ensureEqual("United_States", code.first().code());
        ensureEqual("Washington", code.second().code());
        ensureEqual("City_San_Jose", code.third().code());
        ensureEqual("District_Telenav", code.fourth().code());
        ensureEqual("United_States", code.first(1).code());
        ensureEqual("United_States-Washington", code.first(2).code());
        ensureEqual("United_States-Washington-City_San_Jose", code.first(3).code());
        ensureEqual("United_States-Washington-City_San_Jose-District_Telenav", code.first(4).code());
        ensureEqual("United_States-Washington-City_San_Jose-District_Telenav", code.code());
        ensure(code.hasState());
        ensure(code.hasCity());
        ensure(code.hasDistrict());
        ensure(code.isAonian());
        ensure(code.isDistrict());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaMetro()
    {
        final var code = code("United_States-Washington-Metro_San_Jose");
        ensureEqual(3, code.size());
        ensureEqual("United_States", code.first().code());
        ensureEqual("Washington", code.second().code());
        ensureEqual("Metro_San_Jose", code.third().code());
        ensureEqual("United_States", code.first(1).code());
        ensureEqual("United_States-Washington", code.first(2).code());
        ensureEqual("United_States-Washington-Metro_San_Jose", code.first(3).code());
        ensureEqual("United_States-Washington-Metro_San_Jose", code.code());
        ensure(code.hasState());
        ensure(code.hasMetropolitanArea());
        ensure(code.isAonian());
        ensure(code.isMetropolitanArea());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaState()
    {
        final var code = code("United_States-Washington");
        ensureEqual(2, code.size());
        ensureEqual("United_States-Washington", code.first(2).code());
        ensure(code.isAonian());
        ensure(code.isState());
        ensure(code.isValid());
    }

    @Test
    public void testAoniaWorld()
    {
        final var code = code("Earth");
        ensureEqual(1, code.size());
        ensureEqual("Earth", code.first().code());
        ensure(code.isAonian());
        ensure(code.isWorld());
        ensure(code.isValid());
    }

    @Test
    public void testAppend()
    {
        final var washington = code("US-WA");
        final var seattle = code("SEATTLE");
        ensureEqual("US-WA-SEATTLE", washington.append(seattle).code());
    }

    @Test
    public void testCode()
    {
        final var code = code("US-WA");
        ensureEqual("US-WA", code.code());
    }

    @Test
    public void testHashAndEquals()
    {
        final Set<RegionCode> codes = new HashSet<>();
        final var code = code("US-CA-COUNTY_ALAMEDA");
        codes.add(code);
        codes.add(code);
        ensureEqual(1, codes.size());
    }

    @Test
    public void testIsoCity()
    {
        final var code = code("US-WA-CITY_SAN_JOSE");
        ensureEqual(3, code.size());
        ensureEqual("US", code.first().code());
        ensureEqual("WA", code.second().code());
        ensureEqual("CITY_SAN_JOSE", code.third().code());
        ensureEqual("US", code.first(1).code());
        ensureEqual("US-WA", code.first(2).code());
        ensureEqual("US-WA-CITY_SAN_JOSE", code.first(3).code());
        ensureEqual("US-WA-CITY_SAN_JOSE", code.code());
        ensure(code.hasState());
        ensure(code.hasCity());
        ensure(code.isIso());
        ensure(code.isCity());
        ensure(code.isValid());
    }

    @Test
    public void testIsoContinent()
    {
        final var code = code("NA");
        ensureEqual(1, code.size());
        ensureEqual("NA", code.first().code());
        ensure(code.isIso());
        ensure(code.isContinent());
        ensure(code.isValid());
    }

    @Test
    public void testIsoCountry()
    {
        final var code = code("US");
        ensureEqual(1, code.size());
        ensureEqual("US", code.first().code());
        ensure(code.isIso());
        ensure(code.isCountry());
        ensure(code.isValid());
    }

    @Test
    public void testIsoCounty()
    {
        final var code = code("US-WA-COUNTY_KING");
        ensureEqual(3, code.size());
        ensureEqual("US", code.first().code());
        ensureEqual("WA", code.second().code());
        ensureEqual("COUNTY_KING", code.third().code());
        ensureEqual("US", code.first(1).code());
        ensureEqual("US-WA", code.first(2).code());
        ensureEqual("US-WA-COUNTY_KING", code.first(3).code());
        ensureEqual("US-WA-COUNTY_KING", code.code());
        ensure(code.hasCounty());
        ensure(code.hasState());
        ensure(code.hasCounty());
        ensure(code.isIso());
        ensure(code.isCounty());
        ensure(code.isValid());
    }

    @Test
    public void testIsoDistrict()
    {
        final var code = code("US-WA-CITY_SAN_JOSE-DISTRICT_TELENAV");
        ensureEqual(4, code.size());
        ensureEqual("US", code.first().code());
        ensureEqual("WA", code.second().code());
        ensureEqual("CITY_SAN_JOSE", code.third().code());
        ensureEqual("DISTRICT_TELENAV", code.fourth().code());
        ensureEqual("US", code.first(1).code());
        ensureEqual("US-WA", code.first(2).code());
        ensureEqual("US-WA-CITY_SAN_JOSE", code.first(3).code());
        ensureEqual("US-WA-CITY_SAN_JOSE-DISTRICT_TELENAV", code.first(4).code());
        ensureEqual("US-WA-CITY_SAN_JOSE-DISTRICT_TELENAV", code.code());
        ensure(code.hasState());
        ensure(code.hasCity());
        ensure(code.hasDistrict());
        ensure(code.isIso());
        ensure(code.isDistrict());
        ensure(code.isValid());
    }

    @Test
    public void testIsoMetro()
    {
        final var code = code("US-WA-METRO_SAN_JOSE");
        ensureEqual(3, code.size());
        ensureEqual("US", code.first().code());
        ensureEqual("WA", code.second().code());
        ensureEqual("METRO_SAN_JOSE", code.third().code());
        ensureEqual("US", code.first(1).code());
        ensureEqual("US-WA", code.first(2).code());
        ensureEqual("US-WA-METRO_SAN_JOSE", code.first(3).code());
        ensureEqual("US-WA-METRO_SAN_JOSE", code.code());
        ensure(code.hasState());
        ensure(code.hasMetropolitanArea());
        ensure(code.isIso());
        ensure(code.isMetropolitanArea());
        ensure(code.isValid());
    }

    @Test
    public void testIsoState()
    {
        final var code = code("US-WA");
        ensureEqual(2, code.size());
        ensureEqual("US-WA", code.first(2).code());
        ensure(code.isIso());
        ensure(code.isState());
        ensure(code.isValid());
    }

    @Test
    public void testIsoWorld()
    {
        final var code = code("EARTH");
        ensureEqual(1, code.size());
        ensureEqual("EARTH", code.first().code());
        ensure(code.isIso());
        ensure(code.isWorld());
        ensure(code.isValid());
    }

    @Test
    public void testTransmogrifications()
    {
        {
            final var code = code("US-WA-County_Thurston--Olympia");
            ensureEqual("County_Thurston\u2014Olympia", code.last().nameized().code());
        }
        {
            final var code = code("US-WA-County_Thurston--Olympia");
            ensureEqual("US-WA-COUNTYTHURSTONOLYMPIA", code.isoized().code());
        }
        {
            final var code = code("United States");
            ensureEqual("United_States", code.aonized().code());
        }
    }
}
