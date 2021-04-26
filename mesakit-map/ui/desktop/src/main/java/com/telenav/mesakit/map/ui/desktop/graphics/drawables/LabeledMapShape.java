package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.swing.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public abstract class LabeledMapShape extends MapShape
{
    private String label;

    private double labelXOffset = 10;

    private double labelYOffset = -10;

    private int margin = 10;

    public LabeledMapShape(final Style style, final String label)
    {
        super(style);
        this.label = label;
    }

    public LabeledMapShape(final Style style, final Location location, final String label)
    {
        super(style);
        this.label = label;
    }

    public LabeledMapShape(final LabeledMapShape that)
    {
        super(that);
        label = that.label;
        labelXOffset = that.labelXOffset;
        labelYOffset = that.labelYOffset;
        margin = that.margin;
    }

    @Override
    public abstract LabeledMapShape copy();

    public Shape drawLabel(final MapCanvas canvas, final Coordinate coordinate)
    {
        if (label != null)
        {
            return label(label)
                    .at(coordinate)
                    .withMargin(margin)
                    .draw(canvas);
        }

        return null;
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        if (label != null)
        {
            return drawLabel(canvas, anchor(canvas, location()));
        }

        return null;
    }

    public LabeledMapShape withLabel(final String label)
    {
        final var copy = copy();
        copy.label = label;
        return copy;
    }

    public LabeledMapShape withMargin(final int margin)
    {
        final var copy = copy();
        copy.margin = margin;
        return copy;
    }

    public LabeledMapShape withOffset(final int dx, final int dy)
    {
        final var copy = copy();
        copy.labelXOffset = dx;
        copy.labelYOffset = dy;
        return copy;
    }

    protected Coordinate anchor(final MapCanvas canvas, final Location location)
    {
        return canvas.inCoordinates(location).plus(labelXOffset, labelYOffset);
    }
}
