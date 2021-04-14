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

package com.telenav.aonia.map.ui.swing.map.coordinates;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.shape.rectangle.Height;
import com.telenav.aonia.map.geography.shape.rectangle.Size;
import com.telenav.aonia.map.geography.shape.rectangle.Width;
import com.telenav.aonia.map.ui.swing.map.coordinates.mappers.SwingMercatorCoordinateMapper;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTile;
import com.telenav.aonia.map.ui.swing.map.tiles.SlippyTileCoordinateSystem;
import org.junit.Test;

public class SwingMercatorCoordinateMapperTest extends BaseCoordinateMapperTest
{
    @Test
    public void testSmall()
    {
        // 10x10 degree square
        final var width = Width.degrees(10);
        final var height = Height.degrees(10);
        final var size = new Size(width, height);

        // Map from 100,100:200,200 swing rectangle to 0,0:10,10 geographic rectangle
        final var mapper = new SwingMercatorCoordinateMapper(Location.ORIGIN.rectangle(size),
                awtRectangle(100, 100, 100, 100), SlippyTile.STANDARD_TILE_SIZE);

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(mapper, point(100, 100), Location.ORIGIN.offset(height));
        checkMapping(mapper, point(100, 200), Location.ORIGIN);
        checkMapping(mapper, point(200, 100), Location.ORIGIN.offset(size));
        checkMapping(mapper, point(200, 200), Location.ORIGIN.offset(width));
    }

    @Test
    public void testWorld()
    {
        final var mapper = new SwingMercatorCoordinateMapper(SlippyTileCoordinateSystem.BOUNDS,
                awtRectangle(100, 100, 100, 100), SlippyTile.STANDARD_TILE_SIZE);

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
        final var mapper = new SwingMercatorCoordinateMapper(seattle, awtRectangle(100, 100, 100, 100),
                SlippyTile.STANDARD_TILE_SIZE);

        // Ensure simple cases in the corners (the middle will be distorted by the projection)
        checkMapping(mapper, point(100, 100), seattle.topLeft());
        checkMapping(mapper, point(100, 200), seattle.bottomLeft());
        checkMapping(mapper, point(200, 100), seattle.topRight());
        checkMapping(mapper, point(200, 200), seattle.bottomRight());
    }
}
