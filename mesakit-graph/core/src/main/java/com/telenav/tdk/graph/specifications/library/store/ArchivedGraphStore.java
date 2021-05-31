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

package com.telenav.kivakit.graph.specifications.library.store;

import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.interfaces.persistence.Unloadable;
import com.telenav.kivakit.kernel.language.reflection.Type;
import com.telenav.kivakit.kernel.language.string.*;
import com.telenav.kivakit.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.scalars.bytes.Bytes;
import com.telenav.kivakit.kernel.time.Time;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.kivakit.graph.specifications.library.attributes.AttributeSet;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public abstract class ArchivedGraphStore extends GraphStore
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /** The archive resource */
    private transient Resource resource;

    /** The archive attached to this graph store (if any) */
    private transient GraphArchive archive;

    /** True if this store is loading (from a resource or not), false if it can no longer be modified */
    private transient boolean loading = true;

    /** True when data is being unloaded */
    private transient boolean unloading;

    /**
     * Construct this graph store for the given graph, including the given features. The estimated number of vertexes
     * and edges is used to prevent unnecessary map rehashes to improve performance. It is a good idea to guess
     * accurately, but round up a bit.
     */
    protected ArchivedGraphStore(final Graph graph)
    {
        super(graph);
    }

    /**
     * True if this store is loading data, false if it's done, at which point the store can't be modified.
     */
    public final boolean isLoading()
    {
        return loading;
    }

    public final boolean isUnloaded()
    {
        for (final var store : stores())
        {
            if (!store.isUnloaded())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads this graph store with data from a .graph archive that was created by {@link #save(GraphArchive)}.
     */
    public final void load(final GraphArchive archive)
    {
        // Check that we haven't already loaded into this graph store
        ensure(isEmpty(), "Cannot load data into an existing, loaded graph");

        // Record start time
        final var start = Time.now();

        // We're about to load
        loading(archive);

        // Attach archive to all the sub-stores
        attach(archive);

        // Clear out all the lazy-loaded fields we will load from archive
        unload();

        // Attach the graph archive and the field archive based on it
        // We can't register the spatial index serializer in KivaKitGraphCore because
        // it needs the graph in order to function, so we do it here.

        // Load metadata
        final var metadata = graph().metadata(archive.metadata());
        bounds(metadata.dataBounds());

        // We're done loading
        loaded(archive);

        // Done!
        graph().information(Strings.textBox(Message.format("Loaded from $ in $",
                graph().metadata().descriptor(), start.elapsedSince()), graph().asString()));
    }

    /**
     * Force each graph element store to fully load
     */
    public void loadAll()
    {
        forEachStore((store) ->
        {
            DEBUG.trace("Force loading $", store.getClass().getSimpleName());
            store.loadAll();
        });
    }

    /**
     * Force each graph element store to fully load, except for the given attributes
     */
    public void loadAll(final AttributeSet except)
    {
        for (final var store : stores())
        {
            store.loadAll(except);
        }
    }

    /**
     * Force each graph element store to fully load, except for the given attributes
     */
    public void loadAllExcept(final AttributeSet except)
    {
        for (final var store : stores())
        {
            store.loadAllExcept(except);
        }
    }

    public void loaded(final Resource resource)
    {
        // Save the data source we loaded,
        resource(resource);

        // then notify each sub-store and our subclass that we're loaded.
        forEachStore((store) -> store.loaded(resource));
        onLoaded(resource);
    }

    public void loading(final Resource resource)
    {
        loading = true;
        onLoading(resource);
        forEachStore((store) -> store.loading(resource));
    }

    @Override
    public String objectName()
    {
        return "graph.store";
    }

    public Resource resource()
    {
        return resource;
    }

    public void resource(final Resource resource)
    {
        this.resource = resource;
    }

    /**
     * Archives this graph store's data for future use via {@link #load(GraphArchive)}.
     *
     * @param archive The .graph file to save to
     */
    public final void save(final GraphArchive archive)
    {
        // If the store is invalid
        if (!isValid())
        {
            // we cannot save
            problem("Cannot save invalid graph to $", archive.zip().resource()).throwAsIllegalStateException();
        }

        // Record start time
        final var start = Time.now();

        try
        {
            // Create archive and save all non-null archived fields
            // We can't register the spatial index serializer in KivaKitGraphCore because
            // it needs the graph in order to function, so we do it here.

            // Attach archive
            attach(archive);

            // We're starting to save
            saving(archive);

            // Save metadata with kryo
            final var metadata = graph().metadata().withDataBounds(bounds());
            archive.saveMetadata(metadata);

            // Save fields of each graph element store
            forEachStore(ArchivedGraphElementStore::save);

            // Close the archive
            archive.close();

            // We're done saving
            saved(archive);
        }
        catch (final Exception e)
        {
            problem(e, "Unable to save to $", archive);
        }

        // We're done!
        final var report = new StringList();
        report.add("output: " + archive);
        report.add("elapsed: " + start.elapsedSince());
        report.add(graph().asString());
        information(Strings.textBox(Message.format("Saved $",
                metadata().descriptor()), report.join("\n")));
    }

    /**
     * Shrinks this store by removing references to reloadable objects
     */
    @Override
    public final synchronized void unload()
    {
        if (!unloading)
        {
            unloading = true;
            try
            {
                onUnloading(archive);

                // Must specify -javaagent to VM, see JavaVirtualMachine.sizeOfObjectGraph()
                JavaVirtualMachine.local().traceSizeChange(this, "unload", this, Bytes.kilobytes(100), () ->
                {
                    for (final var object : Type.of(this).reachableObjects(this))
                    {
                        if (object instanceof Unloadable)
                        {
                            ((Unloadable) object).unload();
                        }
                    }
                });

                onUnloaded(archive);
            }
            finally
            {
                unloading = false;
            }
        }
    }

    protected void onAttached(final GraphArchive archive)
    {
    }

    protected void onAttaching(final GraphArchive archive)
    {
    }

    protected void onLoaded(final Resource resource)
    {
    }

    protected void onLoaded(final GraphArchive archive)
    {
    }

    protected void onLoading(final Resource resource)
    {
    }

    protected void onLoading(final GraphArchive archive)
    {
    }

    protected void onSaved(final GraphArchive archive)
    {
    }

    protected void onSaving(final GraphArchive archive)
    {
    }

    protected void onUnloaded(final GraphArchive archive)
    {
    }

    protected void onUnloading(final GraphArchive archive)
    {
    }

    /**
     * Attach the given field archive to each graph element store
     */
    private void attach(final GraphArchive archive)
    {
        ensure(archive != null);
        this.archive = archive;
        onAttaching(archive);
        forEachStore((store) -> store.attach(archive));
        onAttached(archive);
    }

    private void loaded(final GraphArchive archive)
    {
        forEachStore((store) -> store.loaded(archive));
        onLoaded(archive);
        loading = false;

        // Save the data source we loaded from
        resource(archive.zip().resource());
    }

    private void loading(final GraphArchive archive)
    {
        loading = true;
        onLoading(archive);
        forEachStore((store) -> store.loading(archive));
    }

    private void saved(final GraphArchive archive)
    {
        forEachStore((store) -> store.saved(archive));
        onSaved(archive);
    }

    private void saving(final GraphArchive archive)
    {
        onSaving(archive);
        forEachStore((store) -> store.saving(archive));
    }
}
