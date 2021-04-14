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

package com.telenav.aonia.map.ui.swing.map.tiles;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.project.MapGeographyUnitTest;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import org.junit.Test;

public class SlippyTileTest extends MapGeographyUnitTest
{
    @Test
    public void test2()
    {
        final int width = ZoomLevel.CLOSEST.widthInTiles();
        ensureEqual(131072, width);
        final int height = ZoomLevel.CLOSEST.heightInTiles();
        ensureEqual(131072, height);
        final long tiles = ZoomLevel.CLOSEST.totalTiles();
        ensureEqual(131072L * 131072L, tiles);
        final var widthInPixels = width * 256;
        ensureEqual(33554432, widthInPixels);
    }

    @Test
    public void testCoordinates()
    {
        final SlippyTile tile_0_0_0 = ZoomLevel.FURTHEST.tileAt(Location.ORIGIN);
        ensure(tile_0_0_0.getX() == 0);
        ensure(tile_0_0_0.getY() == 0);
        ensure(tile_0_0_0.getZoomLevel().level() == 0);

        final SlippyTile tile_0_0_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.northWestQuadrant().center());
        ensure(tile_0_0_1.getX() == 0);
        ensure(tile_0_0_1.getY() == 0);
        ensure(tile_0_0_1.getZoomLevel().level() == 1);

        final SlippyTile tile_1_0_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.northEastQuadrant().center());
        ensure(tile_1_0_1.getX() == 1);
        ensure(tile_1_0_1.getY() == 0);
        ensure(tile_1_0_1.getZoomLevel().level() == 1);

        final SlippyTile tile_0_1_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.southWestQuadrant().center());
        ensure(tile_0_1_1.getX() == 0);
        ensure(tile_0_1_1.getY() == 1);
        ensure(tile_0_1_1.getZoomLevel().level() == 1);

        final SlippyTile tile_1_1_1 = ZoomLevel.FURTHEST.zoomIn()
                .tileAt(Rectangle.MAXIMUM.southEastQuadrant().center());
        ensure(tile_1_1_1.getX() == 1);
        ensure(tile_1_1_1.getY() == 1);
        ensure(tile_1_1_1.getZoomLevel().level() == 1);
    }
}
