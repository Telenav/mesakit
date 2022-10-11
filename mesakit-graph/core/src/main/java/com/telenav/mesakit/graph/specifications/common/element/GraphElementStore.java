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

package com.telenav.mesakit.graph.specifications.common.element;

import com.telenav.kivakit.core.collections.iteration.BaseIterator;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.messages.status.Glitch;
import com.telenav.kivakit.core.messaging.messages.status.Problem;
import com.telenav.kivakit.core.messaging.messages.status.Quibble;
import com.telenav.kivakit.core.messaging.messages.status.Warning;
import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.thread.Batcher;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Estimate;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.collection.Addable;
import com.telenav.kivakit.interfaces.naming.NamedObject;
import com.telenav.kivakit.primitive.collections.CompressibleCollection;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitCharArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitIntArray;
import com.telenav.kivakit.primitive.collections.array.scalars.SplitLongArray;
import com.telenav.kivakit.primitive.collections.list.PrimitiveList;
import com.telenav.kivakit.primitive.collections.list.store.PackedStringStore;
import com.telenav.kivakit.primitive.collections.map.split.SplitLongToIntMap;
import com.telenav.kivakit.resource.compression.archive.ArchivedField;
import com.telenav.kivakit.validation.BaseValidator;
import com.telenav.kivakit.validation.Validatable;
import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.validation.Validator;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.identifiers.GraphElementIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.element.store.TagStore;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeList;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeStore;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodec;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.mesakit.graph.Metadata.CountType.ALLOW_ESTIMATE;

/**
 * Packed metadata attributes for {@link GraphElement}s.
 *
 * @author jonathanl (shibo)
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public abstract class GraphElementStore<T extends GraphElement> extends BaseRepeater implements
        Iterable<T>,
        CompressibleCollection,
        AttributeStore,
        Validatable
{
    private static final boolean BATCHING_ENABLED = false;

    private static final Count BATCH_SIZE = Count._16_384;

    private static final Maximum QUEUE_SIZE = Maximum._128;

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public enum IndexingMode
    {
        GET,
        CREATE
    }

    protected abstract class StoreValidator extends BaseValidator
    {
        @Override
        protected Glitch glitch(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.glitch(storeName() + " is invalid because " + message, parameters);
            }
            else
            {
                return addGlitch(message, parameters);
            }
        }

        protected boolean isNull(PrimitiveList list, int index)
        {
            assert list != null;
            return list.isPrimitiveNull(list.safeGetPrimitive(index));
        }

        @Override
        protected Problem problem(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.problem(storeName() + " is invalid because " + message, parameters);
            }
            else
            {
                return addProblem(message, parameters);
            }
        }

        @Override
        protected Quibble quibble(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.quibble(storeName() + " is imperfect because " + message, parameters);
            }
            else
            {
                return addQuibble(message, parameters);
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
            return storeName();
        }

        @Override
        protected Warning warning(String message, Object... parameters)
        {
            if (DEBUG.isDebugOn())
            {
                return super.warning(storeName() + " is imperfect because " + message, parameters);
            }
            else
            {
                return addWarning(message, parameters);
            }
        }

        private String storeName()
        {
            return elementType().getSimpleName() + " store (" + Count.count(size()) + " elements)";
        }
    }

    /** The interface for batch adding for each thread */
    private ThreadLocal<Addable<T>> adder;

    /** A batch queue with an associated thread to speed up throughput */
    private Batcher<T> batcher;

    /** True if we are using the batcher to add elements on a separate thread */
    private boolean batching;

    /** True if this graph store has been committed */
    private transient volatile boolean committed;

    /** The method used to compress this store */
    private CompressibleCollection.Method compressionMethod;

    /** The data specification for this element store */
    private final transient DataSpecification dataSpecification;

    /** The number of elements that have been discarded due to validation problems */
    private int discarded;

    /** Cached element factory for efficiency */
    private final DataSpecification.GraphElementFactory<T> elementFactory;

    /** True if this store is batching and has been flushed (which can only be done once) */
    private boolean flushed;

    /** The graph for which this is a graph */
    private final transient Graph graph;

    private final AttributeReference<SplitLongArray> IDENTIFIER =
            new AttributeReference<>(this, GraphElementAttributes.get().IDENTIFIER, "identifier",
                    () -> (SplitLongArray) new SplitLongArray("identifier").initialSize(estimatedElements()));

    private final AttributeReference<SplitLongToIntMap> IDENTIFIER_TO_INDEX =
            new AttributeReference<>(this, GraphElementAttributes.get().IDENTIFIER_TO_INDEX, "identifierToIndex",
                    () -> (SplitLongToIntMap) new SplitLongToIntMap("identifierToIndex").initialSize(estimatedElements()));

    private final AttributeReference<SplitLongArray> LAST_MODIFIED =
            new AttributeReference<>(this, GraphElementAttributes.get().LAST_MODIFIED, "lastModified",
                    () -> (SplitLongArray) new SplitLongArray("lastModified").initialSize(estimatedElements()));

    private final AttributeReference<SplitLongArray> PBF_CHANGE_SET_IDENTIFIER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_CHANGE_SET_IDENTIFIER, "pbfChangeSetIdentifier",
                    () -> (SplitLongArray) new SplitLongArray("pbfChangeSetIdentifier").initialSize(estimatedElements()));

    private final AttributeReference<SplitCharArray> PBF_REVISION_NUMBER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_REVISION_NUMBER, "pbfRevisionNumber",
                    () -> (SplitCharArray) new SplitCharArray("pbfRevisionNumber").initialSize(estimatedElements()));

    private final AttributeReference<PackedStringStore> PBF_USER_NAME =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_USER_NAME, "pbfUserName",
                    () -> (PackedStringStore) new PackedStringStore("pbfUserName")
                            .initialSize(estimatedElements()).initialChildSize(32));

    private final AttributeReference<SplitIntArray> PBF_USER_IDENTIFIER =
            new AttributeReference<>(this, GraphElementAttributes.get().PBF_USER_IDENTIFIER, "pbfUserIdentifier",
                    () -> (SplitIntArray) new SplitIntArray("pbfUserIdentifier").initialSize(estimatedElements()));

    private final AttributeReference<TagStore> TAGS =
            new AttributeReference<>(this, GraphElementAttributes.get().TAGS, "tags",
                    () -> new TagStore("tags", metadata().tagCodec()))
            {
                @Override
                protected void onLoaded(TagStore store)
                {
                    store.codec(metadata().tagCodec());
                    super.onLoaded(store);
                }
            };

    @ArchivedField
    private SplitLongArray identifier;

    @ArchivedField
    private SplitLongToIntMap identifierToIndex;

    @ArchivedField
    private SplitLongArray lastModified;

    /**
     * The next index for an element in this store. Note that we start index values at 1 rather than 0 because we want
     * to catch bugs that involve uninitialized index values (the default value for an int in Java is zero).
     */
    @ArchivedField
    private int nextIndex = 1;

    @ArchivedField
    private SplitLongArray pbfChangeSetIdentifier;

    @ArchivedField
    private SplitCharArray pbfRevisionNumber;

    @ArchivedField
    private SplitIntArray pbfUserIdentifier;

    @ArchivedField
    private PackedStringStore pbfUserName;

    /** The number of elements in this store (distinct from the count(), which takes into account reversible edges) */
    @ArchivedField
    private int size;

    /**
     * Tags for this attribute
     */
    @ArchivedField
    private TagStore tags;

    /** True if this store has been trimmed to its minimum size */
    private boolean trimmed;

    protected GraphElementStore(Graph graph)
    {
        this.graph = graph;
        dataSpecification = graph.dataSpecification();
        elementFactory = elementFactory();

        onInitialize();
    }

    /**
     * Returns an interface through which elements can be added to this store. When batching is enabled, this will be a
     * {@link Batcher.BatchAdder} and when it's not, it will be a reference to {@link #internalAdd(GraphElement)}.
     */
    public synchronized Addable<T> adder()
    {
        // If batching is enabled
        if (BATCHING_ENABLED)
        {
            // and we haven't started batching,
            if (!batching)
            {
                // then start the batcher
                batcher().start(Count._1);
                listenTo(batcher());

                // and keep the batch adder for this thread in a thread local
                adder = ThreadLocal.withInitial(batcher()::adder);
                batching = true;
            }

            // otherwise, return the batch adder for this thread.
            return adder.get();
        }
        else
        {
            var outer = this;
            return new Addable<>()
            {
                @Override
                public boolean onAdd(T t)
                {
                    return internalAdd(t);
                }

                @Override
                public int size()
                {
                    return outer.size();
                }
            };
        }
    }

    @Override
    public AttributeList attributes()
    {
        return dataSpecification().attributes(getClass());
    }

    /**
     * Returns iterator of graph element batches of the given size
     */
    public Iterator<List<T>> batches(Count batchSize)
    {
        var outer = this;
        return new BaseIterator<>()
        {
            int index = 1;

            final int batchSizeAsInt = batchSize.asInt();

            @Override
            protected List<T> onNext()
            {
                var batch = new ArrayList<T>();
                var size = size();
                for (var i = 0; i < batchSizeAsInt && index < size + 1; i++)
                {
                    var identifier = outer.identifier.safeGet(index++);
                    if (!outer.identifier.isNull(identifier))
                    {
                        batch.add(outer.elementFactory.newElement(outer.graph, identifier));
                    }
                }
                return batch.isEmpty() ? null : batch;
            }
        };
    }

    /**
     * Finalizes changes to this element store
     */
    public final void commit()
    {
        if (!committed)
        {
            flush();
            onCommit();
            compress(Method.FREEZE);
            committed = true;
        }
    }

    @Override
    public Method compress(Method method)
    {
        if (!isCompressed())
        {
            compressionMethod = method;

            CompressibleCollection.compressReachableObjects(this, this, method, event ->
                    DEBUG.trace("Compressed $", NamedObject.syntheticName(event)));
            return method;
        }

        return compressionMethod;
    }

    @Override
    public Method compressionMethod()
    {
        return compressionMethod;
    }

    /**
     * Returns true if this store contains the given element identifier
     */
    public final boolean containsIdentifier(GraphElementIdentifier identifier)
    {
        return containsIdentifier(identifier.asLong());
    }

    /**
     * Returns true if this store contains the given element identifier
     */
    public boolean containsIdentifier(long identifier)
    {
        if (!IDENTIFIER_TO_INDEX.load())
        {
            IDENTIFIER_TO_INDEX.allocate();
        }
        return identifierToIndex.containsKey(identifier);
    }

    /**
     * Returns the number of elements in this store. This is distinct from the store {@link #size()}. The store size is
     * the number of elements being physically stored. The count is the number of elements, taking into account
     * reversible edges (in the EdgeStore subclass).
     */
    public int count()
    {
        // By default, this is the size, but this is overridden by EdgeStore since it logically stores reversible edges
        // but physically stores only forward edges
        return size();
    }

    /**
     * Returns the number of elements discarded by {@link #internalAdd(GraphElement)} due to failing validation
     */
    public Count discarded()
    {
        return Count.count(discarded);
    }

    /**
     * If batching is enabled, flushes any remaining items
     */
    public synchronized void flush()
    {
        if (BATCHING_ENABLED)
        {
            if (!flushed)
            {
                batcher().stop();
                flushed = true;
            }
        }
    }

    /**
     * Returns the graph associated with this element store
     */
    @Override
    public final Graph graph()
    {
        return graph;
    }

    /**
     * Returns true if this store has no elements
     */
    public boolean isEmpty()
    {
        return count() == 0;
    }

    /**
     * Returns the sequence of all elements in this store
     */
    @Override
    public Iterator<T> iterator()
    {
        var outer = this;
        IDENTIFIER.load();
        return new BaseIterator<>()
        {
            // The first index (see details in comment above for nextIndex)
            int index = 1;

            @Override
            protected T onNext()
            {
                if (index < size() + 1)
                {
                    var identifier = outer.identifier.safeGet(index++);
                    if (!outer.identifier.isNull(identifier))
                    {
                        return outer.elementFactory.newElement(outer.graph, identifier);
                    }
                }
                return null;
            }
        };
    }

    /**
     * Called to initialize this store
     */
    @MustBeInvokedByOverriders
    public void onInitialize()
    {
        resetIndex();
    }

    /**
     * In special circumstance, it may be necessary to run logic and add extra elements to a committed element store.
     * This method runs the given code, allowing final additions.
     */
    public void postCommit(Runnable code)
    {
        committed = false;
        try
        {
            code.run();
            compress(Method.FREEZE);
        }
        finally
        {
            committed = true;
        }
    }

    /**
     * Returns the number of directional edges in this store. For the number of forward edges, call {@link #size()}.
     */
    public Count retrieveCount()
    {
        return Count.count(count());
    }

    public long retrieveIdentifier(int index)
    {
        IDENTIFIER.load();
        return identifier.safeGet(index);
    }

    /**
     * Retrieves the existing index of the given {@link GraphElement}. An index is used to efficiently address packed,
     * array-like data structures within element stores.
     *
     * @return The index for the given element
     */
    public int retrieveIndex(long identifier)
    {
        checkIdentifier(identifier);
        return identifierToIndex(identifier, IndexingMode.GET);
    }

    /**
     * Retrieves the existing index of the given {@link GraphElement}. An index is used to efficiently address packed,
     * array-like data structures within element stores.
     *
     * @return The index for the given element
     */
    public int retrieveIndex(T element)
    {
        var identifier = element.identifierAsLong();
        checkIdentifier(identifier);
        return identifierToIndex(identifier, IndexingMode.GET);
    }

    /**
     * Returns the last modification time of the given {@link GraphElement}
     */
    public Time retrieveLastModificationTime(GraphElement element)
    {
        return LAST_MODIFIED.retrieveObject(element, Time::epochMilliseconds);
    }

    /**
     * This method returns an PBF-specific change set identifier for the changes in which this {@link GraphElement} was
     * last modified. This value is not in a PBF-specific subclass because it is metadata for all graph elements when it
     * is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfChangeSetIdentifier retrievePbfChangeSetIdentifier(GraphElement element)
    {
        return PBF_CHANGE_SET_IDENTIFIER.retrieveObject(element, value -> new PbfChangeSetIdentifier((int) value));
    }

    /**
     * This method returns an PBF-specific revision number for the changes in which this {@link GraphElement} was last
     * modified. This value is not in a PBF-specific subclass because it is metadata for all graph elements when it is
     * available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfRevisionNumber retrievePbfRevisionNumber(GraphElement element)
    {
        return PBF_REVISION_NUMBER.retrieveObject(element, revision -> new PbfRevisionNumber((int) revision));
    }

    /**
     * This method returns the PBF-specific user identifier for the user who last modified this {@link GraphElement}.
     * This value is not in a PBF-specific subclass because it is metadata for all graph elements when it is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfUserIdentifier retrievePbfUserIdentifier(GraphElement element)
    {
        return PBF_USER_IDENTIFIER.retrieveObject(element, value -> new PbfUserIdentifier((int) value));
    }

    /**
     * This method returns the PBF-specific username for the user who last modified this {@link GraphElement}. This
     * value is not in a PBF-specific subclass because it is metadata for all graph elements when it is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfUserName retrievePbfUserName(GraphElement element)
    {
        var userName = PBF_USER_NAME.retrieveString(element);
        if (userName != null)
        {
            return new PbfUserName(userName);
        }
        return null;
    }

    /**
     * Returns the list of tags for this {@link GraphElement}.
     */
    public final PbfTagList retrieveTagList(GraphElement element)
    {
        var tags = tags();
        return tags == null ? null : tags.tagList(element);
    }

    /**
     * Returns the list of tags for this {@link GraphElement}.
     */
    public final PbfTagMap retrieveTagMap(GraphElement element)
    {
        var tags = tags();
        return tags == null ? null : tags.tagMap(element);
    }

    /**
     * Returns the number of physical {@link GraphElement}s that are stored in this store. This is distinct from the
     * {@link #count()}, which takes into account reversible edges in the EdgeStore subclass.
     */
    @Override
    public final int size()
    {
        return size;
    }

    /**
     * Stores all the metadata attributes of the given graph element using its index
     */
    @MustBeInvokedByOverriders
    public void storeAttributes(GraphElement element)
    {
        LAST_MODIFIED.storeObject(element, element.lastModificationTime());
        PBF_CHANGE_SET_IDENTIFIER.storeObject(element, element.pbfChangeSetIdentifier());
        PBF_REVISION_NUMBER.storeObject(element, element.pbfRevisionNumber());
        PBF_USER_IDENTIFIER.storeObject(element, element.pbfUserIdentifier());
        var userName = element.pbfUserName();
        if (userName != null)
        {
            PBF_USER_NAME.storeString(element, userName.name());
        }
        storeTags(element, element.tagList());
    }

    /**
     * Stores any tags for a given {@link GraphElement}
     */
    public void storeTags(GraphElement element, PbfTagList tags)
    {
        if (dataSpecification().supports(GraphElementAttributes.get().TAGS))
        {
            if (!tags.isEmpty())
            {
                tags().set(element, tags);
            }
        }
    }

    public ObjectList<Attribute<?>> supportedAttributes()
    {
        return attributeLoader().attributes();
    }

    @Override
    public boolean supports(Attribute<?> attribute)
    {
        return attributeLoader().supports(attribute);
    }

    /**
     * Returns the tag codec being used to compress tags in this store
     */
    public PbfTagCodec tagCodec()
    {
        var tags = tags();
        return tags == null ? null : tags().codec();
    }

    /**
     * Returns the tag store for this store
     */
    @SuppressWarnings({ "exports" })
    public TagStore tags()
    {
        if (!TAGS.load())
        {
            TAGS.allocate();
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(ValidationType validation)
    {
        var outer = this;
        return new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                outer.IDENTIFIER.load();
                outer.IDENTIFIER_TO_INDEX.load();

                // If we're supposed to validate this store,
                if (validation.shouldValidate(outer.getClass()))
                {
                    // then make sure we are not empty
                    warningIf(outer.isEmpty(), "it is empty");

                    // Go through graph element indexes
                    for (var index = 1; index < size() && !isInvalid(); index++)
                    {
                        // and get the identifier
                        var identifier = outer.identifier.get(index);

                        // and if the identifier is valid
                        if (identifier > 0)
                        {
                            // and the mapping back to index isn't correct
                            if (outer.identifierToIndex.get(identifier) != index)
                            {
                                // then we're invalid
                                problem("graph element identifier " + identifier + " does not map back to index " + index);
                            }
                        }
                        else
                        {
                            // otherwise we're invalid
                            problem("identifier is missing for index $", index);
                        }
                    }
                }
            }
        };
    }

    protected final DataSpecification dataSpecification()
    {
        return dataSpecification;
    }

    /**
     * Returns the {@link EdgeStore} associated with the graph which owns this store
     */
    protected EdgeStore edgeStore()
    {
        return graph().edgeStore();
    }

    /**
     * Returns factory that can create graph elements of type T
     */
    protected abstract DataSpecification.GraphElementFactory<T> elementFactory();

    /**
     * Returns the class of {@link GraphElement} in this store
     */
    protected abstract Class<T> elementType();

    protected Estimate estimatedElements()
    {
        return metadata().count(ALLOW_ESTIMATE, elementType()).asEstimate();
    }

    protected Estimate estimatedElements(Class<? extends GraphElement> type)
    {
        return metadata().count(ALLOW_ESTIMATE, type).asEstimate();
    }

    protected Estimate estimatedNodes()
    {
        return metadata().nodeCount(ALLOW_ESTIMATE).asEstimate();
    }

    protected Estimate estimatedRelations()
    {
        return metadata().relationCount(ALLOW_ESTIMATE).asEstimate();
    }

    protected Estimate estimatedWays()
    {
        return metadata().wayCount(ALLOW_ESTIMATE).asEstimate();
    }

    /**
     * Gets (or creates) an index for the given element identifier
     *
     * @param mode Specifies whether to simply get the index or if it doesn't exist, create it instead
     * @return An index for the given graph element identifier
     */
    protected int identifierToIndex(long identifier, IndexingMode mode)
    {
        if (!IDENTIFIER_TO_INDEX.load())
        {
            IDENTIFIER_TO_INDEX.allocate();
        }

        // Get the index for the given element identifier if any
        var index = identifierToIndex.get(Math.abs(identifier));
        var isNull = identifierToIndex.isNull(index);

        // If there's no index, and we can create one,
        if (isNull && mode == IndexingMode.CREATE)
        {
            // get the next index,
            index = nextIndex();

            // store the index <--> identifier mapping
            storeIdentifier(index, identifier);

            // and return it.
            checkIndex(index);
            return index;
        }

        // If the index isn't null
        if (!isNull)
        {
            // return it
            checkIndex(index);
            return index;
        }

        // We were not able to get or create the index
        throw new IllegalStateException("No index for identifier " + identifier);
    }

    protected Metadata metadata()
    {
        return graph().metadata();
    }

    /**
     * Returns the next element index in this store
     */
    protected int nextIndex()
    {
        return nextIndex++;
    }

    /**
     * Called when an element is being added
     */
    @MustBeInvokedByOverriders
    protected void onAdd(T element)
    {
    }

    /**
     * Called after an element is added
     */
    @MustBeInvokedByOverriders
    protected void onAdded(T ignored)
    {
    }

    /**
     * Called before an element is added
     */
    @MustBeInvokedByOverriders
    protected void onAdding(T ignored)
    {
    }

    /**
     * Called when it's time to finalize changes to this store
     */
    @MustBeInvokedByOverriders
    protected void onCommit()
    {
    }

    /**
     * Overwrites the existing element with a new element
     *
     * @param existingIndex The index of the existing element
     * @param existingIdentifier The identifier of the existing element
     * @param newIdentifier The new element identifier
     */
    protected void overwriteElement(int existingIndex, long existingIdentifier, long newIdentifier)
    {
        IDENTIFIER_TO_INDEX.load();

        // Remove the existing mapping from identifier to index,
        identifierToIndex.remove(Math.abs(existingIdentifier));

        // add a new mapping from identifier to index
        identifierToIndex.put(Math.abs(newIdentifier), existingIndex);

        // and overwrite the identifier for the existing index
        storeIdentifier(existingIndex, newIdentifier);
    }

    /**
     * Returns the place store associated with the graph that owns this store
     */
    protected PlaceStore placeStore()
    {
        return graph().placeStore();
    }

    /**
     * Returns the relation store associated with the graph that owns this store
     */
    protected RelationStore relationStore()
    {
        return graph().relationStore();
    }

    /**
     * Removes the given graph element. This can only be performed during graph loading. Once the graph is loaded, it
     * becomes unmodifiable.
     */
    protected void remove(GraphElement element)
    {
        IDENTIFIER_TO_INDEX.load();
        identifierToIndex.remove(element.identifierAsLong());
    }

    /**
     * Resets the next element index to the first index
     */
    protected void resetIndex()
    {
        nextIndex = 1;
    }

    /**
     * Returns the shape point store associated with the graph that owns this store
     */
    protected ShapePointStore shapePointStore()
    {
        return graph().shapePointStore();
    }

    /**
     * Returns the vertex store associated with the graph that owns this store
     */
    protected VertexStore vertexStore()
    {
        return graph().vertexStore();
    }

    private Batcher<T> batcher()
    {
        if (batcher == null)
        {
            batcher = Batcher.<T>create()
                    .withName(qualifiedName())
                    .withQueueSize(QUEUE_SIZE)
                    .withBatchSize(BATCH_SIZE)
                    .withConsumer(batch -> batch.forEach(this::internalAdd));
        }
        return batcher;
    }

    private void checkIdentifier(long identifier)
    {
        assert identifier != 0;
        assert identifier != Long.MIN_VALUE;
    }

    private void checkIndex(int index)
    {
        assert index > 0;
    }

    /**
     * @param element The {@link GraphElement} to add to this store
     * @return True if the element was added and false if it was invalid
     */
    @SuppressWarnings("unchecked")
    private synchronized boolean internalAdd(T element)
    {
        // If we have already finalized changes to this store,
        if (committed)
        {
            // then we cannot add any more data
            LOGGER.problem("Attempt to add $ to committed graph element store '$'", elementType().getSimpleName(), objectName());
            return false;
        }

        // If the element is valid
        if (element.validator(GraphElement.VALIDATE_RAW).validate(DEBUG.listener()))
        {
            // NOTE: An element MUST be heavyweight to be added to a store. This is because a flyweight element cannot
            // belong to two graphs at once. The flyweight element will have its own graph and index, but those fields
            // will be overwritten prior to adding the element's values to storage. As soon as those fields are changed,
            // it belongs to the new graph and its values can no longer be retrieved in order to add it.
            var heavyweight = (T) element.asHeavyWeight();

            // assign it to this graph,
            heavyweight.graph(graph());

            // and then add it to the store.
            onAdding(heavyweight);
            try
            {
                onAdd(heavyweight);
                onAdded(heavyweight);
                size++;
            }
            catch (Exception e)
            {
                warning(e, "Unable to add graph element $", heavyweight);
            }

            return true;
        }
        else
        {
            discarded++;
        }
        return false;
    }

    private String qualifiedName()
    {
        return graph().name() + "-" + elementType().getSimpleName();
    }

    /**
     * Stores the element identifier for the element at the given index
     */
    private void storeIdentifier(int index, long identifier)
    {
        IDENTIFIER.allocate();
        IDENTIFIER_TO_INDEX.allocate();

        this.identifier.set(index, identifier);
        identifierToIndex.put(identifier, index);

        ensure(this.identifier.get(index) == identifier);
        ensure(identifierToIndex.get(identifier) == index);
    }
}
