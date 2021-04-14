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

package com.telenav.aonia.map.ui.swing.map.graphics.canvas;

import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;
import com.telenav.kivakit.ui.swing.graphics.color.Color;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public class Style
{
    // Filling
    private Color fill;

    private Stroke fillStroke;

    // Drawing
    private Color draw;

    private Stroke drawStroke;

    // Text
    private Color text;

    private Font font;

    public Style()
    {
    }

    private Style(final Style that)
    {
        fill = that.fill;
        fillStroke = that.fillStroke;
        draw = that.draw;
        drawStroke = that.drawStroke;
        text = that.text;
        font = that.font;
    }

    public void applyDraw(final MapCanvas canvas, final Width width)
    {

        ensure(draw != null);
        ensure(drawStroke != null);

        canvas.graphics().setColor(draw.asAwtColor());
        drawStroke.apply(canvas, width);
    }

    public void applyFill(final MapCanvas canvas, final Width width)
    {

        ensure(fill != null);
        ensure(fillStroke != null);

        canvas.graphics().setColor(fill.asAwtColor());
        fillStroke.apply(canvas, width);
    }

    public Style darkened()
    {
        return darkened(Percent.of(15));
    }

    public Style darkened(final Percent percent)
    {
        return withFillColor(fill.darkened(percent)).withDrawColor(draw.darkened(percent));
    }

    public Color draw()
    {
        return draw;
    }

    @KivaKitIncludeProperty
    public Stroke drawStroke()
    {
        return drawStroke;
    }

    public Color fill()
    {
        return fill;
    }

    @KivaKitIncludeProperty
    public Stroke fillStroke()
    {
        return fillStroke;
    }

    @KivaKitIncludeProperty
    public Font font()
    {
        return font;
    }

    public FontMetrics fontMetrics(final MapCanvas canvas)
    {
        text(canvas);
        return canvas.graphics().getFontMetrics();
    }

    public Style lightened()
    {
        return lightened(Percent.of(15));
    }

    public Style lightened(final Percent percent)
    {
        return withFillColor(fill.lightened(percent)).withDrawColor(draw.lightened(percent));
    }

    public Shape shape(final MapCanvas canvas, final Shape shape)
    {
        return fillStroke.strokedShape(canvas, Width.pixels(1f), shape);
    }

    public Shape shape(final MapCanvas canvas, final Width width, final Shape shape)
    {
        return drawStroke.strokedShape(canvas, width, shape);
    }

    public void text(final MapCanvas canvas)
    {
        assert canvas != null;
        assert text != null;
        assert font != null;

        canvas.graphics().setColor(text.asAwtColor());
        canvas.graphics().setFont(font);
    }

    public Color text()
    {
        return text;
    }

    public Rectangle2D textBounds(final MapCanvas canvas, final String text)
    {
        return fontMetrics(canvas).getStringBounds(text, canvas.graphics());
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public Style withDrawColor(final Color color)
    {
        final var copy = new Style(this);
        copy.draw = color;
        return copy;
    }

    public Style withDrawStroke(final Stroke stroke)
    {
        final var copy = new Style(this);
        copy.drawStroke = stroke;
        return copy;
    }

    public Style withFillColor(final Color fill)
    {
        final var copy = new Style(this);
        copy.fill = fill;
        return copy;
    }

    public Style withFillStroke(final Stroke stroke)
    {
        final var copy = new Style(this);
        copy.fillStroke = stroke;
        return copy;
    }

    public Style withFont(final Font font)
    {
        final var copy = new Style(this);
        copy.font = font;
        return copy;
    }

    public Style withFontSize(final int size)
    {
        return withFont(new Font(font.getFontName(), font.getStyle(), size));
    }

    public Style withTextColor(final Color text)
    {
        final var copy = new Style(this);
        copy.text = text;
        return copy;
    }
}
