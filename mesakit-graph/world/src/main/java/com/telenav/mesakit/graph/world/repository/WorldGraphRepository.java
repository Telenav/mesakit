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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.filesystem.spi.FileSystemService;
import com.telenav.kivakit.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.kivakit.resource.path.FilePath;
import com.telenav.mesakit.graph.Metadata;

import java.io.Serializable;

import static com.telenav.kivakit.ensure.Ensure.unsupported;

/**
 * A {@link Folder} on some {@link FileSystemService} containing {@link WorldGraphRepositoryFolder}s. Since the {@link
 * Folder} base class {@link Folder} is virtualized and supports pluggable filesystems, a world graph folder may be
 * local, or remote on an HDFS or S3 filesystem. A {@link SwitchParser} is available to choose a world graph repository
 * from the command line. For an example, see the PbfWorldGraphExtractorApplication in mesakit-tools/applications.
 * <p>
 * There is a local repository in ~/.mesakit which is used to store local world graphs and also to cache remote graphs
 * on the local machine so they will load faster.
 * <p>
 * The method {@link #folder(FilePath, Metadata)} returns a folder at the given path within the repository having the
 * name derived from the given metadata. To specify a folder at the root of the repository, a file path of "/" may be
 * used. When creating new world graphs, the {@link #temporaryFolder(FilePath)} method is used to contain the data as it
 * is being written by the PbfWorldGraphExtractorApplication. If the extraction succeeds, the folder is renamed to a
 * folder name derived from the metadata for the extracted data. If it fails, the folder remains a temporary folder.
 * This helps to ensure that no correctly named {@link WorldGraphRepositoryFolder} contains invalid data.
 *
 * @author jonathanl (shibo)
 * @see Folder
 * @see Metadata
 * @see FilePath
 * @see FileName
 */
public class WorldGraphRepository extends Folder implements Serializable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<WorldGraphRepository> worldGraphRepositorySwitchParser(String description)
    {
        return SwitchParser.builder(WorldGraphRepository.class).name("world-graph-repository")
                .converter(new Converter(LOGGER)).description(description);
    }

    public static class Converter extends BaseStringConverter<WorldGraphRepository>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected WorldGraphRepository onToValue(String value)
        {
            if (value == null || "none".equals(value))
            {
                return null;
            }
            var folder = Folder.parse(this, value);
            return folder == null ? null : new WorldGraphRepository(folder);
        }
    }

    /**
     * For serialization
     */
    public WorldGraphRepository()
    {
    }

    /**
     * Construct
     */
    public WorldGraphRepository(Folder folder)
    {
        super(folder.path());
    }

    @Override
    public WorldGraphRepositoryFolder folder(FileName child)
    {
        return unsupported();
    }

    @Override
    public WorldGraphRepositoryFolder folder(Folder child)
    {
        return unsupported();
    }

    /**
     * @return The world graph folder for the given sub-folder in this repository with the name derived from the given
     * metadata
     */
    public WorldGraphRepositoryFolder folder(FilePath subfolder, Metadata metadata)
    {
        return new WorldGraphRepositoryFolder(this, subfolder, metadata);
    }

    public WorldGraphRepositoryFolder folder(Metadata metadata)
    {
        return new WorldGraphRepositoryFolder(this, null, metadata);
    }

    public WorldGraphRepositoryFolder temporaryFolder()
    {
        return temporaryFolder(null);
    }

    /**
     * @return A temporary grid folder to extract new data to
     */
    public WorldGraphRepositoryFolder temporaryFolder(FilePath subfolder)
    {
        int i = 1;
        while (true)
        {
            WorldGraphRepositoryFolder folder = temporary(subfolder, i++);
            if (!folder.exists())
            {
                folder.mkdirs();
                return folder;
            }
        }
    }

    private WorldGraphRepositoryFolder temporary(FilePath subfolder, int i)
    {
        var name = WorldGraphRepositoryFolder.TEMPORARY_FOLDER_NAME.withSuffix("-" + i);
        if (subfolder != null)
        {
            return new WorldGraphRepositoryFolder(this, subfolder.file(name));
        }
        else
        {
            return new WorldGraphRepositoryFolder(this, FilePath.filePath(name));
        }
    }
}
