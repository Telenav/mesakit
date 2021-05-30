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

import com.telenav.tdk.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.tdk.data.formats.library.map.identifiers.WayIdentifier;
import com.telenav.tdk.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.tdk.graph.EdgeRelationMember;
import com.telenav.tdk.graph.Route;
import com.telenav.tdk.graph.Vertex;
import com.telenav.tdk.graph.world.grid.WorldCell;

import static com.telenav.tdk.core.kernel.validation.Validate.ensure;

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

    public WorldRelationMember(final WorldRelation relation, final WorldCell worldCell, final MapIdentifier identifier,
                               final String role)
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
        final var wayIdentifier = (WayIdentifier) identifier();

        // Find route in world graph
        var route = this.worldCell.worldGraph().routeForWayIdentifier(wayIdentifier);

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
        final var vertex = super.vertex();
        if (vertex != null)
        {
            return new WorldVertex(this.worldCell, vertex);
        }

        // We couldn't find the vertex for this member in this cell, so look in neighboring cells
        final var identifier = new PbfNodeIdentifier(super.identifier().asLong());
        for (final var neighbor : this.worldCell.neighbors())
        {
            final var neighborGraph = neighbor.cellGraph();
            if (neighborGraph.contains(identifier))
            {
                final var neighboringVertex = neighborGraph.vertexForNodeIdentifier(identifier);
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
        return this.worldCell;
    }
}
