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

import com.telenav.kivakit.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.kernel.interfaces.persistence.Unloadable;
import com.telenav.kivakit.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.kivakit.graph.GraphElement;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.node.store.all.disk.*;
import com.telenav.kivakit.map.geography.Location;

import java.util.*;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public class PbfAllNodeIdentifierStore implements Unloadable
{
    private final PbfAllNodeIdentifierDiskStore store;

    private final Map<AllNodeDiskCell, LongArray> identifiersForCell = new HashMap<>();

    public PbfAllNodeIdentifierStore(final GraphArchive archive)
    {
        ensure(archive != null);
        store = new PbfAllNodeIdentifierDiskStore(archive);
    }

    public NodeIdentifier identifier(final GraphElement element, final Location location)
    {
        final var index = element.index();

        final var cell = new AllNodeDiskCell(location);
        var identifiers = identifiersForCell.get(cell);
        if (identifiers == null)
        {
            identifiers = store.load(cell);
            identifiersForCell.put(cell, identifiers);
        }
        final var identifier = identifiers.get(index);
        if (identifiers.isNull(identifier))
        {
            return null;
        }
        return new PbfNodeIdentifier(identifier);
    }

    public boolean isSupported()
    {
        return store.containsData();
    }

    public boolean isUnloaded()
    {
        return identifiersForCell.isEmpty();
    }

    @Override
    public void unload()
    {
        identifiersForCell.clear();
    }
}
