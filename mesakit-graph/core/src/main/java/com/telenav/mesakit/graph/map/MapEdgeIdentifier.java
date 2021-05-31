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

package com.telenav.mesakit.graph.map;

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.strings.Split;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfNodeIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;

/**
 * Represents an map "edge" using the form "[way-identifier]:[from-node-identifier]:[to-node-identifier]"
 *
 * @author jonathanl (shibo)
 */
public class MapEdgeIdentifier
{
    public static class Converter extends BaseStringConverter<MapEdgeIdentifier>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected MapEdgeIdentifier onConvertToObject(final String value)
        {
            final var values = Split.split(value, ':').iterator();
            final var way = PbfWayIdentifier.parse(values.next());
            final var from = PbfNodeIdentifier.parse(values.next());
            final var to = PbfNodeIdentifier.parse(values.next());
            if (way != null && from != null && to != null)
            {
                return new MapEdgeIdentifier(way, from, to);
            }
            return null;
        }
    }

    public static class EdgeConverter extends BaseStringConverter<Edge>
    {
        private final Graph graph;

        private final Converter identifierConverter;

        public EdgeConverter(final Listener listener, final Graph graph)
        {
            super(listener);
            this.graph = graph;
            identifierConverter = new Converter(listener);
        }

        @Override
        protected Edge onConvertToObject(final String value)
        {
            return graph.edgeForIdentifier(identifierConverter.convert(value));
        }

        @Override
        protected String onConvertToString(final Edge edge)
        {
            return edge.mapEdgeIdentifier().toString();
        }
    }

    private final MapWayIdentifier way;

    private final MapNodeIdentifier from;

    private final MapNodeIdentifier to;

    public MapEdgeIdentifier(final MapWayIdentifier way, final MapNodeIdentifier from, final MapNodeIdentifier to)
    {
        this.way = way;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof MapEdgeIdentifier)
        {
            final var that = (MapEdgeIdentifier) object;
            return way.equals(that.way) && from.equals(that.from) && to.equals(that.to);
        }
        return false;
    }

    public MapNodeIdentifier from()
    {
        return from;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(way, from, to);
    }

    public MapNodeIdentifier to()
    {
        return to;
    }

    @Override
    public String toString()
    {
        return way + ":" + from + ":" + to;
    }

    public MapWayIdentifier way()
    {
        return way;
    }
}
