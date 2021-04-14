////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.region.project.MapRegionUnitTest;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.kivakit.core.resource.Resource;
import org.junit.Ignore;

import java.util.ArrayList;
import java.util.List;

@Ignore
public abstract class RoadNameStandardizerTest extends MapRegionUnitTest
{
    private final RoadNameStandardizer standardizer;

    private List<String> lines;

    protected RoadNameStandardizerTest(final String g2Country, final String g2Language)
    {
        standardizer = RoadNameStandardizer.get(locale(), RoadNameStandardizer.Mode.AONIA_STANDARDIZATION);
    }

    protected abstract MapLocale locale();

    protected abstract String normalize(final String string);

    protected Iterable<String> regressionTestCases(final Resource resource)
    {
        if (lines == null)
        {
            lines = new ArrayList<>();
            for (final String line : resource.reader().linesAsStringList())
            {
                if (!line.startsWith("#"))
                {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    protected ParsedRoadName standardize(final String given)
    {
        return standardizer.standardize(RoadName.forName(given));
    }

    protected void test(final String expected, String given)
    {
        given = normalize(given);
        final var parsed = standardize(given);
        ensureEqual(expected, parsed.toString());
        ensureEqual(given, parsed.asRawRoadName().name());
        trace(given + " -> " + parsed);
    }
}
