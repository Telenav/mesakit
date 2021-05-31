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

package com.telenav.mesakit.graph.analytics.ramp;

import com.telenav.kivakit.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.library.matchers.Matchers;

/**
 * Identifies connector links that function as ramps. Also locates ramp links along the way.
 *
 * @author ranl
 * @author jonathanl (shibo)
 */
public abstract class RampFinder extends BaseRepeater
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Maximum MAXIMUM_LINKS = Maximum.maximum(250);

    /** Connection edges which have been crawled by {@link LinkCrawler} */
    private final EdgeSet visited = new EdgeSet();

    private final MutableCount ramps = new MutableCount();

    private final MutableCount rampConnector = new MutableCount();

    private final Graph graph;

    protected RampFinder(final Graph graph)
    {
        this.graph = graph;
    }

    public void find()
    {
        // Visit each edge in the sequence
        final var progress = isDeaf() ? Progress.NULL : Progress.create(this, "edges");
        progress.steps(graph.edgeCount().asMaximum());
        progress.start();
        for (final var edge : graph.edges())
        {
            try
            {
                visit(edge);
                progress.next();
            }
            catch (final Exception e)
            {
                LOGGER.problem(e, "Unable to process edge $", edge.identifier());
            }
        }
        progress.end();
    }

    /**
     * Called for each ramp link
     *
     * @param ramp The ramp
     */
    @SuppressWarnings({ "EmptyMethod" })
    protected abstract void onRamp(Edge ramp);

    /**
     * Called for each connector that's been identified as a ramp
     *
     * @param connector The connector edge to be marked as a ramp
     */
    protected abstract void onRampConnector(Edge connector);

    /**
     * Visits the given edge looking for ramps
     */
    private void visit(final Edge edge)
    {
        // If we're including ramps and this edge is a ramp
        if (edge.isRamp())
        {
            // we found a ramp
            ramps.increment();
            onRamp(edge);
        }

        // If the edge is a connector and hasn't been visited already
        if (edge.isConnector() && !visited.contains(edge))
        {
            // Get all links connected to this edge
            final var links = new LinkCrawler(MAXIMUM_LINKS).crawl(edge);
            if (links.count().isGreaterThanOrEqualTo(MAXIMUM_LINKS))
            {
                LOGGER.warning("Edge $ has $ links: $", edge, links.size(), links);
            }

            // If the set of links forms a ramp
            if (new LinkSetJudger(links).isRamp())
            {
                // notify the subclass of each edge
                for (final var link : links)
                {
                    // If the link is a connector
                    if (link.isConnector())
                    {
                        // then it's a connector that should be marked as a ramp
                        rampConnector.increment();
                        onRampConnector(link);
                    }

                    // If the link is explicitly marked as a ramp already
                    if (link.isRamp())
                    {
                        // notify the subclass of it
                        ramps.increment();
                        onRamp(link);
                    }
                }
            }

            // Add checked connectors to visited set
            visited.addAll(links.logicalSetMatching(Matchers.CONNECTORS));
        }
    }
}
