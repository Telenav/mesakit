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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.primitive.Longs;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Strip;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.DiagramPbfModelIdentifiers;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;

@UmlClassDiagram(diagram = DiagramPbfModelIdentifiers.class)
public class PbfWayIdentifier extends MapWayIdentifier implements PbfIdentifierType
{
    public static MapWayIdentifier forLong(long identifier)
    {
        if (identifier == 0)
        {
            return NULL;
        }
        return new PbfWayIdentifier(identifier);
    }

    public static PbfWayIdentifier forWay(Way way)
    {
        return new PbfWayIdentifier(way.getId());
    }

    public static PbfWayIdentifier parse(String string)
    {
        var identifier = Longs.parseFast(Strip.trailing(string, "L"));
        if (identifier != Longs.INVALID)
        {
            return new PbfWayIdentifier(identifier);
        }
        return null;
    }

    public static SwitchParser.Builder<PbfWayIdentifier> pbfWayIdentifierSwitchParser(Listener listener,
                                                                                      String name,
                                                                                      String description)
    {
        return SwitchParser.builder(PbfWayIdentifier.class)
                .name(name)
                .description(description)
                .converter(new Converter(listener));
    }

    public static class Converter extends BaseStringConverter<PbfWayIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfWayIdentifier onToValue(String value)
        {
            return new PbfWayIdentifier(Long.parseLong(value));
        }
    }

    /**
     * Construct from identifier
     */
    public PbfWayIdentifier(long identifier)
    {
        super(identifier);
    }

    public PbfWayIdentifier(PbfWay way)
    {
        this(way.identifierAsLong());
    }

    @Override
    public EntityType entityType()
    {
        return EntityType.Way;
    }

    @Override
    public MapIdentifier newIdentifier(long identifier)
    {
        return new PbfWayIdentifier(identifier);
    }
}
