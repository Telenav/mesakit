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

package com.telenav.mesakit.graph.analytics.classification.classifiers.signpost;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.analytics.classification.EdgeClassifier;
import com.telenav.mesakit.map.road.model.RoadType;

/**
 * Created by udayg on 2/10/16.
 */
public class SignPostDetectionClassifier implements EdgeClassifier
{
    public static SignPostDetectionClassifier INSTANCE = new SignPostDetectionClassifier();

    @Override
    public boolean accept(Edge edge)
    {
        if (edge.leadsToFork())
        {
            var to = edge.to();
            return hasSignPost(to) || hasOldSignPost(to);
        }
        return false;
    }

    private boolean hasOldSignPost(Vertex vertex)
    {
        return vertex.tag("exit_to") != null;
    }

    private boolean hasSignPost(Vertex vertex)
    {
        if (vertex.tag("ref") == null && !"yes".equalsIgnoreCase(vertex.tagValue("noref")))
        {
            return false;
        }
        var foundDestination = false;
        for (var edge : vertex.outEdges())
        {
            if (edge.roadType() != RoadType.FREEWAY)
            {
                if (edge.tagValue("destination") != null || edge.tagValue("destination:ref") != null)
                {
                    foundDestination = true;
                }
            }
        }
        return foundDestination;
    }
}
