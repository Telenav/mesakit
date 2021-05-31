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

package com.telenav.mesakit.graph.specifications.osm;

import com.telenav.kivakit.kernel.data.comparison.Differences;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Graph;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.io.convert.GraphConverter;
import com.telenav.mesakit.graph.io.load.GraphLoader;
import com.telenav.mesakit.graph.specifications.common.CommonDataSpecification;
import com.telenav.mesakit.graph.specifications.common.edge.HeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.VertexStore;
import com.telenav.mesakit.graph.specifications.osm.graph.OsmGraph;
import com.telenav.mesakit.graph.specifications.osm.graph.converter.OsmPbfToGraphConverter;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmEdge;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.OsmHeavyWeightEdge;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeAttributes;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeDifferences;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeProperties;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.store.OsmEdgeStore;
import com.telenav.mesakit.graph.specifications.osm.graph.loader.OsmPbfGraphLoader;
import com.telenav.mesakit.map.data.formats.library.DataFormat;

/**
 * The specification for OpenStreetMap (OSM) data, adding OSM-specific attributes to the common attributes in {@link
 * CommonDataSpecification}. This specification is a singleton which can be retrieved with {@link #get()}.
 */
public class OsmDataSpecification extends CommonDataSpecification
{
    private static OsmDataSpecification singleton;

    public static OsmDataSpecification get()
    {
        if (singleton == null)
        {
            singleton = new OsmDataSpecification();
        }
        return singleton;
    }

    private OsmDataSpecification()
    {
        includeAttributes(OsmEdgeStore.class, edgeAttributes());
        includeAttributes(RelationStore.class, relationAttributes());
        includeAttributes(PlaceStore.class, placeAttributes());
        includeAttributes(VertexStore.class, vertexAttributes());

        showAttributes();
    }

    @Override
    public Differences compare(final Edge a, final Edge b)
    {
        return new OsmEdgeDifferences((OsmEdge) a, (OsmEdge) b).compare();
    }

    @Override
    public OsmEdgeAttributes edgeAttributes()
    {
        return OsmEdgeAttributes.get();
    }

    @Override
    public OsmEdgeProperties edgeProperties()
    {
        return OsmEdgeProperties.get();
    }

    @Override
    @SuppressWarnings({ "exports" })
    public OsmEdgeStore newEdgeStore(final Graph graph)
    {
        return new OsmEdgeStore(graph);
    }

    @Override
    public Type type()
    {
        return Type.OSM;
    }

    @Override
    protected Edge onNewEdge(final Graph graph, final long identifier, final int index)
    {
        return new OsmEdge(graph, identifier, index);
    }

    @Override
    protected Edge onNewEdge(final Graph graph, final long identifier)
    {
        return new OsmEdge(graph, identifier);
    }

    @Override
    protected Graph onNewGraph(final Metadata metadata)
    {
        return new OsmGraph(metadata);
    }

    @Override
    protected GraphConverter onNewGraphConverter(final Metadata metadata)
    {
        if (metadata.dataFormat() == DataFormat.PBF)
        {
            return new OsmPbfToGraphConverter(metadata);
        }
        return null;
    }

    @Override
    protected GraphLoader onNewGraphLoader(final Metadata metadata)
    {
        if (metadata.dataFormat() == DataFormat.PBF)
        {
            return new OsmPbfGraphLoader();
        }
        return null;
    }

    @Override
    protected HeavyWeightEdge onNewHeavyWeightEdge(final Graph graph, final long identifier)
    {
        return new OsmHeavyWeightEdge(graph, identifier);
    }
}
