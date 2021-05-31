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

package com.telenav.kivakit.graph.world;

import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.identifiers.EdgeIdentifier;
import com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes.*;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier;
import com.telenav.kivakit.graph.world.grid.WorldCell;
import com.telenav.kivakit.graph.world.identifiers.WorldEdgeIdentifier;
import com.telenav.kivakit.map.measurements.*;
import com.telenav.kivakit.map.road.model.*;

import java.util.*;

/**
 * An edge in a {@link WorldGraph}, scoped by a {@link WorldCell} and having a {@link WorldEdgeIdentifier}. The cell for
 * this graph element can be retrieved with {@link #worldCell()} and an override of the {@link #identifier()} method
 * from {@link Edge} returns the {@link WorldEdgeIdentifier} for the edge. Other methods ({@link #from()}, {@link
 * #to()}, etc) are overridden to ensure that graph elements accessed through a world edge continue to be scoped by
 * cell.
 * <p>
 * Note that there are two methods to retrieve the graph of this {@link GraphElement}. The {@link #graph()} method
 * overrides {@link #graph()} to return the {@link WorldGraph} that contains this element, while the {@link #subgraph()}
 * method returns the graph where the element is actually stored (the cell sub-graph of the world graph).
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldEdgeIdentifier
 */
public class WorldEdge extends Edge
{
    public static class Converter extends Edge.Converter
    {
        private final WorldGraph graph;

        public Converter(final WorldGraph graph, final Listener<Message> listener)
        {
            super(graph, listener);
            this.graph = graph;
        }

        @Override
        protected Edge onConvertToObject(final String value)
        {
            // Try to find the third '-' in the string value (for example, "cell-60-36-")
            final var end = Strings.nth(value, 3, '-');

            // and if we found a third '-'
            if (end > 0)
            {
                // extract the cell name from the value
                final var cellName = value.substring(0, end);

                // and look up the cell by name,
                final var worldCell = graph.worldGrid().worldCell(cellName);

                // and if we got the cell
                if (worldCell != null)
                {
                    // then convert the edge using the cell's graph
                    final var converter = new Edge.Converter(worldCell.cellGraph(), this);
                    final var edge = converter.convert(value.substring(end + 1));

                    // and if the edge converted
                    if (edge != null)
                    {
                        // then we can return the world edge
                        return new WorldEdge(worldCell, edge);
                    }
                }
            }
            return null;
        }

        @Override
        protected String onConvertToString(final Edge edge)
        {
            return edge.toString();
        }
    }

    /** The cell where this edge is located */
    private final WorldCell worldCell;

    public WorldEdge(final WorldCell worldCell, final Edge edge)
    {
        super(null, edge.identifier());
        this.worldCell = worldCell;
    }

    public WorldEdge(final WorldCell worldCell, final EdgeIdentifier identifier)
    {
        super(null, identifier);
        this.worldCell = worldCell;
    }

    public WorldEdge(final WorldEdgeIdentifier identifier)
    {
        super(null, identifier);
        worldCell = identifier.worldCell();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof WorldEdge)
        {
            final var that = (WorldEdge) object;
            return worldCell.equals(that.worldCell) && super.equals(that);
        }
        return false;
    }

    @Override
    public Vertex from()
    {
        return new WorldVertex(worldCell, fromVertexIdentifier());
    }

    @Override
    public GradeSeparation fromGradeSeparation()
    {
        return cellEdge().fromGradeSeparation();
    }

    @Override
    public Graph graph()
    {
        return worldCell.worldGraph();
    }

    @Override
    public int hashCode()
    {
        return 31 * worldCell.hashCode() + (int) identifierAsLong();
    }

    @Override
    public EdgeIdentifier identifier()
    {
        return new WorldEdgeIdentifier(worldCell, super.identifier());
    }

    @Override
    public boolean osmCouldBeDoubleDigitized()
    {
        return cellEdge().osmCouldBeDoubleDigitized();
    }

    @Override
    public Distance osmMaximumDoubleDigitizationSeparation()
    {
        return cellEdge().osmMaximumDoubleDigitizationSeparation();
    }

    @Override
    public RoadSectionIdentifier osmTelenavTrafficLocationIdentifier()
    {
        return cellEdge().osmTelenavTrafficLocationIdentifier();
    }

    @Override
    public Count osmTraceCount()
    {
        return cellEdge().osmTraceCount();
    }

    @Override
    public Set<EdgeRelation> relations()
    {
        final Set<EdgeRelation> relations = new HashSet<>();
        for (final var relation : super.relations())
        {
            relations.add(new WorldRelation(worldCell, relation));
        }
        return relations;
    }

    @Override
    public WorldEdge reversed()
    {
        if (isTwoWay())
        {
            return new WorldEdge(worldCell, identifier().reversed());
        }
        return null;
    }

    @Override
    public Speed speedLimit()
    {
        return cellEdge().speedLimit();
    }

    @Override
    public Vertex to()
    {
        return new WorldVertex(worldCell, toVertexIdentifier());
    }

    @Override
    public GradeSeparation toGradeSeparation()
    {
        return cellEdge().toGradeSeparation();
    }

    @Override
    public String toString()
    {
        return identifier().toString();
    }

    @Override
    public Access uniDbAccessType()
    {
        return cellEdge().uniDbAccessType();
    }

    @Override
    public AdasRegionCode uniDbAdasRegionCode()
    {
        return cellEdge().uniDbAdasRegionCode();
    }

    @Override
    public ObjectList<AdasZCoordinate> uniDbAdasZCoordinates()
    {
        return cellEdge().uniDbAdasZCoordinates();
    }

    @Override
    public CurvatureHeadingSlopeSequence uniDbCurvatureHeadingSlopeSequence()
    {
        return cellEdge().uniDbCurvatureHeadingSlopeSequence();
    }

    @Override
    public ObjectList<AdasCurvature> uniDbCurvatures()
    {
        return cellEdge().uniDbCurvatures();
    }

    @Override
    public FormOfWay uniDbFormOfWay()
    {
        return cellEdge().uniDbFormOfWay();
    }

    @Override
    public Count uniDbForwardLaneCount()
    {
        return cellEdge().uniDbForwardLaneCount();
    }

    @Override
    public ObjectList<Heading> uniDbHeadings()
    {
        return cellEdge().uniDbHeadings();
    }

    @Override
    public HighwayType uniDbHighwayType()
    {
        return cellEdge().uniDbHighwayType();
    }

    @Override
    public Boolean uniDbIsBuildUpArea()
    {
        return cellEdge().uniDbIsBuildUpArea();
    }

    @Override
    public Boolean uniDbIsComplexIntersection()
    {
        return cellEdge().uniDbIsComplexIntersection();
    }

    @Override
    public Boolean uniDbIsDividedRoad()
    {
        return cellEdge().uniDbIsDividedRoad();
    }

    @Override
    public Boolean uniDbIsLeftSideDriving()
    {
        return cellEdge().uniDbIsLeftSideDriving();
    }

    @Override
    public List<LaneDivider> uniDbLaneDividers()
    {
        return cellEdge().uniDbLaneDividers();
    }

    @Override
    public List<OneWayLane> uniDbLaneOneWays()
    {
        return cellEdge().uniDbLaneOneWays();
    }

    @Override
    public List<Lane> uniDbLaneTypes()
    {
        return cellEdge().uniDbLaneTypes();
    }

    @Override
    public OverpassUnderpassType uniDbOverpassUnderpass()
    {
        return cellEdge().uniDbOverpassUnderpass();
    }

    @Override
    public Speed uniDbReferenceSpeed()
    {
        return cellEdge().uniDbReferenceSpeed();
    }

    @Override
    public Count uniDbReverseLaneCount()
    {
        return cellEdge().uniDbReverseLaneCount();
    }

    @Override
    public RouteType uniDbRouteType()
    {
        return cellEdge().uniDbRouteType();
    }

    @Override
    public ObjectList<Slope> uniDbSlopes()
    {
        return cellEdge().uniDbSlopes();
    }

    @Override
    public SpeedLimitSource uniDbSpeedLimitSource()
    {
        return cellEdge().uniDbSpeedLimitSource();
    }

    @Override
    public List<TurnLane> uniDbTurnLaneArrows()
    {
        return cellEdge().uniDbTurnLaneArrows();
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }

    @Override
    protected Graph subgraph()
    {
        return worldCell.cellGraph();
    }

    private Edge cellEdge()
    {
        return subgraph().edgeForIdentifier(identifierAsLong());
    }
}
