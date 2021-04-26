package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.swing.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.swing.graphics.style.Shapes;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

/**
 * @author jonathanl (shibo)
 */
public class LabeledMapBox extends LabeledMapShape
{
    public static LabeledMapBox box(final Style style, final Rectangle box, final String label)
    {
        return new LabeledMapBox(style, box, label);
    }

    public static LabeledMapBox label(final Style style)
    {
        return new LabeledMapBox(style, null, null);
    }

    private final Size size;

    protected LabeledMapBox(final Style style, final Rectangle box, final String label)
    {
        super(style, box.topLeft(), label);
        size = box.size();
    }

    protected LabeledMapBox(final LabeledMapBox that)
    {
        super(that);
        size = that.size;
    }

    @Override
    public LabeledMapBox at(final Coordinate at)
    {
        return (LabeledMapBox) super.at(at);
    }

    @Override
    public LabeledMapBox copy()
    {
        return new LabeledMapBox(this);
    }

    @Override
    public LabeledMapBox location(final Location at)
    {
        return (LabeledMapBox) super.location(at);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var box = box(canvas.inCoordinates(size))
                .at(canvas.inCoordinates(location()))
                .draw(canvas);

        final var label = super.onDraw(canvas);

        return Shapes.combine(box, label);
    }

    @Override
    public LabeledMapBox scaled(final double scaleFactor)
    {
        return withSize(size.scaledBy(scaleFactor, scaleFactor));
    }

    @Override
    public LabeledMapBox withLabel(final String label)
    {
        return (LabeledMapBox) super.withLabel(label);
    }

    @Override
    public LabeledMapBox withMargin(final int margin)
    {
        return (LabeledMapBox) super.withMargin(margin);
    }

    @Override
    public LabeledMapBox withOffset(final int dx, final int dy)
    {
        return (LabeledMapBox) super.withOffset(dx, dy);
    }

    public LabeledMapBox withSize(final Size size)
    {
        return copy().withSize(size);
    }

    @Override
    public LabeledMapBox withStyle(final Style style)
    {
        return (LabeledMapBox) super.withStyle(style);
    }
}
