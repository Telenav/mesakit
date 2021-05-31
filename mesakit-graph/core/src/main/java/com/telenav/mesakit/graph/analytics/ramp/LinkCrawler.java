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

import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.library.matchers.Matchers;
import com.telenav.mesakit.graph.navigation.Navigator.Direction;

import java.util.LinkedList;
import java.util.Queue;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * Crawls out all links (connectors and ramps) beginning from an initial link.
 *
 * @author ranl
 * @author jonathanl (shibo)
 */
class LinkCrawler
{
    /** Set of links found during crawling */
    private EdgeSet links;

    /** Queue of link edges to visit */
    private Queue<Edge> itinerary;

    /** The maximum number of links to crawl */
    private final Maximum maximumLinks;

    public LinkCrawler(final Maximum maximumLinks)
    {
        this.maximumLinks = maximumLinks;
    }

    /**
     * @param start The edge at which to begin crawling
     * @return The set of links (connectors or ramps) directly or indirectly connected to the starting edge
     */
    EdgeSet crawl(final Edge start)
    {
        // Make sure we're starting with a connecting road or ramp
        if (!start.isLink())
        {
            return fail("Must start on a link edge (connector or ramp)");
        }

        // Initialize set of connectors and visit queue
        links = new EdgeSet(maximumLinks, Estimate._65536);
        itinerary = new LinkedList<>();

        // Add the starting edge
        add(start);

        // While there are edges to visit
        while (!itinerary.isEmpty() && links.count().isLessThan(maximumLinks))
        {
            // visit the next edge from the queue
            visit(itinerary.poll());
        }

        // Return the accumulated links
        return links;
    }

    /**
     * Adds the edge to the set of connectors we're accumulating
     */
    private void add(final Edge connector)
    {
        // If we haven't already added this edge to the set of connectors
        if (!links.contains(connector) && links.count().isLessThan(maximumLinks))
        {
            // add it
            links.add(connector);

            // and make it a new destination
            itinerary.offer(connector);

            // then get any reversed edge
            final var reversed = connector.reversed();

            // and if the reversed edge exists and we haven't added that
            if (reversed != null && !links.contains(reversed))
            {
                // add the reversed edge
                links.add(reversed);

                // and make it a new destination as well
                itinerary.offer(reversed);
            }
        }
    }

    /**
     * Add each edge in the set of connectors
     */
    private void add(final EdgeSet connectors)
    {
        for (final var connector : connectors)
        {
            add(connector);
        }
    }

    /**
     * Adds links connected to the given edges in the given direction
     *
     * @param edge The edge to visit
     * @param direction The direction to extend
     */
    private void addLinks(final Edge edge, final Direction direction)
    {
        // Get the vertex to extend from
        final var vertex = direction.isOut() ? edge.to() : edge.from();

        // If the vertex has only link edges
        if (hasOnlyLinkEdges(vertex))
        {
            // If there are any links extending in the given direction
            final var next = links(edge, vertex, direction);
            if (!next.isEmpty())
            {
                // add those forward links
                add(next);

                // and add any links in the reverse direction
                add(links(edge, vertex, direction.reverse()));
            }
        }
    }

    /**
     * @return True if the vertex has any navigable non-link edge
     */
    private boolean hasOnlyLinkEdges(final Vertex vertex)
    {
        for (final var edge : vertex.edges())
        {
            if (!edge.isLink() && edge.isDrivable())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The set of in or out connector (connecting or ramp) edges from the given vertex
     */
    private EdgeSet links(final Edge edge, final Vertex vertex, final Direction direction)
    {
        final var edges = direction.isOut() ? vertex.outEdges() : vertex.inEdges();
        return edges.without(edge).without(edge.reversed()).logicalSetMatching(Matchers.LINKS);
    }

    /**
     * Visits the given edge
     */
    private void visit(final Edge edge)
    {
        addLinks(edge, Direction.OUT);
        addLinks(edge, Direction.IN);
    }
}
