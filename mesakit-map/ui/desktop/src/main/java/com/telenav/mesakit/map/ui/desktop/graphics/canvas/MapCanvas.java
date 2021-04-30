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

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSize;
import com.telenav.kivakit.ui.desktop.graphics.drawing.awt.AwtDrawingSurface;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Dot;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.desktop.graphics.drawing.drawables.Text;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateDistance;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateHeight;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSize;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSlope;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateWidth;
import com.telenav.kivakit.ui.desktop.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Dimensioned;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

@SuppressWarnings({ "ConstantConditions" })
public class MapCanvas extends AwtDrawingSurface
{
    public static MapCanvas canvas(final Graphics2D graphics,
                                   final Rectangle bounds,
                                   final MapScale scale,
                                   final MapDrawingSurfaceProjection projection)
    {
        return new MapCanvas(graphics, bounds, scale, projection);
    }

    private final MapScale scale;

    private final Rectangle bounds;

    private final MapCoordinateSystem coordinateSystem;

    protected MapCanvas(final Graphics2D graphics,
                        final Rectangle bounds,
                        final MapScale scale,
                        final MapDrawingSurfaceProjection projection)
    {
        super(graphics);

        coordinateSystem = new MapCoordinateSystem(projection);

        this.bounds = bounds;
        this.scale = scale;
    }

    public Rectangle bounds()
    {
        return bounds;
    }

    public Location center()
    {
        return bounds().center();
    }

    public Shape drawBox(final Style style, final Rectangle rectangle)
    {
        return Box.box(style)
                .at(toCoordinates(rectangle.topLeft()))
                .withSize(toCoordinates(rectangle))
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

    public MapScale scale()
    {
        return scale;
    }

    @Override
    public CoordinateSlope slope(final Coordinate a, final Coordinate b)
    {
        return coordinateSystem.slope(a, b);
    }

    public Coordinate toCoordinates(final Location location)
    {
        return at(location.longitudeInDegrees(), location.latitudeInDegrees());
    }

    public CoordinateDistance toCoordinates(final Distance distance)
    {
        return distance(distance.asDegrees());
    }

    @Override
    public Coordinate toCoordinates(final DrawingPoint point)
    {
        return coordinateSystem.toCoordinates(point);
    }

    @Override
    public CoordinateSize toCoordinates(final DrawingSize size)
    {
        return coordinateSystem.toCoordinates(size);
    }

    @Override
    public CoordinateDistance toCoordinates(final DrawingDistance distance)
    {
        return coordinateSystem.toCoordinates(distance);
    }

    public CoordinateSize toCoordinates(final Size size)
    {
        return size(size.width().asDegrees(), size.height().asDegrees());
    }

    public CoordinateHeight toCoordinates(final Height height)
    {
        return CoordinateHeight.height(coordinateSystem, height.asDegrees());
    }

    public CoordinateWidth toCoordinates(final Width width)
    {
        return CoordinateWidth.width(coordinateSystem, width.asDegrees());
    }

    public CoordinateSize toCoordinates(final Dimensioned size)
    {
        return size(
                size.height().asDegrees(),
                size.width().asDegrees());
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateHeight height)
    {
        return coordinateSystem.toDrawingUnits(height);
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateWidth width)
    {
        return coordinateSystem.toDrawingUnits(width);
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateDistance distance)
    {
        return coordinateSystem.toDrawingUnits(distance);
    }

    @Override
    public DrawingPoint toDrawingUnits(final Coordinate coordinate)
    {
        return coordinateSystem.toDrawingUnits(coordinate);
    }

    @Override
    public DrawingSize toDrawingUnits(final CoordinateSize coordinate)
    {
        return coordinateSystem.toDrawingUnits(coordinate);
    }

    public DrawingDistance toDrawingUnits(final Height height)
    {
        return coordinateSystem.toDrawingUnits(toCoordinates(height));
    }

    public DrawingDistance toDrawingUnits(final Width width)
    {
        return coordinateSystem.toDrawingUnits(toCoordinates(width));
    }

    public DrawingDistance toDrawingUnits(final Distance distance)
    {
        return coordinateSystem.toDrawingUnits(toCoordinates(distance));
    }

    public DrawingPoint toDrawingUnits(final Location location)
    {
        return coordinateSystem.toDrawingUnits(toCoordinates(location));
    }

    public DrawingSize toDrawingUnits(final Size size)
    {
        return coordinateSystem.toDrawingUnits(toCoordinates(size));
    }

    public Location toMap(final DrawingPoint point)
    {
        return Location.degrees(point.y(), point.x());
    }

    public Distance toMap(final DrawingDistance distance)
    {
        return Distance.degrees(coordinateSystem.toCoordinates(distance).units());
    }

    public Distance width()
    {
        return bounds().widestWidth();
    }

    private Path2D path(final Polyline line)
    {
        var first = true;
        final Path2D path = new Path2D.Double();
        for (final var to : line.locationSequence())
        {
            final var point = toDrawingUnits(to);
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
