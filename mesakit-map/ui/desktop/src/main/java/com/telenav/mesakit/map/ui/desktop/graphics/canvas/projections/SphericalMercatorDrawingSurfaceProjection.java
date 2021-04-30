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

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.projection.MetricCoordinate;
import com.telenav.mesakit.map.geography.projection.projections.SphericalMercatorMetricProjection;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapDrawingSurfaceProjection;

/**
 * Maps points between the {@link DrawingSurface} coordinate system and spherical geographic coordinates. The mapping is
 * accomplished by using {@link SphericalMercatorMetricProjection} to map between {@link Location}s in degrees and
 * {@link MetricCoordinate}s in meters. The (Cartesian) metric value is then used to produce normalized values between 0
 * and 1 which are used to interpolate a location within the {@link DrawingRectangle}.
 *
 * @author jonathanl (shibo)
 * @see <a href="https://wiki.openstreetmap.org/wiki/Mercator">OpenStreetMap Wiki - Mercator Projection</a>
 * @see SphericalMercatorMetricProjection
 */
public class SphericalMercatorDrawingSurfaceProjection implements MapDrawingSurfaceProjection
{
    /** Radius of Earth at equator */
    public static final double RADIUS_IN_METERS = 6378137.0;

    /**
     * Maps between coordinates in degrees and coordinates in meters from the map origin (0, 0) using a simple spherical
     * Mercator projection.
     */
    private final SphericalMercatorMetricProjection mercatorProjection = new SphericalMercatorMetricProjection();

    private final MetricCoordinate mapAreaTopLeftInMeters;

    private final double mapAreaWidthInMeters;

    private final double mapAreaHeightInMeters;

    private final DrawingRectangle drawingArea;

    public SphericalMercatorDrawingSurfaceProjection(final Rectangle mapArea, final DrawingRectangle drawingArea)
    {
        // Save the drawing area,
        this.drawingArea = drawingArea;

        // project the top left and bottom right corners of the map area to x,y values in meters
        mapAreaTopLeftInMeters = mercatorProjection.toCoordinate(mapArea.topLeft());
        final var metricBottomRight = mercatorProjection.toCoordinate(mapArea.bottomRight());

        // then compute the width of the map area in meters
        mapAreaWidthInMeters = metricBottomRight.xInMeters() - mapAreaTopLeftInMeters.xInMeters();
        mapAreaHeightInMeters = metricBottomRight.yInMeters() - mapAreaTopLeftInMeters.yInMeters();
    }

    /**
     * @param location The geographic location
     * @return The drawing area point for the given location
     */
    @Override
    public DrawingPoint toDrawingPoint(final Location location)
    {
        // Project the location to a coordinate in meters from the map origin
        final var projected = mercatorProjection.toCoordinate(location);

        // then normalize the projected point to the unit interval (0 to 1)
        final double xUnit = (projected.xInMeters() - mapAreaTopLeftInMeters.xInMeters()) / mapAreaWidthInMeters;
        final double yUnit = (projected.yInMeters() - mapAreaTopLeftInMeters.yInMeters()) / mapAreaHeightInMeters;

        // compute the offset into the drawing area by scaling the width and height by the unit values,
        final var dx = xUnit * drawingArea.width();
        final var dy = yUnit * drawingArea.height();

        // and return the point within the drawing area.
        return DrawingPoint.at(drawingArea.x() + dx, drawingArea.y() + dy);
    }

    /**
     * @param point The drawing surface point
     * @return The geographic location for the given point
     */
    @Override
    public Location toMapLocation(final DrawingPoint point)
    {
        // Normalize the x,y location on the drawing surface to the unit interval (0 to 1)
        final double xUnit = (point.x() - drawingArea.x()) / drawingArea.width();
        final double yUnit = (point.y() - drawingArea.y()) / drawingArea.height();

        // then convert the unit interval to meters within the map width
        final var xMeters = mapAreaWidthInMeters * xUnit;
        final var yMeters = mapAreaHeightInMeters * yUnit;

        // offset the relative location to the metric coordinate system
        final var x = mapAreaTopLeftInMeters.xInMeters() + xMeters;
        final var y = mapAreaTopLeftInMeters.yInMeters() + yMeters;

        // Project the drawing surface point to
        return mercatorProjection.toLocation(new MetricCoordinate(x, y));
    }
}
