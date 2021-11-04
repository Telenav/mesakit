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

import com.telenav.kivakit.kernel.language.values.level.Percent;
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

/**
 * @author jonathanl (shibo)
 */
public class MapDot extends LabeledMapShape
{
    public static MapDot dot()
    {
        return dot(null, null, null, null);
    }

    public static MapDot dot(Style style, Location at, Distance radius, String label)
    {
        return new MapDot(style, at, radius, label);
    }

    public static MapDot dot(Style style)
    {
        return new MapDot(style, null, null, null);
    }

    private Distance radius;

    private DrawingLength coordinateRadius;

    protected MapDot(Style style, Location at, Distance radius, String label)
    {
        super(style, at, label);
        this.radius = radius;
    }

    protected MapDot(MapDot that)
    {
        super(that);
        radius = that.radius;
        coordinateRadius = that.coordinateRadius;
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
    public Shape onDraw(MapCanvas canvas)
    {
        var radius = coordinateRadius != null
                ? coordinateRadius
                : canvas.toDrawing(this.radius);

        var dot = Dot.dot()
                .withRadius(radius)
                .withLocation(withLocation())
                .draw(canvas);

        var label = super.onDraw(canvas);

        return Java2dShapes.combine(dot, label);
    }

    @Override
    public MapDot scaledBy(Percent scaleFactor)
    {
        return copy().withRadius(radius.times(scaleFactor));
    }

    @Override
    public MapDot scaledBy(double scaleFactor)
    {
        return copy().withRadius(radius.times(scaleFactor));
    }

    @Override
    public MapDot withColors(Style style)
    {
        return (MapDot) super.withColors(style);
    }

    @Override
    public MapDot withDrawColor(Color color)
    {
        return (MapDot) super.withDrawColor(color);
    }

    @Override
    public MapDot withDrawStrokeWidth(DrawingWidth width)
    {
        return (MapDot) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapDot withDrawStrokeWidth(Distance width)
    {
        return (MapDot) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapDot withFillColor(Color color)
    {
        return (MapDot) super.withFillColor(color);
    }

    @Override
    public MapDot withFillStrokeWidth(Distance width)
    {
        return (MapDot) super.withFillStrokeWidth(width);
    }

    @Override
    public MapDot withFillStrokeWidth(DrawingWidth width)
    {
        return (MapDot) super.withFillStrokeWidth(width);
    }

    @Override
    public MapDot withLabelText(String label)
    {
        return (MapDot) super.withLabelText(label);
    }

    @Override
    public MapDot withLocation(DrawingPoint at)
    {
        return (MapDot) super.withLocation(at);
    }

    @Override
    public MapDot withLocation(Location at)
    {
        return (MapDot) super.withLocation(at);
    }

    @Override
    public MapDot withMargin(int margin)
    {
        return (MapDot) super.withMargin(margin);
    }

    @Override
    public MapDot withOffset(int dx, int dy)
    {
        return (MapDot) super.withOffset(dx, dy);
    }

    public MapDot withRadius(Distance radius)
    {
        var copy = copy();
        copy.radius = radius;
        return copy;
    }

    public MapDot withRadius(DrawingLength radius)
    {
        var copy = copy();
        copy.coordinateRadius = radius;
        return copy;
    }

    @Override
    public MapDot withRoundedLabelCorners(DrawingLength corner)
    {
        return (MapDot) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapDot withRoundedLabelCorners(DrawingWidth cornerWidth,
                                          DrawingHeight cornerHeight)
    {
        return (MapDot) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
    }

    @Override
    public MapDot withStyle(Style style)
    {
        return (MapDot) super.withStyle(style);
    }

    @Override
    public MapDot withTextColor(Color color)
    {
        return (MapDot) super.withTextColor(color);
    }
}
