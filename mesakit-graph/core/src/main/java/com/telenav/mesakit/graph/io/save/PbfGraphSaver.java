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

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.MutableCount;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.resource.writing.WritableResource;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.ShapePoint;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.processing.writers.PbfWriter;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;

import java.util.Map;
import java.util.TreeMap;

public class PbfGraphSaver
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public void save(Graph graph, WritableResource resource)
    {
        if (!graph.supportsFullPbfNodeInformation())
        {
            throw new IllegalArgumentException(
                    "Graph file does not contain OSM node information required to convert to PBF\n"
                            + "To convert a graph file to a PBF, the graph file must be created with -osmNodeInformation=true");
        }
        var writer = new PbfWriter(resource, true);
        LOGGER.information("Writing bounds");
        writeBounds(graph, writer);
        LOGGER.information("Writing nodes");
        var nodes = writeNodes(graph, writer);
        LOGGER.information("Wrote $ nodes", nodes);
        LOGGER.information("Writing ways");
        var ways = writeWays(graph, writer);
        LOGGER.information("Wrote $ ways", ways);
        LOGGER.information("Writing relations");
        var relations = writeRelations(graph, writer);
        writer.close();
        LOGGER.information("Wrote $ relations", relations);
        LOGGER.information("Done");
    }

    private void writeBounds(Graph graph, PbfWriter writer)
    {
        var bounds = graph.bounds();
        writer.write(new Bound(bounds.right().asDegrees(), bounds.left().asDegrees(), bounds.top().asDegrees(),
                bounds.bottom().asDegrees(), graph.name()));
    }

    private Count writeNodes(Graph graph, PbfWriter writer)
    {
        Map<MapNodeIdentifier, ShapePoint> toWrite = new TreeMap<>();
        for (var edge : graph.forwardEdges())
        {
            for (var point : edge.shapePoints())
            {
                var identifier = point.mapIdentifier();
                if (!toWrite.containsKey(identifier))
                {
                    toWrite.put(identifier, point);
                }
            }
        }
        for (var key : toWrite.keySet())
        {
            var point = toWrite.get(key);
            writer.write(point.asPbfNode());
        }
        return Count.count(toWrite.keySet());
    }

    private Count writeRelations(Graph graph, PbfWriter writer)
    {
        var count = new MutableCount();
        for (var relation : graph.relations())
        {
            writer.write(relation.asPbfRelation());
            count.increment();
        }
        return count.asCount();
    }

    private Count writeWays(Graph graph, PbfWriter writer)
    {
        var count = new MutableCount();
        for (var identifier : graph.wayIdentifiers())
        {
            writer.write(graph.routeForWayIdentifier(identifier).asWay());
            count.increment();
        }
        return count.asCount();
    }
}
