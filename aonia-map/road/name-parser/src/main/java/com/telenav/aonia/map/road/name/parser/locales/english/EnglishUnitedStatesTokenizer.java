////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.region.regions.Country;
import com.telenav.aonia.map.road.name.parser.tokenizer.Token;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.Symbol;
import com.telenav.kivakit.core.kernel.language.strings.PathStrings;

public class EnglishUnitedStatesTokenizer extends EnglishTokenizer
{
    public final Token US_HIGHWAY = create("UsHighway").addTo(roadTypes());

    public final Token STATE_ROUTE = create("StateRoute").addTo(roadTypes());

    {
        // U. S. - 405, U S 90, U S - 109
        US_HIGHWAY.matches("U", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            if (stream.matches("S"))
            {
                stream.skipAnyWhiteSpace();
                stream.skipAny("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY", "ROUTE", "RTE", "RT", "RN");
                stream.skipAnyWhiteSpace();
                stream.skipAny(Symbol.DASH);
                stream.skipAnyWhiteSpace();
                if (lookingAtHighwayDesignator(stream))
                {
                    return US_HIGHWAY.of(stream);
                }
            }
            return null;
        });

        // UNITED STATES HIGHWAY 9, UNITED STATES RTE 101
        US_HIGHWAY.matches("UNITED", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            if (stream.matches("STATES"))
            {
                stream.skipAnyWhiteSpace();
                stream.skipAny("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY", "ROUTE", "RTE", "RT", "RN");
                stream.skipAnyWhiteSpace();
                stream.skipAny(Symbol.DASH);
                stream.skipAnyWhiteSpace();
                if (lookingAtHighwayDesignator(stream))
                {
                    return US_HIGHWAY.of(stream);
                }
            }
            return null;
        });

        // US-99, US - 5, US 5, US Highway 9, US HWY 9, US ROUTE 30
        US_HIGHWAY.matches("US", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY", "ROUTE", "RTE", "RT", "RN");
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.DASH);
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return US_HIGHWAY.of(stream);
            }
            return null;
        });
    }

    {
        // SR-99, SR 1
        US_HIGHWAY.matches("SR", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.DASH);
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return STATE_ROUTE.of(stream);
            }
            return null;
        });

        // WA-520, GA-99, etc.
        for (final var state : Country.UNITED_STATES.states())
        {
            final var code = PathStrings.optionalSuffix(state.identity().iso().code(), '-');
            US_HIGHWAY.matches(code, (stream) ->
            {
                stream.next();
                stream.skipAnyWhiteSpace();
                stream.skipAny(Symbol.DASH);
                stream.skipAnyWhiteSpace();
                if (lookingAtHighwayDesignator(stream))
                {
                    return STATE_ROUTE.of(stream);
                }
                return null;
            });
        }
    }
}
