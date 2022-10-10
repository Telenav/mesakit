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

import com.telenav.kivakit.core.language.primitive.Doubles;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * In OSM, zoom level 0 is zoomed out so that the entire world is one tile and zoom level 17 is zoomed in as far as
 * possible with 68 billion tiles. The OSM zoom level can be retrieved with {@link #level()}.
 * <p>
 * In some Telenav applications, zoom level 0 is zoomed in as far as possible and zoom level 17 is zoomed out as far as
 * possible. The Telenav zoom level can be retrieved with {@link #asTelenavZoomLevel()}.
 *
 * @author jonathanl (shibo)
 */
public class ZoomLevel implements StringFormattable
{
    /** Maximum zoom level in OSM */
    public static final ZoomLevel CLOSEST = new ZoomLevel(17);

    /** Minimum zoom level in OSM */
    public static final ZoomLevel FURTHEST = new ZoomLevel(0);

    private static final List<ZoomLevel> levels = new ArrayList<>();

    static
    {
        for (var level = FURTHEST.level; level <= CLOSEST.level; level++)
        {
            levels.add(new ZoomLevel(level));
        }
    }

    public static List<ZoomLevel> all()
    {
        return levels;
    }

    /**
     * Returns the zoom level that is the best to fit the given bounds into the visible drawing rectangle using the
     * given tile size
     */
    public static ZoomLevel bestFit(DrawingRectangle visible,
                                    DrawingSize tileSize,
                                    Rectangle bounds)
    {
        // If the bounds is all in the same tile at the highest zoom level
        if (CLOSEST.tileAt(bounds.topLeft()).equals(CLOSEST.tileAt(bounds.bottomRight())))
        {
            // then we want to be zoomed all the way in
            return CLOSEST;
        }

        // Get the dimensions of the display in tiles
        var displayWidthInTiles = visible.width() / tileSize.widthInUnits();
        var displayHeightInTiles = visible.height() / tileSize.heightInUnits();

        // Zoom out from closest in to furthest out
        for (var zoom = CLOSEST; !zoom.isFurthestOut(); zoom = zoom.zoomOut())
        {
            var topLeft = zoom.tileAt(bounds.topLeft());
            var bottomRight = zoom.tileAt(bounds.bottomRight());

            // If the screen width is big enough to accommodate the given bounds
            var widthInTiles = bottomRight.x() - topLeft.x();
            var heightInTiles = bottomRight.y() - topLeft.y();
            if (widthInTiles < displayWidthInTiles && heightInTiles < displayHeightInTiles)
            {
                // then this zoom level is good enough
                return zoom;
            }
        }
        return FURTHEST;
    }

    /**
     * Returns the specified OSM zoom level, from 0 (furthest) to 18 (closest)
     */
    public static ZoomLevel osm(int level)
    {
        return levels.get(level);
    }

    /**
     * Returns the given Telenav zoom level from 0 (closest) to 17 (furthest)
     */
    public static ZoomLevel telenav(int level)
    {
        ensure(level < CLOSEST.level, "Level must be greater than or equal to " + CLOSEST.level);
        ensure(level >= FURTHEST.level, "Level must be less than or equal to " + FURTHEST.level);

        return levels.get(17 - level);
    }

    /**
     * The OSM zoom level
     */
    private final int level;

    /**
     * @param level The Telenav zoom level
     */
    private ZoomLevel(int level)
    {
        if (level < 0 || level > 18)
        {
            fail("Invalid zoom level $", level);
        }
        this.level = level;
    }

    @Override
    public String asString(@NotNull Format format)
    {
        return "[ZoomLevel level = " + level + ", widthInTiles = " + widthInTiles() + "]";
    }

    /**
     * Returns this zoom level as an OSM zoom level
     */
    public int asTelenavZoomLevel()
    {
        return 17 - level;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof ZoomLevel)
        {
            var that = (ZoomLevel) object;
            return level == that.level;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return level;
    }

    /**
     * Returns the total height in pixels for all tiles at the given tile size
     */
    public double heightInPixels(DrawingSize tileSize)
    {
        return heightInTiles() * tileSize.heightInUnits();
    }

    /**
     * Returns the number of tiles vertically at this level
     */
    public int heightInTiles()
    {
        return 1 << level;
    }

    public DrawingPoint inRange(DrawingPoint point, DrawingSize tileSize)
    {
        return DrawingPoint.point(point.coordinates(),
                Doubles.inRange(point.x(), 0, widthInPixels(tileSize)),
                Doubles.inRange(point.y(), 0, heightInPixels(tileSize)));
    }

    /**
     * Returns true if this zoom level is zoomed in more than the given zoom level
     */
    public boolean isCloserThan(ZoomLevel that)
    {
        return level > that.level;
    }

    /**
     * Returns true if this zoom level is a close as possible
     */
    public boolean isClosestIn()
    {
        return equals(CLOSEST);
    }

    /**
     * Returns true if this zoom level is zoomed out more than the given zoom level
     */
    public boolean isFurtherThan(ZoomLevel that)
    {
        return level < that.level;
    }

    /**
     * Returns true if this zoom level is the furthest out possible
     */
    public boolean isFurthestOut()
    {
        return equals(FURTHEST);
    }

    /**
     * Returns the OSM zoom level from 0 (zoomed out) to 18 (zoomed in)
     */
    @KivaKitIncludeProperty
    public int level()
    {
        return level;
    }

    /**
     * Returns the total dimension of an image of the world at this zoom level using the given tile size
     */
    public DrawingSize sizeInDrawingUnits(DrawingSize tileSize)
    {
        return DrawingSize.size(tileSize.coordinates(), widthInPixels(tileSize), heightInPixels(tileSize));
    }

    /**
     * Returns the tile at the given location using this zoom level
     */
    public SlippyTile tileAt(Location location)
    {
        return new SlippyTileCoordinateSystem(this).tileForLocation(location);
    }

    @Override
    public String toString()
    {
        return Integer.toString(level());
    }

    /**
     * Returns the total number of tiles at this zoom level
     */
    public long totalTiles()
    {
        return (long) widthInTiles() * (long) heightInTiles();
    }

    /**
     * Returns the total width in pixels for all tiles at the given tile size
     */
    public double widthInPixels(DrawingSize tileSize)
    {
        return widthInTiles() * tileSize.widthInUnits();
    }

    /**
     * Returns the number of tiles horizontally at this level
     */
    public int widthInTiles()
    {
        return 1 << level;
    }

    /**
     * Returns this zoom level zoomed in one level
     */
    public ZoomLevel zoomIn()
    {
        return zoomIn(1);
    }

    /**
     * Returns this zoom level zoomed in by the given number of levels
     */
    public ZoomLevel zoomIn(int levels)
    {
        return osm(Math.min(CLOSEST.level, level + levels));
    }

    /**
     * Returns this zoom level zoomed out one level
     */
    public ZoomLevel zoomOut()
    {
        return zoomOut(1);
    }

    /**
     * Returns this zoom level zoomed out by the given number of levels
     */
    public ZoomLevel zoomOut(int levels)
    {
        return osm(Math.max(FURTHEST.level, level - levels));
    }
}
