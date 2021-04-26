package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.swing.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.swing.graphics.style.Shapes;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class LabeledMapDot extends LabeledMapShape
{
    public static LabeledMapDot label(final Style style, final Location at, final Distance radius, final String label)
    {
        return new LabeledMapDot(style, at, radius, label);
    }

    public static LabeledMapDot label(final Style style)
    {
        return new LabeledMapDot(style, null, null, null);
    }

    private Distance radius;

    protected LabeledMapDot(final Style style, final Location at, final Distance radius, final String label)
    {
        super(style, at, label);
        this.radius = radius;
    }

    protected LabeledMapDot(final LabeledMapDot that)
    {
        super(that);
        radius = that.radius;
    }

    @Override
    public LabeledMapDot at(final Coordinate at)
    {
        return (LabeledMapDot) super.at(at);
    }

    @Override
    public LabeledMapDot copy()
    {
        return new LabeledMapDot(this);
    }

    @Override
    public LabeledMapDot location(final Location at)
    {
        return (LabeledMapDot) super.location(at);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var box = dot(canvas.inCoordinates(radius))
                .at(canvas.inCoordinates(location()))
                .draw(canvas);

        final var label = super.onDraw(canvas);

        return Shapes.combine(box, label);
    }

    @Override
    public LabeledMapDot scaled(final double scaleFactor)
    {
        return unsupported();
    }

    @Override
    public LabeledMapDot withLabel(final String label)
    {
        return (LabeledMapDot) super.withLabel(label);
    }

    @Override
    public LabeledMapDot withMargin(final int margin)
    {
        return (LabeledMapDot) super.withMargin(margin);
    }

    @Override
    public LabeledMapDot withOffset(final int dx, final int dy)
    {
        return (LabeledMapDot) super.withOffset(dx, dy);
    }

    public LabeledMapDot withRadius(final Distance radius)
    {
        final var copy = copy();
        copy.radius = radius;
        return copy;
    }

    @Override
    public LabeledMapDot withStyle(final Style style)
    {
        return (LabeledMapDot) super.withStyle(style);
    }
}
