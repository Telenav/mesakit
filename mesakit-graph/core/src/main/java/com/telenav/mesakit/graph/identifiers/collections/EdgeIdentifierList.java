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

package com.telenav.mesakit.graph.identifiers.collections;

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.string.StringList;
import com.telenav.kivakit.kernel.language.string.formatting.Separators;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.scalars.counts.Maximum;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;

import java.util.List;

public class EdgeIdentifierList extends ObjectList<EdgeIdentifier>
{
    public static class Converter extends BaseStringConverter<EdgeIdentifierList>
    {
        private final Separators separators;

        public Converter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected EdgeIdentifierList onConvertToObject(final String value)
        {
            return new EdgeIdentifierList(StringList.split(value, separators.current())
                    .asObjectList(Maximum.MAXIMUM, new EdgeIdentifier.Converter(this)));
        }

        @Override
        protected String onConvertToString(final EdgeIdentifierList value)
        {
            return value.join(separators.current());
        }
    }

    public EdgeIdentifierList(final List<EdgeIdentifier> identifiers)
    {
        super(Maximum.maximum(identifiers.size()));
        appendAll(identifiers);
    }

    public EdgeIdentifierList(final Maximum maximumSize)
    {
        super(maximumSize);
    }

    public WayIdentifierList asPbfWayIdentifierList()
    {
        final var identifiers = new WayIdentifierList(Maximum.MAXIMUM);
        for (final var identifier : this)
        {
            identifiers.add(identifier.asWayIdentifier());
        }
        return identifiers;
    }
}
