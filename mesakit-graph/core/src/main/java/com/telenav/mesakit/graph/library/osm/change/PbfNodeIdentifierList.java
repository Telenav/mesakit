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

package com.telenav.mesakit.graph.library.osm.change;

import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.mesakit.graph.library.osm.change.store.PbfNodeStore;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.geography.Location;

import java.util.ArrayList;

/**
 * A list of {@link PbfNodeIdentifier}s.
 *
 * @author jonathanl (shibo)
 */
public class PbfNodeIdentifierList extends ArrayList<PbfNodeIdentifier>
{
    private static final long serialVersionUID = -5736095881373152168L;

    // Store of OSM nodes
    private final PbfNodeStore nodes;

    /**
     * @param nodes The OSM node store to work with
     * @param locations A list of locations to add
     */
    public PbfNodeIdentifierList(PbfNodeStore nodes, Iterable<Location> locations)
    {
        this.nodes = nodes;
        for (var location : locations)
        {
            append(location);
        }
    }

    /**
     * Appends the given location to this list if it's not already contained in the list
     */
    public void append(Location location)
    {
        var identifier = nodes.identifier(location);
        add(identifier);
    }

    /**
     * Prepends the given location to this list if it's not already contained in the list
     */
    public void prepend(Location location)
    {
        var identifier = nodes.identifier(location);
        add(0, identifier);
    }

    /**
     * Adds a node reference XML string to the given list for each node in this list
     *
     * @return The references
     */
    public StringList references()
    {
        var lines = new StringList();
        for (var identifier : this)
        {
            lines.add("    <nd ref=\"" + identifier + "\"/>");
        }
        return lines;
    }
}
