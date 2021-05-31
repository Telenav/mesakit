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

package com.telenav.kivakit.graph.specifications.library.attributes;

import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.graph.Graph;
import com.telenav.kivakit.graph.io.archive.GraphArchive;
import com.telenav.kivakit.graph.specifications.common.edge.store.EdgeStore;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementStore;
import com.telenav.kivakit.graph.specifications.common.vertex.store.VertexStore;

/**
 * A store of attributes which is loaded with an {@link AttributeLoader}. Examples of attribute stores are all of the
 * subclasses of {@link GraphElementStore}, such as {@link VertexStore} and {@link EdgeStore} An attribute store has an
 * owning graph and a count of attributes.
 *
 * @author jonathanl (shibo)
 * @see GraphElementStore
 * @see Graph
 * @see AttributeLoader
 * @see AttributeReference
 */
public interface AttributeStore extends NamedObject
{
    /**
     * @return The archive associated with the attribute store
     */
    GraphArchive archive();

    /**
     * @return The attribute loader responsible for loading and unloading individual attributes as referenced by {@link
     * AttributeReference}
     */
    AttributeLoader attributeLoader();

    /**
     * @return A list of attributes supported by this store
     */
    AttributeList attributes();

    /**
     * @return The graph that owns this store
     */
    Graph graph();

    /**
     * @return The estimated size of this attribute store at allocation time. Returning a more accurate estimated size
     * can reduce the number of re-allocations that occur. In large attribute stores, it can be expensive to grow
     * dynamic data structures.
     */
    Estimate initialSize();

    /**
     * @return The number of attributes in this store
     */
    int size();

    /**
     * @return True if this store supports the given attribute
     */
    boolean supports(Attribute<?> attribute);
}
