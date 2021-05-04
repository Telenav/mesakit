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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections;

import com.telenav.kivakit.core.test.UnitTest;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;

public abstract class BaseCoordinateMapperTest extends UnitTest
{
    protected void checkMapping(final MapProjection projection,
                                final Point point,
                                final Location location)
    {
        final var projected = projection.toDrawing(location);
        ensureEqual(point, projected);
        final var mappedPoint = projection.toMap(point);
        if (!location.isClose(mappedPoint, Angle.degrees(0.001)))
        {
            fail("location " + location + " is not close enough to " + mappedPoint);
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected Rectangle drawingRectangle(final int x, final int y, final int width, final int height)
    {
        return Rectangle.pixels(x, y, width, height);
    }

    protected Point point(final int x, final int y)
    {
        return Point.pixels(x, y);
    }
}