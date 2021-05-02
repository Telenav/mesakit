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

import com.telenav.kivakit.ui.desktop.graphics.drawing.awt.AwtDrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Dot;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Text;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateRectangle;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSize;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

@SuppressWarnings({ "ConstantConditions" })
public class MapCanvas extends AwtDrawingSurface implements MapProjection
{
    public static MapCanvas canvas(final Graphics2D graphics,
                                   final MapScale scale,
                                   final MapProjection projection)
    {
        return new MapCanvas(graphics, scale, projection);
    }

    private final MapScale scale;

    private final MapProjection projection;

    protected MapCanvas(final Graphics2D graphics,
                        final MapScale scale,
                        final MapProjection projection)
    {
        super(graphics, projection.coordinateArea().topLeft(), projection.coordinateArea().size());

        this.scale = scale;
        this.projection = projection;
    }

    public Location center()
    {
        return mapBounds().center();
    }

    @Override
    public CoordinateRectangle coordinateArea()
    {
        return projection.coordinateArea();
    }

    public CoordinateRectangle coordinateBounds()
    {
        return projection.coordinateArea();
    }

    public Shape drawBox(final Style style, final Rectangle rectangle)
    {
        return Box.box(style)
                .at(toCoordinates(rectangle.topLeft()))
                .withSize(toCoordinates(rectangle).size())
                .draw(this);
    }

    public Shape drawDot(final Style style,
                         final Location at,
                         final Distance radius)
    {
        return Dot.dot(style)
                .at(toCoordinates(at))
                .withRadius(toCoordinates(radius))
                .draw(this);
    }

    public Shape drawLabel(final Style style, final Location location, final String text)
    {
        return Label.label(style, text)
                .at(toCoordinates(location))
                .draw(this);
    }

    public Shape drawPolyline(final Style style, final Polyline line)
    {
        return drawPath(style, path(line));
    }

    public Shape drawText(final Style style, final Location at, final String text)
    {
        return Text.text(style, text)
                .at(toCoordinates(at))
                .draw(this);
    }

    @Override
    public Rectangle mapArea()
    {
        return projection.mapArea();
    }

    public Rectangle mapBounds()
    {
        return projection.mapArea();
    }

    public MapScale scale()
    {
        return scale;
    }

    @Override
    public Coordinate toCoordinates(final Location location)
    {
        return projection.toCoordinates(location);
    }

    @Override
    public CoordinateSize toCoordinates(final Size size)
    {
        return projection.toCoordinates(size);
    }

    @Override
    public Location toMapUnits(final Coordinate coordinate)
    {
        return projection.toMapUnits(coordinate);
    }

    public Distance width()
    {
        return mapBounds().widestWidth();
    }

    private Path2D path(final Polyline line)
    {
        var first = true;
        final Path2D path = new Path2D.Double();
        for (final var to : line.locationSequence())
        {
            final var point = projection.toCoordinates(to);
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
