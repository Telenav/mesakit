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

package com.telenav.aonia.map.ui.swing.debug.viewer.swing;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.measurements.geographic.Heading;
import com.telenav.aonia.map.ui.swing.debug.InteractiveDrawingSurface;
import com.telenav.aonia.map.ui.swing.map.coordinates.mappers.CoordinateMapper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

public class SwingDrawingSurface implements InteractiveDrawingSurface
{
    private final CoordinateMapper mapper;

    private final Graphics2D graphics;

    private final float strokeWidth;

    private final BasicStroke thinStroke;

    private final BasicStroke outlineStroke;

    private final BasicStroke hitTestStroke;

    private final BasicStroke fillStroke;

    private final BasicStroke highlightOutlineStroke;

    private final BasicStroke highlightFillStroke;

    private BasicStroke stroke;

    private BasicStroke fill;

    private Path2D path;

    private Shape shape;

    private Rectangle2D.Double labelBounds;

    private Color highlightColor;

    private boolean highlight;

    private final Rectangle view;

    public SwingDrawingSurface(final Graphics2D graphics, final CoordinateMapper mapper, final Rectangle view)
    {
        final var hints = new HashMap<RenderingHints.Key, Object>();
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHints(new RenderingHints(hints));

        this.graphics = graphics;
        this.mapper = mapper;
        this.view = view;
        strokeWidth = strokeWidth();

        // Regular strokes
        thinStroke = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        outlineStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        fillStroke = new BasicStroke(strokeWidth - (strokeWidth / 8.0f), BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_ROUND);

        // Hit testing stroke
        hitTestStroke = new BasicStroke(strokeWidth + 4, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

        // Highlight strokes
        final var highlightStrokeWidth = strokeWidth;
        highlightOutlineStroke = new BasicStroke(highlightStrokeWidth, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_ROUND, 2.0f, new float[] { 2.0f }, 0.0f);
        highlightFillStroke = new BasicStroke(highlightStrokeWidth - (highlightStrokeWidth / 8.0f),
                BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);

        // Start with regular strokes
        highlight(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final Location location, final Color background, final Color foreground, final String label)
    {
        // Get point on canvas
        final Point2D point = mapper.pointForLocation(location);

        // Draw point
        graphics.setStroke(stroke);
        graphics.setPaint(color(foreground));
        graphics.drawArc((int) point.getX() - 2, (int) point.getY() - 2, 4, 4, 0, 360);

        // Save shape for hit testing
        shape = new Ellipse2D.Double((int) point.getX() - 2, (int) point.getY() - 2, 4, 4);

        // Draw label
        drawLabel(location, background, color(foreground), label);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(final Polyline line, final Color background, final Color foreground, final String label)
    {
        path = polylinePath(line);

        // Draw the outline path
        graphics.setStroke(stroke);
        graphics.setPaint(color(foreground.brighter()));
        graphics.draw(path);

        // Draw the fill path
        if (strokeWidth > 3)
        {
            graphics.setStroke(fill);
            graphics.setPaint(color(foreground));
            graphics.draw(path);
        }

        // Draw label above first segment
        drawLabel(line.firstSegment().start(), background, color(foreground), label);
    }

    @Override
    public void highlight(final boolean highlight)
    {
        this.highlight = highlight;
        stroke = highlight ? highlightOutlineStroke : outlineStroke;
        fill = highlight ? highlightFillStroke : fillStroke;
    }

    @Override
    public void highlightColor(final Color highlightColor)
    {
        this.highlightColor = highlightColor;
    }

    @Override
    public Shape hitTestShape()
    {
        final Area area;
        if (path != null)
        {
            area = new Area(hitTestStroke.createStrokedShape(path));
        }
        else if (shape != null)
        {
            area = new Area(shape);
        }
        else
        {
            return fail("Expected either path or shape to be available");
        }
        if (labelBounds != null)
        {
            area.add(new Area(labelBounds));
        }
        return area;
    }

    private Color color(final Color color)
    {
        return highlight ? highlightColor : color;
    }

    private void drawLabel(final Location at, final Color background, final Color foreground, final String label)
    {
        if (label != null)
        {
            // Text margin
            final var margin = 4;

            // Get font measurements
            graphics.setFont(new Font("Helvetica", Font.PLAIN, 12));
            final var metrics = graphics.getFontMetrics();
            final var bounds = metrics.getStringBounds(label, graphics);
            final var width = bounds.getWidth() + margin;
            final var height = bounds.getHeight() + margin;

            // Get location to draw at
            final Point2D point = mapper.pointForLocation(at);
            final var x = point.getX();
            final var y = point.getY() - margin - margin - metrics.getAscent();

            // Calculate label bounds
            labelBounds = new Rectangle2D.Double(x, y, width, height);

            // Fill behind text
            graphics.setStroke(fillStroke);
            graphics.setColor(background);
            graphics.fill(labelBounds);
            graphics.setStroke(thinStroke);
            graphics.setColor(foreground);
            graphics.draw(labelBounds);

            // Draw text
            graphics.setColor(foreground);
            graphics.drawString(label, (float) x + (float) margin / 2,
                    (float) y + metrics.getAscent() + (float) margin / 2);
        }
        else
        {
            labelBounds = null;
        }
    }

    private Path2D polylinePath(final Polyline line)
    {
        var first = true;
        final Path2D path = new Path2D.Double();
        for (final var to : line.locationSequence())
        {
            final Point2D point = mapper.pointForLocation(to);
            if (first)
            {
                path.moveTo(point.getX(), point.getY());
                first = false;
            }
            else
            {
                path.lineTo(point.getX(), point.getY());
            }
        }
        return path;
    }

    private float strokeWidth()
    {
        final var origin = view.center();
        final var movedEast = origin.moved(Heading.EAST, Distance.kilometers(8));
        final var pixels = (float) mapper.pointForLocation(movedEast).getX() / 1000.0f;
        return pixels < 1 ? 1f : pixels;
    }
}
