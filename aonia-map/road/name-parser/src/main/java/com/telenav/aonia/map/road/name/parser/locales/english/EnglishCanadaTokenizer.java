////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.road.name.parser.tokenizer.Token;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.Symbol;

public class EnglishCanadaTokenizer extends EnglishTokenizer
{
    public final Token CANADIAN_HIGHWAY = create("CanadianHighway").addTo(roadTypes());

    {
        // CA 90
        CANADIAN_HIGHWAY.matches("CA", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY", "ROUTE", "RTE", "RT", "RN");
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.DASH);
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return CANADIAN_HIGHWAY.of(stream);
            }
            return null;
        });

        // CANADA 401, CANADA HIGHWAY 99
        CANADIAN_HIGHWAY.matches("CANADA", (stream) ->
        {
            stream.next();
            stream.skipAnyWhiteSpace();
            stream.skipAny("HIGHWAY", "HIGHWY", "HIWAY", "HIWY", "HWAY", "HWY", "ROUTE", "RTE", "RT", "RN");
            stream.skipAnyWhiteSpace();
            stream.skipAny(Symbol.DASH);
            stream.skipAnyWhiteSpace();
            if (lookingAtHighwayDesignator(stream))
            {
                return CANADIAN_HIGHWAY.of(stream);
            }
            return null;
        });
    }
}
