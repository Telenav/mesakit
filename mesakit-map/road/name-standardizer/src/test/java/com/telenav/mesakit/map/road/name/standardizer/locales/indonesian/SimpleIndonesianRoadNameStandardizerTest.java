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

import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.core.test.UnitTest;
import com.telenav.kivakit.core.test.annotations.SlowTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
@Category({ SlowTests.class })
public class SimpleIndonesianRoadNameStandardizerTest extends UnitTest
{
    @Test
    public void test()
    {
        final List<String> lines = new ArrayList<>();
        for (final String line : testCases().reader().linesAsStringList())
        {
            final var columns = line.split(",");
            if (columns.length == 2)
            {
                if (AsciiArt.isNaturalNumber(columns[0]))
                {
                    lines.add(line);
                }
            }
        }
        for (var i = 0; i < lines.size(); i += 2)
        {
            final var given = lines.get(i).split(",")[1];
            final var expected = normalize(lines.get(i + 1).split(",")[1]);
            test(expected, given);
        }
    }

    protected String normalize(final String string)
    {
        return string.replaceAll("[.?]", "");
    }

    protected PackageResource testCases()
    {
        return PackageResource.of(getClass(), "test-cases.csv");
    }

    private void test(final String expected, final String given)
    {
        final var standardizer = new SimpleIndonesianRoadNameStandardizer();
        ensureEqual(RoadName.forName(expected), standardizer.standardize(RoadName.forName(given)));
    }
}
