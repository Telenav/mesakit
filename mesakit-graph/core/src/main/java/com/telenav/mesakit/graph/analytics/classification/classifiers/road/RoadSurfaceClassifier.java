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

package com.telenav.mesakit.graph.analytics.classification.classifiers.road;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.analytics.classification.EdgeClassifier;

public class RoadSurfaceClassifier implements EdgeClassifier
{
    public static RoadSurfaceClassifier INSTANCE = new RoadSurfaceClassifier();

    /**
     * Determines whether the edge has a surface
     *
     * @param edge the Edge to determine surface
     * @return boolean
     */
    @Override
    public boolean accept(Edge edge)
    {
        for (var tag : edge.tagList())
        {
            var key = tag.getKey();
            if ("surface".equalsIgnoreCase(key))
            {
                return true;
            }
        }
        return false;
    }
}
