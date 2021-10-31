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

package com.telenav.mesakit.graph.io.load.loaders.copying;

import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.path.FileName;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.io.load.GraphConstraints;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.mesakit.graph.specifications.common.node.store.all.disk.PbfAllNodeDiskStores;
import com.telenav.mesakit.graph.specifications.library.store.GraphStore;

/**
 * A {@link GraphLoader} that copies graph elements from a source graph. As with all graph loaders, constraints can be
 * applied to restrict what is loaded.
 *
 * @author jonathanl (shibo)
 * @see Graph#load(GraphLoader, GraphConstraints)
 * @see GraphLoader
 */
public class CopyingGraphLoader extends BaseGraphLoader
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    /** The graph we are copying from */
    private final Graph source;

    /** Detailed node information, if any */
    private final PbfAllNodeDiskStores pbfNodeDiskStores;

    /**
     * @param source The source graph to copy (load) from
     */
    public CopyingGraphLoader(GraphArchive source)
    {
        this(source.load(LOGGER));
    }

    /**
     * @param source The source graph to copy (load) from
     */
    public CopyingGraphLoader(Graph source)
    {
        this.source = source;
        pbfNodeDiskStores = new PbfAllNodeDiskStores(PbfAllNodeDiskStores.temporary(),
                FileName.parse(this, source.name() + "-" + Time.now().asMilliseconds()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCommit(GraphStore store)
    {
        if (source.supportsFullPbfNodeInformation())
        {
            store.vertexStore().allPbfNodeDiskStores(pbfNodeDiskStores);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata onLoad(GraphStore store, GraphConstraints constraints)
    {
        if (!source.isEmpty())
        {
            var edgeCountBefore = store.edgeStore().count();
            var vertexCountBefore = store.vertexStore().count();
            var relationCountBefore = store.relationStore().count();
            var placeCountBefore = store.placeStore().count();

            // Go through the forward edges in the source graph,
            var edgeAdder = store.edgeStore().adder();
            var relationAdder = store.relationStore().adder();
            for (var edge : source.forwardEdgesIntersecting(constraints.bounds()))
            {
                // and if the constraints include the edge,
                if (constraints.includes(edge))
                {
                    // then add the edge
                    edgeAdder.add(edge);

                    // and each relation,
                    for (var relation : edge.relations())
                    {
                        // if it hasn't already been added.
                        if (!store.relationStore().containsIdentifier(relation.identifierAsLong()))
                        {
                            relationAdder.add(relation);
                        }
                    }

                    // Then, if the source supports full node information,
                    if (source.supportsFullPbfNodeInformation())
                    {
                        // go through the shape points
                        for (var point : edge.shapePoints())
                        {
                            // and add them to the disk store.
                            pbfNodeDiskStores.add(point.asPbfNode());
                        }
                    }
                }
            }

            // Go through places in the source graph
            var placeAdder = store.placeStore().adder();
            for (var place : source.placesInside(constraints.bounds()))
            {
                // and if the constraints includes the place,
                if (constraints.includes(place))
                {
                    // add it to the store.
                    placeAdder.add(place);
                }
            }

            store.flush();

            // Return the source metadata, with the number of edges, vertexes relations and places loaded
            return source.metadata()
                    .withEdgeCount(Count.count(store.edgeStore().count() - edgeCountBefore))
                    .withVertexCount(Count.count(store.vertexStore().count() - vertexCountBefore))
                    .withRelationCount(Count.count(store.relationStore().count() - relationCountBefore))
                    .withPlaceCount(Count.count(store.placeStore().count() - placeCountBefore));
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource resource()
    {
        return source.resource();
    }
}
