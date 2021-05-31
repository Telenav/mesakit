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

package com.telenav.kivakit.graph.specifications.common.graph.loader;

import com.telenav.kivakit.collections.primitive.map.split.SplitLongToLongMap;
import com.telenav.kivakit.data.extraction.*;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.interfaces.collection.Addable;
import com.telenav.kivakit.kernel.interfaces.collection.Compressible.Method;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.string.Strings;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.kernel.validation.Validation;
import com.telenav.kivakit.data.formats.library.map.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.tags.*;
import com.telenav.kivakit.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.kivakit.data.formats.pbf.processing.PbfDataProcessor.Result;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.identifiers.EdgeIdentifier;
import com.telenav.kivakit.graph.io.load.GraphConstraints;
import com.telenav.kivakit.graph.metadata.DataSpecification;
import com.telenav.kivakit.graph.specifications.common.edge.*;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.kivakit.graph.specifications.common.graph.loader.extractors.*;
import com.telenav.kivakit.graph.specifications.common.graph.loader.extractors.RoadStateExtractor.ExtractedRoadState;
import com.telenav.kivakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.kivakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.kivakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.kivakit.graph.specifications.library.pbf.*;
import com.telenav.kivakit.graph.specifications.library.store.GraphStore;
import com.telenav.kivakit.graph.specifications.osm.graph.edge.model.attributes.extractors.PlaceExtractor;
import com.telenav.kivakit.graph.specifications.osm.graph.loader.sectioner.EdgeNodeMap;
import com.telenav.kivakit.graph.traffic.extractors.*;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.geography.polyline.*;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;
import com.telenav.kivakit.map.geography.segment.Segment;
import com.telenav.kivakit.map.geography.shape.Outline;
import com.telenav.kivakit.map.geography.shape.Outline.Containment;
import com.telenav.kivakit.map.measurements.*;
import com.telenav.kivakit.map.region.Country;
import com.telenav.kivakit.map.road.model.*;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

import java.util.*;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;
import static com.telenav.kivakit.data.formats.pbf.processing.PbfDataProcessor.Result.*;
import static com.telenav.kivakit.graph.GraphElement.VALIDATE_RAW;
import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.kivakit.map.geography.Precision.DM7;

/**
 * Base class for loading raw graphs from PBF files. In the OSM data specification, a raw graph contains data that has
 * not been way-sectioned, where each edge is an OSM way. In the UniDb specification, the data is already way-sectioned
 * and there is no such distinction.
 */
@SuppressWarnings("unused")
public abstract class RawPbfGraphLoader extends PbfGraphLoader
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Set<String> ignoreHighwayTypes = new HashSet<>();

    private static final Debug DEBUG = new Debug(RawPbfGraphLoader.LOGGER);

    static
    {
        RawPbfGraphLoader.ignoreHighwayTypes.add("abandoned");
        RawPbfGraphLoader.ignoreHighwayTypes.add("raceway");
        RawPbfGraphLoader.ignoreHighwayTypes.add("dismantled");
        RawPbfGraphLoader.ignoreHighwayTypes.add("disused");
        RawPbfGraphLoader.ignoreHighwayTypes.add("escape");
        RawPbfGraphLoader.ignoreHighwayTypes.add("planned");
        RawPbfGraphLoader.ignoreHighwayTypes.add("razed");
        RawPbfGraphLoader.ignoreHighwayTypes.add("platform");
        RawPbfGraphLoader.ignoreHighwayTypes.add("bus_stop");
        RawPbfGraphLoader.ignoreHighwayTypes.add("corridor");
        RawPbfGraphLoader.ignoreHighwayTypes.add("elevator");
        RawPbfGraphLoader.ignoreHighwayTypes.add("crossing");
        RawPbfGraphLoader.ignoreHighwayTypes.add("yes");
        RawPbfGraphLoader.ignoreHighwayTypes.add("dummy");
    }

    protected enum ProcessingDirective
    {
        ACCEPT,
        REJECT
    }

    protected static class ExtractedEdges
    {
        private final List<? extends Edge> edges;

        private final PbfWay way;

        private final ExtractedRoadState state;

        @SuppressWarnings("ClassEscapesDefinedScope")
        public ExtractedEdges(final List<? extends Edge> edges, final PbfWay way, final ExtractedRoadState state)
        {
            this.edges = edges;
            this.way = way;
            this.state = state;
        }

        public Edge edge()
        {
            return edges.iterator().next();
        }

        public List<? extends Edge> edges()
        {
            return edges;
        }

        @SuppressWarnings("ClassEscapesDefinedScope")
        public ExtractedRoadState state()
        {
            return state;
        }

        public PbfWay way()
        {
            return way;
        }
    }

    private static class WayChunk
    {
        private final WayIdentifier identifier;

        private final Polyline shape;

        private final ObjectList<NodeIdentifier> nodes;

        private final boolean fromOnBorder;

        private final boolean toOnBorder;

        private final Location from;

        private final Location to;

        WayChunk(final WayIdentifier identifier, final Location from, final Location to, final Polyline shape,
                 final ObjectList<NodeIdentifier> nodes, final boolean fromOnBorder, final boolean toOnBorder)
        {
            assert identifier != null;
            assert shape == null || shape.size() == nodes.size();
            assert from != null && to != null;

            this.from = from;
            this.to = to;
            this.nodes = nodes;
            this.shape = shape;
            this.identifier = identifier;
            this.fromOnBorder = fromOnBorder;
            this.toOnBorder = toOnBorder;
        }

        @Override
        public String toString()
        {
            return "[WayChunk way = " + identifier +
                    ", fromNode = " + fromNode() + ", toNode = " + toNode() +
                    ", from = " + from + ", to = " + to +
                    ", fromOnBorder = " + isFromOnBorder() + ", toOnBorder = " + isToOnBorder() +
                    ", nodes = " + nodes() + ", shape = " + shape + "]";
        }

        Location from()
        {
            return from;
        }

        NodeIdentifier fromNode()
        {
            return nodes.first();
        }

        boolean isFromOnBorder()
        {
            return fromOnBorder;
        }

        boolean isToOnBorder()
        {
            return toOnBorder;
        }

        ObjectList<NodeIdentifier> nodes()
        {
            return nodes;
        }

        WayChunk reversed()
        {
            return new WayChunk(identifier, to, from,
                    shape == null ? null : shape().reversed(), nodes().reversed(),
                    isToOnBorder(), isFromOnBorder());
        }

        Polyline shape()
        {
            return shape;
        }

        Location to()
        {
            return to;
        }

        NodeIdentifier toNode()
        {
            return nodes.last();
        }
    }

    // Extractors
    private final BridgeTypeExtractor bridgeTypeExtractor;

    private final ClosedToThroughTrafficExtractor closedToThroughTrafficExtractor;

    private final HovLaneCountExtractor hovLaneCountExtractor;

    private final ReferenceSpeedExtractor referenceSpeedExtractor;

    private final RoadFunctionalClassExtractor roadFunctionalClassExtractor;

    private final RoadStateExtractor roadStateExtractor;

    private final RoadSubTypeExtractor roadSubTypeExtractor;

    private final RoadTypeExtractor roadTypeExtractor;

    private final SpeedCategoryExtractor speedCategoryExtractor;

    private final SpeedLimitExtractor speedLimitExtractor;

    private final SpeedPatternIdentifierExtractor speedPatternIdentifierExtractor;

    private final SurfaceExtractor surfaceExtractor;

    private final TollRoadExtractor tollRoadExtractor;

    private final UnderConstructionExtractor underConstructionExtractor;

    private final PlaceExtractor placeExtractor;

    private final GradeSeparationExtractor gradeSeparationFromExtractor;

    private final GradeSeparationExtractor gradeSeparationToExtractor;

    // The maximum area we're loading
    private Rectangle bounds;

    // Map from identifier to location for nodes
    private final SplitLongToLongMap nodeIdentifierToLocation;

    // Nodes for ways (this is needed during way sectioning to find intersections by node id)
    private final EdgeNodeMap edgeNodes = new EdgeNodeMap("RawPbfGraphLoader.edgeNodes");

    /** Which tags to include (if any) */
    private final PbfTagFilter tagFilter;

    private ThreadLocal<Addable<Place>> placeAdder;

    private ThreadLocal<Addable<Edge>> edgeAdder;

    private ThreadLocal<Addable<EdgeRelation>> relationAdder;

    private final TrafficIdentifiersExtractor trafficIdentifiersExtractor;

    // Temporary variables scoped between onExtractEdge and onDoneExtractingEdges
    private final ThreadLocal<GradeSeparation> fromGradeSeparation = new ThreadLocal<>();

    private final ThreadLocal<GradeSeparation> toGradeSeparation = new ThreadLocal<>();

    /**
     * @param metadata Information describing the data in the given source
     * @param data OSM data source to read from
     * @param tagFilter What tags (if any) should be included in the loaded graph
     */
    @SuppressWarnings("ConstantConditions")
    protected RawPbfGraphLoader(final PbfDataSourceFactory data, final Metadata metadata, final PbfTagFilter tagFilter)
    {
        ensure(data != null);
        ensure(metadata != null);
        ensure(metadata.validator(Metadata.VALIDATE_EXCEPT_STATISTICS).isValid(), "Metadata is invalid");
        ensure(tagFilter != null);

        dataSourceFactory(data, metadata);

        this.tagFilter = tagFilter;

        // Create extractors
        bridgeTypeExtractor = new BridgeTypeExtractor(this);
        closedToThroughTrafficExtractor = new ClosedToThroughTrafficExtractor(this);
        hovLaneCountExtractor = new HovLaneCountExtractor(this);
        roadFunctionalClassExtractor = new RoadFunctionalClassExtractor(this);
        roadStateExtractor = new RoadStateExtractor(this);
        roadSubTypeExtractor = new RoadSubTypeExtractor(this);
        roadTypeExtractor = new RoadTypeExtractor(this);
        speedCategoryExtractor = new SpeedCategoryExtractor(this);
        speedLimitExtractor = new SpeedLimitExtractor(this);
        surfaceExtractor = new SurfaceExtractor(this);
        tollRoadExtractor = new TollRoadExtractor(this);
        underConstructionExtractor = new UnderConstructionExtractor(this);
        referenceSpeedExtractor = new ReferenceSpeedExtractor(this);
        speedPatternIdentifierExtractor = new SpeedPatternIdentifierExtractor(this);
        gradeSeparationFromExtractor = new GradeSeparationExtractor(this, GradeSeparationExtractor.Type.FROM);
        gradeSeparationToExtractor = new GradeSeparationExtractor(this, GradeSeparationExtractor.Type.TO);

        final Extractor<Location, WayNode> wayNodeLocationExtractor = new BaseExtractor<>(this)
        {
            @Override
            public Location onExtract(final WayNode node)
            {
                return nodeToLocation(node);
            }
        };

        placeExtractor = new PlaceExtractor(this, metadata, new LocationExtractor(this, wayNodeLocationExtractor));

        nodeIdentifierToLocation = new SplitLongToLongMap("raw-pbf-graph-loader.nodeIdentifierToLocation");
        nodeIdentifierToLocation.nullLong(Long.MIN_VALUE);
        nodeIdentifierToLocation.initialSize(metadata.nodeCount(ALLOW_ESTIMATE).asEstimate());
        nodeIdentifierToLocation.initialize();
        trafficIdentifiersExtractor = new TrafficIdentifiersExtractor(this);
    }

    public EdgeNodeMap edgeNodes()
    {
        return edgeNodes;
    }

    @Override
    public final Metadata onLoad(final GraphStore store, final GraphConstraints constraints)
    {
        final var graph = store.graph();

        placeAdder = ThreadLocal.withInitial(() -> store.placeStore().adder());
        edgeAdder = ThreadLocal.withInitial(() -> store.edgeStore().adder());
        relationAdder = ThreadLocal.withInitial(() -> store.relationStore().adder());

        final var resource = resource();
        final var metadata = metadata();
        final var name = metadata.name();

        graph.metadata(metadata);

        // Process the input
        bounds = constraints.bounds();
        final var outer = this;

        final var acceptedNodes = new MutableCount();
        final var acceptedWays = new MutableCount();
        final var acceptedRelations = new MutableCount();

        final var filteredNodes = new MutableCount();
        final var filteredWays = new MutableCount();
        final var filteredRelations = new MutableCount();

        final var discardedNodes = new MutableCount();
        final var discardedWays = new MutableCount();
        final var discardedRelations = new MutableCount();

        final var dataSource = dataSourceFactory().newInstance(metadata);
        metadata.configure(dataSource);
        dataSource.process("Loading Raw Data", new PbfDataProcessor()
        {
            @Override
            public void onEndNodes()
            {
                // This map is now populated so we can freeze it
                outer.nodeIdentifierToLocation.compress(Method.FREEZE);

                // Allow subclass to free data structures
                outer.onEndNodes();
            }

            @Override
            public void onEndRelations()
            {
            }

            @Override
            public void onEndWays()
            {
                // Create vertexes for use in relation processing
                store.vertexStore().addVertexes();
            }

            @Override
            public Result onNode(final PbfNode node)
            {
                final Result result = processNode(store, node);
                switch (result)
                {
                    case ACCEPTED:
                        acceptedNodes.increment();
                        break;

                    case DISCARDED:
                        discardedNodes.increment();
                        break;

                    case FILTERED_OUT:
                        filteredNodes.increment();
                        break;
                }
                return result;
            }

            @Override
            public void onNodes(final Collection<PbfNode> nodes)
            {
                for (final var node : nodes)
                {
                    onNode(node);
                }
            }

            @Override
            public Result onRelation(final PbfRelation relation)
            {
                assert relation != null;
                final var result = processRelation(store, relation);
                switch (result)
                {
                    case ACCEPTED:
                        acceptedRelations.increment();
                        break;

                    case DISCARDED:
                        discardedRelations.increment();
                        break;

                    case FILTERED_OUT:
                        filteredRelations.increment();
                        break;
                }
                return result;
            }

            @Override
            public Result onWay(final PbfWay way)
            {
                final var result = processWay(store, way);
                switch (result)
                {
                    case ACCEPTED:
                        acceptedWays.increment();
                        break;

                    case DISCARDED:
                        discardedWays.increment();
                        break;

                    case FILTERED_OUT:
                        filteredWays.increment();
                        break;
                }
                return result;
            }

            @Override
            public void onWays(final Collection<PbfWay> ways)
            {
                for (final var way : ways)
                {
                    onWay(way);
                }
            }
        });

        store.flush();

        final var edgeStore = store.edgeStore();
        final var relationStore = store.relationStore();
        final var placeStore = store.placeStore();

        final var edgeCount = edgeStore.retrieveCount();
        final var relationCount = relationStore.retrieveCount();
        final var placeCount = placeStore.retrieveCount();

        information(Strings.topLine("Raw Data Statistics for '$'", name));
        information("");
        information("           Nodes:     accepted ${left} discarded ${left} filtered ${left}", acceptedNodes, discardedNodes, filteredNodes);
        information("            Ways:     accepted ${left} discarded ${left} filtered ${left}", acceptedWays, discardedWays, filteredWays);
        information("       Relations:     accepted ${left} discarded ${left} filtered ${left}", acceptedRelations, discardedRelations, filteredRelations);
        information("           Edges:        added ${left} discarded ${left}", edgeCount, edgeStore.discarded());
        information("  Edge Relations:        added ${left} discarded ${left}", relationCount, relationStore.discarded());
        information("          Places:        added ${left} discarded ${left}", placeCount, placeStore.discarded());
        information("");
        information("  To find out why any data was discarded, define TDK_DEBUG=GraphElementStore,RawPbfGraphLoader");
        information(Strings.bottomLine());

        return store.metadata()
                .withNodeCount(acceptedNodes.asCount())
                .withWayCount(acceptedWays.asCount())
                .withRelationCount(acceptedRelations.asCount());
    }

    @Override
    public Validation validation()
    {
        return new Validation("VALIDATE_RAW_GRAPH_STORE")
                .skip(RelationStore.class)
                .skip(VertexStore.class);
    }

    protected Location nodeToLocation(final WayNode node)
    {
        // If way node locations are available due to more recent PBF data
        if (nodeIdentifierToLocation.isEmpty())
        {
            // then get the location directly from the node object
            return Location.degrees(node.getLatitude(), node.getLongitude());
        }
        else
        {
            // otherwise return the node's location by looking at the map we populated earlier
            // while reading nodes
            final var location = nodeIdentifierToLocation.get(node.getNodeId());
            if (!nodeIdentifierToLocation.isNull(location))
            {
                return DM7.toLocation(location);
            }
        }
        return null;
    }

    protected void onCleanCutCrossedBorder(final WayNode node, final Location location)
    {
    }

    protected void onCleanCutInside(final WayNode node, final Location location)
    {
    }

    protected void onCleanCutOutside(final WayNode node, final Location location)
    {
    }

    /**
     * Called when done processing nodes to allow freeing of data structures
     */
    protected void onEndNodes()
    {
    }

    @SuppressWarnings({ "UnusedReturnValue", "SameReturnValue" })
    protected abstract ProcessingDirective onExtractEdge(ExtractedEdges edges);

    protected void onProcessWayNode(final GraphStore store, final WayNode node, final Location location)
    {
    }

    @SuppressWarnings({ "EmptyMethod" })
    protected void onProcessedNode(final GraphStore store, final PbfNode node)
    {
    }

    @SuppressWarnings({ "EmptyMethod" })
    protected void onProcessedRelation(final GraphStore store, final PbfRelation relation)
    {
    }

    @SuppressWarnings({ "EmptyMethod" })
    protected void onProcessedWay(final GraphStore store, final PbfWay way)
    {
    }

    /**
     * @return True if the given node should be processed
     */
    @SuppressWarnings({ "SameReturnValue" })
    protected abstract ProcessingDirective onProcessingNode(final GraphStore store, final PbfNode node);

    /**
     * @return True if the given relation should be processed
     */
    protected abstract ProcessingDirective onProcessingRelation(final GraphStore store, final PbfRelation relation);

    /**
     * @return True if the given way should be processed
     */
    protected abstract ProcessingDirective onProcessingWay(final GraphStore store, final PbfWay way);

    /**
     * Determines if the given node's location should be stored. If this method returns true, the node's location must
     * be stored in a memory-hungry map for later use. There are three different ways it can be determined if a node's
     * location needs to be stored, depending on the subclass and its {@link DataSpecification} and the presence or
     * absence of way node locations in the data:
     * <pre>
     *     1. If the PBF file has way node locations (as determined by the metadata section of the PBF file)
     *        then the method should return false as way node locations don't need to be stored
     *     2. If the PBF file contains OSM data, then the {@link PbfDataAnalysis} performed during the first pass
     *        has already determined which nodes are way nodes.
     *     3. In the case of UniDb data, there are no nodes in the file that are not way nodes and so each node
     *        must have its location stored (unless #1 is true), so the method should return true.
     * </pre>
     *
     * @param node The node to inspect
     * @return True if the node is a way node and we should store its location in a map for later
     */
    protected abstract boolean shouldStoreNodeLocation(PbfNode node);

    boolean includeTags(final Graph graph)
    {
        // For small graphs, we throw in the tags for debugging purposes
        return graph.supports(GraphElementAttributes.get().TAGS) || graph.edgeCount().isLessThan(Count._1_000);
    }

    private void addRouteName(final GraphStore store, final Edge edge, final PbfRelation relation)
    {
        String ref = null;
        String network = null;
        for (final var tag : relation)
        {
            if ("network".equalsIgnoreCase(tag.getKey()))
            {
                network = tag.getValue();
            }
            if ("ref".equalsIgnoreCase(tag.getKey()))
            {
                ref = tag.getValue();
            }
        }
        if (ref != null && network != null)
        {
            final var name = network.replaceAll("\\w+:", "") + "-" + ref;
            if (!Strings.isEmpty(name))
            {
                store.edgeStore().storeRoadName(edge, RoadName.Type.ROUTE, RoadName.forName(name));
            }
        }
    }

    /**
     * @return A single chunk for the given way
     */
    private WayChunk chunk(final GraphStore store, final PbfWay way)
    {
        Polyline shape = null;
        final var wayNodes = way.nodes();
        final var nodes = new ObjectList<NodeIdentifier>();
        final Location from;
        final Location to;
        if (wayNodes.size() == 2)
        {
            final var fromNode = wayNodes.get(0);
            final var toNode = wayNodes.get(1);
            nodes.add(new PbfNodeIdentifier(fromNode));
            nodes.add(new PbfNodeIdentifier(toNode));
            from = nodeToLocation(fromNode);
            to = nodeToLocation(toNode);
        }
        else
        {
            // Build polyline
            final var builder = new PolylineBuilder();

            // Go through way nodes
            for (final var node : wayNodes)
            {
                // get the location for the node
                final var location = nodeToLocation(node);
                if (location != null)
                {
                    // add node to list
                    nodes.add(new PbfNodeIdentifier(node));

                    // add location to polyline
                    builder.add(location);

                    // and let the subclass do any processing it needs to
                    onProcessWayNode(store, node, location);
                }
                else
                {
                    RawPbfGraphLoader.DEBUG.trace("Discarding $ because node ${long} has no location", way, node.getNodeId());
                    return null;
                }
            }

            // If the builder has a valid polyline
            if (builder.isValid())
            {
                // then build the way shape
                shape = builder.build();
                from = shape.start();
                to = shape.end();
            }
            else
            {
                return null;
            }
        }

        // Validate to see if the 'from' or 'to' node is exactly on the border
        final var cleanCutTo = configuration().cleanCutTo();
        final var fromOnBorder = cleanCutTo != null && cleanCutTo.containment(from) == Containment.ON_BORDER;
        final var toOnBorder = cleanCutTo != null && cleanCutTo.containment(to) == Containment.ON_BORDER;

        // Create a chunk for this way
        if (from != null && to != null)
        {
            return new WayChunk(new PbfWayIdentifier(way), from, to, shape, nodes, fromOnBorder, toOnBorder);
        }
        return null;
    }

    /**
     * @return Breaks the given way into chunks if it crosses the clean-cutting region border
     */
    private List<WayChunk> cleanCut(final GraphStore store, final PbfWay way)
    {
        // Chunks to return
        final List<WayChunk> chunks = new ArrayList<>();

        // Polyline builder for way shape
        var builder = new PolylineBuilder();

        // List of nodes
        var nodes = new ObjectList<NodeIdentifier>();

        // Crossing and border states
        Outline.Containment were = null;
        Outline.Containment now;
        var crossedBorder = false;
        var fromOnBorder = false;
        var toOnBorder = false;

        // Location prior to the current one as we loop
        Location previousLocation = null;

        // Go through way nodes
        for (final var node : way.nodes())
        {
            // Get location of node
            final var location = nodeToLocation(node);
            if (location != null)
            {
                // If we were inside and now we're outside the cleanCutTo region, or we were outside
                // and now we're inside the cleanCutToRegion, then we crossed the border
                now = configuration().cleanCutTo().containment(location);
                crossedBorder = were != null
                        && ((were.isInside() && now.isOutside()) || (were.isOutside() && now.isInside()));

                // If we crossed the border,
                if (crossedBorder)
                {
                    // compute the intersection with the segment from the previous location to
                    // the current location.
                    final var intersection = configuration().cleanCutTo().intersection(new Segment(previousLocation, location));

                    // If there is an intersection
                    if (intersection != null)
                    {
                        // Create a synthetic node identifier for the intersection
                        final var synthetic = PbfNodeIdentifier.nextSyntheticNodeIdentifier();

                        switch (now)
                        {
                            case INDETERMINATE:
                            case INSIDE:

                                // The 'from' location is synthetic at the border intersection,
                                // while the 'to' node is normal.
                                fromOnBorder = true;
                                toOnBorder = false;

                                // We entered the region, so add the synthetic border intersection
                                builder.add(intersection);
                                nodes.add(synthetic);

                                // and then the node itself which is inside the region.
                                builder.add(location);
                                nodes.add(new PbfNodeIdentifier(node));

                                onCleanCutInside(node, location);
                                break;

                            case OUTSIDE:

                                // The 'to' location is synthetic at the border intersection
                                toOnBorder = true;

                                // We left the region, so add the synthetic border intersection
                                builder.add(intersection);
                                nodes.add(synthetic);

                                onCleanCutOutside(node, location);
                                break;

                            case ON_BORDER:
                                throw new IllegalStateException();
                        }
                    }
                }

                // If we didn't cross the border and we're inside or on the border
                if (!crossedBorder && !now.isOutside())
                {
                    // If we have no "from" yet,
                    if (nodes.isEmpty())
                    {
                        // then this is the "from"
                        fromOnBorder = (now == Outline.Containment.ON_BORDER);
                    }
                    else
                    {
                        // otherwise this is the "to"
                        toOnBorder = (now == Outline.Containment.ON_BORDER);
                    }

                    // then just add the node
                    builder.add(location);
                    nodes.add(new PbfNodeIdentifier(node));
                }

                // If we just left the region,
                if (now.isOutside() && were != null && !were.isOutside())
                {
                    onCleanCutCrossedBorder(node, location);

                    // and there is a valid polyline under construction
                    if (builder.isValid() && !builder.isZeroLength())
                    {
                        // then build and add it
                        final var line = builder.build();
                        chunks.add(new WayChunk(new PbfWayIdentifier(way), line.start(), line.end(), line, nodes, fromOnBorder, toOnBorder));

                        // Reset on-border booleans until we re-enter the region
                        fromOnBorder = false;
                        toOnBorder = false;
                    }

                    // Prepare to work on the next chunk
                    builder = new PolylineBuilder();
                    nodes = new ObjectList<>();
                }

                // Advance to the next location
                were = now;
                previousLocation = location;
            }
        }

        // If there's a polyline to add
        if (builder.isValid() && !builder.isZeroLength())
        {
            // build and add it
            final var line = builder.build();
            chunks.add(new WayChunk(new PbfWayIdentifier(way), line.start(), line.end(), line, nodes, fromOnBorder, toOnBorder));
        }

        return chunks;
    }

    private Country country(final Polyline shape)
    {
        return configuration().regionInformation() ? Country.forLocation(shape.start()) : null;
    }

    /**
     * @return A list of {@link HeavyWeightEdge}s for the given way. The tag map is just passed in for the sake of
     * efficiency since we already have it in the calling code.
     */
    @SuppressWarnings("SpellCheckingInspection")
    private List<HeavyWeightEdge> extractEdges(final GraphStore store, final PbfWay way)
    {
        // We don't want to process certain highway types for any data supplier
        if (RawPbfGraphLoader.ignoreHighwayTypes.contains(way.highway()))
        {
            return Collections.emptyList();
        }

        final var tags = way.tagMap();

        // Get the graph's store
        final var graph = store.graph();

        // Extracted edges to return
        final List<HeavyWeightEdge> edges = new ArrayList<>();

        // Get way identifier as a sequence numbered edge identifier
        var identifier = new EdgeIdentifier(way.identifierAsLong()).sequenceNumbered();

        // Get road state from way tags
        final var state = roadStateExtractor.extract(way);
        if (state.state() == RoadState.NULL)
        {
            quibble("Way $ has invalid road state (oneway = $, highway = $), assuming TWO_WAY",
                    way, tags.get("oneway"), tags.get("highway"));
            state.state(RoadState.TWO_WAY);
        }

        // Go through chunks of the way. If the way is being clean-cut, there might be multiple
        // chunks because the way may be broken into pieces as it crosses back and forth across the
        // clean cutting region. If we are not clean-cutting, there will be only one chunk for
        // each way.
        for (var chunk : wayChunks(store, way))
        {
            // If the road state says this way is reversed
            if (state.isReversed())
            {
                // then reverse the chunk
                chunk = chunk.reversed();
            }

            // Add way nodes for this chunk for later use in way sectioning
            synchronized (edgeNodes)
            {
                edgeNodes.put(identifier, chunk.nodes());
            }

            // Create temporary edge
            final var edge = graph.newHeavyWeightEdge(identifier);

            // We identify multiple chunks in the high two digits, like NNNNNN0100, NNNNNN0200, etc.
            // The low two decimal digits are used for edge sectioning.
            identifier = identifier.nextChunk();

            // Get any shape
            final var shape = chunk.shape();

            // and if it's got just one point,
            if (shape != null && shape.size() < 2)
            {
                // skip it
                continue;
            }

            // otherwise, save road shape and/or the from and to locations and compute the length
            edge.roadShapeAndLength(shape, chunk.from(), chunk.to());

            // and find what country the edge is in. Note that if the country has been explicitly specified
            // we can save a lot of time by not looking it up in a spatial index
            if (edge.supports(EdgeAttributes.get().COUNTRY))
            {
                edge.country(country(edge.roadShape()));
            }

            var subType = roadSubTypeExtractor.extract(way);
            if (subType == RoadSubType.CONNECTING_ROAD && edge.length().isGreaterThan(Distance.meters(300)))
            {
                subType = RoadSubType.RAMP;
            }

            // The edge length has to be stored here before the road sub-type extractor is called
            // because that extractor uses the current edge length in determining the sub-type
            edge.bridgeType(bridgeTypeExtractor.extract(way));
            edge.closedToThroughTraffic(closedToThroughTrafficExtractor.extract(way));
            edge.freeFlow(speedCategoryExtractor.extract(way));
            edge.hovLaneCount(hovLaneCountExtractor.extract(way));
            edge.roadFunctionalClass(roadFunctionalClassExtractor.extract(way));
            edge.roadState(state.state());
            edge.roadSubType(subType);
            edge.roadType(roadTypeExtractor.extract(way));
            final var speedLimit = speedLimitExtractor.extract(way);
            if (speedLimit != null)
            {
                edge.speedLimit(speedLimit.minimum(Speed.kilometersPerHour(160)));
            }
            edge.surface(surfaceExtractor.extract(way));
            edge.tollRoad(tollRoadExtractor.extract(way));
            edge.type(Edge.Type.NORMAL);
            edge.underConstruction(underConstructionExtractor.extract(way));
            edge.fromNodeIdentifier(chunk.fromNode());
            edge.toNodeIdentifier(chunk.toNode());
            edge.fromVertexClipped(chunk.isFromOnBorder());
            edge.toVertexClipped(chunk.isToOnBorder());

            // Traffic identifiers
            final var extract = trafficIdentifiersExtractor.extract(way);
            if (extract != null)
            {
                // TMC only for now
                if (extract.hasForward())
                {
                    edge.tmcIdentifiers(extract.forward());
                }
                if (extract.hasReverse())
                {
                    edge.reverseTmcIdentifiers(extract.reverse());
                }
            }

            if (includeTags(graph))
            {
                final var pbfTags = way.tagList(tagFilter);
                edge.tags(pbfTags);
            }

            // speed pattern
            final var speedPatternIdentifiers = speedPatternIdentifierExtractor.extract(way);
            if (speedPatternIdentifiers != null)
            {
                edge.speedPatternIdentifier(speedPatternIdentifiers.a());
                edge.reverseSpeedPatternIdentifier(speedPatternIdentifiers.b());
            }

            final var referenceSpeeds = referenceSpeedExtractor.extract(way);
            if (referenceSpeeds != null)
            {
                edge.referenceSpeed(referenceSpeeds.a());
                edge.uniDbReverseReferenceSpeed(referenceSpeeds.b());
            }

            if (edge.validator(VALIDATE_RAW).isValid(RawPbfGraphLoader.LOGGER))
            {
                edges.add(edge);
            }

            onExtractEdge(new ExtractedEdges(Collections.singletonList(edge), way, state));

            // Extract grade separations
            fromGradeSeparation.set(gradeSeparationFromExtractor.extract(way));
            toGradeSeparation.set(gradeSeparationToExtractor.extract(way));
            final var maximumGradeSeparation = fromGradeSeparation.get().maximum(toGradeSeparation.get());
            edge.fromGradeSeparation(maximumGradeSeparation);
            edge.toGradeSeparation(maximumGradeSeparation);
        }

        // If there are some edges
        if (!edges.isEmpty())
        {
            // Get first and last edge
            final var first = (HeavyWeightEdge) edges.get(0);
            final var last = (HeavyWeightEdge) edges.get(edges.size() - 1);

            // If the first edge's 'from' vertex is NOT clipped
            if (!first.isFromVertexClipped())
            {
                // then the edge is not in the middle (which should be at maximum grade), so we can
                // set the grade separation based on the 'from' node of the way
                first.fromGradeSeparation(state.isReversed() ? toGradeSeparation.get() : fromGradeSeparation.get());
            }

            // If the last edge's 'to' vertex is NOT clipped
            if (!last.isToVertexClipped())
            {
                // then the edge is not in the middle (which should be at maximum grade), so we can
                // set the grade separation based on the 'to' node of the way
                last.toGradeSeparation(state.isReversed() ? fromGradeSeparation.get() : toGradeSeparation.get());
            }
        }

        return edges;
    }

    private Location firstWayLocation(final PbfWay way)
    {
        final var nodes = way.nodes();
        if (!nodes.isEmpty())
        {
            return nodeToLocation(nodes.get(0));
        }
        return null;
    }

    private Location lastWayLocation(final PbfWay way)
    {
        final var nodes = way.nodes();
        if (!nodes.isEmpty())
        {
            return nodeToLocation(nodes.get(nodes.size() - 1));
        }
        return null;
    }

    private Result processNode(final GraphStore store, final PbfNode node)
    {
        // HOTSPOT: This method has been determined to be a hotspot by YourKit profiling

        // Extract any place
        final var place = placeExtractor.extract(node);
        if (place != null)
        {
            placeAdder.get().add(place);
        }

        // and if the subclass accepts the node for processing
        if (onProcessingNode(store, node) == ProcessingDirective.ACCEPT)
        {
            // and the node is on a way that we care about,
            if (shouldStoreNodeLocation(node))
            {
                // then store the location of the node for later use when processing ways.
                // To do this, we convert the latitude and longitude to DM7. Normally,
                // this would be done by Location.degrees(latitude, longitude).asLong()
                // but YourKit shows this code to be a hotspot, so we do that inline here.
                final var latitude = (long) (node.latitude() * 1_000_000_0);
                final var longitude = (long) (node.longitude() * 1_000_000_0);
                final var locationAsLong = (latitude << 32) | (longitude & 0xffff_ffffL);
                synchronized (nodeIdentifierToLocation)
                {
                    nodeIdentifierToLocation.put(node.identifierAsLong(), locationAsLong);
                }
            }

            onProcessedNode(store, node);

            return ACCEPTED;
        }
        return FILTERED_OUT;
    }

    private Result processRelation(final GraphStore store, final PbfRelation relation)
    {
        final var tags = relation.tagMap();

        if (!"administrative".equals(tags.get("boundary"))
                && configuration().relationFilter().accepts(relation)
                && onProcessingRelation(store, relation) == ProcessingDirective.ACCEPT)
        {
            // Add the relation,
            final var newRelation = new HeavyWeightRelation(relation);
            relationAdder.get().add(newRelation);

            // then go through relation members
            for (final var member : relation.members())
            {
                // and if the member is a way
                if (member.getMemberType() == EntityType.Way)
                {
                    // get the member as a way identifier
                    final var wayIdentifier = new PbfWayIdentifier(member.getMemberId());

                    // and find the route, if any, for the way
                    final var route = store.graph().routeForWayIdentifier(wayIdentifier);
                    if (route != null)
                    {
                        // and go through edges in the route
                        for (final var edge : route)
                        {
                            // naming each edge based on the relation
                            addRouteName(store, edge, relation);

                            // and storing the relation for the edge
                            store.edgeStore().storeRelation(edge, newRelation);
                        }
                    }
                    else
                    {
                        // In many PBF input files, relations do reference non-existent ways, so this is a debug
                        // statement rather than a warning
                        RawPbfGraphLoader.DEBUG.quibble("Relation $ references missing way identifier $", relation.identifierAsLong(), wayIdentifier);
                    }
                }
            }

            onProcessedRelation(store, relation);

            return ACCEPTED;
        }
        else
        {
            return FILTERED_OUT;
        }
    }

    private Result processWay(final GraphStore store, final PbfWay way)
    {
        // If the way is accepted by the way filter, it should be processed
        if (configuration().wayFilter().accepts(way) && onProcessingWay(store, way) == ProcessingDirective.ACCEPT)
        {
            // then get the first and last locations of the way
            final var firstWayLocation = firstWayLocation(way);
            final var lastWayLocation = lastWayLocation(way);

            // and if either location is missing (because the way references a non-existent node),
            if (firstWayLocation == null && lastWayLocation == null)
            {
                // we must discard the way
                return DISCARDED;
            }

            // otherwise if this way is a place (yes, ways can be places),
            final var place = placeExtractor.extract(way);
            if (place != null)
            {
                // add it to the place store
                placeAdder.get().add(place);
            }
            else
            {
                // and finally, if the tags are acceptable and the end points are in our constraint bounds
                if (bounds.contains(firstWayLocation) || bounds.contains(lastWayLocation))
                {
                    // then break the way down into edges
                    final var edges = extractEdges(store, way);
                    if (edges.isEmpty())
                    {
                        return DISCARDED;
                    }
                    else
                    {
                        for (final Edge edge : edges)
                        {
                            // and add them to the store.
                            edgeAdder.get().add(edge);
                        }

                        onProcessedWay(store, way);

                        return ACCEPTED;
                    }
                }
            }
        }
        return FILTERED_OUT;
    }

    /**
     * @return The given way broken into chunks if we're clean cutting and it crosses the clean-cutting region, or in a
     * single chunk if we're not clean-cutting.
     */
    private List<WayChunk> wayChunks(final GraphStore store, final PbfWay way)
    {
        // If we're clean cutting a soft-cut way
        if (configuration().cleanCutTo() != null && way.hasKey("telenav:softcut"))
        {
            // return a list of polylines for the way cut against the cleanCutTo region
            return cleanCut(store, way);
        }
        else
        {
            // return the way as a single chunk
            final var chunk = chunk(store, way);
            if (chunk != null)
            {
                return Collections.singletonList(chunk);
            }
            else
            {
                return Collections.emptyList();
            }
        }
    }
}
