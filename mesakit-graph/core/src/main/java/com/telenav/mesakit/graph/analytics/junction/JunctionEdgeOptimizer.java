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

package com.telenav.mesakit.graph.analytics.junction;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.map.measurements.geographic.Distance;

/**
 * This {@link JunctionEdgeOptimizer} class adds more junction edges to junction edge store by trying to find out
 * triangle cases, means if two edges of a triangle are junction edges, the third edge should be junction edge too
 *
 * @author tuom
 * @author jonathanl (shibo)
 */
public class JunctionEdgeOptimizer
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final EdgeSet junctions;

    public JunctionEdgeOptimizer(EdgeSet junctions)
    {
        this.junctions = junctions;
    }

    public EdgeSet optimize()
    {
        // handle triangle junctions
        optimizeTriangularJunctionEdges();

        // handle rectangle junctions
        optimizeRectangularJunctionEdges();

        return junctions;
    }

    /**
     * Returns another vertex of edge other than the shared vertex of edge and junctionEdge
     */
    private Vertex anotherVertex(Edge edge, Edge junctionEdge)
    {
        var shared = edge.vertexConnecting(junctionEdge);
        if (shared != null)
        {
            return edge.oppositeVertex(shared);
        }
        else
        {
            LOGGER.warning("Can't get another vertex for edge ${debug} and edge ${debug}", edge, junctionEdge);
            return null;
        }
    }

    private boolean hasSameName(Edge edge1, Edge edge2)
    {
        var name1 = edge1.roadName();
        var name2 = edge2.roadName();

        return name1 != null && name1.equals(name2);
    }

    /**
     * Returns ture if the difference of length is less or equals to 15 meters
     */
    private boolean isLengthValid(Edge edge, Edge junctionEdge)
    {
        return edge.length().difference(junctionEdge.length()).isLessThanOrEqualTo(Distance.meters(10));
    }

    /**
     * Try to find out rectangle cases, if two connected edges of a junction edge are both less than 60 meters, and they
     * are connected to another junction edge
     */
    private void optimizeRectangularJunctionEdges()
    {
        var junctionEdges = new EdgeSet();

        // Loop through every junction edge
        for (var junctionEdge : junctions)
        {
            // Validate if there are two connected edges are both less than 60 meters and both
            // connecting to another junction edge
            for (var edge1 : junctionEdge.fromEdgesWithoutThisEdge())
            {
                // connected edges should not have same road name as the junctionEdge
                if (hasSameName(edge1, junctionEdge))
                {
                    continue;
                }

                var found = false;
                for (var edge2 : junctionEdge.toEdgesWithoutThisEdge())
                {
                    // connected edges should not have same road name as the junctionEdge
                    if (hasSameName(edge2, junctionEdge))
                    {
                        continue;
                    }

                    if (!edge1.equals(edge2) && !junctions.contains(edge1)
                            && !junctions.contains(edge2) && isLengthValid(edge1, junctionEdge)
                            && isLengthValid(edge2, junctionEdge))
                    {
                        var vertex1 = anotherVertex(edge1, junctionEdge);
                        var vertex2 = anotherVertex(edge2, junctionEdge);
                        if (vertex1 != null && vertex2 != null)
                        {
                            var anotherEdge = vertex1.edgeBetween(vertex2);
                            if (anotherEdge != null && junctions.contains(anotherEdge)
                                    && sameAttribute(anotherEdge, junctionEdge))
                            {
                                junctionEdges.add(edge1);
                                junctionEdges.add(edge2);
                                found = true;
                                break;
                            }
                        }
                    }
                }
                // If already found the rectangle, break the following check for current junction
                // edge
                if (found)
                {
                    break;
                }
            }
        }
        junctions.addAll(junctionEdges);
    }

    /**
     * Try to find out triangle cases, if two edges of a triangle are junction edges, the third edge should be junction
     * edge too
     */
    private void optimizeTriangularJunctionEdges()
    {
        var junctionEdges = new EdgeSet();

        // if edge1 is junction edge
        for (var edge1 : junctions)
        {
            for (var edge2 : edge1.connectedEdgesWithoutReversed())
            {
                // and edge2 is junction edge
                if (junctions.contains(edge2))
                {
                    var shared = edge1.vertexConnecting(edge2);
                    // edge1, edge2 and triangleEdge form a triangle
                    var triangleEdge = edge1.oppositeVertex(shared).edgeBetween(edge2.oppositeVertex(shared));
                    if (triangleEdge != null && triangleEdge.length().isLessThanOrEqualTo(Distance.meters(60)))
                    {
                        junctionEdges.add(triangleEdge);
                    }
                }
            }
        }
        junctions.addAll(junctionEdges);
    }

    private boolean sameAttribute(Edge edge1, Edge edge2)
    {
        return edge1.roadFunctionalClass().equals(edge2.roadFunctionalClass())
                && edge1.roadType().equals(edge2.roadType());
    }
}
