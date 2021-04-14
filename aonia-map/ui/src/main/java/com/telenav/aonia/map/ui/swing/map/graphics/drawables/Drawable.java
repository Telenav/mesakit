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

import com.telenav.aonia.map.ui.swing.map.graphics.canvas.Style;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;

import java.util.Objects;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public abstract class Drawable
{
    private Style style;

    protected Drawable()
    {
    }

    protected Drawable(final Style style)
    {
        this.style = Objects.requireNonNull(style);
    }

    @SuppressWarnings("ConstantConditions")
    protected Drawable(final Drawable that)
    {
        ensure(that != null);
        style = that.style;
    }

    @KivaKitIncludeProperty
    public Style style()
    {
        return style;
    }

    public void style(final Style style)
    {
        this.style = style;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }
}
