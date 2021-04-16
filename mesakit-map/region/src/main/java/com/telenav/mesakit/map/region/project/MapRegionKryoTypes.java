////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.map.region.project;

import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.BorderSpatialIndex;
import com.telenav.mesakit.map.region.border.BorderSpatialIndexKryoSerializer;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.region.regions.TimeZone;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

public class MapRegionKryoTypes extends KryoTypes
{
    public MapRegionKryoTypes()
    {
        //----------------------------------------------------------------------------------------------
        // NOTE: To maintain backward compatibility of serialization, registration groups and the types
        // in each registration group must remain in the same order.
        //----------------------------------------------------------------------------------------------

        group("borders", () ->
        {
            register(Border.class);
            register(BorderSpatialIndex.class, new BorderSpatialIndexKryoSerializer<>());
        });

        group("regions", () ->
        {
            register(RegionIdentity.class);
            register(Continent.class);
            register(Country.class);
            register(State.class);
            register(County.class);
            register(TimeZone.class);
            register(MetropolitanArea.class);
        });
    }
}
