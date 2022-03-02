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

import com.telenav.kivakit.language.level.Percent;
import com.telenav.kivakit.ui.desktop.graphics.drawing.Drawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapLabel extends LabeledMapShape
{
    public static MapLabel label()
    {
        return label(null, null, null);
    }

    public static MapLabel label(Style style, Location at, String label)
    {
        return new MapLabel(style, at, label);
    }

    public static MapLabel label(Style style)
    {
        return new MapLabel(style, null, null);
    }

    protected MapLabel(Style style, Location at, String label)
    {
        super(style, at, label);
    }

    protected MapLabel(MapLabel that)
    {
        super(that);
    }

    @Override
    public Rectangle bounds()
    {
        return unsupported();
    }

    @Override
    public MapLabel copy()
    {
        return new MapLabel(this);
    }

    @Override
    public Shape onDraw(MapCanvas canvas)
    {
        return super.onDraw(canvas);
    }

    @Override
    public Drawable scaledBy(double scaleFactor)
    {
        return unsupported();
    }

    @Override
    public Drawable scaledBy(Percent scaleFactor)
    {
        return unsupported();
    }

    @Override
    public MapLabel withColors(Style style)
    {
        return (MapLabel) super.withColors(style);
    }

    @Override
    public MapLabel withDrawColor(Color color)
    {
        return (MapLabel) super.withDrawColor(color);
    }

    @Override
    public MapLabel withDrawStrokeWidth(DrawingWidth width)
    {
        return (MapLabel) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLabel withDrawStrokeWidth(Distance width)
    {
        return (MapLabel) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLabel withFillColor(Color color)
    {
        return (MapLabel) super.withFillColor(color);
    }

    @Override
    public MapLabel withFillStrokeWidth(Distance width)
    {
        return (MapLabel) super.withFillStrokeWidth(width);
    }

    @Override
    public MapLabel withFillStrokeWidth(DrawingWidth width)
    {
        return (MapLabel) super.withFillStrokeWidth(width);
    }

    @Override
    public MapLabel withLabelText(String label)
    {
        return (MapLabel) super.withLabelText(label);
    }

    @Override
    public MapLabel withLocation(DrawingPoint at)
    {
        return (MapLabel) super.withLocation(at);
    }

    @Override
    public MapLabel withLocation(Location at)
    {
        return (MapLabel) super.withLocation(at);
    }

    @Override
    public MapLabel withMargin(int margin)
    {
        return (MapLabel) super.withMargin(margin);
    }

    @Override
    public MapLabel withOffset(int dx, int dy)
    {
        return (MapLabel) super.withOffset(dx, dy);
    }

    @Override
    public MapLabel withRoundedLabelCorners(DrawingLength corner)
    {
        return (MapLabel) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapLabel withRoundedLabelCorners(DrawingWidth cornerWidth,
                                            DrawingHeight cornerHeight)
    {
        return (MapLabel) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
    }

    @Override
    public MapLabel withStyle(Style style)
    {
        return (MapLabel) super.withStyle(style);
    }

    @Override
    public MapLabel withTextColor(Color color)
    {
        return (MapLabel) super.withTextColor(color);
    }
}
