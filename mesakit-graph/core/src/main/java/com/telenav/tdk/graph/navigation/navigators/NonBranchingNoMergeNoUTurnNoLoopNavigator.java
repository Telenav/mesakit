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
import com.telenav.kivakit.graph.collections.EdgeSet;

public class NonBranchingNoMergeNoUTurnNoLoopNavigator extends NonBranchingNoMergeNoUTurnNavigator
{
    private final EdgeSet visited = new EdgeSet();

    @Override
    public Edge next(final Edge edge, final Direction direction)
    {
        final var next = super.next(edge, direction);
        if (!this.visited.contains(next))
        {
            this.visited.add(next);
            return next;
        }
        return null;
    }
}
