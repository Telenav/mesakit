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

package com.telenav.mesakit.graph.traffic.roadsection.codings.tmc;

import com.telenav.mesakit.map.geography.project.KivaKitMapGeographyUnitTest;
import org.junit.Test;

public class TmcCodeTest extends KivaKitMapGeographyUnitTest
{
    @Test
    public void testIsTmcCode()
    {
        ensure(TmcCode.isTmcCode("106+18521"));
        ensure(TmcCode.isTmcCode("105-05972"));
        ensureFalse(TmcCode.isTmcCode("xyz"));
        ensureFalse(TmcCode.isTmcCode("105-059721"));
        ensureFalse(TmcCode.isTmcCode("105X05972"));
    }
}
