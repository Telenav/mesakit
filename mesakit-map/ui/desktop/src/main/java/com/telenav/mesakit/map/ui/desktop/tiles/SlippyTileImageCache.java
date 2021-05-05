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

package com.telenav.mesakit.map.ui.desktop.tiles;

import com.telenav.kivakit.core.collections.map.CacheMap;
import com.telenav.kivakit.core.collections.set.ConcurrentHashSet;
import com.telenav.kivakit.core.kernel.language.threading.RepeatingKivaKitThread;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.core.MesaKit;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.telenav.kivakit.core.network.core.NetworkAccessConstraints.DEFAULT;
import static java.awt.AlphaComposite.SRC_OVER;

/**
 * Cache of {@link SlippyTile} images
 *
 * @author jonathanl (shibo)
 */
public abstract class SlippyTileImageCache extends BaseRepeater
{
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
    private final SlippyTileCache cache = listenTo(new SlippyTileCache(MesaKit.get().mesakitAllVersionsFolder().folder("tile-cache")));

    /**
     * @param maximumTiles The maximum number of images to hold in the cache
     */
    protected SlippyTileImageCache(final Maximum maximumTiles)
    {
        imageForTile = Collections.synchronizedMap(new CacheMap<>(maximumTiles));
        requested = new ArrayBlockingQueue<>(maximumTiles.asInt());

        // Request tiles (on a single thread only because of limits on OpenStreetMap tile servers)
        RepeatingKivaKitThread.run(this, "TileDownloader", this::downloadNext);
    }

    /**
     * Draw map tiles on the given graphics
     */
    public void drawTiles(final DrawingSurface surface,
                          final Iterable<SlippyTile> tiles)
    {
        // Go through each tile
        for (final var tile : tiles)
        {
            final var tileArea = tile.drawingArea();
            if (surface.bounds().intersects(tileArea))
            {
                // get any available image for the tile
                final var image = image(tile);

                // and if one is available,
                if (image != null)
                {
                    // draw it at the appropriate x, y location using the coordinate mapper
                    information("drawing tile $ with bounds of $", tile, tileArea);
                    surface.drawImage(tileArea.topLeft(), image, AlphaComposite.getInstance(SRC_OVER, 0.5f));
                }
            }
        }
    }

    /**
     * @return The size of a slippy tile in pixels, as determined by fetching a tile from the server
     */
    @SuppressWarnings("SameReturnValue")
    public abstract DrawingSize tileSize();

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
        var resource = cache.resource(tile);
        if (resource == null)
        {
            // then download the resource into the cache
            resource = cache.add(tile, networkLocation(tile).get(DEFAULT, get ->
            {
                get.setHeader("User-Agent", "MesaKit");
                get.setHeader("Referer", "http://www.mesakit.org");
            }));
        }

        // If we have a slippy-tile resource,
        if (resource != null)
        {
            // read and return it.
            try (final var in = resource.openForReading())
            {
                return ImageIO.read(in);
            }
            catch (final IOException e)
            {
                warning(e, "Unable to read image $", resource);
            }
        }

        warning("Unable to find slippy-tile: $", tile);
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
