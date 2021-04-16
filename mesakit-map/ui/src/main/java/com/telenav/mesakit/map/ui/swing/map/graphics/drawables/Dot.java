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

package com.telenav.mesakit.map.ui.swing.map.graphics.drawables;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.MapCanvas;
import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.Style;
import com.telenav.mesakit.map.ui.swing.map.graphics.canvas.Width;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.core.kernel.language.values.level.Percent;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

public class Dot extends Drawable
{
    private Width width;

    private Width outlineWidth;

    private Style outlineStyle;

    public Dot(final Width width, final Style style, final Width outlineWidth, final Style outlineStyle)
    {
        super(style);

        this.width = ensureNotNull(width);
        this.outlineWidth = ensureNotNull(outlineWidth);
        this.outlineStyle = ensureNotNull(outlineStyle);
    }

    public Dot()
    {
    }

    private Dot(final Dot that)
    {
        super(that);
        width = that.width;
        outlineWidth = that.outlineWidth;
        outlineStyle = that.outlineStyle;
    }

    public Shape draw(final MapCanvas canvas, final Location location)
    {
        return canvas.drawDot(style(), width, outlineStyle, outlineWidth, location);
    }

    public Dot fattened(final Percent percent)
    {
        return withWidth(width.fattened(percent));
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    @KivaKitIncludeProperty
    public Width width()
    {
        return width;
    }

    public Dot withOutlineStyle(final Style style)
    {
        final var copy = new Dot(this);
        copy.outlineStyle = style;
        return copy;
    }

    public Dot withOutlineWidth(final Width width)
    {
        final var copy = new Dot(this);
        copy.outlineWidth = width;
        return copy;
    }

    public Dot withStyle(final Style style)
    {
        final var copy = new Dot(this);
        copy.style(style);
        return copy;
    }

    public Dot withWidth(final Width width)
    {
        final var copy = new Dot(this);
        copy.width = width;
        return copy;
    }
}
