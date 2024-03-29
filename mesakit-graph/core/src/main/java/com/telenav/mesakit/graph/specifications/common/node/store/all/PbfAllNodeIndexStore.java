////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.specifications.common.node.store.all;

import com.telenav.kivakit.interfaces.loading.Unloadable;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToIntMap;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.AllNodeDiskCell;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.PbfAllNodeIndexDiskStore;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Precision;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

public class PbfAllNodeIndexStore implements Unloadable
{
    private final PbfAllNodeIndexDiskStore store;

    private final Map<AllNodeDiskCell, LongToIntMap> indexMapForCell = new HashMap<>();

    private final Precision precision;

    public PbfAllNodeIndexStore(GraphArchive archive, Precision precision)
    {
        ensure(archive != null);
        this.precision = precision;
        store = new PbfAllNodeIndexDiskStore(archive);
    }

    public int index(Location location)
    {
        var cell = new AllNodeDiskCell(location);
        var map = indexMapForCell.get(cell);
        if (map == null)
        {
            map = store.load(cell);
            indexMapForCell.put(cell, map);
        }
        var index = map.get(location.asLong(precision));
        if (map.isNull(index))
        {
            return -1;
        }
        return index;
    }

    public boolean isSupported()
    {
        return store.containsData();
    }

    public boolean isUnloaded()
    {
        return indexMapForCell.isEmpty();
    }

    @Override
    public void unload()
    {
        indexMapForCell.clear();
    }
}
