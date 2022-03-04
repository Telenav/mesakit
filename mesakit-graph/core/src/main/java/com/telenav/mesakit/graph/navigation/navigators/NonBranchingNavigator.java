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
import com.telenav.mesakit.graph.navigation.Navigator;

/**
 * Returns next and previous edges so long as there are no branches. Note that this includes the u-turn option on any
 * two way street. If you don't want to consider u-turns on a two-way street as "branches", try using {@link
 * NonBranchingNoUTurnNavigator}.
 *
 * @author jonathanl (shibo)
 * @see NonBranchingNoUTurnNavigator
 */
public class NonBranchingNavigator extends Navigator
{
    @Override
    public Edge in(Edge edge)
    {
        var from = edge.from();
        if (from.inEdgeCount().equals(Count._1))
        {
            return from.inEdgeSequence().iterator().next();
        }
        return null;
    }

    @Override
    public Edge out(Edge edge)
    {
        var to = edge.to();
        if (to.outEdgeCount().equals(Count._1))
        {
            return to.outEdgeSequence().iterator().next();
        }
        return null;
    }
}
