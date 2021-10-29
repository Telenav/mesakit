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

package com.telenav.mesakit.graph.specifications.osm.graph.loader;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class OsmRawPbfGraphLoaderTest
{
    @Test
    public void testAdasZCoordinates()
    {
        assertEquals(56818L, getLongValue1("568.18"));
        assertEquals(56817L, getLongValue2("568.18"));
    }

    @SuppressWarnings("SameParameterValue")
    private long getLongValue1(String str)
    {
        return BigDecimal.valueOf(Double.parseDouble(str)).multiply(BigDecimal.valueOf(100)).longValue();
    }

    @SuppressWarnings("SameParameterValue")
    private long getLongValue2(String str)
    {
        return (long) (Double.parseDouble(str) * 100);
    }
}
