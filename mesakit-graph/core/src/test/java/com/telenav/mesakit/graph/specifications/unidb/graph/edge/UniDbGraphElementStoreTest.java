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

package com.telenav.mesakit.graph.specifications.unidb.graph.edge;

import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Place.Type;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.project.GraphCoreUnitTest;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.graph.specifications.unidb.graph.UniDbGraph;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.UniDbHeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.Access;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasCurvature;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasRegionCode;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasZCoordinate;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.CurvatureHeadingSlopeSequence;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.FormOfWay;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.Lane;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.LaneDivider;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.OneWayLane;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.RouteType;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.SpeedLimitSource;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.TurnLane;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.polyline.*;
import com.telenav.mesakit.map.road.model.HighwayType;
import com.telenav.mesakit.map.road.model.OverpassUnderpassType;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadType;
import com.telenav.mesakit.map.road.model.SpeedCategory;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class UniDbGraphElementStoreTest extends GraphCoreUnitTest
{
    private static final long EDGE_IDENTIFIER = 666_000_000;

    @Test
    public void testAdasZCoordinates()
    {
        // Create and store edge
        final var graph = uniDbGraph();
        final UniDbHeavyWeightEdge edge = heavyWeightEdge(graph);
        edge.populateWithTestValues();
        final var coordinates = adasZCoordinates();
        edge.uniDbAdasZCoordinates(coordinates);
        graph.edgeStore().adder().add(edge);
        graph.edgeStore().flush();

        // Retrieve and check forward edge
        final Edge forwardEdge = graph.edgeForIdentifier(new EdgeIdentifier(EDGE_IDENTIFIER));
        ensureEqual(1, forwardEdge.index());
        final List<AdasZCoordinate> forwardAdasZCoordinates = forwardEdge.uniDbAdasZCoordinates();
        for (var i = 0; i < coordinates.size(); i++)
        {
            ensureEqual(coordinates.get(i), forwardAdasZCoordinates.get(i));
        }

        // Retrieve and check reverse edge
        final Edge reverseEdge = forwardEdge.reversed();
        final List<AdasZCoordinate> reverseAdasZCoordinates = reverseEdge.uniDbAdasZCoordinates();
        Collections.reverse(coordinates);
        for (var i = 0; i < coordinates.size(); i++)
        {
            ensureEqual(coordinates.get(i), reverseAdasZCoordinates.get(i));
        }
    }

    @Test
    public void testOverpassUnderpassIsNull()
    {
        final var graph = uniDbGraph();
        final UniDbHeavyWeightEdge edge = heavyWeightEdge(graph);
        edge.populateWithTestValues();
        edge.uniDbOverpassUnderpass(null);
        graph.edgeStore().adder().add(edge);
        graph.edgeStore().flush();

        final Edge forwardEdge = graph.edgeForIdentifier(new EdgeIdentifier(EDGE_IDENTIFIER));
        ensureEqual(OverpassUnderpassType.NONE, forwardEdge.uniDbOverpassUnderpass());
    }

    @Test
    public void testOverpassUnderpassIsOverpass()
    {
        final var graph = uniDbGraph();
        final UniDbHeavyWeightEdge edge = heavyWeightEdge(graph);
        edge.populateWithTestValues();

        edge.uniDbOverpassUnderpass(OverpassUnderpassType.OVERPASS);
        ensure(graph.edgeStore().adder().add(edge));
        graph.edgeStore().flush();

        final Edge forwardEdge = graph.edgeForIdentifier(new EdgeIdentifier(EDGE_IDENTIFIER));
        ensureEqual(OverpassUnderpassType.OVERPASS, forwardEdge.uniDbOverpassUnderpass());
    }

    @Test
    public void testPlace()
    {
        final var graph = uniDbGraph();
        final HeavyWeightPlace place = OsmDataSpecification.get().newHeavyWeightPlace(graph, 100);
        place.location(Location.degrees(20, 20));
        place.name("123");
        place.type(Type.CITY);
        place.population(Count._100);
        ensure(graph.placeStore().adder().add(place));
        graph.placeStore().flush();
        ensure(graph.contains(new PlaceIdentifier(100)));
    }

    @Test
    public void testUniDb()
    {
        final var graph = uniDbGraph();
        final UniDbHeavyWeightEdge edge = heavyWeightEdge(graph);
        edge.populateWithTestValues();

        edge.uniDbSpeedLimitSource(new SpeedLimitSource(1));
        edge.uniDbLeftSideDriving(true);
        edge.uniDbAccessType(new Access(2));
        edge.uniDbReverseLaneCount(Count.count(3));
        edge.uniDbTurnLanes(laneArrows());
        edge.uniDbLaneOneWays(laneOneWays());
        edge.uniDbLaneTypes(laneTypes());
        edge.uniDbLaneDividers(laneDividers());
        edge.uniDbDividedRoad(true);
        edge.uniDbBuildUpArea(true);
        edge.uniDbComplexIntersection(true);
        edge.uniDbFormOfWay(FormOfWay.forIdentifier(4));
        edge.uniDbRouteType(RouteType.forBits(5));
        edge.lastModificationTime(Time.now());
        edge.uniDbCurvatureHeadingSlopeSequence(new CurvatureHeadingSlopeSequence()
                .curvatures(curvatures())
                .headings(headings())
                .slopes(slopes()));
        edge.uniDbForwardRegionCode(new AdasRegionCode(1));
        edge.uniDbReverseRegionCode(new AdasRegionCode(2));
        edge.uniDbHighwayType(HighwayType.MOTORWAY);
        graph.edgeStore().adder().add(edge);
        graph.edgeStore().flush();

        final var valueList = integers();
        final var twoValues = valueList.leftOf(2);
        final Edge forwardEdge = graph.edgeForIdentifier(new EdgeIdentifier(EDGE_IDENTIFIER));
        final Integer forwardSpeedLimitSource = (int) (forwardEdge.uniDbSpeedLimitSource().quantum());
        ensureEqual(1, forwardSpeedLimitSource);
        final Boolean forwardLeftSideDriving = forwardEdge.uniDbIsLeftSideDriving();
        ensure(forwardLeftSideDriving);
        final Integer forwardAccessType = (int) forwardEdge.uniDbAccessType().quantum();
        ensureEqual(2, forwardAccessType);
        final Count reverseLaneCount = forwardEdge.uniDbReverseLaneCount();
        ensureEqual(3, reverseLaneCount.asInt());
        final List<TurnLane> forwardLaneArrows = forwardEdge.uniDbTurnLaneArrows();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new TurnLane(valueList.get(i)), forwardLaneArrows.get(i));
        }
        final List<OneWayLane> forwardLaneOneways = forwardEdge.uniDbLaneOneWays();
        for (var i = 0; i < twoValues.size(); i++)
        {
            ensureEqual(new OneWayLane(twoValues.get(i)), forwardLaneOneways.get(i));
        }
        final List<Lane> forwardLaneTypes = forwardEdge.uniDbLaneTypes();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new Lane(valueList.get(i)), forwardLaneTypes.get(i));
        }
        final List<LaneDivider> forwardLaneDividers = forwardEdge.uniDbLaneDividers();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(LaneDivider.forIdentifier(valueList.get(i)), forwardLaneDividers.get(i));
        }
        final Boolean forwardDividedRoad = forwardEdge.uniDbIsDividedRoad();
        ensure(forwardDividedRoad);
        final Boolean forwardBuildUpArea = forwardEdge.uniDbIsBuildUpArea();
        ensure(forwardBuildUpArea);
        final Boolean forwardComplexIntersection = forwardEdge.uniDbIsComplexIntersection();
        ensure(forwardComplexIntersection);
        final Integer forwardFormOfWay = (int) forwardEdge.uniDbFormOfWay().quantum();
        ensureEqual(4, forwardFormOfWay);
        final Integer forwardRouteType = (int) forwardEdge.uniDbRouteType().quantum();
        ensureEqual(5, forwardRouteType);
        final List<AdasCurvature> forwardCurvatures = forwardEdge.uniDbCurvatures();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new AdasCurvature(valueList.get(i)), forwardCurvatures.get(i));
        }
        final List<Heading> forwardHeadings = forwardEdge.uniDbHeadings();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(Heading.degrees(valueList.get(i)), forwardHeadings.get(i));
        }
        final List<Slope> forwardSlopes = forwardEdge.uniDbSlopes();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(Slope.degrees(valueList.get(i)), forwardSlopes.get(i));
        }
        final AdasRegionCode forwardRegionCode = forwardEdge.uniDbAdasRegionCode();
        ensureEqual(new AdasRegionCode(1), forwardRegionCode);

        ensureEqual(HighwayType.MOTORWAY, forwardEdge.uniDbHighwayType());

        Collections.reverse(valueList);
        final Edge reverseEdge = graph.edgeForIdentifier(new EdgeIdentifier(-EDGE_IDENTIFIER));
        final Integer reverseSpeedLimitSource = (int) reverseEdge.uniDbSpeedLimitSource().quantum();
        ensureEqual(1, reverseSpeedLimitSource);
        final Boolean reverseLeftSideDriving = reverseEdge.uniDbIsLeftSideDriving();
        ensure(reverseLeftSideDriving);
        final Integer reverseAccessType = (int) reverseEdge.uniDbAccessType().quantum();
        ensureEqual(2, reverseAccessType);
        final Count reverseReverseLaneCount = reverseEdge.uniDbReverseLaneCount();
        ensureEqual(1, reverseReverseLaneCount.asInt());
        final List<TurnLane> reverseLaneArrows = reverseEdge.uniDbTurnLaneArrows();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new TurnLane(valueList.get(i)), reverseLaneArrows.get(i));
        }
        final List<OneWayLane> reverseLaneOneways = reverseEdge.uniDbLaneOneWays();
        ensureEqual(new OneWayLane(2), reverseLaneOneways.get(0));
        ensureEqual(new OneWayLane(1), reverseLaneOneways.get(1));
        final List<Lane> reverseLaneTypes = reverseEdge.uniDbLaneTypes();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new Lane(valueList.get(i)), reverseLaneTypes.get(i));
        }
        final List<LaneDivider> reverseLaneDividers = reverseEdge.uniDbLaneDividers();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(LaneDivider.forIdentifier(valueList.get(i)), reverseLaneDividers.get(i));
        }
        final Boolean reverseDividedRoad = reverseEdge.uniDbIsDividedRoad();
        ensure(reverseDividedRoad);
        final Boolean reverseBuildUpArea = reverseEdge.uniDbIsBuildUpArea();
        ensure(reverseBuildUpArea);
        final Boolean reverseComplexIntersection = reverseEdge.uniDbIsComplexIntersection();
        ensure(reverseComplexIntersection);
        final FormOfWay reverseFormOfWay = reverseEdge.uniDbFormOfWay();
        ensureEqual(FormOfWay.forIdentifier(4), reverseFormOfWay);
        final RouteType reverseRouteType = reverseEdge.uniDbRouteType();
        ensureEqual(RouteType.forBits(5), reverseRouteType);
        final List<AdasCurvature> reverseCurvatures = reverseEdge.uniDbCurvatures();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(new AdasCurvature(valueList.get(i)), reverseCurvatures.get(i));
        }
        final List<Heading> reverseHeadings = reverseEdge.uniDbHeadings();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(Heading.degrees(valueList.get(i)), reverseHeadings.get(i));
        }
        final List<Slope> reverseSlopes = reverseEdge.uniDbSlopes();
        for (var i = 0; i < valueList.size(); i++)
        {
            ensureEqual(Slope.degrees(valueList.get(i)), reverseSlopes.get(i));
        }
        final AdasRegionCode reverseRegionCode = reverseEdge.uniDbAdasRegionCode();
        ensureEqual(new AdasRegionCode(2), reverseRegionCode);

        ensureEqual(HighwayType.MOTORWAY, reverseEdge.uniDbHighwayType());
    }

    private ObjectList<AdasZCoordinate> adasZCoordinates()
    {
        final var list = new ObjectList<AdasZCoordinate>();
        list.add(AdasZCoordinate.meters(6));
        list.add(AdasZCoordinate.meters(7.5));
        list.add(AdasZCoordinate.meters(8));
        return list;
    }

    private ObjectList<AdasCurvature> curvatures()
    {
        return integers().mapped(AdasCurvature::new);
    }

    private ObjectList<Heading> headings()
    {
        return integers().mapped(Heading::degrees);
    }

    private UniDbHeavyWeightEdge heavyWeightEdge(final UniDbGraph graph)
    {
        final var identifier = new EdgeIdentifier(EDGE_IDENTIFIER);
        final UniDbHeavyWeightEdge edge = graph.newHeavyWeightEdge(identifier);
        edge.uniDbForwardLaneCount(Count._1);
        edge.uniDbReverseOneWay(false);
        edge.roadFunctionalClass(RoadFunctionalClass.FIRST_CLASS);
        edge.fromLocation(Location.TELENAV_HEADQUARTERS);
        edge.toLocation(Location.TELENAV_HEADQUARTERS.moved(Heading.NORTH, Distance.meters(100)));
        edge.roadShapeAndLength(roadShape(), edge.fromLocation(), edge.toLocation());
        edge.roadState(RoadState.TWO_WAY);
        edge.roadType(RoadType.FREEWAY);
        edge.roadSubType(RoadSubType.FUNCTIONAL_SPECIAL_ROAD);
        edge.freeFlow(SpeedCategory.forIdentifier(3));
        edge.fromNodeIdentifier(new PbfNodeIdentifier(100L));
        edge.toNodeIdentifier(new PbfNodeIdentifier(1000L));
        return edge;
    }

    private ObjectList<Integer> integers()
    {
        final var list = new ObjectList<Integer>(Maximum._3);
        list.add(1);
        list.add(2);
        list.add(3);
        return list;
    }

    private ObjectList<TurnLane> laneArrows()
    {
        return integers().mapped(TurnLane::new);
    }

    private ObjectList<LaneDivider> laneDividers()
    {
        return integers().mapped(LaneDivider::forIdentifier);
    }

    private ObjectList<OneWayLane> laneOneWays()
    {
        return integers().leftOf(2).mapped(OneWayLane::new);
    }

    private ObjectList<Lane> laneTypes()
    {
        return integers().mapped(Lane::new);
    }

    private Polyline roadShape()
    {
        final var builder = new PolylineBuilder();
        builder.add(Location.degrees(1, 1));
        builder.add(Location.degrees(2, 2));
        builder.add(Location.degrees(3, 3));
        return builder.build();
    }

    private ObjectList<Slope> slopes()
    {
        return integers().mapped(Slope::degrees);
    }
}
