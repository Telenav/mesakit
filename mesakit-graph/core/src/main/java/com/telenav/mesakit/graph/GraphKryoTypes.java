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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.data.formats.library.DataFormat;
import com.telenav.kivakit.serialization.kryo.types.KryoTypes;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.metadata.DataBuild;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.graph.metadata.DataVersion;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeListStore;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedEdgeSpatialIndex;
import com.telenav.mesakit.graph.specifications.common.edge.store.index.CompressedLeaf;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline.PolylineStore;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.polyline.SplitPolylineStore;
import com.telenav.mesakit.graph.specifications.common.edge.store.stores.roadname.RoadNameStore;
import com.telenav.mesakit.graph.specifications.common.element.store.TagStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.ConnectivityStore;
import com.telenav.mesakit.graph.specifications.common.vertex.store.EdgeArrayStore;
import com.telenav.mesakit.graph.specifications.osm.OsmDataSpecification;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;

/**
 * @author jonathanl (shibo)
 */
public class GraphKryoTypes extends KryoTypes
{
    public GraphKryoTypes()
    {
        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility, classes are assigned identifiers by KivaKitKryoSerializer.
        // If classes are appended to groups and no classes are removed, older data can always be read.
        //----------------------------------------------------------------------------------------------

        group("graph", () ->
        {
            register(Metadata.class);
            register(DataFormat.class);
            register(DataSpecification.class);
            register(DataSupplier.class);
            register(DataBuild.class);
            register(OsmDataSpecification.class);
            register(DataVersion.class);
        });

        group("entities", () ->
        {
            register(Place.Type.class);
            register(CompressedEdgeListStore.class);
            register(ConnectivityStore.class);
            register(EdgeArrayStore.class);
        });

        group("tags", () ->
        {
            register(TagStore.class);
            register(PbfStringListTagCodec.class);
        });

        group("stores", () ->
        {
            register(RoadNameStore.class);
            register(PolylineStore.class);
            register(SplitPolylineStore.class);
        });

        group("spatial-index", () ->
        {
            register(CompressedLeaf.class);
            register(CompressedEdgeSpatialIndex.class);
        });
    }
}
