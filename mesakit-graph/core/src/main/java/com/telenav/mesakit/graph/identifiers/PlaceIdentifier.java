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
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;

/**
 * Identifier of {@link Place}s in a {@link Graph}. The identifier for a place can be retrieved with {@link
 * #identifier()} and the place for an identifier can be retrieved with {@link Graph#placeForIdentifier(PlaceIdentifier)}.
 *
 * @author jonathanl (shibo)
 */
public class PlaceIdentifier extends MapNodeIdentifier implements GraphElementIdentifier
{
    public static final int SIZE_IN_BITS = 64;

    public static SwitchParser.Builder<PlaceIdentifier> placeIdentifierSwitchParser(Listener listener,
                                                                                    String name,
                                                                                    String description)
    {
        return SwitchParser.switchParserBuilder(PlaceIdentifier.class)
                .name(name)
                .description(description)
                .converter(new Converter(listener));
    }

    public static class Converter extends LongValuedConverter<PlaceIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener, PlaceIdentifier::new);
        }
    }

    /**
     * Construct from identifier
     */
    public PlaceIdentifier(long identifier)
    {
        super(identifier);
    }

    @Override
    public GraphElement element(Graph graph)
    {
        return graph.placeForIdentifier(this);
    }

    /**
     * Returns the next identifier
     */
    @Override
    public PlaceIdentifier next()
    {
        return new PlaceIdentifier(asLong() + 1);
    }

    @Override
    protected MapIdentifier newIdentifier(long identifier)
    {
        return new PlaceIdentifier(identifier);
    }
}
