package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.desktop.graphics.geometry.measurements.Length;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * @author jonathanl (shibo)
 */
public abstract class LabeledMapShape extends BaseMapDrawable
{
    private String label;

    private double labelXOffset = 10;

    private double labelYOffset = -10;

    private int margin = 8;

    private Style labelStyle;

    private Length labelCornerWidth;

    private Length labelCornerHeight;

    public LabeledMapShape(final Style style, final String label)
    {
        super(style);
        this.label = label;
    }

    public LabeledMapShape(final Style style, final Location at, final String label)
    {
        super(style, at);
        this.label = label;
    }

    public LabeledMapShape(final LabeledMapShape that)
    {
        super(that);
        labelCornerWidth = that.labelCornerWidth;
        labelCornerHeight = that.labelCornerHeight;
        label = that.label;
        labelXOffset = that.labelXOffset;
        labelYOffset = that.labelYOffset;
        margin = that.margin;
        labelStyle = that.labelStyle;
    }

    @Override
    public abstract LabeledMapShape copy();

    public Shape drawLabel(final MapCanvas canvas, final Point coordinate)
    {
        ensureNotNull(canvas);
        ensureNotNull(coordinate);
        ensureNotNull(labelStyle);

        if (label != null)
        {
            return label(label)
                    .at(coordinate)
                    .withStyle(labelStyle)
                    .withRoundedCorners(labelCornerWidth, labelCornerHeight)
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
            ensure(labelStyle != null);
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

    public LabeledMapShape withLabelStyle(final Style style)
    {
        final var copy = copy();
        copy.labelStyle = style;
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

    public LabeledMapShape withRoundedLabelCorners(final Length cornerWidth,
                                                   final Length cornerHeight)
    {
        final var copy = copy();
        copy.labelCornerWidth = cornerWidth;
        copy.labelCornerHeight = cornerHeight;
        return copy;
    }

    public LabeledMapShape withRoundedLabelCorners(final Length corner)
    {
        return withRoundedLabelCorners(corner, corner);
    }

    protected Point anchor(final MapCanvas canvas, final Location location)
    {
        return canvas.toDrawing(location).plus(labelXOffset, labelYOffset);
    }
}