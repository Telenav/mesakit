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

package com.telenav.tdk.graph.specifications.library.attributes;

import com.telenav.tdk.core.collections.primitive.list.*;
import com.telenav.tdk.core.collections.primitive.list.store.PackedStringStore;
import com.telenav.tdk.core.collections.primitive.map.PrimitiveScalarMap;
import com.telenav.tdk.core.collections.primitive.map.multi.PrimitiveScalarMultiMap;
import com.telenav.tdk.core.collections.primitive.set.PrimitiveSet;
import com.telenav.tdk.core.kernel.debug.Debug;
import com.telenav.tdk.core.kernel.interfaces.collection.*;
import com.telenav.tdk.core.kernel.interfaces.factory.*;
import com.telenav.tdk.core.kernel.interfaces.naming.NamedObject;
import com.telenav.tdk.core.kernel.interfaces.numeric.Quantizable;
import com.telenav.tdk.core.kernel.interfaces.operation.Initializable;
import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.language.reflection.Type;
import com.telenav.tdk.core.kernel.language.reflection.property.Property;
import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.messaging.messages.lifecycle.OperationSucceeded;
import com.telenav.tdk.core.resource.compression.archive.FieldArchive;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.io.archive.GraphArchive;
import com.telenav.tdk.graph.metadata.DataSpecification;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.List;

import static com.telenav.tdk.core.kernel.validation.Validate.*;

/**
 * Manages a referent (a value that's referred to by a reference) {@link Attribute}s in an {@link AttributeStore}.
 * References are lazy-loaded with {@link #load()} and can be unloaded with {@link #unload()}.  An attribute reference
 * has a field name and a factory that will create a referent if one needs to be allocated.
 * <p>
 * The kind of attribute being managed can be retrieved with {@link #attribute()} and the archive to load from can be
 * attached with {@link #attach(FieldArchive)}. When a referent is loaded, {@link #onLoaded(NamedObject)} is called and
 * when an attribute is allocated, {@link #onAllocated()} is called.
 * <p>
 * The remaining methods in attribute reference are methods for retrieving and storing different kinds of values in the
 * referent object. Many of these methods take a {@link Quantizable} rather than a specific primitive key value. This
 * permits any quantizable object to be used as a key. For example, all {@link GraphElement}s are {@link Indexable} and
 * indexable objects are {@link Quantizable}, so an {@link Edge} (which is a graph element) can be used as an index into
 * an array. The quantum (long value) is just the index in this case.
 *
 * @author jonathanl (shibo)
 * @see Attribute
 * @see AttributeStore
 * @see FieldArchive
 * @see GraphArchive
 * @see Property
 * @see Factory
 * @see Quantizable
 */
@SuppressWarnings({ "ConstantConditions", "rawtypes" })
public class AttributeReference<Referent extends NamedObject & Initializable> implements NamedObject
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** The store being referenced */
    private final AttributeStore store;

    /** The attached field archive to load from */
    private FieldArchive archive;

    /** The attribute in the store */
    private final Attribute<?> attribute;

    /** The store field to manage */
    private final Property field;

    /** A cached reference to the store field (so reflection does not have to be used often) */
    private volatile Referent reference;

    /** A factory to create the referent */
    private final Factory<Referent> factory;

    /** The name of the field referenced in the attribute store */
    private final String fieldName;

    /** True if a load has been attempted */
    private boolean loadAttempted;

    /**
     * @param store The attribute store that's being managed
     * @param attribute The attribute in the store
     * @param fieldName The name of the field in the store that's being managed
     * @param factory A factory that can create the referent
     */
    public AttributeReference(final AttributeStore store, final Attribute<?> attribute, final String fieldName,
                              final Factory<Referent> factory)
    {
        assert store != null;
        assert attribute != null;
        assert factory != null;

        this.store = store;
        this.attribute = attribute;
        this.factory = factory;
        this.fieldName = fieldName;

        // Store a field reference for the named field in the given store
        field = Type.forClass(this.store.getClass()).field(fieldName);
        assert field != null : store.objectName() + "." + fieldName + " does not exist";

        // and add this attribute reference to the store's attribute loader
        store.attributeLoader().add(this);
    }

    /**
     * Allocates and assigns a referent to the reference if it is null
     *
     * @return The allocated referent, or the existing referent
     */
    @SuppressWarnings("unchecked")
    public synchronized Referent allocate()
    {
        // If there is no reference yet,
        if (reference == null)
        {
            // and this attribute is supported
            if (supported())
            {
                // check the field in the store, and if it hasn't been allocated yet,
                final var storeValue = (Referent) field.get(store);
                if (storeValue == null)
                {
                    // create and initialize the referent,
                    final var referent = factory.newInstance();
                    trace("allocated");
                    ensureEqual(field.name(), referent.objectName(), "The field name '$' and the name of the referent '$' should match", field.name(), referent.objectName());
                    referent.objectName(store.objectName() + "." + field.name());
                    referent.initialize();

                    // and assign to the reference and store field.
                    reference(referent);
                }
                else
                {
                    // otherwise, assign the existing referent from the store.
                    reference = storeValue;
                }
            }
        }

        // Return the reference
        return reference;
    }

    /**
     * Attaches this reference to the given field archive so that referenced attributes can be loaded with {@link
     * #load()}
     */
    public void attach(final FieldArchive archive)
    {
        this.archive = archive;
        trace("attached '$'", archive.zip().resource());
    }

    /**
     * @return The attribute that's being referenced
     */
    public Attribute<?> attribute()
    {
        return attribute;
    }

    /**
     * @return The name of the field in the attribute store that is being referenced
     */
    public String fieldName()
    {
        return fieldName;
    }

    /**
     * @return True if the referent is loaded. Note that the store field and the reference are always assigned at the
     * same time with {@link #reference(NamedObject)}
     */
    public boolean isLoaded()
    {
        return reference != null;
    }

    public synchronized boolean load()
    {
        if (reference == null)
        {
            if (!loadAttempted)
            {
                loadAttempted = true;

                // load the reference
                final var archive = archive();
                if (archive != null)
                {
                    final Referent reference = archive.loadFieldOf(store, field.name());
                    if (reference != null)
                    {
                        if (reference instanceof Sized)
                        {
                            final var size = ((Sized) reference).size();
                            trace("loaded (size $)", size);
                        }
                        else
                        {
                            trace("loaded");
                        }
                        onLoaded(reference);
                        reference(reference);
                        return true;
                    }
                    else
                    {
                        DEBUG.trace("Unable to load $ from $", objectName(), store.objectName());
                    }
                }
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * @return The name of this attribute reference as [store].[property]
     */
    @Override
    public String objectName()
    {
        return store.objectName() + "." + attribute.name();
    }

    /**
     * @return Returns the boolean value at the index specified by the quantizable index object
     */
    public boolean retrieveBoolean(final Quantizable index)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof LongList)
        {
            final var list = (LongList) reference;
            return list.safeGet((int) index.quantum()) != 0;
        }
        else if (reference instanceof PrimitiveSet)
        {
            final var set = (PrimitiveSet) reference;
            return set.contains(index.quantum());
        }
        else if (reference instanceof PrimitiveScalarMap)
        {
            final var map = (PrimitiveScalarMap) reference;
            return map.getScalar(index.quantum()) == 1;
        }

        failedToRetrieveAttribute();
        return false;
    }

    /**
     * @return Returns the int value at the index specified by the quantizable index object
     */
    public int retrieveInt(final Quantizable index)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof IntList)
        {
            final var list = (IntList) reference;
            return list.safeGet((int) index.quantum());
        }

        failedToRetrieveAttribute();
        return Integer.MIN_VALUE;
    }

    /**
     * @return Returns the long value at the index specified by the quantizable index object
     */
    public long retrieveLong(final Quantizable index)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof LongList)
        {
            final var list = (LongList) reference;
            return list.safeGet((int) index.quantum());
        }

        failedToRetrieveAttribute();
        return Long.MIN_VALUE;
    }

    /**
     * @return An object for the given quantizable value
     */
    public <T> T retrieveObject(final Quantizable index, final PrimitiveMapFactory<T> factory)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof PrimitiveList)
        {
            final var array = (PrimitiveList) reference;
            final var value = array.safeGetPrimitive((int) index.quantum());
            if (!array.isPrimitiveNull(value))
            {
                return factory.newInstance(value);
            }
            return null;
        }
        else if (reference instanceof PrimitiveScalarMap)
        {
            final var array = (PrimitiveScalarMap) reference;
            final var value = array.getScalar((int) index.quantum());
            if (!array.isScalarValueNull(value))
            {
                return factory.newInstance(value);
            }
            return null;
        }

        failedToRetrieveAttribute();
        return null;
    }

    public <T> ObjectList<T> retrieveObjectList(final Quantizable index, final PrimitiveMapFactory<T> factory)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof PrimitiveScalarMultiMap)
        {
            final var map = (PrimitiveScalarMultiMap) reference;
            ensure(index != null);
            final var values = map.getPrimitiveList(index.quantum());
            if (values != null)
            {
                return values.asList(factory);
            }
            return ObjectList.emptyList();
        }

        failedToRetrieveAttribute();
        return null;
    }

    public <T> ObjectList<T> retrieveSignedObjectList(final Quantizable index, final PrimitiveMapFactory<T> factory)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof PrimitiveScalarMultiMap)
        {
            final var map = (PrimitiveScalarMultiMap) reference;
            ensure(index != null);
            final var values = map.getSignedPrimitiveList(index.quantum());
            if (values != null)
            {
                return values.asList(factory);
            }
            return ObjectList.emptyList();
        }

        failedToRetrieveAttribute();
        return null;
    }

    /**
     * @return Returns the long value at the index specified by the quantizable index object
     */
    public String retrieveString(final Quantizable index)
    {
        if (!load())
        {
            allocate();
        }

        if (reference instanceof PackedStringStore)
        {
            return ((PackedStringStore) reference).get((int) index.quantum());
        }

        failedToRetrieveAttribute();
        return null;
    }

    /**
     * Stores the given value at the index specified by the quantizable index object
     */
    public void storeBoolean(final Quantizable index, final boolean value)
    {
        if (supported())
        {
            storeObject(index, value ? 1 : 0);
        }
    }

    /**
     * Stores the given value at the index specified by the quantizable index object
     */
    public void storeBoolean(final Quantizable index, final Boolean value)
    {
        if (value != null)
        {
            if (supported())
            {
                storeBoolean(index, value.booleanValue());
            }
        }
    }

    /**
     * Stores the given value at the index specified by the quantizable index object
     */
    public void storeObject(final Quantizable index, final Quantizable value)
    {
        if (value != null)
        {
            if (supported())
            {
                storeObject(index, value.quantum());
            }
        }
    }

    /**
     * Stores the given value at the index specified by the quantizable index object
     */
    public void storeObject(final Quantizable index, final long value)
    {
        assert index != null;

        if (supported())
        {
            final var attribute = allocate();
            if (attribute instanceof PrimitiveList)
            {
                ((PrimitiveList) attribute).setPrimitive((int) index.quantum(), value);
            }
            else if (reference instanceof PrimitiveScalarMap)
            {
                final var map = (PrimitiveScalarMap) reference;
                map.putScalar(index.quantum(), value);
            }
            else if (reference instanceof PrimitiveSet)
            {
                if (value != 0)
                {
                    final var set = (PrimitiveSet) reference;
                    set.add(index.quantum());
                }
            }
            else
            {
                failedToStoreAttribute();
            }
        }
    }

    /**
     * Stores the given list of values at the index specified by the quantizable index object
     */
    public void storeObjectList(final Quantizable index, final List<? extends Quantizable> values)
    {
        if (values != null)
        {
            if (supported())
            {

                final var attribute = allocate();
                if (attribute instanceof PrimitiveScalarMultiMap)
                {
                    ((PrimitiveScalarMultiMap) attribute).putPrimitiveList((int) index.quantum(), values);
                }
                else
                {
                    failedToStoreAttribute();
                }
            }
        }
    }

    /**
     * Stores the given string at the index specified by the quantizable index object
     */
    public void storeString(final Quantizable index, final String value)
    {
        if (supported())
        {
            final var attribute = allocate();
            if (attribute instanceof PackedStringStore)
            {
                ((PackedStringStore) attribute).set((int) index.quantum(), value);
            }
            else
            {
                failedToStoreAttribute();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return attribute.name();
    }

    /**
     * Unloads the referent to free up memory
     */
    public void unload()
    {
        // We must have an archive or we cannot reload the attribute
        final var archive = archive();
        assert archive != null : "Cannot clear attribute without archive attached or the attribute cannot be reloaded";

        // Clear the reference
        if (reference != null)
        {
            reference(null);
            trace("unloaded");
        }
    }

    /** Called when a referent is allocated */
    @MustBeInvokedByOverriders
    protected void onAllocated()
    {
    }

    /** Called when a referent is loaded */
    @MustBeInvokedByOverriders
    protected void onLoaded(final Referent value)
    {
    }

    private FieldArchive archive()
    {
        if (archive == null)
        {
            archive = store.archive();
        }
        return archive;
    }

    private DataSpecification dataSpecification()
    {
        return store.graph().dataSpecification();
    }

    private void failedToRetrieveAttribute()
    {
        if (supported())
        {
            fail("Unable to retrieve value for attribute $", objectName());
        }
    }

    private void failedToStoreAttribute()
    {
        fail("Unable to store value for attribute $", objectName());
    }

    /**
     * Assigns the given reference to the reference and to the attribute field in the store
     */
    private synchronized void reference(final Referent referent)
    {
        reference = referent;
        if (!(field.setter().set(store, referent) instanceof OperationSucceeded))
        {
            LOGGER.problem("Unable to set value of $ field", field);
        }
        assert field.getter().get(store) == referent;
    }

    private boolean supported()
    {
        return dataSpecification().supports(attribute);
    }

    private void trace(final String message, final Object... arguments)
    {
        final var objects = new Object[arguments.length + 3];
        objects[0] = store.objectName();
        objects[1] = store.hashCode();
        objects[2] = attribute.name();
        System.arraycopy(arguments, 0, objects, 3, arguments.length);
        DEBUG.trace("AttributeReference $[${hex}].$: " + message, objects);
    }
}
