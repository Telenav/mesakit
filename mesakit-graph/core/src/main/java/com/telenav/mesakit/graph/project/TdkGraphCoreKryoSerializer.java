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

package com.telenav.mesakit.graph.project;

import com.telenav.kivakit.data.formats.library.DataFormat;
import com.telenav.kivakit.serialization.kryo.KryoTypes;
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
import com.telenav.mesakit.graph.specifications.unidb.UniDbDataSpecification;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.compression.PbfStringListTagCodec;

/**
 * @author jonathanl (shibo)
 */
public class KivaKitGraphCoreKryoSerializer extends KryoTypes
{
    public KivaKitGraphCoreKryoSerializer()
    {
        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility, classes are assigned identifiers by KivaKitKryoSerializer.
        // If classes are appended to groups and no classes are removed, older data can always be read.
        //----------------------------------------------------------------------------------------------

        setReferences(true);

        tdkRegisterAllFrom(KivaKitMapRegion.get());
        tdkRegisterAllFrom(KivaKitUtilitiesCompression.get());

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
