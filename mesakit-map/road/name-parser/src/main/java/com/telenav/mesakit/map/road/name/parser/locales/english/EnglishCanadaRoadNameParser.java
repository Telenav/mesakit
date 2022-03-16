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

import com.telenav.kivakit.core.string.CaseFormat;
import com.telenav.mesakit.map.road.name.parser.tokenizer.Token;

public class EnglishCanadaRoadNameParser extends EnglishRoadNameParser
{
    private static final EnglishCanadaTokenizer TOKENIZER = new EnglishCanadaTokenizer();

    /**
     * <pre>
     * PARSES and STANDARDIZES: A road name, which could be:
     *
     * 1. A numeric ordinal like '4th' or '172nd'
     * 2. A named ordinal like 'twenty-first' or 'one hundred and fifth'
     * 3. Any English road name
     * </pre>
     */
    @Override
    protected void parseRoadName()
    {
        // If we can't parse it as a numeric (3rd), named ordinal (tenth), interstate (I-5),
        // canadian highway (CA-99) or provincial road (PR 201)
        if (!parseCanadianLane() && !parseNumericOrdinal() && !parseNamedOrdinal() && !parseCanadianInterstate()
                && !parseCanadianHighway())
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
                    builder.append(shouldCapitalize(previous, token) ? CaseFormat.capitalizeOnlyFirstLetter(token.text())
                            : token.text().toLowerCase());
                }
                previous = token;
            }
            builder().baseName(builder.toString().replaceAll(" - ", "-"), rawText());
        }
    }

    /**
     * PARSES: CA highway 7
     */
    private boolean parseCanadianHighway()
    {
        if (match(TOKENIZER.CANADIAN_HIGHWAY))
        {
            var number = number();
            if (number != null)
            {
                builder().baseName("CA-" + number, rawText());
                return true;
            }
        }
        reset();
        return false;
    }

    /**
     * PARSES: I-5
     */
    private boolean parseCanadianInterstate()
    {
        if (match(TOKENIZER.INTERSTATE))
        {
            var number = number();
            if (number != null)
            {
                if (hasMore())
                {
                    builder().baseName("I-" + number + " " + remainderCapitalized(), rawText());
                }
                else
                {
                    builder().baseName("I-" + number, rawText());
                }
                return true;
            }
        }
        reset();
        return false;
    }

    /**
     * PARSES: Lane South Dundas West Hamilton Street
     */
    private boolean parseCanadianLane()
    {
        // Lane <N> <Direction> <X> <Direction> <Y>
        if (match(TOKENIZER.LANE))
        {
            var name = new StringBuilder();
            while (hasMore())
            {
                if (TOKENIZER.isCardinalDirection(current()))
                {
                    name.append(direction(current()));
                }
                else
                {
                    name.append(current().text());
                }
                next();
            }
            builder().baseName("Ln" + name, rawText());
            return true;
        }

        return false;
    }

    /**
     * @return True if there should be a space between the given tokens
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
                || TOKENIZER.isSlash(previous) || TOKENIZER.isDash(token) || TOKENIZER.isDash(previous))
        {
            return false;
        }

        // Don't split up highway identifiers like Y32 or 2A
        if ((TOKENIZER.isLetter(previous) && TOKENIZER.isDigit(token))
                || (TOKENIZER.isDigit(previous) && TOKENIZER.isLetter(token)))
        {
            return false;
        }

        if (TOKENIZER.isDigit(token) || TOKENIZER.isNumericOrdinalSuffix(token))
        {
            return !TOKENIZER.isDigit(previous) && !TOKENIZER.isDash(previous) && !TOKENIZER.isInterstate(previous);
        }
        return true;
    }
}
