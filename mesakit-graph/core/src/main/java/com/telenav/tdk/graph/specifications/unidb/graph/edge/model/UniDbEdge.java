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

package com.telenav.tdk.graph.specifications.unidb.graph.edge.model;

import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.validation.*;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.identifiers.EdgeIdentifier;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.*;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.store.UniDbEdgeStore;
import com.telenav.tdk.map.measurements.*;
import com.telenav.tdk.map.road.model.*;

import java.util.List;

public class UniDbEdge extends Edge
{
    public UniDbEdge(final Graph graph, final EdgeIdentifier identifier)
    {
        super(graph, identifier);
    }

    public UniDbEdge(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    public UniDbEdge(final Graph graph, final long identifier, final int index)
    {
        super(graph, identifier, index);
    }

    @Override
    public boolean isIntersectionEdge()
    {
        return false;
    }

    @Override
    public Access uniDbAccessType()
    {
        return store().retrieveAccessType(this);
    }

    @Override
    public AdasRegionCode uniDbAdasRegionCode()
    {
        return store().retrieveRegionCode(this);
    }

    @Override
    public ObjectList<AdasZCoordinate> uniDbAdasZCoordinates()
    {
        return store().retrieveAdasZCoordinates(this);
    }

    @Override
    public CurvatureHeadingSlopeSequence uniDbCurvatureHeadingSlopeSequence()
    {
        return store().retrieveCurvatureHeadingSlopeSequence(this);
    }

    @Override
    public ObjectList<AdasCurvature> uniDbCurvatures()
    {
        return store().retrieveCurvatures(this);
    }

    @Override
    public FormOfWay uniDbFormOfWay()
    {
        return store().retrieveFormOfWay(this);
    }

    @Override
    public Count uniDbForwardLaneCount()
    {
        return store().retrieveForwardLaneCount(this);
    }

    @Override
    public ObjectList<Heading> uniDbHeadings()
    {
        return store().retrieveHeadings(this);
    }

    @Override
    public HighwayType uniDbHighwayType()
    {
        return store().retrieveHighwayType(this);
    }

    @Override
    public Boolean uniDbIsBuildUpArea()
    {
        return store().retrieveBuildUpArea(this);
    }

    @Override
    public Boolean uniDbIsComplexIntersection()
    {
        return store().retrieveComplexIntersection(this);
    }

    @Override
    public Boolean uniDbIsDividedRoad()
    {
        return store().retrieveDividedRoad(this);
    }

    @Override
    public Boolean uniDbIsLeftSideDriving()
    {
        return store().retrieveLeftSideDriving(this);
    }

    @Override
    public Boolean uniDbIsReverseOneWay()
    {
        return store().retrieveIsReverseOneWay(this);
    }

    @Override
    public List<LaneDivider> uniDbLaneDividers()
    {
        return store().retrieveLaneDividers(this);
    }

    @Override
    public List<OneWayLane> uniDbLaneOneWays()
    {
        return store().retrieveLaneOneWays(this);
    }

    @Override
    public List<Lane> uniDbLaneTypes()
    {
        return store().retrieveLaneTypes(this);
    }

    @Override
    public OverpassUnderpassType uniDbOverpassUnderpass()
    {
        return store().retrieveOverpassUnderpass(this);
    }

    /**
     * @return The maximum historical speed for this edge as computed by Telenav
     */
    @Override
    public Speed uniDbReferenceSpeed()
    {
        return store().retrieveReferenceSpeed(this);
    }

    @Override
    public Count uniDbReverseLaneCount()
    {
        return store().retrieveReverseLaneCount(this);
    }

    @Override
    public RouteType uniDbRouteType()
    {
        return store().retrieveRouteType(this);
    }

    @Override
    public ObjectList<Slope> uniDbSlopes()
    {
        return store().retrieveSlopes(this);
    }

    @Override
    public SpeedLimitSource uniDbSpeedLimitSource()
    {
        return store().retrieveSpeedLimitSource(this);
    }

    @Override
    public List<TurnLane> uniDbTurnLaneArrows()
    {
        return store().retrieveTurnLanes(this);
    }

    @Override
    public Validator validator(final Validation type)
    {
        return new ElementValidator()
        {
            @Override
            protected void onValidate()
            {
                // Validate the superclass
                validate(UniDbEdge.super.validator(type));

                // then check for issues
                quibbleIf(uniDbAccessType() == null, "accessType is missing");
                quibbleIf(isEmpty(uniDbAdasZCoordinates()), "the list of adasZCoordinates is empty");
                quibbleIf(uniDbReverseLaneCount() == null, "backwardLaneCount is missing");
                quibbleIf(uniDbIsBuildUpArea() == null, "buildUpArea is missing");
                quibbleIf(uniDbCurvatureHeadingSlopeSequence() == null, "curvatureHeadingSlopeSequence is missing");
                quibbleIf(isEmpty(uniDbCurvatures()), "curvatures is empty");
                quibbleIf(uniDbIsDividedRoad() == null, "dividedRoad is missing");
                quibbleIf(uniDbForwardLaneCount() == null, "forwardLaneCount is missing");
                quibbleIf(isEmpty(uniDbHeadings()), "the list of headings is empty");
                quibbleIf(uniDbHighwayType() == null, "highwayTag is missing or empty");
                quibbleIf(uniDbIsReverseOneWay() == null, "isBackwardOneWay is missing");
                quibbleIf(isEmpty(uniDbTurnLaneArrows()), "laneArrows is empty");
                quibbleIf(isEmpty(uniDbLaneDividers()), "laneDividers is empty");
                quibbleIf(isEmpty(uniDbLaneOneWays()), "laneOneWays is empty");
                quibbleIf(uniDbIsLeftSideDriving(), "leftSideDriving is empty");
                quibbleIf(uniDbReferenceSpeed() == null, "referenceSpeed is missing");
                quibbleIf(uniDbAdasRegionCode() == null, "regionCodes is missing");
                quibbleIf(isEmpty(uniDbSlopes()), "list of slopes is empty");
                quibbleIf(uniDbSpeedLimitSource() == null, "speedLimitSource is missing");
            }
        };
    }

    @Override
    protected UniDbEdgeStore store()
    {
        return (UniDbEdgeStore) graph().edgeStore();
    }
}
