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

import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.filesystem.FileCache;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.resource.CopyMode;
import com.telenav.kivakit.resource.Resource;

/**
 * A persistent cache of tile images.
 *
 * @author jonathanl (shibo)
 */
public class SlippyTileCache extends FileCache
{
    /**
     * @param folder The cache folder
     */
    public SlippyTileCache(Folder folder)
    {
        super(folder);
        startPruner();
    }

    /**
     * Add the given tile image to this cache
     */
    public File add(SlippyTile tile, Resource image)
    {
        return addAs(image, tile.asFileName(), CopyMode.OVERWRITE);
    }

    /**
     * @return Input stream to read the given tile if it exists in this cache or null if it doesn't
     */
    public Resource resource(SlippyTile tile)
    {
        var file = file(tile.asFileName());
        return file.exists() ? file : null;
    }
}
