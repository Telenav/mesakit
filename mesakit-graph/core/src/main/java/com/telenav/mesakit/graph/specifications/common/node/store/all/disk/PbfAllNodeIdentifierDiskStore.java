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
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.RegionLimits;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Stores the id and location of a large number of PBF nodes in a disk folder that is segmented into cells addressed by
 * latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeIdentifierDiskStore extends AllNodeDiskStore
{
    public PbfAllNodeIdentifierDiskStore(Folder data)
    {
        super(data);
    }

    public PbfAllNodeIdentifierDiskStore(GraphArchive archive)
    {
        super(archive);
    }

    public void add(PbfNode node)
    {
        var location = Location.degrees(node.latitude(), node.longitude());
        var out = output(new AllNodeDiskCell(location));
        try
        {
            out.writeLong(node.identifierAsLong());
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to add node identifier", e);
        }
    }

    public LongArray load(AllNodeDiskCell cell)
    {
        var identifiers = new LongArray(name() + ".identifiers");
        identifiers.initialSize(RegionLimits.ESTIMATED_NODES);
        identifiers.initialize();

        try (var in = new DataInputStream(entry(cell).openForReading()))
        {
            while (in.available() > 0)
            {
                identifiers.add(in.readLong());
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Unable to load node identifiers", e);
        }

        return identifiers;
    }

    @Override
    public String name()
    {
        return "all-nodes-identifier";
    }
}
