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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections;

import com.telenav.kivakit.ui.desktop.graphics.drawing.CoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;

/**
 * Does a simple linear mapping between a (Cartesian) {@link CoordinateSystem} and a {@link Rectangle} of the world in
 * degrees of latitude and longitude. This projection is fast, but it is inaccurate at longer distances, especially at
 * high latitudes.
 *
 * @author jonathanl (shibo)
 */
public class CartesianMapProjection implements MapProjection
{
    private final double mapTopInDegrees;

    private final double mapLeftInDegrees;

    private final double mapWidthInDegrees;

    private final double mapHeightInDegrees;

    private final double mapBottomInDegrees;

    private final Rectangle mapArea;

    private final DrawingRectangle drawingArea;

    /**
     * @param mapArea The map map to map to and from
     * @param drawingArea The {@link DrawingSurface} map to map to and from
     */
    public CartesianMapProjection(final Rectangle mapArea,
                                  final DrawingRectangle drawingArea)
    {
        this.mapArea = mapArea;
        this.drawingArea = drawingArea;

        mapBottomInDegrees = mapArea.bottom().asDegrees();
        mapLeftInDegrees = mapArea.left().asDegrees();
        mapTopInDegrees = mapArea.top().asDegrees();
        mapWidthInDegrees = mapArea.width().asDegrees();
        mapHeightInDegrees = mapArea.height().asDegrees();
    }

    /**
     * Updates the {@link CoordinateSystem} of the drawing area for this projection
     */
    public void coordinateSystem(final CoordinateSystem coordinateSystem)
    {
        drawingArea.coordinateSystem(coordinateSystem);
    }

    @Override
    public DrawingRectangle drawingArea()
    {
        return drawingArea;
    }

    @Override
    public Rectangle mapArea()
    {
        return mapArea;
    }

    @Override
    public DrawingPoint toDrawing(final Location location)
    {
        // Get the latitude and longitude of the location,
        final var latitude = location.latitudeInDegrees();
        final var longitude = location.longitudeInDegrees();

        // convert to unit values between 0 and 1 relative to the top left,
        final var latitudeUnit = (mapTopInDegrees - latitude) / mapHeightInDegrees;
        final var longitudeUnit = (longitude - mapLeftInDegrees) / mapWidthInDegrees;

        // then compute the x, y location.
        final var x = drawingArea.left() + drawingArea.width() * longitudeUnit;
        final var y = drawingArea.top() + drawingArea.height() * latitudeUnit;

        return DrawingPoint.at(drawingArea.coordinateSystem(), x, y);
    }

    @Override
    public Location toMap(final DrawingPoint point)
    {
        // Get the offset of the drawing point from the bottom left
        final var xOffset = point.x() - drawingArea.left();
        final var yOffset = drawingArea.height() - (point.y() - drawingArea.top());

        // compute a unit value between 0 and 1 from bottom left,
        final var xUnit = xOffset / drawingArea.width();
        final var yUnit = yOffset / drawingArea.height();

        // scale the map area map by the unit value,
        final var latitudeOffset = mapHeightInDegrees * yUnit;
        final var longitudeOffset = mapWidthInDegrees * xUnit;

        // and add the offset to the bottom left of the map map area.
        final var latitude = mapBottomInDegrees + latitudeOffset;
        final var longitude = mapLeftInDegrees + longitudeOffset;

        if (Latitude.isValid(latitude) && Longitude.isValid(longitude))
        {
            return Location.degrees(latitude, longitude);
        }

        return null;
    }
}
