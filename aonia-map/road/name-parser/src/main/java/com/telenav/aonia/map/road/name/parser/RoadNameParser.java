////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.locales.english.EnglishUnitedStatesRoadNameParser;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;

import java.util.Objects;

/**
 * Road name parser.
 *
 * @author jonathanl (shibo)
 */
public interface RoadNameParser
{
    /**
     * @return RoadNameParser for the given locale
     */
    static RoadNameParser get(final MapLocale locale)
    {
        // Get the road name parser from the locale
        final RoadNameParser parser = locale.create(PackagePath.packagePath(RoadNameParser.class), "RoadNameParser");

        // and if there isn't one
        // default to US English
        return Objects.requireNonNullElseGet(parser, EnglishUnitedStatesRoadNameParser::new);

        // otherwise return the parser
    }

    /**
     * @param name The road name to parse
     * @return The parsed road name
     */
    ParsedRoadName parse(final RoadName name);
}
