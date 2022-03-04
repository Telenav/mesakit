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

import com.telenav.kivakit.core.language.primitive.Longs;
import com.telenav.kivakit.core.vm.JavaVirtualMachine.KivaKitExcludeFromSizeOf;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToLongMap;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionSet;
import com.telenav.mesakit.map.region.project.RegionLimits;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionNodes
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** Map between region indexes and regions */
    @KivaKitExcludeFromSizeOf
    private final RegionIndexMap regionIndexMap;

    /** Set of regions that are in this store */
    @KivaKitExcludeFromSizeOf
    private final Set<Integer> regionIndexes = new HashSet<>();

    /** Regions for each node */
    private final SplitLongToLongMap regionsForNode;

    public RegionNodes(String name, RegionIndexMap regionIndexMap)
    {
        this.regionIndexMap = regionIndexMap;

        regionsForNode = new SplitLongToLongMap(name + ".regionsForNode");
        regionsForNode.initialSize(RegionLimits.ESTIMATED_NODES);
        regionsForNode.initialize();
    }

    public void add(long nodeIdentifier, int regionIndex)
    {
        // Add the region index to our set
        regionIndexes.add(regionIndex);

        // Ensure the main map for a region
        var value = regionsForNode.get(nodeIdentifier);

        // If there's no entry at all yet,
        if (regionsForNode.isNull(value))
        {
            // add the region index
            regionsForNode.put(nodeIdentifier, regionIndex);
        }
        else
        {
            // Convert the value to a list of region indexes
            var regionIndexes = longToList(value);

            // If we don't have too many indexes for this node
            if (regionIndexes.size() < 4)
            {
                // add the index
                regionIndexes.add(regionIndex);
            }
            else
            {
                // complain about too many indexes for the given node
                DEBUG.warning("Node $ belongs to more than 4 regions", nodeIdentifier);
            }

            // Store the list of indexes back in the long to long map
            regionsForNode.put(nodeIdentifier, listToLong(regionIndexes));
        }
    }

    public void addAll(List<Node> nodes, int regionIndex)
    {
        // Go through each node
        for (var node : nodes)
        {
            // and add it to the given region
            add(node.getId(), regionIndex);
        }
    }

    public boolean inRegion(long nodeIdentifier, int regionIndex)
    {
        var value = regionsForNode.get(nodeIdentifier);
        if (!regionsForNode.isNull(value))
        {
            return Longs.searchWords(value, 16, regionIndex);
        }
        return false;
    }

    public Region<?> regionForIndex(int index)
    {
        return regionIndexMap.regionForIndex(index);
    }

    public RegionIndexMap regionIndexMap()
    {
        return regionIndexMap;
    }

    public Set<Integer> regionIndexes(PbfWay way)
    {
        Set<Integer> indexes = new HashSet<>();
        for (var node : way.nodes())
        {
            indexes.addAll(regionIndexes(node.getNodeId()));
        }
        return indexes;
    }

    public RegionSet regions()
    {
        return regionIndexMap.regionsForIndexes(regionIndexes);
    }

    public RegionSet regions(Iterable<Integer> identifiers)
    {
        return regionIndexMap.regionsForIndexes(identifiers);
    }

    public RegionSet regions(long wayIdentifier)
    {
        return regionIndexMap.regionsForIndexes(regionIndexes(wayIdentifier));
    }

    private void checkRegionIndex(int regionIndex)
    {
        assert regionIndexMap.isValidRegionIndex(regionIndex) : "Bad region index " + regionIndex;
    }

    private void checkRegionIndexes(List<Integer> regionIndexes)
    {
        assert regionIndexMap.isValidRegionIndexList(regionIndexes) : "Bad region index list: " + regionIndexes;
    }

    private long listToLong(List<Integer> regionIndexes)
    {
        checkRegionIndexes(regionIndexes);

        // Long value has no indexes yet (indexes are non-zero)
        var value = 0L;

        // Go through each region index
        for (var regionIndex : regionIndexes)
        {
            // ensure the index is valid
            assert regionIndexMap.isValidRegionIndex(regionIndex) : "Bad region index " + regionIndex;

            // shift the value to make room for the index
            value <<= 16;

            // or in the region index
            value |= (long) regionIndex;
        }

        return value;
    }

    private List<Integer> longToList(long value)
    {
        // The region index list to return
        List<Integer> regionIndexes = new ArrayList<>();

        // Go through up to four 16 bit values in the long
        for (var i = 0; i < 4; i++)
        {
            // Get the next potential region index
            var regionIndex = (int) (value & 0xffffL);

            // If the region index is non-zero,
            if (regionIndex > 0)
            {
                // ensure it's a valid region index
                checkRegionIndex(regionIndex);

                // and add it to the list
                regionIndexes.add(regionIndex);
            }

            // Shift the value 2 bytes (one index value) to the right
            value >>>= 16;
        }

        // Ensure indexes
        checkRegionIndexes(regionIndexes);

        return regionIndexes;
    }

    private List<Integer> regionIndexes(long nodeIdentifier)
    {
        var value = regionsForNode.get(nodeIdentifier);
        if (!regionsForNode.isNull(value))
        {
            var regionIndexes = longToList(value);
            checkRegionIndexes(regionIndexes);
            return regionIndexes;
        }
        else
        {
            DEBUG.warning("No regions found for node identifier ${long}", nodeIdentifier);
        }
        return Collections.emptyList();
    }
}
