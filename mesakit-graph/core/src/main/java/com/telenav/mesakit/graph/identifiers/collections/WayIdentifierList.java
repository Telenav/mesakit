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
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WayIdentifierList extends ObjectList<PbfWayIdentifier>
{
    public static WayIdentifierList fromEdges(Maximum count, Iterable<Edge> edges)
    {
        var ways = new WayIdentifierList(count);
        for (var edge : edges)
        {
            if (!ways.contains(edge.wayIdentifier()))
            {
                ways.add(edge.wayIdentifier());
            }
        }
        return ways;
    }

    public static WayIdentifierList parse(String string)
    {
        return new Converter(Listener.none(), new Separators(",")).convert(string);
    }

    public static class Converter extends BaseStringConverter<WayIdentifierList>
    {
        private final Separators separators;

        public Converter(Listener listener, Separators separators)
        {
            super(listener);
            this.separators = separators;
        }

        @Override
        protected String onToString(WayIdentifierList value)
        {
            return value.join(separators.current());
        }

        @Override
        protected WayIdentifierList onToValue(String value)
        {
            var split = StringList.split(value, separators.current());
            var identifiers = new ObjectList<PbfWayIdentifier>();
            var converter = new PbfWayIdentifier.Converter(this);
            for (var at : split)
            {
                identifiers.add(converter.convert(at));
            }
            return new WayIdentifierList(identifiers);
        }
    }

    public WayIdentifierList(List<PbfWayIdentifier> identifiers)
    {
        super(Maximum.maximum(identifiers.size()));
        appendAll(identifiers);
    }

    public WayIdentifierList(Maximum maximumSize)
    {
        super(maximumSize);
    }

    @Override
    public WayIdentifierList uniqued()
    {
        var uniqued = new WayIdentifierList(count().asMaximum());
        Set<PbfWayIdentifier> identifiers = new HashSet<>();
        for (var identifier : this)
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
