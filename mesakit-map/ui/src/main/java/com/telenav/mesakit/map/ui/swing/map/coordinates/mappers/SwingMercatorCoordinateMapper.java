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

package com.telenav.mesakit.map.ui.swing.map.coordinates.mappers;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.ui.swing.map.coordinates.projections.SphericalMercatorProjection;
import com.telenav.mesakit.map.ui.swing.map.tiles.ZoomLevel;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Maps point between the spherical geographical coordinate system and a Swing rectangle.
 *
 * @author jonathanl (shibo)
 */
public class SwingMercatorCoordinateMapper implements CoordinateMapper
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    /**
     * Mercator projection to/from maximum pixel area
     */
    private final SphericalMercatorProjection highResolutionProjection;

    /**
     * The swing rectangle to map from/to
     */
    private final java.awt.Rectangle pixelBounds;

    /**
     * High resolution rectangle that corresponds to the pixelBounds rectangle
     */
    private final java.awt.Rectangle highResolutionPixelBounds;

    /**
     * @param geographicBounds The map area to map to/from
     * @param swingBounds The pixel area to map to/from
     * @param tileSize Tile dimensions in pixels
     */
    public SwingMercatorCoordinateMapper(final Rectangle geographicBounds, final java.awt.Rectangle swingBounds,
                                         final Dimension tileSize)
    {
        // Save the geographic rectangle to map from/to
        pixelBounds = swingBounds;

        // Compute the maximum resolution in pixels at the highest zoom level
        final var highResolution = new Dimension(ZoomLevel.CLOSEST.widthInPixels(tileSize),
                ZoomLevel.CLOSEST.heightInPixels(tileSize));
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("highResolution = $", highResolution);
        }

        // Do projections using the maximum resolution
        highResolutionProjection = new SphericalMercatorProjection(highResolution);

        // Compute high resolution bounds
        final var topLeft = highResolutionProjection.pointForLocation(geographicBounds.topLeft());
        final var bottomRight = highResolutionProjection.pointForLocation(geographicBounds.bottomRight());
        highResolutionPixelBounds = new java.awt.Rectangle((int) topLeft.getX(), (int) topLeft.getY(),
                (int) (bottomRight.getX() - topLeft.getX()),
                (int) (bottomRight.getY() - topLeft.getY()));
        if (DEBUG.isDebugOn())
        {
            LOGGER.information("highResolutionPixelBounds = $", highResolutionPixelBounds);
        }
    }

    @Override
    public Location locationForPoint(final Point2D point)
    {
        // Convert the point to high resolution
        final var x = highResolutionPixelBounds.x + (int) Math.round(
                ((point.getX() - pixelBounds.x) * highResolutionPixelBounds.width / pixelBounds.width));
        final var y = highResolutionPixelBounds.y + (int) Math.round(((point.getY() - pixelBounds.y)
                * highResolutionPixelBounds.height / pixelBounds.height));

        // Project the high resolution coordinate
        return highResolutionProjection.locationForPoint(new Point(x, y));
    }

    @Override
    public Point2D pointForLocation(final Location location)
    {
        final var highResolution = highResolutionProjection.pointForLocation(location);
        final var x = pixelBounds.x
                + (int) Math.round(((highResolution.getX() - highResolutionPixelBounds.x) * pixelBounds.width
                / highResolutionPixelBounds.width));
        final var y = pixelBounds.y
                + (int) Math.round(((highResolution.getY() - highResolutionPixelBounds.y) * pixelBounds.height
                / highResolutionPixelBounds.height));
        return new Point2D.Double(x, y);
    }
}
