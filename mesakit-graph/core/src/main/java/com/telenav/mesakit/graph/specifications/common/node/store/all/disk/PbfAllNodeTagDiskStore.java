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
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.library.pbf.PbfNodeTagStore;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfTagCodec;
import com.telenav.mesakit.map.geography.Location;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Stores the PBF tags for a large number of PBF nodes in a disk folder that is segmented into cells addressed by
 * latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeTagDiskStore extends AllNodeDiskStore
{
    private final PbfTagCodec codec;

    public PbfAllNodeTagDiskStore(Folder data)
    {
        super(data);
        codec = null;
    }

    public PbfAllNodeTagDiskStore(GraphArchive archive, PbfTagCodec codec)
    {
        super(archive);
        this.codec = codec;
    }

    public void add(PbfNode node)
    {
        var location = Location.degrees(node.latitude(), node.longitude());
        var out = output(new AllNodeDiskCell(location));
        try
        {
            var tags = node.tagList();
            out.writeInt(tags.size());
            for (var tag : tags)
            {
                out.writeUTF(tag.getKey());
                out.writeUTF(tag.getValue());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to add node tags", e);
        }
    }

    public PbfNodeTagStore load(Graph graph, AllNodeDiskCell cell)
    {
        var store = new PbfNodeTagStore(codec);
        try (var in = new DataInputStream(entry(cell).openForReading()))
        {
            while (in.available() > 0)
            {
                var count = in.readInt();
                var tags = PbfTagList.create();
                for (var i = 0; i < count; i++)
                {
                    tags.add(new Tag(in.readUTF(), in.readUTF()));
                }
                store.add(tags);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to load node tags", e);
        }
        return store;
    }

    @Override
    public String name()
    {
        return "all-nodes-tags";
    }
}
