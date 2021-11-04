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

package com.telenav.mesakit.map.ui.desktop.graphics.canvas.projections;

import com.telenav.kivakit.ui.desktop.graphics.drawing.CoordinateSystem;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingPoint;
import com.telenav.kivakit.ui.desktop.graphics.drawing.geometry.objects.DrawingSize;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.ui.desktop.graphics.canvas.MapProjection;

/**
 * Does a simple linear mapping between a (Cartesian) {@link CoordinateSystem} and a {@link Rectangle} of the world in
 * degrees of latitude and longitude. This projection is fast, but it is inaccurate at longer distances, especially at
 * high latitudes.
 *
 * @author jonathanl (shibo)
 */
public class CartesianMapProjection implements MapProjection
{
    private final double mapTopInDegrees;

    private final double mapLeftInDegrees;

    private final double mapWidthInDegrees;

    private final double mapHeightInDegrees;

    private final double mapBottomInDegrees;

    private final Rectangle mapArea;

    private final DrawingSize drawingSize;

    /**
     * @param mapArea The map map to map to and from
     * @param drawingSize The size of the drawing area to map to and from
     */
    public CartesianMapProjection(Rectangle mapArea,
                                  DrawingSize drawingSize)
    {
        this.mapArea = mapArea;
        this.drawingSize = drawingSize;

        mapBottomInDegrees = mapArea.bottom().asDegrees();
        mapLeftInDegrees = mapArea.left().asDegrees();
        mapTopInDegrees = mapArea.top().asDegrees();
        mapWidthInDegrees = mapArea.width().asDegrees();
        mapHeightInDegrees = mapArea.height().asDegrees();
    }

    @Override
    public DrawingSize drawingSize()
    {
        return drawingSize;
    }

    @Override
    public Rectangle mapArea()
    {
        return mapArea;
    }

    @Override
    public DrawingPoint toDrawing(Location location)
    {
        // Get the latitude and longitude of the location,
        var latitude = location.latitudeInDegrees();
        var longitude = location.longitudeInDegrees();

        // convert to unit values between 0 and 1 relative to the top left,
        var latitudeUnit = (mapTopInDegrees - latitude) / mapHeightInDegrees;
        var longitudeUnit = (longitude - mapLeftInDegrees) / mapWidthInDegrees;

        // then compute the x, y location.
        var x = drawingSize.widthInUnits() * longitudeUnit;
        var y = drawingSize.heightInUnits() * latitudeUnit;

        return DrawingPoint.point(coordinates(), x, y);
    }

    @Override
    public DrawingSize toDrawing(Size size)
    {
        // Get the size in degrees,
        var latitude = size.height().asDegrees();
        var longitude = size.width().asDegrees();

        // convert to unit values,
        var latitudeUnit = latitude / mapHeightInDegrees;
        var longitudeUnit = longitude / mapWidthInDegrees;

        // then compute the x, y location.
        var width = drawingSize.widthInUnits() * longitudeUnit;
        var height = drawingSize.heightInUnits() * latitudeUnit;

        return DrawingSize.size(coordinates(), width, height);
    }

    @Override
    public Location toMap(DrawingPoint point)
    {
        DrawingPoint localPoint;
        if (coordinates().isBounded())
        {
            localPoint = point.toCoordinates(coordinates());
        }
        else
        {
            localPoint = point;
        }

        // Get the offset of the drawing point from the bottom left
        var xOffset = localPoint.x();
        var yOffset = drawingSize.heightInUnits() - localPoint.y();

        // compute a unit value between 0 and 1 from bottom left,
        var xUnit = xOffset / drawingSize.widthInUnits();
        var yUnit = yOffset / drawingSize.heightInUnits();

        // scale the map area map by the unit value,
        var latitudeOffset = mapHeightInDegrees * yUnit;
        var longitudeOffset = mapWidthInDegrees * xUnit;

        // and add the offset to the bottom left of the map map area.
        var latitude = mapBottomInDegrees + latitudeOffset;
        var longitude = mapLeftInDegrees + longitudeOffset;

        if (Latitude.isValid(latitude) && Longitude.isValid(longitude))
        {
            return Location.degrees(latitude, longitude);
        }

        return null;
    }

    private CoordinateSystem coordinates()
    {
        return drawingSize.coordinates();
    }
}
