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

package com.telenav.mesakit.graph.specifications.unidb.graph.loader;

import com.telenav.kivakit.collections.primitive.map.split.SplitLongToIntMap;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.collections.map.BoundedMap;
import com.telenav.kivakit.kernel.language.primitive.Booleans;
import com.telenav.kivakit.kernel.scalars.counts.Maximum;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.specifications.common.graph.loader.RawPbfGraphLoader;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.AlternateRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.ExitRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.LaneCountExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.OfficialRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.RoadStateExtractor.ExtractedRoadState;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.RouteRoadNameExtractor;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfDataSourceFactory;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.UniDbHeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.AdasZCoordinate;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.OneWayLane;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.AccessExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.AdasCurvatureHeadingSlopeExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.AdasRegionCodesExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.BackwardOneWayExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.BuildUpAreaExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.ComplexIntersectionExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.DividedRoadExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.FormOfWayExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.IsoCountryExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.LaneDividerExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.LaneExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.LeftSideDrivingExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.OneWayLaneExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.RouteTypeExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.SpeedLimitSourceExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes.extractors.TurnLaneExtractor;
import com.telenav.mesakit.graph.specifications.unidb.graph.node.model.attributes.extractor.AdasZCoordinateExtractor;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.HighwayType;
import com.telenav.mesakit.map.road.model.RoadName;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

import java.util.Objects;

import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.kivakit.map.road.name.standardizer.RoadNameStandardizer.Mode.NO_STANDARDIZATION;

public class UniDbRawPbfGraphLoader extends RawPbfGraphLoader
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final AdasCurvatureHeadingSlopeExtractor adasChsExtractor;

    private final AdasZCoordinateExtractor adasZCoordinateExtractor;

    private final BackwardOneWayExtractor backwardOneWayExtractor;

    private final BuildUpAreaExtractor buildUpAreaExtractor;

    private final ComplexIntersectionExtractor complexIntersectionExtractor;

    private final DividedRoadExtractor dividedRoadExtractor;

    private final AccessExtractor accessTypeExtractor;

    private final FormOfWayExtractor formOfWayExtractor;

    private final RouteTypeExtractor routeTypeExtractor;

    private final SpeedLimitSourceExtractor speedLimitSourceExtractor;

    private final LaneCountExtractor backwardLaneCountExtractor;

    private final LaneCountExtractor forwardLaneCountExtractor;

    private final TurnLaneExtractor laneArrowExtractor;

    private final LaneDividerExtractor laneDividersExtractor;

    private final LaneExtractor laneTypeExtractor;

    private final OneWayLaneExtractor laneOneWayExtractor;

    private final LeftSideDrivingExtractor leftSideDrivingExtractor;

    private final AdasRegionCodesExtractor regionCodesExtractor;

    private final SplitLongToIntMap adasZCoordinateInCentimetersForIdentifier;

    private final IsoCountryExtractor countryExtractor;

    private ObjectList<AdasZCoordinate> coordinates = new ObjectList<>(Maximum._10_000);

    private final BoundedMap<MapLocale, OfficialRoadNameExtractor> officialRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected OfficialRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new OfficialRoadNameExtractor(locale, NO_STANDARDIZATION, UniDbRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, ExitRoadNameExtractor> exitRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected ExitRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new ExitRoadNameExtractor(locale, NO_STANDARDIZATION, UniDbRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, AlternateRoadNameExtractor> alternateRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected AlternateRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new AlternateRoadNameExtractor(locale, NO_STANDARDIZATION, UniDbRawPbfGraphLoader.this);
        }
    };

    private final BoundedMap<MapLocale, RouteRoadNameExtractor> routeRoadNameExtractorForLocale = new BoundedMap<>()
    {
        @Override
        protected RouteRoadNameExtractor onInitialize(final MapLocale locale)
        {
            return new RouteRoadNameExtractor(locale, NO_STANDARDIZATION, UniDbRawPbfGraphLoader.this);
        }
    };

    /**
     * @param metadata Information about the source data
     * @param source UniDb PBF data source to load
     */
    public UniDbRawPbfGraphLoader(final PbfDataSourceFactory source, final Metadata metadata, final PbfTagFilter filter)
    {
        super(source, metadata, filter);

        // Create extractors
        accessTypeExtractor = new AccessExtractor(this);
        adasChsExtractor = new AdasCurvatureHeadingSlopeExtractor(this);
        adasZCoordinateExtractor = new AdasZCoordinateExtractor(this);
        backwardLaneCountExtractor = new LaneCountExtractor(this, "lanes:backward");
        backwardOneWayExtractor = new BackwardOneWayExtractor(this);
        buildUpAreaExtractor = new BuildUpAreaExtractor(this);
        complexIntersectionExtractor = new ComplexIntersectionExtractor(this);
        dividedRoadExtractor = new DividedRoadExtractor(this);
        formOfWayExtractor = new FormOfWayExtractor(this);
        forwardLaneCountExtractor = new LaneCountExtractor(this, "lanes:forward");
        laneArrowExtractor = new TurnLaneExtractor(this);
        laneDividersExtractor = new LaneDividerExtractor(this);
        laneOneWayExtractor = new OneWayLaneExtractor(this);
        laneTypeExtractor = new LaneExtractor(this);
        leftSideDrivingExtractor = new LeftSideDrivingExtractor(this);
        regionCodesExtractor = new AdasRegionCodesExtractor(this);
        routeTypeExtractor = new RouteTypeExtractor(this);
        speedLimitSourceExtractor = new SpeedLimitSourceExtractor(this);
        countryExtractor = new IsoCountryExtractor(this);

        adasZCoordinateInCentimetersForIdentifier = new SplitLongToIntMap("unidb-raw-graph-loader.adasZCoordinate");
        adasZCoordinateInCentimetersForIdentifier.initialSize(metadata.nodeCount(ALLOW_ESTIMATE).asEstimate());
        adasZCoordinateInCentimetersForIdentifier.nullLong(Long.MIN_VALUE);
        adasZCoordinateInCentimetersForIdentifier.nullInt(Integer.MIN_VALUE);
        adasZCoordinateInCentimetersForIdentifier.initialize();
    }

    @Override
    protected ProcessingDirective onExtractEdge(final ExtractedEdges edges)
    {
        final var edge = (UniDbHeavyWeightEdge) edges.edge();

        // Extract HERE attributes
        final var way = edges.way();
        final var state = edges.state();

        // Extract road names
        final var locale = edge.country() == null ? MapLocale.ENGLISH_UNITED_STATES.get() : edge.country().locale();
        edge.roadNames(RoadName.Type.ALTERNATE, alternateRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.OFFICIAL, officialRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.EXIT, exitRoadNameExtractorForLocale.getOrCreate(locale).extract(way));
        edge.roadNames(RoadName.Type.ROUTE, routeRoadNameExtractorForLocale.getOrCreate(locale).extract(way));

        edge.uniDbAccessType(accessTypeExtractor.extract(way));
        edge.uniDbReverseOneWay(backwardOneWayExtractor.extract(way));
        edge.uniDbBuildUpArea(buildUpAreaExtractor.extract(way));
        edge.uniDbComplexIntersection(complexIntersectionExtractor.extract(way));
        edge.uniDbDividedRoad(dividedRoadExtractor.extract(way));
        edge.uniDbFormOfWay(formOfWayExtractor.extract(way));
        edge.uniDbAdasZCoordinates(ensureAllValidCoordinates(coordinates));
        edge.country(countryExtractor.extract(way));

        coordinates = new ObjectList<>();

        final var highway = way.highway();
        if (highway != null)
        {
            edge.uniDbHighwayType(HighwayType.forName(highway.toUpperCase()));
        }
        else
        {
            DEBUG.quibble("No highway tag found for edge $", edge.identifier());
        }
        final var tags = way.tagMap();
        edge.uniDbTurnLanes(maybeReversed(laneArrowExtractor.extractList(tags, "turn:lanes"), state));
        edge.uniDbLaneDividers(maybeReversed(laneDividersExtractor.extractList(tags, "divider:lanes"), state));
        edge.uniDbLaneTypes(maybeReversed(laneTypeExtractor.extractList(tags, "type:lanes"), state));
        final var laneOneWays = maybeReversed(laneOneWayExtractor.extractList(tags, "oneway:lanes"), state);
        if (laneOneWays != null)
        {
            edge.uniDbLaneOneWays(laneOneWays.mapped(OneWayLane::reversed));
        }
        edge.uniDbLeftSideDriving(leftSideDrivingExtractor.extract(way));
        final var regionCodes = regionCodesExtractor.extract(way);
        if (regionCodes != null)
        {
            edge.uniDbForwardRegionCode(regionCodes.get(0));
            if (edge.isTwoWay())
            {
                edge.uniDbReverseRegionCode(regionCodes.last());
            }
        }
        edge.uniDbRouteType(routeTypeExtractor.extract(way));
        edge.uniDbSpeedLimitSource(speedLimitSourceExtractor.extract(way));

        final var sequence = adasChsExtractor.extract(way);
        if (sequence != null && !sequence.isEmpty())
        {
            edge.uniDbCurvatureHeadingSlopeSequence(sequence.maybeReverse(state.isReversed()));
        }

        // Set lane counts
        final var forwardLaneCount = forwardLaneCountExtractor.extract(way);
        var backwardLaneCount = backwardLaneCountExtractor.extract(way);

        if (backwardLaneCount == null)
        {
            backwardLaneCount = forwardLaneCount;
        }

        if (edge.isTwoWay())
        {
            if (edge.isReverse())
            {
                edge.uniDbForwardLaneCount(backwardLaneCount);
                edge.uniDbReverseLaneCount(forwardLaneCount);
            }
            else
            {
                edge.uniDbForwardLaneCount(forwardLaneCount);
                edge.uniDbReverseLaneCount(backwardLaneCount);
            }
        }
        else if (edge.uniDbIsReverseOneWay())
        {
            edge.uniDbForwardLaneCount(backwardLaneCount);
        }
        else
        {
            edge.uniDbForwardLaneCount(forwardLaneCount);
        }

        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected void onProcessWayNode(final GraphStore store, final WayNode node, final Location location)
    {
        coordinates.add(adasZCoordinateForWayNode(node));
    }

    @Override
    protected void onProcessedNode(final GraphStore store, final PbfNode node)
    {
        final var coordinate = adasZCoordinateExtractor.extract(node);
        if (null != coordinate)
        {
            adasZCoordinateInCentimetersForIdentifier.put(node.identifierAsLong(), coordinate.centimeters());
        }
    }

    @Override
    protected ProcessingDirective onProcessingNode(final GraphStore store, final PbfNode node)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected ProcessingDirective onProcessingRelation(final GraphStore store, final PbfRelation relation)
    {
        // Reject any unacceptable relations
        return configuration().relationFilter().accepts(relation) ? ProcessingDirective.ACCEPT : ProcessingDirective.REJECT;
    }

    @Override
    protected ProcessingDirective onProcessingWay(final GraphStore store, final PbfWay way)
    {
        return ProcessingDirective.ACCEPT;
    }

    @Override
    protected boolean shouldStoreNodeLocation(final PbfNode node)
    {
        // If the file does not have way node locations, then we need to store node locations
        return !Booleans.isTrue(metadata().property(WayNode.METADATA_KEY_LOCATION_INCLUDED));
    }

    private AdasZCoordinate adasZCoordinateForWayNode(final WayNode node)
    {
        return AdasZCoordinate.centimeters(adasZCoordinateInCentimetersForIdentifier.get(node.getNodeId()));
    }

    private ObjectList<AdasZCoordinate> ensureAllValidCoordinates(final ObjectList<AdasZCoordinate> coordinates)
    {
        if (coordinates.stream().noneMatch(Objects::isNull))
        {
            return coordinates;
        }
        return ObjectList.emptyList();
    }

    private <T> ObjectList<T> maybeReversed(final ObjectList<T> list, final ExtractedRoadState state)
    {
        if (list != null)
        {
            list.maybeReversed(state.isReversed());
        }
        return list;
    }
}
