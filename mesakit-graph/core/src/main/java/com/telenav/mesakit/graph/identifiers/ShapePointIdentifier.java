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

package com.telenav.mesakit.graph.identifiers;

import com.telenav.kivakit.conversion.core.value.LongValuedConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.value.identifier.Identifier;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.ShapePoint;

import static com.telenav.kivakit.core.ensure.Ensure.unsupported;

/**
 * Identifier of {@link ShapePoint}s in a {@link Graph}.
 *
 * @author jonathanl (shibo)
 */
public class ShapePointIdentifier extends Identifier implements GraphElementIdentifier
{
    public static class Converter extends LongValuedConverter<ShapePointIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener, ShapePointIdentifier.class, ShapePointIdentifier::new);
        }
    }

    /**
     * Construct from identifier
     */
    public ShapePointIdentifier(long identifier)
    {
        super(identifier);
    }

    /**
     * Implementation of {@link GraphElementIdentifier#element(Graph)}.
     *
     * @return The graph element from the given graph for this identifier
     */
    @Override
    public GraphElement element(Graph graph)
    {
        return unsupported();
    }

    /**
     * Returns the next identifier higher than this one
     */
    public ShapePointIdentifier next()
    {
        return new ShapePointIdentifier(asLong() + 1);
    }
}
