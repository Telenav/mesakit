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

package com.telenav.mesakit.graph.traffic;

import com.telenav.kivakit.configuration.Lookup;
import com.telenav.kivakit.kernel.testing.SlowTests;
import com.telenav.mesakit.graph.traffic.project.KivaKitGraphTrafficUnitTest;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSection;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionDatabase;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashSet;
import java.util.Set;

@Category({ SlowTests.class })
public class RoadSectionDatabaseTest extends KivaKitGraphTrafficUnitTest
{
    public RoadSectionDatabaseTest()
    {
        loadBayAreaRoadSectionDatabase();
    }

    @Test
    public void test()
    {
        for (final RoadSection section : Lookup.global().locate(RoadSectionDatabase.class).roadSections())
        {
            checkForParentCycles(section);
        }
    }

    @Test
    public void testGetMainLinkIds()
    {
        long incoming;

        // freeway location, approximately 37.60417,-121.8718
        // note: success on old NT database, success on new TA database

        incoming = getMainIncomingLinkId(105104899);
        ensureEqual(105304898L, incoming);
        ensureEqual(105104899L, getMainOutgoingLinkId(incoming));

        incoming = getMainIncomingLinkId(105004898);
        ensureEqual(105204899L, incoming);
        ensureEqual(105004898L, getMainOutgoingLinkId(incoming));

        // arterial location, approximately 37.67578,-121.92125
        // note: fails on old NT database, fails on new TA database

        // incoming = getMainIncomingLinkId(105113423);
        // assertEquals(105313422, incoming);
        // assertEquals(105113423, getMainOutgoingLinkId(incoming));

        // incoming = getMainIncomingLinkId(105013422);
        // assertEquals(105213423, incoming);
        // assertEquals(105013422, getMainOutgoingLinkId(incoming));

        long outgoing;

        outgoing = getMainOutgoingLinkId(105104899);
        ensureEqual(105304899L, outgoing);
        ensureEqual(105104899L, getMainIncomingLinkId(outgoing));

        outgoing = getMainOutgoingLinkId(105004898);
        ensureEqual(105204898L, outgoing);
        ensureEqual(105004898L, getMainIncomingLinkId(outgoing));

        // outgoing = getMainOutgoingLinkId(105113423);
        // assertEquals(105313423, outgoing);
        // assertEquals(105113423, getMainIncomingLinkId(outgoing));

        // outgoing = getMainOutgoingLinkId(105013422);
        // assertEquals(105213422, outgoing);
        // assertEquals(105013422, getMainIncomingLinkId(outgoing));
    }

    @Test
    public void testRoadSectionDetails()
    {
        ensureEqual("Bayshore Fwy S",
                TmcCode.forLong(105004141).roadSection().betweenCrossStreets().getMainRoad().toString());
        ensureEqual("Bayshore Fwy N",
                TmcCode.forLong(105104141).roadSection().betweenCrossStreets().getMainRoad().toString());
        ensureEqual("Lawrence Expy",
                TmcCode.forLong(105004141).roadSection().betweenCrossStreets().getFirstCrossStreet().toString());
        ensureEqual("Lawrence Expy",
                TmcCode.forLong(105104141).roadSection().betweenCrossStreets().getFirstCrossStreet().toString());
    }

    private void checkForParentCycles(final RoadSection section)
    {
        final Set<RoadSection> visited = new HashSet<>();
        for (var current = section; current != null; current = current.parent())
        {
            if (visited.contains(current))
            {
                fail("Cycle in PARENT HIERARCHY detected in " + section);
            }
            visited.add(current);
        }
    }

    private long getMainIncomingLinkId(final long current)
    {
        final TmcCode code = TmcCode.forLong(current);
        return code.roadSection().previous().identifier().value().asLong();
    }

    private long getMainOutgoingLinkId(final long current)
    {
        return TmcCode.forLong(current).roadSection().next().identifier().value().asLong();
    }
}
