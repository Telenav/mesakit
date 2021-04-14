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

package com.telenav.aonia.map.measurements;

import com.telenav.aonia.map.measurements.geographic.Direction;
import com.telenav.aonia.map.measurements.project.MapMeasurementsUnitTest;
import org.junit.Test;

public class DirectionTest extends MapMeasurementsUnitTest
{
    @Test
    public void testParse()
    {
        ensureEqual(Direction.parse("SW").toString(), "SW");
        ensureEqual(Direction.parse("northbound").toString(), "N");
        ensureEqual(Direction.parse("SoutheAst").toString(), "SE");
        ensureEqual(Direction.parse("SOUTHbound").toString(), "S");
    }
}
