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

package com.telenav.mesakit.graph.tests;

import com.telenav.kivakit.testing.UnitTest;
import com.telenav.mesakit.graph.metadata.DataSupplier;
import com.telenav.mesakit.map.geography.Precision;
import org.junit.Test;

public class DataSupplierTest extends UnitTest
{
    @Test
    public void testConstructDataSupplierMatchesAll()
    {
        var dataSupplier = DataSupplier.ALL;

        ensureFalse(dataSupplier.isHere());
        ensureFalse(dataSupplier.isOsm());
        ensureFalse(dataSupplier.isTomTom());
        ensureEqual(dataSupplier.precision(), Precision.NONE);
        ensure(dataSupplier.matches(DataSupplier.ALL));
    }

    @Test
    public void testConstructOSMDataSupplier()
    {
        var dataSupplier = DataSupplier.OSM;

        ensure(dataSupplier.isOsm());
        ensureEqual(dataSupplier.precision(), Precision.DM6);
        ensureFalse(dataSupplier.matches(DataSupplier.TomTom));
    }
}
