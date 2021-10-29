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

import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingRectangle;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * Maps between drawing coordinates, as expressed by <i>Drawing*</i> objects, and map coordinates, as expressed by
 * {@link Location}, {@link Latitude}, {@link Longitude}, {@link Rectangle} {@link Distance}, {@link Width}, {@link
 * Height} and {@link Size}. The projection area has an origin at 0, 0 in the upper left and an extent defined by {@link
 * #drawingSize()}
 *
 * @author jonathanl (shibo)
 */
public interface MapProjection
{
    /**
     * @return The drawing area in drawing coordinates in the coordinate system for this projection
     */
    DrawingSize drawingSize();

    /**
     * @return The map area as a {@link Rectangle} in latitude and longitude
     */
    Rectangle mapArea();

    /**
     * @return The given {@link Location} in the drawing area
     */
    DrawingPoint toDrawing(Location location);

    /**
     * @return The given {@link Rectangle} in the drawing area
     */
    default DrawingRectangle toDrawing(Rectangle rectangle)
    {
        return DrawingRectangle.rectangle(
                toDrawing(rectangle.topLeft()),
                toDrawing(rectangle.bottomRight()));
    }

    /**
     * @return The given {@link Width} in the drawing area
     */
    default DrawingWidth toDrawing(Width width)
    {
        return toDrawing(width.asSize()).width();
    }

    /**
     * @return The given {@link Distance} in the drawing area
     */
    default DrawingLength toDrawing(Distance distance)
    {
        return toDrawing(Width.degrees(distance.asDegrees()));
    }

    /**
     * @return The given {@link Height} in the drawing area
     */
    default DrawingHeight toDrawing(Height height)
    {
        return toDrawing(height.asSize()).height();
    }

    /**
     * @return The given {@link Size} in the drawing area
     */
    DrawingSize toDrawing(Size size);

    /**
     * @return The given drawing area size as a map {@link Size}
     */
    default Size toMap(DrawingSize size)
    {
        var width = toMap(size.width());
        var height = toMap(size.height());
        return width == null || height == null ? null : Size.of(width, height);
    }

    /**
     * @return The given drawing area height as a map {@link Height}
     */
    default Height toMap(DrawingHeight height)
    {
        var location = toMap(height.asPoint());
        return location == null ? null : location.asHeight();
    }

    /**
     * @return The given drawing area width as a map {@link Width}
     */
    default Width toMap(DrawingWidth width)
    {
        var location = toMap(width.asPoint());
        return location == null ? null : location.asWidth();
    }

    /**
     * @return The given drawing area length as a map {@link Distance}
     */
    default Distance toMap(DrawingLength length)
    {
        return Distance.degrees(toMap(length.asWidth()).asDegrees());
    }

    /**
     * @return The given drawing space coordinate as a map {@link Location}
     */
    Location toMap(DrawingPoint point);

    /**
     * @return The given rectangle in drawing coordinates as a map {@link Rectangle}
     */
    default Rectangle toMap(DrawingRectangle rectangle)
    {
        var at = toMap(rectangle.at());
        var to = toMap(rectangle.asPoint());
        return at == null || to == null ? null : Rectangle.fromLocations(at, to);
    }
}
