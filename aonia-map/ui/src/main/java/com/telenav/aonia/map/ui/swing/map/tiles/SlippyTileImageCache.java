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

package com.telenav.aonia.map.ui.swing.map.tiles;

import com.telenav.aonia.map.ui.swing.map.coordinates.mappers.CoordinateMapper;
import com.telenav.kivakit.core.collections.map.CacheMap;
import com.telenav.kivakit.core.collections.set.ConcurrentHashSet;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.language.io.IO;
import com.telenav.kivakit.core.kernel.language.threading.RepeatingKivaKitThread;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.vm.JavaVirtualMachine;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Cache of {@link SlippyTile} images
 *
 * @author jonathanl (shibo)
 */
public abstract class SlippyTileImageCache extends BaseRepeater
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Map from slippy tile to buffered image
     */
    private final Map<SlippyTile, BufferedImage> imageForTile;

    /**
     * Queue of requested tiles
     */
    private final BlockingQueue<SlippyTile> requested;

    /**
     * The current tile being requested
     */
    private final Set<SlippyTile> downloading = new ConcurrentHashSet<>();

    /**
     * The disk cache folder
     */
    private final SlippyTileCacheFolder cacheFolder = new SlippyTileCacheFolder(
            Folder.kivakitTemporaryFolder().folder("slippy-tile-cache"));

    /**
     * @param maximumTiles The maximum number of images to hold in the cache
     */
    protected SlippyTileImageCache(final Maximum maximumTiles)
    {
        imageForTile = Collections.synchronizedMap(new CacheMap<>(maximumTiles));
        requested = new ArrayBlockingQueue<>(maximumTiles.asInt());

        // Request tiles
        for (var i = 0; i < JavaVirtualMachine.local().processors().asInt(); i++)
        {
            RepeatingKivaKitThread.run(LOGGER, "SlippyTile-" + i, this::downloadNext);
        }
    }

    /**
     * @return The size of a slippy tile in pixels, as determined by fetching a tile from the server
     */
    @SuppressWarnings("SameReturnValue")
    public abstract Dimension dimension();

    /**
     * Draw map tiles on the given graphics
     */
    public void drawTiles(final Graphics2D graphics, final CoordinateMapper mapper, final Iterable<SlippyTile> tiles)
    {
        // Go through each tile
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        for (final var tile : tiles)
        {
            // get any available image for the tile
            final var image = image(tile);

            // and if one is available,
            if (image != null)
            {
                // draw it at the appropriate x, y location using the coordinate mapper
                final var bounds = mapper.toAwt(tile.bounds());
                if (DEBUG.isDebugOn())
                {
                    LOGGER.information("drawing tile $ with bounds of $ at $, $", tile,
                            tile.bounds(), bounds.x, bounds.y);
                }
                graphics.drawImage(image, bounds.x, bounds.y, null);
            }
        }
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * @return The HTTP network location of the given slippy tile
     */
    protected abstract HttpNetworkLocation networkLocation(final SlippyTile tile);

    /**
     * Called when a download finishes and an image is added to the cache
     */
    protected abstract void onCacheUpdated();

    /**
     * @return The image for the given tile
     */
    private BufferedImage download(final SlippyTile tile)
    {
        // If we can't find the file in the cache folder,
        var resource = cacheFolder.resource(tile);
        if (resource == null)
        {
            // then read it from the network
            resource = networkLocation(tile).get();
        }
        if (resource != null)
        {
            trace("Reading tile from $", resource);
            final var in = resource.openForReading();
            try
            {
                // Read the image
                final var image = ImageIO.read(in);

                // and if it read okay,
                if (image != null)
                {
                    // add it to the cache folder
                    cacheFolder.add(tile, image);
                }
                else
                {
                    // otherwise warn that we didn't read it okay
                    LOGGER.warning("Unable to read image for $ from $", tile, resource);
                }
                return image;
            }
            catch (final IOException e)
            {
                LOGGER.warning("Unable to download $", downloading);
            }
            finally
            {
                IO.close(in);
            }
        }
        return null;
    }

    /**
     * Download the next
     */
    private void downloadNext()
    {
        try
        {
            // Get the next request
            final var next = requested.take();
            downloading.add(next);

            // and download the image for it
            final var image = download(next);

            // and if we got the image,
            if (image != null)
            {
                // add it to the cache
                imageForTile.put(next, image);
                downloading.remove(next);
                onCacheUpdated();
            }
        }
        catch (final InterruptedException ignored)
        {
        }
    }

    /**
     * @return The current buffered image for the given tile from the cache. If the image is not available it is
     * requested so it will be available in the future.
     */
    private BufferedImage image(final SlippyTile tile)
    {
        final var image = imageForTile.get(tile);
        if (image == null)
        {
            if (!isRequested(tile))
            {
                try
                {
                    requested.put(tile);
                }
                catch (final InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return image;
    }

    /**
     * @return True if the given tile is being downloaded or is requested to be downloaded
     */
    private boolean isRequested(final SlippyTile tile)
    {
        return downloading.contains(tile) || requested.contains(tile);
    }
}
