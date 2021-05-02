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
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateRectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSize;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections.SphericalMercatorMapProjection;

import static com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSystem.drawingSurface;

public class SlippyTileCoordinateSystem implements MapProjection
{
    /**
     * The bounds of the slippy tile coordinate system
     */
    public static final Rectangle SLIPPY_TILE_BOUNDS = Rectangle.fromLocations(
            Location.degrees(-85.0511, -180),
            Location.degrees(85.0511, 180));

    private final SphericalMercatorMapProjection projection;

    private final ZoomLevel zoom;

    private final CoordinateSize tileSize;

    public SlippyTileCoordinateSystem(final ZoomLevel zoom)
    {
        this(zoom, SlippyTile.STANDARD_TILE_SIZE);
    }

    public SlippyTileCoordinateSystem(final ZoomLevel zoomlevel, final CoordinateSize tileSize)
    {
        zoom = zoomlevel;
        this.tileSize = tileSize;
        projection = new SphericalMercatorMapProjection(SLIPPY_TILE_BOUNDS,
                zoom.sizeInDrawingUnits(tileSize).asRectangle());
    }

    public Rectangle bounds(final SlippyTile tile)
    {
        final var upperLeft = toMapUnits(
                Coordinate.at(drawingSurface(),
                        tile.x() * tileSize.widthInUnits(),
                        tile.y() * tileSize.heightInUnits()));

        final var lowerRight = toMapUnits(
                Coordinate.at(drawingSurface(),
                        (tile.x() + 1) * tileSize.widthInUnits(),
                        (tile.y() + 1) * tileSize.heightInUnits()));

        return Rectangle.fromLocations(upperLeft, lowerRight);
    }

    @Override
    public CoordinateRectangle coordinateArea()
    {
        return null;
    }

    @Override
    public Rectangle mapArea()
    {
        return projection.mapArea();
    }

    public SlippyTile tileForLocation(final Location location)
    {
        final var point = toCoordinates(location);
        return tileForPoint(point);
    }

    public SlippyTile tileForPoint(final Coordinate point)
    {
        final var x = Ints.inRange((int) (point.x() / tileSize.widthInUnits()), 0, zoom.widthInTiles() - 1);
        final var y = Ints.inRange((int) (point.y() / tileSize.heightInUnits()), 0, zoom.heightInTiles() - 1);

        return new SlippyTile(zoom, x, y);
    }

    @Override
    public Coordinate toCoordinates(final Location location)
    {
        return projection.toCoordinates(location);
    }

    @Override
    public Location toMapUnits(final Coordinate point)
    {
        return projection.toMapUnits(point);
    }
}
