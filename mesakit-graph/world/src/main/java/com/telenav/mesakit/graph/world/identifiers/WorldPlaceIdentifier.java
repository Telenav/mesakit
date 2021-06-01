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

import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.WorldPlace;
import com.telenav.mesakit.graph.world.grid.WorldCell;

/**
 * A place identifier in a {@link WorldGraph}. Scopes a place identifier with a {@link WorldCell}. The cell for this
 * identifier can be retrieved with {@link #worldCell()} and the place ({@link WorldPlace}) with {@link #place()}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldPlace
 */
public class WorldPlaceIdentifier extends PlaceIdentifier
{
    /** The cell where this identifier resides */
    private final WorldCell worldCell;

    public WorldPlaceIdentifier(final WorldCell worldCell, final PlaceIdentifier identifier)
    {
        super(identifier.asLong());
        this.worldCell = worldCell;
    }

    public Graph graph()
    {
        return worldCell.cellGraph();
    }

    public WorldPlace place()
    {
        return new WorldPlace(this);
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
