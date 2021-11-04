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

package com.telenav.mesakit.map.road.name.parser.locales.english;

import com.telenav.kivakit.kernel.language.strings.Paths;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.road.name.parser.tokenizer.Token;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.Symbol;

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
        for (var state : Country.UNITED_STATES.states())
        {
            var code = Paths.optionalSuffix(state.identity().iso().code(), '-');
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
