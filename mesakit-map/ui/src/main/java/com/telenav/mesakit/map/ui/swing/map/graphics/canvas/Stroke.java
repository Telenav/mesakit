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

package com.telenav.mesakit.map.ui.swing.map.graphics.canvas;

import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.formatting.ObjectFormatter;

import java.awt.BasicStroke;
import java.awt.Shape;

public class Stroke
{
    @KivaKitIncludeProperty
    private BasicStroke stroke;

    public Stroke(final int cap, final int join)
    {
        stroke = new BasicStroke(1, cap, join);
    }

    public Stroke(final int cap, final int join, final float miterlimit, final float[] dash, final float dash_phase)
    {
        stroke = new BasicStroke(1, cap, join, miterlimit, dash, dash_phase);
    }

    public Stroke(final Stroke that)
    {
        stroke = that.stroke;
    }

    public void apply(final MapCanvas canvas, final Width width)
    {
        canvas.graphics().setStroke(stroke(canvas, width));
    }

    public Shape strokedShape(final MapCanvas canvas, final Width width, final Shape shape)
    {
        return stroke(canvas, width).createStrokedShape(shape);
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(stroke).toString();
    }

    private BasicStroke stroke(final MapCanvas canvas, final Width width)
    {
        assert width != null;
        assert canvas != null;

        final var widthInPixels = width.asPixels(canvas);
        if (widthInPixels >= 1)
        {
            if (stroke.getLineWidth() != widthInPixels)
            {
                stroke = new BasicStroke(widthInPixels, stroke.getEndCap(), stroke.getLineJoin(),
                        stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
            }
        }
        return stroke;
    }
}
