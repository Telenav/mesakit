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

import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.swing.map.coordinates.mappers.CoordinateMapper;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;

import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

/**
 * A set of {@link SlippyTile}s that covers a given rectangular area.
 *
 * @author jonathanl (shibo)
 */
public class SlippyTileGrid extends BaseRepeater implements Iterable<SlippyTile>
{
    private final Rectangle bounds;

    private final ZoomLevel zoom;

    private final Set<SlippyTile> tiles = new HashSet<>();

    /**
     * @param bounds The bounds that this grid of slippy tiles should cover
     */
    public SlippyTileGrid(final Rectangle bounds)
    {
        this(bounds, SlippyTile.largerThan(bounds.size()).getZoomLevel());
    }

    /**
     * @param bounds The bounds for this slippy tile grid
     * @param zoom The zoom level of tiles
     */
    public SlippyTileGrid(final Rectangle bounds, final ZoomLevel zoom)
    {
        // Save bounds and zoom level
        this.bounds = bounds;
        this.zoom = zoom;
        if (isDebugOn())
        {
            information("bounds = " + bounds);
            information("zoom = " + zoom);
        }

        // Get the top left and bottom right slippy tiles
        final var topLeft = zoom.tileAt(bounds.topLeft());
        final var bottomRight = zoom.tileAt(bounds.bottomRight());

        // Iterate through the tile coordinates, adding one for each tile in the grid
        final var startx = topLeft.getX();
        final var starty = topLeft.getY();
        final var endx = bottomRight.getX();
        final var endy = bottomRight.getY();
        if (isDebugOn())
        {
            information("startx = " + startx + ", starty = " + starty + ", endx = " + endx + ", endy = " + endy);
        }

        // Compute the total number of tiles in the grid
        final var total = (endx - startx + 1) * (endy - starty + 1);
        if (isDebugOn())
        {
            information("total = " + total);
        }
        if (total < 0)
        {
            fail("Unable to construct tile grid");
        }

        // and if it's quite unreasonable (greater than 20x20 where 5000 pixel screen width / 256
        // pixels ~= 20 tiles in each direction)
        if (total > 400)
        {
            fail("Bounds is too big for zoom level");
        }

        // Create slippy tiles for the grid
        for (var y = starty; y <= endy; y++)
        {
            for (var x = startx; x <= endx; x++)
            {
                tiles.add(new SlippyTile(zoom, x, y));
            }
        }
    }

    /**
     * @return True if this grid contains the given tile
     */
    public boolean contains(final SlippyTile tile)
    {
        return tiles.contains(tile);
    }

    /**
     * Draws outlines of all the tiles in this grid
     *
     * @param graphics The graphics to draw on
     * @param mapper The coordinate mapper
     */
    public void drawOutlines(final Graphics2D graphics, final CoordinateMapper mapper)
    {
        for (final var tile : tiles)
        {
            tile.drawOutline(graphics, bounds, mapper);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<SlippyTile> iterator()
    {
        return tiles.iterator();
    }

    /**
     * @return The number of tiles in this grid
     */
    public int size()
    {
        return tiles.size();
    }

    /**
     * @return The zoom level of tiles in this grid
     */
    public ZoomLevel zoomLevel()
    {
        return zoom;
    }
}