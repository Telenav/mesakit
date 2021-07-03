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

import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.project.GraphCoreUnitTest;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import org.junit.Test;

import java.util.ArrayList;

import static com.telenav.mesakit.graph.metadata.DataSupplier.OSM;
import static com.telenav.mesakit.map.data.formats.library.DataFormat.PBF;

public class EdgeStoreTest extends GraphCoreUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void test()
    {
        final var graph = OsmDataSpecification.get().newGraph(Metadata.osm(OSM, PBF));
        graph.addListener(LOGGER);
        final var store = graph.edgeStore();

        final int iterations = 1_000;

        final var edges = new ArrayList<Edge>();
        final var adder = store.adder();
        for (var index = 1; index < iterations + 1; index++)
        {
            final Edge edge = osmEdge(graph, index, index * 1_000_000);
            adder.add(edge);
            edges.add(edge);
        }
        store.flush();

        for (var index = 1; index < iterations + 1; index++)
        {
            final var edge = graph.edgeForIdentifier(index * 1_000_000);
            ensure(edges.get(index - 1).equals(edge));
        }
    }
}