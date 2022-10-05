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

import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.parser.ParsedRoadName;
import com.telenav.mesakit.map.road.name.parser.RoadNameParser;
import com.telenav.mesakit.map.road.name.standardizer.BaseRoadNameStandardizer;

import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.mesakit.map.road.name.parser.ParsedRoadName.TypePosition.FIRST;

/**
 * Takes a {@link ParsedRoadName} and standardizes it. There is very little that needs to be done for Indonesia in terms
 * of special cases, so this class is mainly a pass through from the {@link RoadNameParser}, which has already done most
 * of the standardization.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("DuplicatedCode") public class IndonesianIndonesiaRoadNameStandardizer extends BaseRoadNameStandardizer
{
    private static final Debug DEBUG = new Debug(LoggerFactory.newLogger());

    // Road name parser (per thread, because of thread safety issues in the parser)
    private final ThreadLocal<RoadNameParser> parser = ThreadLocal
            .withInitial(() -> RoadNameParser.get(MapLocale.INDONESIA_INDONESIAN.get()));

    private Mode mode;

    @Override
    public void mode(Mode mode)
    {
        this.mode = mode;
    }

    @Override
    public ParsedRoadName standardize(RoadName name)
    {
        if (mode == null)
        {
            return fail("Must set standardizer mode");
        }
        try
        {
            var parsed = parser.get().parse(name);
            if (parsed != null)
            {
                return standardize(parsed);
            }
        }
        catch (Exception e)
        {
            DEBUG.warning(e, "Unable to standardize '$'", name);
        }
        return null;
    }

    private ParsedRoadName standardize(ParsedRoadName name)
    {
        var builder = new ParsedRoadName.Builder(name);
        builder.type(name.type(), name.rawType());
        builder.baseName(standardizedBaseName(name), name.rawBaseName());
        builder.position(FIRST);
        return builder.build();
    }

    private String standardizedBaseName(ParsedRoadName name)
    {
        return name.baseName();
    }
}
