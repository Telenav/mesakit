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

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

public class Label extends Drawable
{
    private final Width width;

    public Label(final Width width, final Style style)
    {
        super(style);
        this.width = ensureNotNull(width);
    }

    protected Label(final Label that)
    {
        super(that);
        width = that.width;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Shape draw(final MapCanvas canvas, final Location location, final String text)
    {
        return canvas.drawLabel(style(), location, width, text);
    }
}
