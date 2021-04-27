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

import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.style.Shapes;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapPolyline extends LabeledMapShape
{
    public static MapPolyline polyline()
    {
        return polyline(null, null);
    }

    public static MapPolyline polyline(final Style style, final Polyline polyline)
    {
        return new MapPolyline(style, polyline);
    }

    public static MapPolyline polyline(final Style style)
    {
        return new MapPolyline(style, null);
    }

    private final Polyline polyline;

    private String startLabel;

    private String endLabel;

    private String centerLabel;

    protected MapPolyline(final Style style, final Polyline polyline)
    {
        super(style, null, null);
        this.polyline = polyline;
    }

    protected MapPolyline(final MapPolyline that)
    {
        super(that);
        startLabel = that.startLabel;
        centerLabel = that.centerLabel;
        endLabel = that.endLabel;
        polyline = that.polyline;
    }

    @Override
    public MapPolyline at(final Coordinate at)
    {
        return (MapPolyline) super.at(at);
    }

    @Override
    public MapPolyline copy()
    {
        return new MapPolyline(this);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var shapes = new ObjectList<Shape>();

        shapes.add(canvas.drawPolyline(style(), polyline));

        if (startLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.start())));
        }

        if (centerLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.midpoint())));
        }

        if (endLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.end())));
        }

        return Shapes.combine(shapes);
    }

    @Override
    public MapPolyline scaled(final double scaleFactor)
    {
        return unsupported();
    }

    public MapPolyline withCenterLabel(final String text)
    {
        final var copy = copy();
        copy.centerLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withColors(final Style style)
    {
        return (MapPolyline) super.withColors(style);
    }

    @Override
    public MapPolyline withDrawColor(final Color color)
    {
        return (MapPolyline) super.withDrawColor(color);
    }

    @Override
    public MapPolyline withDrawStrokeWidth(final Distance width)
    {
        return (MapPolyline) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapPolyline withDrawStrokeWidth(final DrawingDistance width)
    {
        return (MapPolyline) super.withDrawStrokeWidth(width);
    }

    public MapPolyline withEndLabel(final String text)
    {
        final var copy = copy();
        copy.endLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withFillColor(final Color color)
    {
        return (MapPolyline) super.withFillColor(color);
    }

    @Override
    public MapPolyline withFillStrokeWidth(final Distance width)
    {
        return (MapPolyline) super.withFillStrokeWidth(width);
    }

    @Override
    public MapPolyline withFillStrokeWidth(final DrawingDistance width)
    {
        return (MapPolyline) super.withFillStrokeWidth(width);
    }

    @Override
    public MapPolyline withLabel(final String label)
    {
        return (MapPolyline) super.withLabel(label);
    }

    @Override
    public MapPolyline withMargin(final int margin)
    {
        return (MapPolyline) super.withMargin(margin);
    }

    @Override
    public MapPolyline withOffset(final int dx, final int dy)
    {
        return (MapPolyline) super.withOffset(dx, dy);
    }

    public MapPolyline withStartLabel(final String text)
    {
        final var copy = copy();
        copy.startLabel = text;
        return copy;
    }

    @Override
    public MapPolyline withStyle(final Style style)
    {
        return (MapPolyline) super.withStyle(style);
    }

    @Override
    public MapPolyline withTextColor(final Color color)
    {
        return (MapPolyline) super.withTextColor(color);
    }
}
