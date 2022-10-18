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

package com.telenav.mesakit.graph.tests;

import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.core.testing.GraphUnitTest;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tony
 * @author jonathanl (shibo)
 */
public class VertexSequenceTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        Edge edge1 = osmDowntownSeattleTestEdge(6428348000002L);
        Edge edge2 = osmDowntownSeattleTestEdge(6428348000003L);
        Edge edge3 = osmDowntownSeattleTestEdge(6428348000004L);

        // test asRoute, vertex1,2,3,4 are in sequence
        Vertex vertex2 = edge1.vertexConnecting(edge2);
        Vertex vertex1 = edge1.oppositeVertex(vertex2);
        Vertex vertex3 = edge2.vertexConnecting(edge3);
        Vertex vertex4 = edge3.oppositeVertex(vertex3);

        List<Vertex> vertexes = new ArrayList<>();
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        VertexSequence vertexSequence1 = new VertexSequence(vertexes);
        Route route1 = vertexSequence1.asRoute();

        ensureEqual(route1, Route.forEdges(edge1, edge2, edge3));

        Rectangle rectangle = Rectangle.fromLocations(vertex1.location(),
                vertex2.location()).expanded(Distance.meters(1));

        // vertexSequence2 should have only first two vertexes of vertexSequence1
        VertexSequence vertexSequence2 = vertexSequence1.inside(rectangle);
        List<Vertex> firstTwoVertexes = new ArrayList<>();
        firstTwoVertexes.add(vertex1);
        firstTwoVertexes.add(vertex2);
        VertexSequence vertexSequence3 = new VertexSequence(firstTwoVertexes);
        ensure(Iterables.equalIterables(vertexSequence2, vertexSequence3));
    }
}
