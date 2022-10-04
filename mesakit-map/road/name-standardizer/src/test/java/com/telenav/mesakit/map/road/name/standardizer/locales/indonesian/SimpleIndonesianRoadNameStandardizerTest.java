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

package com.telenav.mesakit.map.road.name.standardizer.locales.indonesian;

import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.resource.packages.PackageResource;
import com.telenav.kivakit.testing.SlowTest;
import com.telenav.kivakit.testing.UnitTest;
import com.telenav.mesakit.map.road.model.RoadName;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
@Category({ SlowTest.class })
public class SimpleIndonesianRoadNameStandardizerTest extends UnitTest
{
    @Test
    public void test()
    {
        List<String> lines = new ArrayList<>();
        for (String line : testCases().reader().readLines())
        {
            var columns = line.split(",");
            if (columns.length == 2)
            {
                if (Strings.isNaturalNumber(columns[0]))
                {
                    lines.add(line);
                }
            }
        }
        for (var i = 0; i < lines.size(); i += 2)
        {
            var given = lines.get(i).split(",")[1];
            var expected = normalize(lines.get(i + 1).split(",")[1]);
            test(expected, given);
        }
    }

    protected String normalize(String string)
    {
        return string.replaceAll("[.?]", "");
    }

    protected PackageResource testCases()
    {
        return PackageResource.packageResource(this, getClass(), "test-cases.csv");
    }

    private void test(String expected, String given)
    {
        var standardizer = new SimpleIndonesianRoadNameStandardizer();
        ensureEqual(RoadName.forName(expected), standardizer.standardize(RoadName.forName(given)));
    }
}
