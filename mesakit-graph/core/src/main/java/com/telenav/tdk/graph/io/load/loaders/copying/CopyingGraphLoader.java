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

package com.telenav.tdk.graph.io.load.loaders.copying;

import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.time.Time;
import com.telenav.tdk.core.resource.Resource;
import com.telenav.tdk.core.resource.path.FileName;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.io.archive.GraphArchive;
import com.telenav.tdk.graph.io.load.*;
import com.telenav.tdk.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.tdk.graph.specifications.common.node.store.all.disk.PbfAllNodeDiskStores;
import com.telenav.tdk.graph.specifications.library.store.GraphStore;

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
    public CopyingGraphLoader(final GraphArchive source)
    {
        this(source.load(LOGGER));
    }

    /**
     * @param source The source graph to copy (load) from
     */
    public CopyingGraphLoader(final Graph source)
    {
        this.source = source;
        pbfNodeDiskStores = new PbfAllNodeDiskStores(PbfAllNodeDiskStores.temporary(),
                new FileName(source.name() + "-" + Time.now().asMilliseconds()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCommit(final GraphStore store)
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
    public Metadata onLoad(final GraphStore store, final GraphConstraints constraints)
    {
        if (!source.isEmpty())
        {
            final var edgeCountBefore = store.edgeStore().count();
            final var vertexCountBefore = store.vertexStore().count();
            final var relationCountBefore = store.relationStore().count();
            final var placeCountBefore = store.placeStore().count();

            // Go through the forward edges in the source graph,
            final var edgeAdder = store.edgeStore().adder();
            final var relationAdder = store.relationStore().adder();
            for (final var edge : source.forwardEdgesIntersecting(constraints.bounds()))
            {
                // and if the constraints include the edge,
                if (constraints.includes(edge))
                {
                    // then add the edge
                    edgeAdder.add(edge);

                    // and each relation,
                    for (final var relation : edge.relations())
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
                        for (final var point : edge.shapePoints())
                        {
                            // and add them to the disk store.
                            pbfNodeDiskStores.add(point.asPbfNode());
                        }
                    }
                }
            }

            // Go through places in the source graph
            final var placeAdder = store.placeStore().adder();
            for (final var place : source.placesInside(constraints.bounds()))
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
                    .withEdgeCount(Count.of(store.edgeStore().count() - edgeCountBefore))
                    .withVertexCount(Count.of(store.vertexStore().count() - vertexCountBefore))
                    .withRelationCount(Count.of(store.relationStore().count() - relationCountBefore))
                    .withPlaceCount(Count.of(store.placeStore().count() - placeCountBefore));
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
