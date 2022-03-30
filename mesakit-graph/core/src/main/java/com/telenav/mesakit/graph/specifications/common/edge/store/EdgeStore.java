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

package com.telenav.mesakit.graph.specifications.common.edge.store;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.registry.RegistryTrait;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.BitCount;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.primitive.collections.array.packed.SplitPackedArray;
import com.telenav.kivakit.primitive.collections.array.scalars.ByteArray;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitByteArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitIntArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitLongArray;
import com.telenav.kivakit.primitive.collections.list.store.LongLinkedListStore;
import com.telenav.kivakit.primitive.collections.map.multi.dynamic.LongToIntMultiMap;
import com.telenav.kivakit.primitive.collections.map.scalars.IntToByteMap;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToIntMap;
import com.telenav.kivakit.primitive.collections.map.scalars.fixed.LongToLongFixedMultiMap;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.serialization.kryo.KryoSerializationSessionFactory;
import com.telenav.kivakit.validation.Validatable;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.EdgeRelation;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.identifiers.GraphElementIdentifier;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.GraphLimits;
import com.telenav.mesakit.graph.GraphLimits.Limit;
import com.telenav.mesakit.graph.specifications.common.edge.EdgeAttributes;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeBulkSpatialIndexer;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeSpatialIndex;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeSpatialIndexKryoSerializer;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline.SplitPolylineStore;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.roadname.RoadNameStore;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.road.model.BridgeType;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadSurface;
import com.telenav.mesakit.map.road.model.RoadType;
import com.telenav.mesakit.map.road.model.SpeedCategory;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.telenav.kivakit.primitive.collections.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.mesakit.graph.GraphElement.NULL_IDENTIFIER;
import static com.telenav.mesakit.graph.GraphElement.VALIDATE_RAW;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

/**
 * An efficient store of edge information used by the flyweight {@link Edge} object to retrieve attributes. A sequence
 * of the edges in this store can be retrieved with {@link #edges()}. The store is also {@link Iterable} because it
 * inherits from {@link GraphElementStore}, so the "advanced" for loop can be used to process all the edges in the
 * store. The existence of an edge in the store can be determined with {@link #contains(Edge)}.
 * <p>
 * When an edge is added to this store the {@link #onAdd(Edge)} method is called. Adding the edge involves storing edge
 * attributes with calls to several store* methods, as well as creating vertexes and storing connectivity information in
 * the {@link VertexStore}. When edges are added, they can be fused together if {@link #merge(boolean)} is set to true.
 * This allows two graphs to be merged together into a coherent whole.
 * <p>
 * When edges are added to the store, only the forward edge is stored if the edge is two-way to increase storage
 * efficiency. When edges are retrieved, a negative {@link EdgeIdentifier} signals that the data for the forward edge
 * should be flipped around to return the reverse edge. Whether an edge is forward or reversed can be determined with
 * {@link Edge#isForward()} and {@link Edge#isReverse()}. Because only forward edges are stored, the size of the store
 * as returned by {@link #size()} will be smaller than from the number of edges in the store as returned by {@link
 * #count()}. In some instances it is desirable to retrieve only the forward edges in the store. This can be achieved
 * with {@link #forwardEdges()}.
 * <p>
 * A mapping is maintained between {@link EdgeIdentifier} and an index, which is stored as a property of each {@link
 * Edge} (it is stored by the parent class {@link GraphElement} along with the edge's {@link Graph} and its {@link
 * GraphElementIdentifier}). This index can be retrieved with {@link Edge#index()} and is used to efficiently retrieve
 * values from the store with calls to the various retrieve* methods.
 * <p>
 * An edge store maintains an {@link RTreeSpatialIndex} of the edge poly-lines to allow fast spatial searches, such as
 * finding all the edges that intersect a given {@link Rectangle}. This index is also used when querying {@link
 * EdgeRelation}s by finding the relevant edges and then returning the set of all relations that those edges reference.
 * <p>
 * This store {@link Validatable} so that it can be validated before saving to a {@link GraphArchive} with a call to
 * {@link #validator(ValidationType)}. See {@link Validatable} and {@link Validator} for details of how validation
 * works.
 *
 * @author jonathanl (shibo)
 * @see GraphElementStore
 * @see ArchivedGraphElementStore
 * @see RTreeSpatialIndex
 * @see Validatable
 * @see Validator
 * @see Edge
 * @see EdgeAttributes
 */
@SuppressWarnings("unused")
public abstract class EdgeStore extends ArchivedGraphElementStore<Edge> implements RegistryTrait
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** ============ The bottom left location of the bounds around each edge in this store */
    private final AttributeReference<SplitLongArray> BOUNDS_BOTTOM_LEFT = new AttributeReference<>(this, EdgeAttributes.get().BOUNDS_BOTTOM_LEFT, "boundsBottomLeft",
            () -> (SplitLongArray) new SplitLongArray("boundsBottomLeft")
                    .hasNullLong(false)
                    .initialSize(estimatedElements()));

    /** ============ The top right location of the bounds around each edge in this store */
    private final AttributeReference<SplitLongArray> BOUNDS_TOP_RIGHT = new AttributeReference<>(this, EdgeAttributes.get().BOUNDS_TOP_RIGHT, "boundsTopRight",
            () -> (SplitLongArray) new SplitLongArray("boundsTopRight")
                    .hasNullLong(false)
                    .initialSize(estimatedElements()));

    /** ============ The bridge type for each edge in this store */
    private final AttributeReference<SplitPackedArray> BRIDGE_TYPE = new AttributeReference<>(this, EdgeAttributes.get().BRIDGE_TYPE, "bridgeType",
            () -> (SplitPackedArray) new SplitPackedArray("bridgeType")
                    .bits(BitCount._3, NO_OVERFLOW)
                    .nullLong(7)
                    .initialSize(estimatedElements()));

    /** ============ The country for each edge in this store */
    private final AttributeReference<SplitPackedArray> COUNTRY = new AttributeReference<>(this, EdgeAttributes.get().COUNTRY, "country",
            () -> (SplitPackedArray) new SplitPackedArray("country")
                    .bits(BitCount._8, NO_OVERFLOW)
                    .nullLong(255)
                    .initialSize(estimatedElements()));

    /** ============ The free flow speed category for each edge in this store */
    private final AttributeReference<SplitPackedArray> FREE_FLOW_SPEED_CATEGORY = new AttributeReference<>(this, EdgeAttributes.get().FREE_FLOW_SPEED_CATEGORY, "freeFlowSpeedCategory",
            () -> (SplitPackedArray) new SplitPackedArray("freeFlowSpeedCategory")
                    .bits(BitCount._5, NO_OVERFLOW)
                    .initialSize(estimatedElements()));

    /** ============ The "from" node identifier for each edge in the store, if any */
    private final AttributeReference<SplitLongArray> FROM_NODE_IDENTIFIER = new AttributeReference<>(this, EdgeAttributes.get().FROM_NODE_IDENTIFIER, "fromNodeIdentifier",
            () -> (SplitLongArray) new SplitLongArray("fromNodeIdentifier").initialSize(estimatedElements()));

    /** ============ The 'from' vertex identifier for each edge in this store */
    private final AttributeReference<SplitIntArray> FROM_VERTEX_IDENTIFIER = new AttributeReference<>(this, EdgeAttributes.get().FROM_VERTEX_IDENTIFIER, "fromVertexIdentifier",
            () -> (SplitIntArray) new SplitIntArray("fromVertexIdentifier")
                    .initialSize(estimatedElements()));

    /** ============ The number of HOV lanes for each edge in this store */
    private final AttributeReference<SplitPackedArray> HOV_LANE_COUNT = new AttributeReference<>(this, EdgeAttributes.get().HOV_LANE_COUNT, "hovLaneCount",
            () -> (SplitPackedArray) new SplitPackedArray("hovLaneCount")
                    .bits(BitCount._2, NO_OVERFLOW)
                    .nullLong(3)
                    .initialSize(estimatedElements()));

    /** ============ The closed to through traffic state for each edge in this store */
    private final AttributeReference<IntToByteMap> IS_CLOSED_TO_THROUGH_TRAFFIC = new AttributeReference<>(this, EdgeAttributes.get().IS_CLOSED_TO_THROUGH_TRAFFIC, "isClosedToThroughTraffic",
            () -> (IntToByteMap) new IntToByteMap("isClosedToThroughTraffic")
                    .initialSize(estimatedElements()));

    /** ============ The toll road state for each edge in this store */
    private final AttributeReference<IntToByteMap> IS_TOLL_ROAD =
            new AttributeReference<>(this, EdgeAttributes.get().IS_TOLL_ROAD, "isTollRoad",
                    () -> (IntToByteMap) new IntToByteMap("isTollRoad")
                            .initialSize(estimatedElements()));

    /** ============ The under construction state for each edge in this store */
    private final AttributeReference<IntToByteMap> IS_UNDER_CONSTRUCTION = new AttributeReference<>(this, EdgeAttributes.get().IS_UNDER_CONSTRUCTION, "isUnderConstruction",
            () -> (IntToByteMap) new IntToByteMap("isUnderConstruction")
                    .initialSize(estimatedElements()));

    /** ============ The lane count for each edge in this store */
    private final AttributeReference<SplitPackedArray> LANE_COUNT = new AttributeReference<>(this, EdgeAttributes.get().LANE_COUNT, "laneCount",
            () -> (SplitPackedArray) new SplitPackedArray("laneCount")
                    .bits(BitCount._6, NO_OVERFLOW)
                    .nullLong(0)
                    .initialSize(estimatedElements()));

    /** ============ The length of each edge in this store in millimeters (2^32mm ~= 4,295km) */
    private final AttributeReference<SplitIntArray> LENGTH_IN_MILLIMETERS = new AttributeReference<>(this, EdgeAttributes.get().LENGTH, "lengthInMillimeters",
            () -> (SplitIntArray) new SplitIntArray("lengthInMillimeters")
                    .nullInt(Integer.MIN_VALUE)
                    .initialSize(estimatedElements()));

    /** ============ The function class for edges in this store */
    private final AttributeReference<SplitPackedArray> ROAD_FUNCTIONAL_CLASS =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_FUNCTIONAL_CLASS, "roadFunctionalClass",
                    () -> (SplitPackedArray) new SplitPackedArray("roadFunctionalClass")
                            .bits(BitCount._3, NO_OVERFLOW)
                            .nullLong(7)
                            .initialSize(estimatedElements()));

    /** ============ The names for edges in this store */
    private final AttributeReference<RoadNameStore> ROAD_NAME =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_NAMES, "roadName",
                    () -> new RoadNameStore("roadName", estimatedElements(), metadata()))
            {
                @Override
                protected void onLoaded(RoadNameStore store)
                {
                    super.onLoaded(store);
                    store.codec(metadata().roadNameCharacterCodec());
                }
            };

    /** ============ The polyline stores for the shapes of edges in this store */
    private final AttributeReference<SplitPolylineStore> ROAD_SHAPE =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_SHAPE, "roadShape",
                    () -> new SplitPolylineStore("roadShape",
                            Limit.EDGES, Maximum._32768,
                            estimatedElements().asEstimate(), Estimate._32768));

    /** ============ The state of edges in the store (null, one way, two-way, closed) */
    private final AttributeReference<ByteArray> ROAD_STATE =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_STATE, "roadState",
                    () -> (ByteArray) new ByteArray("roadState")
                            .initialSize(estimatedElements()));

    /** ============ The road subtype for edges in this store */
    private final AttributeReference<SplitPackedArray> ROAD_SUB_TYPE =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_SUB_TYPE, "roadSubType",
                    () -> (SplitPackedArray) new SplitPackedArray("roadSubType")
                            .bits(BitCount._4, NO_OVERFLOW)
                            .nullLong(15)
                            .initialSize(estimatedElements()));

    /** ============ The road surface types of edges in this store */
    private final AttributeReference<SplitPackedArray> ROAD_SURFACE =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_SURFACE, "roadSurface",
                    () -> (SplitPackedArray) new SplitPackedArray("roadSurface")
                            .bits(BitCount._3, NO_OVERFLOW)
                            .nullLong(7)
                            .initialSize(estimatedElements()));

    /** ============ The road type for edges in this store */
    private final AttributeReference<SplitPackedArray> ROAD_TYPE =
            new AttributeReference<>(this, EdgeAttributes.get().ROAD_TYPE, "roadType",
                    () -> (SplitPackedArray) new SplitPackedArray("roadType")
                            .bits(BitCount._5, NO_OVERFLOW)
                            .nullLong(31)
                            .initialSize(estimatedElements()));

    /** ============ The speed limit on edges in this store as a multiple of 5kph */
    private final AttributeReference<SplitByteArray> SPEED_LIMIT =
            new AttributeReference<>(this, EdgeAttributes.get().SPEED_LIMIT, "speedLimit",
                    () -> (SplitByteArray) new SplitByteArray("speedLimit")
                            .nullByte((byte) 0)
                            .initialSize(estimatedElements()));

    /** ============ The speed limit on edges in this store */
    private final AttributeReference<SplitIntArray> SPEED_PATTERN_IDENTIFIER =
            new AttributeReference<>(this, EdgeAttributes.get().SPEED_PATTERN_IDENTIFIER, "speedPatternIdentifier",
                    () -> (SplitIntArray) new SplitIntArray("speedPatternIdentifier").initialSize(estimatedElements()));

    /** ============ The "to" node identifier for each edge in the store, if any */
    private final AttributeReference<SplitLongArray> TO_NODE_IDENTIFIER =
            new AttributeReference<>(this, EdgeAttributes.get().TO_NODE_IDENTIFIER, "toNodeIdentifier",
                    () -> (SplitLongArray) new SplitLongArray("toNodeIdentifier").initialSize(estimatedElements()));

    /** ============ The "to" vertex identifiers for edges in this store */
    private final AttributeReference<SplitIntArray> TO_VERTEX_IDENTIFIER =
            new AttributeReference<>(this, EdgeAttributes.get().TO_VERTEX_IDENTIFIER, "toVertexIdentifier",
                    () -> (SplitIntArray) new SplitIntArray("toVertexIdentifier").initialSize(estimatedElements()));

    /** ============ A reverse map from way identifier back to edge identifiers */
    private final AttributeReference<LongToIntMultiMap> WAY_IDENTIFIER_TO_EDGE_INDEX =
            new AttributeReference<>(this, EdgeAttributes.get().WAY_IDENTIFIER_TO_EDGE_IDENTIFIER, "wayIdentifierToEdgeIndex",
                    () -> (LongToIntMultiMap) new LongToIntMultiMap("wayIdentifierToEdgeIndex")
                            .initialSize(metadata().wayCount(ALLOW_ESTIMATE).asEstimate()));

    /** ============ The relation identifiers of ways */
    private final AttributeReference<LongToLongFixedMultiMap> WAY_IDENTIFIER_TO_RELATIONS = new AttributeReference<>(this, EdgeAttributes.get().RELATIONS, "wayIdentifierToRelationIdentifiers",
            () -> (LongToLongFixedMultiMap) new LongToLongFixedMultiMap("wayIdentifierToRelationIdentifiers")
                    .initialChildSize(GraphLimits.Estimated.RELATIONS_PER_EDGE)
                    .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongArray boundsBottomLeft;

    @KivaKitArchivedField
    private SplitLongArray boundsTopRight;

    @KivaKitArchivedField
    private SplitPackedArray bridgeType;

    /** Used by tests */
    private boolean commitSpatialIndex = true;

    /**
     * The number of edges in this graph (this is different from edgeIndex because edge count reflects two-way edges,
     * while edge index does not)
     */
    @KivaKitArchivedField
    private int count;

    @KivaKitArchivedField
    private SplitPackedArray country;

    @KivaKitArchivedField
    private SplitPackedArray freeFlowSpeedCategory;

    @KivaKitArchivedField
    private SplitLongArray fromNodeIdentifier;

    @KivaKitArchivedField
    private SplitIntArray fromVertexIdentifier;

    @KivaKitArchivedField
    private SplitPackedArray hovLaneCount;

    @KivaKitArchivedField
    private IntToByteMap isClosedToThroughTraffic;

    @KivaKitArchivedField
    private IntToByteMap isTollRoad;

    @KivaKitArchivedField
    private IntToByteMap isUnderConstruction;

    @KivaKitArchivedField
    private SplitPackedArray laneCount;

    @KivaKitArchivedField
    private SplitIntArray lengthInMillimeters;

    /** True if merging edges into this store */
    private boolean merging;

    /** Next relation identifier */
    private RelationIdentifier nextRelationIdentifier = new RelationIdentifier(1_000_000_000);

    @KivaKitArchivedField
    private SplitPackedArray roadFunctionalClass;

    @KivaKitArchivedField
    private RoadNameStore roadName;

    @KivaKitArchivedField
    private SplitPolylineStore roadShape;

    @KivaKitArchivedField
    private ByteArray roadState;

    @KivaKitArchivedField
    private SplitPackedArray roadSubType;

    @KivaKitArchivedField
    private SplitPackedArray roadSurface;

    @KivaKitArchivedField
    private SplitPackedArray roadType;

    /** A spatial index for the edges in this graph */
    @KivaKitArchivedField(lazy = true)
    private CompressedEdgeSpatialIndex spatialIndex;

    @KivaKitArchivedField
    private SplitByteArray speedLimit;

    @KivaKitArchivedField
    private SplitIntArray speedPatternIdentifier;

    /** Edges that are clean cut by location */
    private transient Map<Location, Edge> temporaryCleanCuts = new HashMap<>();

    private LongLinkedListStore temporaryRelations;

    private LongToIntMap temporaryWayIdentifierToRelationIndex;

    @KivaKitArchivedField
    private SplitLongArray toNodeIdentifier;

    @KivaKitArchivedField
    private SplitIntArray toVertexIdentifier;

    @KivaKitArchivedField
    private LongToIntMultiMap wayIdentifierToEdgeIndex;

    @KivaKitArchivedField
    private LongToLongFixedMultiMap wayIdentifierToRelationIdentifiers;

    protected EdgeStore(Graph graph)
    {
        super(graph);
    }

    /**
     * Adds the given edges matching the given constraints to this edge store.
     *
     * @return The number of directional edges added
     */
    public Count addAll(EdgeSequence edges, GraphConstraints constraints)
    {
        var adder = adder();
        var start = Time.now();
        var count = 0;
        for (var edge : edges)
        {
            if (constraints.includes(edge))
            {
                if (edge.isForward())
                {
                    adder.add(edge);
                    count++;
                    if (edge.isTwoWay())
                    {
                        count++;
                    }
                }
            }
        }
        information("Added $ edges, discarded $ in $", Count.count(size()), discarded(), start.elapsedSince());
        return Count.count(count);
    }

    /**
     * Used in testing to eliminate generation of a spatial index when that is not useful
     */
    public void commitSpatialIndex(boolean commitSpatialIndex)
    {
        this.commitSpatialIndex = commitSpatialIndex;
    }

    /**
     * @return True if this store contains the given edge
     */
    public boolean contains(Edge edge)
    {
        return contains(edge.identifier());
    }

    /**
     * @return True if this store contains the given edge identifier
     */
    public boolean contains(EdgeIdentifier identifier)
    {
        return containsIdentifier(identifier.asLong());
    }

    /**
     * @return True if this store contains the given way identifier
     */
    public boolean contains(PbfWayIdentifier identifier)
    {
        WAY_IDENTIFIER_TO_EDGE_INDEX.load();
        return wayIdentifierToEdgeIndex.containsKey(identifier.asLong());
    }

    /**
     * Determines if this store contains the edge identifier. If the edge identifier is negative, then the store
     * contains the identifier only if the edge is a two-way road.
     *
     * @return True if this store contains the given directional edge identifier.
     */
    @Override
    public boolean containsIdentifier(long identifier)
    {
        // If the edge is a forward edge
        if (identifier > 0)
        {
            // then it's in the graph if the index contains the edge identifier
            return super.containsIdentifier(identifier);
        }
        else
        {
            // If the edge is a reverse edge and the graph contains the forward edge identifier
            var forward = -identifier;
            if (super.containsIdentifier(forward))
            {
                // then it's in the graph if the forward edge is a reversible road
                return isTwoWay(identifierToIndex(identifier, IndexingMode.GET));
            }

            return false;
        }
    }

    /**
     * @return The number of directional edges in this store
     */
    @Override
    public int count()
    {
        return count;
    }

    /**
     * @return The edge for the given index. If the index is negative, then the reverse edge is returned.
     */
    public Edge edgeForIndex(int index)
    {
        var negative = index < 0;
        var absoluteIndex = negative ? -index : index;
        var identifier = retrieveIdentifier(absoluteIndex);
        if (identifier <= 0)
        {
            return null;
        }
        return dataSpecification().newEdge(graph(), negative ? -identifier : identifier);
    }

    /**
     * @return The sequence of all edges in this graph
     */
    public EdgeSequence edges()
    {
        // The sequence of forward edges in this store as directional edges
        return new EdgeSequence(this).asDirectional();
    }

    /**
     * @return The sequence of all forward edges in this graph. This sequence does not include reverse edges.
     */
    public EdgeSequence forwardEdges()
    {
        return new EdgeSequence(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return estimatedElements();
    }

    public boolean isOneWay(Edge edge)
    {
        return isOneWay(edge.index());
    }

    public boolean isTwoWay(Edge edge)
    {
        return isTwoWay(edge.index());
    }

    /**
     * Sets the merge state of this store. When merging is enabled, clean cut edges that are added will be fused
     * together to form a single connected graph.
     */
    public void merge(boolean merging)
    {
        this.merging = merging;
        if (merging)
        {
            for (var edge : forwardEdges())
            {
                if (edge.fromNodeIdentifier() == null)
                {
                    temporaryCleanCuts.put(edge.fromLocation(), edge);
                }
                if (edge.toNodeIdentifier() == null)
                {
                    temporaryCleanCuts.put(edge.toLocation(), edge);
                }
            }
        }
    }

    /**
     * Ways are split into edges during OSM loading. An edge identifier is derived from a way identifier by shifting the
     * way identifier N decimal places to the left and then populating these digits with information regarding the
     * edge's place in the sequence of edges that was extracted from the way. For details, see {@link EdgeIdentifier}.
     *
     * @return The next unique edge identifier for the given way identifier
     * @see EdgeIdentifier
     */
    public long nextEdgeIdentifier(long wayIdentifier)
    {
        var edgeIdentifier = wayIdentifier * EdgeIdentifier.SEQUENCE_NUMBER_SHIFT;
        while (containsIdentifier(edgeIdentifier))
        {
            edgeIdentifier = edgeIdentifier + EdgeIdentifier.SEQUENCE_NUMBER_EDGE_SECTION_INCREMENT;
        }
        return edgeIdentifier;
    }

    @Override
    public String objectName()
    {
        return "edge-store";
    }

    /**
     * Initializes this edge store by resetting the count and the next index.
     */
    @Override
    public void onInitialize()
    {
        super.onInitialize();

        count = 0;

        resetIndex();

        temporaryWayIdentifierToRelationIndex = new LongToIntMap(objectName() + ".temporaryEdgeIdentifierToRelationsIndex");
        temporaryWayIdentifierToRelationIndex.initialize();

        temporaryRelations = new LongLinkedListStore(objectName() + ".temporaryRelations");
        temporaryRelations.initialize();
    }

    /**
     * @return The bounds of the given edge
     */
    public final Rectangle retrieveBounds(Edge edge)
    {
        BOUNDS_BOTTOM_LEFT.load();
        BOUNDS_TOP_RIGHT.load();

        var index = edge.index();
        var bottomLeft = boundsBottomLeft.get(index);
        var topRight = boundsTopRight.get(index);
        return Rectangle.fromLongs(bottomLeft, topRight);
    }

    /**
     * @return The bridge type for the given edge
     */
    public final BridgeType retrieveBridgeType(Edge edge)
    {
        return BRIDGE_TYPE.retrieveObject(edge, value -> BridgeType.forIdentifier((int) value));
    }

    /**
     * @return The country for the given edge, as determined by the 'from' vertex.
     */
    public final Country retrieveCountry(Edge edge)
    {
        COUNTRY.load();
        if (country != null)
        {
            var identifierByte = (byte) country.safeGet(edge.index());
            if (!country.isNull(identifierByte))
            {
                var identifier = identifierByte & 0xff;
                return Country.forIdentifier(new RegionIdentifier(identifier));
            }
        }
        return null;
    }

    /**
     * @return The number of non-directional forward edges
     */
    public Count retrieveForwardEdgeCount()
    {
        return Count.count(super.size());
    }

    /**
     * @return Free flow for the edge
     * @see SpeedCategory
     */
    public final SpeedCategory retrieveFreeFlow(Edge edge)
    {
        if (freeFlowSpeedCategory == null)
        {
            FREE_FLOW_SPEED_CATEGORY.load();
        }
        return SpeedCategory.forIdentifier((int) freeFlowSpeedCategory.safeGet(edge.index()));
    }

    /**
     * @return The node identifier of the 'from' vertex
     */
    public final MapNodeIdentifier retrieveFromNodeIdentifier(Edge edge)
    {
        if (edge.isReverse())
        {
            return TO_NODE_IDENTIFIER.retrieveObject(edge, PbfNodeIdentifier::new);
        }
        else
        {
            return FROM_NODE_IDENTIFIER.retrieveObject(edge, PbfNodeIdentifier::new);
        }
    }

    /**
     * @return The vertex identifier of the 'from' vertex
     */
    public final int retrieveFromVertexIdentifier(Edge edge)
    {
        if (edge.isReverse())
        {
            if (toVertexIdentifier == null)
            {
                TO_VERTEX_IDENTIFIER.load();
            }
            return toVertexIdentifier.safeGet(edge.index());
        }
        else
        {
            if (fromVertexIdentifier == null)
            {
                FROM_VERTEX_IDENTIFIER.load();
            }
            return fromVertexIdentifier.safeGet(edge.index());
        }
    }

    /**
     * @return The number of HOV lanes for the given edge
     */
    public final Count retrieveHovLaneCount(Edge edge)
    {
        return HOV_LANE_COUNT.retrieveObject(edge, Count::count);
    }

    /**
     * @return True if the edge is closed to through traffic
     */
    public final boolean retrieveIsClosedToThroughTraffic(Edge edge)
    {
        return IS_CLOSED_TO_THROUGH_TRAFFIC.retrieveBoolean(edge);
    }

    /**
     * @return True if the given edge is on a toll road
     */
    public final boolean retrieveIsTollRoad(Edge edge)
    {
        return IS_TOLL_ROAD.retrieveBoolean(edge);
    }

    /**
     * @return True if the given edge is under construction
     */
    public final boolean retrieveIsUnderConstruction(Edge edge)
    {
        return IS_UNDER_CONSTRUCTION.retrieveBoolean(edge);
    }

    /**
     * @return The number of lanes for the given edge
     */
    public final Count retrieveLaneCount(Edge edge)
    {
        return LANE_COUNT.retrieveObject(edge, Count::count);
    }

    /**
     * @return The length of the given edge as a unit-less {@link Distance} value
     */
    public final long retrieveLengthInMillimeters(Edge edge)
    {
        if (lengthInMillimeters == null)
        {
            LENGTH_IN_MILLIMETERS.load();
        }

        var millimeters = lengthInMillimeters.safeGet(edge.index());

        // If we have a zero length edge (should not happen, but could), return 1mm distance to prevent
        // divide-by-zero errors
        return millimeters == 0 ? 1 : millimeters;
    }

    /**
     * @return The relations for which the given edge is a member
     * @see EdgeRelation
     */
    public final Set<EdgeRelation> retrieveRelations(Edge edge)
    {
        WAY_IDENTIFIER_TO_RELATIONS.load();

        if (wayIdentifierToRelationIdentifiers != null)
        {
            var relations = wayIdentifierToRelationIdentifiers.get(edge.mapIdentifier().asLong());
            if (relations != null)
            {
                Set<EdgeRelation> set = new HashSet<>();
                var iterator = relations.iterator();
                while (iterator.hasNext())
                {
                    var relation = iterator.next();
                    set.add(graph().relationForIdentifier(new RelationIdentifier(relation)));
                }
                return set;
            }
        }
        return Collections.emptySet();
    }

    /**
     * @return The functional class of the given edge
     * @see RoadFunctionalClass
     */
    public final RoadFunctionalClass retrieveRoadFunctionalClass(Edge edge)
    {
        return ROAD_FUNCTIONAL_CLASS.retrieveObject(edge,
                value -> RoadFunctionalClass.forIdentifier((int) value));
    }

    /**
     * @param type The type of road name to retrieve for example, the official or alternate name
     * @return The road names of the given type for the given edge
     * @see RoadName.Type
     */
    public final List<RoadName> retrieveRoadNames(Edge edge, RoadName.Type type)
    {
        ROAD_NAME.load();
        if (roadName != null)
        {
            List<RoadName> names = new ArrayList<>();
            for (var index = 0; index < 4; index++)
            {
                var name = roadName.get(edge, type, index);
                if (name != null)
                {
                    names.add(name);
                }
                else
                {
                    break;
                }
            }
            return names;
        }
        return Collections.emptyList();
    }

    /**
     * @return The shape of the given edge as a {@link Polyline}, or null if the road is segmental
     */
    public final Polyline retrieveRoadShape(Edge edge)
    {
        ROAD_SHAPE.load();
        return roadShape.get(edge);
    }

    /**
     * @return The state of the given road among {@link RoadState#ONE_WAY}, {@link RoadState#TWO_WAY} and {@link
     * RoadState#CLOSED}
     */
    public final RoadState retrieveRoadState(Edge edge)
    {
        if (roadState == null)
        {
            ROAD_STATE.load();
        }
        return RoadState.forIdentifier(roadState.safeGet(edge.index()));
    }

    /**
     * @return The road subtype, for example, a main road, a ramp or a bridge.
     * @see RoadSubType
     */
    public final RoadSubType retrieveRoadSubType(Edge edge)
    {
        return ROAD_SUB_TYPE.retrieveObject(edge, value -> RoadSubType.forIdentifier((int) value));
    }

    /**
     * @return The road surface, among paved, unpaved and poor condition
     * @see RoadSurface
     */
    public final RoadSurface retrieveRoadSurface(Edge edge)
    {
        return ROAD_SURFACE.retrieveObject(edge, value -> RoadSurface.forIdentifier((int) value));
    }

    /**
     * @return The type of road, for example a highway, local road or freeway
     */
    public final RoadType retrieveRoadType(Edge edge)
    {
        if (roadType == null)
        {
            ROAD_TYPE.load();
        }
        return RoadType.forIdentifier((int) roadType.safeGet(edge.index()));
    }

    /**
     * @return The sequence of edges that correspond to the given way identifier
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public Route retrieveRouteForWayIdentifier(MapWayIdentifier wayIdentifier)
    {
        assert wayIdentifier != null;

        WAY_IDENTIFIER_TO_EDGE_INDEX.load();

        // NOTE: These edges will always be in order because edges are added to the store in the
        // order they appear from way sectioning
        var edges = wayIdentifierToEdgeIndex.iterator(wayIdentifier.asLong());
        var set = new EdgeSet(Estimate._32);
        if (edges != null)
        {
            while (edges.hasNext())
            {
                var index = edges.next();
                set.add(dataSpecification().newEdge(graph(), NULL_IDENTIFIER, index));
            }
        }
        else
        {
            // When processing relations, very long relations like interstate highways, will produce a huge number of
            // these debug traces, so this line is commented out until it's (temporarily) needed during debugging.
            // DEBUG.trace("No edges found for way identifier $", wayIdentifier);
        }

        var routes = set.asRoutes();
        return routes.size() == 1 ? routes.get(0) : null;
    }

    /**
     * @return The speed limit along the given edge
     */
    public final Speed retrieveSpeedLimit(Edge edge)
    {
        return SPEED_LIMIT.retrieveObject(edge, value ->
        {
            if (!speedLimit.isNull((int) value))
            {
                // Magnitude is always in increments of 5 kph as that's the case for 99% of speed
                // limits. If you have a posted speed limit of 9mph or something it will need to be
                // rounded down to 5.
                return Speed.kilometersPerHour(value * 5.0);
            }
            return null;
        });
    }

    /**
     * @return The node identifier of the "to" vertex of the edge
     */
    public final MapNodeIdentifier retrieveToNodeIdentifier(Edge edge)
    {
        if (edge.isForward())
        {
            return TO_NODE_IDENTIFIER.retrieveObject(edge, PbfNodeIdentifier::new);
        }
        else
        {
            return FROM_NODE_IDENTIFIER.retrieveObject(edge, PbfNodeIdentifier::new);
        }
    }

    /**
     * @return The identifier of the "to" vertex of the edge
     */
    public final int retrieveToVertexIdentifier(Edge edge)
    {
        if (edge.isReverse())
        {
            if (fromVertexIdentifier == null)
            {
                FROM_VERTEX_IDENTIFIER.load();
            }
            return fromVertexIdentifier.safeGet(edge.index());
        }
        else
        {
            if (toVertexIdentifier == null)
            {
                TO_VERTEX_IDENTIFIER.load();
            }
            return toVertexIdentifier.safeGet(edge.index());
        }
    }

    /**
     * @return The edge spatial index either created freshly, or loaded from any {@link GraphArchive} attached to this
     * store (by virtue of the store being loaded from a graph file)
     */
    public synchronized RTreeSpatialIndex<Edge> spatialIndex()
    {
        // If there's no spatial index
        if (spatialIndex == null)
        {
            // and there is an archive
            if (archive() != null)
            {
                // then load the spatial index from the graph file
                var start = Time.now();
                configureSerializer();
                loadField("spatial-index");
                assert spatialIndex != null : "No spatial index for " + graph().name();
                spatialIndex.graph(graph());
                graph().information("Loaded edge spatial index in $: $",
                        start.elapsedSince(), spatialIndex.statistics());
            }
            else
            {
                // otherwise, create the index from scratch
                spatialIndex = new CompressedEdgeBulkSpatialIndexer(this).index(graph());
            }
        }

        return spatialIndex;
    }

    /**
     * Stores all the simple attributes of the given edge using the given edge index
     *
     * @see GraphElementStore#retrieveIndex(GraphElement)
     */
    public synchronized void storeAttributes(Edge edge)
    {
        assert edge != null;

        super.storeAttributes(edge);

        BRIDGE_TYPE.storeObject(edge, edge.bridgeType());
        COUNTRY.storeObject(edge, edge.country());
        FREE_FLOW_SPEED_CATEGORY.storeObject(edge, edge.freeFlowSpeed());
        FROM_VERTEX_IDENTIFIER.storeObject(edge, edge.fromVertexIdentifier());
        HOV_LANE_COUNT.storeObject(edge, edge.hovLaneCount());
        var laneCount = edge.laneCount();
        if (laneCount != null)
        {
            LANE_COUNT.storeObject(edge, laneCount.minimum(Count.count(63)));
        }
        LENGTH_IN_MILLIMETERS.storeObject(edge, edge.length());
        ROAD_FUNCTIONAL_CLASS.storeObject(edge, edge.roadFunctionalClass());
        ROAD_STATE.storeObject(edge, edge.roadState());
        ROAD_TYPE.storeObject(edge, edge.roadType());
        ROAD_SURFACE.storeObject(edge, edge.roadSurface());
        TO_VERTEX_IDENTIFIER.storeObject(edge, edge.toVertexIdentifier());

        if (edge.isClosedToThroughTraffic())
        {
            IS_CLOSED_TO_THROUGH_TRAFFIC.storeBoolean(edge, true);
        }
        if (edge.isTollRoad())
        {
            IS_TOLL_ROAD.storeBoolean(edge, true);
        }
        if (edge.isUnderConstruction())
        {
            IS_UNDER_CONSTRUCTION.storeBoolean(edge, true);
        }

        if (edge.speedLimit() != null)
        {
            var kph = edge.speedLimit().asKilometersPerHour();
            var nearestMultipleOf5Kph = (kph + 2.5) / 5.0;
            SPEED_LIMIT.storeObject(edge, (byte) nearestMultipleOf5Kph);
        }

        storeRoadSubType(edge, edge.roadSubType());

        assert edge.fromNodeIdentifier() != null;
        FROM_NODE_IDENTIFIER.storeObject(edge, edge.fromNodeIdentifier());
        assert edge.toNodeIdentifier() != null;
        TO_NODE_IDENTIFIER.storeObject(edge, edge.toNodeIdentifier());

        // Store road names
        for (var type : RoadName.Type.values())
        {
            var names = edge.roadNames(type);
            if (names != null)
            {
                for (var name : names)
                {
                    storeRoadName(edge, type, name);
                }
                if (DEBUG.isDebugOn())
                {
                    var stored = new HashSet<>(names);
                    var retrieved = new HashSet<>(retrieveRoadNames(edge, type));
                    if (!retrieved.equals(stored))
                    {
                        DEBUG.warning("Stored road names: $\n Retrieved road names $", stored, retrieved);
                    }
                }
            }
        }

        // If we've got a heavy-weight edge,
        if (edge instanceof HeavyWeightEdge)
        {
            var heavy = (HeavyWeightEdge) edge;

            // store its vertex clip state (if any)
            if (heavy.isFromVertexClipped() == Boolean.TRUE)
            {
                vertexStore().clipped(edge.fromLocation());
            }
            if (heavy.isToVertexClipped() == Boolean.TRUE)
            {
                vertexStore().clipped(edge.toLocation());
            }
        }
    }

    /**
     * Stores the given speed as the free flow value for the given edge. This method is called when loading free flow
     * information in CommonGraph.loadFreeFlow()
     */
    public synchronized void storeFreeFlow(Edge edge, Speed speed)
    {
        assert speed != null;
        FREE_FLOW_SPEED_CATEGORY.storeObject(edge, SpeedCategory.forSpeed(speed));
    }

    /**
     * Stores the given "from" vertex identifier for the given edge
     */
    public synchronized void storeFromVertexIdentifier(Edge edge)
    {
        assert edge.fromVertexIdentifier() != null;
        FROM_VERTEX_IDENTIFIER.storeObject(edge, edge.fromVertexIdentifier());
    }

    /**
     * Stores the given relation for the given edge
     */
    public final synchronized void storeRelation(Edge edge, EdgeRelation relation)
    {
        assert edge != null;
        assert relation != null;

        // Get any existing list,
        var list = temporaryWayIdentifierToRelationIndex.get(edge.identifierAsLong());

        // add to the list or create it if it doesn't exist,
        list = temporaryRelations.add(list, relation.identifierAsLong());

        // and store the list for the edge.
        temporaryWayIdentifierToRelationIndex.put(edge.wayIdentifier().asLong(), list);
    }

    /**
     * Stores a road name of the given type for the given edge
     */
    public final synchronized void storeRoadName(Edge edge, RoadName.Type type, RoadName name)
    {
        assert type != null;
        assert name != null;

        ROAD_NAME.allocate();
        var count = roadName.size(edge, type);
        roadName.set(edge, type, count, name);
    }

    public final synchronized void storeRoadShape(Edge edge, Polyline shape)
    {
        long bottomLeft;
        long topRight;

        // If the edge has no shape,
        if (shape == null)
        {
            // our bounds is [from, to]
            bottomLeft = edge.fromLocationAsLong();
            topRight = edge.toLocationAsLong();
        }
        else
        {
            // otherwise, store the full road curvature in the graph's precision,
            ROAD_SHAPE.allocate();
            roadShape.set(edge, shape.compressed());

            // and get the bounds from the polyline
            var bounds = shape.bounds();
            bottomLeft = Location.toLong(bounds.bottomInDm7(), bounds.leftInDm7());
            topRight = Location.toLong(bounds.topInDm7(), bounds.rightInDm7());
        }

        // Next, store the bounds
        BOUNDS_BOTTOM_LEFT.allocate();
        BOUNDS_TOP_RIGHT.allocate();

        var index = edge.index();
        boundsBottomLeft.set(index, bottomLeft);
        boundsTopRight.set(index, topRight);

        var store = graph().graphStore();
        store.addToBounds(bottomLeft);
        store.addToBounds(topRight);
    }

    /**
     * Stores the given subtype for the given edge
     *
     * @see #retrieveRoadSubType(Edge)
     */
    public final synchronized void storeRoadSubType(Edge edge, RoadSubType subtype)
    {
        assert subtype != null;
        ROAD_SUB_TYPE.storeObject(edge, subtype);
    }

    /**
     * Stores the given "to" vertex identifier for the given edge
     */
    public synchronized void storeToVertexIdentifier(Edge edge)
    {
        assert edge.toVertexIdentifier() != null;
        TO_VERTEX_IDENTIFIER.storeObject(edge, edge.toVertexIdentifier());
    }

    /**
     * Stores a turn restriction route
     */
    public final synchronized void storeTurnRestriction(Route restriction)
    {
        assert restriction != null;
        assert restriction.size() > 0;

        // Loop through all edges in the restriction
        for (var edge : restriction)
        {
            // and for each edge, go through the existing relations for the edge
            for (var relation : retrieveRelations(edge))
            {
                // and if the relation is a turn restriction that has the same route as the
                // restriction that we're proposing to add,
                if (relation.isTurnRestriction() && relation.asRoute().equals(restriction))
                {
                    // then the turn restriction already exists, so don't add it
                    return;
                }
            }
        }

        // Get the next relation identifier for identifiers loaded from a side-file
        nextRelationIdentifier = nextRelationIdentifier.next();

        // Create a new relation for the turn restriction
        var newRelation = (HeavyWeightRelation) graph().dataSpecification()
                .newHeavyWeightRelation(graph(), nextRelationIdentifier.asLong());
        var tags = PbfTagList.create();
        tags.add(new Tag("type", "restriction"));
        newRelation.tags(tags);
        newRelation.bounds(restriction.bounds());

        // Go through each edge in the turn restriction
        for (var edge : restriction)
        {
            // get the existing relations for the edge
            Set<EdgeRelation> relations = new HashSet<>(retrieveRelations(edge));

            // add the new turn restriction relation
            relations.add(newRelation);

            // and store them again (note that space will be lost because there is no garbage
            // collection for the underlying long to long multi-map data structure, but that's
            // just how it goes)
            relations.forEach(relation -> storeRelation(edge, relation));
        }

        // Store the new relation
        graph().relationStore().adder().add(newRelation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(ValidationType validation)
    {
        var outer = this;
        return !validation.shouldValidate(getClass()) ? Validator.NULL : new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                // Validate the superclass,
                validate(EdgeStore.super.validator(validation));

                problemIf(outer.retrieveForwardEdgeCount().asInt() != size(), "forward edge count doesn't match size");

                // Go through each edge index
                for (var index = 1; index < size() && !isInvalid(); index++)
                {
                    // and check identifiers
                    problemIf(outer.fromNodeIdentifier == null, "all 'from' node identifiers are missing");
                    problemIf(outer.toNodeIdentifier == null, "all 'to' node identifiers are missing");
                    problemIf(outer.fromVertexIdentifier == null, "all 'from' vertex identifiers are missing");
                    problemIf(outer.toVertexIdentifier == null, "all 'to' vertex identifiers are missing");

                    problemIf(!Precision.DM7.isValidLocation(outer.boundsBottomLeft.get(index)), "the bottom left bounds is invalid");
                    problemIf(!Precision.DM7.isValidLocation(outer.boundsTopRight.get(index)), "the top right bounds is invalid");

                    var edgeIdentifier = retrieveIdentifier(index);

                    problemIf(isNull(outer.bridgeType, index), "the bridge type for edge ${long} at index $ is null", edgeIdentifier, index);
                    warningIf(isNull(outer.country, index), "the country for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.freeFlowSpeedCategory, index), "the free flow speed category for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.fromVertexIdentifier, index), "the from vertex identifier for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.fromNodeIdentifier, index), "the from node identifier for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.hovLaneCount, index), "the HOV lane count for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.laneCount, index), "the lane count for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.lengthInMillimeters, index), "the length for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.roadFunctionalClass, index), "the road functional class for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.roadState, index), "the road state for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.roadSubType, index), "the road sub-type for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.roadSurface, index), "the road surface for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.roadType, index), "the road type for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.toVertexIdentifier, index), "the from vertex identifier for edge ${long} is null", edgeIdentifier);
                    problemIf(isNull(outer.toNodeIdentifier, index), "the to node identifier for edge ${long} is null", edgeIdentifier);
                    problemIf(outer.lengthInMillimeters.get(index) > Distance.EARTH_CIRCUMFERENCE.asMillimeters(),
                            "the road length of edge ${long} is unreasonably long ($)", edgeIdentifier, Distance.millimeters(lengthInMillimeters.get(index)));
                }
            }
        };
    }

    @Override
    protected DataSpecification.GraphElementFactory<Edge> elementFactory()
    {
        return dataSpecification()::newEdge;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Edge> elementType()
    {
        return Edge.class;
    }

    /**
     * Adds the given edge to this store, adding attributes to store data structures in {@link EdgeStore} and the
     * ancestor class, {@link GraphElementStore}.
     */
    @Override
    protected void onAdd(Edge edge)
    {
        super.onAdd(edge);

        // Special case for graph-merging loader
        if (merging && edge.isClipped())
        {
            edge = fuse(edge);
        }

        // Get the edge forward identifier
        var identifier = Math.abs(edge.identifierAsLong());

        // and if the element identifier is already in this store
        var exists = containsIdentifier(identifier);
        if (exists)
        {
            // we make up a new element identifier using the way identifier
            identifier = nextEdgeIdentifier(EdgeIdentifier.identifierToWayIdentifier(identifier));
            edge.identifier(identifier);
        }

        // then we create a new index for the element,
        var index = identifierToIndex(identifier, IndexingMode.CREATE);

        // set the edge's index,
        edge.index(index);

        // add "from" and "to" vertexes to the edge and store connectivity information,
        vertexStore().temporaryAddVertexes(edge);

        // store the road shape and other edge attributes,
        var shape = edge.roadShape();
        if (shape.size() == 2)
        {
            storeRoadShape(edge, null);
        }
        else
        {
            storeRoadShape(edge, shape);
        }
        storeAttributes(edge);

        // add mapping from way identifier to directional edge identifier,
        WAY_IDENTIFIER_TO_EDGE_INDEX.allocate();
        wayIdentifierToEdgeIndex.add(edge.wayIdentifier().asLong(), edge.index());

        // and finally increase the number of elements.
        count++;
        if (edge.isTwoWay())
        {
            count++;
        }

        if (DEBUG.isDebugOn())
        {
            var retrieved = dataSpecification().newEdge(graph(), identifier);
            assert retrieved.validator(VALIDATE_RAW).validate(this);
        }
    }

    @Override
    protected void onAdded(Edge edge)
    {
        super.onAdded(edge);

        // Store grade information
        vertexStore().storeGradeSeparation(edge.from(), edge.from().gradeSeparation());
        vertexStore().storeGradeSeparation(edge.to(), edge.to().gradeSeparation());
    }

    @Override
    protected void onCommit()
    {
        super.onCommit();

        if (commitSpatialIndex)
        {
            spatialIndex();
        }

        WAY_IDENTIFIER_TO_RELATIONS.allocate();

        var wayIdentifiers = temporaryWayIdentifierToRelationIndex.keys();
        while (wayIdentifiers.hasNext())
        {
            var wayIdentifier = wayIdentifiers.next();

            var relations = new LongArray("temporary");
            relations.initialSize(Estimate._8);
            relations.initialize();

            var list = temporaryRelations.list(temporaryWayIdentifierToRelationIndex.get(wayIdentifier));
            while (list.hasNext())
            {
                relations.add(list.next());
            }

            wayIdentifierToRelationIdentifiers.putAll(wayIdentifier, relations);
        }

        temporaryRelations = null;
        temporaryWayIdentifierToRelationIndex = null;
    }

    /**
     * Free data structures that are no longer needed
     */
    @Override
    protected void onLoaded(GraphArchive archive)
    {
        super.onLoaded(archive);

        // Clear out any stray clean cuts that didn't get fused
        if (!temporaryCleanCuts.isEmpty())
        {
            warning("Discarding $ un-fused clean cuts", temporaryCleanCuts.size());
        }
        temporaryCleanCuts = null;
    }

    @Override
    protected void onLoading(GraphArchive archive)
    {
        configureSerializer();
        super.onLoading(archive);
        count = metadata().edgeCount(Metadata.CountType.REQUIRE_EXACT).asInt();
    }

    /**
     * Validate that a spatial index exists any speed pattern store is saved before saving this store
     */
    @Override
    protected void onSaving(GraphArchive archive)
    {
        configureSerializer();
        super.onSaving(archive);

        // If there's no spatial index,
        if (spatialIndex == null)
        {
            // create it so that it will be saved
            spatialIndex = new CompressedEdgeBulkSpatialIndexer(this).index(graph());
        }
    }

    private void configureSerializer()
    {
        var session = require(KryoSerializationSessionFactory.class).newSession(this);
        var types = session.kryoTypes();
        types.registerDynamic(CompressedEdgeSpatialIndex.class,
                new CompressedEdgeSpatialIndexKryoSerializer(graph()),
                CompressedEdgeSpatialIndexKryoSerializer.IDENTIFIER);
    }

    /**
     * Fuses the given edge with any clean cut neighbors by using temporary clean cutting information. The edges that
     * were fused together are removed with {@link #remove(Edge)}.
     *
     * @return The fused edge incorporating all neighboring clean cut edges
     */
    private Edge fuse(Edge edge)
    {
        var fused = edge.asHeavyWeight();

        // Get road shape and start/end location from that
        var shape = edge.roadShape();

        // If the 'from' node needs to be fused
        if (edge.fromNodeIdentifier() == null)
        {
            var start = shape.start();
            var head = temporaryCleanCuts.get(start);
            if (head != null)
            {
                if (start.equals(head.fromLocation()))
                {
                    head = head.reversed();
                }
                fused.roadShape(shape = head.roadShape().append(shape));
                fused.fromNodeIdentifier(head.fromNodeIdentifier());
                if (fused.fromNodeIdentifier() == null)
                {
                    temporaryCleanCuts.put(fused.roadShape().start(), fused);
                }
                else
                {
                    temporaryCleanCuts.remove(start);
                }
                remove(head);
            }
            else
            {
                temporaryCleanCuts.put(start, edge);
            }
        }

        // If the 'to' node needs to be fused
        if (edge.toNodeIdentifier() == null)
        {
            var end = shape.end();
            var tail = temporaryCleanCuts.get(end);
            if (tail != null)
            {
                if (end.equals(tail.toLocation()))
                {
                    tail = tail.reversed();
                }
                fused.roadShape(shape.append(tail.roadShape()));
                fused.toNodeIdentifier(tail.toNodeIdentifier());
                if (fused.toNodeIdentifier() == null)
                {
                    temporaryCleanCuts.put(fused.roadShape().end(), fused);
                }
                else
                {
                    temporaryCleanCuts.remove(end);
                }
                remove(tail);
            }
            else
            {
                temporaryCleanCuts.put(end, edge);
            }
        }

        return fused;
    }

    /**
     * @return True if the edge at the given index is two-way
     */
    private boolean isOneWay(int index)
    {
        if (roadState == null)
        {
            ROAD_STATE.load();
        }
        return roadState.get(index) == RoadState.ONE_WAY.quantum();
    }

    /**
     * @return True if the edge at the given index is two-way
     */
    private boolean isTwoWay(int index)
    {
        if (roadState == null)
        {
            ROAD_STATE.load();
        }
        return roadState.get(index) == RoadState.TWO_WAY.quantum();
    }

    /**
     * Removes the given edge from this store and the vertex store
     */
    private void remove(Edge edge)
    {
        var index = identifierToIndex(edge.identifierAsLong(), IndexingMode.GET);
        edge.index(index);
        vertexStore().temporaryRemove(edge);
        super.remove(edge);
    }
}
