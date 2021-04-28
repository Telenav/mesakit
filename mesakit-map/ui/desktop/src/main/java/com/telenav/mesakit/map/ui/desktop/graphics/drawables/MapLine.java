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

import com.telenav.kivakit.ui.desktop.graphics.drawing.Drawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.desktop.graphics.drawing.awt.AwtShapes;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.style.Stroke;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapLine extends LabeledMapShape
{
    public static MapLine line()
    {
        return line(null);
    }

    public static MapLine line(final Style style)
    {
        return new MapLine(style, null, null, null);
    }

    public static MapLine line(final Style style, final Location from, final Location to, final String label)
    {
        return new MapLine(style, from, to, label);
    }

    private Drawable fromArrowHead;

    private Drawable toArrowHead;

    private Location to;

    private final String label;

    protected MapLine(final Style style, final Location at, final Location to, final String label)
    {
        super(style, at, label);

        this.to = to;
        this.label = label;
    }

    protected MapLine(final MapLine that)
    {
        super(that);

        to = that.to;
        label = that.label;
        fromArrowHead = that.fromArrowHead;
        toArrowHead = that.toArrowHead;
    }

    @Override
    public MapLine at(final Coordinate at)
    {
        return (MapLine) super.at(at);
    }

    @Override
    public MapLine copy()
    {
        return new MapLine(this);
    }

    public Location from()
    {
        return location();
    }

    @Override
    public MapLine location(final Location at)
    {
        return (MapLine) super.location(at);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var line = line(style(), from(), to, label)
                .withFromArrowHead(fromArrowHead)
                .withToArrowHead(toArrowHead)
                .at(canvas.toCoordinates(location()))
                .draw(canvas);

        return AwtShapes.combine(line, super.onDraw(canvas));
    }

    @Override
    public MapLine scaled(final double scaleFactor)
    {
        return unsupported();
    }

    @Override
    public MapLine withColors(final Style style)
    {
        return (MapLine) super.withColors(style);
    }

    @Override
    public MapLine withDrawColor(final Color color)
    {
        return (MapLine) super.withDrawColor(color);
    }

    @Override
    public MapLine withDrawStrokeWidth(final Distance width)
    {
        return (MapLine) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLine withDrawStrokeWidth(final DrawingDistance width)
    {
        return (MapLine) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLine withFillColor(final Color color)
    {
        return (MapLine) super.withFillColor(color);
    }

    @Override
    public MapLine withFillStroke(final Stroke stroke)
    {
        return (MapLine) super.withFillStroke(stroke);
    }

    @Override
    public MapLine withFillStrokeWidth(final Distance width)
    {
        return (MapLine) super.withFillStrokeWidth(width);
    }

    @Override
    public MapLine withFillStrokeWidth(final DrawingDistance width)
    {
        return (MapLine) super.withFillStrokeWidth(width);
    }

    public MapLine withFrom(final Location from)
    {
        return location(from);
    }

    public MapLine withFromArrowHead(final Drawable arrowHead)
    {
        final var copy = copy();
        copy.fromArrowHead = arrowHead;
        return copy;
    }

    @Override
    public MapLine withLabel(final String label)
    {
        return (MapLine) super.withLabel(label);
    }

    @Override
    public MapLine withMargin(final int margin)
    {
        return (MapLine) super.withMargin(margin);
    }

    @Override
    public MapLine withOffset(final int dx, final int dy)
    {
        return (MapLine) super.withOffset(dx, dy);
    }

    @Override
    public MapLine withStyle(final Style style)
    {
        return (MapLine) super.withStyle(style);
    }

    @Override
    public MapLine withTextColor(final Color color)
    {
        return (MapLine) super.withTextColor(color);
    }

    public MapLine withTo(final Location to)
    {
        final var copy = copy();
        this.to = to;
        return copy;
    }

    public MapLine withToArrowHead(final Drawable arrowHead)
    {
        final var copy = copy();
        copy.toArrowHead = arrowHead;
        return copy;
    }
}
