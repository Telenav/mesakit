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

package com.telenav.tdk.graph.analytics.classification.classifiers.road;

import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.analytics.classification.EdgeClassifier;

public class RoadNameClassifier implements EdgeClassifier
{
    public static RoadNameClassifier INSTANCE = new RoadNameClassifier();

    /**
     * Determines whether the edge is named
     *
     * @param edge the edge to determined naming
     * @return boolean
     */
    @Override
    public boolean accept(final Edge edge)
    {
        return edge.roadName() != null;
    }
}
