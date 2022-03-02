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

import com.telenav.kivakit.conversion.string.BaseStringConverter;
import com.telenav.kivakit.core.language.collections.list.ObjectList;
import com.telenav.kivakit.core.language.collections.list.StringList;
import com.telenav.kivakit.core.language.strings.formatting.Separators;
import com.telenav.kivakit.language.count.Maximum;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;

import java.util.List;

public class NodeIdentifierList extends ObjectList<MapNodeIdentifier>
{
    public static class Converter extends BaseStringConverter<NodeIdentifierList>
    {
        private final Separators separators;

        public Converter(Listener listener, Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected String onToString(NodeIdentifierList value)
        {
            return value.join();
        }

        @Override
        protected NodeIdentifierList onToValue(String value)
        {
            var split = StringList.split(value, separators.current());
            var identifiers = new ObjectList<MapNodeIdentifier>();
            var converter = new PbfNodeIdentifier.Converter(this);
            for (var at : split)
            {
                identifiers.add(converter.convert(at));
            }
            return new NodeIdentifierList(identifiers);
        }
    }

    public NodeIdentifierList(List<MapNodeIdentifier> identifiers)
    {
        super(Maximum.maximum(identifiers.size()));
        appendAll(identifiers);
    }

    public NodeIdentifierList(Maximum maximumSize)
    {
        super(maximumSize);
    }
}
