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
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingSize;
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

/**
 * Maps points between the {@link DrawingSurface} coordinate system and spherical geographic coordinates.
 *
 * @author jonathanl (shibo)
 */
public class MercatorCoordinateMapper implements MapCoordinateMapper
{
    /**
     * The dimension of the Swing area to project from/to
     */
    private final DrawingSize maximum;

    /**
     * @param maximum The dimensions of the Swing coordinate system
     */
    public MercatorCoordinateMapper(final DrawingSize maximum)
    {
        this.maximum = maximum;
    }

    /**
     * @param location The geographic location
     * @return The Swing point for the given location
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

        return DrawingPoint.at(
                (int) Math.round(x * (maximum.width() - 1)),
                (int) Math.round(y * (maximum.height() - 1)));
    }

    /**
     * @param point The Swing point
     * @return The geographic location for the given point
     */
    @Override
    public Location toMapLocation(final DrawingPoint point)
    {
        final var x = point.x() / maximum.width() - 0.5;
        final var y = 0.5 - (point.y() / maximum.height());
        final var longitudeInDegrees = Doubles.inRange(x * 360, -180, 180);
        final var radians = Math.atan(Math.exp(-y * 2 * Math.PI)) * 2;
        if (Double.isNaN(radians))
        {
            fail("Cannot map point " + point);
        }
        final var latitudeInDegrees = Doubles.inRange(90 - Math.toDegrees(radians), -90, 90);
        return Location.degrees(latitudeInDegrees, longitudeInDegrees);
    }
}
