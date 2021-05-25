package com.telenav.mesakit.map.ui.desktop.graphics.drawables;

import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingHeight;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingLength;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.measurements.DrawingWidth;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapCanvas;

import java.awt.Shape;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * A map drawable object which has an optional label
 *
 * @author jonathanl (shibo)
 */
public abstract class LabeledMapShape extends BaseMapDrawable
{
    private String label;

    private double labelXOffset = 10;

    private double labelYOffset = -10;

    private int margin = 8;

    private Style labelStyle;

    private DrawingWidth labelCornerWidth;

    private DrawingHeight labelCornerHeight;

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

    public Shape drawLabel(final MapCanvas canvas, final DrawingPoint coordinate)
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

    public LabeledMapShape withRoundedLabelCorners(final DrawingWidth cornerWidth,
                                                   final DrawingHeight cornerHeight)
    {
        final var copy = copy();
        copy.labelCornerWidth = cornerWidth;
        copy.labelCornerHeight = cornerHeight;
        return copy;
    }

    public LabeledMapShape withRoundedLabelCorners(final DrawingLength corner)
    {
        return withRoundedLabelCorners(corner.asWidth(), corner.asHeight());
    }

    protected DrawingPoint anchor(final MapCanvas canvas, final Location location)
    {
        return canvas.toDrawing(location).plus(labelXOffset, labelYOffset);
    }
}
