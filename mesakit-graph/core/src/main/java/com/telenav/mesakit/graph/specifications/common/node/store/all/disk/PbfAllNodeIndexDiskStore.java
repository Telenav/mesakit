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

import com.telenav.kivakit.collections.primitive.map.scalars.LongToIntMap;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfNode;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.map.geography.Location;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Stores the store indexes of a large number of PBF nodes in a disk folder that is segmented into cells addressed by
 * latitude and longitude as integer values.
 *
 * @author jonathanl (shibo)
 */
public class PbfAllNodeIndexDiskStore extends AllNodeDiskStore
{
    public PbfAllNodeIndexDiskStore(final Folder data)
    {
        super(data);
    }

    public PbfAllNodeIndexDiskStore(final GraphArchive archive)
    {
        super(archive);
    }

    public void add(final PbfNode node)
    {
        final var location = Location.degrees(node.latitude(), node.longitude());
        final var out = output(new AllNodeDiskCell(location));
        try
        {
            out.writeLong(location.asLong());
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Unable to add node index", e);
        }
    }

    public LongToIntMap load(final AllNodeDiskCell cell)
    {
        final var indexForLocation = new LongToIntMap(name() + ".indexForLocation");
        indexForLocation.initialSize(Estimate._65536);
        indexForLocation.initialize();

        try (final var in = new DataInputStream(entry(cell).openForReading()))
        {
            for (var index = 0; in.available() > 0; index++)
            {
                final var location = in.readLong();
                indexForLocation.put(location, index);
            }
        }
        catch (final IOException e)
        {
            throw new IllegalStateException("Unable to load node indexes", e);
        }
        return indexForLocation;
    }

    @Override
    public String name()
    {
        return "all-nodes-index";
    }
}
