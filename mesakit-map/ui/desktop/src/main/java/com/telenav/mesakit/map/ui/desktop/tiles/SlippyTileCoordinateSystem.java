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

import com.telenav.kivakit.core.language.primitive.Ints;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.DrawingCoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections.SphericalMercatorMapProjection;

import static com.telenav.mesakit.map.ui.desktop.tiles.SlippyTile.STANDARD_TILE_SIZE;

/**
 * A tile coordinate system with an origin at 0, 0 and an extent based on the {@link ZoomLevel} and tile size. The
 * coordinate system supports mapping between {@link Location}s and {@link DrawingPoint}s using a {@link
 * SphericalMercatorMapProjection}.
 *
 * @author jonathanl (shibo)
 */
public class SlippyTileCoordinateSystem extends DrawingCoordinateSystem
{
    /**
     * The bounds of the slippy tile coordinate system
     */
    public static final Rectangle SLIPPY_TILE_MAP_AREA = Rectangle.fromLocations(
            Location.degrees(Latitude.MINIMUM_DEGREES, Longitude.MINIMUM_DEGREES),
            Location.degrees(Latitude.MAXIMUM_DEGREES, Longitude.MAXIMUM_DEGREES));

    private final MapProjection projection;

    private final ZoomLevel zoom;

    private final DrawingSize tileSize;

    public SlippyTileCoordinateSystem(ZoomLevel zoom)
    {
        this(zoom, STANDARD_TILE_SIZE);
    }

    public SlippyTileCoordinateSystem(ZoomLevel zoom, DrawingSize tileSize)
    {
        super("slippy-tile");

        origin(0, 0);

        var width = zoom.widthInPixels(tileSize);
        var height = zoom.heightInPixels(tileSize);

        extent(width, height);

        this.zoom = zoom;
        this.tileSize = tileSize;

        projection = new SphericalMercatorMapProjection(SLIPPY_TILE_MAP_AREA, extent());
    }

    public DrawingRectangle drawingArea(SlippyTile tile)
    {
        var upperLeft =
                DrawingPoint.point(this,
                        tile.x() * tileSize.widthInUnits(),
                        tile.y() * tileSize.heightInUnits());

        var lowerRight =
                DrawingPoint.point(this,
                        (tile.x() + 1) * tileSize.widthInUnits(),
                        (tile.y() + 1) * tileSize.heightInUnits());

        return DrawingRectangle.rectangle(upperLeft, lowerRight);
    }

    public Rectangle mapArea(SlippyTile tile)
    {
        return projection.toMap(drawingArea(tile));
    }

    public SlippyTile tileForLocation(Location location)
    {
        return tileForPoint(projection.toDrawing(location));
    }

    public SlippyTile tileForPoint(DrawingPoint point)
    {
        var x = Ints.intInRangeInclusive((int) (point.x() / tileSize.widthInUnits()), 0, zoom.widthInTiles() - 1);
        var y = Ints.intInRangeInclusive((int) (point.y() / tileSize.heightInUnits()), 0, zoom.heightInTiles() - 1);

        return new SlippyTile(zoom, x, y);
    }

    public DrawingPoint toDrawing(Location at)
    {
        return projection.toDrawing(at);
    }

    public Location toMap(DrawingPoint at)
    {
        return projection.toMap(at);
    }
}
