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

public class RegionIdentityTest extends MapRegionUnitTest
{
    @Test
    public void testContinent()
    {
        {
            final var identity = new RegionIdentity("Oceania");
            ensure(identity.isContinent());
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.aonia().size());
            ensureEqual("Oceania", identity.aonia().code());
        }
        {
            final var identity = new RegionIdentity("Oceania").withIsoCode("OC");
            ensure(identity.isContinent());
            ensureEqual("Oceania", identity.name());
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.aonia().size());
            ensureEqual("OC", identity.iso().code());
        }
    }

    @Test
    public void testCountry()
    {
        final var identity = new RegionIdentity("United States").withIsoCode("US");
        ensure(identity.isCountry());
        ensureEqual("United States", identity.name());
        ensureEqual(1, identity.iso().size());
        ensureEqual(1, identity.aonia().size());
        ensure(identity.iso().isIso());
        ensureEqual("US", identity.iso().code());
    }

    @Test
    public void testCounty()
    {
        final var identity = new RegionIdentity("Alameda")
                .withAoniaCode("County_Alameda")
                .withIsoCode("COUNTYALAMEDA");
        ensureEqual("Alameda", identity.name());
        ensureEqual(1, identity.iso().size());
        ensureEqual(1, identity.aonia().size());
        ensure(identity.iso().isIso());
        ensureEqual("COUNTYALAMEDA", identity.iso().first().code());
    }

    @Test
    public void testHashAndEquals()
    {
        final Set<RegionIdentity> identities = new HashSet<>();
        final var identity = new RegionIdentity("Alameda").withIsoCode("US-CA-COUNTY_ALAMEDA");
        identities.add(identity);
        identities.add(identity);
        ensureEqual(1, identities.size());
    }

    @Test
    public void testMetro()
    {
        {
            final var identity = new RegionIdentity("San Jose");
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.aonia().size());
            ensureEqual("San Jose", identity.name());
            ensureEqual("SANJOSE", identity.iso().code());
            ensureEqual("San_Jose", identity.aonia().code());
        }
        {
            final var identity = new RegionIdentity("Metro San Francisco");
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.aonia().size());
            ensureEqual("Metro San Francisco", identity.name());
            ensureEqual("METROSANFRANCISCO", identity.iso().code());
            ensureEqual("Metro_San_Francisco", identity.aonia().code());
        }
    }

    @Test
    public void testPrefix()
    {
        final var us = new RegionIdentity("United States")
                .withIsoCode("US")
                .withAoniaCode("United_States");

        final var ohio = new RegionIdentity("Ohio")
                .withIsoCode("OH")
                .withAoniaCode("Ohio");

        final var prefixed = ohio.withPrefix(us);
        ensureEqual(1, us.iso().size());
        ensureEqual(1, us.aonia().size());
        ensureEqual(1, ohio.iso().size());
        ensureEqual(1, ohio.aonia().size());
        ensure(prefixed.hasIsoCode());
        ensure(prefixed.hasAoniaCode());
        ensureEqual("Ohio", prefixed.name());
        ensureEqual("United_States-Ohio", prefixed.aonia().code());
        ensureEqual("US-OH", prefixed.iso().code());
    }

    @Test
    public void testState()
    {
        final var identity = new RegionIdentity("California").withIsoCode("CA");
        ensureEqual(1, identity.iso().size());
        ensureEqual("CA", identity.iso().first().code());

        final var fiji = new RegionIdentity("Eastern").withAoniaCode("Fiji-Eastern");
        ensure(fiji.isState());
        ensureEqual("Fiji-Eastern", fiji.aonia().code());
    }
}
