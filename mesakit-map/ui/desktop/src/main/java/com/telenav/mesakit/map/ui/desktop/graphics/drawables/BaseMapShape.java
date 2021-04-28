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

import com.telenav.kivakit.ui.desktop.graphics.drawing.BaseDrawable;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.desktop.theme.MapStroke;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public abstract class BaseMapShape extends BaseDrawable
{
    private Location location;

    public BaseMapShape(final Style style,
                        final Location location)
    {
        super(style);
        this.location = location;
    }

    public BaseMapShape(final BaseMapShape that)
    {
        super(that);
        location = that.location;
    }

    protected BaseMapShape(final Style style)
    {
        super(style);
    }

    public BaseMapShape atLocation(final Location at)
    {
        final var copy = (BaseMapShape) copy();
        copy.location = at;
        return copy;
    }

    public Location atLocation()
    {
        return location;
    }

    @Override
    public final Shape draw(final DrawingSurface surface)
    {
        return onDraw((MapCanvas) surface);
    }

    public abstract Shape onDraw(final MapCanvas canvas);

    public BaseMapShape withDrawStrokeWidth(final Distance width)
    {
        return (BaseMapShape) withDrawStroke(MapStroke.stroke(width));
    }

    public BaseMapShape withFillStrokeWidth(final Distance width)
    {
        return (BaseMapShape) withFillStroke(MapStroke.stroke(width));
    }
}
