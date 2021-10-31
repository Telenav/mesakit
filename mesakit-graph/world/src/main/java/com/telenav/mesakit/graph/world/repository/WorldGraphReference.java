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

package com.telenav.mesakit.graph.world.repository;

import com.telenav.kivakit.configuration.settings.deployment.Deployment;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.collections.map.count.CountMap;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.objects.Objects;
import com.telenav.kivakit.kernel.language.strings.Strip;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.path.FilePath;
import com.telenav.mesakit.graph.world.WorldGraph;
import com.telenav.mesakit.graph.world.WorldGraphDeployments;
import com.telenav.mesakit.graph.world.grid.WorldCellReference;
import com.telenav.mesakit.map.utilities.grid.GridCell;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

/**
 * A logical reference that lazy loads a {@link WorldGraph} using descriptors passed to the constructor when {@link
 * #get()} is called. A {@link WorldGraphReference} ensures that the graph is never loaded twice and it can be
 * serialized without serializing the entire {@link WorldGraph} referent with it. A {@link WorldGraphReference} is
 * logical in the sense that the reference object isn't important but rather the set of descriptors. Two different
 * {@link WorldGraphReference} objects can be created with the same descriptors and they each represent a single
 * reference.
 * <p>
 * <b>Reference Count</b>
 * <p>
 * The reference count of this reference can be increased with {@link #increment()} or decreased with {@link
 * #release()}. The reference count is not affected by calls to get the referent with {@link #get()}. When a logical
 * reference created for the first time (there have been no other references to the given world graph), the reference
 * count is automatically incremented. When it is no longer in use, it can be released with {@link #release()}. If the
 * reference count reaches zero, the {@link WorldGraph} is no longer referenced by the underlying cache in this class
 * and it becomes eligible for garbage collection. In cases where an application is only working with a single world
 * graph, the reference count can often be ignored.
 *
 * @author jonathanl (shibo)
 * @see Deployment
 * @see WorldGraph
 * @see WorldCellReference
 */
public class WorldGraphReference implements Source<WorldGraph>, Serializable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /** Cache of loaded world graphs for each world graph reference */
    private static final Map<WorldGraphReference, WorldGraph> worldGraphCache = new HashMap<>();

    /** The number of world graph references to a given world graph */
    private static final CountMap<WorldGraphReference> referenceCount = new CountMap<>();

    /** The world graph being referenced */
    private transient WorldGraph graph;

    /** The repository folder where the world graph resides */
    private transient WorldGraphRepositoryFolder folder;

    /** The deployment configuration specifying the local repository location */
    private final Deployment deployment;

    private final String serializedWorldGraphRepository;

    private String serializedPath;

    public WorldGraphReference(Deployment deployment, WorldGraphRepositoryFolder folder)
    {
        this.deployment = deployment;
        this.folder = folder;

        serializedWorldGraphRepository = folder.repository().path().toString();
        serializedPath = folder.path().toString().substring(serializedWorldGraphRepository.length());
        serializedPath = Strip.ending(serializedPath, ".world");

        ensure(folder.isLocal());

        synchronized (WorldGraphReference.class)
        {
            if (!referenceCount.contains(this))
            {
                increment();
            }
        }
    }

    public WorldGraphReference(Deployment deployment, WorldGraph graph)
    {
        this(deployment, graph.worldGrid().repositoryFolder());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof WorldGraphReference)
        {
            var that = (WorldGraphReference) object;
            return Objects.equalPairs(folder(), that.folder(), deployment, that.deployment);
        }
        return false;
    }

    /**
     * @return Loads the world graph referred to by this reference (if it is not already loaded) and returns it
     */
    @Override
    public WorldGraph get()
    {
        synchronized (WorldGraphReference.class)
        {
            // If there's no graph in the transient field,
            if (graph == null)
            {
                // check the cache to see if some other thread loaded this graph already,
                graph = worldGraphCache.get(this);

                // and if nobody has loaded our graph yet,
                if (graph == null)
                {
                    // install the deployment in case it wasn't installed yet,
                    new WorldGraphDeployments(LOGGER).deployment(deployment.name()).install();

                    // create the world graph,
                    graph = LOGGER.listenTo(WorldGraph.load(folder()));

                    // and save it in the cache
                    worldGraphCache.put(this, graph);
                }
            }
            return graph;
        }
    }

    @Override
    public int hashCode()
    {
        return Hash.many(folder(), deployment);
    }

    /**
     * Increments the reference count for this logical reference
     */
    public void increment()
    {
        synchronized (WorldGraphReference.class)
        {
            referenceCount.increment(this);
        }
    }

    /**
     * Decrements this reference, possibly allowing garbage collection of the world graph referent
     */
    public void release()
    {
        synchronized (WorldGraphReference.class)
        {
            referenceCount.decrement(this);
            if (referenceCount.count(this).isZero())
            {
                worldGraphCache.remove(this);
            }
        }
    }

    /**
     * @return A reference to a cell within the world graph referred to by this reference object
     */
    public WorldCellReference worldCellSource(GridCell gridCell)
    {
        return new WorldCellReference(this, gridCell);
    }

    private WorldGraphRepositoryFolder folder()
    {
        if (folder == null)
        {
            var folder = Folder.parse(LOGGER, serializedWorldGraphRepository);
            if (folder != null)
            {
                var repository = new WorldGraphRepository(folder);
                this.folder = new WorldGraphRepositoryFolder(repository, FilePath.parseFilePath(LOGGER, serializedPath));
            }
        }
        return folder;
    }
}
