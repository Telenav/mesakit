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

package com.telenav.mesakit.map.road.model.converters;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.road.model.RoadName;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class RoadNameConverter extends BaseStringConverter<RoadName>
{
    private static final Pattern SYMBOLS_AND_ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public RoadNameConverter(Listener listener)
    {
        super(listener);
    }

    @Override
    protected RoadName onToValue(String value)
    {
        if ("null".equalsIgnoreCase(value))
        {
            return null;
        }
        return RoadName.forName(normalizeSymbolsAndAccents(value));
    }

    /**
     * Normalizes all non-English accented and symbol characters in a String.
     */
    private static String normalizeSymbolsAndAccents(String string)
    {
        if (Strings.isNullOrEmpty(string))
        {
            string = "";
        }
        // We remove the degree' and 'german sharp s' characters, which cause parsing problems on
        // traffic client
        string = Strings.replaceAll(string.replace('\u00B0', 'o'), "\u00DF", "ss");
        var normalized = Normalizer.normalize(string, Normalizer.Form.NFD);
        return SYMBOLS_AND_ACCENTS.matcher(normalized).replaceAll("");
    }
}
