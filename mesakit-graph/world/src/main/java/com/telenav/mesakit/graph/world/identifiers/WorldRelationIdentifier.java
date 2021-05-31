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

import com.telenav.kivakit.kernel.language.object.Hash;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.WorldRelation;
import com.telenav.mesakit.graph.world.grid.WorldCell;

/**
 * A relation identifier in a {@link WorldGraph}. Scopes a relation identifier with a {@link WorldCell}. The cell for
 * this identifier can be retrieved with {@link #worldCell()} and the relation ({@link WorldRelation}) with {@link
 * #relation()}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldRelation
 */
public class WorldRelationIdentifier extends RelationIdentifier
{
    /** The cell where this identifier resides */
    private final WorldCell worldCell;

    public WorldRelationIdentifier(final WorldCell worldCell, final RelationIdentifier identifier)
    {
        super(identifier.asLong());
        this.worldCell = worldCell;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof WorldRelationIdentifier)
        {
            final var that = (WorldRelationIdentifier) object;
            return worldCell.equals(that.worldCell) && super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(worldCell, super.hashCode());
    }

    public WorldRelation relation()
    {
        return new WorldRelation(this);
    }

    @Override
    public String toString()
    {
        return worldCell.identity().tdk().code() + "-" + super.toString();
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }
}
