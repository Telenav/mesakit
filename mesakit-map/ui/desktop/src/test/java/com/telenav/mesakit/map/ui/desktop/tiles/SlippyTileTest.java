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

package com.telenav.mesakit.map.ui.desktop.tiles;

import com.telenav.mesakit.map.geography.test.GeographyUnitTest;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import org.junit.Test;

public class SlippyTileTest extends GeographyUnitTest
{
    @Test
    public void test2()
    {
        int width = ZoomLevel.CLOSEST.widthInTiles();
        ensureEqual(131072, width);
        int height = ZoomLevel.CLOSEST.heightInTiles();
        ensureEqual(131072, height);
        long tiles = ZoomLevel.CLOSEST.totalTiles();
        ensureEqual(131072L * 131072L, tiles);
        var widthInPixels = width * 256;
        ensureEqual(33554432, widthInPixels);
    }

    @Test
    public void testCoordinates()
    {
        SlippyTile tile_0_0_0 = ZoomLevel.FURTHEST.tileAt(Location.ORIGIN);
        ensure(tile_0_0_0.x() == 0);
        ensure(tile_0_0_0.y() == 0);
        ensure(tile_0_0_0.getZoomLevel().level() == 0);

        SlippyTile tile_0_0_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.northWestQuadrant().center());
        ensure(tile_0_0_1.x() == 0);
        ensure(tile_0_0_1.y() == 0);
        ensure(tile_0_0_1.getZoomLevel().level() == 1);

        SlippyTile tile_1_0_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.northEastQuadrant().center());
        ensure(tile_1_0_1.x() == 1);
        ensure(tile_1_0_1.y() == 0);
        ensure(tile_1_0_1.getZoomLevel().level() == 1);

        SlippyTile tile_0_1_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.southWestQuadrant().center());
        ensure(tile_0_1_1.x() == 0);
        ensure(tile_0_1_1.y() == 1);
        ensure(tile_0_1_1.getZoomLevel().level() == 1);

        SlippyTile tile_1_1_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.southEastQuadrant().center());
        ensure(tile_1_1_1.x() == 1);
        ensure(tile_1_1_1.y() == 1);
        ensure(tile_1_1_1.getZoomLevel().level() == 1);
    }
}
