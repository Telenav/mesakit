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

import com.telenav.kivakit.core.kernel.language.primitives.Ints;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;
import com.telenav.mesakit.map.ui.desktop.coordinates.mappers.MercatorCoordinateMapper;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

public class SlippyTileCoordinateSystem implements MapCoordinateMapper
{
    /**
     * The bounds of the slippy tile coordinate system
     */
    public static final Rectangle BOUNDS = Rectangle.fromLocations(Location.degrees(-85.0511, -180),
            Location.degrees(85.0511, 180));

    private final MercatorCoordinateMapper projection;

    private final ZoomLevel zoom;

    private final Dimension tileSize;

    public SlippyTileCoordinateSystem(final ZoomLevel zoom)
    {
        this(zoom, SlippyTile.STANDARD_TILE_SIZE);
    }

    public SlippyTileCoordinateSystem(final ZoomLevel zoomlevel, final Dimension tileSize)
    {
        zoom = zoomlevel;
        this.tileSize = tileSize;
        projection = new MercatorCoordinateMapper(zoom.sizeInPixels(tileSize));
    }

    public Rectangle bounds(final SlippyTile tile)
    {
        final var upperLeft = locationForPoint(
                new Point(tile.getX() * tileSize.width, tile.getY() * tileSize.height));
        final var lowerRight = locationForPoint(
                new Point((tile.getX() + 1) * tileSize.width, (tile.getY() + 1) * tileSize.height));
        return Rectangle.fromLocations(upperLeft, lowerRight);
    }

    @Override
    public Location locationForPoint(final Point2D point)
    {
        return projection.locationForPoint(point);
    }

    public SlippyTile tileForLocation(final Location location)
    {
        final var point = toDrawingPoint(location);
        return tileForPoint(point);
    }

    public SlippyTile tileForPoint(final Point2D point)
    {
        final var x = Ints.inRange((int) (point.getX() / tileSize.width), 0, zoom.widthInTiles());
        final var y = Ints.inRange((int) (point.getY() / tileSize.height), 0, zoom.heightInTiles());
        return new SlippyTile(zoom, x, y);
    }

    @Override
    public Point2D toDrawingPoint(final Location location)
    {
        return projection.toDrawingPoint(location);
    }
}
