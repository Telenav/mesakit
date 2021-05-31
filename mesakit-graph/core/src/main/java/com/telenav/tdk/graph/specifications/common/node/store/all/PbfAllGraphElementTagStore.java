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

package com.telenav.kivakit.graph.specifications.common.node.store.all;

import com.telenav.kivakit.kernel.interfaces.persistence.Unloadable;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.kivakit.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.node.store.all.disk.*;
import com.telenav.kivakit.graph.specifications.library.pbf.PbfNodeTagStore;
import com.telenav.kivakit.map.geography.Location;

import java.util.*;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public class PbfAllGraphElementTagStore implements Unloadable
{
    private final Graph graph;

    private final PbfAllNodeTagDiskStore store;

    private final Map<AllNodeDiskCell, PbfNodeTagStore> tagStoreForCell = new WeakHashMap<>();

    public PbfAllGraphElementTagStore(final Graph graph, final GraphArchive archive, final PbfTagCodec codec)
    {
        this.graph = graph;
        ensure(archive != null);
        store = new PbfAllNodeTagDiskStore(archive, codec);
    }

    public boolean isSupported()
    {
        return store.containsData();
    }

    public boolean isUnloaded()
    {
        return tagStoreForCell.isEmpty();
    }

    public PbfTagList tags(final GraphElement element, final Location location)
    {
        final var cell = new AllNodeDiskCell(location);
        var tagStore = tagStoreForCell.get(cell);
        if (tagStore == null)
        {
            tagStore = store.load(graph, cell);
            tagStoreForCell.put(cell, tagStore);
        }
        return tagStore.get(element);
    }

    @Override
    public void unload()
    {
        tagStoreForCell.clear();
    }
}
