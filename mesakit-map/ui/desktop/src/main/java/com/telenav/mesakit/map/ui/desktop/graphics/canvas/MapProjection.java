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

    default CoordinateSize toCoordinates(final Size size)
    {
        return toCoordinates(size.asLocation()).asSize();
    }

    default CoordinateWidth toCoordinates(final Width width)
    {
        return toCoordinates(width.asLocation()).asSize().width();
    }

    default CoordinateDistance toCoordinates(final Distance distance)
    {
        return toCoordinates(Width.degrees(distance.asDegrees()));
    }

    default CoordinateHeight toCoordinates(final Height height)
    {
        return toCoordinates(height.asLocation()).asSize().height();
    }

    default Size toMapUnits(final CoordinateSize size)
    {
        return Size.of(toMapUnits(size.width()), toMapUnits(size.height()));
    }

    default Height toMapUnits(final CoordinateHeight height)
    {
        return toMapUnits(height.asCoordinate()).asHeight();
    }

    default Width toMapUnits(final CoordinateWidth width)
    {
        return toMapUnits(width.asCoordinate()).asWidth();
    }

    Location toMapUnits(Coordinate point);

    default Rectangle toMapUnits(final CoordinateRectangle rectangle)
    {
        return Rectangle.fromLocations(
                toMapUnits(rectangle.at()),
                toMapUnits(rectangle.to()));
    }
}
