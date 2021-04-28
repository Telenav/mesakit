package com.telenav.mesakit.map.ui.desktop.graphics.canvas;

import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingSize;
import com.telenav.kivakit.ui.desktop.graphics.geometry.Coordinate;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateDistance;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateHeight;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSize;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.geometry.CoordinateWidth;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.ui.desktop.coordinates.MapCoordinateMapper;

import static com.telenav.kivakit.ui.desktop.graphics.drawing.DrawingDistance.pixels;

/**
 * @author jonathanl (shibo)
 */
public class MapCoordinateSystem implements CoordinateSystem
{
    private final MapCoordinateMapper mapper;

    public MapCoordinateSystem(final MapCoordinateMapper mapper)
    {
        this.mapper = mapper;
    }

    @Override
    public CoordinateDistance toCoordinates(final DrawingDistance distance)
    {
        return CoordinateDistance.units(this, distance.units());
    }

    @Override
    public Coordinate toCoordinates(final DrawingPoint point)
    {
        final var location = mapper.toMapLocation(point);
        return Coordinate.at(this, location.longitude().asDegrees(), location.latitude().asDegrees());
    }

    @Override
    public CoordinateSize toCoordinates(final DrawingSize size)
    {
        final var location = mapper.toMapLocation(size.asPoint());
        return CoordinateSize.size(this, location.longitude().asDegrees(), location.latitude().asDegrees());
    }

    @Override
    public DrawingPoint toDrawingUnits(final Coordinate coordinate)
    {
        return mapper.toDrawingPoint(Location.degrees(coordinate.y(), coordinate.x()));
    }

    @Override
    public DrawingSize toDrawingUnits(final CoordinateSize coordinate)
    {
        final var point = mapper.toDrawingPoint(Location.degrees(coordinate.height(), coordinate.width()));
        return DrawingSize.size(point.x(), point.y());
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateHeight height)
    {
        return pixels(mapper.toDrawingPoint(Location.degrees(height.units(), 0)).y());
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateWidth width)
    {
        return pixels(mapper.toDrawingPoint(Location.degrees(0.0, width.units())).x());
    }

    @Override
    public DrawingDistance toDrawingUnits(final CoordinateDistance distance)
    {
        return pixels(mapper.toDrawingPoint(Location.degrees(0.0, distance.units())).x());
    }
}
