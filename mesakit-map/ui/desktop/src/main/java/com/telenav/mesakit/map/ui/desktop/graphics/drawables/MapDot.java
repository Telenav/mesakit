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
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapDot extends LabeledMapShape
{
    public static MapDot dot()
    {
        return dot(null, null, null, null);
    }

    public static MapDot dot(final Style style, final Location at, final Distance radius, final String label)
    {
        return new MapDot(style, at, radius, label);
    }

    public static MapDot dot(final Style style)
    {
        return new MapDot(style, null, null, null);
    }

    private Distance radius;

    private DrawingDistance drawingRadius;

    protected MapDot(final Style style, final Location at, final Distance radius, final String label)
    {
        super(style, at, label);
        this.radius = radius;
    }

    protected MapDot(final MapDot that)
    {
        super(that);
        radius = that.radius;
        drawingRadius = that.drawingRadius;
    }

    @Override
    public MapDot at(final Coordinate at)
    {
        return (MapDot) super.at(at);
    }

    @Override
    public MapDot atLocation(final Location at)
    {
        return (MapDot) super.atLocation(at);
    }

    /**
     * Note that this method cannot be called until this drawable has been drawn
     *
     * @return The bounds of this dot on the world map
     */
    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromCenterAndRadius(atLocation(), radius);
    }

    @Override
    public MapDot copy()
    {
        return new MapDot(this);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        if (radius == null)
        {
            radius = canvas.toMap(drawingRadius);
        }

        if (drawingRadius == null)
        {
            drawingRadius = canvas.toDrawingUnits(radius);
        }

        final var dot = dot()
                .withRadius(drawingRadius)
                .at(canvas.toCoordinates(atLocation()))
                .draw(canvas);

        final var label = super.onDraw(canvas);

        return AwtShapes.combine(dot, label);
    }

    @Override
    public MapDot scaled(final double scaleFactor)
    {
        return unsupported();
    }

    @Override
    public MapDot withColors(final Style style)
    {
        return (MapDot) super.withColors(style);
    }

    @Override
    public MapDot withDrawColor(final Color color)
    {
        return (MapDot) super.withDrawColor(color);
    }

    @Override
    public MapDot withDrawStrokeWidth(final DrawingDistance width)
    {
        return (MapDot) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapDot withDrawStrokeWidth(final Distance width)
    {
        return (MapDot) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapDot withFillColor(final Color color)
    {
        return (MapDot) super.withFillColor(color);
    }

    @Override
    public MapDot withFillStrokeWidth(final Distance width)
    {
        return (MapDot) super.withFillStrokeWidth(width);
    }

    @Override
    public MapDot withFillStrokeWidth(final DrawingDistance width)
    {
        return (MapDot) super.withFillStrokeWidth(width);
    }

    @Override
    public MapDot withLabel(final String label)
    {
        return (MapDot) super.withLabel(label);
    }

    @Override
    public MapDot withMargin(final int margin)
    {
        return (MapDot) super.withMargin(margin);
    }

    @Override
    public MapDot withOffset(final int dx, final int dy)
    {
        return (MapDot) super.withOffset(dx, dy);
    }

    public MapDot withRadius(final Distance radius)
    {
        final var copy = copy();
        copy.radius = radius;
        return copy;
    }

    public MapDot withRadius(final DrawingDistance radius)
    {
        final var copy = copy();
        copy.drawingRadius = drawingRadius;
        return copy;
    }

    @Override
    public MapDot withStyle(final Style style)
    {
        return (MapDot) super.withStyle(style);
    }

    @Override
    public MapDot withTextColor(final Color color)
    {
        return (MapDot) super.withTextColor(color);
    }
}
