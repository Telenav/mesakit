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

import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import org.junit.Test;

import static com.telenav.mesakit.map.ui.desktop.tiles.SlippyTileCoordinateSystem.SLIPPY_TILE_MAP_AREA;

public class SlippyTileGridTest extends MapGeographyUnitTest
{
    @Test
    public void testZoomLevel0()
    {
        final var grid = new SlippyTileGrid(SLIPPY_TILE_MAP_AREA, ZoomLevel.FURTHEST);
        ensure(grid.contains(new SlippyTile(ZoomLevel.FURTHEST, 0, 0)));
    }

    @Test
    public void testZoomLevels()
    {
        for (var zoom = ZoomLevel.FURTHEST; zoom.level() < 4; zoom = zoom.zoomIn())
        {
            final var grid = new SlippyTileGrid(SLIPPY_TILE_MAP_AREA, zoom);
            ensureEqual((int) zoom.totalTiles(), grid.size());
        }
    }
}
