////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.ui.swing.map.tiles;

import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.filesystem.FolderPruner;
import com.telenav.kivakit.core.kernel.language.time.Duration;
import com.telenav.kivakit.core.kernel.language.time.Frequency;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.kivakit.core.resource.Resource;
import com.telenav.kivakit.core.resource.path.Extension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A persistent cache of tile images.
 *
 * @author jonathanl (shibo)
 */
public class SlippyTileCacheFolder
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * The folder that holds tile images
     */
    private final Folder folder;

    /**
     * @param folder The cache folder
     */
    public SlippyTileCacheFolder(final Folder folder)
    {
        // Save folder
        this.folder = folder;

        // Ensure folder exists
        folder.mkdirs();
        if (!folder.exists())
        {
            LOGGER.warning("Unable to create $", folder);
        }

        // Start folder pruner
        final var pruner = new FolderPruner(this.folder, Frequency.EVERY_30_SECONDS);
        pruner.minimumUsableDiskSpace(Percent.of(10));
        pruner.minimumAge(Duration.days(30));
        pruner.start();

        LOGGER.information("Started slippy tile cache folder in $", folder);
    }

    /**
     * Add the given tile image to this cache
     */
    public void add(final SlippyTile tile, final BufferedImage image)
    {
        try
        {
            if (DEBUG.isDebugOn())
            {
                LOGGER.information("Adding tile $", tile);
            }
            ImageIO.write(image, "png", file(tile).asJavaFile());
        }
        catch (final IOException e)
        {
            LOGGER.warning("Unable to save $ to disk image cache", tile);
        }
    }

    /**
     * @return Input stream to read the given tile if it exists in this cache or null if it doesn't
     */
    public Resource resource(final SlippyTile tile)
    {
        final var file = file(tile);
        if (file.exists())
        {
            return file;
        }
        return null;
    }

    /**
     * @return The file for the tile
     */
    private File file(final SlippyTile tile)
    {
        return folder.file(tile.asFileName().withExtension(Extension.PNG));
    }
}
