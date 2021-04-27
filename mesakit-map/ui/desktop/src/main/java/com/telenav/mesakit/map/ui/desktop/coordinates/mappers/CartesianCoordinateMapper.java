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

package com.telenav.mesakit.map.ui.desktop.coordinates.mappers;

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;

/**
 * Does a simple linear Cartesian mapping between a {@link DrawingSurface} and a {@link Rectangle} of the world.
 *
 * @author jonathanl (shibo)
 */
public class CartesianCoordinateMapper implements MapCoordinateMapper
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
    public CartesianCoordinateMapper(final Rectangle mapBounds,
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
        final var longitude = location.longitude().asDegrees();
        final var latitude = location.latitude().asDegrees();
        final var x = drawingSurfaceLeft + drawingSurfaceWidth * ((longitude - boundsLeftInDegrees) / boundsWidthInDegrees);
        final var y = drawingSurfaceTop + drawingSurfaceHeight * ((boundsTopInDegrees - latitude) / boundsHeightInDegrees);
        return DrawingPoint.at(x, y);
    }

    @Override
    public Location toMapLocation(final DrawingPoint point)
    {
        var latitude = boundsBottomInDegrees + (boundsHeightInDegrees * ((drawingSurfaceHeight - (point.y() - drawingSurfaceTop)) / drawingSurfaceHeight));
        var longitude = boundsLeftInDegrees + (boundsWidthInDegrees * ((point.x() - drawingSurfaceLeft) / drawingSurfaceWidth));
        if (latitude > 90)
        {
            latitude = 90;
        }
        if (latitude < -90)
        {
            latitude = -90;
        }
        if (longitude > 180)
        {
            longitude = 180;
        }
        if (longitude < -180)
        {
            longitude = -180;
        }
        return Location.degrees(latitude, longitude);
    }
}
