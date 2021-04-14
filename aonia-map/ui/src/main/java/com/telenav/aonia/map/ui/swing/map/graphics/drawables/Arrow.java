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

package com.telenav.aonia.map.ui.swing.map.graphics.drawables;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.measurements.geographic.Heading;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.MapCanvas;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Style;
import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;

import java.awt.Shape;

public class Arrow extends Drawable
{
    private Dot dot;

    public Arrow(final Dot dot, final Style arrowStyle)
    {
        super(arrowStyle);
        this.dot = dot;
    }

    private Arrow(final Arrow that)
    {
        super(that);
        dot = that.dot;
    }

    @KivaKitIncludeProperty
    public Dot dot()
    {
        return dot;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Shape draw(final MapCanvas canvas, final Location location, final Heading heading)
    {
        dot.draw(canvas, location);
        final var size = dot.width().times(Percent.of(40)).asDistance(canvas);
        return canvas.drawArrow(style(), location, size, heading);
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public Arrow withDot(final Dot dot)
    {
        final var copy = new Arrow(this);
        copy.dot = dot;
        return copy;
    }

    public Arrow withWidth(final Width width)
    {
        return withDot(dot.withWidth(width).withOutlineWidth(width.times(Percent.of(20))));
    }
}
