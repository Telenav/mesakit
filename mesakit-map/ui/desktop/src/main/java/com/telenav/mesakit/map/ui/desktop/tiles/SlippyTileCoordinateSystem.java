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
import com.telenav.kivakit.ui.desktop.graphics.geometry.coordinates.CartesianCoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections.SphericalMercatorMapProjection;

import static com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile.STANDARD_TILE_SIZE;

public class SlippyTileCoordinateSystem extends CartesianCoordinateSystem
{
    /**
     * The bounds of the slippy tile coordinate system
     */
    public static final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle SLIPPY_TILE_MAP_AREA = com.telenav.mesakit.map.geography.shape.rectangle.Rectangle.fromLocations(
            Location.degrees(-85.0511, -180),
            Location.degrees(85.0511, 180));

    private final MapProjection projection;

    private final ZoomLevel zoom;

    private final Size tileSize;

    public SlippyTileCoordinateSystem(final ZoomLevel zoom)
    {
        this(zoom, STANDARD_TILE_SIZE);
    }

    public SlippyTileCoordinateSystem(final ZoomLevel zoom, final Size tileSize)
    {
        super(Point.pixels(0, 0));

        this.zoom = zoom;
        this.tileSize = tileSize;

        projection = new SphericalMercatorMapProjection(SLIPPY_TILE_MAP_AREA, size().asRectangle());
    }

    public Rectangle drawingArea(final SlippyTile tile)
    {
        final var upperLeft =
                Point.at(this,
                        tile.x() * tileSize.widthInUnits(),
                        tile.y() * tileSize.heightInUnits());

        final var lowerRight =
                Point.at(this,
                        (tile.x() + 1) * tileSize.widthInUnits(),
                        (tile.y() + 1) * tileSize.heightInUnits());

        return Rectangle.rectangle(upperLeft, lowerRight);
    }

    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle mapArea(final SlippyTile tile)
    {
        return projection.toMap(drawingArea(tile));
    }

    @Override
    public Size size()
    {
        return zoom.sizeInDrawingUnits(tileSize);
    }

    public SlippyTile tileForLocation(final Location location)
    {
        return tileForPoint(projection.toDrawing(location));
    }

    public SlippyTile tileForPoint(final Point point)
    {
        final var x = Ints.inRange((int) (point.x() / tileSize.widthInUnits()), 0, zoom.widthInTiles() - 1);
        final var y = Ints.inRange((int) (point.y() / tileSize.heightInUnits()), 0, zoom.heightInTiles() - 1);

        return new SlippyTile(zoom, x, y);
    }

    public Point toDrawing(final Location at)
    {
        return projection.toDrawing(at);
    }

    public Location toMap(final Point at)
    {
        return projection.toMap(at);
    }
}
