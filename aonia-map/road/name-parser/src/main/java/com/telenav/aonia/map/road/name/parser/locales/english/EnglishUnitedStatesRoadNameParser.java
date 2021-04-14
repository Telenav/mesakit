////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.road.name.parser.tokenizer.Token;
import com.telenav.kivakit.core.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.core.kernel.language.strings.Strip;

public class EnglishUnitedStatesRoadNameParser extends EnglishRoadNameParser
{
    private static final EnglishUnitedStatesTokenizer TOKENIZER = new EnglishUnitedStatesTokenizer();

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
        // If we can't parse it as a numeric (3rd), named ordinal (tenth), interstate (I-5) or us
        // highway (US 7)
        if (!parseNumericOrdinal() && !parseNamedOrdinal() && !parseUsInterstate() && !parseUsHighway()
                && !parseUsStateRoute())
        {
            // then it's a normal street name so just accept it as it is
            final var builder = new StringBuilder();
            Token previous = null;
            for (final var token : tokens())
            {
                if (!token.isWhitespace())
                {
                    if (builder.length() > 0 && space(previous, token))
                    {
                        builder.append(" ");
                    }
                    builder.append(shouldCapitalize(previous, token) ? CaseFormat.capitalizeOnlyFirstLetter(token.text())
                            : token.text().toLowerCase());
                    previous = token;
                }
            }
            builder().baseName(builder.toString().replaceAll(" - ", "-"), rawText());
        }
    }

    /**
     * PARSES: us highway 7
     */
    private boolean parseUsHighway()
    {
        if (match(TOKENIZER.US_HIGHWAY))
        {
            final var number = number();
            if (number != null)
            {
                builder().baseName("US-" + number, rawText());
                return true;
            }
        }
        reset();
        return false;
    }

    /**
     * PARSES: I-5
     */
    private boolean parseUsInterstate()
    {
        if (match(TOKENIZER.INTERSTATE))
        {
            final var number = number();
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
     * PARSES: SR 520, WA-99, WY-89
     */
    private boolean parseUsStateRoute()
    {
        if (lookingAt(TOKENIZER.STATE_ROUTE))
        {
            final var state = Strip.trailing(current().text().trim(), "-");
            next();
            skipAny(TOKENIZER.WHITESPACE);
            if (lookingAt(TOKENIZER.DASH))
            {
                next();
            }
            skipAny(TOKENIZER.WHITESPACE);
            final var number = number();
            if (number != null)
            {
                builder().baseName(state + "-" + number, rawText());
                return true;
            }
        }
        reset();
        return false;
    }

    /**
     * @return True if there should be a space between the given tokens
     */
    @SuppressWarnings("RedundantIfStatement")
    private boolean space(final Token previous, final Token token)
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

        if (TOKENIZER.isDigit(token) || TOKENIZER.isNumericOrdinalSuffix(token))
        {
            if (TOKENIZER.isDigit(previous) || TOKENIZER.isDash(previous) || TOKENIZER.isInterstate(previous))
            {
                return false;
            }
        }
        return true;
    }
}
