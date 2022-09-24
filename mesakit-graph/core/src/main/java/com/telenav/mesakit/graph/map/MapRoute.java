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

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.list.StringList;
import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.messaging.messages.status.Problem;
import com.telenav.kivakit.core.string.Join;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.string.Split;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.RouteBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class MapRoute implements Iterable<MapEdgeIdentifier>
{
    @SuppressWarnings("DuplicatedCode") public static class Converter extends BaseStringConverter<MapRoute>
    {
        private final Separators separators;

        private final MapEdgeIdentifier.EdgeConverter edgeConverter;

        private final MapEdgeIdentifier.Converter edgeIdentifierConverter;

        private final Graph graph;

        public Converter(Listener listener, Graph graph, Separators separators)
        {
            super(listener);
            this.graph = graph;
            this.separators = separators;
            edgeConverter = new MapEdgeIdentifier.EdgeConverter(listener, this.graph);
            edgeIdentifierConverter = new MapEdgeIdentifier.Converter(listener);
        }

        public Converter(Listener listener, Separators separators)
        {
            this(listener, null, separators);
        }

        @Override
        protected String onToString(MapRoute route)
        {
            var identifiers = route.pbfEdgeIdentifiers();
            var edges = new StringList(Maximum.maximum(identifiers.size()));
            for (var identifier : identifiers)
            {
                edges.add(identifier.toString());
            }
            return edges.join(separators.current());
        }

        @Override
        protected MapRoute onToValue(String value)
        {
            if (graph == null)
            {
                List<MapEdgeIdentifier> identifiers = new ArrayList<>();
                for (var identifier : Split.split(value, separators.current()))
                {
                    identifiers.add(edgeIdentifierConverter.convert(identifier));
                }
                return new MapRoute(identifiers);
            }
            else
            {
                var builder = new RouteBuilder();
                try
                {
                    for (var identifier : Split.split(value, separators.current()))
                    {
                        var next = edgeConverter.convert(identifier);
                        if (next == null)
                        {
                            problem("Unable to locate edge $ ", identifier);
                            return null;
                        }
                        builder.append(next);
                    }
                }
                catch (Exception e)
                {
                    transmit(new Problem( e, "${class}: Problem converting ${debug} with graph ${debug}", subclass(),
                            value, graph.name()).maximumFrequency(problemBroadcastFrequency()));
                }
                return builder.route().asMapRoute();
            }
        }
    }

    private final List<MapEdgeIdentifier> identifiers;

    public MapRoute(List<MapEdgeIdentifier> identifiers)
    {
        this.identifiers = identifiers;
    }

    public MapRoute(Route route)
    {
        identifiers = new ArrayList<>();
        for (var edge : route)
        {
            identifiers.add(edge.mapEdgeIdentifier());
        }
    }

    @Override
    public Iterator<MapEdgeIdentifier> iterator()
    {
        return identifiers.iterator();
    }

    public String join(String separator)
    {
        return Join.join(identifiers, separator);
    }

    public Stream<MapEdgeIdentifier> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    public List<MapEdgeIdentifier> pbfEdgeIdentifiers()
    {
        return identifiers;
    }

    public Stream<MapEdgeIdentifier> stream()
    {
        return Streams.stream(this);
    }

    @Override
    public String toString()
    {
        return join("/");
    }
}
