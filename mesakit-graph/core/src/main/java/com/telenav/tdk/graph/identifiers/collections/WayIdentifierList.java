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

package com.telenav.kivakit.graph.identifiers.collections;

import com.telenav.kivakit.kernel.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.string.StringList;
import com.telenav.kivakit.kernel.language.string.formatting.Separators;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.kernel.scalars.counts.Maximum;
import com.telenav.kivakit.data.formats.library.map.identifiers.WayIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.kivakit.graph.Edge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WayIdentifierList extends ObjectList<WayIdentifier>
{
    public static WayIdentifierList fromEdges(final Maximum count, final Iterable<Edge> edges)
    {
        final var ways = new WayIdentifierList(count);
        for (final var edge : edges)
        {
            if (!ways.contains(edge.wayIdentifier()))
            {
                ways.add(edge.wayIdentifier());
            }
        }
        return ways;
    }

    public static WayIdentifierList parse(final String string)
    {
        return new Converter(Listener.NULL, new Separators(",")).convert(string);
    }

    public static class Converter extends BaseStringConverter<WayIdentifierList>
    {
        private final Separators separators;

        public Converter(final Listener<Message> listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected WayIdentifierList onConvertToObject(final String value)
        {
            final var split = StringList.split(value, separators.current());
            final var identifiers = new ObjectList<WayIdentifier>();
            final var converter = new PbfWayIdentifier.Converter(this);
            for (final var at : split)
            {
                identifiers.add(converter.convert(at));
            }
            return new WayIdentifierList(identifiers);
        }

        @Override
        protected String onConvertToString(final WayIdentifierList value)
        {
            return value.join(separators.current());
        }
    }

    public WayIdentifierList(final List<WayIdentifier> identifiers)
    {
        super(Maximum.of(identifiers.size()));
        appendAll(identifiers);
    }

    public WayIdentifierList(final Maximum maximumSize)
    {
        super(maximumSize);
    }

    public WayIdentifierList uniqued()
    {
        final var uniqued = new WayIdentifierList(Maximum.of(count()));
        final Set<WayIdentifier> identifiers = new HashSet<>();
        for (final var identifier : this)
        {
            if (!identifiers.contains(identifier))
            {
                uniqued.add(identifier);
            }
            identifiers.add(identifier);
        }
        return uniqued;
    }
}
