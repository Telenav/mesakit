package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.core.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.ui.swing.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.swing.graphics.style.Shapes;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.unsupported;

/**
 * @author jonathanl (shibo)
 */
public class LabeledMapPolyline extends LabeledMapShape
{
    public static LabeledMapPolyline label(final Style style, final Polyline polyline)
    {
        return new LabeledMapPolyline(style, polyline);
    }

    public static LabeledMapPolyline label(final Style style)
    {
        return new LabeledMapPolyline(style, null);
    }

    private final Polyline polyline;

    private String startLabel;

    private String endLabel;

    private String centerLabel;

    protected LabeledMapPolyline(final Style style, final Polyline polyline)
    {
        super(style, null, null);
        this.polyline = polyline;
    }

    protected LabeledMapPolyline(final LabeledMapPolyline that)
    {
        super(that);
        startLabel = that.startLabel;
        centerLabel = that.centerLabel;
        endLabel = that.endLabel;
        polyline = that.polyline;
    }

    @Override
    public LabeledMapPolyline at(final Coordinate at)
    {
        return (LabeledMapPolyline) super.at(at);
    }

    @Override
    public LabeledMapPolyline copy()
    {
        return new LabeledMapPolyline(this);
    }

    @Override
    public Shape onDraw(final MapCanvas canvas)
    {
        final var shapes = new ObjectList<Shape>();

        shapes.add(canvas.drawPolyline(style(), polyline));

        if (startLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.start())));
        }

        if (centerLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.midpoint())));
        }

        if (endLabel != null)
        {
            shapes.add(super.drawLabel(canvas, anchor(canvas, polyline.end())));
        }

        return Shapes.combine(shapes);
    }

    @Override
    public LabeledMapPolyline scaled(final double scaleFactor)
    {
        return unsupported();
    }

    public LabeledMapPolyline withCenterLabel(final String text)
    {
        final var copy = copy();
        copy.centerLabel = text;
        return copy;
    }

    public LabeledMapPolyline withEndLabel(final String text)
    {
        final var copy = copy();
        copy.endLabel = text;
        return copy;
    }

    @Override
    public LabeledMapPolyline withLabel(final String label)
    {
        return (LabeledMapPolyline) super.withLabel(label);
    }

    @Override
    public LabeledMapPolyline withMargin(final int margin)
    {
        return (LabeledMapPolyline) super.withMargin(margin);
    }

    @Override
    public LabeledMapPolyline withOffset(final int dx, final int dy)
    {
        return (LabeledMapPolyline) super.withOffset(dx, dy);
    }

    public LabeledMapPolyline withStartLabel(final String text)
    {
        final var copy = copy();
        copy.startLabel = text;
        return copy;
    }

    @Override
    public LabeledMapPolyline withStyle(final Style style)
    {
        return (LabeledMapPolyline) super.withStyle(style);
    }
}
