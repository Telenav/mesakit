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
import com.telenav.mesakit.graph.collections.EdgeSet;

public class NonBranchingNoUTurnNoLoopNavigator extends NonBranchingNoUTurnNavigator
{
    private final EdgeSet visited = new EdgeSet();

    @Override
    protected Edge next(Edge edge, Count size, EdgeSequence nextEdges)
    {
        var next = super.next(edge, size, nextEdges);
        if (!visited.contains(next))
        {
            visited.add(next);
            return next;
        }
        return null;
    }
}
