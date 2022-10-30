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
import com.telenav.kivakit.core.progress.ProgressReporter;
import com.telenav.kivakit.core.progress.reporters.BroadcastingProgressReporter;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.identifier.Identifier;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.Extension;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataSource;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataStatistics;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfStopProcessingException;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfProcessing;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.fail;

/**
 * Base class for PBF readers, handling progress reporting and gathering statistics.
 *
 * @see SerialPbfReader
 * @see ParallelPbfReader
 */
@UmlClassDiagram(diagram = DiagramPbfProcessing.class)
public abstract class BasePbfReader extends BaseRepeater implements PbfDataSource
{
    // Progress in processing each entity type
    private ProgressReporter nodeProgress = BroadcastingProgressReporter.progressReporter(this, "nodes");

    private ProgressReporter wayProgress = BroadcastingProgressReporter.progressReporter(this, "ways");

    private ProgressReporter relationProgress = BroadcastingProgressReporter.progressReporter(this, "relations");

    // The resource being read
    @UmlAggregation
    private final Resource resource;

    // True if we're processing a given entity type
    private boolean processingNodes;

    private boolean processingWays;

    private boolean processingRelations;

    // True if we're done processing a given entity type
    private boolean doneProcessingNodes;

    private boolean doneProcessingWays;

    private boolean doneProcessingRelations;

    // Maximum identifiers
    private long maximumNodeIdentifier;

    private long maximumWayIdentifier;

    private long maximumRelationIdentifier;

    // Statistics on nodes, ways and relations read
    @UmlAggregation
    private final PbfDataStatistics statistics;

    // Processor to call
    @UmlAggregation
    private PbfDataProcessor processor;

    private boolean started;

    private Count nodes;

    private Count ways;

    private Count relations;

    protected BasePbfReader(Resource resource)
    {
        if (!resource.hasExtension(Extension.PBF) && !resource.hasExtension(Extension.OSM_PBF))
        {
            fail("Resource '$' does not end in .osm.pbf or .pbf", resource);
        }
        this.resource = resource;
        statistics = new PbfDataStatistics(resource);
    }

    /**
     * Returns statistics on PBF data once reading has completed
     */
    public PbfDataStatistics dataStatistics()
    {
        return statistics;
    }

    /**
     * Called when the subclass is finished reading and processing data
     */
    public void end()
    {
        // It's possible we didn't get any of some entity types,
        // so we make sure the start/end methods got called here.
        trace("Done reading data");
        startProcessingNodes();
        doneProcessingNodes();
        startProcessingWays();
        doneProcessingWays();
        startProcessingRelations();
        doneProcessingRelations();
    }

    @Override
    public void expectedNodes(Count nodes)
    {
        this.nodes = nodes;
        nodeProgress.steps(nodes.asMaximum());
    }

    @Override
    public void expectedRelations(Count relations)
    {
        this.relations = relations;
        relationProgress.steps(relations.asMaximum());
    }

    @Override
    public void expectedWays(Count ways)
    {
        this.ways = ways;
        wayProgress.steps(ways.asMaximum());
    }

    /**
     * Returns the maximum node identifier encountered during reading
     */
    public Identifier maximumNodeIdentifier()
    {
        return new Identifier(maximumNodeIdentifier);
    }

    /**
     * Returns the maximum relation identifier encountered during reading
     */
    public Identifier maximumRelationIdentifier()
    {
        return new Identifier(maximumRelationIdentifier);
    }

    /**
     * Returns the maximum way identifier encountered during reading
     */
    public Identifier maximumWayIdentifier()
    {
        return new Identifier(maximumWayIdentifier);
    }

    public BasePbfReader noProgress()
    {
        nodeProgress = ProgressReporter.nullProgressReporter();
        wayProgress = ProgressReporter.nullProgressReporter();
        relationProgress = ProgressReporter.nullProgressReporter();
        return this;
    }

    @Override
    public Count nodes()
    {
        return nodes;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd()
    {
        nodeProgress.end();
        wayProgress.end();
        relationProgress.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @MustBeInvokedByOverriders
    public void onStart()
    {
        ensure(!started);
        started = true;
    }

    @Override
    public void phase(String phase)
    {
        nodeProgress.phase("  [" + phase + "] ");
        wayProgress.phase("  [" + phase + "] ");
        relationProgress.phase("  [" + phase + "] ");
    }

    /**
     * Called when the subclass has an entity to process
     */
    public void process(Entity entity)
    {
        var type = entity.getType();

        switch (type)
        {
            case Bound -> processor.onBounds((Bound) entity);
            case Node ->
            {

                // Now we're reading nodes
                startProcessingNodes();

                // Process the node
                var node = new PbfNode((Node) entity);
                processor.onEntity(node);
                processNode(node);
            }
            case Relation ->
            {

                // If we didn't get any nodes or ways, these might not have been called yet
                startProcessingNodes();
                doneProcessingNodes();
                startProcessingWays();
                doneProcessingWays();

                // Process the relation
                var relation = new PbfRelation((Relation) entity);
                processor.onEntity(relation);
                processRelation(relation);
            }
            case Way ->
            {

                // If we didn't get any nodes, these might not have been called yet
                startProcessingNodes();
                doneProcessingNodes();

                // Now we're reading ways
                startProcessingWays();

                // Process the way
                var way = new PbfWay((Way) entity);
                processor.onEntity(way);
                processWay(way);
            }
        }
    }

    public void processBounds(Bound bound)
    {
        processor.onBounds(bound);
    }

    /**
     * Called when the subclass encounters PBF metadata
     */
    public void processMetadata(Map<String, Object> metadata)
    {
        Map<String, String> properties = new HashMap<>();
        for (var key : metadata.keySet())
        {
            properties.put(key, metadata.get(key).toString());
        }
        processor.onMetadata(properties);
    }

    @Override
    public Count relations()
    {
        return relations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resource()
    {
        return resource;
    }

    public void start(PbfDataProcessor processor)
    {
        trace("Processing data");
        this.processor = processor;
    }

    @Override
    public String toString()
    {
        return resource().toString();
    }

    @Override
    public Count ways()
    {
        return ways;
    }

    private void doneProcessingNodes()
    {
        if (!doneProcessingNodes)
        {
            doneProcessingNodes = true;
            processor.onEndNodes();
            nodeProgress.end();
            wayProgress.start();
        }
    }

    private void doneProcessingRelations()
    {
        if (!doneProcessingRelations)
        {
            doneProcessingRelations = true;
            processor.onEndRelations();
            relationProgress.end();
        }
    }

    private void doneProcessingWays()
    {
        if (!doneProcessingWays)
        {
            doneProcessingWays = true;
            processor.onEndWays();
            wayProgress.end();
            relationProgress.start();
        }
    }

    private void processNode(PbfNode node)
    {
        try
        {
            // and process it
            processedNode(node, processor.onNode(node));
        }
        catch (PbfStopProcessingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            problem(e, "Exception thrown by onNode($)", node.identifierAsLong());
        }
    }

    private void processRelation(PbfRelation relation)
    {
        try
        {
            // and process it
            processedRelation(relation, processor.onRelation(relation));
        }
        catch (PbfStopProcessingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            problem(e, "Exception thrown by onRelation($)", relation.identifierAsLong());
        }
    }

    private void processWay(PbfWay way)
    {
        try
        {
            // and process it
            processedWay(way, processor.onWay(way));
        }
        catch (PbfStopProcessingException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            problem(e, "Exception thrown by onWay($)", way.identifierAsLong());
        }
    }

    private void processedNode(PbfNode node, PbfDataProcessor.Action result)
    {
        if (result == PbfDataProcessor.Action.ACCEPTED)
        {
            maximumNodeIdentifier = Math.max(maximumNodeIdentifier, node.identifierAsLong());
            statistics.incrementNodes();
        }
        nodeProgress.next();
    }

    private void processedRelation(PbfRelation relation, PbfDataProcessor.Action result)
    {
        if (result == PbfDataProcessor.Action.ACCEPTED)
        {
            maximumRelationIdentifier = Math.max(maximumRelationIdentifier, relation.identifierAsLong());
            statistics.incrementRelations();
        }
        relationProgress.next();
    }

    private void processedWay(PbfWay way, PbfDataProcessor.Action result)
    {
        if (result == PbfDataProcessor.Action.ACCEPTED)
        {
            maximumWayIdentifier = Math.max(maximumWayIdentifier, way.identifierAsLong());
            statistics.incrementWays();
        }
        wayProgress.next();
    }

    private void startProcessingNodes()
    {
        if (!processingNodes)
        {
            processingNodes = true;
            nodeProgress.start();
            processor.onStartNodes();
        }
    }

    private void startProcessingRelations()
    {
        if (!processingRelations)
        {
            processingRelations = true;
            wayProgress.end();
            relationProgress.start();
            processor.onStartRelations();
        }
    }

    private void startProcessingWays()
    {
        if (!processingWays)
        {
            processingWays = true;
            nodeProgress.end();
            wayProgress.start();
            processor.onStartWays();
        }
    }
}
