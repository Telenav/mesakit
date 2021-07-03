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

import com.telenav.kivakit.kernel.interfaces.loading.Unloadable;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeLoader;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeSet;
import com.telenav.mesakit.map.geography.Precision;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * Base class for various kinds of attribute stores that are stored in {@link GraphArchive}s. Archived stores are
 * lazy-loaded, but can be forced partly or entirely into memory by calling {@link #loadAll()} or {@link
 * #loadAllExcept(AttributeSet)}. Loading of attributes is performed by an associated {@link AttributeLoader} which uses
 * {@link AttributeReference}s manage individual attributes. Archived stores can be unloaded from memory by calling the
 * method {@link #unload()} and the attached {@link GraphArchive} can be retrieved by subclasses with {@link
 * #archive()}.
 *
 * @author jonathanl (shibo)
 * @see EdgeStore
 * @see VertexStore
 * @see RelationStore
 */
public abstract class ArchivedGraphElementStore<T extends GraphElement> extends GraphElementStore<T> implements Unloadable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Field archive to load fields from
     */
    private transient GraphArchive archive;

    /**
     * The attribute loader
     */
    private transient AttributeLoader loader;

    /** True if the store has been loaded */
    private boolean loaded;

    /**
     * @param graph The graph that owns this element store
     */
    protected ArchivedGraphElementStore(final Graph graph)
    {
        super(graph);
    }

    /**
     * @return The graph archive for this element store
     */
    @Override
    public GraphArchive archive()
    {
        return archive;
    }

    /**
     * Associates the given archive with this store as a source of attribute data that can be lazy-loaded.
     *
     * @param archive The field archive to load fields from
     */
    public final void attach(final GraphArchive archive)
    {
        ensureNotNull(archive);

        // Before attaching the archive
        onAttaching(archive);

        // Save the archive reference,
        this.archive = archive;

        // and attach the archive to the attribute loader that manages attributes of this store.
        attributeLoader().attach(archive);

        // After attaching the archive
        onAttached(archive);
    }

    /**
     * @return The loader for this attribute store
     */
    @Override
    public AttributeLoader attributeLoader()
    {
        if (loader == null)
        {
            loader = new AttributeLoader(objectName());
        }
        return loader;
    }

    /**
     * True if this store is currently shrunken
     */
    public boolean isUnloaded()
    {
        return !attributeLoader().isLoaded();
    }

    /**
     * Forces this store to load all attributes
     */
    public void loadAll()
    {
        attributeLoader().loadAll();
    }

    /**
     * Forces all attributes to load except the given ones
     */
    public void loadAll(final AttributeSet attributes)
    {
        attributeLoader().loadAll(attributes);
    }

    /**
     * Forces all attributes to load except the given ones
     */
    public void loadAllExcept(final AttributeSet attributes)
    {
        attributeLoader().loadAllExcept(attributes);
    }

    public void loaded(final Resource resource)
    {
        if (!loaded)
        {
            loaded = true;
            onLoaded(resource);
        }
    }

    public void loaded(final GraphArchive archive)
    {
        onLoaded(archive);
    }

    public void loading(final Resource resource)
    {
        onLoading(resource);
    }

    public void loading(final GraphArchive archive)
    {
        // load the unmanaged fields of the superclass,
        archive.loadFieldOf(this, "size");
        archive.loadFieldOf(this, "nextIndex");

        onLoading(archive);
    }

    /**
     * @return Metadata for the graph that owns this element store
     */
    @Override
    public Metadata metadata()
    {
        return graph().metadata();
    }

    /**
     * Saves the state of this element store
     */
    public void save()
    {
        DEBUG.trace("Saving $", objectName());
        onSaving(archive());
        attributeLoader().allocateAll();
        archive().saveFieldsOf(this, GraphArchive.VERSION);
        onSaved(archive());
    }

    public void saved(final GraphArchive archive)
    {
        onSaved(archive);
    }

    public void saving(final GraphArchive archive)
    {
        onSaving(archive);
    }

    /**
     * Shrinks this store by clearing out all the attribute fields
     */
    @Override
    public final void unload()
    {
        onUnloading();
        attributeLoader().unload();
        onUnloaded();
    }

    /**
     * Loads the given attribute
     */
    protected void load(final Attribute<?> attribute)
    {
        attributeLoader().load(attribute);
    }

    /**
     * Loads the named field from this attribute store
     */
    @SuppressWarnings("SameParameterValue")
    protected void loadField(final String fieldName)
    {
        archive.loadFieldOf(this, fieldName);
    }

    /**
     * Called after an archive has been attached to this store
     */
    @MustBeInvokedByOverriders
    protected void onAttached(final GraphArchive archive)
    {
    }

    /**
     * Called before an archive is attached to this store
     */
    @MustBeInvokedByOverriders
    protected void onAttaching(final GraphArchive archive)
    {
    }

    @MustBeInvokedByOverriders
    protected void onLoaded(final GraphArchive archive)
    {

    }

    @MustBeInvokedByOverriders
    protected void onLoaded(final Resource resource)
    {
    }

    @MustBeInvokedByOverriders
    protected void onLoading(final Resource resource)
    {
    }

    @MustBeInvokedByOverriders
    protected void onLoading(final GraphArchive archive)
    {
    }

    /**
     * Called after this store is saved
     */
    @MustBeInvokedByOverriders
    protected void onSaved(final GraphArchive archive)
    {
    }

    /**
     * Called before this store is saved
     */
    @MustBeInvokedByOverriders
    protected void onSaving(final GraphArchive archive)
    {
    }

    /**
     * Called after attributes are unloaded
     */
    @MustBeInvokedByOverriders
    protected void onUnloaded()
    {
    }

    /**
     * Called before unloading takes place
     */
    @MustBeInvokedByOverriders
    protected void onUnloading()
    {
    }

    protected Precision precision()
    {
        return graph().precision();
    }
}