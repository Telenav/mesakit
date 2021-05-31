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

package com.telenav.kivakit.graph.io.load.loaders.decimation;

import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.operation.progress.ProgressReporter;
import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.io.load.GraphConstraints;
import com.telenav.kivakit.graph.io.load.loaders.BaseGraphLoader;
import com.telenav.kivakit.graph.navigation.Navigator;
import com.telenav.kivakit.graph.specifications.library.store.GraphStore;
import com.telenav.kivakit.map.geography.polyline.Polyline;
import com.telenav.kivakit.map.measurements.*;

/**
 * A graph loader that loads edges, simplifying them in the process for efficient display at a low zoom level. Relations
 * (like turn restrictions) are not included in a decimated graph as it is not likely to be useful to display them at a
 * low zoom level.
 */
public class DecimatingGraphLoader extends BaseGraphLoader
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Graph source;

    private final Distance minimumLength;

    private final Angle maximumDeviation;

    private final ProgressReporter reporter;

    /**
     * Loads data from a source graph, decimating edges during the process
     *
     * @param source The source graph
     * @param minimumLength The minimum length for edges in the decimated graph. For example, if you're zoomed out far
     * enough to see an entire city, edges that are shorter than a few hundred meters need to be combined since the
     * detail cannot be seen at such a low zoom level.
     * @param maximumDeviation The most that an edge can bend before it is included even if it is a short edge
     */
    public DecimatingGraphLoader(final Graph source, final Distance minimumLength, final Angle maximumDeviation,
                                 final ProgressReporter reporter)
    {
        this.source = source;
        this.minimumLength = minimumLength;
        this.maximumDeviation = maximumDeviation;
        this.reporter = reporter;
    }

    @Override
    public Metadata onLoad(final GraphStore store, final GraphConstraints constraints)
    {
        var edges = 0;
        try
        {
            // Go through the source edges within the bounds that match the constraints
            final var decimated = new EdgeSet();
            final var edgeStore = store.edgeStore();
            final var edgeStoreAdder = edgeStore.adder();
            final var matching = source.forwardEdgesIntersecting(constraints.bounds()).matching(constraints.edgeMatcher());
            reporter.start("Decimating");
            reporter.steps(Count.of(matching));
            for (final var edge : matching)
            {
                // and if the edge is not already decimated
                if (!decimated.contains(edge))
                {
                    // and it's short enough to need to be combined with other edges to meet the minimum length requirement,
                    if (edge.length().isLessThan(minimumLength))
                    {
                        // then add a decimated edge based on the surrounding edges that meets the requirement,
                        edgeStoreAdder.add(decimate(decimated, edge));
                    }
                    else
                    {
                        // otherwise, the edge is already long enough, so just add the edge
                        edgeStoreAdder.add(edge);
                    }

                    // Increase edge count
                    edges++;
                    if (edge.isTwoWay())
                    {
                        edges++;
                    }
                }
                reporter.next();
            }
            reporter.end("Decimated");

            final var placeStoreAdder = store.placeStore().adder();
            for (final var place : source.places())
            {
                if (place.isCity() || place.isTown() || place.population().isGreaterThan(Count._10_000))
                {
                    placeStoreAdder.add(place);
                }
            }

            return store.metadata();
        }
        catch (final Exception e)
        {
            LOGGER.problem(e, "Only able to load the first $ edges of source graph  $", Count.of(edges),
                    source.name());
            return new Metadata().withEdgeCount(Count.of(edges));
        }
    }

    @Override
    public Resource resource()
    {
        return source.resource();
    }

    /**
     * Simplifies the edges connected to the given edge, if possible, by navigating the connected edges within a minimum
     * length and a maximum deviation. The simplified edge is returned.
     *
     * @return The simplified edge replacing the set of edges that were decimated
     */
    private Edge decimate(final EdgeSet decimated, final Edge edge)
    {
        // If we can navigate a small route,
        final Navigator navigator = new DecimationNavigator(edge, decimated, maximumDeviation);
        final var route = edge.route(navigator, Distance.MAXIMUM);
        if (route.size() > 1)
        {
            // create a new edge that goes directly from the start of the first edge to the end of the last one
            final var simplified = source.dataSpecification().newHeavyWeightEdge(null, edge.identifierAsLong());
            simplified.copy(edge);
            final var first = route.first();
            final var last = route.last();
            simplified.from(first.from());
            simplified.to(last.to());
            simplified.toNodeIdentifier(last.to().mapIdentifier());
            simplified.length(first.fromLocation().distanceTo(last.toLocation()));
            simplified.roadShape(Polyline.fromLocations(first.fromLocation(), last.toLocation()));
            decimated.addAll(route.asEdgeSet());
            return simplified;
        }
        return edge;
    }
}
