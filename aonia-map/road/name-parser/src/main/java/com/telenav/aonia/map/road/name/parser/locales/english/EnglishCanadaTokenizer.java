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
