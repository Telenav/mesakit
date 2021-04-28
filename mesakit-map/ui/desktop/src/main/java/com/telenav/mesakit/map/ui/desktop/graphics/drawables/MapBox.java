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

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.desktop.graphics.drawing.awt.AwtShapes;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public class MapBox extends LabeledMapShape
{
    public static MapBox box(final Style style, final Rectangle box, final String label)
    {
        return new MapBox(style, box, label);
    }

    public static MapBox box(final Style style)
    {
        return new MapBox(style, null, null);
    }

    public static MapBox box()
    {
        return box(null, null, null);
    }

    private final Size size;

    protected MapBox(final Style style, final Rectangle box, final String label)
    {
        super(style, box.topLeft(), label);
        size = box.size();
    }

    protected MapBox(final MapBox that)
    {
        super(that);
        size = that.size;
    }

    @Override
    public MapBox at(final Coordinate at)
    {
        return (MapBox) super.at(at);
    }

    @Override
    public MapBox atLocation(final Location at)
    {
        return (MapBox) super.atLocation(at);
    }

    @Override
    public MapBox copy()
    {
        return new MapBox(this);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var box = box(canvas.toCoordinates(size))
                .at(canvas.toCoordinates(atLocation()))
                .draw(canvas);

        final var label = super.onDraw(canvas);

        return AwtShapes.combine(box, label);
    }

    @Override
    public MapBox scaled(final double scaleFactor)
    {
        return withSize(size.scaledBy(scaleFactor, scaleFactor));
    }

    @Override
    public MapBox withColors(final Style style)
    {
        return (MapBox) super.withColors(style);
    }

    @Override
    public MapBox withDrawColor(final Color color)
    {
        return (MapBox) super.withDrawColor(color);
    }

    @Override
    public MapBox withDrawStrokeWidth(final DrawingDistance width)
    {
        return (MapBox) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapBox withDrawStrokeWidth(final Distance width)
    {
        return (MapBox) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapBox withFillColor(final Color color)
    {
        return (MapBox) super.withFillColor(color);
    }

    @Override
    public MapBox withFillStrokeWidth(final Distance width)
    {
        return (MapBox) super.withFillStrokeWidth(width);
    }

    @Override
    public MapBox withFillStrokeWidth(final DrawingDistance width)
    {
        return (MapBox) super.withFillStrokeWidth(width);
    }

    @Override
    public MapBox withLabel(final String label)
    {
        return (MapBox) super.withLabel(label);
    }

    @Override
    public MapBox withMargin(final int margin)
    {
        return (MapBox) super.withMargin(margin);
    }

    @Override
    public MapBox withOffset(final int dx, final int dy)
    {
        return (MapBox) super.withOffset(dx, dy);
    }

    public MapBox withRectangle(final Rectangle rectangle)
    {
        return copy()
                .atLocation(rectangle.topLeft())
                .withSize(rectangle.size());
    }

    public MapBox withSize(final Size size)
    {
        return copy().withSize(size);
    }

    @Override
    public MapBox withStyle(final Style style)
    {
        return (MapBox) super.withStyle(style);
    }

    @Override
    public MapBox withTextColor(final Color color)
    {
        return (MapBox) super.withTextColor(color);
    }
}
