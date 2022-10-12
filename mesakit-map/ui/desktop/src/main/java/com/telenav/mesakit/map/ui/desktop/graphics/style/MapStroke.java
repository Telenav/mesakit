/*
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * //
 * // © 2011-2021 Telenav, Inc.
 * //
 * // Licensed under the Apache License, Version 2.0 (the "License");
 * // you may not use this file except in compliance with the License.
 * // You may obtain a copy of the License at
 * //
 * // http://www.apache.org/licenses/LICENSE-2.0
 * //
 * // Unless required by applicable law or agreed to in writing, software
 * // distributed under the License is distributed on an "AS IS" BASIS,
 * // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * // See the License for the specific language governing permissions and
 * // limitations under the License.
 * //
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 *
 */

package com.telenav.mesakit.map.ui.desktop.graphics.style;

import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Stroke;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Graphics2D;

/**
 * @author jonathanl (shibo)
 */
public class MapStroke extends Stroke
{
    public static MapStroke stroke()
    {
        return new MapStroke();
    }

    public static MapStroke stroke(Distance width)
    {
        return stroke().withWidth(width);
    }

    private Distance width;

    private DrawingWidth drawingWidth;

    protected MapStroke()
    {
    }

    protected MapStroke(MapStroke that)
    {
        super(that);
        width = that.width;
        drawingWidth = that.drawingWidth;
    }

    public void apply(Graphics2D graphics, MapCanvas canvas)
    {
        apply(graphics);
        drawingWidth = canvas.toDrawing(width).asWidth();
    }

    @Override
    public MapStroke copy()
    {
        return new MapStroke(this);
    }

    @Override
    public MapStroke scale(double scaleFactor)
    {
        return (MapStroke) super.scale(scaleFactor);
    }

    @Override
    public DrawingWidth width()
    {
        return drawingWidth;
    }

    @Override
    public MapStroke withCap(int cap)
    {
        return (MapStroke) super.withCap(cap);
    }

    @Override
    public MapStroke withDash(float[] dash)
    {
        return (MapStroke) super.withDash(dash);
    }

    @Override
    public MapStroke withDashPhase(float dash)
    {
        return (MapStroke) super.withDashPhase(dash);
    }

    @Override
    public MapStroke withJoin(int join)
    {
        return (MapStroke) super.withJoin(join);
    }

    @Override
    public MapStroke withMiterLimit(int miterLimit)
    {
        return (MapStroke) super.withMiterLimit(miterLimit);
    }

    @Override
    public MapStroke withWidth(DrawingWidth width)
    {
        return (MapStroke) super.withWidth(width);
    }

    public MapStroke withWidth(Distance width)
    {
        var copy = copy();
        copy.width = width;
        return copy;
    }
}
