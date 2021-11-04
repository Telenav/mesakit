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

package com.telenav.mesakit.graph.specifications.library.attributes;

import com.telenav.kivakit.kernel.interfaces.loading.Unloadable;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.language.collections.CompressibleCollection;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.primitive.collections.array.scalars.IntArray;
import com.telenav.kivakit.resource.compression.archive.FieldArchive;
import com.telenav.mesakit.graph.io.archive.GraphArchive;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * Loads attributes from a {@link FieldArchive} (note that {@link GraphArchive} is a field archive) and populates the
 * fields of an {@link AttributeStore} through {@link AttributeReference} objects. Operations on the attribute loader
 * for a store apply to all references. If you want to operate on just one attribute, see {@link AttributeReference}.
 * <p>
 * <b>Attributes</b>
 * <ul>
 *     <li>{@link #add(AttributeReference)} - Adds the given attribute reference to this loader</li>
 *     <li>{@link #attributes()} - Retrieves the attributes managed by this loader</li>
 *     <li>{@link #supports(Attribute)} - True if the given attribute is supported by this loader</li>
 * </ul>
 * <p>
 * <b>Loading and Unloading</b>
 * <ul>
 *     <li>{@link #attach(FieldArchive)} - Attaches the given field archive to load from</li>
 *     <li>{@link #load(Attribute)} - Loads the given attribute</li>
 *     <li>{@link #loadAll()} - Loads all attributes</li>
 *     <li>{@link #loadAllExcept(AttributeSet)} - Loads all attributes except for the given ones</li>
 *     <li>{@link #loadRequired(Attribute)} - Loads the given attribute, but throws an exception if this fails</li>
 *     <li>{@link #isLoaded()} - True if any attribute is loaded, false if all attributes are unloaded</li>
 *     <li>{@link #unload()} - Unloads all attributes</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see FieldArchive
 * @see GraphArchive
 * @see Attribute
 * @see AttributeReference
 * @see AttributeStore
 */
public class AttributeLoader implements Unloadable, NamedObject
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Map from attributes to referenced fields
     */
    private final Map<Attribute<?>, AttributeReference<?>> referenceForAttribute = new TreeMap<>(
            Comparator.comparing(Attribute::name));

    /**
     * PrimitiveArray of integers indexed by attribute identifier. If the integer is zero, the attribute is not
     * supported. If it is non-zero, the attribute is supported
     */
    private final transient IntArray supported;

    /** The name of this object for debugging purposes */
    private String objectName;

    public AttributeLoader(String objectName)
    {
        assert objectName != null;

        this.objectName = objectName;

        supported = new IntArray(objectName() + ".supported");
        supported.initialize();
    }

    /**
     * Adds the attribute reference to this loader
     */
    public void add(AttributeReference<?> reference)
    {
        var attribute = reference.attribute();
        referenceForAttribute.put(attribute, reference);
        supported.set(attribute.identifier(), 1);
    }

    /**
     * Allocate any attributes that are null, but trim them to a minimum size. This method is called before saving to
     * ensure that empty stores have data structures that are empty, but non-null.
     */
    public void allocateAll()
    {
        for (var reference : references())
        {
            var referent = reference.allocate();
            if (referent instanceof CompressibleCollection)
            {
                ((CompressibleCollection) referent).compress(CompressibleCollection.Method.FREEZE);
            }
        }
    }

    public void attach(FieldArchive archive)
    {
        assert archive != null;

        for (var reference : references())
        {
            reference.attach(archive);
        }
    }

    /**
     * @return The attributes being loaded by this loader
     */
    public ObjectList<Attribute<?>> attributes()
    {
        return ObjectList.objectList(referenceForAttribute.keySet()).sorted();
    }

    /**
     * True if one or more attributes is loaded, false otherwise
     */
    public boolean isLoaded()
    {
        // Go through each reference
        for (var reference : references())
        {
            // and if it's populated,
            if (reference.isLoaded())
            {
                // the store is loaded
                return true;
            }
        }
        return false;
    }

    /**
     * Loads the given attribute
     */
    public boolean load(Attribute<?> attribute)
    {
        return reference(attribute).load();
    }

    /**
     * Forces all attributes in this store to load from the archive
     */
    public void loadAll()
    {
        for (var reference : references())
        {
            DEBUG.trace("Force loading $", reference);
            reference.load();
        }
    }

    /**
     * Forces all attributes to load except the given ones
     */
    public void loadAll(AttributeSet attributes)
    {
        for (var reference : references())
        {
            if (attributes.contains(reference.attribute()))
            {
                DEBUG.trace("Force loading $", reference);
                reference.load();
            }
        }
    }

    /**
     * Forces all attributes to load except the given ones
     */
    public void loadAllExcept(AttributeSet attributes)
    {
        for (var reference : references())
        {
            if (!attributes.contains(reference.attribute()))
            {
                DEBUG.trace("Force loading $", reference);
                reference.load();
            }
        }
    }

    /**
     * Loads the given attribute and throws an exception if it cannot be loaded
     */
    public void loadRequired(Attribute<?> attribute)
    {
        if (!load(attribute))
        {
            fail("Required attribute '" + attribute + "' is not available.");
        }
    }

    @Override
    public String objectName()
    {
        return objectName;
    }

    @Override
    public void objectName(String objectName)
    {
        this.objectName = objectName;
    }

    /**
     * @return All attribute references that this loader is loading
     */
    public Collection<AttributeReference<?>> references()
    {
        return referenceForAttribute.values();
    }

    /**
     * @return True if this attribute loader supports the given attribute
     */
    public boolean supports(Attribute<?> attribute)
    {
        return supported.safeGet(attribute.identifier()) != 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unload()
    {
        // Go through each reference
        for (var reference : references())
        {
            // clearing it
            reference.unload();
        }
    }

    /**
     * @return The attribute reference for the given attribute
     */
    private AttributeReference<?> reference(Attribute<?> attribute)
    {
        return referenceForAttribute.get(attribute);
    }
}
