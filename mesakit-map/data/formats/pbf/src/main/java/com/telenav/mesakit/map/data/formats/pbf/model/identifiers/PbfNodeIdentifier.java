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

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.primitive.Longs;
import com.telenav.kivakit.core.string.Strip;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.internal.lexakai.DiagramPbfModelIdentifiers;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

@SuppressWarnings("unused")
@UmlClassDiagram(diagram = DiagramPbfModelIdentifiers.class)
public class PbfNodeIdentifier extends MapNodeIdentifier implements PbfIdentifierType
{
    private static Factory nodeIdentifierFactory;

    private static final long SEQUENCE_NUMBER_BASE = 1_000_000L;

    /**
     * First of 48 bit synthetic identifiers (within one graph)
     */
    private static final PbfNodeIdentifier SYNTHETIC_IDENTIFIER_FIRST = new PbfNodeIdentifier(
            99_999_999L * SEQUENCE_NUMBER_BASE);

    public static PbfNodeIdentifier forLong(long identifier)
    {
        if (identifier == 0)
        {
            return null;
        }
        return new PbfNodeIdentifier(identifier);
    }

    public static boolean isSynthetic(long identifier)
    {
        return SYNTHETIC_IDENTIFIER_FIRST.asLong() == (identifier / SEQUENCE_NUMBER_BASE * SEQUENCE_NUMBER_BASE);
    }

    public static PbfNodeIdentifier nextSyntheticNodeIdentifier()
    {
        if (nodeIdentifierFactory == null)
        {
            nodeIdentifierFactory = new Factory();
        }
        return nodeIdentifierFactory.newInstance();
    }

    public static PbfNodeIdentifier parse(String string)
    {
        var identifier = Longs.parseFastLong(Strip.trailing(string, "L"));
        if (identifier != Longs.INVALID)
        {
            return new PbfNodeIdentifier(identifier);
        }
        return null;
    }

    public static class Converter extends BaseStringConverter<PbfNodeIdentifier>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfNodeIdentifier onToValue(String value)
        {
            return new PbfNodeIdentifier(Long.parseLong(value));
        }
    }

    private static class Factory implements com.telenav.kivakit.interfaces.factory.Factory<PbfNodeIdentifier>
    {
        private PbfNodeIdentifier next = SYNTHETIC_IDENTIFIER_FIRST;

        @Override
        public synchronized PbfNodeIdentifier onNewInstance()
        {
            var next = this.next;
            this.next = (PbfNodeIdentifier) next.next();
            return next;
        }
    }

    public PbfNodeIdentifier(long identifier)
    {
        super(identifier);
    }

    public PbfNodeIdentifier(PbfNode node)
    {
        super(node.identifierAsLong());
    }

    public PbfNodeIdentifier(WayNode node)
    {
        super(node.getNodeId());
    }

    @Override
    public EntityType entityType()
    {
        return EntityType.Node;
    }

    public boolean isSynthetic()
    {
        return isSynthetic(asLong());
    }

    @Override
    public PbfNodeIdentifier newIdentifier(long identifier)
    {
        return new PbfNodeIdentifier(identifier);
    }
}
