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

package com.telenav.mesakit.map.road.name.standardizer;

import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.testing.RegionUnitTest;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.parser.ParsedRoadName;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

@Ignore
public abstract class RoadNameStandardizerTest extends RegionUnitTest
{
    private final RoadNameStandardizer standardizer;

    private List<String> lines;

    protected RoadNameStandardizerTest(String g2Country, String g2Language)
    {
        standardizer = RoadNameStandardizer.get(locale(), RoadNameStandardizer.Mode.MESAKIT_STANDARDIZATION);
    }

    protected abstract MapLocale locale();

    protected abstract String normalize(String string);

    protected Iterable<String> regressionTestCases(Resource resource)
    {
        if (lines == null)
        {
            lines = new ArrayList<>();
            for (String line : resource.reader().readLines())
            {
                if (!line.startsWith("#"))
                {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    protected ParsedRoadName standardize(String given)
    {
        return standardizer.standardize(RoadName.forName(given));
    }

    protected void test(String expected, String given)
    {
        given = normalize(given);
        var parsed = standardize(given);
        ensureEqual(expected, parsed.toString());
        ensureEqual(given, parsed.asRawRoadName().name());
        trace(given + " -> " + parsed);
    }
}
