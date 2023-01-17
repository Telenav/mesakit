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

import com.telenav.kivakit.conversion.core.language.object.ConvertedProperty;
import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.language.reflection.property.IncludeProperty;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.filesystem.FolderConverter;
import com.telenav.kivakit.resource.WriteMode;
import com.telenav.kivakit.filesystem.FilePath;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepository;
import com.telenav.mesakit.graph.world.repository.WorldGraphRepositoryFolder;
import com.telenav.mesakit.map.measurements.geographic.Angle;

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

    @IncludeProperty
    private Angle cellSize;

    @IncludeProperty
    private WorldGraphRepository localRepository;

    @IncludeProperty
    private WorldGraphRepository remoteRepository;

    public Angle cellSize()
    {
        return cellSize;
    }

    @ConvertedProperty(Angle.Converter.class)
    public void cellSize(Angle size)
    {
        cellSize = size;
    }

    public WorldGraphRepository localRepository()
    {
        return localRepository;
    }

    @ConvertedProperty(FolderConverter.class)
    public void localRepository(Folder local)
    {
        localRepository = new WorldGraphRepository(local);
    }

    public WorldGraphRepositoryFolder materializedGraphFolder(Metadata metadata)
    {
        // Get the local and remote repositories
        var local = localRepository();
        var remote = remoteRepository();

        // and if there is a remote repository,
        if (remote != null)
        {
            // copy any out-of-date files in the specified graph metadata to the local repository
            var progress = BroadcastingProgressReporter.progressReporter(LOGGER, "bytes");
            var remoteGraph = remoteRepository.folder(metadata);
            var localGraph = localRepository.folder(metadata);
            remoteGraph.copyTo(localGraph, WriteMode.UPDATE, progress);
        }

        return local.folder(FilePath.parseFilePath(LOGGER, "/"), metadata);
    }

    public WorldGraphRepository materializedRepository()
    {
        // Get the local and remote repositories
        var local = localRepository();
        var remote = remoteRepository();

        // and if there is a remote repository,
        if (remote != null)
        {
            // copy any out-of-date files to the local repository
            var progress = BroadcastingProgressReporter.progressReporter(LOGGER, "bytes");
            remote.copyTo(local, WriteMode.UPDATE, progress);
        }

        return local;
    }

    @ConvertedProperty(FolderConverter.class)
    public void remoteRepository(Folder remote)
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
