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

package com.telenav.mesakit.graph.io.save;

import com.telenav.kivakit.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.kivakit.data.formats.pbf.processing.writers.PbfWriter;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.resource.WritableResource;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.ShapePoint;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

import java.util.Map;
import java.util.TreeMap;

public class PbfGraphSaver
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public void save(final Graph graph, final WritableResource resource)
    {
        if (!graph.supportsFullPbfNodeInformation())
        {
            throw new IllegalArgumentException(
                    "Graph file does not contain OSM node information required to convert to PBF\n"
                            + "To convert a graph file to a PBF, the graph file must be created with -osmNodeInformation=true");
        }
        final var writer = new PbfWriter(resource, true);
        LOGGER.information("Writing bounds");
        writeBounds(graph, writer);
        LOGGER.information("Writing nodes");
        final var nodes = writeNodes(graph, writer);
        LOGGER.information("Wrote $ nodes", nodes);
        LOGGER.information("Writing ways");
        final var ways = writeWays(graph, writer);
        LOGGER.information("Wrote $ ways", ways);
        LOGGER.information("Writing relations");
        final var relations = writeRelations(graph, writer);
        writer.close();
        LOGGER.information("Wrote $ relations", relations);
        LOGGER.information("Done");
    }

    private void writeBounds(final Graph graph, final PbfWriter writer)
    {
        final var bounds = graph.bounds();
        writer.write(new Bound(bounds.right().asDegrees(), bounds.left().asDegrees(), bounds.top().asDegrees(),
                bounds.bottom().asDegrees(), graph.name()));
    }

    private Count writeNodes(final Graph graph, final PbfWriter writer)
    {
        final Map<NodeIdentifier, ShapePoint> toWrite = new TreeMap<>();
        for (final var edge : graph.forwardEdges())
        {
            for (final var point : edge.shapePoints())
            {
                final var identifier = point.mapIdentifier();
                if (!toWrite.containsKey(identifier))
                {
                    toWrite.put(identifier, point);
                }
            }
        }
        for (final var key : toWrite.keySet())
        {
            final var point = toWrite.get(key);
            writer.write(point.asPbfNode());
        }
        return Count.count(toWrite.keySet());
    }

    private Count writeRelations(final Graph graph, final PbfWriter writer)
    {
        final var count = new MutableCount();
        for (final var relation : graph.relations())
        {
            writer.write(relation.asPbfRelation());
            count.increment();
        }
        return count.asCount();
    }

    private Count writeWays(final Graph graph, final PbfWriter writer)
    {
        final var count = new MutableCount();
        for (final var identifier : graph.wayIdentifiers())
        {
            writer.write(graph.routeForWayIdentifier(identifier).asWay());
            count.increment();
        }
        return count.asCount();
    }
}
