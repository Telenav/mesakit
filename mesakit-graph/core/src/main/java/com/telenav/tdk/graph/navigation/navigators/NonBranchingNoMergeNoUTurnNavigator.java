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

package com.telenav.kivakit.graph.navigation.navigators;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.graph.navigation.Navigator;

/**
 * Returns in and out edges on a road with no bifurcations (either branches or merges).
 *
 * @author jonathanl (shibo)
 */
public class NonBranchingNoMergeNoUTurnNavigator extends Navigator
{
    @Override
    public Edge in(final Edge edge)
    {
        final var from = edge.from();
        if (from.isThroughVertex())
        {
            return Navigator.NON_BRANCHING_NO_UTURN.in(edge);
        }
        return null;
    }

    @Override
    public Edge out(final Edge edge)
    {
        final var to = edge.to();
        if (to.isThroughVertex())
        {
            return Navigator.NON_BRANCHING_NO_UTURN.out(edge);
        }
        return null;
    }
}
