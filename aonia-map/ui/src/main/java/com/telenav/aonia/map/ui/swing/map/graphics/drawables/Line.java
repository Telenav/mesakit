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

package com.telenav.aonia.map.ui.swing.map.graphics.drawables;

import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.MapCanvas;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Stroke;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Style;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.ui.swing.graphics.color.Color;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

public class Line extends Drawable
{
    private Width width;

    private Width outlineWidth;

    private Style outlineStyle;

    private Arrow fromArrow;

    private Arrow toArrow;

    public Line()
    {
    }

    public Line(final Width width, final Style style, final Width outlineWidth, final Style outlineStyle)
    {
        super(style);
        this.width = ensureNotNull(width);
        this.outlineWidth = ensureNotNull(outlineWidth);
        this.outlineStyle = ensureNotNull(outlineStyle);
    }

    private Line(final Line that)
    {
        super(that);
        width = that.width;
        outlineWidth = that.outlineWidth;
        outlineStyle = that.outlineStyle;
        fromArrow = that.fromArrow;
        toArrow = that.toArrow;
    }

    public Width arrowWidth()
    {
        return width.times(Percent._50);
    }

    public Line darkened()
    {
        return withStyle(style().darkened());
    }

    public Line darkened(final Percent percent)
    {
        return withStyle(style().darkened(percent));
    }

    @SuppressWarnings("ConstantConditions")
    public Shape draw(final MapCanvas canvas, final Polyline line)
    {
        ensure(canvas != null);
        ensure(line != null);

        // Draw line
        final Shape shape;
        if (line.size() == 1)
        {
            final var dot = new Dot(width, style(), outlineWidth, outlineStyle);
            shape = dot.draw(canvas, line.start());
        }
        else
        {
            shape = canvas.drawPolyline(style(), width, outlineStyle, outlineWidth, line);
        }

        // Draw any arrows at the ends
        if (fromArrow != null)
        {
            fromArrow.withWidth(arrowWidth()).draw(canvas, line.start(), line.firstSegment().heading());
        }
        if (toArrow != null)
        {
            toArrow.withWidth(arrowWidth()).draw(canvas, line.end(), line.lastSegment().heading());
        }

        return shape;
    }

    public Line fattened(final Percent percent)
    {
        return withWidth(width.fattened(percent))
                .withOutlineWidth(outlineWidth.fattened(percent));
    }

    public Line lightened()
    {
        return withStyle(style().lightened());
    }

    public Line lightened(final Percent percent)
    {
        return withStyle(style().lightened(percent));
    }

    public Line withColor(final Color color)
    {
        return withFill(color).withDrawColor(color.darkened(Percent.of(20)));
    }

    public Line withDrawColor(final Color color)
    {
        return withStyle(style().withDrawColor(color));
    }

    public Line withDrawStroke(final Stroke stroke)
    {
        final var copy = new Line(this);
        copy.style(style().withDrawStroke(stroke));
        return copy;
    }

    public Line withFill(final Color color)
    {
        return withStyle(style().withFillColor(color));
    }

    public Line withFillStroke(final Stroke stroke)
    {
        final var copy = new Line(this);
        copy.style(style().withFillStroke(stroke));
        return copy;
    }

    public Line withFromArrow(final Arrow arrow)
    {
        final var copy = new Line(this);
        copy.fromArrow = arrow;
        return copy;
    }

    public Line withOutlineStyle(final Style style)
    {
        final var copy = new Line(this);
        copy.outlineStyle = style;
        return copy;
    }

    public Line withOutlineWidth(final Width width)
    {
        final var copy = new Line(this);
        copy.outlineWidth = width;
        return copy;
    }

    public Line withStyle(final Style style)
    {
        final var copy = new Line(this);
        copy.style(style);
        return copy;
    }

    public Line withToArrow(final Arrow arrow)
    {
        final var copy = new Line(this);
        copy.toArrow = arrow;
        return copy;
    }

    public Line withWidth(final Width width)
    {
        final var copy = new Line(this);
        copy.width = width;
        return copy;
    }
}
