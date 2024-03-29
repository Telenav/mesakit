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

package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Stroke;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.graphics.drawing.surfaces.java2d.Java2dShapes;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

/**
 * @author jonathanl (shibo)
 */
public class MapBox extends LabeledMapShape
{
    public static MapBox box(Style style, Rectangle box, String label)
    {
        return new MapBox(style, box, label);
    }

    public static MapBox box(Style style)
    {
        return new MapBox(style, null, null);
    }

    public static MapBox box()
    {
        return box(null, null, null);
    }

    private Size size;

    protected MapBox(Style style, Rectangle box, String label)
    {
        super(style, box == null ? null : box.topLeft(), label);
        if (box != null)
        {
            size = box.size();
        }
    }

    protected MapBox(MapBox that)
    {
        super(that);
        size = that.size;
    }

    @Override
    public Rectangle bounds()
    {
        return location().rectangle(size);
    }

    @Override
    public MapBox copy()
    {
        return new MapBox(this);
    }

    @Override
    public Shape onDraw(MapCanvas canvas)
    {
        var box = Box.box()
                .withLocation(canvas.toDrawing(location()))
                .withStyle(style())
                .withSize(canvas.toDrawing(size))
                .draw(canvas);

        var label = super.onDraw(canvas);

        return Java2dShapes.combine(box, label);
    }

    @Override
    public MapBox scaledBy(double scaleFactor)
    {
        return withSize(size.scaledBy(scaleFactor, scaleFactor));
    }

    public Size size()
    {
        return size;
    }

    @Override
    public MapBox withColors(Style style)
    {
        return (MapBox) super.withColors(style);
    }

    @Override
    public MapBox withDrawColor(Color color)
    {
        return (MapBox) super.withDrawColor(color);
    }

    @Override
    public MapBox withDrawStroke(Stroke stroke)
    {
        return (MapBox) super.withDrawStroke(stroke);
    }

    @Override
    public MapBox withDrawStrokeWidth(DrawingWidth width)
    {
        return (MapBox) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapBox withDrawStrokeWidth(Distance width)
    {
        return (MapBox) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapBox withFillColor(Color color)
    {
        return (MapBox) super.withFillColor(color);
    }

    @Override
    public MapBox withFillStroke(Stroke stroke)
    {
        return (MapBox) super.withFillStroke(stroke);
    }

    @Override
    public MapBox withFillStrokeWidth(Distance width)
    {
        return (MapBox) super.withFillStrokeWidth(width);
    }

    @Override
    public MapBox withFillStrokeWidth(DrawingWidth width)
    {
        return (MapBox) super.withFillStrokeWidth(width);
    }

    @Override
    public MapBox withLabelStyle(Style style)
    {
        ensureNotNull(style);
        return (MapBox) super.withLabelStyle(style);
    }

    @Override
    public MapBox withLabelText(String label)
    {
        return (MapBox) super.withLabelText(label);
    }

    @Override
    public MapBox withLocation(DrawingPoint at)
    {
        return (MapBox) super.withLocation(at);
    }

    @Override
    public MapBox withLocation(Location at)
    {
        return (MapBox) super.withLocation(at);
    }

    @Override
    public MapBox withMargin(int margin)
    {
        return (MapBox) super.withMargin(margin);
    }

    @Override
    public MapBox withOffset(int dx, int dy)
    {
        return (MapBox) super.withOffset(dx, dy);
    }

    public MapBox withRectangle(Rectangle rectangle)
    {
        return copy()
                .withLocation(rectangle.topLeft())
                .withSize(rectangle.size());
    }

    @Override
    public MapBox withRoundedLabelCorners(DrawingLength corner)
    {
        return (MapBox) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapBox withRoundedLabelCorners(DrawingWidth cornerWidth,
                                          DrawingHeight cornerHeight)
    {
        return (MapBox) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
    }

    public MapBox withSize(Size size)
    {
        var copy = copy();
        copy.size = size;
        return copy;
    }

    @Override
    public MapBox withStyle(Style style)
    {
        return (MapBox) super.withStyle(style);
    }

    @Override
    public MapBox withTextColor(Color color)
    {
        return (MapBox) super.withTextColor(color);
    }
}
