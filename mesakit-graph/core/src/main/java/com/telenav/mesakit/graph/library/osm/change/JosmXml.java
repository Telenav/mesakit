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

import com.telenav.kivakit.kernel.language.io.IO;
import com.telenav.kivakit.resource.WritableResource;
import com.telenav.kivakit.data.formats.library.map.identifiers.PbfWayIdentifier;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Models a JOSM XML change file. Nodes, new ways and modified ways can be added and the file can be subsequently
 * saved.
 *
 * @author jonathanl (shibo)
 */
public class JosmXml
{
    // The writable resource to save to
    private final WritableResource writable;

    // Store of OSM nodes in this file
    private PbfNodeStore nodes;

    // The new ways in this file
    private final List<NewWay> newWays = new ArrayList<>();

    // Any ways to delete
    private final List<RemovedWay> removedWays = new ArrayList<>();

    // The modified ways in this file
    private final List<ModifiableWay> modifiedWays = new ArrayList<>();

    private boolean addNewTags;

    private boolean addModifiedTags;

    /**
     * @param writable The writable resource to save to
     */
    public JosmXml(final WritableResource writable)
    {
        this.writable = writable;
    }

    public void addModifiedTags(final boolean add)
    {
        addModifiedTags = add;
    }

    /**
     * Adds modified ways
     */
    public void addModifiedWays(final Collection<ModifiableWay> ways)
    {
        modifiedWays.addAll(ways);
    }

    public void addNewTags(final boolean add)
    {
        addNewTags = add;
    }

    /**
     * Adds new ways
     */
    public void addNewWays(final Collection<NewWay> ways)
    {
        newWays.addAll(ways);
    }

    /**
     * Adds OSM nodes
     */
    public void addNodes(final PbfNodeStore nodes)
    {
        this.nodes = nodes;
    }

    public void removeWay(final PbfWayIdentifier identifier)
    {
        removedWays.add(new RemovedWay(identifier));
    }

    /**
     * Saves the nodes, modified ways and new ways to this JOSM XML file.
     */
    public void save(final boolean debug)
    {
        try
        {
            // Get file output
            final var out = writable.writer(StandardCharsets.UTF_8).textWriter();

            // Header
            out.write("<?xml version='1.0' encoding='UTF-8'?>\n");
            out.write("<osm version=\"0.6\" generator=\"GraphEnhancer 1.0.0\">\n");

            // Save OSM nodes
            out.write(nodes.toString() + "\n");

            // Save modified ways
            for (final var way : modifiedWays)
            {
                out.write(way.toString(addModifiedTags, debug) + "\n");
            }

            // Save new ways
            for (final var way : newWays)
            {
                out.write(way.toString(addNewTags) + "\n");
            }

            // Save removed ways
            for (final var way : removedWays)
            {
                out.write(way.toString() + "\n");
            }

            // Footer
            out.write("</osm>\n");

            // Close
            IO.flush(out);
            IO.close(out);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Cannot write to " + writable, e);
        }
    }
}
