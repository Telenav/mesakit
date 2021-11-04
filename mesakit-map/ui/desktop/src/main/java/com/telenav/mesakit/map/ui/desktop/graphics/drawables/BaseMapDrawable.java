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
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.BaseDrawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Color;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Stroke;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.graphics.style.MapStroke;

import java.awt.Shape;

/**
 * A {@link Drawable} base class for objects that can be drawn on a map. Adds a map {@link Location} to the {@link
 * DrawingPoint} location in {@link BaseDrawable}.
 *
 * @author jonathanl (shibo)
 */
public abstract class BaseMapDrawable extends BaseDrawable implements MapDrawable
{
    private Location location;

    public BaseMapDrawable(Style style,
                           Location location)
    {
        super(style);
        this.location = location;
    }

    public BaseMapDrawable(BaseMapDrawable that)
    {
        super(that);
        location = that.location;
    }

    protected BaseMapDrawable(Style style)
    {
        super(style);
    }

    @Override
    public final Shape draw(DrawingSurface surface)
    {
        return onDraw((MapCanvas) surface);
    }

    @Override
    public Location location()
    {
        return location;
    }

    public abstract Shape onDraw(MapCanvas canvas);

    @Override
    public BaseMapDrawable withColors(Style style)
    {
        return (BaseMapDrawable) super.withColors(style);
    }

    @Override
    public BaseMapDrawable withDrawColor(Color color)
    {
        return (BaseMapDrawable) super.withDrawColor(color);
    }

    @Override
    public BaseMapDrawable withDrawStroke(Stroke stroke)
    {
        return (BaseMapDrawable) super.withDrawStroke(stroke);
    }

    @Override
    public BaseMapDrawable withDrawStrokeWidth(DrawingWidth width)
    {
        return (BaseMapDrawable) super.withDrawStrokeWidth(width);
    }

    public BaseMapDrawable withDrawStrokeWidth(Distance width)
    {
        return withDrawStroke(MapStroke.stroke(width));
    }

    @Override
    public BaseMapDrawable withFillColor(Color color)
    {
        return (BaseMapDrawable) super.withFillColor(color);
    }

    @Override
    public BaseMapDrawable withFillStroke(Stroke stroke)
    {
        return (BaseMapDrawable) super.withFillStroke(stroke);
    }

    @Override
    public BaseMapDrawable withFillStrokeWidth(DrawingWidth width)
    {
        return (BaseMapDrawable) super.withFillStrokeWidth(width);
    }

    public BaseMapDrawable withFillStrokeWidth(Distance width)
    {
        return withFillStroke(MapStroke.stroke(width));
    }

    @Override
    public BaseMapDrawable withLocation(Location at)
    {
        var copy = (BaseMapDrawable) copy();
        copy.location = at;
        return copy;
    }

    @Override
    public BaseMapDrawable withStyle(Style style)
    {
        return (BaseMapDrawable) super.withStyle(style);
    }

    @Override
    public BaseMapDrawable withTextColor(Color color)
    {
        return (BaseMapDrawable) super.withTextColor(color);
    }

    protected DrawingPoint at(MapCanvas surface)
    {
        var at = withLocation();
        if (at == null)
        {
            at = surface.toDrawing(location());
        }
        return at;
    }
}
