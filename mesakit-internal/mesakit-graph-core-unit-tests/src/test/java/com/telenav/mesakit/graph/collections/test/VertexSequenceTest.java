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

package com.telenav.mesakit.graph.collections.test;

import com.telenav.kivakit.core.collections.iteration.Iterables;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.VertexSequence;
import com.telenav.mesakit.graph.core.test.GraphUnitTest;
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
        final Edge edge1 = osmDowntownSeattleTestEdge(6428348000002L);
        final Edge edge2 = osmDowntownSeattleTestEdge(6428348000003L);
        final Edge edge3 = osmDowntownSeattleTestEdge(6428348000004L);

        // test asRoute, vertex1,2,3,4 are in sequence
        final Vertex vertex2 = edge1.vertexConnecting(edge2);
        final Vertex vertex1 = edge1.oppositeVertex(vertex2);
        final Vertex vertex3 = edge2.vertexConnecting(edge3);
        final Vertex vertex4 = edge3.oppositeVertex(vertex3);

        final List<Vertex> vertexes = new ArrayList<>();
        vertexes.add(vertex1);
        vertexes.add(vertex2);
        vertexes.add(vertex3);
        vertexes.add(vertex4);

        final VertexSequence vertexSequence1 = new VertexSequence(vertexes);
        final Route route1 = vertexSequence1.asRoute();

        ensureEqual(route1, Route.forEdges(edge1, edge2, edge3));

        final Rectangle rectangle = Rectangle.fromLocations(vertex1.location(),
                vertex2.location()).expanded(Distance.meters(1));

        // vertexSequence2 should have only first two vertexes of vertexSequence1
        final VertexSequence vertexSequence2 = vertexSequence1.inside(rectangle);
        final List<Vertex> firstTwoVertexes = new ArrayList<>();
        firstTwoVertexes.add(vertex1);
        firstTwoVertexes.add(vertex2);
        final VertexSequence vertexSequence3 = new VertexSequence(firstTwoVertexes);
        ensure(Iterables.equals(vertexSequence2, vertexSequence3));
    }
}
