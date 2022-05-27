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
import com.telenav.kivakit.core.time.Time;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.specifications.common.node.store.all.PbfAllNodeMetadataStore;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfChangeSetIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.geography.Location;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores the version, change set id, timestamp and user of a large number of PBF nodes in a disk folder that is
 * segmented into cells addressed by latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeMetadataDiskStore extends AllNodeDiskStore
{
    private final Map<AllNodeDiskCell, PbfNode> lastForCell = new HashMap<>();

    public PbfAllNodeMetadataDiskStore(Folder data)
    {
        super(data);
    }

    public PbfAllNodeMetadataDiskStore(GraphArchive archive)
    {
        super(archive);
    }

    public void add(PbfNode node)
    {
        var location = Location.degrees(node.latitude(), node.longitude());
        var cell = new AllNodeDiskCell(location);
        var out = output(cell);
        try
        {
            out.writeLong(node.changeSetIdentifier());
            out.writeInt(node.version());
            out.writeLong(node.timestamp().getTime());
            var last = lastForCell.get(cell);
            if (last != null && last.user().getId() == node.user().getId())
            {
                out.writeInt(-1);
            }
            else
            {
                out.writeInt(node.user().getId());
                out.writeUTF(node.user().getName());
            }
            lastForCell.put(cell, node);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to add node metadata", e);
        }
    }

    @SuppressWarnings({ "exports" })
    public PbfAllNodeMetadataStore load(GraphArchive archive, AllNodeDiskCell cell)
    {
        var meta = new PbfAllNodeMetadataStore(archive);
        try (var in = new DataInputStream(entry(cell).openForReading()))
        {
            var lastUserIdentifier = -1;
            String lastUserName = null;
            for (var index = 0; in.available() > 0; index++)
            {
                var edge = OsmDataSpecification.get().newHeavyWeightEdge(null, index);
                edge.index(index);
                edge.pbfChangeSetIdentifier(new PbfChangeSetIdentifier(in.readLong()));
                edge.pbfRevisionNumber(new PbfRevisionNumber(in.readInt()));
                edge.lastModificationTime(Time.epochMilliseconds(in.readLong()));
                var userIdentifier = in.readInt();
                String userName;
                if (userIdentifier == -1)
                {
                    userIdentifier = lastUserIdentifier;
                    userName = lastUserName;
                }
                else
                {
                    userName = in.readUTF();
                }
                edge.pbfUserIdentifier(new PbfUserIdentifier(userIdentifier));
                edge.pbfUserName(new PbfUserName(userName));
                lastUserIdentifier = userIdentifier;
                lastUserName = userName;
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to load node metadata", e);
        }
        return meta;
    }

    @Override
    public String name()
    {
        return "all-nodes-metadata";
    }
}
