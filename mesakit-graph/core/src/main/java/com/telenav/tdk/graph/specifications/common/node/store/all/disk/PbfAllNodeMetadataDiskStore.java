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

package com.telenav.tdk.graph.specifications.common.node.store.all.disk;

import com.telenav.tdk.core.filesystem.Folder;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.data.formats.pbf.model.change.*;
import com.telenav.tdk.data.formats.pbf.model.tags.PbfNode;
import com.telenav.tdk.graph.io.archive.GraphArchive;
import com.telenav.tdk.graph.specifications.common.node.store.all.PbfAllNodeMetadataStore;
import com.telenav.tdk.graph.specifications.osm.OsmDataSpecification;
import com.telenav.tdk.map.geography.Location;

import java.io.*;
import java.util.*;

/**
 * Stores the version, changeset id, timestamp and user of a large number of PBF nodes in a disk folder that is
 * segmented into cells addressed by latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeMetadataDiskStore extends AllNodeDiskStore
{
    private final Map<AllNodeDiskCell, PbfNode> lastForCell = new HashMap<>();

    public PbfAllNodeMetadataDiskStore(final Folder data)
    {
        super(data);
    }

    public PbfAllNodeMetadataDiskStore(final GraphArchive archive)
    {
        super(archive);
    }

    public void add(final PbfNode node)
    {
        final var location = Location.degrees(node.latitude(), node.longitude());
        final var cell = new AllNodeDiskCell(location);
        final var out = output(cell);
        try
        {
            out.writeLong(node.changeSetIdentifier());
            out.writeInt(node.version());
            out.writeLong(node.timestamp().getTime());
            final var last = lastForCell.get(cell);
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
        catch (final IOException e)
        {
            throw new IllegalStateException("Unable to add node metadata", e);
        }
    }

    @SuppressWarnings({ "exports" })
    public PbfAllNodeMetadataStore load(final GraphArchive archive, final AllNodeDiskCell cell)
    {
        final var meta = new PbfAllNodeMetadataStore(archive);
        try (final var in = new DataInputStream(entry(cell).openForReading()))
        {
            var lastUserIdentifier = -1;
            String lastUserName = null;
            for (var index = 0; in.available() > 0; index++)
            {
                final var edge = OsmDataSpecification.get().newHeavyWeightEdge(null, index);
                edge.index(index);
                edge.pbfChangeSetIdentifier(new PbfChangeSetIdentifier(in.readLong()));
                edge.pbfRevisionNumber(new PbfRevisionNumber(in.readInt()));
                edge.lastModificationTime(Time.milliseconds(in.readLong()));
                var userIdentifier = in.readInt();
                final String userName;
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
        catch (final IOException e)
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
