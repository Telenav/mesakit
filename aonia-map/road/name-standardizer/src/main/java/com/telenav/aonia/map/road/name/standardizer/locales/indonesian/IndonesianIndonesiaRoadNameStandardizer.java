////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.standardizer.locales.indonesian;

import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.aonia.map.road.name.parser.RoadNameParser;
import com.telenav.aonia.map.road.name.standardizer.BaseRoadNameStandardizer;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;

import static com.telenav.aonia.map.road.name.parser.ParsedRoadName.TypePosition.FIRST;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

/**
 * Takes a {@link ParsedRoadName} and standardizes it. There is very little that needs to be done for Indonesia in terms
 * of special cases, so this class is mainly a pass through from the {@link RoadNameParser}, which has already done most
 * of the standardization.
 *
 * @author jonathanl (shibo)
 */
public class IndonesianIndonesiaRoadNameStandardizer extends BaseRoadNameStandardizer
{
    private static final Debug DEBUG = new Debug(LoggerFactory.newLogger());

    // Road name parser (per thread, because of thread safety issues in the parser)
    private final ThreadLocal<RoadNameParser> parser = ThreadLocal
            .withInitial(() -> RoadNameParser.get(MapLocale.INDONESIA.get()));

    private Mode mode;

    @Override
    public void mode(final Mode mode)
    {
        this.mode = mode;
    }

    @Override
    public ParsedRoadName standardize(final RoadName name)
    {
        if (mode == null)
        {
            return fail("Must set standardizer mode");
        }
        try
        {
            final var parsed = parser.get().parse(name);
            if (parsed != null)
            {
                return standardize(parsed);
            }
        }
        catch (final Exception e)
        {
            DEBUG.warning(e, "Unable to standardize '$'", name);
        }
        return null;
    }

    private ParsedRoadName standardize(final ParsedRoadName name)
    {
        final var builder = new ParsedRoadName.Builder(name);
        builder.type(name.type(), name.rawType());
        builder.baseName(standardizedBaseName(name), name.rawBaseName());
        builder.position(FIRST);
        return builder.build();
    }

    private String standardizedBaseName(final ParsedRoadName name)
    {
        return name.baseName();
    }
}
