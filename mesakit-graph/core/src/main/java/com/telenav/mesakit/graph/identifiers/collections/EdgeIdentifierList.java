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
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;

import java.util.List;

public class EdgeIdentifierList extends ObjectList<EdgeIdentifier>
{
    public static class Converter extends BaseStringConverter<EdgeIdentifierList>
    {
        private final Separators separators;

        public Converter(Listener listener, Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected String onToString(EdgeIdentifierList value)
        {
            return value.join(separators.current());
        }

        @Override
        protected EdgeIdentifierList onToValue(String value)
        {
            return new EdgeIdentifierList(StringList.split(value, separators.current())
                    .asObjectList(new EdgeIdentifier.Converter(this)));
        }
    }

    public EdgeIdentifierList(List<EdgeIdentifier> identifiers)
    {
        super(Maximum.maximum(identifiers.size()));
        appendAll(identifiers);
    }

    public EdgeIdentifierList(Maximum maximumSize)
    {
        super(maximumSize);
    }

    public WayIdentifierList asPbfWayIdentifierList()
    {
        var identifiers = new WayIdentifierList(Maximum.MAXIMUM);
        for (var identifier : this)
        {
            identifiers.add(identifier.asWayIdentifier());
        }
        return identifiers;
    }
}
