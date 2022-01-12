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

package com.telenav.mesakit.graph.analytics.crawler;

import com.telenav.kivakit.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.LinkedList;
import java.util.Queue;

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
    private final EdgeSet visitedEdges;

    /** Queue of edges to visit */
    private final Queue<Edge> itineraryEdges = new LinkedList<>();

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
    public GraphCrawler(Maximum maximumEdges, Distance maximumDistance, DistanceMetric metric)
    {
        // Save maximum distance
        this.maximumDistance = maximumDistance;
        this.metric = metric;

        // Initialize the visited set and the itinerary
        visitedEdges = new EdgeSet(maximumEdges, com.telenav.kivakit.kernel.language.values.count.Estimate._65536);
    }

    /**
     * @param start The vertex to start crawling at
     * @return The set of edges directly or indirectly connected to the starting edge
     */
    public EdgeSet crawl(Vertex start)
    {
        // Reset this crawler for a new crawl,
        reset();

        // save the given start vertex,
        this.start = start;

        // add all edges attached to the starting vertex to the itinerary,
        visit(start);

        // and start crawling.
        return crawl();
    }

    /**
     * @param start The vertex to start crawling at
     * @return The set of edges directly or indirectly connected to the starting edge
     */
    public EdgeSet crawl(Edge start)
    {
        // Reset this crawler for a new crawl,
        reset();

        // save the start vertex,
        this.start = start.from();

        // add the start edge to the itinerary,
        itineraryEdges.add(start);

        // and start crawling.
        return crawl();
    }

    /**
     * @return True if the given edge has been visited
     */
    public boolean hasVisited(Edge edge)
    {
        return visitedEdges.contains(edge);
    }

    public void progress(Progress progress)
    {
        this.progress = progress;
    }

    /**
     * @return True if the edge can be visited, false if it should be ignored
     */
    @SuppressWarnings({ "SameReturnValue" })
    protected boolean accept(Edge edge)
    {
        return true;
    }

    /**
     * @return The set of edges to visit from the given vertex
     */
    protected EdgeSet candidates(Vertex vertex)
    {
        return vertex.edges();
    }

    protected boolean isCloseEnough(Edge edge)
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

    private EdgeSet crawl()
    {
        // While there are edges to visit
        while (!itineraryEdges.isEmpty() && visitedEdges.count().isLessThan(visitedEdges.maximumSize()))
        {
            // get the next edge from the queue,
            var next = itineraryEdges.poll();

            // and if it's acceptable, and we haven't already visited it or its reverse
            if (next != null && accept(next) && !visitedEdges.contains(next))
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
        return visitedEdges;
    }

    private boolean isCloseEnough(Vertex vertex)
    {
        return vertex.location().distanceTo(start.location()).isLessThan(maximumDistance);
    }

    /**
     * Resets the crawler for another crawl
     */
    private void reset()
    {
        // Clear out visited edges and itinerary
        visitedEdges.clear();
        itineraryEdges.clear();
    }

    private void visit(Edge next)
    {
        visit(next.from());
        visit(next.to());
        visitedEdges.add(next);
    }

    private void visit(Vertex vertex)
    {
        for (var edge : candidates(vertex))
        {
            if (!visitedEdges.contains(edge))
            {
                itineraryEdges.add(edge);
            }
        }
    }
}
