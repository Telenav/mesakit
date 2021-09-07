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

package com.telenav.mesakit.graph.specifications.library.pbf;

import com.telenav.kivakit.kernel.interfaces.naming.Named;
import com.telenav.kivakit.kernel.language.collections.CompressibleCollection;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToByteMap;
import com.telenav.kivakit.primitive.collections.set.SplitLongSet;
import com.telenav.mesakit.map.region.project.RegionLimits;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

/**
 * Determines which nodes are intersections by counting the number of times that {@link WayNode}s are referenced by
 * ways. If a node is referenced more than two times (one reference to a node shared by two connected ways is not an
 * intersection), then it is an intersection involving three or more ways. Way node references are indicated by calling
 * {@link #addReference(long nodeIdentifier)} for each way node in each way. Once processing of ways has completed, a
 * call to {@link #doneAdding()} frees the memory used by the (very large) intersection counting data structures after
 * copying the intersection node identifiers into a much more memory-efficient {@link SplitLongSet}.
 *
 * @author jonathanl (shibo)
 */
public class IntersectionMap implements Named
{
    // General case of way node counting for large identifiers
    private SplitLongToByteMap countMap;

    // The intersection nodes once way processing has completed
    private SplitLongSet intersections;

    // Name for debugging purposes
    private final String name;

    /**
     * @param name A name for debugging purposes
     */
    public IntersectionMap(final String name)
    {
        this.name = name;

        countMap = new SplitLongToByteMap(name() + ".count.map");
        countMap.initialSize(RegionLimits.ESTIMATED_WAYS);
        countMap.initialize();
    }

    /**
     * Increment the reference count for the given node identifier
     */
    public int addReference(final long nodeIdentifier)
    {
        // Increment the counter in the map
        var count = countMap.get(nodeIdentifier);
        if (count == 0)
        {
            count = 1;
        }
        else
        {
            if (count <= 2)
            {
                count++;
            }
        }
        countMap.put(nodeIdentifier, count);
        return count;
    }

    /**
     * Called when finished processing ways to reduce memory consumption
     */
    public void doneAdding()
    {
        // Allocate set of intersections
        intersections = new SplitLongSet("intersections");
        intersections.initialSize(Estimate._65536);
        intersections.initialize();

        // then iterate through the count map keys,
        final var map = countMap;
        final var iterator = map.keys();
        while (iterator.hasNext())
        {
            // and if the count for a identifier is greater than two
            final var identifier = iterator.next();
            if (map.get(identifier) > 2)
            {
                // then add the identifier to the set of intersections
                intersections.add(identifier);
            }
        }

        // finally, free the potentially giant count map
        countMap = null;

        // and reduce the storage of the intersections set.
        intersections.compress(CompressibleCollection.Method.FREEZE);
    }

    /**
     * @return True if the given node identifier is an intersection (has more than TWO references, one for an incoming
     * way and one for an outgoing way)
     */
    public boolean isIntersection(final long nodeIdentifier)
    {
        ensure(intersections != null);
        return intersections.contains(nodeIdentifier);
    }

    @Override
    public String name()
    {
        return name;
    }
}
