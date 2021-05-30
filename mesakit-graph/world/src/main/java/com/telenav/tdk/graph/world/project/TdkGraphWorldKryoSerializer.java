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

package com.telenav.tdk.graph.world.project;

import com.telenav.tdk.core.kernel.language.io.serialization.kryo.TdkKryoSerializer;
import com.telenav.tdk.core.kernel.project.TdkKryoIdentifiers;
import com.telenav.tdk.graph.project.TdkGraphCore;
import com.telenav.tdk.graph.traffic.project.TdkGraphTraffic;
import com.telenav.tdk.graph.world.WorldGraphIndex;
import com.telenav.tdk.graph.world.WorldPlace;
import com.telenav.tdk.graph.world.grid.WorldCell;
import com.telenav.tdk.graph.world.grid.WorldGrid;
import com.telenav.tdk.map.utilities.grid.project.TdkMapUtilitiesGrid;

public class TdkGraphWorldKryoSerializer extends TdkKryoSerializer
{
    public TdkGraphWorldKryoSerializer()
    {
        super(TdkKryoIdentifiers.TDK_GRAPH_WORLD);

        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility, classes are assigned identifiers by TdkKryoSerializer.
        // If classes are appended to groups and no classes are removed, older data can always be read.
        //----------------------------------------------------------------------------------------------

        tdkRegisterAllFrom(TdkGraphCore.get());
        tdkRegisterAllFrom(TdkGraphTraffic.get());
        tdkRegisterAllFrom(TdkMapUtilitiesGrid.get());

        tdkNextRegistrationGroup("world-graph-index", () -> tdkRegister(WorldGraphIndex.class));

        tdkNextRegistrationGroup("world-graph", () ->
        {
            tdkRegister(WorldPlace.class);
            tdkRegister(WorldCell.class);
            tdkRegister(WorldGrid.class);
        });
    }
}
