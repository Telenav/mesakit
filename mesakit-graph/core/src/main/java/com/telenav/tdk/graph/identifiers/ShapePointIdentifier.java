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

package com.telenav.tdk.graph.identifiers;

import com.telenav.tdk.core.kernel.interfaces.numeric.Quantizable;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.core.kernel.scalars.identifiers.Identifier;
import com.telenav.tdk.graph.Graph;
import com.telenav.tdk.graph.GraphElement;
import com.telenav.tdk.graph.ShapePoint;

import static com.telenav.tdk.core.kernel.validation.Validate.unsupported;

/**
 * Identifier of {@link ShapePoint}s in a {@link Graph}.
 *
 * @author jonathanl (shibo)
 */
public class ShapePointIdentifier extends Identifier implements GraphElementIdentifier
{
    public static class Converter extends Quantizable.Converter<ShapePointIdentifier>
    {
        public Converter(final Listener<Message> listener)
        {
            super(listener, ShapePointIdentifier::new);
        }
    }

    /**
     * Construct from identifier
     */
    public ShapePointIdentifier(final long identifier)
    {
        super(identifier);
    }

    /**
     * Implementation of {@link GraphElementIdentifier#element(Graph)}.
     *
     * @return The graph element from the given graph for this identifier
     */
    @Override
    public GraphElement element(final Graph graph)
    {
        return unsupported();
    }

    /**
     * @return The next identifier higher than this one
     */
    public ShapePointIdentifier next()
    {
        return new ShapePointIdentifier(asLong() + 1);
    }
}
