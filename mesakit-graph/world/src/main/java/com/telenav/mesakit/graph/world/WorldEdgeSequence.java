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

import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.world.grid.WorldCell;

import java.util.Iterator;

/**
 * A sequence of {@link WorldEdge}s that are scoped by cell.
 *
 * @author jonathanl (shibo)
 */
class WorldEdgeSequence extends EdgeSequence
{
    /** The cell where this sequence resides */
    private final WorldCell worldCell;

    private final Iterable<Edge> sequence;

    public WorldEdgeSequence(WorldCell worldCell, Iterable<Edge> sequence)
    {
        super(sequence);
        this.worldCell = worldCell;
        this.sequence = sequence;
    }

    public WorldEdge first()
    {
        var iterator = sequence.iterator();
        return iterator.hasNext() ? new WorldEdge(worldCell, iterator.next()) : null;
    }

    @Override
    public Iterator<Edge> iterator()
    {
        return new BaseIterator<>()
        {
            private final Iterator<Edge> iterator = sequence.iterator();

            @Override
            protected WorldEdge onNext()
            {
                if (iterator.hasNext())
                {
                    return new WorldEdge(worldCell, iterator.next());
                }
                return null;
            }
        };
    }
}
