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

package com.telenav.mesakit.graph.navigation.navigators;

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.navigation.Navigator;

/**
 * Returns next and previous edges without considering the omnipresent u-turn option on two way roads.
 *
 * @author jonathanl (shibo)
 */
public class NonBranchingNoUTurnNavigator extends Navigator
{
    /**
     * Returns the in edge to navigate (backwards) to from the given edge, or null if there is no such edge.
     */
    @Override
    public Edge in(Edge edge)
    {
        // Return the next using the in edges that lead to this edge's "from" vertex
        var from = edge.from();
        return next(edge, from.inEdgeCount(), from.inEdgeSequence());
    }

    /**
     * Returns the out edge to navigate to from the given edge, or null if there is no such edge.
     */
    @Override
    public Edge out(Edge edge)
    {
        // Return next using the out edges that lead away from this edge's "to" vertex
        var to = edge.to();
        return next(edge, to.outEdgeCount(), to.outEdgeSequence());
    }

    /**
     * Returns the next edge from the set of next edges that doesn't branch or u-turn
     */
    protected Edge next(Edge edge, Count size, EdgeSequence nextEdges)
    {
        // The u-turn edge is the reversed edge
        var uturn = edge.reversed();

        // If we have only one choice,
        if (size.equals(Count._1))
        {
            // get the first edge
            var first = nextEdges.iterator().next();

            // and so long as it's not the u-turn edge (which would be a dead-end)
            if (!first.equals(uturn))
            {
                // return the sole choice we have
                return first;
            }
        }

        // If there are two next edges,
        if (size.equals(Count._2))
        {
            // and we're on a two way street,
            if (edge.isTwoWay())
            {
                // get the two edges
                var edges = nextEdges.iterator();
                var first = edges.next();
                var second = edges.next();

                // and if the first is the u-turn edge,
                if (first.equals(uturn))
                {
                    // return the other second
                    return second;
                }

                // and if the second is the u-turn edge,
                if (second.equals(uturn))
                {
                    // return the first
                    return first;
                }
            }
        }

        // The road branches, so there's no further edge
        return null;
    }
}
