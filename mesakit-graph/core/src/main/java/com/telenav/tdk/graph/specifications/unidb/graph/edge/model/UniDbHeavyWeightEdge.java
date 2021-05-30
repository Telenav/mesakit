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
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.metadata.DataSpecification;
import com.telenav.tdk.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.*;
import com.telenav.tdk.map.measurements.*;
import com.telenav.tdk.map.road.model.*;

import java.util.*;

public class UniDbHeavyWeightEdge extends HeavyWeightEdge
{
    private Access accessType;

    private ObjectList<AdasZCoordinate> adasZCoordinates;

    private Boolean buildUpArea;

    private Boolean complexIntersection;

    private CurvatureHeadingSlopeSequence curvatureHeadingSlopeSequence;

    private Boolean dividedRoad;

    private FormOfWay formOfWay;

    private Count forwardLaneCount;

    private AdasRegionCode forwardRegionCode;

    private HighwayType highwayTag;

    private Boolean isReverseOneWay;

    private List<LaneDivider> laneDividers;

    private List<OneWayLane> laneOneWays;

    private List<Lane> laneTypes;

    private Boolean leftSideDriving;

    private OverpassUnderpassType overpassUnderpass;

    private Count reverseLaneCount;

    private Speed reverseReferenceSpeed;

    private AdasRegionCode reverseRegionCode;

    private RouteType routeType;

    private SpeedLimitSource speedLimitSource;

    private List<TurnLane> turnLanes;

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public UniDbHeavyWeightEdge(final Graph graph, final long identifier)
    {
        super(graph, identifier);
    }

    @Override
    public void copy(final Edge that)
    {
        super.copy(that);
        uniDbAdasZCoordinates(that.uniDbAdasZCoordinates());
        uniDbSpeedLimitSource(that.uniDbSpeedLimitSource());
        uniDbLeftSideDriving(that.uniDbIsLeftSideDriving());
        uniDbAccessType(that.uniDbAccessType());
        uniDbReverseLaneCount(that.uniDbReverseLaneCount());
        uniDbReverseOneWay(that.uniDbIsReverseOneWay());
        uniDbTurnLanes(that.uniDbTurnLaneArrows());
        uniDbLaneOneWays(that.uniDbLaneOneWays());
        uniDbLaneTypes(that.uniDbLaneTypes());
        uniDbLaneDividers(that.uniDbLaneDividers());
        uniDbDividedRoad(that.uniDbIsDividedRoad());
        uniDbBuildUpArea(that.uniDbIsBuildUpArea());
        uniDbComplexIntersection(that.uniDbIsComplexIntersection());
        uniDbFormOfWay(that.uniDbFormOfWay());
        uniDbRouteType(that.uniDbRouteType());
        uniDbCurvatureHeadingSlopeSequence(that.uniDbCurvatureHeadingSlopeSequence());
        uniDbForwardRegionCode(that.uniDbAdasRegionCode());
        uniDbHighwayType(that.uniDbHighwayType());

        // This attribute isn't yet extracted from HERE PBF files in RawPbfGraphLoader -- Shibo
        if (that.supports(UniDbEdgeAttributes.get().OVERPASS_UNDERPASS))
        {
            uniDbOverpassUnderpass(that.uniDbOverpassUnderpass());
        }

        if (that.isTwoWay())
        {
            uniDbReverseRegionCode(that.reversed().uniDbAdasRegionCode());
        }

        if (that.supports(UniDbEdgeAttributes.get().FORWARD_REFERENCE_SPEED))
        {
            referenceSpeed(that.uniDbReferenceSpeed());
            if (that instanceof UniDbHeavyWeightEdge)
            {
                uniDbReverseReferenceSpeed(((UniDbHeavyWeightEdge) that).uniDbReverseReferenceSpeed());
            }
            else
            {
                if (that.isTwoWay())
                {
                    uniDbReverseReferenceSpeed(that.reversed().uniDbReferenceSpeed());
                }
            }
        }
    }

    @Override
    public void populateWithTestValues()
    {
        super.populateWithTestValues();
        uniDbAccessType(new Access(3));
        uniDbAdasZCoordinates(ObjectList.fromIntegers(AdasZCoordinate::centimeters, 0, 1, 2));
        uniDbReverseLaneCount(Count._1);
        uniDbReverseOneWay(false);
        uniDbReverseReferenceSpeed(Speed.milesPerHour(50));
        uniDbBuildUpArea(false);
        uniDbComplexIntersection(false);
        uniDbCurvatureHeadingSlopeSequence(new CurvatureHeadingSlopeSequence()//
                .curvatures(ObjectList.fromIntegers(AdasCurvature::new, 1, 2, 3))
                .headings(ObjectList.fromIntegers(Heading::degrees, 45, 50, 60))
                .slopes(ObjectList.fromIntegers(Slope::degrees, 1, 2, 3)));
        uniDbDividedRoad(false);
        uniDbFormOfWay(FormOfWay.forIdentifier(3));
        uniDbForwardLaneCount(Count._1);
        uniDbHighwayType(HighwayType.RESIDENTIAL);
        uniDbTurnLanes(ObjectList.fromIntegers(TurnLane::new, 2, 3));
        uniDbLaneDividers(ObjectList.fromIntegers(LaneDivider::forIdentifier, 1, 2, 1));
        uniDbLaneOneWays(ObjectList.fromIntegers(OneWayLane::new, 1, 2, 1));
        uniDbLaneTypes(ObjectList.of(
                new Lane()
                        .withLaneType(Lane.Type.SLOW, true)
                        .withLaneType(Lane.Type.BUS, true),
                new Lane()
                        .withLaneType(Lane.Type.HOV, true)
                        .withLaneType(Lane.Type.CENTER, true)));
        uniDbLeftSideDriving(false);
        uniDbOverpassUnderpass(OverpassUnderpassType.OVERPASS);
        uniDbForwardRegionCode(new AdasRegionCode(1));
        uniDbReverseRegionCode(new AdasRegionCode(2));
        uniDbRouteType(RouteType.forBits(5));
        uniDbSpeedLimitSource(new SpeedLimitSource(3));
    }

    public void uniDbAccessType(final Access accessType)
    {
        this.accessType = accessType;
    }

    @Override
    public Access uniDbAccessType()
    {
        return accessType;
    }

    @Override
    public AdasRegionCode uniDbAdasRegionCode()
    {
        return isForward() ? forwardRegionCode : reverseRegionCode;
    }

    public void uniDbAdasZCoordinates(final ObjectList<AdasZCoordinate> coordinates)
    {
        adasZCoordinates = coordinates;
    }

    @Override
    public ObjectList<AdasZCoordinate> uniDbAdasZCoordinates()
    {
        return adasZCoordinates;
    }

    public void uniDbBuildUpArea(final Boolean buildUpArea)
    {
        this.buildUpArea = buildUpArea;
    }

    public void uniDbComplexIntersection(final Boolean complexIntersection)
    {
        this.complexIntersection = complexIntersection;
    }

    public void uniDbCurvatureHeadingSlopeSequence(final CurvatureHeadingSlopeSequence sequence)
    {
        curvatureHeadingSlopeSequence = sequence;
    }

    @Override
    public CurvatureHeadingSlopeSequence uniDbCurvatureHeadingSlopeSequence()
    {
        return curvatureHeadingSlopeSequence;
    }

    @Override
    public ObjectList<AdasCurvature> uniDbCurvatures()
    {
        if (curvatureHeadingSlopeSequence != null)
        {
            return curvatureHeadingSlopeSequence.curvatures();
        }
        return ObjectList.emptyList();
    }

    public void uniDbDividedRoad(final Boolean dividedRoad)
    {
        this.dividedRoad = dividedRoad;
    }

    public void uniDbFormOfWay(final FormOfWay formOfWay)
    {
        this.formOfWay = formOfWay;
    }

    @Override
    public FormOfWay uniDbFormOfWay()
    {
        return formOfWay;
    }

    public void uniDbForwardLaneCount(final Count forwardLaneCount)
    {
        this.forwardLaneCount = forwardLaneCount;
    }

    @Override
    public Count uniDbForwardLaneCount()
    {
        return forwardLaneCount;
    }

    public void uniDbForwardRegionCode(final AdasRegionCode forwardRegionCode)
    {
        this.forwardRegionCode = forwardRegionCode;
    }

    @Override
    public ObjectList<Heading> uniDbHeadings()
    {
        if (curvatureHeadingSlopeSequence != null)
        {
            return curvatureHeadingSlopeSequence.headings();
        }
        return ObjectList.emptyList();
    }

    public void uniDbHighwayType(final HighwayType highwayTag)
    {
        this.highwayTag = highwayTag;
    }

    @Override
    public HighwayType uniDbHighwayType()
    {
        return highwayTag;
    }

    @Override
    public Boolean uniDbIsBuildUpArea()
    {
        return buildUpArea;
    }

    @Override
    public Boolean uniDbIsComplexIntersection()
    {
        return complexIntersection;
    }

    @Override
    public Boolean uniDbIsDividedRoad()
    {
        return dividedRoad;
    }

    @Override
    public Boolean uniDbIsLeftSideDriving()
    {
        return leftSideDriving;
    }

    public void uniDbIsReverseOneWay(final Boolean isReverseOneWay)
    {
        this.isReverseOneWay = isReverseOneWay;
    }

    @Override
    public Boolean uniDbIsReverseOneWay()
    {
        return Objects.requireNonNullElse(isReverseOneWay, false);
    }

    public void uniDbLaneDividers(final List<LaneDivider> laneDividers)
    {
        this.laneDividers = laneDividers;
    }

    @Override
    public List<LaneDivider> uniDbLaneDividers()
    {
        return laneDividers;
    }

    public void uniDbLaneOneWays(final List<OneWayLane> laneOneWays)
    {
        this.laneOneWays = laneOneWays;
    }

    @Override
    public List<OneWayLane> uniDbLaneOneWays()
    {
        return laneOneWays;
    }

    public void uniDbLaneTypes(final List<Lane> laneTypes)
    {
        this.laneTypes = laneTypes;
    }

    @Override
    public List<Lane> uniDbLaneTypes()
    {
        return laneTypes;
    }

    public void uniDbLeftSideDriving(final Boolean leftSideDriving)
    {
        this.leftSideDriving = leftSideDriving;
    }

    public void uniDbOverpassUnderpass(final OverpassUnderpassType overpassUnderpass)
    {
        this.overpassUnderpass = overpassUnderpass;
    }

    @Override
    public OverpassUnderpassType uniDbOverpassUnderpass()
    {
        return overpassUnderpass;
    }

    public void uniDbReverseLaneCount(final Count reverseLaneCount)
    {
        this.reverseLaneCount = reverseLaneCount;
    }

    @Override
    public Count uniDbReverseLaneCount()
    {
        return reverseLaneCount;
    }

    public void uniDbReverseOneWay(final Boolean isReverseOneWay)
    {
        this.isReverseOneWay = isReverseOneWay;
    }

    @Override
    public Speed uniDbReverseReferenceSpeed()
    {
        return reverseReferenceSpeed;
    }

    @Override
    public void uniDbReverseReferenceSpeed(final Speed reverseSpeedLimit)
    {
        reverseReferenceSpeed = reverseSpeedLimit;
    }

    public AdasRegionCode uniDbReverseRegionCode()
    {
        return reverseRegionCode;
    }

    public void uniDbReverseRegionCode(final AdasRegionCode reverseRegionCode)
    {
        this.reverseRegionCode = reverseRegionCode;
    }

    public void uniDbRouteType(final RouteType routeType)
    {
        this.routeType = routeType;
    }

    @Override
    public RouteType uniDbRouteType()
    {
        return routeType;
    }

    @Override
    public ObjectList<Slope> uniDbSlopes()
    {
        if (curvatureHeadingSlopeSequence != null)
        {
            return curvatureHeadingSlopeSequence.slopes();
        }
        return ObjectList.emptyList();
    }

    public void uniDbSpeedLimitSource(final SpeedLimitSource speedLimitSource)
    {
        this.speedLimitSource = speedLimitSource;
    }

    @Override
    public SpeedLimitSource uniDbSpeedLimitSource()
    {
        return speedLimitSource;
    }

    @Override
    public List<TurnLane> uniDbTurnLaneArrows()
    {
        return turnLanes;
    }

    public void uniDbTurnLanes(final List<TurnLane> laneArrows)
    {
        turnLanes = laneArrows;
    }
}
