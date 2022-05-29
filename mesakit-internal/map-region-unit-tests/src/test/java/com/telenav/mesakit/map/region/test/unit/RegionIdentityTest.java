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

import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.mesakit.map.region.test.RegionUnitTest;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class RegionIdentityTest extends RegionUnitTest
{
    @Test
    public void testContinent()
    {
        {
            var identity = new RegionIdentity("Oceania");
            ensure(identity.isContinent());
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.mesakit().size());
            ensureEqual("Oceania", identity.mesakit().code());
        }
        {
            var identity = new RegionIdentity("Oceania").withIsoCode("OC");
            ensure(identity.isContinent());
            ensureEqual("Oceania", identity.name());
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.mesakit().size());
            ensureEqual("OC", identity.iso().code());
        }
    }

    @Test
    public void testCountry()
    {
        var identity = new RegionIdentity("United States").withIsoCode("US");
        ensure(identity.isCountry());
        ensureEqual("United States", identity.name());
        ensureEqual(1, identity.iso().size());
        ensureEqual(1, identity.mesakit().size());
        ensure(identity.iso().isIso());
        ensureEqual("US", identity.iso().code());
    }

    @Test
    public void testCounty()
    {
        var identity = new RegionIdentity("Alameda")
                .withMesaKitCode("County_Alameda")
                .withIsoCode("COUNTYALAMEDA");
        ensureEqual("Alameda", identity.name());
        ensureEqual(1, identity.iso().size());
        ensureEqual(1, identity.mesakit().size());
        ensure(identity.iso().isIso());
        ensureEqual("COUNTYALAMEDA", identity.iso().first().code());
    }

    @Test
    public void testHashAndEquals()
    {
        final Set<RegionIdentity> identities = new HashSet<>();
        var identity = new RegionIdentity("Alameda").withIsoCode("US-CA-COUNTY_ALAMEDA");
        identities.add(identity);
        identities.add(identity);
        ensureEqual(1, identities.size());
    }

    @Test
    public void testMetro()
    {
        {
            var identity = new RegionIdentity("San Jose");
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.mesakit().size());
            ensureEqual("San Jose", identity.name());
            ensureEqual("SANJOSE", identity.iso().code());
            ensureEqual("San_Jose", identity.mesakit().code());
        }
        {
            var identity = new RegionIdentity("Metro San Francisco");
            ensureEqual(1, identity.iso().size());
            ensureEqual(1, identity.mesakit().size());
            ensureEqual("Metro San Francisco", identity.name());
            ensureEqual("METROSANFRANCISCO", identity.iso().code());
            ensureEqual("Metro_San_Francisco", identity.mesakit().code());
        }
    }

    @Test
    public void testPrefix()
    {
        var us = new RegionIdentity("United States")
                .withIsoCode("US")
                .withMesaKitCode("United_States");

        var ohio = new RegionIdentity("Ohio")
                .withIsoCode("OH")
                .withMesaKitCode("Ohio");

        var prefixed = ohio.withPrefix(us);
        ensureEqual(1, us.iso().size());
        ensureEqual(1, us.mesakit().size());
        ensureEqual(1, ohio.iso().size());
        ensureEqual(1, ohio.mesakit().size());
        ensure(prefixed.hasIsoCode());
        ensure(prefixed.hasMesaKitCode());
        ensureEqual("Ohio", prefixed.name());
        ensureEqual("United_States-Ohio", prefixed.mesakit().code());
        ensureEqual("US-OH", prefixed.iso().code());
    }

    @Test
    public void testState()
    {
        var identity = new RegionIdentity("California").withIsoCode("CA");
        ensureEqual(1, identity.iso().size());
        ensureEqual("CA", identity.iso().first().code());

        var fiji = new RegionIdentity("Eastern").withMesaKitCode("Fiji-Eastern");
        ensure(fiji.isState());
        ensureEqual("Fiji-Eastern", fiji.mesakit().code());
    }
}
