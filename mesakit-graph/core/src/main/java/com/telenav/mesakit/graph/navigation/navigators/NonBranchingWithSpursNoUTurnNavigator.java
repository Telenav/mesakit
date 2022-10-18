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

import com.telenav.mesakit.graph.Edge;

/**
 * Same as {@link NonBranchingNoUTurnNavigator}, except dead ends don't count as branches
 *
 * @author jonathanl (shibo)
 */
public class NonBranchingWithSpursNoUTurnNavigator extends NonBranchingNoUTurnNavigator
{
    /**
     * Returns the in edge to navigate (backwards) to from the given edge, or null if there is no such edge.
     */
    @Override
    public Edge in(Edge edge)
    {
        var edges = edge.inEdges().without(e -> e.leadsToDeadEnd() || e.isLoop());
        return next(edge, edges.count(), edges.asSequence());
    }

    /**
     * Returns the out edge to navigate to from the given edge, or null if there is no such edge.
     */
    @Override
    public Edge out(Edge edge)
    {
        var edges = edge.outEdges().without(e -> e.leadsToDeadEnd() || e.isLoop());
        return next(edge, edges.count(), edges.asSequence());
    }
}
