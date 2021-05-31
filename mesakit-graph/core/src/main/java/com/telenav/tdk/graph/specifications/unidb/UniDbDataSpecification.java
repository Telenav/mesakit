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

package com.telenav.kivakit.graph.specifications.unidb;

import com.telenav.kivakit.kernel.comparison.Differences;
import com.telenav.kivakit.data.formats.library.DataFormat;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.io.convert.GraphConverter;
import com.telenav.kivakit.graph.io.load.GraphLoader;
import com.telenav.kivakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.kivakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.kivakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.kivakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.kivakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.kivakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.kivakit.graph.specifications.unidb.graph.UniDbGraph;
import com.telenav.kivakit.graph.specifications.unidb.graph.converter.UniDbPbfToGraphConverter;
import com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.*;
import com.telenav.kivakit.graph.specifications.unidb.graph.edge.store.UniDbEdgeStore;
import com.telenav.kivakit.graph.specifications.unidb.graph.loader.UniDbPbfGraphLoader;
import com.telenav.kivakit.graph.specifications.unidb.graph.vertex.UniDbVertex;

/**
 * Specification for UniDb data, adding attributes to the common data specification
 */
public class UniDbDataSpecification extends CommonDataSpecification
{
    private static UniDbDataSpecification singleton;

    public static UniDbDataSpecification get()
    {
        if (singleton == null)
        {
            singleton = new UniDbDataSpecification();
        }
        return singleton;
    }

    private UniDbDataSpecification()
    {
        // The UniDb specification does not include these attributes from the common base specification
        excludeAttribute(GraphElementAttributes.get().PBF_CHANGE_SET_IDENTIFIER);
        excludeAttribute(GraphElementAttributes.get().PBF_USER_IDENTIFIER);
        excludeAttribute(GraphElementAttributes.get().PBF_USER_NAME);
        excludeAttribute(GraphElementAttributes.get().PBF_REVISION_NUMBER);
        excludeAttribute(GraphElementAttributes.get().LAST_MODIFIED);

        includeAttributes(UniDbEdgeStore.class, edgeAttributes());
        includeAttributes(VertexStore.class, vertexAttributes());
        includeAttributes(RelationStore.class, relationAttributes());
        includeAttributes(PlaceStore.class, placeAttributes());

        showAttributes();
    }

    @Override
    public Differences compare(final Edge a, final Edge b)
    {
        return new UniDbEdgeDifferences((UniDbEdge) a, (UniDbEdge) b).compare();
    }

    @Override
    public UniDbEdgeAttributes edgeAttributes()
    {
        return UniDbEdgeAttributes.get();
    }

    @Override
    public UniDbEdgeProperties edgeProperties()
    {
        return UniDbEdgeProperties.get();
    }

    @Override
    public UniDbEdgeStore newEdgeStore(final Graph graph)
    {
        return new UniDbEdgeStore(graph);
    }

    @Override
    public GraphConverter newGraphConverter(final Metadata metadata)
    {
        if (metadata.dataFormat() == DataFormat.PBF)
        {
            return new UniDbPbfToGraphConverter(metadata);
        }
        return null;
    }

    @Override
    public GraphLoader newGraphLoader(final Metadata metadata)
    {
        if (metadata.dataFormat() == DataFormat.PBF)
        {
            return new UniDbPbfGraphLoader();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type type()
    {
        return Type.UniDb;
    }

    @Override
    protected Edge onNewEdge(final Graph graph, final long identifier)
    {
        return new UniDbEdge(graph, identifier);
    }

    @Override
    protected Edge onNewEdge(final Graph graph, final long identifier, final int index)
    {
        return new UniDbEdge(graph, identifier, index);
    }

    @Override
    protected Graph onNewGraph(final Metadata metadata)
    {
        return new UniDbGraph(metadata);
    }

    @Override
    protected HeavyWeightEdge onNewHeavyWeightEdge(final Graph graph, final long identifier)
    {
        return new UniDbHeavyWeightEdge(graph, identifier);
    }

    @Override
    protected Vertex onNewVertex(final Graph graph, final long identifier)
    {
        return new UniDbVertex(graph, identifier);
    }
}
