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

import com.telenav.kivakit.test.UnitTest;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;

public abstract class BaseCoordinateMapperTest extends UnitTest
{
    protected void checkMapping(final MapProjection projection,
                                final DrawingPoint point,
                                final Location location)
    {
        final var projectedPoint = projection.toDrawing(location);
        ensure(point.isClose(projectedPoint, 0.01), "");

        final var projectedLocation = projection.toMap(point);
        if (!location.isClose(projectedLocation, Angle.degrees(0.001)))
        {
            fail("location " + location + " is not close enough to " + projectedLocation);
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected DrawingSize drawingSize(final int width, final int height)
    {
        return DrawingSize.pixels(width, height);
    }

    protected DrawingPoint point(final int x, final int y)
    {
        return DrawingPoint.pixels(x, y);
    }
}
