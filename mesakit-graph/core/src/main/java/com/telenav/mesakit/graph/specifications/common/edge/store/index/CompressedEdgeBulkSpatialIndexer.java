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

package com.telenav.mesakit.graph.specifications.common.edge.store.index;

import com.telenav.kivakit.collections.primitive.array.scalars.LongArray;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeBulkLoader;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Bulk loads edges into a spatial index
 *
 * @author jonathanl (shibo)
 */
public class CompressedEdgeBulkSpatialIndexer extends BaseRepeater implements NamedObject
{
    /** Used while loading to make sorting in bulk R-Tree loader faster */
    private LongArray edgeCenter;

    public CompressedEdgeBulkSpatialIndexer(final Listener listener)
    {
        broadcastTo(listener);
    }

    /**
     * @return A comparison result for the centers of the given edges that is suitable for use in a sort algorithm
     * @see Comparable#compareTo(Object)
     */
    public final int compareHorizontal(final Edge a, final Edge b)
    {
        final var acenter = Location.longitude(edgeCenter.get(a.index()));
        final var bcenter = Location.longitude(edgeCenter.get(b.index()));
        return Integer.compare(acenter, bcenter);
    }

    /**
     * @return A comparison result for the centers of the given edges that is suitable for use in a sort algorithm.
     * @see Comparable#compareTo(Object)
     */
    public final int compareVertical(final Edge a, final Edge b)
    {
        final var acenter = Location.latitude(edgeCenter.get(a.index()));
        final var bcenter = Location.latitude(edgeCenter.get(b.index()));
        return Integer.compare(acenter, bcenter);
    }

    /**
     * Creates a compressed spatial index of edges
     *
     * @param graph The graph containing the edges
     */
    public CompressedEdgeSpatialIndex index(final Graph graph)
    {
        // Record start time
        final var start = Time.now();
        information("Creating edge spatial index");

        // Get the number of edges to index
        final var initialSize = graph.edgeCount().asEstimate();

        // Create array of edge centers
        edgeCenter = new LongArray("CompressedEdgeBulkSpatialIndexer.edgeCenter");
        edgeCenter.initialSize(initialSize);
        edgeCenter.initialize();

        // Create edge spatial index
        final var index = new CompressedEdgeSpatialIndex(objectName() + ".index", graph, new RTreeSettings().withEstimatedNodes(initialSize));

        // Allocate edge array
        final List<Edge> edges = new ArrayList<>(initialSize.asInt());

        // Loop through edge identifiers adding forward edges and edge centers
        for (final var edge : graph.forwardEdges())
        {
            edges.add(edge);
            edgeCenter.set(edge.index(), edge.bounds().center().asLong(graph.precision()));
        }

        // If we are loading exactly the number of forward edges,
        final var edgeCount = Count.count(edges.size());
        if (edgeCount.equals(graph.forwardEdgeCount()))
        {
            // then show the number of edges,
            information("Indexing $ forward edges", edgeCount);
        }
        else
        {
            // otherwise, warn about it.
            warning("$ edges to bulk load doesn't match forward edge edgeCount of $", edgeCount, graph.forwardEdgeCount());
        }

        // Bulk load the spatial index
        if (!edges.isEmpty())
        {
            new RTreeBulkLoader<>(index)
            {
                @Override
                protected int compareHorizontal(final Edge a, final Edge b)
                {
                    return CompressedEdgeBulkSpatialIndexer.this.compareHorizontal(a, b);
                }

                @Override
                protected int compareVertical(final Edge a, final Edge b)
                {
                    return CompressedEdgeBulkSpatialIndexer.this.compareVertical(a, b);
                }
            }.load(edges);
        }
        else
        {
            warning("No edges to add to spatial index");
        }

        // We're done
        information("Done creating spatial index in ${debug}", start.elapsedSince());
        return index;
    }
}
