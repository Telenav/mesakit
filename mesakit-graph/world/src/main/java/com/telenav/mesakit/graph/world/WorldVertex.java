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

package com.telenav.mesakit.graph.world;

import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.MutableCount;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.GraphElement;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSequence;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.identifiers.VertexIdentifier;
import com.telenav.mesakit.graph.project.GraphCoreLimits;
import com.telenav.mesakit.graph.specifications.common.node.NodeAttributes;
import com.telenav.mesakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.mesakit.graph.world.grid.WorldCell;
import com.telenav.mesakit.graph.world.identifiers.WorldVertexIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.GradeSeparation;

import java.util.ArrayList;
import java.util.List;

/**
 * An vertex in a {@link WorldGraph}, scoped by a {@link WorldCell} and having a {@link WorldVertexIdentifier}. The cell
 * for this graph element can be retrieved with {@link #worldCell()} and an override of the {@link #identifier()} method
 * from {@link Vertex} returns the {@link WorldVertexIdentifier} for the vertex. Other methods ({@link #inEdges()},
 * {@link #outEdges()}, etc) are overridden to ensure that graph elements accessed through a world vertex continue to be
 * scoped by cell.
 * <p>
 * Note that there are two methods to retrieve the graph of this {@link GraphElement}. The {@link #graph()} method
 * overrides {@link #graph()} to return the {@link WorldGraph} that contains this element, while the {@link #subgraph()}
 * method returns the graph where the element is actually stored (the cell sub-graph of the world graph).
 * <p>
 * World graph vertexes have special logic to handle the case where a vertex has been created at a "clean cut" node at
 * the exact edge of the cell during graph extraction with the PbfWorldGraphExtractorApplication. In this case, a vertex
 * will return true from {@link #isClipped()} (and possibly {@link #isSynthetic()} if the node didn't already exist and
 * had to be created by MesaKit). For clipped edges, methods like {@link #inEdgeCount()} and {@link #inEdges()} will
 * look for an equivalent vertex in any neighboring cells (using the private method {@link
 * #equivalentNeighboringVertexes()}) to produce the illusion that the vertex has one set of in edges, even if these
 * edges might come from two or more cell graphs.
 *
 * @author jonathanl (shibo)
 * @see WorldGraph
 * @see WorldCell
 * @see WorldVertexIdentifier
 */
public class WorldVertex extends Vertex
{
    /** The cell where this vertex is located */
    private final WorldCell worldCell;

    public WorldVertex(final WorldCell worldCell, final Vertex vertex)
    {
        super(null, vertex.identifier());
        this.worldCell = worldCell;
    }

    public WorldVertex(final WorldCell worldCell, final VertexIdentifier identifier)
    {
        super(null, identifier);
        this.worldCell = worldCell;
    }

    public WorldVertex(final WorldVertexIdentifier identifier)
    {
        super(null, identifier);
        worldCell = identifier.worldCell();
    }

    @Override
    public Count edgeCount()
    {
        if (isClipped())
        {
            return inEdgeCount().plus(outEdgeCount());
        }
        else
        {
            return super.edgeCount();
        }
    }

    @Override
    public boolean equals(final Object object)
    {
        // If the object is a world vertex
        if (object instanceof WorldVertex)
        {
            // and the vertex is clipped or synthetic,
            final var that = (WorldVertex) object;
            if (isClipped() || isSynthetic())
            {
                // it doesn't have an identifier that will compare with vertexes in neighboring
                // cell(s), so compare by location and grade separation
                final var sameLocation = location().equals(that.location());

                // For OSM specified data,
                if (isOsm())
                {
                    // we must be at the same grade separation as well as the same location
                    if (sameLocation)
                    {
                        final var thisVertex = vertex(this);
                        final var thatVertex = vertex(that);
                        final var thisGrade = thisVertex.gradeSeparation();
                        final var thatGrade = thatVertex.gradeSeparation();
                        return thisGrade.equals(thatGrade);
                    }
                }
                return sameLocation;
            }
            else
            {
                // otherwise, compare the cell-relative vertex identifier and then make sure this
                // and that are the same cell
                return super.equals(that) && worldCell.equals(that.worldCell);
            }
        }
        return false;
    }

    @Override
    public GradeSeparation gradeSeparation()
    {
        return cellVertex().gradeSeparation();
    }

    @Override
    public Graph graph()
    {
        return worldCell.worldGraph();
    }

    @Override
    public int hashCode()
    {
        if (isClipped() || isSynthetic())
        {
            return Hash.many(location(), vertex(this).gradeSeparation());
        }
        else
        {
            return (int) identifierAsLong() ^ worldCell.hashCode();
        }
    }

    @Override
    public VertexIdentifier identifier()
    {
        return new WorldVertexIdentifier(worldCell, super.identifier());
    }

    @Override
    public Count inEdgeCount()
    {
        if (isClipped())
        {
            final var count = new MutableCount();
            for (final var vertex : equivalentNeighboringVertexes())
            {
                count.plus(vertex.superInEdgeCount());
            }
            return count.asCount();
        }
        else
        {
            return superInEdgeCount();
        }
    }

    @Override
    public EdgeSequence inEdgeSequence()
    {
        if (isClipped())
        {
            return inEdges().asSequence();
        }
        else
        {
            return superInEdgeSequence();
        }
    }

    @Override
    public EdgeSet inEdges()
    {
        if (isClipped())
        {
            final var edges = new WorldEdgeSet(GraphCoreLimits.Estimated.EDGES_PER_VERTEX);
            for (final var vertex : equivalentNeighboringVertexes())
            {
                edges.addAll(vertex.worldCell(), vertex.superInEdges());
            }
            return edges;
        }
        else
        {
            return superInEdges();
        }
    }

    /**
     * We have to override this method because {@link #equivalentNeighboringVertexes()} calls it and {@link
     * Vertex#mapIdentifier()} calls outEdges(), which is implemented in this class by calling (you guessed it!) {@link
     * #equivalentNeighboringVertexes()}.
     * <p>
     * Since we don't actually need all the edges in neighboring cells to get the identifier of this vertex, so we can
     * call {@link #superInEdges()} and {@link #superOutEdges()} instead.
     */
    @Override
    public MapNodeIdentifier mapIdentifier()
    {
        if (supports(NodeAttributes.get().NODE_IDENTIFIER))
        {
            return super.mapIdentifier();
        }
        final Edge out = superOutEdgeSequence().first();
        if (out != null)
        {
            return out.fromNodeIdentifier();
        }
        final Edge in = superInEdgeSequence().first();
        if (in != null)
        {
            return in.toNodeIdentifier();
        }
        return null;
    }

    @Override
    public Count outEdgeCount()
    {
        if (isClipped())
        {
            final var count = new MutableCount();
            for (final var vertex : equivalentNeighboringVertexes())
            {
                count.plus(vertex.superOutEdgeCount());
            }
            return count.asCount();
        }
        else
        {
            return superOutEdgeCount();
        }
    }

    @Override
    public EdgeSequence outEdgeSequence()
    {
        if (isClipped())
        {
            return outEdges().asSequence();
        }
        else
        {
            return superOutEdgeSequence();
        }
    }

    @Override
    public EdgeSet outEdges()
    {
        if (isClipped())
        {
            final var edges = new WorldEdgeSet(GraphCoreLimits.Estimated.EDGES_PER_VERTEX);
            for (final var vertex : equivalentNeighboringVertexes())
            {
                edges.addAll(vertex.worldCell(), vertex.superOutEdges());
            }
            return edges;
        }
        else
        {
            return superOutEdges();
        }
    }

    public WorldCell worldCell()
    {
        return worldCell;
    }

    @Override
    protected Graph subgraph()
    {
        return worldCell.cellGraph();
    }

    protected Vertex vertex(final WorldVertex vertex)
    {
        final var thisGraph = (OsmGraph) vertex.worldCell().cellGraph();
        return thisGraph.vertexForIdentifier(vertex.identifier());
    }

    private Vertex cellVertex()
    {
        return subgraph().vertexForIdentifier(identifier());
    }

    /**
     * @return Clipped vertexes at the same exact location in neighboring cells
     */
    private List<WorldVertex> equivalentNeighboringVertexes()
    {
        // List of vertexes to return
        final List<WorldVertex> neighbors = new ArrayList<>();

        // Go through all cells very near to the location (less than 1 meter away)
        for (final var worldCell : worldCell().worldGrid().neighbors(location()))
        {
            // Find any vertex in the neighboring cell very close to this location
            final var graph = worldCell.cellGraph();
            if (graph != null)
            {
                for (final var vertex : graph.vertexesInside(location().within(Distance.meters(1))))
                {
                    // and if this neighboring vertex is the same as this vertex
                    final var neighbor = new WorldVertex(new WorldVertexIdentifier(worldCell, vertex.identifier()));
                    if (equals(neighbor))
                    {
                        // then add it to the list of neighbor vertexes
                        neighbors.add(neighbor);
                    }
                }
            }
        }

        return neighbors;
    }

    private Count superInEdgeCount()
    {
        return super.inEdgeCount();
    }

    private WorldEdgeSequence superInEdgeSequence()
    {
        return new WorldEdgeSequence(worldCell, super.inEdgeSequence());
    }

    private WorldEdgeSet superInEdges()
    {
        return new WorldEdgeSet(GraphCoreLimits.Estimated.EDGES_PER_VERTEX, worldCell, super.inEdges());
    }

    private Count superOutEdgeCount()
    {
        return super.outEdgeCount();
    }

    private WorldEdgeSequence superOutEdgeSequence()
    {
        return new WorldEdgeSequence(worldCell, super.outEdgeSequence());
    }

    private WorldEdgeSet superOutEdges()
    {
        return new WorldEdgeSet(GraphCoreLimits.Estimated.EDGES_PER_VERTEX, worldCell, super.outEdges());
    }
}
