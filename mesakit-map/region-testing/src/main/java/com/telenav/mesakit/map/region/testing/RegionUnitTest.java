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

package com.telenav.mesakit.map.region.testing;

import com.telenav.kivakit.serialization.kryo.types.KivaKitCoreKryoTypes;
import com.telenav.kivakit.serialization.kryo.types.KryoTypes;
import com.telenav.mesakit.map.geography.GeographyKryoTypes;
import com.telenav.mesakit.map.geography.testing.GeographyUnitTest;
import com.telenav.mesakit.map.measurements.MeasurementsKryoTypes;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionKryoTypes;
import com.telenav.mesakit.map.region.RegionProject;

public class RegionUnitTest extends GeographyUnitTest
{
    public RegionUnitTest()
    {
        initializeProject(RegionProject.class);
    }

    protected RegionCode code(String string)
    {
        var code = RegionCode.parse(string);
        if (code != null)
        {
            return code;
        }
        throw new IllegalStateException();
    }

    @Override
    protected KryoTypes kryoTypes()
    {
        return new KivaKitCoreKryoTypes()
                .mergedWith(new MeasurementsKryoTypes())
                .mergedWith(new GeographyKryoTypes())
                .mergedWith(new RegionKryoTypes());
    }
}
