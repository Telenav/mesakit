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

package com.telenav.mesakit.map.ui.desktop.coordinates;

import com.telenav.kivakit.core.test.UnitTest;
import com.telenav.kivakit.ui.swing.graphics.drawing.awt.Points;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Angle;

import java.awt.geom.Point2D;

public abstract class BaseCoordinateMapperTest extends UnitTest
{
    @SuppressWarnings("SameParameterValue")
    protected java.awt.Rectangle awtRectangle(final int x, final int y, final int width, final int height)
    {
        return new java.awt.Rectangle(x, y, width, height);
    }

    protected void checkMapping(final MapCoordinateMapper mapper, final Point2D point, final Location location)
    {
        final var mappedLocation = mapper.toDrawingPoint(location);
        ensureEqual(Points.to2d(point), Points.to2d(mappedLocation));
        final var mappedPoint = mapper.toMapLocation(point);
        if (!location.isClose(mappedPoint, Angle.degrees(0.001)))
        {
            fail("location " + location + " is not close enough to " + mappedPoint);
        }
    }

    protected Point2D point(final int x, final int y)
    {
        return new Point2D.Double(x, y);
    }
}
