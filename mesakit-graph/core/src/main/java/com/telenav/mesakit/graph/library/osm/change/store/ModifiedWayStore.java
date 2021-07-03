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

package com.telenav.mesakit.graph.library.osm.change.store;

import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.library.osm.change.MutableWay;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.data.formats.pbf.processing.writers.PbfWriter;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A store of {@link MutableWay}s that can be retrieved by Edge.
 *
 * @author jonathanl (shibo)
 */
public class ModifiedWayStore
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    // The node store to work with
    private final PbfNodeStore nodes;

    // Modified ways stored by OSM way identifier
    private final Map<PbfWayIdentifier, MutableWay> modifiedWayForWayIdentifier = new HashMap<>();

    /**
     * @param nodes The node store to work with
     */
    public ModifiedWayStore(final PbfNodeStore nodes)
    {
        this.nodes = nodes;
    }

    public List<PbfWay> asWays(final boolean modified)
    {
        final List<PbfWay> ways = new ArrayList<>();
        for (final var way : modifiedWays())
        {
            ways.add(way.asPbfWay());
        }
        return ways;
    }

    public boolean isEmpty()
    {
        return modifiedWays().isEmpty();
    }

    /**
     * @return The {@link MutableWay} for the given edge
     */
    public MutableWay modifiableWay(final Edge edge)
    {
        // Get any existing modified way
        var way = modifiedWayForWayIdentifier.get(edge.wayIdentifier());

        // and if none exists
        if (way == null)
        {
            // create one and store it in the map
            way = new MutableWay(nodes, edge);
            modifiedWayForWayIdentifier.put(edge.wayIdentifier(), way);
        }

        return way;
    }

    /**
     * @return The {@link MutableWay} for the given edge
     */
    public MutableWay modifiableWay(final PbfUserIdentifier userIdentifier, final PbfUserName userName,
                                    final PbfWayIdentifier identifier, final Polyline shape, final PbfTagList tags,
                                    final int version)
    {
        // Get any existing modified way
        var way = modifiedWayForWayIdentifier.get(identifier);

        // and if none exists
        if (way == null)
        {
            // create one and store it in the map
            way = new MutableWay(nodes, userIdentifier, userName, identifier, shape, tags, version);
            modifiedWayForWayIdentifier.put(identifier, way);
        }

        return way;
    }

    /**
     * @return Any modified {@link MutableWay}s in this store
     */
    public Collection<MutableWay> modifiedWays()
    {
        final List<MutableWay> modified = new ArrayList<>();
        for (final var way : modifiedWayForWayIdentifier.values())
        {
            if (way.isModified())
            {
                modified.add(way);
            }
        }
        return modified;
    }

    public PbfNodeStore nodes()
    {
        return nodes;
    }

    public void saveAll(final PbfWriter writer)
    {
        save(writer, false);
    }

    public void saveModified(final PbfWriter writer)
    {
        save(writer, true);
    }

    public Count size()
    {
        return Count.count(modifiedWayForWayIdentifier.size());
    }

    private void save(final PbfWriter writer, final boolean modified)
    {
        LOGGER.information("Preparing $ nodes", nodes.size());
        final var nodes = this.nodes.asNodes();
        LOGGER.information("Saving $ nodes", nodes.size());
        writer.writeNodes(nodes);
        LOGGER.information("Preparing $ ways", size());
        final var ways = asWays(modified);
        LOGGER.information("Saving $ ways", ways.size());
        writer.writeWays(ways);
    }
}