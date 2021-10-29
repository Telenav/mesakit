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

import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.EdgeRelationMember;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.identifiers.WorldRelationIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * An relation in a {@link WorldGraph}, scoped by a {@link WorldCell} and having a {@link WorldRelationIdentifier}. The
 * cell for this graph element can be retrieved with {@link #worldCell()} and an override of the {@link #identifier()}
 * method from {@link EdgeRelation} returns the {@link WorldRelationIdentifier} for the relation. The {@link #members()}
 * method is overridden to ensure that {@link WorldRelationMember}s are returned which are scoped by cell.
 * <p>
 * Note that there are two methods to retrieve the graph of this {@link GraphElement}. The {@link #graph()} method
 * overrides {@link #graph()} to return the {@link WorldGraph} that contains this element, while the {@link #subgraph()}
 * method returns the graph where the element is actually stored (the cell sub-graph of the world graph).
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldRelationIdentifier
 */
public class WorldRelation extends EdgeRelation
{
    /** The cell where this relation is located */
    private final WorldCell worldCell;

    public WorldRelation(WorldCell worldCell, EdgeRelation relation)
    {
        super(worldCell.cellGraph(), relation.identifier());
        this.worldCell = worldCell;
    }

    public WorldRelation(WorldRelationIdentifier identifier)
    {
        super(identifier.worldCell().cellGraph(), identifier);
        worldCell = identifier.worldCell();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof WorldRelation)
        {
            var that = (WorldRelation) object;
            return worldCell.equals(that.worldCell) && super.equals(that);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(worldCell, super.hashCode());
    }

    @Override
    public RelationIdentifier identifier()
    {
        return new WorldRelationIdentifier(worldCell, super.identifier());
    }

    @Override
    public List<EdgeRelationMember> members()
    {
        // List of members
        List<EdgeRelationMember> members = new ArrayList<>();

        // Go through this cell's relation members
        for (var member : super.members())
        {
            // If it's a node
            if (member.isNode())
            {
                // we just add it
                members.add(worldEdgeRelationMember(worldCell, member));
            }

            // If it's a way,
            if (member.isWay())
            {
                // then find out what cell the way is in
                var owner = worldCell(member);
                if (owner != null)
                {
                    // create world edge relation member for the given edge relation member
                    var worldMember = worldEdgeRelationMember(owner, member);

                    // get the route
                    var route = worldMember.route();

                    // and if the route is valid
                    if (route != null)
                    {
                        // add the way member
                        members.add(worldMember);

                        // and if either end of the route is clipped,
                        if (route.first().from().isClipped() || route.last().to().isClipped())
                        {
                            // go through neighboring cells
                            for (var neighbor : worldCell.neighbors())
                            {
                                // and if the neighboring cell contains the osm way identifier
                                if (neighbor.cellGraph().contains(member.identifier()))
                                {
                                    // then add the neighboring way
                                    members.add(
                                            new WorldRelationMember(this, neighbor, member.identifier(), member.role()));
                                }
                            }
                        }
                    }
                }
            }
        }

        return members;
    }

    @Override
    public String toString()
    {
        return "[WorldEdgeRelation identifier = " + identifier() + ", osmIdentifier = " + mapIdentifier()
                + ", members = " + members().size() + "]";
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }

    private WorldCell worldCell(EdgeRelationMember member)
    {
        if (worldCell.cellGraph().contains(member.identifier()))
        {
            return worldCell;
        }
        for (var neighbor : worldCell.neighbors())
        {
            if (neighbor.cellGraph().contains(member.identifier()))
            {
                return neighbor;
            }
        }
        return null;
    }

    private WorldRelationMember worldEdgeRelationMember(WorldCell worldCell, EdgeRelationMember member)
    {
        return new WorldRelationMember(this, worldCell, member.identifier(), member.role());
    }
}
