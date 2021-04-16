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

import com.telenav.kivakit.core.kernel.language.collections.set.Sets;
import com.telenav.mesakit.map.region.project.MapRegionUnitTest;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import org.junit.Test;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegionWaysTest extends MapRegionUnitTest
{
    @Test
    public void test()
    {
        final RegionIndexMap regionIndexMap = regionIndexMap();
        test(regionIndexMap, new RegionWays("test", regionIndexMap));
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

    private void test(final RegionIndexMap regionIndexMap, final RegionWays ways)
    {
        final var waysPerList = 5;

        var wayIdentifier = 1;
        for (final State state : Country.UNITED_STATES.states())
        {
            ways.addAll(regionIndexMap.indexForRegion(state), ways(wayIdentifier, waysPerList));
            wayIdentifier += waysPerList;
        }

        wayIdentifier = 1;
        for (final State state : Country.UNITED_STATES.states())
        {
            for (var i = 0; i < waysPerList; i++)
            {
                ensure(ways.contains(regionIndexMap.indexForRegion(state), wayIdentifier + i));
                ensure(ways.contains(regionIndexMap.indexForRegion(state), wayIdentifier + i));
            }
            ensure(ways.regions().contains(state));
            ensureEqual(Sets.hashset(state), Sets.hashset(ways.regions(wayIdentifier)));
            wayIdentifier += waysPerList;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private List<Way> ways(final long wayIdentifier, final int count)
    {
        final List<Way> ways = new ArrayList<>();
        for (var i = 0; i < count; i++)
        {
            ways.add(new Way(new CommonEntityData(wayIdentifier + i, 1, new Date(), new OsmUser(3, "test"), 1)));
        }
        return ways;
    }
}
