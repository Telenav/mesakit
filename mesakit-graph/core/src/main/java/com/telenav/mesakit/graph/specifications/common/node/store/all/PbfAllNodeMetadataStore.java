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

import com.telenav.kivakit.kernel.interfaces.loading.Unloadable;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.AllNodeDiskCell;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.PbfAllNodeMetadataDiskStore;
import com.telenav.mesakit.map.geography.Location;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

public class PbfAllNodeMetadataStore implements Unloadable
{
    private final PbfAllNodeMetadataDiskStore store;

    private final Map<AllNodeDiskCell, PbfAllNodeMetadata> metadataForCell = new HashMap<>();

    private final GraphArchive archive;

    public PbfAllNodeMetadataStore(final GraphArchive archive)
    {
        this.archive = archive;
        ensure(archive != null);
        store = new PbfAllNodeMetadataDiskStore(archive);
    }

    public boolean isSupported()
    {
        return store.containsData();
    }

    public boolean isUnloaded()
    {
        return metadataForCell.isEmpty();
    }

    public PbfAllNodeMetadata metadata(final Location location)
    {
        final var cell = new AllNodeDiskCell(location);
        final var metadata = metadataForCell.get(cell);
        if (metadata == null)
        {
            final var store = this.store.load(archive, cell);
            metadataForCell.put(cell, store.metadata(location));
        }
        return metadata;
    }

    @Override
    public void unload()
    {
        metadataForCell.clear();
    }
}
