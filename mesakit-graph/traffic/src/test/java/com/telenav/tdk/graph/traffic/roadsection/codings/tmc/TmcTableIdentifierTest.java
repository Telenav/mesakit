////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.traffic.roadsection.codings.tmc;

import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.testing.KivaKitUnitTest;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier;
import org.junit.Test;

public class TmcTableIdentifierTest extends KivaKitUnitTest
{
    private static final TmcTableIdentifierExtractor extractor = new TmcTableIdentifierExtractor(Listener.NULL);

    @Test
    public void testCountryCode()
    {
        var identifier = new TmcTableIdentifier(105);
        ensureEqual(1, identifier.countryCode());

        identifier = new TmcTableIdentifier(1209);
        ensureEqual(12, identifier.countryCode());

        identifier = new TmcTableIdentifier(1401);
        ensureEqual(14, identifier.countryCode());

        identifier = new TmcTableIdentifier(1532);
        ensureEqual(15, identifier.countryCode());
    }

    @Test
    public void testFromTmcIdentifier()
    {
        testFromTmcIdentifier(105, "105+02345");
        testFromTmcIdentifier(1209, "C09P02345");
        testFromTmcIdentifier(1301, "D01-16358");
        testFromTmcIdentifier(1426, "E26N43345");

        ensureNull(extractor.extract(RoadSectionIdentifier
                .forCodingSystemAndIdentifier(RoadSectionCodingSystem.TELENAV_TRAFFIC_LOCATION, 45678, false)));
        ensureNull(extractor.extract(RoadSectionIdentifier
                .forCodingSystemAndIdentifier(RoadSectionCodingSystem.OSM_EDGE_IDENTIFIER, 45678, false)));
    }

    @Test
    public void testTableNumber()
    {
        var identifier = new TmcTableIdentifier(105);
        ensureEqual(5, identifier.tableNumber());

        identifier = new TmcTableIdentifier(1209);
        ensureEqual(9, identifier.tableNumber());

        identifier = new TmcTableIdentifier(1401);
        ensureEqual(1, identifier.tableNumber());

        identifier = new TmcTableIdentifier(1532);
        ensureEqual(32, identifier.tableNumber());
    }

    private void testFromTmcIdentifier(final int table, final String tmc)
    {
        final TmcCode tmcCode = TmcCode.forCode(tmc);
        final RoadSectionIdentifier tmcIdentifier = tmcCode.asIdentifier(false);
        ensureEqual(table, extractor.extract(tmcIdentifier).asInteger());
    }
}
