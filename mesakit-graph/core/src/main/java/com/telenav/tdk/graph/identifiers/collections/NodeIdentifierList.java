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
import com.telenav.kivakit.data.formats.library.map.identifiers.NodeIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfNodeIdentifier;

import java.util.List;

public class NodeIdentifierList extends ObjectList<NodeIdentifier>
{
    public static class Converter extends BaseStringConverter<NodeIdentifierList>
    {
        private final Separators separators;

        public Converter(final Listener<Message> listener, final Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected NodeIdentifierList onConvertToObject(final String value)
        {
            final var split = StringList.split(value, separators.current());
            final var identifiers = new ObjectList<NodeIdentifier>();
            final var converter = new PbfNodeIdentifier.Converter(this);
            for (final var at : split)
            {
                identifiers.add(converter.convert(at));
            }
            return new NodeIdentifierList(identifiers);
        }

        @Override
        protected String onConvertToString(final NodeIdentifierList value)
        {
            return value.join();
        }
    }

    public NodeIdentifierList(final List<NodeIdentifier> identifiers)
    {
        super(Maximum.of(identifiers.size()));
        appendAll(identifiers);
    }

    public NodeIdentifierList(final Maximum maximumSize)
    {
        super(maximumSize);
    }
}
