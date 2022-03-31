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

import com.telenav.kivakit.core.string.Paths;
import com.telenav.kivakit.core.time.LocalTime;
import com.telenav.kivakit.core.time.TimeZones;
import com.telenav.kivakit.test.UnitTest;
import org.junit.Test;

public class DataBuildTest extends UnitTest
{
    @Test
    public void testConstructDataBuildFromTimeString()
    {
        final String given = "2020.08.06_09.59AM_PT";
        final String expected = "2020.08.06_09.59AM_PT";

        var build = DataBuild.parse(given);

        ensureEqual(expected, build.toString());
    }

    @Test
    public void testConstructDataBuildObjectFromLocalTime()
    {
        var time = LocalTime.now();
        var build = DataBuild.parse(time.toString());

        ensureEqual(Paths.withoutSuffix(time.toString(), '_'), Paths.withoutSuffix(build.toString(), '_'));
        ensureEqual(TimeZones.shortDisplayName(time.timeZone()), Paths.optionalSuffix(build.toString(), '_'));
    }

    @Test
    public void testLocalTime()
    {
        var time = DataBuild.parse("2015.09.23_4.01PM_PT");
        ensureEqual(time.localTime().toString(), "2015.09.23_04.01PM_PT");
        ensureEqual(time.utcTime().toString(), "2015.09.23_11.01PM_UTC");
    }
}
