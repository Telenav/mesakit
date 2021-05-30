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

package com.telenav.tdk.graph.library.osm.change;

import com.telenav.tdk.core.kernel.language.string.StringList;
import com.telenav.tdk.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.tdk.map.geography.Location;

import java.util.ArrayList;

/**
 * A list of {@link NodeIdentifier}s.
 *
 * @author jonathanl (shibo)
 */
public class PbfNodeIdentifierList extends ArrayList<NodeIdentifier>
{
    private static final long serialVersionUID = -5736095881373152168L;

    // Store of OSM nodes
    private final PbfNodeStore nodes;

    /**
     * @param nodes The OSM node store to work with
     * @param locations A list of locations to add
     */
    public PbfNodeIdentifierList(final PbfNodeStore nodes, final Iterable<Location> locations)
    {
        this.nodes = nodes;
        for (final var location : locations)
        {
            append(location);
        }
    }

    /**
     * Appends the given location to this list if it's not already contained in the list
     */
    public void append(final Location location)
    {
        final var identifier = nodes.identifier(location);
        add(identifier);
    }

    /**
     * Prepends the given location to this list if it's not already contained in the list
     */
    public void prepend(final Location location)
    {
        final var identifier = nodes.identifier(location);
        add(0, identifier);
    }

    /**
     * Adds a node reference XML string to the given list for each node in this list
     *
     * @return The references
     */
    public StringList references()
    {
        final var lines = new StringList();
        for (final var identifier : this)
        {
            lines.add("    <nd ref=\"" + identifier + "\"/>");
        }
        return lines;
    }
}
