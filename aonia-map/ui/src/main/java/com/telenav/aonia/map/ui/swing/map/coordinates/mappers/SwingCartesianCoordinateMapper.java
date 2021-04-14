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

package com.telenav.aonia.map.ui.swing.map.coordinates.mappers;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;

import java.awt.geom.Point2D;

/**
 * Does a simple linear Cartesian mapping.
 *
 * @author jonathanl (shibo)
 */
public class SwingCartesianCoordinateMapper implements CoordinateMapper
{
    private final double componentLeft;

    private final double componentTop;

    private final double componentWidth;

    private final double componentHeight;

    private final double viewLeft;

    private final double viewTop;

    private final double viewBottom;

    private final double viewWidth;

    private final double viewHeight;

    public SwingCartesianCoordinateMapper(final Rectangle view, final java.awt.Rectangle bounds)
    {
        componentLeft = bounds.x;
        componentTop = bounds.y;
        componentWidth = bounds.width;
        componentHeight = bounds.height;
        viewLeft = view.left().asDegrees();
        viewTop = view.top().asDegrees();
        viewBottom = view.bottom().asDegrees();
        viewWidth = view.width().asDegrees();
        viewHeight = view.height().asDegrees();
    }

    @Override
    public Location locationForPoint(final Point2D point)
    {
        var latitude = viewBottom
                + (viewHeight * ((componentHeight - (point.getY() - componentTop)) / componentHeight));
        var longitude = viewLeft + (viewWidth * ((point.getX() - componentLeft) / componentWidth));
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

    @Override
    public Point2D pointForLocation(final Location location)
    {
        final var longitude = location.longitude().asDegrees();
        final var latitude = location.latitude().asDegrees();
        final var x = (int) (componentLeft + componentWidth * ((longitude - viewLeft) / viewWidth));
        final var y = (int) (componentTop + componentHeight * ((viewTop - latitude) / viewHeight));
        return new Point2D.Double(x, y);
    }
}
