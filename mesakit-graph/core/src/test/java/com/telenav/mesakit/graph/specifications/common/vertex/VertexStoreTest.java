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

package com.telenav.mesakit.graph.specifications.common.vertex;

import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.project.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import org.junit.Test;

import static com.telenav.mesakit.graph.metadata.DataSupplier.OSM;
import static com.telenav.mesakit.map.data.formats.library.DataFormat.PBF;

public class VertexStoreTest extends GraphUnitTest
{
    @Test
    public void testAdd()
    {
        // Create graph
        var specification = OsmDataSpecification.get();
        var graph = specification.newGraph(Metadata.osm(OSM, PBF));
        graph.addListener(Listener.none());

        // and get vertex vertexStore
        var edgeStore = graph.edgeStore();
        var vertexStore = graph.vertexStore();

        // then create a heavyweight edge
        var edge = specification.newHeavyWeightEdge(graph, 1_000_000);

        // populate it with test values
        edge.populateWithTestValues();

        // and add it to the edge store
        edgeStore.adder().add(edge);

        // and commit changes
        edgeStore.commitSpatialIndex(false);
        graph.graphStore().commit();

        Vertex from = vertexStore.vertexForNodeIdentifier(edge.fromNodeIdentifier().asLong());
        Vertex to = vertexStore.vertexForNodeIdentifier(edge.toNodeIdentifier().asLong());

        ensureNotNull(from);
        ensureNotNull(to);
        ensureEqual(from.location(), edge.fromLocation());
        ensureEqual(to.location(), edge.toLocation());
        ensureEqual(from.identifier(), edge.fromVertexIdentifier());
        ensureEqual(to.identifier(), edge.toVertexIdentifier());
        ensureEqual(from.nodeIdentifier(), edge.fromNodeIdentifier());
        ensureEqual(to.nodeIdentifier(), edge.toNodeIdentifier());
        ensureEqual(from.index(), 1);
        ensureEqual(to.index(), 2);
        ensureEqual(from.inEdgeCount(), Count._1);
        ensureEqual(from.outEdgeCount(), Count._1);
        ensureEqual(to.inEdgeCount(), Count._1);
        ensureEqual(to.outEdgeCount(), Count._1);
        ensureEqual(from.outEdges(), EdgeSet.singleton(edge));
        ensureEqual(from.inEdges(), EdgeSet.singleton(edge.reversed()));
    }
}
