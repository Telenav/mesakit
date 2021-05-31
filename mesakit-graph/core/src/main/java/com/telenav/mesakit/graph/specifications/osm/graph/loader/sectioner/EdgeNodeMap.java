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

package com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner;

import com.telenav.kivakit.collections.primitive.array.scalars.SplitLongArray;
import com.telenav.kivakit.collections.primitive.map.split.SplitLongToIntMap;
import com.telenav.kivakit.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.specifications.library.pbf.IntersectionMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A store that holds the nodes (in order) for each edge. This is required to look up intersections by node identifier
 * in the {@link IntersectionMap} class during edge sectioning.
 *
 * @author jonathanl (shibo)
 */
public class EdgeNodeMap
{
    /** Index of node identifiers for a given edge in the nodeIdentifiers array */
    private final SplitLongToIntMap indexForEdge;

    /** Number of nodes for a given edge */
    private final SplitLongToIntMap nodeCountForEdge;

    /** All node identifiers for all edges laid out end-to-end */
    private final SplitLongArray nodeIdentifiers;

    public EdgeNodeMap(final String name)
    {
        indexForEdge = new SplitLongToIntMap(name + ".indexForEdge");
        indexForEdge.nullInt(Integer.MIN_VALUE);
        indexForEdge.initialize();

        nodeCountForEdge = new SplitLongToIntMap(name + ".nodeCountForEdge");
        nodeCountForEdge.initialize();

        nodeIdentifiers = new SplitLongArray(name + ".nodeIdentifiers");
        nodeIdentifiers.initialize();
    }

    /**
     * @return The list of node identifiers (in order) for the given edge identifier
     */
    public List<NodeIdentifier> get(final EdgeIdentifier identifier)
    {
        // Get the node count for the edge
        final var nodeCount = nodeCountForEdge.get(identifier.asLong());

        // If the count is not null
        if (!nodeCountForEdge.isNull(nodeCount))
        {
            // Get the index of the node identifiers
            final var index = indexForEdge.get(identifier.asLong());

            // and add each node identifier starting at that index to the list
            final List<NodeIdentifier> identifiers = new ArrayList<>();
            for (var offset = 0; offset < nodeCount; offset++)
            {
                identifiers.add(new PbfNodeIdentifier(nodeIdentifiers.get(index + offset)));
            }

            return identifiers;
        }
        return Collections.emptyList();
    }

    /**
     * Store node identifiers (in order) for the given edge for later retrieval via {@link #get(EdgeIdentifier)}
     */
    public void put(final EdgeIdentifier identifier, final List<NodeIdentifier> nodes)
    {
        // Add nodes at next available index
        var index = nodeIdentifiers.size();

        // Save the index and the number of nodes
        indexForEdge.put(identifier.asLong(), index);
        nodeCountForEdge.put(identifier.asLong(), nodes.size());

        // Go through nodes
        for (final var node : nodes)
        {
            // adding each node identifier
            nodeIdentifiers.set(index++, node.asLong());
        }
    }
}
