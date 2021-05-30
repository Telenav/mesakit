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

package com.telenav.tdk.graph.specifications.common.element;

import com.telenav.tdk.core.collections.batcher.Batcher;
import com.telenav.tdk.core.collections.primitive.array.scalars.*;
import com.telenav.tdk.core.collections.primitive.list.PrimitiveList;
import com.telenav.tdk.core.collections.primitive.list.store.PackedStringStore;
import com.telenav.tdk.core.collections.primitive.map.split.SplitLongToIntMap;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.collection.*;
import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.language.iteration.BaseIterator;
import com.telenav.tdk.core.kernel.language.string.Strings;
import com.telenav.tdk.core.kernel.language.vm.JavaVirtualMachine;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.tdk.core.kernel.scalars.bytes.Bytes;
import com.telenav.tdk.core.kernel.scalars.counts.*;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.core.kernel.validation.*;
import com.telenav.tdk.core.kernel.validation.validators.BaseValidator;
import com.telenav.tdk.core.resource.compression.archive.TdkArchivedField;
import com.telenav.tdk.data.formats.pbf.model.change.*;
import com.telenav.tdk.data.formats.pbf.model.tags.*;
import com.telenav.tdk.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.identifiers.GraphElementIdentifier;
import com.telenav.tdk.graph.metadata.DataSpecification;
import com.telenav.tdk.graph.metadata.DataSpecification.GraphElementFactory;
import com.telenav.tdk.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.tdk.graph.specifications.common.element.store.TagStore;
import com.telenav.tdk.graph.specifications.common.place.store.PlaceStore;
import com.telenav.tdk.graph.specifications.common.relation.store.RelationStore;
import com.telenav.tdk.graph.specifications.common.shapepoint.store.ShapePointStore;
import com.telenav.tdk.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.tdk.graph.specifications.library.attributes.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static com.telenav.tdk.core.kernel.validation.Validate.ensure;
import static com.telenav.tdk.graph.Metadata.CountType.ALLOW_ESTIMATE;

/**
 * Packed meta data attributes for {@link GraphElement}s.
 *
 * @author jonathanl (shibo)
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings("unused")
public abstract class GraphElementStore<T extends GraphElement> extends BaseRepeater<Message> implements Iterable<T>, Compressible, AttributeStore, Validatable
{
    private static final boolean BATCHING_ENABLED = false;

    private static final Count BATCH_SIZE = Count._16384;

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
        protected boolean isNull(final PrimitiveList list, final int index)
        {
            assert list != null;
            return list.isPrimitiveNull(list.safeGetPrimitive(index));
        }

        @Override
        protected void problem(final String message, final Object... parameters)
        {
            if (DEBUG.isEnabled())
            {
                super.problem(storeName() + " is invalid because " + message, parameters);
            }
            else
            {
                problem();
            }
        }

        @Override
        protected void quibble(final String message, final Object... parameters)
        {
            if (DEBUG.isEnabled())
            {
                super.quibble(storeName() + " is invalid because " + message, parameters);
            }
            else
            {
                quibble();
            }
        }

        @Override
        protected boolean validationReport()
        {
            return true;
        }

        @Override
        protected String validationTarget()
        {
            return storeName();
        }

        @Override
        protected void warning(final String message, final Object... parameters)
        {
            if (DEBUG.isEnabled())
            {
                super.warning(storeName() + " is imperfect because " + message, parameters);
            }
        }

        private String storeName()
        {
            return elementType().getSimpleName() + " store (" + Count.of(size()) + " elements)";
        }
    }

    @TdkArchivedField
    private SplitLongArray identifier;

    @TdkArchivedField
    private SplitLongToIntMap identifierToIndex;

    @TdkArchivedField
    private SplitLongArray lastModified;

    @TdkArchivedField
    private SplitLongArray pbfChangeSetIdentifier;

    @TdkArchivedField
    private SplitCharArray pbfRevisionNumber;

    @TdkArchivedField
    private PackedStringStore pbfUserName;

    @TdkArchivedField
    private SplitIntArray pbfUserIdentifier;

    /**
     * Tags for this attribute
     */
    @TdkArchivedField
    private TagStore tags;

    /**
     * The next index for an element in this store. Note that we start index values at 1 rather than 0 because we want
     * to catch bugs that involve uninitialized index values (the default value for an int in Java is zero).
     */
    @TdkArchivedField
    private int nextIndex = 1;

    /** The number of elements in this store (distinct from the count(), which takes into account reversible edges) */
    @TdkArchivedField
    private int size;

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
                protected void onLoaded(final TagStore store)
                {
                    store.codec(metadata().tagCodec());
                    super.onLoaded(store);
                }
            };

    /** True if this graph store has been committed */
    private transient boolean committed;

    /** Cached element factory for efficiency */
    private final GraphElementFactory<T> elementFactory;

    /** The data specification for this element store */
    private final transient DataSpecification dataSpecification;

    /** The number of elements that have been discarded due to validation problems */
    private int discarded;

    /** True if this store has been trimmed to its minimum size */
    private boolean trimmed;

    /** A batch queue with an associated thread to speed up throughput */
    private Batcher<T> batcher;

    /** True if we are using the batcher to add elements on a separate thread */
    private boolean batching;

    /** The interface for batch adding for each thread */
    private ThreadLocal<Addable<T>> adder;

    /** True if this store is batching and has been flushed (which can only be done once) */
    private boolean flushed;

    /** The method used to compress this store */
    private Method compressionMethod;

    protected GraphElementStore(final Graph graph)
    {
        this.graph = graph;
        dataSpecification = graph.dataSpecification();
        elementFactory = elementFactory();

        onInitialize();
    }

    /**
     * @return An interface through which elements can be added to this store. When batching is enabled, this will be a
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
            return this::internalAdd;
        }
    }

    @Override
    public AttributeList attributes()
    {
        return dataSpecification().attributes(getClass());
    }

    /**
     * @return Iterator of graph element batches of the given size
     */
    public Iterator<List<T>> batches(final Count batchSize)
    {
        final var outer = this;
        return new BaseIterator<>()
        {
            int index = 1;

            final int batchSizeAsInt = batchSize.asInt();

            @Override
            protected List<T> onNext()
            {
                final var batch = new ArrayList<T>();
                final var size = size();
                for (var i = 0; i < batchSizeAsInt && index < size + 1; i++)
                {
                    final var identifier = outer.identifier.safeGet(index++);
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
    public Method compress(final Method method)
    {
        if (!isCompressed())
        {
            compressionMethod = method;

            // Must specify -javaagent to VM, see JavaVirtualMachine.sizeOfObjectGraph()
            JavaVirtualMachine.local().traceSizeChange(this, "compress", this, Bytes.kilobytes(100), () ->
            {
                final var size = Compressible.compressReachableObjects(this, this, method, event ->
                        DEBUG.trace("Compressed $", Strings.name(event)));
                if (size != null)
                {
                    graph().estimatedMemorySize(size);
                }
            });

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
     * @return True if this store contains the given element identifier
     */
    public final boolean containsIdentifier(final GraphElementIdentifier identifier)
    {
        return containsIdentifier(identifier.asLong());
    }

    /**
     * @return True if this store contains the given element element identifier
     */
    public boolean containsIdentifier(final long identifier)
    {
        if (!IDENTIFIER_TO_INDEX.load())
        {
            IDENTIFIER_TO_INDEX.allocate();
        }
        return identifierToIndex.containsKey(identifier);
    }

    /**
     * @return The number of elements in this store. This is distinct from the store {@link #size()}. The store size is
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
     * @return The number of elements discarded by {@link #internalAdd(GraphElement)} due to failing validation
     */
    public Count discarded()
    {
        return Count.of(discarded);
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
                batcher().close();
                flushed = true;
            }
        }
    }

    /**
     * @return The graph associated with this element store
     */
    @Override
    public final Graph graph()
    {
        return graph;
    }

    /**
     * @return True if this store has no elements
     */
    public boolean isEmpty()
    {
        return count() == 0;
    }

    /**
     * @return The sequence of all elements in this store
     */
    @Override
    public Iterator<T> iterator()
    {
        final var outer = this;
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
                    final var identifier = outer.identifier.safeGet(index++);
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
     * In special circumstance, it may be necessary to run logic and add extra elements to a committed element store.
     * This method runs the given code, allowing final additions.
     */
    public void postCommit(final Runnable code)
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
     * @return The number of directional edges in this store. For the number of forward edges, call {@link #size()}.
     */
    public Count retrieveCount()
    {
        return Count.of(count());
    }

    public long retrieveIdentifier(final int index)
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
    public int retrieveIndex(final long identifier)
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
    public int retrieveIndex(final T element)
    {
        final var identifier = element.identifierAsLong();
        checkIdentifier(identifier);
        return identifierToIndex(identifier, IndexingMode.GET);
    }

    /**
     * @return The last modification time of the given {@link GraphElement}
     */
    public Time retrieveLastModificationTime(final GraphElement element)
    {
        return LAST_MODIFIED.retrieveObject(element, Time::milliseconds);
    }

    /**
     * This method returns an PBF-specific change set identifier for the changes in which this {@link GraphElement} was
     * last modified. This value is not in a PBF-specific subclass because it is metadata for all graph elements when it
     * is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfChangeSetIdentifier retrievePbfChangeSetIdentifier(final GraphElement element)
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
    public PbfRevisionNumber retrievePbfRevisionNumber(final GraphElement element)
    {
        return PBF_REVISION_NUMBER.retrieveObject(element, revision -> new PbfRevisionNumber((int) revision));
    }

    /**
     * This method returns the PBF-specific user identifier for the user who last modified this {@link GraphElement}.
     * This value is not in a PBF-specific subclass because it is metadata for all graph elements when it is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfUserIdentifier retrievePbfUserIdentifier(final GraphElement element)
    {
        return PBF_USER_IDENTIFIER.retrieveObject(element, value -> new PbfUserIdentifier((int) value));
    }

    /**
     * This method returns the PBF-specific user name for the user who last modified this {@link GraphElement}. This
     * value is not in a PBF-specific subclass because it is metadata for all graph elements when it is available.
     *
     * @return The OSM change set identifier for the given {@link GraphElement}.
     */
    public PbfUserName retrievePbfUserName(final GraphElement element)
    {
        final var userName = PBF_USER_NAME.retrieveString(element);
        if (userName != null)
        {
            return new PbfUserName(userName);
        }
        return null;
    }

    /**
     * @return The list of tags for this {@link GraphElement}.
     */
    public final PbfTagList retrieveTagList(final GraphElement element)
    {
        final var tags = tags();
        return tags == null ? null : tags.tagList(element);
    }

    /**
     * @return The list of tags for this {@link GraphElement}.
     */
    public final PbfTagMap retrieveTagMap(final GraphElement element)
    {
        final var tags = tags();
        return tags == null ? null : tags.tagMap(element);
    }

    /**
     * @return The number of physical {@link GraphElement}s that are stored in this store. This is distinct from the
     * {@link #count()}, which takes into account reversible edges in the EdgeStore subclass.
     */
    @Override
    public final int size()
    {
        return size;
    }

    /**
     * Stores all of the metadata attributes of the given graph element using its index
     */
    @MustBeInvokedByOverriders
    public void storeAttributes(final GraphElement element)
    {
        LAST_MODIFIED.storeObject(element, element.lastModificationTime());
        PBF_CHANGE_SET_IDENTIFIER.storeObject(element, element.pbfChangeSetIdentifier());
        PBF_REVISION_NUMBER.storeObject(element, element.pbfRevisionNumber());
        PBF_USER_IDENTIFIER.storeObject(element, element.pbfUserIdentifier());
        final var userName = element.pbfUserName();
        if (userName != null)
        {
            PBF_USER_NAME.storeString(element, userName.name());
        }
        storeTags(element, element.tagList());
    }

    /**
     * Stores any tags for a given {@link GraphElement}
     */
    public void storeTags(final GraphElement element, final PbfTagList tags)
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
    public boolean supports(final Attribute<?> attribute)
    {
        return attributeLoader().supports(attribute);
    }

    /**
     * @return The tag codec being used to compress tags in this store
     */
    public PbfTagCodec tagCodec()
    {
        final var tags = tags();
        return tags == null ? null : tags().codec();
    }

    /**
     * @return The tag store for this store
     */
    @SuppressWarnings({ "exports", "ClassEscapesDefinedScope" })
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
    public Validator validator(final Validation validation)
    {
        final var outer = this;
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
                        final var identifier = outer.identifier.get(index);

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
     * @return The {@link EdgeStore} associated with the graph which owns this store
     */
    protected EdgeStore edgeStore()
    {
        return graph().edgeStore();
    }

    /**
     * @return Factory that can create graph elements of type T
     */
    protected abstract GraphElementFactory<T> elementFactory();

    /**
     * @return The class of {@link GraphElement} in this store
     */
    protected abstract Class<T> elementType();

    protected Estimate estimatedElements()
    {
        return metadata().count(ALLOW_ESTIMATE, elementType()).asEstimate();
    }

    protected Estimate estimatedElements(final Class<? extends GraphElement> type)
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
    protected int identifierToIndex(final long identifier, final IndexingMode mode)
    {
        if (!IDENTIFIER_TO_INDEX.load())
        {
            IDENTIFIER_TO_INDEX.allocate();
        }

        // Get the index for the given element identifier if any
        var index = identifierToIndex.get(Math.abs(identifier));
        final var isNull = identifierToIndex.isNull(index);

        // If there's no index and we can create one,
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
     * @return The next element index in this store
     */
    protected int nextIndex()
    {
        return nextIndex++;
    }

    /**
     * Called when an element is being added
     */
    @MustBeInvokedByOverriders
    protected void onAdd(final T element)
    {
    }

    /**
     * Called after an element is added
     */
    @MustBeInvokedByOverriders
    protected void onAdded(final T ignored)
    {
    }

    /**
     * Called before an element is added
     */
    @MustBeInvokedByOverriders
    protected void onAdding(final T ignored)
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
     * Called to initialize this store
     */
    @MustBeInvokedByOverriders
    protected void onInitialize()
    {
        resetNextIndex();
    }

    /**
     * Overwrites the existing element with a new element
     *
     * @param existingIndex The index of the existing element
     * @param existingIdentifier The identifier of the existing element
     * @param newIdentifier The new element identifier
     */
    protected void overwriteElement(final int existingIndex, final long existingIdentifier, final long newIdentifier)
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
     * @return The place store associated with the graph that owns this store
     */
    protected PlaceStore placeStore()
    {
        return graph().placeStore();
    }

    /**
     * @return The relation store associated with the graph that owns this store
     */
    protected RelationStore relationStore()
    {
        return graph().relationStore();
    }

    /**
     * Removes the given graph element. This can only be performed during graph loading. Once the graph is loaded, it
     * becomes unmodifiable.
     */
    protected void remove(final GraphElement element)
    {
        IDENTIFIER_TO_INDEX.load();
        identifierToIndex.remove(element.identifierAsLong());
    }

    /**
     * Resets the next element index to the first index
     */
    protected void resetNextIndex()
    {
        nextIndex = 1;
    }

    /**
     * @return The shape point store associated with the graph that owns this store
     */
    protected ShapePointStore shapePointStore()
    {
        return graph().shapePointStore();
    }

    /**
     * @return The vertex store associated with the graph that owns this store
     */
    protected VertexStore vertexStore()
    {
        return graph().vertexStore();
    }

    private Batcher<T> batcher()
    {
        if (batcher == null)
        {
            batcher = new Batcher<>(qualifiedName(), QUEUE_SIZE, BATCH_SIZE)
            {
                @Override
                protected void onBatch(final Batch batch)
                {
                    for (final var element : batch)
                    {
                        internalAdd(element);
                    }
                }
            };
        }
        return batcher;
    }

    private void checkIdentifier(final long identifier)
    {
        assert identifier != 0;
        assert identifier != Long.MIN_VALUE;
    }

    private void checkIndex(final int index)
    {
        assert index > 0;
    }

    /**
     * @param element The {@link GraphElement} to add to this store
     * @return True if the element was added and false if it was invalid
     */
    @SuppressWarnings("unchecked")
    private synchronized boolean internalAdd(final T element)
    {
        // If we have already finalized changes to this store,
        if (committed)
        {
            // then we cannot add any more data
            LOGGER.problem("Attempt to add $ to committed graph store '$'", elementType().getSimpleName(), objectName());
            return false;
        }

        // If the element is valid
        if (element.validator(GraphElement.VALIDATE_RAW).isValid(DEBUG.listener()))
        {
            // NOTE: An element MUST be heavyweight to be added to a store. This is because a flyweight element cannot
            // belong to two graphs at once. The flyweight element will have its own graph and index, but those fields
            // will be overwritten prior to adding the element's values to storage. As soon as those fields are changed,
            // it belongs to the new graph and its values can no longer be retrieved in order to add it.
            final var heavyweight = (T) element.asHeavyWeight();

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
            catch (final Exception e)
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

    @NotNull
    private String qualifiedName()
    {
        return graph().name() + "-" + elementType().getSimpleName();
    }

    /**
     * Stores the element identifier for the element at the given index
     */
    private void storeIdentifier(final int index, final long identifier)
    {
        IDENTIFIER.allocate();
        IDENTIFIER_TO_INDEX.allocate();

        this.identifier.set(index, identifier);
        identifierToIndex.put(identifier, index);

        ensure(this.identifier.get(index) == identifier);
        ensure(identifierToIndex.get(identifier) == index);
    }
}
