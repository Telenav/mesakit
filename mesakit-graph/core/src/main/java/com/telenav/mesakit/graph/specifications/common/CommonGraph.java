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

package com.telenav.mesakit.graph.specifications.common;

import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.strings.formatting.Separators;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.messaging.filters.operators.All;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.identifiers.EdgeIdentifier;
import com.telenav.mesakit.graph.io.archive.GraphArchive;
import com.telenav.mesakit.graph.map.MapEdgeIdentifier;
import com.telenav.mesakit.graph.specifications.library.attributes.AttributeSet;
import com.telenav.mesakit.graph.traffic.historical.SpeedPatternStore;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.motion.Speed;

import java.util.Collections;
import java.util.Iterator;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensureNotNull;

/**
 * An efficient in-memory {@link Graph} implementation.
 *
 * @author jonathanl (shibo)
 */
public class CommonGraph extends Graph
{
    public CommonGraph(final Metadata metadata)
    {
        super(metadata);
    }

    /**
     * @return The edges whose road shape intersects the given bounding rectangle which also match the given matcher
     */
    @Override
    public EdgeSequence edgesIntersecting(final Rectangle bounds, final Matcher<Edge> matcher,
                                          final EdgeSequence.Type type)
    {
        ensureNotNull(bounds);

        if (edgeCount().isGreaterThan(Count._0))
        {
            if (bounds.isMaximum())
            {
                return edges();
            }
            else
            {
                final var index = edgeStore().spatialIndex();
                ensureNotNull(index);
                final var sequence = new EdgeSequence(index.intersecting(bounds, matcher));
                return type == EdgeSequence.Type.FORWARD_EDGES ? sequence : sequence.asDirectional();
            }
        }
        else
        {
            return new EdgeSequence(Collections.emptyList());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(final GraphArchive archive)
    {
        super.load(archive);
        graphStore().load(archive);
    }

    @Override
    public Graph loadAllExcept(final AttributeSet except)
    {
        graphStore().loadAllExcept(except);
        return this;
    }

    /**
     * Loads free flow data from the given side-resource
     *
     * @param side The resource to load
     */
    @Override
    public final void loadFreeFlow(final Resource side)
    {
        final var speedConverter = new Speed.KilometersPerHourConverter(this);
        for (final var line : side.reader().lines())
        {
            final var columns = line.split(";");
            if (columns.length < 2)
            {
                warning("Lines in " + side + " must contain two or more columns separated by ';'");
            }
            Edge edge = null;
            final String column = columns[1];
            if (column.contains(":"))
            {
                final var edgeIdentifierConverter = new MapEdgeIdentifier.Converter(this);
                final var identifier = edgeIdentifierConverter.convert(column);
                if (identifier != null)
                {
                    edge = edgeForIdentifier(identifier);
                }
            }
            else
            {
                final var edgeIdentifierConverter = new EdgeIdentifier.Converter(this);
                final var identifier = edgeIdentifierConverter.convert(column);
                if (identifier != null && contains(identifier))
                {
                    edge = edgeForIdentifier(identifier);
                }
            }
            if (edge != null)
            {
                final var speed = speedConverter.convert(columns[2]);
                if (speed != null)
                {
                    edgeStore().storeFreeFlow(edge, speed);
                }
            }
        }
    }

    @Override
    public final void loadSpeedPattern(final Resource side)
    {
        final var speedPattern = SpeedPatternStore.load(side);
        edgeStore().speedPatternStore(speedPattern);
    }

    /**
     * Loads turn restriction data from the given side-resource
     *
     * @param side The resource to load
     */
    @Override
    public final void loadTurnRestrictions(final Resource side)
    {
        final var routeConverter = new Route.MapIdentifierConverter(this, new Separators(","), this);
        for (final var line : side.reader().lines())
        {
            final var columns = line.split(";");
            if (columns.length < 4)
            {
                warning("Lines in " + side + " must contain four or more columns separated by ';'");
            }
            final var restriction = routeConverter.convert(columns[3]);
            if (restriction != null)
            {
                edgeStore().storeTurnRestriction(restriction);
            }
        }
    }

    @Override
    public Iterable<Place> placesInside(final Rectangle bounds)
    {
        ensureNotNull(bounds);
        return Iterables.iterable(() -> new Next<>()
        {
            final Iterator<Place> iterator = placeStore().spatialIndex().inside(bounds);

            @Override
            public Place onNext()
            {
                if (iterator.hasNext())
                {
                    return iterator.next();
                }
                return null;
            }
        });
    }

    @Override
    public Resource resource()
    {
        return graphStore().resource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final GraphArchive archive)
    {
        // Get the file the archive is targeting
        final var file = archive.file();

        // then save to that file with a ".tmp" extension"
        final var temporaryFile = file.withExtension(Extension.TMP);
        temporaryFile.delete();
        final var temporary = new GraphArchive(temporaryFile, archive.reporter(), archive.mode());
        super.save(temporary);
        graphStore().save(temporary);
        temporary.close();

        // delete the original target file if we're overwriting
        file.delete();

        // and rename the temporary archive to the final one
        temporary.file().renameTo(file);
    }

    @Override
    public VertexSequence vertexes()
    {
        return new VertexSequence(vertexStore());
    }

    @Override
    public VertexSequence vertexesInside(final Rectangle bounds)
    {
        return vertexesInside(bounds, new All<>());
    }

    @Override
    public VertexSequence vertexesInside(final Rectangle bounds, final Matcher<Vertex> matcher)
    {
        return vertexStore().vertexesInside(bounds, matcher);
    }
}
