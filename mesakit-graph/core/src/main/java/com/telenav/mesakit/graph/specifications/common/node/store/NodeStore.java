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

package com.telenav.mesakit.graph.specifications.common.node.store;

import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitLongArray;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToIntMap;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToLongMap;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.GraphNode;
import com.telenav.mesakit.graph.ShapePoint;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.common.node.NodeAttributes;
import com.telenav.mesakit.graph.specifications.common.node.store.all.PbfAllGraphElementTagStore;
import com.telenav.mesakit.graph.specifications.common.node.store.all.PbfAllNodeIdentifierStore;
import com.telenav.mesakit.graph.specifications.common.node.store.all.PbfAllNodeIndexStore;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.PbfAllNodeDiskStores;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.geography.Location;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.mesakit.map.geography.Precision.DM7;

/**
 * Store of node information for subclasses of {@link GraphNode}, including {@link Vertex} and {@link ShapePoint}. This
 * store holds node identifiers and node locations and maps from node identifiers to indexes.
 * <p>
 * In addition, this store holds all node information when a {@link Graph} supports it, as determined by {@link
 * Graph#supportsFullPbfNodeInformation()}. Full node information is only a requirement for map editing tools used by
 * the OpenTerra team, such as the map-enhancer project, also known as Cygnus. Having full node information is required
 * in order to contribute enhanced or fixed map data back to the OSM community. In cases where full node information is
 * being stored like this, node information for all of the nodes in each edge's road shape will be stored in the {@link
 * ShapePointStore} subclass of this class.
 *
 * @param <T> The subclass of {@link GraphNode} being stored, either {@link VertexStore} or {@link ShapePointStore}.
 * @see VertexStore
 * @see ShapePointStore
 * @see Vertex
 * @see ShapePoint
 */
@SuppressWarnings({ "unused", "FieldCanBeLocal" })
public abstract class NodeStore<T extends GraphNode> extends ArchivedGraphElementStore<T>
{
    @FunctionalInterface
    protected interface NodeVisitor
    {
        void visit(int index, long pbfNodeIdentifier, long locationAsLong);
    }

    private final AttributeReference<SplitLongArray> NODE_IDENTIFIER =
            new AttributeReference<>(this, NodeAttributes.get().NODE_IDENTIFIER, "nodeIdentifier",
                    () -> (SplitLongArray) new SplitLongArray("nodeIdentifier")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongArray nodeIdentifier;

    private final AttributeReference<SplitLongArray> NODE_LOCATION =
            new AttributeReference<>(this, NodeAttributes.get().NODE_LOCATION, "nodeLocation",
                    () -> (SplitLongArray) new SplitLongArray("nodeLocation")
                            .nullLong(Long.MIN_VALUE)
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongArray nodeLocation;

    private final AttributeReference<SplitLongToIntMap> NODE_IDENTIFIER_TO_INDEX =
            new AttributeReference<>(this, NodeAttributes.get().NODE_IDENTIFIER_TO_INDEX, "nodeIdentifierToIndex",
                    () -> (SplitLongToIntMap) new SplitLongToIntMap("nodeIdentifierToIndex")
                            .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitLongToIntMap nodeIdentifierToIndex;

    /** Disk stores for storing all node information when handling OSM data to be contributed back to the community */
    private PbfAllNodeDiskStores allPbfNodeDiskStores;

    /** Store of all PBF node identifiers */
    private PbfAllNodeIdentifierStore allPbfNodeIdentifierStore;

    /** Store of all PBF node indexes */
    private PbfAllNodeIndexStore allPbfNodeIndexStore;

    /** Store of all PBF node tags */
    private PbfAllGraphElementTagStore allPbfNodeTagStore;

    /** PBF node identifier -> node location map which holds this information until vertexes are created */
    private transient SplitLongToLongMap temporaryNodeIdentifierToLocation;

    protected NodeStore(Graph graph)
    {
        super(graph);
    }

    /**
     * Provides storage for full node information
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    public void allPbfNodeDiskStores(PbfAllNodeDiskStores stores)
    {
        allPbfNodeDiskStores = stores;
    }

    /**
     * @return True if this node store contains the given identifier
     */
    public boolean contains(MapNodeIdentifier identifier)
    {
        NODE_IDENTIFIER_TO_INDEX.load();
        return nodeIdentifierToIndex.containsKey(identifier.asLong());
    }

    /**
     * @return True if this node store contains all node information, including for shape points
     */
    public boolean containsAllPbfNodeInformation()
    {
        if (allPbfNodeDiskStores != null)
        {
            return allPbfNodeDiskStores.containsData();
        }
        return false;
    }

    /**
     * Allocate temporary map from node identifier -> location
     */
    @Override
    public void onInitialize()
    {
        super.onInitialize();

        temporaryNodeIdentifierToLocation = new SplitLongToLongMap("node.store.temporaryNodeIdentifierToLocation");
        temporaryNodeIdentifierToLocation.initialSize(graph().metadata().nodeCount(ALLOW_ESTIMATE).asEstimate());
        temporaryNodeIdentifierToLocation.initialize();
    }

    @Override
    public long retrieveIdentifier(int index)
    {
        // With GraphNodes, the index is the identifier
        return index;
    }

    /**
     * @return True if the given node is synthetic (not in the original source data)
     */
    public final boolean retrieveIsNodeSynthetic(GraphNode node)
    {
        if (nodeIdentifier == null)
        {
            NODE_IDENTIFIER.load();
        }
        return PbfNodeIdentifier.isSynthetic(nodeIdentifier.safeGet(node.index()));
    }

    /**
     * @return The location of the given vertex
     */
    public final long retrieveLocationAsLong(Vertex vertex)
    {
        // If the vertex is valid,
        if (vertex != null)
        {
            return retrieveLocationAsLong(vertex.index());
        }
        return Location.NULL;
    }

    /**
     * @return The location of the given vertex
     */
    public final long retrieveLocationAsLong(int vertexIndex)
    {
        // If the vertex is valid,
        if (vertexIndex > 0)
        {
            // get its location
            NODE_LOCATION.load();
            var location = nodeLocation.safeGet(vertexIndex);

            // and if that's valid,
            if (!nodeLocation.isNull(location))
            {
                // return a Location object
                return location;
            }

            // No location was mapped for this vertex
            assert false : "Vertex " + vertexIndex + " has no location";
        }

        assert false;
        return Location.NULL;
    }

    /**
     * @return The node identifier for the given node
     */
    public MapNodeIdentifier retrieveNodeIdentifier(GraphNode node)
    {
        return NODE_IDENTIFIER.retrieveObject(node, PbfNodeIdentifier::new);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeAttributes(GraphElement element)
    {
        // Store base attributes
        super.storeAttributes(element);

        // and store the node identifier
        var identifier = element.mapIdentifier();
        ensure(identifier instanceof MapNodeIdentifier);
        NODE_IDENTIFIER.storeObject(element, identifier);
    }

    @Override
    public Validator validator(ValidationType validation)
    {
        var outer = this;
        return new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                // We don't validate the superclass here because node stores do not have an identifier <-> index
                // mapping because identifiers ARE indexes in this store.

                problemIf(outer.size() == 0, "it is empty");

                outer.NODE_IDENTIFIER.load();
                outer.NODE_LOCATION.load();
                outer.NODE_IDENTIFIER_TO_INDEX.load();

                // Go through each node index
                for (var index = 1; index < size() && !isInvalid(); index++)
                {
                    // and check the node identifier
                    var nodeIdentifier = outer.nodeIdentifier.safeGet(index);
                    problemIf(nodeIdentifier <= 0, "nodeIdentifier is missing for index $", index);

                    // and the node identifier <-> index mapping
                    problemIf(outer.nodeIdentifierToIndex.get(nodeIdentifier) != index,
                            "node identifier $ does not map back to index $", nodeIdentifier, index);
                    problemIf(outer.nodeLocation.get(index) <= 0, "nodeLocation is missing for index $", index);
                }
            }
        };
    }

    /**
     * Calls the given visitor with a node index, node identifier and location for each node
     *
     * @see NodeVisitor
     */
    public void visitElementNodes(NodeVisitor visitor)
    {
        // Traverse the node identifier --> index entries,
        if (nodeIdentifierToIndex != null)
        {
            nodeIdentifierToIndex.entries((nodeIdentifier, index) ->
            {
                // get the location for the node identifier,
                var locationAsLong = temporaryNodeIdentifierToLocation.get(nodeIdentifier);
                if (!temporaryNodeIdentifierToLocation.isNull(locationAsLong))
                {
                    // and visit the node, passing the index, node identifier and location
                    visitor.visit(index, nodeIdentifier, locationAsLong);
                }
                else
                {
                    warning("PBF node index ${long} has no location", nodeIdentifier);
                }
            });
        }

        // We can free this data structure now since only VertexStore uses these locations at present
        // and so only one call is ever made to this method.
        temporaryNodeIdentifierToLocation = null;
    }

    /**
     * Adds the given location to the {@link PbfAllNodeIndexStore}, when full node information is available
     */
    protected int allNodeIndex(Location location)
    {
        return allPbfNodeIndexStore.index(location);
    }

    /**
     * @return Store for the tags of all nodes, when full node information is available
     */
    @SuppressWarnings("ClassEscapesDefinedScope")
    protected PbfAllGraphElementTagStore allNodeTagStore()
    {
        if (allPbfNodeTagStore == null)
        {
            allPbfNodeTagStore = new PbfAllGraphElementTagStore(graph(), archive(), tagCodec());
        }
        return allPbfNodeTagStore;
    }

    /**
     * @return The index for the given node identifier, or a new index if none exists. For all {@link GraphNode}
     * elements, the index will serve as the identifier, since node elements are synthetic.
     */
    protected int maybeCreateNodeIdentifierToIndex(long nodeIdentifier)
    {
        NODE_IDENTIFIER_TO_INDEX.allocate();

        // Get any existing index for the node identifier
        var index = nodeIdentifierToIndex(nodeIdentifier);

        // and if it's null,
        if (nodeIdentifierToIndex.isNull(index))
        {
            // get the next index
            index = nextIndex();

            // and store it both as index -> node identifier and node identifier -> index.
            storeNodeIdentifier(index, nodeIdentifier);
            nodeIdentifierToIndex.put(nodeIdentifier, index);
        }

        return index;
    }

    /**
     * @return The index for the given node identifier, or a new index if none exists. For all {@link GraphNode}
     * elements, the index will serve as the identifier, since node elements are synthetic.
     */
    protected int nodeIdentifierToIndex(long nodeIdentifier)
    {
        // Allocate data structures, if need be,
        if (!NODE_IDENTIFIER_TO_INDEX.load())
        {
            NODE_IDENTIFIER_TO_INDEX.allocate();
        }

        // get the index for the node identifier
        return nodeIdentifierToIndex.get(nodeIdentifier);
    }

    /**
     * Adds the given {@link GraphNode} to this store. The node's index, identifier and location, which were assigned in
     * during onCommit() of VertexStore, are stored here.
     */
    @Override
    protected void onAdd(T element)
    {
        super.onAdd(element);

        // For vertexes (and shape points), the index and identifier have already been assigned and since the index
        // is the identifier, there is no need to call identifierToIndex to create a mapping from index <-> identifier.

        // Save the node identifier and location
        NODE_LOCATION.allocate();
        var index = element.index();
        storeNodeIdentifier(index, element.mapIdentifier().asLong());
        var location = element.location().asLong();
        assert location != 0 && DM7.isValidLocation(location);
        nodeLocation.set(index, location);
        assert nodeLocation.get(index) == location;
    }

    /**
     * If full node information is supported in the graph that owns this store, this method will attach the various
     * relevant node stores to the given {@link GraphArchive}.
     */
    @Override
    protected void onAttached(GraphArchive archive)
    {
        super.onAttached(archive);

        if (containsAllPbfNodeInformation())
        {
            allPbfNodeIndexStore = new PbfAllNodeIndexStore(archive, DM7);
            allPbfNodeIdentifierStore = new PbfAllNodeIdentifierStore(archive);
            if (vertexStore().tagCodec() != null)
            {
                allPbfNodeTagStore = new PbfAllGraphElementTagStore(graph(), archive, vertexStore().tagCodec());
            }
        }
    }

    /**
     * If full node information is supported, saves node information into the given {@link GraphArchive}.
     */
    @Override
    protected void onSaving(GraphArchive archive)
    {
        super.onSaving(archive);

        if (containsAllPbfNodeInformation())
        {
            allPbfNodeDiskStores.saveTo(archive);
            allPbfNodeDiskStores.delete();
        }
    }

    /**
     * Unloads re-loadable data when graph is being unloaded
     */
    @Override
    protected void onUnloading()
    {
        super.onUnloading();

        NODE_LOCATION.unload();
        NODE_IDENTIFIER_TO_INDEX.unload();
        NODE_IDENTIFIER.unload();
    }

    /**
     * Stores the node's location under the node identifier as well as the element identifier, which is the same as the
     * index in the case of this store.
     *
     * @param nodeIdentifier The node identifier
     * @param location The location
     * @return The index for the node identifier, for the sake of efficiency since we retrieve it
     */
    protected int storeNodeLocation(long nodeIdentifier, Location location)
    {
        assert location != null;

        // Store node identifier -> location mapping,
        var locationAsLong = location.asLong();
        assert locationAsLong != 0 : "The location of " + nodeIdentifier + " is " + locationAsLong;
        temporaryNodeIdentifierToLocation.put(nodeIdentifier, locationAsLong);

        // get or create the index for the node identifier,
        var index = maybeCreateNodeIdentifierToIndex(nodeIdentifier);
        storeNodeLocation(index, locationAsLong);
        return index;
    }

    protected void storeNodeLocation(int index, long locationAsLong)
    {
        assert index > 0;
        assert locationAsLong != 0 && DM7.isValidLocation(locationAsLong);

        // and store the location of the given node at that index
        NODE_LOCATION.allocate();
        nodeLocation.set(index, locationAsLong);
    }

    /**
     * Stores the given node identifier at the given index
     */
    private void storeNodeIdentifier(int index, long nodeIdentifier)
    {
        NODE_IDENTIFIER.allocate();
        this.nodeIdentifier.set(index, nodeIdentifier);
    }
}
