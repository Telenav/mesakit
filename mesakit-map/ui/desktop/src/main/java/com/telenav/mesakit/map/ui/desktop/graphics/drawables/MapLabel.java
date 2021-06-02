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

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class MapLabel extends LabeledMapShape
{
    public static MapLabel label()
    {
        return label(null, null, null);
    }

    public static MapLabel label(final Style style, final Location at, final String label)
    {
        return new MapLabel(style, at, label);
    }

    public static MapLabel label(final Style style)
    {
        return new MapLabel(style, null, null);
    }

    protected MapLabel(final Style style, final Location at, final String label)
    {
        super(style, at, label);
    }

    protected MapLabel(final MapLabel that)
    {
        super(that);
    }

    @Override
    public MapLabel at(final DrawingPoint at)
    {
        return (MapLabel) super.at(at);
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
    public Shape onDraw(final MapCanvas canvas)
    {
        return super.onDraw(canvas);
    }

    @Override
    public Drawable scaledBy(final double scaleFactor)
    {
        return unsupported();
    }

    @Override
    public Drawable scaledBy(final Percent scaleFactor)
    {
        return unsupported();
    }

    @Override
    public MapLabel withColors(final Style style)
    {
        return (MapLabel) super.withColors(style);
    }

    @Override
    public MapLabel withDrawColor(final Color color)
    {
        return (MapLabel) super.withDrawColor(color);
    }

    @Override
    public MapLabel withDrawStrokeWidth(final DrawingWidth width)
    {
        return (MapLabel) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLabel withDrawStrokeWidth(final Distance width)
    {
        return (MapLabel) super.withDrawStrokeWidth(width);
    }

    @Override
    public MapLabel withFillColor(final Color color)
    {
        return (MapLabel) super.withFillColor(color);
    }

    @Override
    public MapLabel withFillStrokeWidth(final Distance width)
    {
        return (MapLabel) super.withFillStrokeWidth(width);
    }

    @Override
    public MapLabel withFillStrokeWidth(final DrawingWidth width)
    {
        return (MapLabel) super.withFillStrokeWidth(width);
    }

    @Override
    public MapLabel withLabelText(final String label)
    {
        return (MapLabel) super.withLabelText(label);
    }

    @Override
    public MapLabel withLocation(final Location at)
    {
        return (MapLabel) super.withLocation(at);
    }

    @Override
    public MapLabel withMargin(final int margin)
    {
        return (MapLabel) super.withMargin(margin);
    }

    @Override
    public MapLabel withOffset(final int dx, final int dy)
    {
        return (MapLabel) super.withOffset(dx, dy);
    }

    @Override
    public MapLabel withRoundedLabelCorners(final DrawingLength corner)
    {
        return (MapLabel) super.withRoundedLabelCorners(corner);
    }

    @Override
    public MapLabel withRoundedLabelCorners(final DrawingWidth cornerWidth,
                                            final DrawingHeight cornerHeight)
    {
        return (MapLabel) super.withRoundedLabelCorners(cornerWidth, cornerHeight);
    }

    @Override
    public MapLabel withStyle(final Style style)
    {
        return (MapLabel) super.withStyle(style);
    }

    @Override
    public MapLabel withTextColor(final Color color)
    {
        return (MapLabel) super.withTextColor(color);
    }
}
