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

package com.telenav.tdk.graph.matching.snapping;

import com.telenav.tdk.core.kernel.scalars.counts.Estimate;
import com.telenav.tdk.core.kernel.scalars.counts.Maximum;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.collections.EdgeSet;
import com.telenav.tdk.graph.navigation.Navigator;
import com.telenav.tdk.map.geography.polyline.PolylineSnap;
import com.telenav.tdk.map.measurements.Distance;

import java.util.Map;

public class GraphSnap
{
    private final Edge closestEdge;

    private final PolylineSnap closestSnap;

    private final Map<Edge, PolylineSnap> candidates;

    public GraphSnap(final Edge closestEdge, final PolylineSnap closestSnap, final Map<Edge, PolylineSnap> candidates)
    {
        this.closestEdge = closestEdge;
        this.closestSnap = closestSnap;
        this.candidates = candidates;
    }

    public Map<Edge, PolylineSnap> candidates()
    {
        return candidates;
    }

    public Edge closestEdge()
    {
        return closestEdge;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof GraphSnap)
        {
            final var that = (GraphSnap) object;
            return closestSnap.equals(that.closestSnap);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return closestEdge.hashCode();
    }

    public boolean isAmbiguous()
    {
        final var candidates = new EdgeSet(Maximum._1_000, Estimate.of(this.candidates.keySet()),
                this.candidates.keySet());
        removeEdgesOnNonBranchingRoute(candidates, closestEdge);
        return !candidates.isEmpty();
    }

    public PolylineSnap polylineSnap()
    {
        return closestSnap;
    }

    private void removeEdgesOnNonBranchingRoute(final EdgeSet edges, final Edge first)
    {
        // We limit the route search to one mile to be sure we don't go too far
        for (final var edge : first.route(Navigator.NON_BRANCHING_NO_MERGE_NO_UTURN_NO_LOOP, Distance.ONE_MILE))
        {
            if (!edges.contains(edge))
            {
                break;
            }
            edges.remove(edge);
        }
    }
}
