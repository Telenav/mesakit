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

package com.telenav.mesakit.map.ui.desktop.coordinates;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Height;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.rectangle.Size;
import com.telenav.mesakit.map.geography.shape.rectangle.Width;
import com.telenav.mesakit.map.ui.desktop.coordinates.mappers.CartesianCoordinateMapper;
import com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem;
import org.junit.Test;

public class SwingCartesianCoordinateMapperTest extends BaseCoordinateMapperTest
{
    @Test
    public void testLocationForPoint1()
    {
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(Rectangle.MAXIMUM,
                awtRectangle(0, 0, 100, 100));

        ensureEqual(Rectangle.MAXIMUM.topLeft(), mapper.toMapLocation(point(0, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomLeft(), mapper.toMapLocation(point(0, 100)));
        ensureEqual(Rectangle.MAXIMUM.topRight(), mapper.toMapLocation(point(100, 0)));
        ensureEqual(Rectangle.MAXIMUM.bottomRight(), mapper.toMapLocation(point(100, 100)));

        ensureEqual(Location.ORIGIN, mapper.toMapLocation(point(50, 50)));
    }

    @Test
    public void testLocationForPoint2()
    {
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(Rectangle.MAXIMUM,
                awtRectangle(100, 100, 100, 100));

        ensureEqual(Rectangle.MAXIMUM.topLeft(), mapper.toMapLocation(point(100, 100)));
        ensureEqual(Rectangle.MAXIMUM.bottomLeft(), mapper.toMapLocation(point(100, 200)));
        ensureEqual(Rectangle.MAXIMUM.topRight(), mapper.toMapLocation(point(200, 100)));
        ensureEqual(Rectangle.MAXIMUM.bottomRight(), mapper.toMapLocation(point(200, 200)));

        ensureEqual(Location.ORIGIN, mapper.toMapLocation(point(150, 150)));
    }

    @Test
    public void testPointForLocation1()
    {
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(Rectangle.MAXIMUM,
                awtRectangle(0, 0, 100, 100));

        ensureEqual(point(0, 0), mapper.toDrawingPoint(Rectangle.MAXIMUM.topLeft()));
        ensureEqual(point(0, 100), mapper.toDrawingPoint(Rectangle.MAXIMUM.bottomLeft()));
        ensureEqual(point(100, 0), mapper.toDrawingPoint(Rectangle.MAXIMUM.topRight()));
        ensureEqual(point(100, 100), mapper.toDrawingPoint(Rectangle.MAXIMUM.bottomRight()));

        ensureEqual(point(50, 50), mapper.toDrawingPoint(Location.ORIGIN));
    }

    @Test
    public void testPointForLocation2()
    {
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(Rectangle.MAXIMUM,
                awtRectangle(100, 100, 100, 100));

        ensureEqual(point(100, 100), mapper.toDrawingPoint(Rectangle.MAXIMUM.topLeft()));
        ensureEqual(point(100, 200), mapper.toDrawingPoint(Rectangle.MAXIMUM.bottomLeft()));
        ensureEqual(point(200, 100), mapper.toDrawingPoint(Rectangle.MAXIMUM.topRight()));
        ensureEqual(point(200, 200), mapper.toDrawingPoint(Rectangle.MAXIMUM.bottomRight()));

        ensureEqual(point(150, 150), mapper.toDrawingPoint(Location.ORIGIN));
    }

    @Test
    public void testSmall()
    {
        // 10x10 degree square
        final var width = Width.degrees(10);
        final var height = Height.degrees(10);
        final var size = new Size(width, height);

        // Map from 100,100:200,200 swing rectangle to 0,0:10,10 geographic rectangle
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(
                Location.ORIGIN.rectangle(size), awtRectangle(100, 100, 100, 100));

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(mapper, point(100, 100), Location.ORIGIN.offset(height));
        checkMapping(mapper, point(100, 200), Location.ORIGIN);
        checkMapping(mapper, point(200, 100), Location.ORIGIN.offset(size));
        checkMapping(mapper, point(200, 200), Location.ORIGIN.offset(width));
    }

    @Test
    public void testWorld()
    {
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(
                SlippyTileCoordinateSystem.BOUNDS, awtRectangle(100, 100, 100, 100));

        // The origin will not be distorted
        checkMapping(mapper, point(150, 150), Location.ORIGIN);

        // Ensure simple corner cases where there is no distortion
        checkMapping(mapper, point(100, 100), SlippyTileCoordinateSystem.BOUNDS.topLeft());
        checkMapping(mapper, point(200, 100), SlippyTileCoordinateSystem.BOUNDS.topRight());
        checkMapping(mapper, point(100, 200), SlippyTileCoordinateSystem.BOUNDS.bottomLeft());
        checkMapping(mapper, point(200, 200), SlippyTileCoordinateSystem.BOUNDS.bottomRight());
    }

    @Test
    public void testZoomedIn()
    {
        // 1x1 degree square
        final var width = Width.degrees(1);
        final var height = Height.degrees(1);
        final var size = new Size(width, height);
        final var seattle = Location.degrees(47.601765, -122.332335).rectangle(size);

        // Map from 100,100:200,200 swing rectangle to 47.601765,-122.332335:48.601765,-121.332335
        // geographic rectangle around downtown Seattle
        final CartesianCoordinateMapper mapper = new CartesianCoordinateMapper(seattle,
                awtRectangle(100, 100, 100, 100));

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(mapper, point(100, 100), seattle.topLeft());
        checkMapping(mapper, point(100, 200), seattle.bottomLeft());
        checkMapping(mapper, point(200, 100), seattle.topRight());
        checkMapping(mapper, point(200, 200), seattle.bottomRight());
    }
}
