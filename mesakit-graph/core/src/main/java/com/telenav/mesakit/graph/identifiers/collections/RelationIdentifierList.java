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
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapRelationIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfRelationIdentifier;

import java.util.List;

public class RelationIdentifierList extends ObjectList<MapRelationIdentifier>
{
    public static class Converter extends BaseStringConverter<RelationIdentifierList>
    {
        private final Separators separators;

        public Converter(final Listener listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected RelationIdentifierList onToValue(final String value)
        {
            final var split = StringList.split(value, separators.current());
            final var identifiers = new ObjectList<MapRelationIdentifier>();
            final var converter = new PbfRelationIdentifier.Converter(this);
            for (final var at : split)
            {
                identifiers.add(converter.convert(at));
            }
            return new RelationIdentifierList(identifiers);
        }

        @Override
        protected String onToString(final RelationIdentifierList value)
        {
            return value.join(separators.current());
        }
    }

    public RelationIdentifierList(final List<MapRelationIdentifier> identifiers)
    {
        super(Maximum.maximum(identifiers.size()));
        appendAll(identifiers);
    }

    public RelationIdentifierList(final Maximum maximumSize)
    {
        super(maximumSize);
    }
}