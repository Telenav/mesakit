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

import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.swing.graphics.drawing.DrawingSize;
import com.telenav.kivakit.ui.swing.graphics.drawing.awt.AwtDrawingSurface;
import com.telenav.kivakit.ui.swing.graphics.drawing.drawables.Box;
import com.telenav.kivakit.ui.swing.graphics.drawing.drawables.Dot;
import com.telenav.kivakit.ui.swing.graphics.drawing.drawables.Label;
import com.telenav.kivakit.ui.swing.graphics.drawing.drawables.Text;
import com.telenav.kivakit.ui.swing.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.swing.graphics.geometry.CoordinateDistance;
import com.telenav.kivakit.ui.swing.graphics.geometry.CoordinateHeight;
import com.telenav.kivakit.ui.swing.graphics.geometry.CoordinateSize;
import com.telenav.kivakit.ui.swing.graphics.geometry.CoordinateSlope;
import com.telenav.kivakit.ui.swing.graphics.geometry.CoordinateWidth;
import com.telenav.kivakit.ui.swing.graphics.style.Style;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Dimensioned;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Path2D;

@SuppressWarnings({ "ConstantConditions" })
public class MapCanvas extends AwtDrawingSurface
{
    private final MapScale scale;

    private final Rectangle bounds;

    private final MapCoordinateSystem coordinateSystem;

    public MapCanvas(final Graphics2D graphics,
                     final Rectangle bounds,
                     final MapScale scale,
                     final MapCoordinateMapper mapper)
    {
        super(graphics);

        coordinateSystem = new MapCoordinateSystem(mapper);

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
                .at(inCoordinates(rectangle.topLeft()))
                .withSize(inCoordinates(rectangle))
                .draw(this);
    }

    public Shape drawDot(final Style style,
                         final Location at,
                         final Distance radius)
    {
        return Dot.dot(style)
                .at(inCoordinates(at))
                .withRadius(inCoordinates(radius))
                .draw(this);
    }

    public Shape drawLabel(final Style style, final Location location, final String text)
    {
        return Label.label(style, text)
                .at(inCoordinates(location))
                .draw(this);
    }

    public Shape drawPolyline(final Style style, final Polyline line)
    {
        return drawPath(style, path(line));
    }

    public Shape drawText(final Style style, final Location at, final String text)
    {
        return Text.text(style, text)
                .at(inCoordinates(at))
                .draw(this);
    }

    public Coordinate inCoordinates(final Location location)
    {
        return at(location.longitudeInDegrees(), location.latitudeInDegrees());
    }

    public CoordinateDistance inCoordinates(final Distance distance)
    {
        return distance(distance.asDegrees());
    }

    @Override
    public Coordinate inCoordinates(final DrawingPoint point)
    {
        return coordinateSystem.inCoordinates(point);
    }

    @Override
    public CoordinateSize inCoordinates(final DrawingSize size)
    {
        return coordinateSystem.inCoordinates(size);
    }

    @Override
    public CoordinateDistance inCoordinates(final DrawingDistance distance)
    {
        return coordinateSystem.inCoordinates(distance);
    }

    public CoordinateSize inCoordinates(final Size size)
    {
        return size(size.width().asDegrees(), size.height().asDegrees());
    }

    public CoordinateSize inCoordinates(final Dimensioned size)
    {
        return size(
                size.height().asDegrees(),
                size.width().asDegrees());
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateHeight height)
    {
        return coordinateSystem.inDrawingUnits(height);
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateWidth width)
    {
        return coordinateSystem.inDrawingUnits(width);
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateDistance distance)
    {
        return coordinateSystem.inDrawingUnits(distance);
    }

    @Override
    public DrawingPoint inDrawingUnits(final Coordinate coordinate)
    {
        return coordinateSystem.inDrawingUnits(coordinate);
    }

    @Override
    public DrawingSize inDrawingUnits(final CoordinateSize coordinate)
    {
        return coordinateSystem.inDrawingUnits(coordinate);
    }

    public Location location(final DrawingPoint point)
    {
        return Location.degrees(point.y(), point.x());
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
            final var point = inDrawingUnits(inCoordinates(to));
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
