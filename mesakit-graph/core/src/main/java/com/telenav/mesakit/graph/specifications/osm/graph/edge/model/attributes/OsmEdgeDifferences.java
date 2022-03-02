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

package com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes;

import com.telenav.kivakit.coredata.comparison.Differences;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmEdge;

public class OsmEdgeDifferences
{
    private final OsmEdge a;

    private final OsmEdge b;

    public OsmEdgeDifferences(OsmEdge a, OsmEdge b)
    {
        this.a = a;
        this.b = b;
    }

    public Differences compare()
    {
        var differences = new Differences();

        differences.compare("pbfFromNodeIdentifier", a.fromNodeIdentifier(), b.fromNodeIdentifier());
        differences.compare("pbfToNodeIdentifier", a.toNodeIdentifier(), b.toNodeIdentifier());

        return differences;
    }
}
