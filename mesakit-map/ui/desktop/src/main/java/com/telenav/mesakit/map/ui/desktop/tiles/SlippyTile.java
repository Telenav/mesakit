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
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.kivakit.ui.desktop.theme.KivaKitColors;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;
import com.telenav.mesakit.map.ui.desktop.debug.viewer.swing.LabelRenderer;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static com.telenav.mesakit.map.ui.desktop.debug.viewer.swing.LabelRenderer.Position.BOTTOM_RIGHT;
import static java.awt.BasicStroke.CAP_BUTT;
import static java.awt.BasicStroke.JOIN_MITER;

/**
 * A "slippy tile" is a rectangle defined by a zoom level and x, y coordinates in the grid of all tiles. You can get the
 * bounds of a particular slippy tile like this:
 *
 * <pre>
 * Rectangle bounds = new SlippyTile(CheckType.TELENAV).setX(300).setY(40).setZoomLevel(ZoomLevel.forInteger(5))
 * 		.bounds()
 * </pre>
 * <p>
 * You can also find the slippy tile for a location like this:
 *
 * <pre>
 * SlippyTile tile = new SlippyTile(CheckType.OSM, zoom, location)
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
    public static final Dimension STANDARD_TILE_SIZE = new Dimension(256, 256);

    private static final Color COLOR = KivaKitColors.BLUE_RIDGE_MOUNTAINS.withAlpha(192);

    private static final Color GRID = KivaKitColors.STEEL_BLUE.withAlpha(192);

    /**
     * @return The smallest tile that is larger than the given size
     */
    public static SlippyTile largerThan(final Size size)
    {
        var tile = ZoomLevel.CLOSEST.tileAt(Location.ORIGIN);
        while (!tile.size().isGreaterThan(size) && !tile.getZoomLevel().isFurthestOut())
        {
            tile = tile.parent();
        }
        return tile;
    }

    private int x;

    private int y;

    private ZoomLevel zoom;

    public SlippyTile(final ZoomLevel zoom, final int x, final int y)
    {
        this.zoom = zoom;
        if (x < 0 || x >= zoom.widthInTiles())
        {
            throw new IllegalArgumentException("Invalid x = " + x);
        }
        if (y < 0 || y >= zoom.heightInTiles())
        {
            throw new IllegalArgumentException("Invalid y = " + y);
        }
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

    public Rectangle bounds()
    {
        return new SlippyTileCoordinateSystem(zoom).bounds(this);
    }

    @SuppressWarnings({ "SameReturnValue" })
    public Dimension dimension()
    {
        return STANDARD_TILE_SIZE;
    }

    /**
     * Draws the outline of this slippy tile on the given graphics with the given coordinate mapper and the give view
     * bounds
     */
    public void drawOutline(final Graphics2D graphics,
                            final MapCoordinateMapper mapper,
                            final Style style,
                            final Rectangle view)
    {
        // Draw tile grid rectangle
        final var rectangleStroke = new BasicStroke(1, CAP_BUTT, JOIN_MITER, 5.0f, new float[] { 5.0f }, 0.0f);
        final var bounds = bounds();
        graphics.setStroke(rectangleStroke);
        GRID.applyAsForeground(graphics);
        final DrawingPoint bottomLeft = mapper.toDrawingPoint(bounds.bottomLeft());
        final DrawingPoint bottomRight = mapper.toDrawingPoint(bounds.bottomRight());
        final DrawingPoint topRight = mapper.toDrawingPoint(bounds.topRight());
        graphics.drawLine((int) bottomLeft.x(), (int) bottomLeft.y(), (int) bottomRight.x(),
                (int) bottomRight.y());
        graphics.drawLine((int) topRight.x(), (int) topRight.y(), (int) bottomRight.x(),
                (int) bottomRight.y());

        // Draw label for tile rectangle
        final var visible = view.intersect(bounds);
        if (visible != null)
        {
            final Point2D lowerRight = mapper.toDrawingPoint(visible.bottomRight());
            new LabelRenderer(style, toString()).draw(graphics, lowerRight, BOTTOM_RIGHT);
        }
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

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
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

    public SlippyTile parent()
    {
        return getZoomLevel().zoomOut().tileAt(bounds().center());
    }

    public Size size()
    {
        return bounds().size();
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
}
