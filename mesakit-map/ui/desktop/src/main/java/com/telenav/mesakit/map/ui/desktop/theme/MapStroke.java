package com.telenav.mesakit.map.ui.desktop.theme;

import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.style.Stroke;
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

    public static MapStroke stroke(final Distance width)
    {
        return stroke().withWidth(width);
    }

    private Distance width;

    private Length drawingWidth;

    protected MapStroke()
    {
    }

    protected MapStroke(final MapStroke that)
    {
        super(that);
        width = that.width;
        drawingWidth = that.drawingWidth;
    }

    public void apply(final Graphics2D graphics, final MapCanvas canvas)
    {
        super.apply(graphics);
        drawingWidth = canvas.toDrawing(width);
    }

    @Override
    public MapStroke copy()
    {
        return new MapStroke(this);
    }

    @Override
    public MapStroke scale(final double scaleFactor)
    {
        return (MapStroke) super.scale(scaleFactor);
    }

    @Override
    public MapStroke withCap(final int cap)
    {
        return (MapStroke) super.withCap(cap);
    }

    @Override
    public MapStroke withDash(final float[] dash)
    {
        return (MapStroke) super.withDash(dash);
    }

    @Override
    public MapStroke withDashPhase(final float dash)
    {
        return (MapStroke) super.withDashPhase(dash);
    }

    @Override
    public MapStroke withJoin(final int join)
    {
        return (MapStroke) super.withJoin(join);
    }

    @Override
    public MapStroke withMiterLimit(final int miterLimit)
    {
        return (MapStroke) super.withMiterLimit(miterLimit);
    }

    @Override
    public MapStroke withWidth(final Length width)
    {
        return (MapStroke) super.withWidth(width);
    }

    public MapStroke withWidth(final Distance width)
    {
        final var copy = copy();
        copy.width = width;
        return copy;
    }

    @Override
    protected Length width()
    {
        return drawingWidth;
    }
}
