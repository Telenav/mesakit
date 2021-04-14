////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer.locales.indonesian;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.name.standardizer.RoadNameStandardizerTest;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.core.test.annotations.SlowTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jonathanl (shibo)
 */
@Category({ SlowTests.class })
public class IndonesianRoadNameStandardizerTest extends RoadNameStandardizerTest
{
    public IndonesianRoadNameStandardizerTest()
    {
        super("ID", "IND");
    }

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

    @Override
    protected MapLocale locale()
    {
        return MapLocale.INDONESIA.get();
    }

    @Override
    protected String normalize(final String string)
    {
        return string.replaceAll("[.?]", "");
    }

    protected PackageResource testCases()
    {
        return PackageResource.of(getClass(), "test-cases.csv");
    }
}
