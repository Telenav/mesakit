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

import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Dot;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.kivakit.ui.desktop.graphics.drawing.surfaces.java2d.Java2dShapes;
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

    private DrawingLength coordinateRadius;

    protected MapDot(final Style style, final Location at, final Distance radius, final String label)
    {
        super(style, at, label);
        this.radius = radius;
    }

    protected MapDot(final MapDot that)
    {
        super(that);
        radius = that.radius;
        coordinateRadius = that.coordinateRadius;
    }

    @Override
    public MapDot at(final DrawingPoint at)
    {
        return (MapDot) super.at(at);
    }

    /**
     * Note that this method cannot be called until this drawable has been drawn
     *
     * @return The bounds of this dot on the world map
     */
    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromCenterAndRadius(location(), radius);
    }

    @Override
    public MapDot copy()
    {
        return new MapDot(this);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var radius = coordinateRadius != null
                ? coordinateRadius
                : canvas.toDrawing(this.radius);

        final var dot = Dot.dot()
                .withRadius(radius)
                .at(at())
                .draw(canvas);

        final var label = super.onDraw(canvas);

        return Java2dShapes.combine(dot, label);
    }

    @Override
    public MapDot scaledBy(final double scaleFactor)
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
    public MapDot withDrawStrokeWidth(final DrawingWidth width)
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
    public MapDot withFillStrokeWidth(final DrawingWidth width)
    {
        return (MapDot) super.withFillStrokeWidth(width);
    }

    @Override
    public MapDot withLabel(final String label)
    {
        return (MapDot) super.withLabel(label);
    }

    @Override
    public MapDot withLocation(final Location at)
    {
        return (MapDot) super.withLocation(at);
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

    public MapDot withRadius(final DrawingLength radius)
    {
        final var copy = copy();
        copy.coordinateRadius = radius;
        return copy;
    }

    @Override
    public MapDot withRoundedLabelCorners(final DrawingLength corner)
    {
        return (MapDot) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapDot withRoundedLabelCorners(final DrawingWidth cornerWidth,
                                          final DrawingHeight cornerHeight)
    {
        return (MapDot) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
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
