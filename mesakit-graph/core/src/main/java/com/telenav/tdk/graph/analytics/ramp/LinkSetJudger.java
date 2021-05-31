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

package com.telenav.kivakit.graph.analytics.ramp;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.Route;
import com.telenav.kivakit.graph.collections.EdgeSet;
import com.telenav.kivakit.graph.library.matchers.Matchers;
import com.telenav.kivakit.map.measurements.Distance;

import java.util.HashSet;

/**
 * This class judges if a set of links is a ramp set using the following rules:
 * <ul>
 * <li>if have bifurcations, they are ramps</li>
 * <li>if have a big curve angle, such as over 180, they are ramps</li>
 * <li>if have a big length, such as over 180 meters, they are ramps</li>
 * </ul>
 *
 * @author ranl
 */
class LinkSetJudger
{
    /** The max accumulated length for connections */
    private static final Distance CONNECTION_LENGTH_LIMIT = Distance.meters(180);

    /** The max accumulated curve angle for connections */
    private static final AccumulatedAngle CONNECTION_CURVE_ANGLE_LIMIT = AccumulatedAngle._100__8Y;

    private final EdgeSet edges;

    /**
     * Constructor with the edge set to be checked.
     */
    LinkSetJudger(final EdgeSet edges)
    {
        this.edges = edges;
    }

    /**
     * @return true, if the edge set are ramps; false, if the edge set are connections.
     */
    boolean isRamp()
    {
        // If there are no edges
        if (edges.isEmpty())
        {
            // its not a ramp set
            return false;
        }

        // If the connector is between two double digitized roads, it's not a ramp. For example, a
        // long curvy connector between dual carriage ways for a Michigan left case. Note that here
        // we just address the simple case where the connector only contains one edge.
        if (edges.size() == 1 && Route.fromEdge(edges.first()).connectsTwoDoubleDigitizedRoads())
        {
            return false;
        }

        // If the edge set has a bifurcation
        if (hasBifurcation())
        {
            // it's a ramp set
            return true;
        }
        // If the edge set has a large curve angle
        else if (hasLargeCurveAngle())
        {
            // it's a ramp set
            return true;
        }

        // Its a ramp set if the total length is greater than the connection length limit
        return edges.length().isGreaterThan(CONNECTION_LENGTH_LIMIT);
    }

    private void accumulateAngleBackward(final Edge edge, final AccumulatedAngle angle,
                                         final HashSet<Edge> visited)
    {
        if (!angle.isGreaterThan(CONNECTION_CURVE_ANGLE_LIMIT) && !visited.contains(edge))
        {
            final var inEdges = edge.from().inEdges().without(edge.reversed()).logicalSetMatching(Matchers.LINKS)
                    .intersection(edges);
            if (!inEdges.isEmpty())
            {
                // if the in edges are not empty, accumulate the angles and continue to search
                // backward.
                final var inEdge = inEdges.first();
                angle.accumulate(inEdge.roadShape().turnAngleTo(edge.roadShape()));
                angle.accumulate(AccumulatedAngle.curveAngle(inEdge));
                visited.add(inEdge);

                accumulateAngleBackward(inEdge, angle, visited);
            }
        }
    }

    private void accumulateAngleForward(final Edge edge, final AccumulatedAngle angle,
                                        final HashSet<Edge> visited)
    {
        if (!angle.isGreaterThan(CONNECTION_CURVE_ANGLE_LIMIT) && !visited.contains(edge))
        {
            final var outEdges = edge.to().outEdges().without(edge.reversed()).logicalSetMatching(Matchers.LINKS)
                    .intersection(edges);
            if (!outEdges.isEmpty())
            {
                // if the out edges are not empty, accumulate the angles and continue to search
                // forward.
                final var outEdge = outEdges.first();
                angle.accumulate(edge.roadShape().turnAngleTo(outEdge.roadShape()));
                angle.accumulate(AccumulatedAngle.curveAngle(outEdge));
                visited.add(edge);

                accumulateAngleForward(outEdge, angle, visited);
            }
        }
    }

    /**
     * Checks if the edge set has any bifurcations.
     */
    private boolean hasBifurcation()
    {
        if (edges.size() > 2)
        {
            for (final var edge : edges)
            {
                if (hasOutBifurcation(edge) || hasInBifurcation(edge))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given edge has any in bifurcation.
     */
    private boolean hasInBifurcation(final Edge edge)
    {
        final var inEdges = edge.from().inEdges().without(edge.reversed()).logicalSetMatching(Matchers.LINKS)
                .intersection(edges);
        if (inEdges.isEmpty())
        {
            // if no in connections/ramps, return false.
            return false;
        }
        else if (inEdges.size() == 1)
        {
            // if only 1 in connection/ramp, check out connections/ramps.
            final var outEdges = edge.from().outEdges().without(edge).without(inEdges.first().reversed())
                    .logicalSetMatching(Matchers.LINKS).intersection(edges);
            // if no out connections/ramps, return false.
            // if have any out connections/ramps (which will form a bifurcation with the current
            // edge), return true.
            return !outEdges.isEmpty();
        }
        else
        {
            // if multiple in connections/ramps, return true.
            return true;
        }
    }

    /**
     * Checks if the edge set contains big curve angles.
     */
    private boolean hasLargeCurveAngle()
    {
        final var edge = edges.first();
        final var visited = new HashSet<Edge>();

        final var angle = AccumulatedAngle.curveAngle(edge);
        accumulateAngleForward(edge, angle, visited);
        accumulateAngleBackward(edge, angle, visited);
        return angle.isGreaterThan(CONNECTION_CURVE_ANGLE_LIMIT);
    }

    /**
     * Checks if the given edge has any out bifurcation.
     */
    private boolean hasOutBifurcation(final Edge edge)
    {
        final var outEdges = edge.to().outEdges().without(edge.reversed()).logicalSetMatching(Matchers.LINKS)
                .intersection(edges);
        if (outEdges.isEmpty())
        {
            // if no out connections/ramps, return false.
            return false;
        }
        else if (outEdges.size() == 1)
        {
            // if only 1 out connection/ramp, check in connections/ramps.
            final var inEdges = edge.to().inEdges().without(edge).without(outEdges.first().reversed())
                    .logicalSetMatching(Matchers.LINKS).intersection(edges);
            // if no in connections/ramps, return false.
            // if have any in connections/ramps (which will form a bifurcation with the current
            // edge), return true.
            return !inEdges.isEmpty();
        }
        else
        {
            // if multiple out connections/ramps, return true.
            return true;
        }
    }
}
