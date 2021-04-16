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
import com.telenav.mesakit.map.geography.shape.rectangle.Size;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Maps between AWT screen coordinates and map coordinates, as expressed by {@link Location}.
 *
 * @author jonathanl (shibo)
 */
public interface CoordinateMapper
{
    Location locationForPoint(Point2D point);

    Point2D pointForLocation(Location location);

    /**
     * @return The geographic rectangle for the given Swing rectangle
     */
    default Rectangle toMesaKitn(final java.awt.Rectangle rectangle)
    {
        final var upperLeft = locationForPoint(new Point(rectangle.x, rectangle.y));
        final var lowerRight = locationForPoint(new Point(rectangle.x + rectangle.width, rectangle.y + rectangle.height));
        return Rectangle.fromLocations(upperLeft, lowerRight);
    }

    default java.awt.Rectangle toAwt(final Size size)
    {
        final var topLeft = Rectangle.MAXIMUM.topLeft();
        final Point2D extent = pointForLocation(topLeft.offset(size));
        return new java.awt.Rectangle(0, 0, (int) extent.getX(), (int) extent.getY());
    }

    default java.awt.Rectangle toAwt(final Rectangle rectangle)
    {
        final Point2D topLeft = pointForLocation(rectangle.topLeft());
        final Point2D bottomRight = pointForLocation(rectangle.bottomRight());
        final var width = bottomRight.getX() - topLeft.getX();
        final var height = bottomRight.getY() - topLeft.getY();
        return new java.awt.Rectangle((int) Math.round(topLeft.getX()), (int) Math.round(topLeft.getY()),
                (int) Math.round(width), (int) Math.round(height));
    }

    default Rectangle2D toAwt2D(final Rectangle rectangle)
    {
        final Point2D topLeft = pointForLocation(rectangle.topLeft());
        final Point2D bottomRight = pointForLocation(rectangle.bottomRight());
        final var width = bottomRight.getX() - topLeft.getX();
        final var height = bottomRight.getY() - topLeft.getY();
        return new Rectangle2D.Double(topLeft.getX(), topLeft.getY(), width, height);
    }
}
