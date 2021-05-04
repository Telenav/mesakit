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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas;

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Dot;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Text;
import com.telenav.kivakit.ui.desktop.graphics.drawing.java2d.Java2dDrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Point;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Rectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.objects.Size;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.graphics.drawables.MapLine;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

/**
 * A {@link DrawingSurface} where map objects can be drawn
 */
public class MapCanvas extends Java2dDrawingSurface implements MapProjection
{
    /**
     * @return A canvas for drawing map objects on the given graphics, at the given scale, and in the coordinate system
     * provided by {@link MapProjection#drawingArea()}.
     */
    public static MapCanvas canvas(final Graphics2D graphics,
                                   final MapScale scale,
                                   final MapProjection projection)
    {
        return new MapCanvas(graphics, scale, projection);
    }

    /** The map scale being viewed */
    private final MapScale scale;

    /** Projection between map and drawing surface coordinates */
    private final MapProjection projection;

    protected MapCanvas(final Graphics2D graphics,
                        final MapScale scale,
                        final MapProjection projection)
    {
        super(graphics, projection.drawingArea().topLeft(), projection.drawingArea().size());

        this.scale = scale;
        this.projection = projection;
    }

    public Shape drawBox(final Style style, final com.telenav.mesakit.map.geography.shape.rectangle.Rectangle rectangle)
    {
        return Box.box(style)
                .at(toDrawing(rectangle.topLeft()))
                .withSize(toDrawing(rectangle).size())
                .draw(this);
    }

    public Shape drawDot(final Style style,
                         final Location at,
                         final Distance radius)
    {
        return Dot.dot(style)
                .at(toDrawing(at))
                .withRadius(toDrawing(radius))
                .draw(this);
    }

    public Shape drawLabel(final Style style, final Location location, final String text)
    {
        return Label.label(style, text)
                .at(toDrawing(location))
                .draw(this);
    }

    public Shape drawPolyline(final Style style, final Polyline line)
    {
        return drawPath(style, path(line));
    }

    public Shape drawSegment(final Style style, final Segment segment)
    {
        return MapLine.line()
                .withFrom(segment.start())
                .withTo(segment.end())
                .withStyle(style)
                .draw(this);
    }

    public Shape drawText(final Style style, final Location at, final String text)
    {
        return Text.text(style, text)
                .at(toDrawing(at))
                .draw(this);
    }

    /**
     * @return The coordinate area being mapped to on the drawing surface
     */
    @Override
    public Rectangle drawingArea()
    {
        return projection.drawingArea();
    }

    /**
     * @return The map area being projected to the {@link #drawingArea()} on this canvas
     */
    @Override
    public com.telenav.mesakit.map.geography.shape.rectangle.Rectangle mapArea()
    {
        return projection.mapArea();
    }

    /**
     * @return The center of the projected map area
     */
    public Location mapCenter()
    {
        return mapArea().center();
    }

    /**
     * @return The scale at which the map is being viewed
     */
    public MapScale mapScale()
    {
        return scale;
    }

    /**
     * @return The widest width of the projected map area
     */
    public Distance mapWidth()
    {
        return mapArea().widestWidth();
    }

    /**
     * @return The given location in projected coordinates
     */
    @Override
    public Point toDrawing(final Location location)
    {
        return projection.toDrawing(location);
    }

    /**
     * @return The given map size in projected coordinates
     */
    @Override
    public Size toDrawing(final com.telenav.mesakit.map.geography.shape.rectangle.Size size)
    {
        return projection.toDrawing(size);
    }

    /**
     * @return The given coordinate as a map {@link Location}
     */
    @Override
    public Location toMap(final Point coordinate)
    {
        return projection.toMap(coordinate);
    }

    private Path2D path(final Polyline line)
    {
        var first = true;
        final Path2D path = new Path2D.Double();
        for (final var to : line.locationSequence())
        {
            final var point = projection.toDrawing(to);
            if (first)
            {
                path.moveTo(point.x(), point.y());
                first = false;
            }
            else
            {
                path.lineTo(point.x(), point.y());
            }
        }
        return path;
    }
}
