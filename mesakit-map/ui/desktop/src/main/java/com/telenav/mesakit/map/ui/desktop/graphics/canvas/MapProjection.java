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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas;

import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateDistance;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateHeight;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateRectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSize;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateWidth;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Maps between {@link CoordinateSystem} coordinates and map coordinates, as expressed by {@link Location}, {@link
 * Latitude} and {@link Longitude}.
 *
 * @author jonathanl (shibo)
 */
public interface MapProjection
{
    CoordinateRectangle coordinateArea();

    Rectangle mapArea();

    Coordinate toCoordinates(Location location);

    default CoordinateRectangle toCoordinates(final Rectangle rectangle)
    {
        return CoordinateRectangle.rectangle(
                toCoordinates(rectangle.topLeft()),
                toCoordinates(rectangle.bottomRight()));
    }

    default CoordinateWidth toCoordinates(final Width width)
    {
        return toCoordinates(width.asSize()).width();
    }

    default CoordinateDistance toCoordinates(final Distance distance)
    {
        return toCoordinates(Width.degrees(distance.asDegrees()));
    }

    default CoordinateHeight toCoordinates(final Height height)
    {
        return toCoordinates(height.asSize()).height();
    }

    default CoordinateSize toCoordinates(final Size size)
    {
        // Convert width and height to a location relative to the top left,
        final var at = Location.degrees(
                90 - size.height().asDegrees(),
                -180 + size.width().asDegrees());

        // project that to coordinate space, and return it as a size.
        return toCoordinates(at).asSize();
    }

    default Size toMapUnits(final CoordinateSize size)
    {
        final var width = toMapUnits(size.width());
        final var height = toMapUnits(size.height());
        return width == null || height == null ? null : Size.of(width, height);
    }

    default Height toMapUnits(final CoordinateHeight height)
    {
        final var location = toMapUnits(height.asCoordinate());
        return location == null ? null : location.asHeight();
    }

    default Width toMapUnits(final CoordinateWidth width)
    {
        final var location = toMapUnits(width.asCoordinate());
        return location == null ? null : location.asWidth();
    }

    Location toMapUnits(Coordinate point);

    default Rectangle toMapUnits(final CoordinateRectangle rectangle)
    {
        final var at = toMapUnits(rectangle.at());
        final var to = toMapUnits(rectangle.to());
        return at == null || to == null ? null : Rectangle.fromLocations(at, to);
    }
}
