package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.swing.graphics.drawing.BaseDrawable;
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public abstract class MapShape extends BaseDrawable
{
    private Location location;

    public MapShape(final Style style,
                    final Location location)
    {
        super(style);
        this.location = location;
    }

    public MapShape(final MapShape that)
    {
        super(that);
        location = that.location;
    }

    protected MapShape(final Style style)
    {
        super(style);
    }

    @Override
    public final Shape draw(final DrawingSurface surface)
    {
        return onDraw((MapCanvas) surface);
    }

    public MapShape location(final Location at)
    {
        final var copy = (MapShape) copy();
        copy.location = at;
        return copy;
    }

    public Location location()
    {
        return location;
    }

    public abstract Shape onDraw(final MapCanvas canvas);
}
