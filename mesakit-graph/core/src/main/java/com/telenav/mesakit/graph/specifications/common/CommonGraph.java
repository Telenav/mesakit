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

import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.interfaces.collection.NextIterable;
import com.telenav.kivakit.interfaces.comparison.Filter;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.Extension;
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
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.motion.Speed;

import java.util.Collections;
import java.util.Iterator;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

/**
 * An efficient in-memory {@link Graph} implementation.
 *
 * @author jonathanl (shibo)
 */
public class CommonGraph extends Graph
{
    public CommonGraph(Metadata metadata)
    {
        super(metadata);
    }

    /**
     * @return The edges whose road shape intersects the given bounding rectangle which also match the given matcher
     */
    @Override
    public EdgeSequence edgesIntersecting(Rectangle bounds, Matcher<Edge> matcher,
                                          EdgeSequence.Type type)
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
                var index = edgeStore().spatialIndex();
                ensureNotNull(index);
                var sequence = new EdgeSequence(index.intersecting(bounds, matcher));
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
    public void load(GraphArchive archive)
    {
        super.load(archive);
        graphStore().load(archive);
    }

    @Override
    public Graph loadAllExcept(AttributeSet except)
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
    public final void loadFreeFlow(Resource side)
    {
        var speedConverter = new Speed.KilometersPerHourConverter(this);
        for (var line : side.reader().lines())
        {
            var columns = line.split(";");
            if (columns.length < 2)
            {
                warning("Lines in " + side + " must contain two or more columns separated by ';'");
            }
            Edge edge = null;
            String column = columns[1];
            if (column.contains(":"))
            {
                var edgeIdentifierConverter = new MapEdgeIdentifier.Converter(this);
                var identifier = edgeIdentifierConverter.convert(column);
                if (identifier != null)
                {
                    edge = edgeForIdentifier(identifier);
                }
            }
            else
            {
                var edgeIdentifierConverter = new EdgeIdentifier.Converter(this);
                var identifier = edgeIdentifierConverter.convert(column);
                if (identifier != null && contains(identifier))
                {
                    edge = edgeForIdentifier(identifier);
                }
            }
            if (edge != null)
            {
                var speed = speedConverter.convert(columns[2]);
                if (speed != null)
                {
                    edgeStore().storeFreeFlow(edge, speed);
                }
            }
        }
    }

    /**
     * Loads turn restriction data from the given side-resource
     *
     * @param side The resource to load
     */
    @Override
    public final void loadTurnRestrictions(Resource side)
    {
        var routeConverter = new Route.MapIdentifierConverter(this, new Separators(","), this);
        for (var line : side.reader().lines())
        {
            var columns = line.split(";");
            if (columns.length < 4)
            {
                warning("Lines in " + side + " must contain four or more columns separated by ';'");
            }
            var restriction = routeConverter.convert(columns[3]);
            if (restriction != null)
            {
                edgeStore().storeTurnRestriction(restriction);
            }
        }
    }

    @Override
    public Iterable<Place> placesInside(Rectangle bounds)
    {
        ensureNotNull(bounds);
        return Iterables.iterable(() -> new NextIterable<>()
        {
            final Iterator<Place> iterator = placeStore().spatialIndex().inside(bounds);

            @Override
            public Place next()
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
    public void save(GraphArchive archive)
    {
        // Get the file the archive is targeting
        var file = archive.file();

        // then save to that file with a ".tmp" extension"
        var temporaryFile = file.withExtension(Extension.TMP);
        temporaryFile.delete();
        var temporary = new GraphArchive(this, temporaryFile, archive.mode(), archive.reporter());
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
    public VertexSequence vertexesInside(Rectangle bounds)
    {
        return vertexesInside(bounds, Filter.acceptingAll());
    }

    @Override
    public VertexSequence vertexesInside(Rectangle bounds, Matcher<Vertex> matcher)
    {
        return vertexStore().vertexesInside(bounds, matcher);
    }
}
