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

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem;
import org.junit.Test;

public class CartesianMapProjectionTest extends BaseCoordinateMapperTest
{
    @Test
    public void testLocationForPoint1()
    {
        CartesianMapProjection projection = new CartesianMapProjection(Rectangle.MAXIMUM,
                drawingSize(100, 100));

        ensureEqual(Rectangle.MAXIMUM.topLeft(), projection.toMap(point(0, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomLeft(), projection.toMap(point(0, 100)));
        ensureEqual(Rectangle.MAXIMUM.topRight(), projection.toMap(point(100, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomRight(), projection.toMap(point(100, 100)));

        ensureEqual(Location.ORIGIN, projection.toMap(point(50, 50)));
    }

    @Test
    public void testLocationForPoint2()
    {
        CartesianMapProjection projection = new CartesianMapProjection(Rectangle.MAXIMUM,
                drawingSize(100, 100));

        ensureEqual(Rectangle.MAXIMUM.topLeft(), projection.toMap(point(0, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomLeft(), projection.toMap(point(0, 100)));
        ensureEqual(Rectangle.MAXIMUM.topRight(), projection.toMap(point(100, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomRight(), projection.toMap(point(100, 100)));

        ensureEqual(Location.ORIGIN, projection.toMap(point(50, 50)));
    }

    @Test
    public void testPointForLocation1()
    {
        CartesianMapProjection projection = new CartesianMapProjection(Rectangle.MAXIMUM,
                drawingSize(100, 100));

        ensureEqual(point(0, 0), projection.toDrawing(Rectangle.MAXIMUM.topLeft()));
        ensureEqual(point(0, 100), projection.toDrawing(Rectangle.MAXIMUM.bottomLeft()));
        ensureEqual(point(100, 0), projection.toDrawing(Rectangle.MAXIMUM.topRight()));
        ensureEqual(point(100, 100), projection.toDrawing(Rectangle.MAXIMUM.bottomRight()));

        ensureEqual(point(50, 50), projection.toDrawing(Location.ORIGIN));
    }

    @Test
    public void testPointForLocation2()
    {
        CartesianMapProjection projection = new CartesianMapProjection(Rectangle.MAXIMUM,
                drawingSize(100, 100));

        ensureEqual(point(0, 0), projection.toDrawing(Rectangle.MAXIMUM.topLeft()));
        ensureEqual(point(0, 100), projection.toDrawing(Rectangle.MAXIMUM.bottomLeft()));
        ensureEqual(point(100, 0), projection.toDrawing(Rectangle.MAXIMUM.topRight()));
        ensureEqual(point(100, 100), projection.toDrawing(Rectangle.MAXIMUM.bottomRight()));

        ensureEqual(point(50, 50), projection.toDrawing(Location.ORIGIN));
    }

    @Test
    public void testSmall()
    {
        // 10x10 degree square
        var width = Width.degrees(10);
        var height = Height.degrees(10);
        var size = new Size(width, height);

        // Map from 100,100:200,200 swing rectangle to 0,0:10,10 geographic rectangle
        CartesianMapProjection projection = new CartesianMapProjection(
                Location.ORIGIN.rectangle(size), drawingSize(100, 100));

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(projection, point(0, 0), Location.ORIGIN.offsetBy(height));
        checkMapping(projection, point(0, 100), Location.ORIGIN);
        checkMapping(projection, point(100, 0), Location.ORIGIN.offsetBy(size));
        checkMapping(projection, point(100, 100), Location.ORIGIN.offsetBy(width));
    }

    @Test
    public void testWorld()
    {
        CartesianMapProjection projection = new CartesianMapProjection(
                SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA, drawingSize(100, 100));

        // The origin will not be distorted
        checkMapping(projection, point(50, 50), Location.ORIGIN);

        // Ensure simple corner cases where there is no distortion
        checkMapping(projection, point(0, 0), SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA.topLeft());
        checkMapping(projection, point(100, 0), SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA.topRight());
        checkMapping(projection, point(0, 100), SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA.bottomLeft());
        checkMapping(projection, point(100, 100), SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA.bottomRight());
    }

    @Test
    public void testZoomedIn()
    {
        // 1x1 degree square
        var width = Width.degrees(1);
        var height = Height.degrees(1);
        var size = new Size(width, height);
        var seattle = Location.degrees(47.601765, -122.332335).rectangle(size);

        // Map from 100,100:200,200 swing rectangle to 47.601765,-122.332335:48.601765,-121.332335
        // geographic rectangle around downtown Seattle
        CartesianMapProjection projection = new CartesianMapProjection(seattle,
                drawingSize(100, 100));

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(projection, point(0, 0), seattle.topLeft());
        checkMapping(projection, point(0, 100), seattle.bottomLeft());
        checkMapping(projection, point(100, 0), seattle.topRight());
        checkMapping(projection, point(100, 100), seattle.bottomRight());
    }
}
