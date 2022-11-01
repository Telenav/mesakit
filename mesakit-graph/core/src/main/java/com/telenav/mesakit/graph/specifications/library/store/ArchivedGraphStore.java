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

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.reflection.Type;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.interfaces.loading.Unloadable;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeSet;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.string.AsciiArt.textBox;
import static com.telenav.kivakit.core.string.Formatter.format;

@SuppressWarnings("unused") public abstract class ArchivedGraphStore extends GraphStore
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
    protected ArchivedGraphStore(Graph graph)
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
        for (var store : stores())
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
    public final void load(GraphArchive archive)
    {
        // Check that we haven't already loaded into this graph store
        ensure(isEmpty(), "Cannot load data into an existing, loaded graph");

        // Record start time
        var start = Time.now();

        // We're about to load
        loading(archive);

        // Attach archive to all the sub-stores
        attach(archive);

        // Clear out all the lazy-loaded fields we will load from archive
        unload();

        // Attach the graph archive and the field archive based on it.
        // We can't register the spatial index serializer in GraphCore because
        // it needs the graph in order to function, so we do it here.

        // Load metadata
        var metadata = graph().metadata(archive.metadata());
        bounds(metadata.dataBounds());

        // We're done loading
        loaded(archive);

        // Done!
        graph().information(textBox(format("Loaded from $ in $",
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
    public void loadAll(AttributeSet except)
    {
        for (var store : stores())
        {
            store.loadAll(except);
        }
    }

    /**
     * Force each graph element store to fully load, except for the given attributes
     */
    public void loadAllExcept(AttributeSet except)
    {
        for (var store : stores())
        {
            store.loadAllExcept(except);
        }
    }

    public void loaded(Resource resource)
    {
        // Save the data source we loaded,
        resource(resource);

        // then notify each sub-store and our subclass that we're loaded.
        forEachStore((store) -> store.loaded(resource));
        onLoaded(resource);
    }

    public void loading(Resource resource)
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

    public void resource(Resource resource)
    {
        this.resource = resource;
    }

    /**
     * Archives this graph store's data for future use via {@link #load(GraphArchive)}.
     *
     * @param archive The .graph file to save to
     */
    public final void save(GraphArchive archive)
    {
        // If the store is invalid
        if (!isValid(this))
        {
            // we cannot save
            throw new IllegalStateException("Cannot save invalid graph to " + archive.zip().resource());
        }

        // Record start time
        var start = Time.now();

        try
        {
            // Create archive and save all non-null archived fields
            // We can't register the spatial index serializer in GraphCore because
            // it needs the graph in order to function, so we do it here.

            // Attach archive
            attach(archive);

            // We're starting to save
            saving(archive);

            // Save metadata with kryo
            var metadata = graph().metadata().withDataBounds(bounds());
            archive.saveMetadata(metadata);

            // Save fields of each graph element store
            forEachStore(ArchivedGraphElementStore::save);

            // Close the archive
            archive.close();

            // We're done saving
            saved(archive);
        }
        catch (Exception e)
        {
            problem(e, "Unable to save to $", archive);
        }

        // We're done!
        var report = new StringList();
        report.add("output: " + archive);
        report.add("elapsed: " + start.elapsedSince());
        report.add(graph().asString());
        information(textBox(format("Saved $", metadata().descriptor()), report.join("\n")));
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
                for (var object : Type.type(this).reachableObjects(this))
                {
                    if (object instanceof Unloadable)
                    {
                        ((Unloadable) object).unload();
                    }
                }

                onUnloaded(archive);
            }
            finally
            {
                unloading = false;
            }
        }
    }

    protected void onAttached(GraphArchive archive)
    {
    }

    protected void onAttaching(GraphArchive archive)
    {
    }

    protected void onLoaded(Resource resource)
    {
    }

    protected void onLoaded(GraphArchive archive)
    {
    }

    protected void onLoading(Resource resource)
    {
    }

    protected void onLoading(GraphArchive archive)
    {
    }

    protected void onSaved(GraphArchive archive)
    {
    }

    protected void onSaving(GraphArchive archive)
    {
    }

    protected void onUnloaded(GraphArchive archive)
    {
    }

    protected void onUnloading(GraphArchive archive)
    {
    }

    /**
     * Attach the given field archive to each graph element store
     */
    private void attach(GraphArchive archive)
    {
        ensure(archive != null);
        this.archive = archive;
        onAttaching(archive);
        forEachStore((store) -> store.attach(archive));
        onAttached(archive);
    }

    private void loaded(GraphArchive archive)
    {
        forEachStore((store) -> store.loaded(archive));
        onLoaded(archive);
        loading = false;

        // Save the data source we loaded from
        resource(archive.zip().resource());
    }

    private void loading(GraphArchive archive)
    {
        loading = true;
        onLoading(archive);
        forEachStore((store) -> store.loading(archive));
    }

    private void saved(GraphArchive archive)
    {
        forEachStore((store) -> store.saved(archive));
        onSaved(archive);
    }

    private void saving(GraphArchive archive)
    {
        onSaving(archive);
        forEachStore((store) -> store.saving(archive));
    }
}
