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

import com.telenav.kivakit.core.kernel.language.objects.Hash;
import com.telenav.kivakit.core.resource.path.FileName;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.mesakit.map.ui.desktop.theme.MapStyles.GRID_LABEL;

/**
 * A "slippy tile" is a rectangle defined by a zoom level and x, y coordinates in the grid of all tiles. You can get the
 * bounds of a particular slippy tile like this:
 *
 * <pre>
 * var bounds = new SlippyTile()
 *     .withX(300)
 *     .withY(40)
 *     .withZoomLevel(ZoomLevel.forInteger(5))
 *     .bounds()
 * </pre>
 *
 * <p>
 * You can also find the slippy tile for a location like this:
 * </p>
 *
 * <pre>
 * var tile = new SlippyTile(CheckType.OSM, zoom, location)
 * </pre>
 *
 * @author jonathanl (shibo)
 * @see "http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames"
 */
public class SlippyTile
{
    /**
     * Width and height of standard slippy tiles
     */
    public static final Size STANDARD_TILE_SIZE = Size.pixels(256, 256);

    /**
     * @return The smallest tile that is larger than the given size
     */
    public static SlippyTile largerThan(final com.telenav.mesakit.map.geography.shape.rectangle.Size size)
    {
        var tile = ZoomLevel.CLOSEST.tileAt(Location.ORIGIN);
        while (!tile.size().isGreaterThan(size) && !tile.getZoomLevel().isFurthestOut())
        {
            tile = tile.parent();
        }
        return tile;
    }

    public static SlippyTile tile()
    {
        return new SlippyTile(null, 0, 0);
    }

    private int x;

    private int y;

    private ZoomLevel zoom;

    protected SlippyTile(final ZoomLevel zoom, final int x, final int y)
    {
        this.zoom = zoom;

        ensure(x >= 0 && x < zoom.widthInTiles());
        ensure(y >= 0 && y < zoom.heightInTiles());

        this.x = x;
        this.y = y;
    }

    private SlippyTile(final SlippyTile that)
    {
        x = that.x;
        y = that.y;
        zoom = that.zoom;
    }

    public FileName asFileName()
    {
        return FileName.parse(x + "-" + y + "-" + zoom.level() + ".png");
    }

    /**
     * Draws the outline of this slippy tile on the given canvas in the given style clipped to the given bounds
     */
    public void drawOutline(final MapCanvas canvas, final Style style)
    {
        // Get the drawing area for this tile in slippy tile coordinates
        final var drawingArea = drawingArea();

        // then draw lines
        canvas.drawBox(style, drawingArea);

        // and draw label for tile rectangle
        final var at = drawingArea.topLeft().plus(8, 8);
        final var visible = canvas.drawingArea().contains(at);
        if (visible)
        {
            Label.label()
                    .at(at)
                    .withStyle(GRID_LABEL)
                    .withMargin(4)
                    .withText(toString())
                    .withRoundedCorners(Length.pixels(8))
                    .draw(canvas);
        }
    }

    public Rectangle drawingArea()
    {
        return new SlippyTileCoordinateSystem(zoom).drawingArea(this);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof SlippyTile)
        {
            final var that = (SlippyTile) object;
            return x == that.x && y == that.y && zoom.equals(that.zoom);
        }
        return false;
    }

    public ZoomLevel getZoomLevel()
    {
        return zoom;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(x, y, zoom);
    }

    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle mapArea()
    {
        return new SlippyTileCoordinateSystem(zoom).mapArea(this);
    }

    public SlippyTile parent()
    {
        return getZoomLevel().zoomOut().tileAt(mapArea().center());
    }

    public com.telenav.mesakit.map.geography.shape.rectangle.Size size()
    {
        return mapArea().size();
    }

    public Size tileSize()
    {
        return STANDARD_TILE_SIZE;
    }

    public List<SlippyTile> tilesInside()
    {
        final List<SlippyTile> tiles = new ArrayList<>();
        final var zoomedIn = getZoomLevel().zoomIn();
        final var x = this.x * 2;
        final var y = this.y * 2;
        tiles.add(new SlippyTile(zoomedIn, x, y));
        tiles.add(new SlippyTile(zoomedIn, x + 1, y));
        tiles.add(new SlippyTile(zoomedIn, x, y + 1));
        tiles.add(new SlippyTile(zoomedIn, x + 1, y + 1));
        return tiles;
    }

    @Override
    public String toString()
    {
        return "x = " + x + ", y = " + y + ", z = " + zoom.level();
    }

    public SlippyTile withX(final int x)
    {
        final var tile = new SlippyTile(this);
        tile.x = x;
        return tile;
    }

    public SlippyTile withY(final int y)
    {
        final var tile = new SlippyTile(this);
        tile.y = y;
        return tile;
    }

    public SlippyTile withZoomLevel(final ZoomLevel zoom)
    {
        final var tile = new SlippyTile(this);
        tile.zoom = zoom;
        return tile;
    }

    public int x()
    {
        return x;
    }

    public int y()
    {
        return y;
    }
}