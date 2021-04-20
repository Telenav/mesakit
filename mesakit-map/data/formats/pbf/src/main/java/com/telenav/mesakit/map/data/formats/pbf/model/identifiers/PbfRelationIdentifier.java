////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.data.formats.pbf.model.identifiers;

import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelIdentifiers;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;

@UmlClassDiagram(diagram = DiagramPbfModelIdentifiers.class)
public class PbfRelationIdentifier extends MapRelationIdentifier implements PbfIdentifierType
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static MapRelationIdentifier forLong(final long identifier)
    {
        if (identifier == 0)
        {
            return NULL;
        }
        return new PbfRelationIdentifier(identifier);
    }

    public static PbfRelationIdentifier forRelation(final Relation relation)
    {
        return new PbfRelationIdentifier(relation.getId());
    }

    public static SwitchParser.Builder<PbfRelationIdentifier> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(PbfRelationIdentifier.class).name(name).description(description)
                .converter(new Converter(LOGGER));
    }

    public static class Converter extends BaseStringConverter<PbfRelationIdentifier>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfRelationIdentifier onConvertToObject(final String value)
        {
            return new PbfRelationIdentifier(Long.parseLong(value));
        }
    }

    /**
     * Construct from identifier
     */
    public PbfRelationIdentifier(final long identifier)
    {
        super(identifier);
    }

    public PbfRelationIdentifier(final PbfRelation relation)
    {
        this(relation.identifierAsLong());
    }

    @Override
    public EntityType entityType()
    {
        return EntityType.Relation;
    }

    @Override
    public MapRelationIdentifier newIdentifier(final long identifier)
    {
        return new PbfRelationIdentifier(identifier);
    }
}