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
    public Coordinate inCoordinates(final DrawingPoint point)
    {
        final var location = mapper.toMapLocation(point);
        return Coordinate.at(this, location.longitude().asDegrees(), location.latitude().asDegrees());
    }

    @Override
    public CoordinateSize inCoordinates(final DrawingSize size)
    {
        final var location = mapper.toMapLocation(size.asPoint());
        return CoordinateSize.size(this, location.longitude().asDegrees(), location.latitude().asDegrees());
    }

    @Override
    public CoordinateDistance inCoordinates(final DrawingDistance distance)
    {
        return CoordinateDistance.units(this, distance.units());
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateHeight height)
    {
        return DrawingDistance.of(mapper.toDrawingPoint(Location.degrees(height.units(), 0)).y());
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateWidth width)
    {
        return DrawingDistance.of(mapper.toDrawingPoint(Location.degrees(0.0, width.units())).x());
    }

    @Override
    public DrawingDistance inDrawingUnits(final CoordinateDistance distance)
    {
        return DrawingDistance.of(mapper.toDrawingPoint(Location.degrees(0.0, distance.units())).x());
    }

    @Override
    public DrawingPoint inDrawingUnits(final Coordinate coordinate)
    {
        return mapper.toDrawingPoint(Location.degrees(coordinate.y(), coordinate.x()));
    }

    @Override
    public DrawingSize inDrawingUnits(final CoordinateSize coordinate)
    {
        final var point = mapper.toDrawingPoint(Location.degrees(coordinate.height(), coordinate.width()));
        return DrawingSize.size(point.x(), point.y());
    }
}
