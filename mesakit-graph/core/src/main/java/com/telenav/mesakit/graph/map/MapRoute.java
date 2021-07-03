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
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.iteration.Streams;
import com.telenav.kivakit.kernel.language.strings.Join;
import com.telenav.kivakit.kernel.language.strings.Split;
import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.RouteBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class MapRoute implements Iterable<MapEdgeIdentifier>
{
    public static class Converter extends BaseStringConverter<MapRoute>
    {
        private final Separators separators;

        private final MapEdgeIdentifier.EdgeConverter edgeConverter;

        private final MapEdgeIdentifier.Converter edgeIdentifierConverter;

        private final Graph graph;

        public Converter(final Listener listener, final Graph graph, final Separators separators)
        {
            super(listener);
            this.graph = graph;
            this.separators = separators;
            this.edgeConverter = new MapEdgeIdentifier.EdgeConverter(listener, this.graph);
            this.edgeIdentifierConverter = new MapEdgeIdentifier.Converter(listener);
        }

        public Converter(final Listener listener, final Separators separators)
        {
            this(listener, null, separators);
        }

        @Override
        protected MapRoute onToValue(final String value)
        {
            if (this.graph == null)
            {
                final List<MapEdgeIdentifier> identifiers = new ArrayList<>();
                for (final var identifier : Split.split(value, this.separators.current()))
                {
                    identifiers.add(this.edgeIdentifierConverter.convert(identifier));
                }
                return new MapRoute(identifiers);
            }
            else
            {
                final var builder = new RouteBuilder();
                try
                {
                    for (final var identifier : Split.split(value, this.separators.current()))
                    {
                        final var next = this.edgeConverter.convert(identifier);
                        if (next == null)
                        {
                            problem("Unable to locate edge $ ", identifier);
                            return null;
                        }
                        builder.append(next);
                    }
                }
                catch (final Exception e)
                {
                    problem(problemBroadcastFrequency(), e, "${class}: Problem converting ${debug} with graph ${debug}", subclass(),
                            value, this.graph.name());
                }
                return builder.route().asMapRoute();
            }
        }

        @Override
        protected String onToString(final MapRoute route)
        {
            final var identifiers = route.pbfEdgeIdentifiers();
            final var edges = new StringList(Maximum.maximum(identifiers.size()));
            for (final var identifier : identifiers)
            {
                edges.add(identifier.toString());
            }
            return edges.join(this.separators.current());
        }
    }

    private final List<MapEdgeIdentifier> identifiers;

    public MapRoute(final List<MapEdgeIdentifier> identifiers)
    {
        this.identifiers = identifiers;
    }

    public MapRoute(final Route route)
    {
        this.identifiers = new ArrayList<>();
        for (final var edge : route)
        {
            this.identifiers.add(edge.mapEdgeIdentifier());
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<MapEdgeIdentifier> iterator()
    {
        return this.identifiers.iterator();
    }

    public String join(final String separator)
    {
        return Join.join(this.identifiers, separator);
    }

    public Stream<MapEdgeIdentifier> parallelStream()
    {
        return Streams.parallelStream(this);
    }

    public List<MapEdgeIdentifier> pbfEdgeIdentifiers()
    {
        return this.identifiers;
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