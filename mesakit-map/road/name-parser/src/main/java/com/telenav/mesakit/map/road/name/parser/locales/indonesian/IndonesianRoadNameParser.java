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

package com.telenav.mesakit.map.road.name.parser.locales.indonesian;

import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.name.parser.BaseRoadNameParser;
import com.telenav.mesakit.map.road.name.parser.ParsedRoadName;
import com.telenav.mesakit.map.road.name.parser.tokenizer.Token;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.mesakit.map.road.name.parser.ParsedRoadName.DirectionFormat.NONE;
import static com.telenav.mesakit.map.road.name.parser.ParsedRoadName.DirectionFormat.PREFIXED;
import static com.telenav.mesakit.map.road.name.parser.ParsedRoadName.DirectionFormat.SUFFIXED;

/**
 * Parses Indonesian {@link RoadName}s into {@link ParsedRoadName}s, which are already mostly standardized. The road
 * name standardizer then finished the job by making minor adjustments or corrections to the parsed road name.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("DuplicatedCode") public class IndonesianRoadNameParser extends BaseRoadNameParser
{
    // Tokenizer for Indonesia
    private static final IndonesianTokenizer TOKENIZER = new IndonesianTokenizer();

    public enum Capitalization
    {
        LOWERCASE,
        UPPERCASE,
        CAPITALIZED
    }

    private final Map<Token, String> direction = new HashMap<>();

    private final Map<Token, String> roadType = new HashMap<>();

    private final Map<Token, String> properName = new HashMap<>();

    // Builder for creating parse information
    private ParsedRoadName.Builder builder;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ParsedRoadName parse(RoadName name)
    {
        // Get input text
        var input = name.name();
        if (input.indexOf(';') >= 0)
        {
            throw new IllegalStateException(
                    "Cannot parse multiple names separated by semicolons (please break into separate names and call multiple times)");
        }

        // Initialize
        builder = new ParsedRoadName.Builder();

        // Tokenize the input name
        tokenize(TOKENIZER, input);

        // If we have only two tokens separated by whitespace
        if (size() == 3 && token(1).isWhitespace())
        {
            // remove road type and then direction
            parseAndRemoveRoadType();
            parseAndRemoveDirection();
        }
        else
        {
            // Parse and remove direction from one end or the other
            parseAndRemoveDirection();

            // Parse and remove any road type from the end
            parseAndRemoveRoadType();
        }

        // Whatever is left is the road name
        parseRoadName();

        return builder.build();
    }

    private Capitalization capitalization(Token previous, Token token)
    {
        if (previous == null)
        {
            return Capitalization.CAPITALIZED;
        }
        if (TOKENIZER.isDigit(previous))
        {
            return Capitalization.LOWERCASE;
        }
        if (TOKENIZER.isRomanNumeral(token))
        {
            return Capitalization.UPPERCASE;
        }
        return Capitalization.CAPITALIZED;
    }

    private String direction(Token token)
    {
        return direction.get(token);
    }

    /**
     * PARSES: How are directions used in Indonesia?
     */
    private void parseAndRemoveDirection()
    {
        if (size() > 1)
        {
            // If the last token is a direction,
            var prefix = false;
            var suffix = false;

            if (TOKENIZER.isQuadrant(last()))
            {
                suffix = true;
            }
            else if (TOKENIZER.isQuadrant(first()))
            {
                prefix = true;
            }
            else if (TOKENIZER.isCardinalDirection(last()))
            {
                suffix = true;
            }
            else if (TOKENIZER.isCardinalDirection(first()))
            {
                prefix = true;
            }

            if (suffix)
            {
                builder.directionFormat(SUFFIXED);
                var removed = removeLast();
                builder.direction(direction(removed), removed.text());
            }
            else if (prefix)
            {
                builder.directionFormat(PREFIXED);
                var removed = removeFirst();
                builder.direction(direction(removed), removed.text());
            }
            else
            {
                builder.directionFormat(NONE);
                builder.direction(null, null);
            }
        }
        else
        {
            builder.directionFormat(NONE);
            builder.direction(null, null);
        }
    }

    /**
     * PARSES and STANDARDIZES: Gang, Jalan, etc
     */
    private void parseAndRemoveRoadType()
    {
        if (size() >= 2)
        {
            var first = first();

            // If the last token is a road type
            if (TOKENIZER.isRoadType(first))
            {
                // remove the token and add to the builder
                var removed = removeFirst();
                builder.type(roadType.get(removed), removed.text());
            }
        }
    }

    /**
     * <pre>
     * PARSES and STANDARDIZES: A road name, which could be:
     *
     * 1. A numeric ordinal like '4th' or '172nd'
     * 2. A named ordinal like 'twenty-first' or 'one hundred and fifth'
     * 3. Any English road name
     * </pre>
     */
    private void parseRoadName()
    {
        // then it's a normal street name so just accept it as it is
        var builder = new StringBuilder();
        Token previous = null;
        for (var token : tokens())
        {
            if (!token.isWhitespace())
            {
                if (builder.length() > 0 && space(previous, token))
                {
                    builder.append(" ");
                }
                if (TOKENIZER.isProperName(token))
                {
                    builder.append(properName.get(token));
                }
                else
                {
                    switch (capitalization(previous, token))
                    {
                        case LOWERCASE -> builder.append(token.text().toLowerCase());
                        case UPPERCASE -> builder.append(token.text().toUpperCase());
                        default -> builder.append(CaseFormat.capitalizeOnlyFirstLetter(token.text()));
                    }
                }
                previous = token;
            }
        }
        this.builder.baseName(builder.toString().replaceAll(" - ", "-"), rawText());
    }

    /**
     * Returns true if there should be a space between the given tokens
     */
    private boolean space(Token previous, Token token)
    {
        if (TOKENIZER.isOpenParenthesis(token) || TOKENIZER.isCloseParenthesis(previous))
        {
            return true;
        }

        if (TOKENIZER.isOpenParenthesis(previous) || TOKENIZER.isCloseParenthesis(token))
        {
            return false;
        }

        if (TOKENIZER.isSemicolon(token) || TOKENIZER.isPoundSign(previous) || TOKENIZER.isSlash(token)
                || TOKENIZER.isSlash(previous))
        {
            return false;
        }

        if (TOKENIZER.isDigit(token))
        {
            return !TOKENIZER.isDigit(previous) && !TOKENIZER.isDash(previous);
        }
        return true;
    }

    {
        direction.put(TOKENIZER.NORTH, "U");
        direction.put(TOKENIZER.SOUTH, "S");
        direction.put(TOKENIZER.EAST, "T");
        direction.put(TOKENIZER.WEST, "B");
        direction.put(TOKENIZER.NORTHEAST, "TL");
        direction.put(TOKENIZER.NORTHWEST, "BL");
        direction.put(TOKENIZER.SOUTHEAST, "TG");
        direction.put(TOKENIZER.SOUTHWEST, "BD");
        direction.put(TOKENIZER.NORTH_NORTHEAST, "UTL");
        direction.put(TOKENIZER.NORTH_NORTHWEST, "UBL");
        direction.put(TOKENIZER.SOUTH_SOUTHEAST, "SM");
        direction.put(TOKENIZER.SOUTH_SOUTHWEST, "SBD");
        direction.put(TOKENIZER.EAST_NORTHEAST, "TTL");
        direction.put(TOKENIZER.EAST_SOUTHEAST, "TM");
        direction.put(TOKENIZER.WEST_SOUTHWEST, "BBD");
        direction.put(TOKENIZER.WEST_NORTHWEST, "BBL");
    }

    {
        roadType.put(TOKENIZER.JALAN_STREET, "Jl");
        roadType.put(TOKENIZER.GANG_ALLEY, "Gg");
    }

    {
        properName.put(TOKENIZER.ABDUL_NAME, "Abd");
        properName.put(TOKENIZER.AWANG_HONORIFIC, "Awg");
        properName.put(TOKENIZER.DAYANG_HONORIFIC, "Dyg");
        properName.put(TOKENIZER.DEPARTEMEN_DEPARTMENT, "Dep");
        properName.put(TOKENIZER.DOKTER_DOCTOR, "Dr");
        properName.put(TOKENIZER.GUNUNG_MOUNTAIN, "Gn");
        properName.put(TOKENIZER.HAJI_PILGRIM, "Hj");
        properName.put(TOKENIZER.INSINYUR_ENGINEER, "Ir");
        properName.put(TOKENIZER.JENDERAL_GENERAL, "Jend");
        properName.put(TOKENIZER.KAMPUNG_TOWN, "Kp");
        properName.put(TOKENIZER.KAROMAH_NAME, "KH");
        properName.put(TOKENIZER.KAVLING_PLOT, "Kav");
        properName.put(TOKENIZER.KOLONEL_COLONEL, "Kol");
        properName.put(TOKENIZER.KOMPLEK_COMPLEX, "Komp");
        properName.put(TOKENIZER.MOHAMMAD_NAME, "Moh");
        properName.put(TOKENIZER.MUHAMMAD_NAME, "Muh");
        properName.put(TOKENIZER.PASAR_MARKET, "Ps");
        properName.put(TOKENIZER.PENGIRAN_HONORIFIC, "Pg");
        properName.put(TOKENIZER.PERGUDANGAN_WAREHOUSING, "Perg");
        properName.put(TOKENIZER.PROFESOR_PROFESSOR, "Prof");
        properName.put(TOKENIZER.SIMPANG_INTERSECTION, "Spg");
        properName.put(TOKENIZER.SUNGAI_RIVER, "Sg");
        properName.put(TOKENIZER.TANJUNG_CAPE, "Tj");
    }
}
