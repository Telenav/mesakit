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

package com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner;

import com.telenav.kivakit.validation.ValidationType;
import com.telenav.kivakit.core.progress.reporters.ConcurrentBroadcastingProgressReporter;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link GraphLoader} that loads edges from a raw graph where each edge is an OSM way, sectioning each edge/way into
 * smaller edges at intersections and according to other rules, as implemented by {@link EdgeSectioner}.
 *
 * @author jonathanl (shibo)
 * @see GraphLoader
 * @see EdgeSectioner
 */
public class WaySectioningGraphLoader extends BaseGraphLoader
{
    /** The raw, un-sectioned graph we're loading from where each edge is an OSM way */
    private final Graph raw;

    /** The source of data in the raw graph */
    private final Resource dataSource;

    /** Sections an individual edge */
    private final EdgeSectioner edgeSectioner;

    /**
     * @param raw The raw graph to section
     * @param edgeSectioner The configured edge sectioner to do the job
     */
    public WaySectioningGraphLoader(Graph raw, EdgeSectioner edgeSectioner)
    {
        this.raw = raw;
        this.edgeSectioner = edgeSectioner;
        dataSource = raw.resource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata onLoad(GraphStore store, GraphConstraints constraints)
    {
        // To add sectioned edges to the graph,
        var start = Time.now();
        var progress = ConcurrentBroadcastingProgressReporter.concurrentProgressReporter(this, "edges",
                raw.forwardEdgeCount().asMaximum()).withPhase("  [Sectioning Ways] ");
        progress.start();

        // we get the edge and relation stores
        var edgeStore = store.edgeStore();
        var relationStore = store.relationStore();

        // and an iterator over batches of edges
        var batches = raw.batches(Count._16_384);

        // then we start a thread pool
        var lock = new ReentrantLock(true);

        // and get adders for each store
        var edgeStoreAdder = edgeStore.adder();
        var relationStoreAdder = relationStore.adder();

        // then we loop
        while (true)
        {
            // so long as there is another batch
            List<Edge> batch = null;
            lock.lock();
            try
            {
                if (batches.hasNext())
                {
                    batch = batches.next();
                }
                if (batch == null)
                {
                    break;
                }
            }
            finally
            {
                lock.unlock();
            }

            // and process each way/edge in the batch
            for (var way : batch)
            {
                // breaking the way into smaller sections
                var sections = edgeSectioner.section(way);
                for (var section : sections)
                {
                    // and if a section is included,
                    if (constraints.includes(section))
                    {
                        // we add it to the edge store.
                        var heavyweight = section.asHeavyWeight();
                        heavyweight.copyRoadNames(way);
                        edgeStoreAdder.add(heavyweight);
                    }
                }

                // Then for each of the way's relations,
                for (var relation : way.relations())
                {
                    // if it hasn't already been added,
                    if (!relationStore.containsIdentifier(relation.identifierAsLong()))
                    {
                        // add it.
                        relationStoreAdder.add(relation.asHeavyWeight());
                    }

                    // and go through the way's sections
                    for (var edge : sections)
                    {
                        // adding the relation to the edge
                        store.edgeStore().storeRelation(edge, relation);
                    }
                }
            }

            progress.next(batch.size());
        }

        progress.end();

        edgeStore.flush();
        relationStore.flush();

        information("Added $ edges, discarded $ in $", Count.count(edgeStore.count()), edgeStore.discarded(), start.elapsedSince());

        // Add places to the graph
        var startPlaces = Time.now();
        var placeStore = store.placeStore();
        var adder = placeStore.adder();
        for (var place : raw.places())
        {
            adder.add(place);
        }
        information("Added $ places, discarded $ in $", Count.count(placeStore.size()), placeStore.discarded(), startPlaces.elapsedSince());
        return raw.metadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resource()
    {
        return dataSource;
    }

    @Override
    public ValidationType validation()
    {
        return new ValidationType().exclude(RelationStore.class);
    }
}
