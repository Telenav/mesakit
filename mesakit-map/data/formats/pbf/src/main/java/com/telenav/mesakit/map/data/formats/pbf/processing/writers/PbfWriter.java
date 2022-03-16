////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.processing.writers;

import com.telenav.kivakit.resource.WritableResource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfProcessing;
import crosby.binary.osmosis.OsmosisSerializer;
import org.openstreetmap.osmosis.core.container.v0_6.BoundContainer;
import org.openstreetmap.osmosis.core.container.v0_6.NodeContainer;
import org.openstreetmap.osmosis.core.container.v0_6.RelationContainer;
import org.openstreetmap.osmosis.core.container.v0_6.WayContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.osmbinary.file.BlockOutputStream;

import java.util.Collection;
import java.util.List;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
public class PbfWriter
{
    @UmlClassDiagram(diagram = DiagramPbfProcessing.class)
    private enum Phase
    {
        NODES,
        WAYS,
        RELATIONS
    }

    private OsmosisSerializer serializer;

    @UmlAggregation
    private final WritableResource resource;

    @UmlAggregation
    private Phase phase;

    public PbfWriter(WritableResource resource, boolean wayNodeLocations)
    {
        this.resource = resource;
        var output = new BlockOutputStream(resource.openForWriting());
        serializer = new OsmosisSerializer(output);
        serializer.setUseDense(true);
        phase = Phase.NODES;
    }

    public synchronized void close()
    {
        if (serializer != null)
        {
            serializer.complete();
            serializer.close();
            serializer = null;
        }
    }

    public WritableResource resource()
    {
        return resource;
    }

    public synchronized void write(Bound bound)
    {
        serializer.process(new BoundContainer(bound));
    }

    public synchronized void write(PbfNode node)
    {
        if (phase != Phase.NODES)
        {
            fail("Node output is already finished");
        }
        serializer.process(new NodeContainer(node.get()));
    }

    public synchronized void write(PbfRelation relation)
    {
        if (phase == Phase.WAYS)
        {
            phase = Phase.RELATIONS;
        }
        if (phase != Phase.RELATIONS)
        {
            fail("Relation output is already finished");
        }
        // With the default batch limit of 4,000, there are runtime issues with very long relations
        serializer.configBatchLimit(100);
        serializer.process(new RelationContainer(relation.get()));
    }

    public synchronized void write(PbfWay way)
    {
        if (phase == Phase.NODES)
        {
            phase = Phase.WAYS;
        }
        if (phase != Phase.WAYS)
        {
            fail("Way output is already finished");
        }
        serializer.process(new WayContainer(way.get()));
    }

    public synchronized void writeNodes(List<PbfNode> nodes)
    {
        for (var node : nodes)
        {
            write(node);
        }
    }

    public synchronized void writeRelations(Collection<PbfRelation> relations)
    {
        for (var relation : relations)
        {
            write(relation);
        }
    }

    public synchronized void writeWays(List<PbfWay> ways)
    {
        for (var way : ways)
        {
            write(way);
        }
    }
}
