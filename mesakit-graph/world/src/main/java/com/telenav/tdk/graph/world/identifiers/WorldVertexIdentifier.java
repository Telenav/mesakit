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

package com.telenav.kivakit.graph.world.identifiers;

import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.identifiers.VertexIdentifier;
import com.telenav.kivakit.graph.world.WorldGraph;
import com.telenav.kivakit.graph.world.WorldVertex;
import com.telenav.kivakit.graph.world.grid.WorldCell;

/**
 * A vertex identifier in a {@link WorldGraph}. Scopes a vertex identifier with a {@link WorldCell}. The cell for this
 * identifier can be retrieved with {@link #worldCell()} and the vertex ({@link WorldVertex}) with {@link #vertex()}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldVertex
 */
public class WorldVertexIdentifier extends VertexIdentifier
{
    /** The cell where this identifier resides */
    private final WorldCell worldCell;

    public WorldVertexIdentifier(final WorldCell worldCell, final VertexIdentifier identifier)
    {
        super(identifier.asInteger());
        this.worldCell = worldCell;
    }

    public Graph graph()
    {
        return worldCell.cellGraph();
    }

    @Override
    public String toString()
    {
        return worldCell.identity().tdk().code() + "-" + super.toString();
    }

    public WorldVertex vertex()
    {
        return new WorldVertex(this);
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }
}
