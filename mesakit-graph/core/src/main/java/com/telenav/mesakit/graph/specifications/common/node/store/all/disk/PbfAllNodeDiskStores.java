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

package com.telenav.mesakit.graph.specifications.common.node.store.all.disk;

import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.resource.FileName;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;

public class PbfAllNodeDiskStores
{
    public static Folder temporary()
    {
        return Folder.temporaryFolderForProcess(Folder.FolderType.CLEAN_UP_ON_EXIT).folder("pbf-node-disk-stores");
    }

    private final PbfAllNodeIdentifierDiskStore pbfNodeIdentifierDiskStore;

    private final PbfAllNodeTagDiskStore pbfNodeTagDiskStore;

    private final PbfAllNodeMetadataDiskStore pbfNodeMetadataDiskStore;

    private final PbfAllNodeIndexDiskStore pbfNodeIndexDiskStore;

    public PbfAllNodeDiskStores(Folder folder, FileName fileName)
    {
        pbfNodeIndexDiskStore = new PbfAllNodeIndexDiskStore(folder.folder(fileName + "-node-indexes"));
        pbfNodeIdentifierDiskStore = new PbfAllNodeIdentifierDiskStore(folder.folder(fileName + "-node-identifiers"));
        pbfNodeTagDiskStore = new PbfAllNodeTagDiskStore(folder.folder(fileName + "-node-tags"));
        pbfNodeMetadataDiskStore = new PbfAllNodeMetadataDiskStore(folder.folder(fileName + "-node-metadata"));
    }

    public void add(PbfNode node)
    {
        pbfNodeIndexDiskStore.add(node);
        pbfNodeIdentifierDiskStore.add(node);
        pbfNodeTagDiskStore.add(node);
        pbfNodeMetadataDiskStore.add(node);
    }

    public boolean containsData()
    {
        return pbfNodeIdentifierDiskStore != null && pbfNodeIdentifierDiskStore.containsData();
    }

    public void delete()
    {
        pbfNodeIndexDiskStore.delete();
        pbfNodeIdentifierDiskStore.delete();
        pbfNodeTagDiskStore.delete();
        pbfNodeMetadataDiskStore.delete();
    }

    public PbfAllNodeIdentifierDiskStore pbfNodeIdentifierDiskStore()
    {
        return pbfNodeIdentifierDiskStore;
    }

    public PbfAllNodeIndexDiskStore pbfNodeIndexDiskStore()
    {
        return pbfNodeIndexDiskStore;
    }

    public PbfAllNodeMetadataDiskStore pbfNodeMetadataDiskStore()
    {
        return pbfNodeMetadataDiskStore;
    }

    public PbfAllNodeTagDiskStore pbfNodeTagDiskStore()
    {
        return pbfNodeTagDiskStore;
    }

    public void saveTo(GraphArchive archive)
    {
        pbfNodeIndexDiskStore.saveTo(archive);
        pbfNodeIdentifierDiskStore.saveTo(archive);
        pbfNodeTagDiskStore.saveTo(archive);
        pbfNodeMetadataDiskStore.saveTo(archive);
    }
}
