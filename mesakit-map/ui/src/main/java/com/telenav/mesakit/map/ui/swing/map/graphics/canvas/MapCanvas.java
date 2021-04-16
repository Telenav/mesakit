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

package com.telenav.mesakit.map.ui.swing.map.graphics.canvas;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.PolygonBuilder;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;
import com.telenav.mesakit.map.ui.swing.map.coordinates.mappers.CoordinateMapper;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

@SuppressWarnings({ "ConstantConditions" })
public class MapCanvas
{
    private final Graphics2D graphics;

    private final Scale scale;

    private final Rectangle bounds;

    private final CoordinateMapper mapper;

    public MapCanvas(final Graphics2D graphics, final Rectangle bounds, final Scale scale,
                     final CoordinateMapper mapper)
    {
        this.bounds = bounds;
        this.mapper = mapper;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        this.graphics = graphics;
        this.scale = scale;
    }

    public float awtDistance(final Distance distance)
    {
        final var width = com.telenav.mesakit.map.geography.shape.rectangle.Width.degrees(distance.asDegrees());
        final var location = bounds().topLeft().offset(width);
        return (float) awtPoint(location).getX();
    }

    public Point2D awtPoint(final Location location)
    {
        return mapper.pointForLocation(location);
    }

    public java.awt.Rectangle awtRectangle(final Rectangle rectangle)
    {
        final var topLeft = awtPoint(rectangle.topLeft());
        final var bottomRight = awtPoint(rectangle.bottomRight());
        final var x = (int) topLeft.getX();
        final var y = (int) topLeft.getY();
        final var width = (int) (bottomRight.getX() - x);
        final var height = (int) (bottomRight.getY() - y);
        return new java.awt.Rectangle(x, y, width, height);
    }

    public Rectangle bounds()
    {
        return bounds;
    }

    public Distance distance(final float pixels)
    {
        final var location = bounds().topLeft();
        final Point2D point = new Point2D.Float(pixels, pixels);
        return location.distanceTo(location(point));
    }

    public Shape drawArrow(final Style style, final Location location, final Distance size, final Heading heading)
    {
        ensure(style != null);
        ensure(location != null);
        ensure(size != null);
        ensure(heading != null);

        final var builder = new PolygonBuilder();
        final var base = location.moved(heading.reversed(), size.times(0.5));
        builder.add(base.moved(heading.minus(Angle._90_DEGREES), size));
        builder.add(base.moved(heading, size.times(1.25)));
        builder.add(base.moved(heading.plus(Angle._90_DEGREES), size));
        if (builder.isValid())
        {
            final var triangle = awtPath(builder.build());
            style.applyFill(this, Width.pixels(1f));
            graphics.fill(triangle);
            return style.shape(this, triangle);
        }
        else
        {
            return null;
        }
    }

    public Shape drawDot(final Style style, final Width width, final Style outlineStyle, final Width outlineWidth,
                         final Location location)
    {
        ensure(style != null);
        ensure(location != null);
        ensure(width != null);

        final var at = awtPoint(location);
        final var east = awtPoint(location.moved(Heading.EAST, width.asDistance(this)));
        final var widthInPixels = east.getX() - at.getX();
        final Shape circle = new Ellipse2D.Double(at.getX() - widthInPixels / 2, at.getY() - widthInPixels / 2,
                widthInPixels, widthInPixels);

        style.applyFill(this, Width.pixels(1f));
        graphics.fill(circle);

        outlineStyle.applyDraw(this, outlineWidth);
        graphics.draw(circle);

        return circle;
    }

    public Rectangle2D drawLabel(final Style style, final Location at, final Width strokeWidth, final String text)
    {
        ensure(style != null);
        ensure(at != null);
        ensure(text != null);

        final var outlineWidth = 3;
        final var textMargin = 5;
        final var totalMargin = outlineWidth + textMargin;

        // Get bounds of text
        final var textBounds = style.textBounds(this, text);
        final var point = awtPoint(at);

        // Get background size
        final var x = (int) point.getX();
        final var y = (int) point.getY();
        final var widthInPixels = (int) textBounds.getWidth() + 2 * totalMargin;
        final var heightInPixels = (int) textBounds.getHeight() + 2 * totalMargin;

        // Fill the inside
        style.applyFill(this, Width.pixels(1f));
        graphics.fillRoundRect(x, y, widthInPixels, heightInPixels, 8, 8);

        // Draw the outline
        style.applyDraw(this, strokeWidth);
        graphics.drawRoundRect(x, y, widthInPixels, heightInPixels, 8, 8);

        // Draw the text
        graphics.setFont(style.font());
        graphics.setColor(style.text().asAwtColor());
        final var textX = (int) (point.getX() + totalMargin);
        final var textY = (int) (point.getY() + totalMargin + textBounds.getHeight());
        graphics.drawString(text, textX, textY);

        return new Rectangle2D.Float(x, y, widthInPixels, heightInPixels);
    }

    public void drawLabeledRectangle(final Style style, final Width width, final Rectangle rectangle, final String text)
    {
        ensure(style != null);
        ensure(rectangle != null);
        ensure(text != null);

        final var topLeft = awtPoint(rectangle.topLeft());
        final var bottomRight = awtPoint(rectangle.bottomRight());
        style.applyFill(this, Width.pixels(1f));
        graphics.fillRect((int) topLeft.getX(), (int) topLeft.getY(), (int) (bottomRight.getX() - topLeft.getX()),
                (int) (bottomRight.getY() - topLeft.getY()));
        style.applyDraw(this, width);
        graphics.drawRect((int) topLeft.getX(), (int) topLeft.getY(), (int) (bottomRight.getX() - topLeft.getX()),
                (int) (bottomRight.getY() - topLeft.getY()));
        style.text(this);
        graphics.drawString(text, (int) topLeft.getX(), (int) topLeft.getY()
                - graphics.getFontMetrics().getAscent() + graphics.getFontMetrics().getDescent());
    }

    public Shape drawPolyline(final Style style, final Width width, final Style outlineStyle, final Width outlineWidth,
                              final Polyline line)
    {
        ensure(style != null);
        ensure(width != null);
        ensure(outlineStyle != null);
        ensure(outlineWidth != null);
        ensure(line != null);

        // Create path
        final var path = awtPath(line);

        // Draw the outline
        outlineStyle.applyDraw(this, width);
        graphics.draw(path);

        // Draw the center with a decreased stroke width
        style.applyFill(this, width.minus(this, outlineWidth));
        graphics.draw(path);

        // Return the shape of the path
        return style.shape(this, width, path);
    }

    public Graphics2D graphics()
    {
        return graphics;
    }

    public Rectangle2D labelBounds(final Style style, final Location at, final String text)
    {
        final var outlineWidth = 3;
        final var textMargin = 5;
        final var totalMargin = outlineWidth + textMargin;

        // Get bounds of text
        final var textBounds = style.textBounds(this, text);
        final var point = awtPoint(at);

        // Get background size
        final var x = (int) point.getX();
        final var y = (int) point.getY();
        final var widthInPixels = (int) textBounds.getWidth() + 2 * totalMargin;
        final var heightInPixels = (int) textBounds.getHeight() + 2 * totalMargin;

        return new Rectangle2D.Float(x, y, widthInPixels, heightInPixels);
    }

    public Location location(final Point2D point)
    {
        return mapper.locationForPoint(point);
    }

    public Rectangle rectangle(final java.awt.Rectangle bounds)
    {
        final var topLeft = location(new Point2D.Double(bounds.getMinX(), bounds.getMinY()));
        final var bottomRight = location(new Point2D.Double(bounds.getMaxX(), bounds.getMaxY()));
        return Rectangle.fromLocations(topLeft, bottomRight);
    }

    public Scale scale()
    {
        return scale;
    }

    public Distance viewWidth()
    {
        return bounds().widestWidth();
    }

    Path2D awtPath(final Polyline line)
    {
        var first = true;
        final Path2D path = new Path2D.Double();
        for (final var to : line.locationSequence())
        {
            final var point = awtPoint(to);
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
}
