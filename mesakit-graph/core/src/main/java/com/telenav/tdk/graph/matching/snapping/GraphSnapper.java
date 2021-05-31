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

package com.telenav.kivakit.graph.matching.snapping;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.geography.polyline.PolylineSnap;
import com.telenav.kivakit.map.geography.polyline.PolylineSnapper;
import com.telenav.kivakit.map.measurements.Angle.Chirality;
import com.telenav.kivakit.map.measurements.Distance;
import com.telenav.kivakit.map.measurements.Heading;

import java.util.HashMap;
import java.util.Map;

/**
 * Takes a location and a heading and snaps it to the nearest edge in the given graph with an appropriate heading.
 *
 * @author jonathanl (shibo)
 */
public class GraphSnapper
{
    private final Graph graph;

    private final Distance maximumSnapDistance;

    private final Heading maximumHeadingDeviation;

    private final Edge.TransportMode transportMode;

    public GraphSnapper(final Graph graph, final Distance maximumSnapDistance, final Heading maximumHeadingDeviation,
                        final Edge.TransportMode transportMode)
    {
        this.graph = graph;
        this.maximumSnapDistance = maximumSnapDistance;
        this.maximumHeadingDeviation = maximumHeadingDeviation;
        this.transportMode = transportMode;
    }

    /**
     * @return The given location and heading snapped to the best edge, or null if there is no reasonable snap
     */
    public GraphSnap snap(final Location location, final Heading heading)
    {
        // Determine the snap bounds
        final var bounds = location.within(maximumSnapDistance);

        // Loop through edges that are close to the given location
        final var snapper = new PolylineSnapper();
        Edge closestEdge = null;
        PolylineSnap closestSnap = null;
        final Map<Edge, PolylineSnap> candidates = new HashMap<>();
        for (final var edge : graph.edgesIntersecting(bounds))
        {
            // If we can snap to the edge
            if (canSnapTo(edge))
            {
                // and we snap to the edge's road shape
                final var snap = snapper.snap(edge.roadShape(), location, heading);
                if (snap != null)
                {
                    // and the heading deviation is "small"
                    if (heading == null || snap.segmentHeading().difference(heading, Chirality.SMALLEST)
                            .isLessThan(maximumHeadingDeviation))
                    {
                        // then the edge is a candidate
                        candidates.put(edge, snap);

                        // and if we're closer than any previous snap
                        if (closestSnap == null || snap.distanceToSource().isLessThan(closestSnap.distanceToSource()))
                        {
                            // then update the closest snap
                            closestEdge = edge;
                            closestSnap = snap;
                        }
                    }
                }
            }
        }
        return closestSnap == null ? null : new GraphSnap(closestEdge, closestSnap, candidates);
    }

    private boolean canSnapTo(final Edge edge)
    {
        return edge.isNavigable(transportMode);
    }
}
