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

package com.telenav.mesakit.graph.specifications.common.edge;

import com.telenav.kivakit.core.io.IO;
import com.telenav.kivakit.core.path.StringPath;
import com.telenav.kivakit.resource.serialization.ObjectMetadata;
import com.telenav.kivakit.serialization.kryo.KryoObjectSerializer;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.GraphKryoTypes;
import com.telenav.mesakit.graph.GraphUnitTest;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeSpatialIndex;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import static com.telenav.mesakit.graph.metadata.DataSupplier.OSM;
import static com.telenav.mesakit.map.data.formats.library.DataFormat.PBF;

public class EdgeStoreTest extends GraphUnitTest
{
    @Test
    public void test()
    {
        var graph = OsmDataSpecification.get().newGraph(Metadata.osm(OSM, PBF));
        graph.addListener(this);
        var store = graph.edgeStore();

        final int iterations = 1_000;

        var edges = new ArrayList<Edge>();
        var adder = store.adder();
        for (var index = 1; index < iterations + 1; index++)
        {
            var edge = osmEdge(graph, index, index * 1_000_000);
            adder.add(edge);
            edges.add(edge);
        }
        store.flush();

        for (var index = 1; index < iterations + 1; index++)
        {
            var edge = graph.edgeForIdentifier(index * 1_000_000);
            ensure(edges.get(index - 1).equals(edge));
        }

        var index = new CompressedEdgeSpatialIndex("test", graph, new RTreeSettings());
        index.bulkLoad(graph.edges().asList());

        var serializer = new KryoObjectSerializer(new GraphKryoTypes());
        var output = new ByteArrayOutputStream();
        var path = StringPath.stringPath("test");
        serializer.write(output, path, index, ObjectMetadata.TYPE);
        IO.close(output);

        var input = new ByteArrayInputStream(output.toByteArray());
        var deserialized = serializer.read(input, path, ObjectMetadata.TYPE).object();

        ensureEqual(index, deserialized);
    }
}
