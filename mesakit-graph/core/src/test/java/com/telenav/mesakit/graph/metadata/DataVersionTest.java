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

package com.telenav.mesakit.graph.metadata;

import com.telenav.kivakit.core.time.LocalTime;
import com.telenav.kivakit.core.test.UnitTest;
import org.junit.Test;

import java.util.stream.IntStream;

public class DataVersionTest extends UnitTest
{
    @Test
    public void testAdjustQuarterForDataVersion()
    {
        final String version = "2020Q1";
        var dataVersion = DataVersion.parse(version);

        IntStream.range(1, 4).forEachOrdered(n ->
        {
            var adjustedDataVersion = dataVersion.withQuarter(n);

            ensureEqual("2020Q" + n,
                    adjustedDataVersion.toString());
        });
    }

    @Test
    public void testAdjustYearForDataVersion()
    {
        final String version = "2020Q1";
        var dataVersion = DataVersion.parse(version);

        var adjustedDataVersion = dataVersion.withYear(2021);

        ensureEqual("2021Q1", adjustedDataVersion.toString());
    }

    @Test
    public void testConstructDataVersionFromLocalTime()
    {
        var time = LocalTime.now();
        var expectedVersion = time.year() + "Q" + time.quarter();

        var dataVersion = new DataVersion(time);

        ensureEqual(expectedVersion, dataVersion.toString());
    }

    @Test
    public void testConstructDataVersionFromString()
    {
        final String version = "2020Q1";

        var dataVersion = DataVersion.parse(version);

        ensureEqual(version, dataVersion.toString());
    }
}
