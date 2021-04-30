/*
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * //
 * // © 2011-2021 Telenav, Inc.
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * // http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 * //
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *
 */

package com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections;

import com.telenav.kivakit.core.kernel.language.primitives.Doubles;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapDrawingSurfaceProjection;

/**
 * Does a simple linear Cartesian mapping between a {@link DrawingSurface} and a {@link Rectangle} of the world. This
 * projection is generally inaccurate and only useful for short distances where accuracy isn't a significant concern.
 *
 * @author jonathanl (shibo)
 */
public class CartesianDrawingSurfaceProjection implements MapDrawingSurfaceProjection
{
    private final double drawingSurfaceTop;

    private final double drawingSurfaceLeft;

    private final double drawingSurfaceWidth;

    private final double drawingSurfaceHeight;

    private final double boundsTopInDegrees;

    private final double boundsLeftInDegrees;

    private final double boundsWidthInDegrees;

    private final double boundsHeightInDegrees;

    private final double boundsBottomInDegrees;

    /**
     * @param mapBounds The map bounds to map to and from
     * @param drawingArea The {@link DrawingSurface} bounds to map to and from
     */
    public CartesianDrawingSurfaceProjection(final Rectangle mapBounds,
                                             final DrawingRectangle drawingArea)
    {
        boundsBottomInDegrees = mapBounds.bottom().asDegrees();
        boundsLeftInDegrees = mapBounds.left().asDegrees();
        boundsTopInDegrees = mapBounds.top().asDegrees();
        boundsWidthInDegrees = mapBounds.width().asDegrees();
        boundsHeightInDegrees = mapBounds.height().asDegrees();

        drawingSurfaceTop = drawingArea.y();
        drawingSurfaceLeft = drawingArea.x();
        drawingSurfaceWidth = drawingArea.width();
        drawingSurfaceHeight = drawingArea.height();
    }

    @Override
    public DrawingPoint toDrawingPoint(final Location location)
    {
        // Get the latitude and longitude of the location,
        final var latitude = location.latitudeInDegrees();
        final var longitude = location.longitudeInDegrees();

        // convert to unit values between 0 and 1 relative to the top left,
        final var latitudeUnit = (boundsTopInDegrees - latitude) / boundsHeightInDegrees;
        final var longitudeUnit = (longitude - boundsLeftInDegrees) / boundsWidthInDegrees;

        // then compute the x, y location.
        final var x = drawingSurfaceLeft + drawingSurfaceWidth * longitudeUnit;
        final var y = drawingSurfaceTop + drawingSurfaceHeight * latitudeUnit;

        return DrawingPoint.at(x, y);
    }

    @Override
    public Location toMapLocation(final DrawingPoint point)
    {
        // Get the offset of the drawing point from the bottom left
        final var xOffset = point.x() - drawingSurfaceLeft;
        final var yOffset = drawingSurfaceHeight - (point.y() - drawingSurfaceTop);

        // compute a unit value between 0 and 1 from bottom left,
        final var xUnit = xOffset / drawingSurfaceWidth;
        final var yUnit = yOffset / drawingSurfaceHeight;

        // scale the map area bounds by the unit value,
        final var latitudeOffset = boundsHeightInDegrees * yUnit;
        final var longitudeOffset = boundsWidthInDegrees * xUnit;

        // and add the offset to the bottom left of the map bounds area.
        final var latitude = boundsBottomInDegrees + latitudeOffset;
        final var longitude = boundsLeftInDegrees + longitudeOffset;

        return Location.degrees(Doubles.inRange(latitude, -90, 90), Doubles.inRange(longitude, -180, 180));
    }
}
