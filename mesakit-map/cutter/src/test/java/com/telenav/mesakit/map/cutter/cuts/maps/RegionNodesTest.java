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

package com.telenav.mesakit.map.cutter.cuts.maps;

import com.telenav.kivakit.kernel.language.collections.set.Sets;
import com.telenav.mesakit.map.region.RegionSet;
import com.telenav.mesakit.map.region.project.MapRegionUnitTest;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;

public class RegionNodesTest extends MapRegionUnitTest
{
    @Test
    public void testHere()
    {
        final RegionIndexMap regionIndexMap = regionIndexMap();
        test(regionIndexMap, new RegionNodes("test", regionIndexMap));
    }

    @Test
    public void testHere2()
    {
        final RegionIndexMap regionIndexMap = regionIndexMap();
        final RegionNodes nodes = new RegionNodes("test", regionIndexMap);
        for (var i = 1; i <= 5; i++)
        {
            nodes.add(1, i);
        }
        for (var i = 1; i <= 4; i++)
        {
            ensure(nodes.inRegion(1, i));
        }
    }

    @Test
    public void testOsm()
    {
        final RegionIndexMap regionIndexMap = regionIndexMap();
        test(regionIndexMap, new RegionNodes("test", regionIndexMap));
    }

    private RegionIndexMap regionIndexMap()
    {
        final RegionIndexMap identifiers = new RegionIndexMap();
        for (final State state : Country.UNITED_STATES.states())
        {
            identifiers.add(state);
            for (final MetropolitanArea area : state.metropolitanAreas())
            {
                identifiers.add(area);
            }
        }
        return identifiers;
    }

    private void test(final RegionIndexMap regionIndexMap, final RegionNodes nodes)
    {
        // Add each state in the US to a synthetic "node"
        var nodeIdentifier = 1;
        for (final State state : Country.UNITED_STATES.states())
        {
            regionIndexMap.add(state);
            nodes.add(nodeIdentifier++, regionIndexMap.indexForRegion(state));
        }

        // Ensure each state to make sure that the given node is in the given region
        nodeIdentifier = 1;
        for (final State state : Country.UNITED_STATES.states())
        {
            ensure(nodes.inRegion(nodeIdentifier, regionIndexMap.indexForRegion(state)));
            ensure(nodes.inRegion(nodeIdentifier, regionIndexMap.indexForRegion(state)));
            ensure(nodes.regions().contains(state));
            ensureEqual(Sets.hashset(state), Sets.hashset(nodes.regions(nodeIdentifier)));
            nodeIdentifier++;
        }

        // Go through all states and metro areas
        nodeIdentifier = 1;
        for (final State state : Country.UNITED_STATES.states())
        {
            final var metropolitanAreas = state.metropolitanAreas();
            if (!metropolitanAreas.isEmpty())
            {
                // adding each region to the given node
                final var area = metropolitanAreas.iterator().next();
                final var regionIndex = regionIndexMap.indexForRegion(area);
                ensure(regionIndex != null, "No region index for $", area);
                nodes.add(nodeIdentifier, regionIndex);

                // then get the set of regions for the node and there should be two for each one
                // (one for the state and one for the metro area)
                final RegionSet regions = nodes.regions(nodeIdentifier);
                ensure(regions.size() == 2);

                // Ensure that the state and area are both found for the node
                final var expected = new RegionSet();
                expected.add(state);
                expected.add(area);
                ensureEqual(expected, nodes.regions(nodeIdentifier));
            }
            nodeIdentifier++;
        }
    }
}
