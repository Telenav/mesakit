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


package com.telenav.tdk.graph.project;

import com.telenav.tdk.core.kernel.language.io.serialization.kryo.TdkKryoSerializer;
import com.telenav.tdk.core.kernel.project.TdkKryoIdentifiers;
import com.telenav.tdk.data.formats.library.DataFormat;
import com.telenav.tdk.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;
import com.telenav.tdk.graph.*;
import com.telenav.tdk.graph.metadata.*;
import com.telenav.tdk.graph.specifications.common.edge.store.index.*;
import com.telenav.tdk.graph.specifications.common.edge.store.stores.polyline.*;
import com.telenav.tdk.graph.specifications.common.edge.store.stores.roadname.RoadNameStore;
import com.telenav.tdk.graph.specifications.common.element.store.TagStore;
import com.telenav.tdk.graph.specifications.common.vertex.store.*;
import com.telenav.tdk.graph.specifications.osm.OsmDataSpecification;
import com.telenav.tdk.graph.specifications.unidb.UniDbDataSpecification;
import com.telenav.tdk.map.region.project.TdkMapRegion;
import com.telenav.tdk.utilities.compression.project.TdkUtilitiesCompression;

/**
 * @author jonathanl (shibo)
 */
public class TdkGraphCoreKryoSerializer extends TdkKryoSerializer
{
    public TdkGraphCoreKryoSerializer()
    {
        super(TdkKryoIdentifiers.TDK_GRAPH_CORE);

        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility, classes are assigned identifiers by TdkKryoSerializer.
        // If classes are appended to groups and no classes are removed, older data can always be read.
        //----------------------------------------------------------------------------------------------

        setReferences(true);

        tdkRegisterAllFrom(TdkMapRegion.get());
        tdkRegisterAllFrom(TdkUtilitiesCompression.get());

        tdkNextRegistrationGroup("graph", () ->
        {
            tdkRegister(Metadata.class);
            tdkRegister(DataFormat.class);
            tdkRegister(DataSpecification.class);
            tdkRegister(DataSupplier.class);
            tdkRegister(DataBuild.class);
            tdkRegister(OsmDataSpecification.class);
            tdkRegister(UniDbDataSpecification.class);
            tdkRegister(DataVersion.class);
        });

        tdkNextRegistrationGroup("entities", () ->
        {
            tdkRegister(Place.Type.class);
            tdkRegister(CompressedEdgeListStore.class);
            tdkRegister(ConnectivityStore.class);
            tdkRegister(EdgeArrayStore.class);
        });

        tdkNextRegistrationGroup("tags", () ->
        {
            tdkRegister(TagStore.class);
            tdkRegister(PbfStringListTagCodec.class);
        });

        tdkNextRegistrationGroup("spatial-index", () ->
        {
            tdkRegister(CompressedLeaf.class);
            tdkRegister(CompressedEdgeSpatialIndex.class);
        });

        tdkNextRegistrationGroup("stores", () ->
        {
            tdkRegister(RoadNameStore.class);
            tdkRegister(PolylineStore.class);
            tdkRegister(SplitPolylineStore.class);
        });
    }
}
