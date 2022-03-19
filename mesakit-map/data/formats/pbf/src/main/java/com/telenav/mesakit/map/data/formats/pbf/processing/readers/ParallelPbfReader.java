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

package com.telenav.mesakit.map.data.formats.pbf.processing.readers;

import com.telenav.kivakit.core.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.thread.Batcher;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.resource.Resource;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataStatistics;
import com.telenav.mesakit.map.data.formats.pbf.lexakai.DiagramPbfProcessing;

/**
 * A parallel version of {@link SerialPbfReader}. Entities are read with a {@link SerialPbfReader} and inserted into a
 * {@link Batcher} for each entity type. The Batcher then dispatches nodes, ways and relations to the {@link
 * PbfDataProcessor} passed to {@link #process(PbfDataProcessor)}.
 */
@SuppressWarnings("SpellCheckingInspection")
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
public class ParallelPbfReader extends BaseRepeater implements PbfDataSource
{
    private static final Count BATCH_SIZE = Count._65_536;

    private static final Maximum QUEUE_SIZE = Maximum._4;

    private PbfDataProcessor processor;

    private final Batcher<PbfNode> nodeBatcher = Batcher.<PbfNode>create()
            .withName("Node")
            .withQueueSize(QUEUE_SIZE)
            .withBatchSize(BATCH_SIZE)
            .withConsumer(batch -> outer().processor.onNodes(batch));

    private final Batcher<PbfWay> wayBatcher = Batcher.<PbfWay>create()
            .withName("Way")
            .withQueueSize(QUEUE_SIZE)
            .withBatchSize(BATCH_SIZE)
            .withConsumer(batch -> outer().processor.onWays(batch));

    private final Batcher<PbfRelation> relationBatcher = Batcher.<PbfRelation>create()
            .withName("Relation")
            .withQueueSize(QUEUE_SIZE)
            .withBatchSize(BATCH_SIZE)
            .withConsumer(batch -> outer().processor.onRelations(batch));

    private final Resource resource;

    private final Count threads;

    private final BasePbfReader reader;

    public ParallelPbfReader(Resource resource, Count threads)
    {
        this.resource = resource;
        this.threads = threads;

        reader = new SerialPbfReader(this.resource);
        reader.addListener(this);
    }

    @Override
    public void expectedNodes(Count nodes)
    {
        reader.expectedNodes(nodes);
    }

    @Override
    public void expectedRelations(Count relations)
    {
        reader.expectedRelations(relations);
    }

    @Override
    public void expectedWays(Count ways)
    {
        reader.expectedWays(ways);
    }

    @Override
    public Count nodes()
    {
        return reader.nodes();
    }

    public void nodes(Count nodes)
    {
        reader.expectedNodes(nodes);
    }

    @Override
    public void onEnd()
    {
    }

    @Override
    public PbfDataStatistics onProcess(PbfDataProcessor dataProcessor)
    {
        processor = dataProcessor;

        nodeBatcher.start(threads);
        wayBatcher.start(threads);
        relationBatcher.start(threads);

        // reading the data,
        trace("Reading data");
        var statistics = read();
        trace("Done reading data");

        return statistics;
    }

    @Override
    public void onStart()
    {
    }

    @Override
    public void phase(String phase)
    {
        reader.phase(phase);
    }

    public void relations(Count relations)
    {
        reader.expectedRelations(relations);
    }

    @Override
    public Count relations()
    {
        return reader.relations();
    }

    @Override
    public Resource resource()
    {
        return resource;
    }

    public void ways(Count ways)
    {
        reader.expectedWays(ways);
    }

    @Override
    public Count ways()
    {
        return reader.ways();
    }

    private ParallelPbfReader outer()
    {
        return this;
    }

    /**
     * Reads the data with a {@link SerialPbfReader}
     *
     * @return Node, way and relation statistics from the read data
     */
    private PbfDataStatistics read()
    {
        var outer = this;

        var nodeAdder = nodeBatcher.adder();
        var wayAdder = wayBatcher.adder();
        var relationAdder = relationBatcher.adder();

        return reader.process(new PbfDataProcessor()
        {
            @Override
            public void onEndNodes()
            {
                outer.nodeBatcher.stop();
                trace("End of nodes");
                outer.processor.onEndNodes();
            }

            @Override
            public void onEndRelations()
            {
                outer.relationBatcher.stop();
                trace("End of relations");
                outer.processor.onEndRelations();
            }

            @Override
            public void onEndWays()
            {
                outer.wayBatcher.stop();
                trace("End of ways");
                outer.processor.onEndWays();
            }

            @Override
            public Action onNode(PbfNode node)
            {
                nodeAdder.add(node);
                return Action.ACCEPTED;
            }

            @Override
            public Action onRelation(PbfRelation relation)
            {
                relationAdder.add(relation);
                return Action.ACCEPTED;
            }

            @Override
            public Action onWay(PbfWay way)
            {
                wayAdder.add(way);
                return Action.ACCEPTED;
            }
        });
    }
}
