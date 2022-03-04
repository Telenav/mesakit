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

package com.telenav.mesakit.graph.library.osm.change.store;

import com.telenav.kivakit.core.collections.map.MultiMap;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.mesakit.graph.library.osm.change.NewWay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A store of new ways to add to the base graph.
 *
 * @author jonathanl (shibo)
 */
public class NewWayStore implements Iterable<NewWay>
{
    /**
     * Map of new ways by way name
     */
    private final MultiMap<String, NewWay> wayForName = new MultiMap<>();

    /**
     * Adds the given new way to this store
     */
    public void add(NewWay way)
    {
        // Store way by its name
        wayForName.add(way.name(), way);
    }

    public boolean isEmpty()
    {
        return ways().isEmpty();
    }

    @Override
    public Iterator<NewWay> iterator()
    {
        return ways().iterator();
    }

    public void referenceNodes()
    {
        for (var way : ways())
        {
            way.referenceNodes();
        }
    }

    /**
     * @return A collection of ways that have been connected up to maximum length by name
     */
    public Collection<NewWay> ways()
    {
        // List of ways
        List<NewWay> ways = new ArrayList<>();

        // Go through each way name
        for (var name : wayForName.keySet())
        {
            // Get the list of ways by the given name
            List<NewWay> list = wayForName.get(name);

            // If there are any ways,
            if (list != null)
            {
                // and the name is non-empty
                if (!Strings.isEmpty(name))
                {
                    // then compact the ways as much as possible
                    list = compact(list);
                }

                // add the list of ways to the return list
                ways.addAll(list);
            }
        }
        return ways;
    }

    /**
     * Compacts the list of new ways by connecting any ways that are connected together at a reasonable angle
     *
     * @param ways The list to compact
     */
    private List<NewWay> compact(List<NewWay> ways)
    {
        // Make a copy of the input list that we can modify
        List<NewWay> compacted = new ArrayList<>(ways);

        NewWay a;
        NewWay b;

        do
        {
            // Initially there is no a or b to connect
            a = null;
            b = null;

            // Loop through ways in the list
            OUTER:
            for (var way : compacted)
            {
                // Loop through every possible way to connect to
                for (var connectTo : compacted)
                {
                    // If the two ways are connected
                    if (!way.equals(connectTo) && way.isConnected(connectTo))
                    {
                        // then we can connect the two
                        a = way;
                        b = connectTo;
                        break OUTER;
                    }
                }
            }

            // If there are two ways to connect
            if (a != null)
            {
                // Remove the two ways
                compacted.remove(a);
                compacted.remove(b);

                // Add a new way that has both polylines
                compacted.add(a.connectedTo(b));
            }
        }
        while (a != null);
        return compacted;
    }
}
