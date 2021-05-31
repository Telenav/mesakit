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

package com.telenav.kivakit.graph.identifiers;

import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.GraphElement;

/**
 * An abstraction for identifiers used in a graph. Implementers of this interface include {@link EdgeIdentifier}, {@link
 * RelationIdentifier}, {@link VertexIdentifier} and {@link PlaceIdentifier}.
 *
 * @author jonathanl (shibo)
 * @see EdgeIdentifier
 * @see RelationIdentifier
 * @see VertexIdentifier
 * @see PlaceIdentifier
 */
public interface GraphElementIdentifier
{
    /**
     * @return This graph identifier value as a long
     */
    long asLong();

    /**
     * @return The graph element for this graph identifier from the given graph
     */
    GraphElement element(Graph graph);
}
