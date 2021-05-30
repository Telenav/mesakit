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


package com.telenav.tdk.graph.analytics.crawler;

import com.telenav.tdk.core.kernel.operation.progress.reporters.Progress;
import com.telenav.tdk.core.kernel.scalars.counts.*;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.collections.EdgeSet;
import com.telenav.tdk.map.measurements.Distance;

import java.util.*;

/**
 * Crawls a graph within a limited distance and maximum number of edges, returning a set of connected edges. A crawler
 * can be re-used by calling {@link #crawl(Vertex)} multiple times, but {@link GraphCrawler} is not thread safe and an
 * instance may not be used by more than one thread.
 *
 * @author jonathanl (shibo)
 */
public class GraphCrawler
{
    public enum DistanceMetric
    {
        /** Don't crawl to any vertex beyond the maximum distance */
        STRICT,

        /** Crawl to vertices beyond the maximum distance, but don't go further */
        RELAXED
    }

    /** Set of edges found during crawling */
    private final EdgeSet visited;

    /** Queue of edges to visit */
    private final Queue<Edge> itinerary = new LinkedList<>();

    /** Start vertex */
    private Vertex start;

    /** Maximum distance to crawl from the start vertex (as the crow flies) */
    private final Distance maximumDistance;

    /** Whether the maximum distance is a strict measurement or not */
    private final DistanceMetric metric;

    private Progress progress;

    /**
     * @param maximumEdges The maximum number of edges to collect
     * @param maximumDistance The maximum distance (as the crow flies) allowable from the start vertex
     */
    public GraphCrawler(final Maximum maximumEdges, final Distance maximumDistance, final DistanceMetric metric)
    {
        // Save maximum distance
        this.maximumDistance = maximumDistance;
        this.metric = metric;

        // Initialize the visited set and the itinerary
        visited = new EdgeSet(maximumEdges, Estimate._65536);
    }

    /**
     * @param start The vertex to start crawling at
     * @return The set of edges directly or indirectly connected to the starting edge
     */
    public EdgeSet crawl(final Vertex start)
    {
        // Save start vertex
        this.start = start;

        // Clear out visited edges and itinerary
        visited.clear();
        itinerary.clear();

        // Add all edges attached to the starting vertex to the itinerary
        visit(start);

        // While there are edges to visit
        while (!itinerary.isEmpty() && visited.count().isLessThan(visited.maximumSize()))
        {
            // get the next edge from the queue,
            final var next = itinerary.poll();

            // and if it's acceptable and we haven't already visited it
            if (next != null && accept(next) && !visited.contains(next))
            {
                // at least one end of the edge is close enough to the start vertex
                if (isCloseEnough(next))
                {
                    // then visit the edge
                    visit(next);
                }
            }

            // Processed next edge in the queue
            if (progress != null)
            {
                progress.next();
            }
        }

        // Completed crawl
        if (progress != null)
        {
            progress.end();
        }

        // Return the accumulated links
        return visited;
    }

    public void progress(final Progress progress)
    {
        this.progress = progress;
    }

    /**
     * @return True if the edge can be visited, false if it should be ignored
     */
    @SuppressWarnings({ "SameReturnValue" })
    protected boolean accept(final Edge edge)
    {
        return true;
    }

    /**
     * @return The set of edges to visit from the given vertex
     */
    protected EdgeSet candidates(final Vertex vertex)
    {
        return vertex.edges();
    }

    protected boolean isCloseEnough(final Edge edge)
    {
        switch (metric)
        {
            case STRICT:
                return isCloseEnough(edge.from()) && isCloseEnough(edge.to());

            case RELAXED:
                return isCloseEnough(edge.from()) || isCloseEnough(edge.to());

            default:
                return false;
        }
    }

    private boolean isCloseEnough(final Vertex vertex)
    {
        return vertex.location().distanceTo(start.location()).isLessThan(maximumDistance);
    }

    private void visit(final Edge next)
    {
        visit(next.from());
        visit(next.to());
        visited.add(next);
    }

    private void visit(final Vertex vertex)
    {
        for (final var edge : candidates(vertex))
        {
            if (!visited.contains(edge))
            {
                itinerary.add(edge);
            }
        }
    }
}
