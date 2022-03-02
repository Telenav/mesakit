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

import com.telenav.kivakit.language.count.Count;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A bi-directional map between regions and an index into the list of used regions.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("rawtypes")
public class RegionIndexMap
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final List<Region> regions = new ArrayList<>();

    public void add(Region region)
    {
        // Index 0 is the null region, so the index stored in the region metadata slot starts at 1
        regions.add(region);
        region.metadata(size());
    }

    public void addAll(Iterable<? extends Region> regions)
    {
        regions.forEach(this::add);
    }

    public Count count()
    {
        return Count.count(size());
    }

    /**
     * @return An index &gt; 0 for the region in the index map
     */
    public Integer indexForRegion(Region region)
    {
        return (Integer) region.metadata();
    }

    public boolean isValidRegionIndex(Integer regionIndex)
    {
        return regionIndex > 0 && regionIndex < size() + 1;
    }

    public boolean isValidRegionIndexList(Collection<Integer> regionIndexes)
    {
        for (var regionIndex : regionIndexes)
        {
            if (!isValidRegionIndex(regionIndex))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The region for an index &gt; 0
     */
    public Region regionForIndex(int index)
    {
        // Subtract one because index 0 is reserved to represent the null region
        return regions.get(index - 1);
    }

    public RegionSet regionsForIndexes(Iterable<Integer> indexes)
    {
        var regions = new RegionSet();
        for (var index : indexes)
        {
            var region = regionForIndex(index);
            if (region != null)
            {
                regions.add(region);
            }
            else
            {
                LOGGER.warning("No region found for index $", index);
            }
        }
        return regions;
    }

    public int size()
    {
        return regions.size();
    }
}
