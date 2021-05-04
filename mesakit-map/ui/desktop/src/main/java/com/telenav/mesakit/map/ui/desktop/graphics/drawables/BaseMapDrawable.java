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

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.BaseDrawable;
import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.style.Stroke;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.theme.MapStroke;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public abstract class BaseMapDrawable extends BaseDrawable implements MapDrawable
{
    private Location location;

    public BaseMapDrawable(final Style style,
                           final Location location)
    {
        super(style);
        this.location = location;
    }

    public BaseMapDrawable(final BaseMapDrawable that)
    {
        super(that);
        location = that.location;
    }

    protected BaseMapDrawable(final Style style)
    {
        super(style);
    }

    @Override
    public final Shape draw(final DrawingSurface surface)
    {
        return onDraw((MapCanvas) surface);
    }

    @Override
    public Location location()
    {
        return location;
    }

    public abstract Shape onDraw(final MapCanvas canvas);

    @Override
    public BaseMapDrawable withColors(final Style style)
    {
        return (BaseMapDrawable) super.withColors(style);
    }

    @Override
    public BaseMapDrawable withDrawColor(final Color color)
    {
        return (BaseMapDrawable) super.withDrawColor(color);
    }

    @Override
    public BaseMapDrawable withDrawStroke(final Stroke stroke)
    {
        return (BaseMapDrawable) super.withDrawStroke(stroke);
    }

    @Override
    public BaseMapDrawable withDrawStrokeWidth(final Length width)
    {
        return (BaseMapDrawable) super.withDrawStrokeWidth(width);
    }

    public BaseMapDrawable withDrawStrokeWidth(final Distance width)
    {
        return withDrawStroke(MapStroke.stroke(width));
    }

    @Override
    public BaseMapDrawable withFillColor(final Color color)
    {
        return (BaseMapDrawable) super.withFillColor(color);
    }

    @Override
    public BaseMapDrawable withFillStroke(final Stroke stroke)
    {
        return (BaseMapDrawable) super.withFillStroke(stroke);
    }

    @Override
    public BaseMapDrawable withFillStrokeWidth(final Length width)
    {
        return (BaseMapDrawable) super.withFillStrokeWidth(width);
    }

    public BaseMapDrawable withFillStrokeWidth(final Distance width)
    {
        return withFillStroke(MapStroke.stroke(width));
    }

    @Override
    public BaseMapDrawable withLocation(final Location at)
    {
        final var copy = (BaseMapDrawable) copy();
        copy.location = at;
        return copy;
    }

    @Override
    public BaseMapDrawable withStyle(final Style style)
    {
        return (BaseMapDrawable) super.withStyle(style);
    }

    @Override
    public BaseMapDrawable withTextColor(final Color color)
    {
        return (BaseMapDrawable) super.withTextColor(color);
    }

    protected Point at(final MapCanvas surface)
    {
        var at = at();
        if (at == null)
        {
            at = surface.toDrawing(location());
        }
        return at;
    }
}
