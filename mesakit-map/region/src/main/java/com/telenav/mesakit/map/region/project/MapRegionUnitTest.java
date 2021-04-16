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

import com.telenav.mesakit.map.geography.project.MapGeographyKryoTypes;
import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import com.telenav.mesakit.map.measurements.project.MapMeasurementsKryoTypes;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.kivakit.core.serialization.kryo.CoreKernelKryoTypes;
import com.telenav.kivakit.core.serialization.kryo.KryoTypes;

public class MapRegionUnitTest extends MapGeographyUnitTest
{
    public MapRegionUnitTest()
    {
        MapRegionProject.get().initialize();
    }

    protected RegionCode code(final String string)
    {
        final var code = RegionCode.parse(string);
        if (code != null)
        {
            return code;
        }
        throw new IllegalStateException();
    }

    @Override
    protected KryoTypes kryoTypes()
    {
        return new CoreKernelKryoTypes()
                .mergedWith(new MapMeasurementsKryoTypes())
                .mergedWith(new MapGeographyKryoTypes())
                .mergedWith(new MapRegionKryoTypes());
    }
}
