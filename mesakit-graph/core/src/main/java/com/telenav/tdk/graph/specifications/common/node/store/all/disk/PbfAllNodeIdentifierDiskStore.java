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

package com.telenav.kivakit.graph.specifications.common.node.store.all.disk;

import com.telenav.kivakit.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfNode;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.region.project.KivaKitMapRegionLimits;

import java.io.*;

/**
 * Stores the id and location of a large number of PBF nodes in a disk folder that is segmented into cells addressed by
 * latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeIdentifierDiskStore extends AllNodeDiskStore
{
    public PbfAllNodeIdentifierDiskStore(final Folder data)
    {
        super(data);
    }

    public PbfAllNodeIdentifierDiskStore(final GraphArchive archive)
    {
        super(archive);
    }

    public void add(final PbfNode node)
    {
        final var location = Location.degrees(node.latitude(), node.longitude());
        final var out = output(new AllNodeDiskCell(location));
        try
        {
            out.writeLong(node.identifierAsLong());
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Unable to add node identifier", e);
        }
    }

    public LongArray load(final AllNodeDiskCell cell)
    {
        final var identifiers = new LongArray(name() + ".identifiers");
        identifiers.initialSize(KivaKitMapRegionLimits.ESTIMATED_NODES);
        identifiers.initialize();

        try (final var in = new DataInputStream(entry(cell).openForReading()))
        {
            while (in.available() > 0)
            {
                identifiers.add(in.readLong());
            }
        }
        catch (final IOException e)
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
