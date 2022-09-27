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

package com.telenav.mesakit.graph.world.identifiers;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.string.Paths;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.world.WorldEdge;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.grid.WorldGrid;

/**
 * An edge identifier in a {@link WorldGraph}. Scopes an edge identifier with a {@link WorldCell}. The cell for this
 * identifier can be retrieved with {@link #worldCell()} and the edge ({@link WorldEdge}) with {@link #edge()}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldEdge
 */
public class WorldEdgeIdentifier extends EdgeIdentifier
{
    public static class Converter extends BaseStringConverter<WorldEdgeIdentifier>
    {
        private final WorldGrid grid;

        public Converter(WorldGrid grid, Listener listener)
        {
            super(listener);
            this.grid = grid;
        }

        @Override
        protected WorldEdgeIdentifier onToValue(String value)
        {
            var cellName = Paths.withoutSuffix(value, '-');
            var identifier = Long.parseLong(Paths.optionalSuffix(value, '-'));
            return new WorldEdgeIdentifier(grid.worldCell(cellName), new EdgeIdentifier(identifier));
        }
    }

    /** The cell where this identifier resides */
    private final WorldCell worldCell;

    public WorldEdgeIdentifier(WorldCell worldCell, EdgeIdentifier identifier)
    {
        super(identifier.asLong());
        this.worldCell = worldCell;
    }

    @Override
    public EdgeIdentifier asForward()
    {
        return new WorldEdgeIdentifier(worldCell, super.asForward());
    }

    public WorldEdge edge()
    {
        return new WorldEdge(this);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof WorldEdgeIdentifier)
        {
            var that = (WorldEdgeIdentifier) object;
            return worldCell.equals(that.worldCell) && super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(worldCell, super.hashCode());
    }

    @Override
    public String toString()
    {
        return worldCell.identity().mesakit().code() + "-" + super.toString();
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }
}
