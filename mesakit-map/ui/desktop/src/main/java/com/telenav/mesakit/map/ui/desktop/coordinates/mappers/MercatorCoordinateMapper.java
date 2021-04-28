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

import com.telenav.kivakit.core.kernel.language.primitives.Doubles;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

/**
 * Maps points between the {@link DrawingSurface} coordinate system and spherical geographic coordinates.
 *
 * @author jonathanl (shibo)
 */
public class MercatorCoordinateMapper implements MapCoordinateMapper
{
    /** The dimensions of the map area */
    private final Rectangle mapArea;

    /** The dimension of the drawing area */
    private final DrawingRectangle drawingArea;

    public MercatorCoordinateMapper(final Rectangle mapArea, final DrawingRectangle drawingArea)
    {
        this.mapArea = mapArea;
        this.drawingArea = drawingArea;
    }

    /**
     * @param location The geographic location
     * @return The drawing area point for the given location
     */
    @Override
    public DrawingPoint toDrawingPoint(final Location location)
    {
        final var siny = Math.sin(Math.toRadians(location.latitude().asDegrees()));
        if (Double.isNaN(siny))
        {
            fail("Cannot map location " + location);
        }

        final var x = location.longitude().asDegrees() / 360 + 0.5;
        final var y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5;

        final var dx = (int) Math.round(x * ((int) drawingArea.width() - 1));
        final var dy = (int) Math.round(y * ((int) drawingArea.height() - 1));

        return DrawingPoint.at(drawingArea.x() + dx, drawingArea.y() + dy);
    }

    /**
     * @param point The Swing point
     * @return The geographic location for the given point
     */
    @Override
    public Location toMapLocation(final DrawingPoint point)
    {
        final var x = point.x() / drawingArea.width() - 0.5;
        final var y = 0.5 - (point.y() / drawingArea.height());
        final var longitudeInDegrees = Doubles.inRange(x * 360, -180, 180);
        final var radians = Math.atan(Math.exp(-y * 2 * Math.PI)) * 2;
        if (Double.isNaN(radians))
        {
            fail("Cannot map point " + point);
        }
        final var latitudeInDegrees = Doubles.inRange(90 - Math.toDegrees(radians), -90, 90);
        return Location.degrees(latitudeInDegrees, longitudeInDegrees)
                .offsetBy(mapArea.topLeft().asSizeFromOrigin());
    }
}
