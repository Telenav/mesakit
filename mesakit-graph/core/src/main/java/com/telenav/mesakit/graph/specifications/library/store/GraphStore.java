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

package com.telenav.mesakit.graph.specifications.library.store;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.interfaces.loading.Unloadable;
import com.telenav.kivakit.validation.BaseValidator;
import com.telenav.kivakit.validation.Validatable;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.mesakit.graph.specifications.common.graph.store.CommonGraphStore;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.mesakit.map.geography.Precision;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A store for graph attributes. Contains information about vertexes, edges, relations and places.
 *
 * @author jonathanl (shibo)
 * @see Attribute
 * @see CommonGraphStore
 */
@SuppressWarnings("unused")
public abstract class GraphStore extends BaseRepeater implements Unloadable, Validatable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public static class GraphStoreValidation extends ValidationType
    {
        public GraphStoreValidation()
        {
            include(EdgeStore.class);
            include(VertexStore.class);
            include(PlaceStore.class);
            include(RelationStore.class);
        }
    }

    /** The graph for which this is a store */
    private final transient Graph graph;

    /** The tag codec used to compress and decompress tags in this store */
    private transient PbfTagCodec tagCodec;

    /** Store of Vertex attributes */
    private VertexStore vertexStore;

    /** Store of Edge attributes */
    private EdgeStore edgeStore;

    /** Store of EdgeRelation attributes */
    private RelationStore relationStore;

    /** Store of Place attributes */
    private PlaceStore placeStore;

    /** Store of ShapePoint attributes when a graph has full node information for Cygnus */
    private ShapePointStore shapePointStore;

    /** The bounding rectangle for all data in this store */
    private Rectangle bounds;

    /** Used to create the bounding box around all graph elements as data is loaded */
    private final BoundingBoxBuilder boundsBuilder = new BoundingBoxBuilder();

    /** Set of graph stores for graph elements */
    private transient List<ArchivedGraphElementStore<?>> stores;

    /** True if this graph store has finalized all changes */
    private boolean committed;

    protected GraphStore(Graph graph)
    {
        this.graph = graph;
    }

    public final void addToBounds(long location)
    {
        bounds = null;
        boundsBuilder.add(location);
    }

    public final void addToBounds(Rectangle rectangle)
    {
        bounds = null;
        boundsBuilder.add(rectangle);
    }

    /**
     * @return The bounding rectangle around all elements in this graph.
     */
    public final Rectangle bounds()
    {
        if (bounds == null && boundsBuilder.isValid())
        {
            bounds = boundsBuilder.build();
        }
        return bounds;
    }

    public void bounds(Rectangle bounds)
    {
        this.bounds = bounds;
    }

    /**
     * Commit any batched changes in each graph element store and finalize any changes. After calling this method, the
     * graph is locked and no further data can be loaded.
     */
    public final void commit()
    {
        if (!committed)
        {
            // Commit changes
            onCommitting();
            forEachStore(GraphElementStore::commit);
            onCommitted();

            vertexStore().freeTemporaryData();

            // Minimize data size
            committed = true;
        }
    }

    /**
     * @return The edge store used to store edge information, generally in a compressed format in memory.
     */
    public final EdgeStore edgeStore()
    {
        if (edgeStore == null)
        {
            edgeStore = dataSpecification().newEdgeStore(graph());
            listenTo(edgeStore);
        }
        return edgeStore;
    }

    public void flush()
    {
        forEachStore(GraphElementStore::flush);
    }

    public final Graph graph()
    {
        return graph;
    }

    /**
     * @return True if all graph element stores are empty
     */
    public final boolean isEmpty()
    {
        for (GraphElementStore<?> store : stores())
        {
            if (!store.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The graph's metadata with accurate counts for sub-stores
     */
    public final Metadata metadata()
    {
        return graph().metadata()
                .withVertexCount(vertexStore().retrieveCount())
                .withEdgeCount(edgeStore().retrieveCount())
                .withForwardEdgeCount(edgeStore().retrieveForwardEdgeCount())
                .withEdgeRelationCount(relationStore().retrieveCount())
                .withPlaceCount(placeStore().retrieveCount());
    }

    public final PlaceStore placeStore()
    {
        if (placeStore == null)
        {
            placeStore = dataSpecification().newPlaceStore(graph());
            listenTo(placeStore);
        }
        return placeStore;
    }

    public final Precision precision()
    {
        return graph().precision();
    }

    public final RelationStore relationStore()
    {
        if (relationStore == null)
        {
            relationStore = dataSpecification().newRelationStore(graph());
            listenTo(relationStore);
        }
        return relationStore;
    }

    public final ShapePointStore shapePointStore()
    {
        if (shapePointStore == null)
        {
            shapePointStore = dataSpecification().newShapePointStore(graph());
            listenTo(shapePointStore);
        }
        return shapePointStore;
    }

    /**
     * @return The tag codec used in storing tag data for this graph
     */
    public final PbfTagCodec tagCodec()
    {
        if (tagCodec == null)
        {
            tagCodec = metadata().tagCodec();
        }
        return tagCodec;
    }

    /**
     * Checks store validity. This method is called after graph loading and before graph saving to ensure that loaded
     * and saved data is valid.
     *
     * @param type The type of validation to perform
     * @return True if this graph store and all graph element sub-stores are in a consistent, valid state.
     */
    @Override
    public final Validator validator(ValidationType type)
    {
        return new BaseValidator()
        {
            @Override
            protected void onValidate()
            {
                if (DEBUG.isDebugOn())
                {
                    var start = Time.now();
                    forEachStore(store -> validate(store.validator(type)));
                    information("Validated ${class} in $", GraphStore.this.getClass(), start.elapsedSince());
                }
            }

            @Override
            protected boolean shouldShowValidationReport()
            {
                return true;
            }

            @Override
            protected String validationTarget()
            {
                return "Graph store";
            }
        };
    }

    @SuppressWarnings({ "exports" })
    public final VertexStore vertexStore()
    {
        if (vertexStore == null)
        {
            vertexStore = dataSpecification().newVertexStore(graph());
            listenTo(vertexStore);
        }
        return vertexStore;
    }

    protected DataSpecification dataSpecification()
    {
        return graph().dataSpecification();
    }

    protected void forEachStore(Consumer<ArchivedGraphElementStore<?>> consumer)
    {
        for (var store : stores())
        {
            consumer.accept(store);
        }
    }

    @MustBeInvokedByOverriders
    protected void onCommitted()
    {
    }

    @MustBeInvokedByOverriders
    protected void onCommitting()
    {
    }

    @MustBeInvokedByOverriders
    protected void onFreezing()
    {
    }

    @MustBeInvokedByOverriders
    protected void onFrozen()
    {
    }

    protected List<ArchivedGraphElementStore<?>> stores()
    {
        if (stores == null)
        {
            stores = new ArrayList<>(5);
            stores.add(edgeStore());
            stores.add(placeStore());
            stores.add(relationStore());
            stores.add(vertexStore());
            if (graph.supportsFullPbfNodeInformation())
            {
                stores.add(shapePointStore());
            }
        }
        return stores;
    }
}
