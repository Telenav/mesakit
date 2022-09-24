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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.core.value.LongValuedConverter;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.value.count.BitCount;
import com.telenav.kivakit.core.value.identifier.IntegerIdentifier;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Vertex;

/**
 * Identifier of {@link Vertex}es in a {@link Graph}. The valid range for a vertex identifier is from 1 to {@link
 * Integer#MAX_VALUE}. Zero is not a valid vertex identifier because it is the Java uninitialized value.
 * <p>
 * Note that this is NOT the same as an PBF node identifier because Graph API {@link Vertex}es only occur at {@link
 * Edge} end-points ('from' and 'to' nodes).
 *
 * @author jonathanl (shibo)
 */
public class VertexIdentifier extends IntegerIdentifier implements GraphElementIdentifier
{
    /**
     * Although node identifiers can exceed 32 bits, the total number of Graph API {@link VertexIdentifier}s is much
     * less than that because vertex identifiers in the Graph API are limited to {@link Edge} intersection points of
     * which there are no more than two times the number of edges or log2(~300M * 2) = 30 bits. We add two extra bits to
     * allow for future proofing.
     * <p>
     * NOTE: The primitive value is present due to limitations in Java annotations (which reference this constant).
     */
    public static final BitCount SIZE = BitCount._32;

    public static SwitchParser.Builder<VertexIdentifier> vertexIdentifierSwitchParser(Listener listener,
                                                                                      String name,
                                                                                      String description)
    {
        return SwitchParser.builder(VertexIdentifier.class)
                .name(name)
                .description(description)
                .converter(new Converter(listener));
    }

    public static class Converter extends LongValuedConverter<VertexIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener, identifier ->
            {
                if (identifier > 0 && identifier < Integer.MAX_VALUE)
                {
                    return new VertexIdentifier(identifier.intValue());
                }
                return null;
            });
        }
    }

    public VertexIdentifier(int identifier)
    {
        super(identifier);
        assert identifier > 0;
    }

    /**
     * Implementation of {@link GraphElementIdentifier#element(Graph)}.
     *
     * @return The graph element from the given graph for this identifier
     */
    @Override
    public GraphElement element(Graph graph)
    {
        return graph.vertexForIdentifier(this);
    }

    /**
     * @return The next vertex identifier higher than this one
     */
    public VertexIdentifier next()
    {
        return new VertexIdentifier(asInt() + 1);
    }
}
