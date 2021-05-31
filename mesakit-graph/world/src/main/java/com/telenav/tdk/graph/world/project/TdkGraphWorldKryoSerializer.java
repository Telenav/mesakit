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

package com.telenav.kivakit.graph.world.project;

import com.telenav.kivakit.kernel.language.io.serialization.kryo.KivaKitKryoSerializer;
import com.telenav.kivakit.kernel.project.KivaKitKryoIdentifiers;
import com.telenav.kivakit.graph.project.KivaKitGraphCore;
import com.telenav.kivakit.graph.traffic.project.KivaKitGraphTraffic;
import com.telenav.kivakit.graph.world.WorldGraphIndex;
import com.telenav.kivakit.graph.world.WorldPlace;
import com.telenav.kivakit.graph.world.grid.WorldCell;
import com.telenav.kivakit.graph.world.grid.WorldGrid;
import com.telenav.kivakit.map.utilities.grid.project.KivaKitMapUtilitiesGrid;

public class KivaKitGraphWorldKryoSerializer extends KivaKitKryoSerializer
{
    public KivaKitGraphWorldKryoSerializer()
    {
        super(KivaKitKryoIdentifiers.TDK_GRAPH_WORLD);

        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility, classes are assigned identifiers by KivaKitKryoSerializer.
        // If classes are appended to groups and no classes are removed, older data can always be read.
        //----------------------------------------------------------------------------------------------

        tdkRegisterAllFrom(KivaKitGraphCore.get());
        tdkRegisterAllFrom(KivaKitGraphTraffic.get());
        tdkRegisterAllFrom(KivaKitMapUtilitiesGrid.get());

        tdkNextRegistrationGroup("world-graph-index", () -> tdkRegister(WorldGraphIndex.class));

        tdkNextRegistrationGroup("world-graph", () ->
        {
            tdkRegister(WorldPlace.class);
            tdkRegister(WorldCell.class);
            tdkRegister(WorldGrid.class);
        });
    }
}
