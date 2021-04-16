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

import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelIdentifiers;
import com.telenav.kivakit.core.kernel.language.bits.BitDiagram;
import com.telenav.kivakit.core.kernel.language.bits.BitDiagram.BitField;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;

/**
 * Implements type encoding for different kinds of PBF identifiers. This class is roughly a "mix-in" through default
 * method implementations and interface fields.
 */
@UmlClassDiagram(diagram = DiagramPbfModelIdentifiers.class)
public interface PbfIdentifierType
{
    BitDiagram TYPE_AND_IDENTIFIER = new BitDiagram("RTTTIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII IIIIIIII");
    BitField TYPE = TYPE_AND_IDENTIFIER.field('T');
    BitField REVERSE = TYPE_AND_IDENTIFIER.field('R');
    BitField IDENTIFIER = TYPE_AND_IDENTIFIER.field('I');

    /**
     * @return An {@link MapIdentifier} of the correct type for the TYPE {@link BitField}
     */
    static MapIdentifier forIdentifierAndType(final long identifierAndType)
    {
        final var type = MapIdentifier.Type.forOrdinal(TYPE.extractInt(identifierAndType));
        if (type != null)
        {
            var identifier = IDENTIFIER.extractLong(identifierAndType);
            if (REVERSE.extractBoolean(identifierAndType))
            {
                identifier *= -1;
            }
            return forIdentifierAndType(type, identifier);
        }
        return null;
    }

    static MapIdentifier forIdentifierAndType(final MapIdentifier.Type type, final long identifier)
    {
        switch (type)
        {
            case NODE:
                return new PbfNodeIdentifier(identifier);

            case WAY:
                return new PbfWayIdentifier(identifier);

            case RELATION:
                return new PbfRelationIdentifier(identifier);

            case INVALID:
            default:
                throw new IllegalArgumentException();
        }
    }

    long asLong();

    EntityType entityType();

    MapIdentifier newIdentifier(long identifier);

    MapIdentifier.Type type();

    /**
     * @return This OSM identifier with the type encoded in it according to the {@link BitDiagram} layout
     * TYPE_AND_IDENTIFIER
     */
    default MapIdentifier withType()
    {
        final var identifierWithType = TYPE.set(Math.abs(asLong()), type().identifier());
        final var identifierWithTypeAndSign = REVERSE.set(identifierWithType, asLong() < 0);
        return newIdentifier(identifierWithTypeAndSign);
    }

    /**
     * @return This OSM identifier with the type encoded in it according to the {@link BitDiagram} layout
     * TYPE_AND_IDENTIFIER
     */
    default MapIdentifier withType(final MapIdentifier identifier, final MapIdentifier.Type type)
    {
        final var identifierWithType = TYPE.set(Math.abs(identifier.asLong()), type.identifier());
        final var identifierWithTypeAndSign = REVERSE.set(identifierWithType, identifier.asLong() < 0);
        return newIdentifier(identifierWithTypeAndSign);
    }

    /**
     * @return This OSM identifier with the type encoded in it according to the {@link BitDiagram} layout
     * TYPE_AND_IDENTIFIER
     */
    default MapIdentifier withoutType()
    {
        var identifier = IDENTIFIER.extractLong(asLong());
        if (REVERSE.extractBoolean(asLong()))
        {
            identifier = identifier * -1;
        }
        return newIdentifier(identifier);
    }
}
