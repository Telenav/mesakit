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

package com.telenav.mesakit.graph.world;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.language.reflection.populator.KivaKitPropertyConverter;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.string.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.operation.progress.reporters.Progress;
import com.telenav.mesakit.map.measurements.Angle;
import com.telenav.kivakit.resource.path.FilePath;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepository;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;

/**
 * The world graph configuration (loaded from the deployment configuration), specifying the cell size and local
 * repository. A remote repository may also be specified.
 *
 * @author jonathanl (shibo)
 * @see WorldGraphDeployments
 */
public final class WorldGraphConfiguration
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @KivaKitIncludeProperty
    private Angle cellSize;

    @KivaKitIncludeProperty
    private WorldGraphRepository localRepository;

    @KivaKitIncludeProperty
    private WorldGraphRepository remoteRepository;

    public Angle cellSize()
    {
        return cellSize;
    }

    @KivaKitPropertyConverter(Angle.Converter.class)
    public void cellSize(final Angle size)
    {
        cellSize = size;
    }

    public WorldGraphRepository localRepository()
    {
        return localRepository;
    }

    @KivaKitPropertyConverter(Folder.Converter.class)
    public void localRepository(final Folder local)
    {
        localRepository = new WorldGraphRepository(local);
    }

    public WorldGraphRepositoryFolder materializedGraphFolder(final Metadata metadata)
    {
        // Get the local and remote repositories
        final var local = localRepository();
        final var remote = remoteRepository();

        // and if there is a remote repository,
        if (remote != null)
        {
            // copy any out-of-date files in the specified graph metadata to the local repository
            final var progress = Progress.create(LOGGER, "bytes");
            final var remoteGraph = remoteRepository.folder(metadata);
            final var localGraph = localRepository.folder(metadata);
            remoteGraph.copyTo(localGraph, progress);
        }

        return local.folder(FilePath.parse("/"), metadata);
    }

    public WorldGraphRepository materializedRepository()
    {
        // Get the local and remote repositories
        final var local = localRepository();
        final var remote = remoteRepository();

        // and if there is a remote repository,
        if (remote != null)
        {
            // copy any out-of-date files to the local repository
            final var progress = Progress.create(LOGGER, "bytes");
            remote.copyTo(local, progress);
        }

        return local;
    }

    @KivaKitPropertyConverter(Folder.Converter.class)
    public void remoteRepository(final Folder remote)
    {
        remoteRepository = new WorldGraphRepository(remote);
    }

    public WorldGraphRepository remoteRepository()
    {
        return remoteRepository;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }
}
