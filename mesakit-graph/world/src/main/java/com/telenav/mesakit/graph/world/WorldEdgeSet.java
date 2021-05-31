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

package com.telenav.mesakit.graph.world;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.identifiers.WorldEdgeIdentifier;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;

/**
 * A set of edges within a world cell.
 *
 * @author jonathanl (shibo)
 * @see WorldVertex
 */
class WorldEdgeSet extends EdgeSet
{
    public WorldEdgeSet(final Estimate estimate)
    {
        super(estimate);
    }

    public WorldEdgeSet(final Estimate estimate, final WorldCell worldCell, final EdgeSet edges)
    {
        this(estimate);
        addAll(worldCell, edges);
    }

    public void add(final WorldCell worldCell, final Edge edge)
    {
        add(new WorldEdge(new WorldEdgeIdentifier(worldCell, edge.identifier())));
    }

    public void add(final WorldEdge edge)
    {
        super.add(edge);
    }

    public void addAll(final WorldCell worldCell, final EdgeSet edges)
    {
        for (final var edge : edges)
        {
            add(worldCell, edge);
        }
    }
}
