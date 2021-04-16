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

import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.project.lexakai.diagrams.DiagramPbfModelIdentifiers;
import com.telenav.kivakit.core.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.kernel.language.primitives.Longs;
import com.telenav.kivakit.core.kernel.language.strings.Strip;
import com.telenav.kivakit.core.kernel.messaging.Listener;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

@UmlClassDiagram(diagram = DiagramPbfModelIdentifiers.class)
public class PbfNodeIdentifier extends MapNodeIdentifier implements PbfIdentifierType
{
    private static Factory nodeIdentifierFactory;

    private static final long SEQUENCE_NUMBER_BASE = 1_000_000L;

    /**
     * First of 48 bit synthetic identifiers (within one graph)
     */
    private static final MapNodeIdentifier SYNTHETIC_IDENTIFIER_FIRST = new PbfNodeIdentifier(
            99_999_999L * SEQUENCE_NUMBER_BASE);

    public static PbfNodeIdentifier forLong(final long identifier)
    {
        if (identifier == 0)
        {
            return null;
        }
        return new PbfNodeIdentifier(identifier);
    }

    public static boolean isSynthetic(final long identifier)
    {
        return SYNTHETIC_IDENTIFIER_FIRST.asLong() == (identifier / SEQUENCE_NUMBER_BASE * SEQUENCE_NUMBER_BASE);
    }

    public static MapNodeIdentifier nextSyntheticNodeIdentifier()
    {
        if (nodeIdentifierFactory == null)
        {
            nodeIdentifierFactory = new Factory();
        }
        return nodeIdentifierFactory.newInstance();
    }

    public static PbfNodeIdentifier parse(final String string)
    {
        final var identifier = Longs.parse(Strip.trailing(string, "L"));
        if (identifier != Longs.INVALID)
        {
            return new PbfNodeIdentifier(identifier);
        }
        return null;
    }

    public static class Converter extends BaseStringConverter<PbfNodeIdentifier>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected PbfNodeIdentifier onConvertToObject(final String value)
        {
            return new PbfNodeIdentifier(Long.parseLong(value));
        }
    }

    private static class Factory implements com.telenav.kivakit.core.kernel.interfaces.factory.Factory<MapNodeIdentifier>
    {
        private MapNodeIdentifier next = SYNTHETIC_IDENTIFIER_FIRST;

        @Override
        public synchronized MapNodeIdentifier newInstance()
        {
            final var next = this.next;
            this.next = (MapNodeIdentifier) next.next();
            return next;
        }
    }

    public PbfNodeIdentifier(final long identifier)
    {
        super(identifier);
    }

    public PbfNodeIdentifier(final PbfNode node)
    {
        super(node.identifierAsLong());
    }

    public PbfNodeIdentifier(final WayNode node)
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
    public PbfNodeIdentifier newIdentifier(final long identifier)
    {
        return new PbfNodeIdentifier(identifier);
    }
}
