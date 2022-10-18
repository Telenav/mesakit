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

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.value.level.Percent;
import com.telenav.kivakit.ui.desktop.graphics.drawing.Drawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.graphics.drawing.surfaces.java2d.Java2dShapes;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapPolyline extends LabeledMapShape
{
    public static MapPolyline polyline()
    {
        return polyline(null, null);
    }

    public static MapPolyline polyline(Style style, Polyline polyline)
    {
        return new MapPolyline(style, polyline);
    }

    public static MapPolyline polyline(Style style)
    {
        return new MapPolyline(style, null);
    }

    private Polyline polyline;

    private String startLabel;

    private String endLabel;

    private String centerLabel;

    private Drawable fromArrowHead;

    private Drawable toArrowHead;

    protected MapPolyline(Style style, Polyline polyline)
    {
        super(style, null, null);
        this.polyline = polyline;
    }

    protected MapPolyline(MapPolyline that)
    {
        super(that);
        startLabel = that.startLabel;
        centerLabel = that.centerLabel;
        endLabel = that.endLabel;
        polyline = that.polyline;
        fromArrowHead = that.fromArrowHead;
        toArrowHead = that.toArrowHead;
    }

    @Override
    public Rectangle bounds()
    {
        return polyline.bounds();
    }

    @Override
    public MapPolyline copy()
    {
        return new MapPolyline(this);
    }

    @Override
    public MapPolyline fattened(Percent percent)
    {
        return (MapPolyline) super.fattened(percent);
    }

    @Override
    public Shape onDraw(MapCanvas canvas)
    {
        var shapes = new ObjectList<Shape>();

        shapes.add(canvas.drawPolyline(style(), polyline));

        if (startLabel != null)
        {
            shapes.add(drawLabel(canvas, anchor(canvas, polyline.start())));
        }

        if (centerLabel != null)
        {
            shapes.add(drawLabel(canvas, anchor(canvas, polyline.midpoint())));
        }

        if (endLabel != null)
        {
            shapes.add(drawLabel(canvas, anchor(canvas, polyline.end())));
        }

        return Java2dShapes.combine(shapes);
    }

    @Override
    public MapPolyline scaledBy(double scaleFactor)
    {
        return unsupported();
    }

    public MapPolyline withCenterLabel(String text)
    {
        var copy = copy();
        copy.centerLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withColors(Style style)
    {
        return (MapPolyline) super.withColors(style);
    }

    @Override
    public MapPolyline withDrawColor(Color color)
    {
        return (MapPolyline) super.withDrawColor(color);
    }

    @Override
    public MapPolyline withDrawStrokeWidth(Distance width)
    {
        return (MapPolyline) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapPolyline withDrawStrokeWidth(DrawingWidth width)
    {
        return (MapPolyline) super.withDrawStrokeWidth(width);
    }

    public MapPolyline withEndLabel(String text)
    {
        var copy = copy();
        copy.endLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withFillColor(Color color)
    {
        return (MapPolyline) super.withFillColor(color);
    }

    @Override
    public MapPolyline withFillStrokeWidth(Distance width)
    {
        return (MapPolyline) super.withFillStrokeWidth(width);
    }

    @Override
    public MapPolyline withFillStrokeWidth(DrawingWidth width)
    {
        return (MapPolyline) super.withFillStrokeWidth(width);
    }

    public MapPolyline withFromArrowHead(Drawable arrowHead)
    {
        var copy = copy();
        copy.fromArrowHead = arrowHead;
        return copy;
    }

    @Override
    public MapPolyline withLabelText(String label)
    {
        return (MapPolyline) super.withLabelText(label);
    }

    @Override
    public MapPolyline withLocation(DrawingPoint at)
    {
        return (MapPolyline) super.withLocation(at);
    }

    @Override
    public MapPolyline withMargin(int margin)
    {
        return (MapPolyline) super.withMargin(margin);
    }

    @Override
    public MapPolyline withOffset(int dx, int dy)
    {
        return (MapPolyline) super.withOffset(dx, dy);
    }

    public MapPolyline withPolyline(Polyline polyline)
    {
        var copy = copy();
        copy.polyline = polyline;
        return copy;
    }

    @Override
    public MapPolyline withRoundedLabelCorners(DrawingLength corner)
    {
        return (MapPolyline) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapPolyline withRoundedLabelCorners(DrawingWidth cornerWidth,
                                               DrawingHeight cornerHeight)
    {
        return (MapPolyline) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
    }

    public MapPolyline withStartLabel(String text)
    {
        var copy = copy();
        copy.startLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withStyle(Style style)
    {
        return (MapPolyline) super.withStyle(style);
    }

    @Override
    public MapPolyline withTextColor(Color color)
    {
        return (MapPolyline) super.withTextColor(color);
    }

    public MapPolyline withToArrowHead(Drawable arrowHead)
    {
        var copy = copy();
        copy.toArrowHead = arrowHead;
        return copy;
    }
}
