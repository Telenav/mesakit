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


package com.telenav.tdk.graph.world.grid;

import com.telenav.tdk.core.kernel.interfaces.object.Matcher;
import com.telenav.tdk.core.kernel.language.iteration.Iterables;
import com.telenav.tdk.core.kernel.language.iteration.Next;
import com.telenav.tdk.core.kernel.scalars.counts.Count;
import com.telenav.tdk.core.kernel.scalars.counts.MutableCount;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.collections.EdgeSequence;
import com.telenav.tdk.graph.collections.RelationSet;
import com.telenav.tdk.graph.collections.RouteList;
import com.telenav.tdk.graph.collections.VertexSequence;
import com.telenav.tdk.graph.project.TdkGraphCoreLimits.Limit;
import com.telenav.tdk.graph.traffic.roadsection.RoadSectionIdentifier;
import com.telenav.tdk.graph.world.WorldEdge;
import com.telenav.tdk.graph.world.WorldRelation;
import com.telenav.tdk.graph.world.WorldVertex;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.road.model.RoadFunctionalClass;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A list of {@link WorldCell}s with convenient methods:
 * <p>
 * <b>Cells</b>
 * <ul>
 *     <li>{@link #first()} - The first cell in the list</li>
 *     <li>{@link #smallest()} - The cell with the smallest file size</li>
 *     <li>{@link #biggest()} - The cell with the largest file size</li>
 *     <li>{@link #cellGraphs()} - All the cell graphs for cells in this list</li>
 * </ul>
 * <p>
 * <b>Graph Element Counts</b>
 * <ul>
 *     <li>{@link #count()} - The number of cells</li>
 *     <li>{@link #edgeCount()} - The total number of edges</li>
 *     <li>{@link #forwardEdgeCount()} - The total number of forward edges</li>
 *     <li>{@link #vertexCount()} - The total number of vertexes</li>
 *     <li>{@link #relationCount()} - The total number of relations</li>
 * </ul>
 * <p>
 * <b>Graph Elements</b>
 * <ul>
 *     <li>{@link #edges()} - All edges</li>
 *     <li>{@link #forwardEdges()} - All forward edges</li>
 *     <li>{@link #vertexes()} - All vertexes</li>
 *     <li>{@link #relations()} - All relations</li>
 *     <li>{@link #edgesIntersecting(Rectangle, Matcher)} - All edges within the rectangle matching the given matcher</li>
 *     <li>{@link #forwardEdgesIntersecting(Rectangle, Matcher)} - All forward edges within the rectangle matching the given matcher</li>
 *     <li>{@link #vertexesInside(Rectangle)} - Vertexes inside the given rectangle</li>
 *     <li>{@link #vertexesNearest(Location, Distance, RoadFunctionalClass)} - Vertexes from cells in the list nearest to the given
 *     location within the given distance having the given minimum functional class</li>
 *     <li>{@link #relationsIntersecting(Rectangle, Matcher)} - All relations that intersect the rectangle and match the matcher</li>
 * </ul>
 * <p>
 * <b>Traffic</b>
 * <ul>
 *     <li>{@link #roadSectionIdentifiersIntersecting(Rectangle)} - All road section identifiers intersecting the rectangle</li>
 *     <li>{@link #routeFor(RoadSectionIdentifier)} - A route through these cells for the given road section identifier</li>
 * </ul>
 *
 * @author jonathanl (shibo)
 * @see WorldCell
 * @see Graph
 * @see EdgeSequence
 * @see VertexSequence
 */
public class WorldCellList extends ArrayList<WorldCell>
{
    private static final long serialVersionUID = 2966124883671643037L;

    public WorldCell biggest()
    {
        WorldCell biggest = null;
        for (final var worldCell : this)
        {
            if (biggest == null || worldCell.fileSize().isGreaterThan(biggest.fileSize()))
            {
                biggest = worldCell;
            }
        }
        return biggest;
    }

    public List<Graph> cellGraphs()
    {
        return stream().map(WorldCell::cellGraph).collect(Collectors.toList());
    }

    public boolean contains(final Edge edge)
    {
        for (final var worldCell : this)
        {
            if (worldCell.cellGraph().contains(edge))
            {
                return true;
            }
        }
        return false;
    }

    public boolean contains(final EdgeRelation relation)
    {
        for (final var worldCell : this)
        {
            if (worldCell.cellGraph().contains(relation))
            {
                return true;
            }
        }
        return false;
    }

    public boolean contains(final Vertex vertex)
    {
        for (final var worldCell : this)
        {
            if (worldCell.cellGraph().contains(vertex))
            {
                return true;
            }
        }
        return false;
    }

    public Count count()
    {
        return Count.of(size());
    }

    public Count edgeCount()
    {
        return count(Graph::edgeCount);
    }

    public EdgeSequence edges()
    {
        return edges(worldCell -> worldCell.cellGraph().edges());
    }

    public EdgeSequence edgesIntersecting(final Rectangle bounds, final Matcher<Edge> matcher)
    {
        return edges(worldCell -> worldCell.cellGraph().edgesIntersecting(bounds, matcher));
    }

    public WorldCell first()
    {
        if (!isEmpty())
        {
            return get(0);
        }
        return null;
    }

    public Count forwardEdgeCount()
    {
        return count(Graph::forwardEdgeCount);
    }

    public EdgeSequence forwardEdges()
    {
        return edges(worldCell -> worldCell.cellGraph().forwardEdges());
    }

    public EdgeSequence forwardEdgesIntersecting(final Rectangle bounds, final Matcher<Edge> matcher)
    {
        return edges(worldCell -> worldCell.cellGraph().forwardEdgesIntersecting(bounds, matcher));
    }

    public Count relationCount()
    {
        return count(Graph::relationCount);
    }

    public Iterable<EdgeRelation> relations()
    {
        return relations(worldCell -> worldCell.cellGraph().relations());
    }

    public RelationSet relationsIntersecting(final Rectangle bounds, final Matcher<EdgeRelation> matcher)
    {
        return relations(worldCell -> worldCell.cellGraph().relationsIntersecting(bounds));
    }

    public Set<RoadSectionIdentifier> roadSectionIdentifiersIntersecting(final Rectangle bounds)
    {
        final Set<RoadSectionIdentifier> identifiers = new HashSet<>();
        for (final var worldCell : this)
        {
            identifiers.addAll(worldCell.cellGraph().roadSectionIdentifiersIntersecting(bounds));
        }
        return identifiers;
    }

    public Route routeFor(final RoadSectionIdentifier identifier)
    {
        final var routes = new RouteList();
        for (final var worldCell : this)
        {
            final var route = worldCell.cellGraph().routeFor(identifier);
            if (route != null)
            {
                routes.add(Route.forEdges(worldCell.asWorldEdges(route)));
            }
        }
        return routes.asRoute();
    }

    public WorldCell smallest()
    {
        WorldCell smallest = null;
        for (final var worldCell : this)
        {
            if (smallest == null || worldCell.fileSize().isSmallerThan(smallest.fileSize()))
            {
                smallest = worldCell;
            }
        }
        return smallest;
    }

    public WorldCellList sortedDescendingByPbfSize()
    {
        final var list = new WorldCellList();
        list.addAll(this);
        list.sort(Comparator.comparing(cell -> cell.pbfFile().size()));
        Collections.reverse(list);
        return list;
    }

    public Count vertexCount()
    {
        return count(Graph::vertexCount);
    }

    public VertexSequence vertexes()
    {
        return vertexes(worldCell -> worldCell.cellGraph().vertexes());
    }

    public VertexSequence vertexesInside(final Rectangle bounds)
    {
        return vertexes(worldCell -> worldCell.cellGraph().vertexesInside(bounds));
    }

    public VertexSequence vertexesInside(final Rectangle bounds, final Matcher<Vertex> matcher)
    {
        return vertexes(worldCell -> worldCell.cellGraph().vertexesInside(bounds, matcher));
    }

    public VertexSequence vertexesNearest(final Location location, final Distance maximum, final RoadFunctionalClass functionalClass)
    {
        return vertexes(worldCell ->
        {
            final var graph = worldCell.cellGraph();
            if (graph != null)
            {
                final var vertex = graph.vertexNearest(location, maximum, functionalClass);
                if (vertex != null)
                {
                    return new VertexSequence(Collections.singletonList(vertex));
                }
            }
            return new VertexSequence(Collections.emptyList());
        });
    }

    private Count count(final Function<Graph, Count> function)
    {
        final var count = new MutableCount();
        for (final var graph : cellGraphs())
        {
            count.add(function.apply(graph));
        }
        return count.asCount();
    }

    @SuppressWarnings("Convert2Diamond")
    private EdgeSequence edges(final Function<WorldCell, EdgeSequence> sequence)
    {
        return new EdgeSequence(Iterables.of(() -> new Next<Edge>()
        {
            final Iterator<WorldCell> worldCellIterator = iterator();

            Iterator<Edge> edgeIterator;

            WorldCell worldCell;

            @Override
            public Edge onNext()
            {
                while (edgeIterator == null || !edgeIterator.hasNext())
                {
                    if (worldCellIterator.hasNext())
                    {
                        worldCell = worldCellIterator.next();
                        edgeIterator = sequence.apply(worldCell).iterator();
                    }
                    else
                    {
                        return null;
                    }
                }
                return new WorldEdge(worldCell, edgeIterator.next());
            }
        }));
    }

    private RelationSet relations(final Function<WorldCell, Iterable<EdgeRelation>> sequence)
    {
        return RelationSet.forIterable(Limit.RELATIONS, Iterables.of(() -> new Next<>()
        {
            final Iterator<WorldCell> cellIterator = iterator();

            Iterator<EdgeRelation> relationIterator;

            WorldCell worldCell;

            @Override
            public EdgeRelation onNext()
            {
                while (relationIterator == null || !relationIterator.hasNext())
                {
                    if (cellIterator.hasNext())
                    {
                        worldCell = cellIterator.next();
                        relationIterator = sequence.apply(worldCell).iterator();
                    }
                    else
                    {
                        return null;
                    }
                }

                // Get the relation
                final var relation = relationIterator.next();

                // and if it's a turn restriction
                if (relation.isTurnRestriction())
                {
                    // with a via node location that is not in this cell
                    final var viaNodeLocation = relation.viaNodeLocation();
                    if (viaNodeLocation != null && !viaNodeLocation.isOrigin()
                            && !worldCell.contains(viaNodeLocation))
                    {
                        // then skip it because it will be found in the other cell
                        return onNext();
                    }
                }

                // otherwise, return the relation
                return new WorldRelation(worldCell, relation);
            }
        }));
    }

    private VertexSequence vertexes(final Function<WorldCell, VertexSequence> sequenceForCell)
    {
        return new VertexSequence(Iterables.of(() -> new Next<>()
        {
            WorldCell worldCell;

            final Iterator<WorldCell> cellIterator = iterator();

            Iterator<Vertex> vertexIterator;

            @SuppressWarnings("LoopStatementThatDoesntLoop")
            @Override
            public Vertex onNext()
            {
                // While there's no vertex iterator, or it's out of elements
                while (vertexIterator == null || !vertexIterator.hasNext())
                {
                    // and there are more cells to look in,
                    while (cellIterator.hasNext())
                    {
                        // then get the next cell
                        worldCell = cellIterator.next();

                        // and ask it for any vertexes.
                        vertexIterator = sequenceForCell.apply(worldCell).iterator();

                        // If there are vertexes,
                        if (vertexIterator.hasNext())
                        {
                            // return the next one
                            return new WorldVertex(worldCell, vertexIterator.next());
                        }
                    }

                    // Out of cells to look in
                    return null;
                }

                return new WorldVertex(worldCell, vertexIterator.next());
            }
        }));
    }
}
