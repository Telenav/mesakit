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

package com.telenav.tdk.graph.world;

import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.graph.Graph;
import com.telenav.tdk.graph.GraphElement;
import com.telenav.tdk.graph.Place;
import com.telenav.tdk.graph.identifiers.PlaceIdentifier;
import com.telenav.tdk.graph.world.grid.WorldCell;
import com.telenav.tdk.graph.world.identifiers.WorldPlaceIdentifier;
import com.telenav.tdk.map.geography.Location;

import static com.telenav.tdk.core.kernel.validation.Validate.ensureNotNull;

/**
 * An place in a {@link WorldGraph}, scoped by a {@link WorldCell} and having a {@link WorldPlaceIdentifier}. The cell
 * for this graph element can be retrieved with {@link #worldCell()} and an override of the {@link #identifier()} method
 * from {@link Place} returns the {@link WorldPlaceIdentifier} for the place.
 * <p>
 * World places are unlike other graph elements in a world graph in that they contain their own data (name, location,
 * population and type). Global spatial queries of places in {@link WorldGraphIndex} retrieve this information more
 * efficiently than having it be retrieved from sub-graphs.
 * <p>
 * Note that there are two methods to retrieve the graph of this {@link GraphElement}. The {@link #graph()} method
 * overrides {@link #graph()} to return the {@link WorldGraph} that contains this element, while the {@link #subgraph()}
 * method returns the graph where the element is actually stored (the cell sub-graph of the world graph).
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldPlaceIdentifier
 */
public class WorldPlace extends Place
{
    /** The cell where this place resides */
    private transient WorldCell worldCell;

    private String name;

    private Location location;

    private Count population;

    private Type type;

    public WorldPlace(final WorldCell worldCell, final Place place)
    {
        super(worldCell.worldGraph(), place.identifier());
        this.worldCell = ensureNotNull(worldCell);
        name = place.name();
        location = place.location();
        population = place.population();
        type = place.type();
    }

    public WorldPlace(final WorldPlaceIdentifier identifier)
    {
        super(identifier.place().worldCell().worldGraph(), identifier);
        worldCell = ensureNotNull(identifier.worldCell());
    }

    protected WorldPlace()
    {
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof WorldPlace)
        {
            final var that = (WorldPlace) object;
            return location().equals(that.location());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return location.hashCode();
    }

    @Override
    public PlaceIdentifier identifier()
    {
        return new WorldPlaceIdentifier(worldCell(), super.identifier());
    }

    @Override
    public Location location()
    {
        if (location != null)
        {
            return location;
        }
        return super.location();
    }

    @Override
    public String name()
    {
        if (name != null)
        {
            return name;
        }
        return super.name();
    }

    @Override
    public Count population()
    {
        if (population != null)
        {
            return population;
        }
        return super.population();
    }

    @Override
    public Type type()
    {
        if (type != null)
        {
            return type;
        }
        return super.type();
    }

    public WorldCell worldCell()
    {
        if (worldCell == null)
        {
            final var worldGraph = (WorldGraph) graph();
            worldCell = worldGraph.worldGrid().worldCell(location);
        }
        return worldCell;
    }

    @Override
    protected Graph subgraph()
    {
        return worldCell().cellGraph();
    }
}
