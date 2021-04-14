////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer.locales.french;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.name.standardizer.RoadNameStandardizerTest;
import com.telenav.kivakit.core.test.annotations.SlowTests;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * @author jonathanl (shibo)
 */
@Category({ SlowTests.class })
@Ignore
public class FrenchCanadaRoadNameStandardizerTest extends RoadNameStandardizerTest
{
    public FrenchCanadaRoadNameStandardizerTest()
    {
        super("Canada", "FRE");
    }

    @Override
    protected MapLocale locale()
    {
        return MapLocale.FRENCH_CANADA.get();
    }

    @Override
    protected String normalize(final String string)
    {
        return string.replaceAll("[.?]", "");
    }
}
