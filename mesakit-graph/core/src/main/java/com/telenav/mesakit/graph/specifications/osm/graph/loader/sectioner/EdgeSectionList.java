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

package com.telenav.mesakit.graph.specifications.osm.graph.loader.sectioner;

import com.telenav.mesakit.graph.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * List of edge sections
 *
 * @author jonathanl (shibo)
 */
public class EdgeSectionList extends ArrayList<EdgeSection>
{
    private static final long serialVersionUID = -7153302710382047193L;

    /**
     * @return A list of edges created from the edge sections in this list with sequential identifiers assigned. For
     * example, if the edge identifier of the sectioned edge were 1234 000 000, a sequence of edges returned by this
     * method might be 1234 000 000, 1234 000 001, 1234 000 002, etc.
     */
    public List<Edge> edges()
    {
        // Edges to return
        List<Edge> edges = new ArrayList<>();

        // Get the parent edge identifier
        var identifier = get(0).edge().identifier();

        // Go through each section in this list
        for (var section : this)
        {
            // add an edge for the section
            edges.add(section.edge(identifier));

            // and advance to the next edge section identifier
            identifier = identifier.next();
        }

        return edges;
    }
}
