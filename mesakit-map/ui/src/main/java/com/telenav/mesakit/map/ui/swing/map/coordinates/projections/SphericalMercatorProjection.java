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

package com.telenav.mesakit.map.ui.swing.map.coordinates.projections;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.swing.map.coordinates.mappers.CoordinateMapper;
import com.telenav.kivakit.core.kernel.language.primitives.Doubles;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Maps points between the Swing coordinate system and spherical geographic coordinates.
 *
 * @author jonathanl (shibo)
 */
public class SphericalMercatorProjection implements CoordinateMapper
{
    /**
     * The dimension of the Swing area to project from/to
     */
    private final java.awt.Dimension maximum;

    /**
     * @param maximum The dimensions of the Swing coordinate system
     */
    public SphericalMercatorProjection(final Dimension maximum)
    {
        this.maximum = maximum;
    }

    /**
     * @param point The Swing point
     * @return The geographic location for the given point
     */
    @Override
    public Location locationForPoint(final Point2D point)
    {
        final var x = point.getX() / maximum.width - 0.5;
        final var y = 0.5 - (point.getY() / maximum.height);
        final var longitudeInDegrees = Doubles.inRange(x * 360, -180, 180);
        final var radians = Math.atan(Math.exp(-y * 2 * Math.PI)) * 2;
        if (Double.isNaN(radians))
        {
            throw new IllegalArgumentException("Cannot map point " + point);
        }
        final var latitudeInDegrees = Doubles.inRange(90 - Math.toDegrees(radians), -90, 90);
        return Location.degrees(latitudeInDegrees, longitudeInDegrees);
    }

    /**
     * @param location The geographic location
     * @return The Swing point for the given location
     */
    @Override
    public Point2D pointForLocation(final Location location)
    {
        final var siny = Math.sin(Math.toRadians(location.latitude().asDegrees()));
        if (Double.isNaN(siny))
        {
            throw new IllegalArgumentException("Cannot map location " + location);
        }
        final var x = location.longitude().asDegrees() / 360 + 0.5;
        final var y = 0.5 * Math.log((1 + siny) / (1 - siny)) / -(2 * Math.PI) + .5;
        return new Point((int) Math.round(x * (maximum.width - 1)),
                (int) Math.round(y * (maximum.height - 1)));
    }
}
