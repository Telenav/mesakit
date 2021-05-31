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

package com.telenav.kivakit.graph.world.grid;

import com.telenav.kivakit.graph.world.WorldGraph;
import com.telenav.kivakit.graph.world.repository.WorldGraphReference;
import com.telenav.kivakit.map.utilities.grid.GridCell;

/**
 * A source of a {@link WorldCell} given a {@link WorldGraphReference} and a {@link GridCell}. This is essentially a
 * reference class that, together with {@link WorldGraphReference} avoids hard-referencing of {@link WorldGraph}s so
 * that they can be unloaded to conserve memory.
 *
 * @author jonathanl (shibo)
 * @see WorldCell
 * @see WorldGraph
 * @see WorldGraphReference
 * @see GridCell
 */
public class WorldCellReference
{
    /** The source of the world graph for this world cell */
    private final WorldGraphReference graph;

    /** The grid cell within the world graph */
    private final GridCell gridCell;

    public WorldCellReference(final WorldGraphReference graph, final GridCell gridCell)
    {
        this.graph = graph;
        this.gridCell = gridCell;
    }

    /**
     * @return The referenced world cell
     */
    public WorldCell worldCell()
    {
        return this.graph.get().worldGrid().worldCell(this.gridCell);
    }
}
