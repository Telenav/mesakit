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

import com.telenav.mesakit.graph.EdgeRelationMember;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

/**
 * A relation member in a {@link WorldGraph}. Overrides {@link #route()} and {@link #vertex()} to provide ways (routes)
 * and via nodes (vertexes) that are scoped by cell.
 *
 * @author jonathanl (shibo)
 * @see Route
 * @see Vertex
 */
public class WorldRelationMember extends EdgeRelationMember
{
    /** The cell where this relation member resides */
    private final WorldCell worldCell;

    public WorldRelationMember(WorldRelation relation, WorldCell worldCell, MapIdentifier identifier,
                               String role)
    {
        super(relation, identifier, role);

        ensure(worldCell != null);
        ensure(identifier != null);

        this.worldCell = worldCell;
    }

    @Override
    public Route route()
    {
        // Get the way identifier
        var wayIdentifier = (PbfWayIdentifier) identifier();

        // Find route in world graph
        var route = worldCell.worldGraph().routeForWayIdentifier(wayIdentifier);

        // If the way identifier is reversed
        if (route != null && wayIdentifier.isReverse())
        {
            // the reverse the route
            route = route.reversed();
        }

        return route;
    }

    @Override
    public String toString()
    {
        return "[WorldEdgeRelationMember identifier = " + identifier() + ", type = " + type() + ", role = " + role()
                + ", exists = " + exists() + "]";
    }

    @Override
    public Vertex vertex()
    {
        var vertex = super.vertex();
        if (vertex != null)
        {
            return new WorldVertex(worldCell, vertex);
        }

        // We couldn't find the vertex for this member in this cell, so look in neighboring cells
        var identifier = new PbfNodeIdentifier(super.identifier().asLong());
        for (var neighbor : worldCell.neighbors())
        {
            var neighborGraph = neighbor.cellGraph();
            if (neighborGraph.contains(identifier))
            {
                var neighboringVertex = neighborGraph.vertexForNodeIdentifier(identifier);
                if (neighboringVertex != null)
                {
                    return new WorldVertex(neighbor, neighboringVertex);
                }
            }
        }
        return null;
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }
}
