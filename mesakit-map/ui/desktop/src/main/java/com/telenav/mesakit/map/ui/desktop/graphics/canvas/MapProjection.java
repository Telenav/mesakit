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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas;

import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Height;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Width;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Maps between {@link CoordinateSystem} coordinates and map coordinates, as expressed by {@link Location}, {@link
 * Latitude} and {@link Longitude}.
 *
 * @author jonathanl (shibo)
 */
public interface MapProjection
{
    /**
     * @return The drawing area in coordinate space
     */
    Rectangle drawingArea();

    /**
     * @return The map area as a {@link com.telenav.mesakit.map.geography.shape.rectangle.Rectangle} in latitude and
     * longitude
     */
    com.telenav.mesakit.map.geography.shape.rectangle.Rectangle mapArea();

    /**
     * @return The given {@link Location} in the drawing area
     */
    Point toDrawing(Location location);

    /**
     * @return The given {@link com.telenav.mesakit.map.geography.shape.rectangle.Rectangle} in the drawing area
     */
    default Rectangle toDrawing(final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle rectangle)
    {
        return Rectangle.rectangle(
                toDrawing(rectangle.topLeft()),
                toDrawing(rectangle.bottomRight()));
    }

    /**
     * @return The given {@link com.telenav.mesakit.map.geography.shape.rectangle.Width} in the drawing area
     */
    default Width toDrawing(final com.telenav.mesakit.map.geography.shape.rectangle.Width width)
    {
        return toDrawing(width.asSize()).width();
    }

    /**
     * @return The given {@link Distance} in the drawing area
     */
    default Length toDrawing(final Distance distance)
    {
        return toDrawing(com.telenav.mesakit.map.geography.shape.rectangle.Width.degrees(distance.asDegrees()));
    }

    /**
     * @return The given {@link com.telenav.mesakit.map.geography.shape.rectangle.Height} in the drawing area
     */
    default Height toDrawing(final com.telenav.mesakit.map.geography.shape.rectangle.Height height)
    {
        return toDrawing(height.asSize()).height();
    }

    /**
     * @return The given {@link com.telenav.mesakit.map.geography.shape.rectangle.Size} in the drawing area
     */
    default Size toDrawing(final com.telenav.mesakit.map.geography.shape.rectangle.Size size)
    {
        // Convert width and height to a location relative to the top left,
        final var at = Location.degrees(
                90 - size.height().asDegrees(),
                -180 + size.width().asDegrees());

        // project that to coordinate space, and return it as a size.
        return toDrawing(at).minus(drawingArea().topLeft()).asSize();
    }

    /**
     * @return The given drawing area size as a map {@link com.telenav.mesakit.map.geography.shape.rectangle.Size}
     */
    default com.telenav.mesakit.map.geography.shape.rectangle.Size toMap(final Size size)
    {
        final var width = toMap(size.width());
        final var height = toMap(size.height());
        return width == null || height == null ? null : com.telenav.mesakit.map.geography.shape.rectangle.Size.of(width, height);
    }

    /**
     * @return The given drawing area height as a map {@link com.telenav.mesakit.map.geography.shape.rectangle.Height}
     */
    default com.telenav.mesakit.map.geography.shape.rectangle.Height toMap(final Height height)
    {
        final var location = toMap(height.asCoordinate());
        return location == null ? null : location.asHeight();
    }

    /**
     * @return The given drawing area width as a map {@link com.telenav.mesakit.map.geography.shape.rectangle.Width}
     */
    default com.telenav.mesakit.map.geography.shape.rectangle.Width toMap(final Width width)
    {
        final var location = toMap(width.asCoordinate());
        return location == null ? null : location.asWidth();
    }

    /**
     * @return The given drawing space coordinate as a map {@link Location}
     */
    Location toMap(Point coordinate);

    /**
     * @return The given rectangle in drawing coordinates as a map {@link com.telenav.mesakit.map.geography.shape.rectangle.Rectangle}
     */
    default com.telenav.mesakit.map.geography.shape.rectangle.Rectangle toMap(final Rectangle rectangle)
    {
        final var at = toMap(rectangle.at());
        final var to = toMap(rectangle.to());
        return at == null || to == null ? null : com.telenav.mesakit.map.geography.shape.rectangle.Rectangle.fromLocations(at, to);
    }
}
