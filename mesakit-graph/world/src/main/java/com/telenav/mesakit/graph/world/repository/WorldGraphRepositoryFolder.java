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
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.ensure.Ensure;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.Message;
import com.telenav.kivakit.core.messaging.messages.status.Problem;
import com.telenav.kivakit.core.messaging.messages.status.activity.StepSuccess;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.FilePath;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.resource.Extension;
import com.telenav.kivakit.resource.FileName;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.world.WorldGraphIndex;

import java.io.Serializable;

/**
 * A {@link WorldGraphRepositoryFolder} is a folder within a {@link WorldGraphRepository} that has a name based on the
 * {@link Metadata} of the world graph information that it contains. The repository containing this folder can be
 * retrieved with {@link #repository()} or {@link #parent()} and the {@link WorldGraphIndex} within the folder with
 * {@link #indexFile()}. A folder can be checked to determine if it is a valid world graph folder with
 * {@link #check(Folder, Check)}.
 *
 * @author jonathanl (shibo)
 * @see WorldGraphIndex
 * @see Folder
 * @see File
 * @see Metadata
 */
@SuppressWarnings("unused")
public class WorldGraphRepositoryFolder extends Folder implements Serializable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /**
     * The name of the temporary extraction folder
     */
    public static final FileName TEMPORARY_FOLDER_NAME = FileName.parseFileName(LOGGER, "temporary");

    /** A switch parser to select an existing world graph folder */
    public static SwitchParser.Builder<WorldGraphRepositoryFolder> SWITCH_PARSER_EXISTING =
            SwitchParser.switchParserBuilder(WorldGraphRepositoryFolder.class)
                    .name("world-graph-folder")
                    .converter(new Converter(LOGGER, Check.EXISTS))
                    .description("Full path to world graph data folder");

    /** A switch parser to select a non-existing world graph folder */
    public static SwitchParser.Builder<WorldGraphRepositoryFolder> SWITCH_PARSER_NON_EXISTING =
            SwitchParser.switchParserBuilder(WorldGraphRepositoryFolder.class)
                    .name("world-graph-folder")
                    .converter(new Converter(LOGGER, Check.IS_VALID))
                    .description("Full path to world graph data folder");

    /** The extension for world graph folders */
    public static final Extension WORLD = Extension.parseExtension(LOGGER, ".world");

    /**
     * @return A {@link Problem} message detailing the problem if the given folder doesn't exist or is not of the
     * correct form, otherwise {@link StepSuccess}.
     */
    public static Message check(Folder folder, Check check)
    {
        if (!folder.name().startsWith("temporary"))
        {
            if (check == Check.EXISTS)
            {
                if (!folder.exists())
                {
                    return new Problem("World graph $ does not exist", folder);
                }
                if (!folder.file("index").withExtension(WORLD).exists())
                {
                    return new Problem("$ is not valid world graph (index.world is missing)", folder);
                }
            }
            if (!folder.name().endsWith(WORLD))
            {
                return new Problem("World graph $ does not end in .world", folder);
            }
        }
        return StepSuccess.INSTANCE;
    }

    /**
     * The type of checking to do in {@link #check(Folder, Check)}. To write to a graph folder, the folder only needs to
     * be valid. To read from it, the folder also has to exist.
     */
    public enum Check
    {
        /** The folder exists */
        EXISTS,

        /** The folder exists and contains a world graph index */
        IS_VALID
    }

    public static class Converter extends BaseStringConverter<WorldGraphRepositoryFolder>
    {
        private final Check existence;

        public Converter(Listener listener, Check existence)
        {
            super(listener);
            this.existence = existence;
        }

        @Override
        protected WorldGraphRepositoryFolder onToValue(String value)
        {
            var folder = Folder.parseFolder(this, value);
            if (folder != null)
            {
                var message = check(folder, existence);
                if (message.status().failed())
                {
                    transmit(message);
                    return null;
                }
                var repository = new WorldGraphRepository(folder.parent());
                return repository.folder(folder.name());
            }
            return null;
        }
    }

    /** The parent repository */
    private final WorldGraphRepository repository;

    /**
     * @param repository The repository where this folder resides
     * @param path The path within the repository to the the graph folder. This path may be "/" if the graph folder is
     * at the root of the repository and not in any sub-folder.
     * @param metadata The metadata of the graph used to construct the folder name
     */
    WorldGraphRepositoryFolder(WorldGraphRepository repository, FilePath path, Metadata metadata)
    {
        this(repository, path == null ? FilePath.filePath(metadata.asFileName()) : path.withChild(metadata.asFileName().toString()));
    }

    WorldGraphRepositoryFolder(WorldGraphRepository repository, FilePath path)
    {
        super(repository.path().withChild(path.withoutRoot().withExtension(WORLD).toString()));
        this.repository = repository;

        // Validate the form of the folder (it does not have to exist)
        var message = check(this, Check.IS_VALID);
        if (message.status().failed())
        {
            throw message.asException();
        }
    }

    /**
     * Ensures that the given {@link Check} passes
     */
    public void ensure(Check check)
    {
        Ensure.ensure(!check(this, check).status().failed(), "No valid local data in $", this);
    }

    /**
     * The file in this folder containing the {@link WorldGraphIndex}
     */
    public File indexFile()
    {
        return file("index").withExtension(WORLD);
    }

    /**
     * @return True if this is a temporary folder (during extraction of a new world graph)
     */
    public boolean isTemporary()
    {
        return name().startsWith(TEMPORARY_FOLDER_NAME.name());
    }

    @Override
    public WorldGraphRepository parent()
    {
        return repository;
    }

    /**
     * @return The repository where this world graph folder resides
     */
    public WorldGraphRepository repository()
    {
        return repository;
    }
}
