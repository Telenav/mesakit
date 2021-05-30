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

package com.telenav.tdk.graph.specifications.unidb.graph.edge.store;

import com.telenav.tdk.core.collections.primitive.array.packed.SplitPackedArray;
import com.telenav.tdk.core.collections.primitive.array.scalars.SplitByteArray;
import com.telenav.tdk.core.collections.primitive.map.multi.fixed.*;
import com.telenav.tdk.core.collections.primitive.set.SplitLongSet;
import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.scalars.counts.*;
import com.telenav.tdk.core.resource.compression.archive.TdkArchivedField;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.tdk.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.tdk.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.tdk.graph.specifications.library.attributes.AttributeReference;
import com.telenav.tdk.graph.specifications.unidb.UniDbDataSpecification;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.*;
import com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes.*;
import com.telenav.tdk.map.measurements.*;
import com.telenav.tdk.map.road.model.*;

import java.util.List;

import static com.telenav.tdk.core.collections.primitive.array.packed.PackedPrimitiveArray.OverflowHandling.*;

/**
 * Store of edge attributes that are specific to the {@link UniDbDataSpecification}.
 *
 * @author jonathanl (shibo)
 * @see EdgeAttributes
 * @see EdgeStore
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings("unused")
public final class UniDbEdgeStore extends EdgeStore
{
    private final AttributeReference<SplitPackedArray> ACCESS_TYPE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().ACCESS_TYPE, "accessType",
                    () -> (SplitPackedArray) new SplitPackedArray("accessType")
                            .bits(Bits._11, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray accessType;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> ADAS_Z_COORDINATES =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().ADAS_Z_COORDINATES, "adasZCoordinates",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("adasZCoordinates")
                            .bits(Bits._20, ALLOW_OVERFLOW)
                            .listTerminator(-500_00) // The lowest road in the world is higher than 500 meters
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap adasZCoordinates;

    private final AttributeReference<SplitPackedArray> REVERSE_LANE_COUNT =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().REVERSE_LANE_COUNT, "reverseLaneCount",
                    () -> (SplitPackedArray) new SplitPackedArray("reverseLaneCount")
                            .bits(Bits._5, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray reverseLaneCount;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> CURVATURES =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().CURVATURES, "curvatures",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("curvatures")
                            .bits(Bits._10, ALLOW_OVERFLOW)
                            .listTerminator(AdasCurvature.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap curvatures;

    private final AttributeReference<SplitPackedArray> FORM_OF_WAY =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().FORM_OF_WAY, "formOfWay",
                    () -> (SplitPackedArray) new SplitPackedArray("formOfWay")
                            .bits(Bits._5, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray formOfWay;

    private final AttributeReference<SplitPackedArray> FORWARD_LANE_COUNT =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().FORWARD_LANE_COUNT, "forwardLaneCount",
                    () -> (SplitPackedArray) new SplitPackedArray("forwardLaneCount")
                            .bits(Bits._5, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray forwardLaneCount;

    private final AttributeReference<SplitByteArray> FORWARD_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().FORWARD_REFERENCE_SPEED, "forwardReferenceSpeedInKilometersPerHour",
                    () -> (SplitByteArray) new SplitByteArray("forwardReferenceSpeedInKilometersPerHour")
                            .nullByte(Byte.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitByteArray forwardReferenceSpeedInKilometersPerHour;

    private final AttributeReference<SplitPackedArray> FORWARD_REGION_CODE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().FORWARD_REGION_CODE, "forwardRegionCode",
                    () -> (SplitPackedArray) new SplitPackedArray("forwardRegionCode")
                            .bits(Bits._16, ALLOW_OVERFLOW)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray forwardRegionCode;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> HEADINGS =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().HEADINGS, "headings",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("headings")
                            .bits(Bits._9, ALLOW_OVERFLOW)
                            .listTerminator(Heading.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap headings;

    private final AttributeReference<SplitByteArray> HIGHWAY_TYPE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().HIGHWAY_TYPE, "highwayType",
                    () -> (SplitByteArray) new SplitByteArray("highwayType")
                            .nullByte(Byte.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitByteArray highwayType;

    private final AttributeReference<SplitPackedArray> IS_BUILD_UP_AREA =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().IS_BUILD_UP_AREA, "isBuildUpArea",
                    () -> (SplitPackedArray) new SplitPackedArray("isBuildUpArea")
                            .bits(Bits._1, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray isBuildUpArea;

    private final AttributeReference<SplitLongSet> IS_COMPLEX_INTERSECTION =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().IS_COMPLEX_INTERSECTION, "isComplexIntersection",
                    () -> new SplitLongSet("isComplexIntersection"));

    @TdkArchivedField
    private SplitLongSet isComplexIntersection;

    private final AttributeReference<SplitPackedArray> IS_DIVIDED_ROAD =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().IS_DIVIDED_ROAD, "isDividedRoad",
                    () -> (SplitPackedArray) new SplitPackedArray("isDividedRoad")
                            .bits(Bits._1, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray isDividedRoad;

    private final AttributeReference<SplitLongSet> IS_REVERSE_ONE_WAY =
            new AttributeReference<>(this, EdgeAttributes.get().IS_REVERSE_ONE_WAY, "isReverseOneWay",
                    () -> new SplitLongSet("isReverseOneWay"));

    @TdkArchivedField
    private SplitLongSet isReverseOneWay;

    private final AttributeReference<SplitPackedArray> IS_LEFT_SIDE_DRIVING =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().IS_LEFT_SIDE_DRIVING, "isLeftSideDriving",
                    () -> (SplitPackedArray) new SplitPackedArray("isLeftSideDriving")
                            .bits(Bits._1, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray isLeftSideDriving;

    private final AttributeReference<SplitPackedArray> SPEED_LIMIT_SOURCE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().SPEED_LIMIT_SOURCE, "speedLimitSource",
                    () -> (SplitPackedArray) new SplitPackedArray("speedLimitSource")
                            .bits(Bits._2, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray speedLimitSource;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> TURN_LANE_ARROWS =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().TURN_LANE_ARROWS, "turnLaneArrows",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("turnLaneArrows")
                            .bits(Bits._16, NO_OVERFLOW)
                            .listTerminator(TurnLane.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap turnLaneArrows;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> LANE_ONE_WAYS =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().LANE_ONE_WAYS, "laneOneWays",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("laneOneWays")
                            .bits(Bits._2, NO_OVERFLOW)
                            .listTerminator(OneWayLane.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap laneOneWays;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> LANE_TYPES =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().LANE_TYPES, "laneTypes",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("laneTypes")
                            .bits(Bits._18, NO_OVERFLOW)
                            .listTerminator(Lane.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap laneTypes;

    private final AttributeReference<IntToPackedArrayFixedMultiMap> LANE_DIVIDERS =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().LANE_DIVIDERS, "laneDividers",
                    () -> (IntToPackedArrayFixedMultiMap) new IntToPackedArrayFixedMultiMap("laneDividers")
                            .bits(Bits._4, NO_OVERFLOW)
                            .listTerminator(LaneDivider.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToPackedArrayFixedMultiMap laneDividers;

    private final AttributeReference<SplitPackedArray> OVERPASS_UNDERPASS =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().OVERPASS_UNDERPASS, "overpassUnderpass",
                    () -> (SplitPackedArray) new SplitPackedArray("overpassUnderpass")
                            .bits(Bits._2, NO_OVERFLOW)
                            .hasNullLong(false)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray overpassUnderpass;

    private final AttributeReference<SplitByteArray> REVERSE_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().REVERSE_REFERENCE_SPEED, "reverseReferenceSpeedInKilometersPerHour",
                    () -> (SplitByteArray) new SplitByteArray("reverseReferenceSpeedInKilometersPerHour")
                            .nullByte(Byte.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitByteArray reverseReferenceSpeedInKilometersPerHour;

    private final AttributeReference<SplitPackedArray> REVERSE_REGION_CODE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().REVERSE_REGION_CODE, "reverseRegionCode",
                    () -> (SplitPackedArray) new SplitPackedArray("reverseRegionCode")
                            .bits(Bits._16, ALLOW_OVERFLOW)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray reverseRegionCode;

    private final AttributeReference<SplitPackedArray> ROUTE_TYPE =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().ROUTE_TYPE, "routeType",
                    () -> (SplitPackedArray) new SplitPackedArray("routeType")
                            .bits(Bits._5, NO_OVERFLOW)
                            .nullLong(RouteType.NULL)
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private SplitPackedArray routeType;

    private final AttributeReference<IntToByteFixedMultiMap> SLOPES =
            new AttributeReference<>(this, UniDbEdgeAttributes.get().SLOPES, "slopes",
                    () -> (IntToByteFixedMultiMap) new IntToByteFixedMultiMap("slopes")
                            .initialSize(estimatedElements()));

    @TdkArchivedField
    private IntToByteFixedMultiMap slopes;

    public UniDbEdgeStore(final Graph graph)
    {
        super(graph);
    }

    public Access retrieveAccessType(final Edge edge)
    {
        return ACCESS_TYPE.retrieveObject(edge, value -> new Access((int) value));
    }

    public ObjectList<AdasZCoordinate> retrieveAdasZCoordinates(final Edge edge)
    {
        return ADAS_Z_COORDINATES.retrieveSignedObjectList(edge, centimeters -> AdasZCoordinate.centimeters((int) centimeters))
                .maybeReversed(edge.isReverse());
    }

    public Boolean retrieveBuildUpArea(final Edge edge)
    {
        return IS_BUILD_UP_AREA.retrieveBoolean(edge);
    }

    public Boolean retrieveComplexIntersection(final Edge edge)
    {
        return IS_COMPLEX_INTERSECTION.retrieveBoolean(edge);
    }

    public CurvatureHeadingSlopeSequence retrieveCurvatureHeadingSlopeSequence(final Edge edge)
    {
        final var curvatures = this.curvatures.get(edge.index());
        final var headings = this.headings.get(edge.index());
        final var slopes = this.slopes.get(edge.index());

        return (curvatures == null || headings == null || slopes == null) ? null :
                new CurvatureHeadingSlopeSequence()
                        .curvatures(curvatures.asArray())
                        .headings(headings.asArray())
                        .slopes(slopes.asArray());
    }

    public ObjectList<AdasCurvature> retrieveCurvatures(final Edge edge)
    {
        return CURVATURES.retrieveObjectList(edge, value -> new AdasCurvature((int) value))
                .maybeReversed(edge.isReverse());
    }

    public Boolean retrieveDividedRoad(final Edge edge)
    {
        return IS_DIVIDED_ROAD.retrieveBoolean(edge);
    }

    public FormOfWay retrieveFormOfWay(final Edge edge)
    {
        return FORM_OF_WAY.retrieveObject(edge, value -> FormOfWay.forIdentifier((int) value));
    }

    public Count retrieveForwardLaneCount(final Edge edge)
    {
        if (edge.isReverse())
        {
            return REVERSE_LANE_COUNT.retrieveObject(edge, Count::of);
        }
        return FORWARD_LANE_COUNT.retrieveObject(edge, Count::of);
    }

    public ObjectList<Heading> retrieveHeadings(final Edge edge)
    {
        return HEADINGS.retrieveObjectList(edge, Heading::degrees)
                .maybeReversed(edge.isReverse());
    }

    public HighwayType retrieveHighwayType(final Edge edge)
    {
        return HIGHWAY_TYPE.retrieveObject(edge, HighwayType::forIdentifier);
    }

    public final Boolean retrieveIsReverseOneWay(final Edge edge)
    {
        return IS_REVERSE_ONE_WAY.retrieveBoolean(edge);
    }

    public List<LaneDivider> retrieveLaneDividers(final Edge edge)
    {
        return LANE_DIVIDERS.retrieveObjectList(edge, value -> LaneDivider.forIdentifier((int) value))
                .maybeReversed(edge.isReverse());
    }

    public ObjectList<OneWayLane> retrieveLaneOneWays(final Edge edge)
    {
        return LANE_ONE_WAYS.retrieveObjectList(edge, value -> new OneWayLane((int) value))
                .maybeReversed(edge.isReverse());
    }

    public List<Lane> retrieveLaneTypes(final Edge edge)
    {
        return LANE_TYPES.retrieveObjectList(edge, value -> new Lane((int) value))
                .maybeReversed(edge.isReverse());
    }

    public Boolean retrieveLeftSideDriving(final Edge edge)
    {
        return IS_LEFT_SIDE_DRIVING.retrieveBoolean(edge);
    }

    public OverpassUnderpassType retrieveOverpassUnderpass(final Edge edge)
    {
        return OVERPASS_UNDERPASS.retrieveObject(edge, value -> OverpassUnderpassType.forIdentifier((int) value));
    }

    public Speed retrieveReferenceSpeed(final Edge edge)
    {
        if (edge.isForward())
        {
            return FORWARD_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR.retrieveObject(edge, Speed::kilometersPerHour);
        }
        else
        {
            return REVERSE_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR.retrieveObject(edge, Speed::kilometersPerHour);
        }
    }

    public AdasRegionCode retrieveRegionCode(final Edge edge)
    {
        final var data = edge.isForward() ? FORWARD_REGION_CODE : REVERSE_REGION_CODE;
        return data.retrieveObject(edge, code -> new AdasRegionCode((int) code));
    }

    public Count retrieveReverseLaneCount(final Edge edge)
    {
        if (edge.isReverse())
        {
            return FORWARD_LANE_COUNT.retrieveObject(edge, Count::of);
        }
        return REVERSE_LANE_COUNT.retrieveObject(edge, Count::of);
    }

    public Speed retrieveReverseReferenceSpeed(final Edge edge)
    {
        return REVERSE_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR.retrieveObject(edge, Speed::kilometersPerHour);
    }

    public RouteType retrieveRouteType(final Edge edge)
    {
        return ROUTE_TYPE.retrieveObject(edge, value -> RouteType.forBits((int) value));
    }

    public ObjectList<Slope> retrieveSlopes(final Edge edge)
    {
        return SLOPES.retrieveSignedObjectList(edge, Slope::degrees).maybeReversed(edge.isReverse());
    }

    public SpeedLimitSource retrieveSpeedLimitSource(final Edge edge)
    {
        return SPEED_LIMIT_SOURCE.retrieveObject(edge, value -> new SpeedLimitSource((int) value));
    }

    public List<TurnLane> retrieveTurnLanes(final Edge edge)
    {
        return TURN_LANE_ARROWS.retrieveObjectList(edge, value -> new TurnLane((int) value))
                .maybeReversed(edge.isReverse());
    }

    /**
     * Stores all of the simple attributes of the given edge at the given edge index
     */

    @Override
    public void storeAttributes(final Edge uncast)
    {
        super.storeAttributes(uncast);

        final var edge = (UniDbHeavyWeightEdge) uncast;

        ACCESS_TYPE.storeObject(edge, edge.uniDbAccessType());
        ADAS_Z_COORDINATES.storeObjectList(edge, edge.uniDbAdasZCoordinates());
        IS_BUILD_UP_AREA.storeBoolean(edge, edge.uniDbIsBuildUpArea());
        IS_COMPLEX_INTERSECTION.storeBoolean(edge, edge.uniDbIsComplexIntersection());
        CURVATURES.storeObjectList(edge, edge.uniDbCurvatures());
        IS_DIVIDED_ROAD.storeBoolean(edge, edge.uniDbIsDividedRoad());
        FORM_OF_WAY.storeObject(edge, edge.uniDbFormOfWay());
        HEADINGS.storeObjectList(edge, edge.uniDbHeadings());
        IS_REVERSE_ONE_WAY.storeBoolean(edge, edge.uniDbIsReverseOneWay());
        TURN_LANE_ARROWS.storeObjectList(edge, edge.uniDbTurnLaneArrows());
        LANE_DIVIDERS.storeObjectList(edge, edge.uniDbLaneDividers());
        LANE_ONE_WAYS.storeObjectList(edge, edge.uniDbLaneOneWays());
        LANE_TYPES.storeObjectList(edge, edge.uniDbLaneTypes());
        IS_LEFT_SIDE_DRIVING.storeBoolean(edge, edge.uniDbIsLeftSideDriving());
        OVERPASS_UNDERPASS.storeObject(edge, edge.uniDbOverpassUnderpass());
        ROUTE_TYPE.storeObject(edge, edge.uniDbRouteType());
        SLOPES.storeObjectList(edge, edge.uniDbSlopes());
        SPEED_LIMIT_SOURCE.storeObject(edge, edge.uniDbSpeedLimitSource());
        HIGHWAY_TYPE.storeObject(edge, edge.uniDbHighwayType());

        FORWARD_REGION_CODE.storeObject(edge, edge.uniDbAdasRegionCode());
        FORWARD_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR.storeObject(edge, edge.uniDbReferenceSpeed());
        FORWARD_LANE_COUNT.storeObject(edge, edge.uniDbForwardLaneCount());

        if (edge.isTwoWay())
        {
            REVERSE_LANE_COUNT.storeObject(edge, edge.uniDbReverseLaneCount());
            REVERSE_REGION_CODE.storeObject(edge, edge.uniDbReverseRegionCode());
            REVERSE_REFERENCE_SPEED_IN_KILOMETERS_PER_HOUR.storeObject(edge, edge.uniDbReverseReferenceSpeed());
        }
    }
}
