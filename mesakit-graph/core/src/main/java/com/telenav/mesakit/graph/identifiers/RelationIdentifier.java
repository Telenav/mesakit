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
import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;

/**
 * Identifier of relations in a map graph
 *
 * @author jonathanl (shibo)
 */
public class RelationIdentifier extends MapRelationIdentifier implements GraphElementIdentifier
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<RelationIdentifier> relationIdentifierSwitchParser(String name,
                                                                                          String description)
    {
        return SwitchParser.builder(RelationIdentifier.class)
                .name(name)
                .description(description)
                .converter(new Converter(LOGGER));
    }

    public static class Converter extends Quantizable.Converter<RelationIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener, RelationIdentifier::new);
        }
    }

    /**
     * Construct from identifier
     */
    public RelationIdentifier(long identifier)
    {
        super(identifier);
    }

    @Override
    public GraphElement element(Graph graph)
    {
        return graph.relationForIdentifier(this);
    }

    /**
     * @return The next identifier
     */
    @Override
    public RelationIdentifier next()
    {
        return new RelationIdentifier(asLong() + 1);
    }

    @Override
    protected MapIdentifier newIdentifier(long identifier)
    {
        return new RelationIdentifier(identifier);
    }
}
