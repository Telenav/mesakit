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
