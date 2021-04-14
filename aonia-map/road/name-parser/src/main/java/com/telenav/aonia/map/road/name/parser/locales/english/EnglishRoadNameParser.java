////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.locales.english;

import com.telenav.aonia.map.road.model.RoadName;
import com.telenav.aonia.map.road.name.parser.BaseRoadNameParser;
import com.telenav.aonia.map.road.name.parser.ParsedRoadName;
import com.telenav.aonia.map.road.name.parser.tokenizer.Token;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.aonia.map.road.name.parser.ParsedRoadName.DirectionFormat.NONE;
import static com.telenav.aonia.map.road.name.parser.ParsedRoadName.DirectionFormat.PREFIXED;
import static com.telenav.aonia.map.road.name.parser.ParsedRoadName.DirectionFormat.SUFFIXED;

public abstract class EnglishRoadNameParser extends BaseRoadNameParser
{
    private static final EnglishUnitedStatesTokenizer TOKENIZER = new EnglishUnitedStatesTokenizer();

    // Builder for creating parse information
    private ParsedRoadName.Builder builder;

    private final Map<Token, String> ordinal = new HashMap<>();

    private final Map<Token, String> number = new HashMap<>();

    private final Map<Token, String> direction = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized ParsedRoadName parse(final RoadName name)
    {
        // Get input text
        final var input = name.name();
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
            // prefer the road type over the direction to handle cases like North Street as North St
            // rather than N Street
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

    protected ParsedRoadName.Builder builder()
    {
        return builder;
    }

    protected String direction(final Token token)
    {
        return direction.get(token);
    }

    protected String number()
    {
        final var builder = new StringBuilder();
        while (lookingAt(TOKENIZER.DIGIT))
        {
            builder.append(current().text());
            next();
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    protected String number(final Token token)
    {
        return number.get(token);
    }

    protected String ordinal(final Token token)
    {
        return ordinal.get(token);
    }

    /**
     * PARSES: 'west greenlake way nw', 'ne 59th', 'sixth northeast'
     */
    protected void parseAndRemoveDirection()
    {
        if (size() > 1)
        {
            // If the last token is a direction,
            var prefix = false;
            var suffix = false;

            // West Greenlake Way NW
            if (TOKENIZER.isQuadrant(last()))
            {
                suffix = true;
            }
            // NW Old West
            else if (TOKENIZER.isQuadrant(first()))
            {
                prefix = true;
            }
            // East of Eden West
            else if (TOKENIZER.isCardinalDirection(last()))
            {
                suffix = true;
            }
            // East Marginal Way
            else if (TOKENIZER.isCardinalDirection(first()))
            {
                prefix = true;
            }

            if (suffix)
            {
                builder.directionFormat(SUFFIXED);
                final var removed = removeLast();
                builder.direction(direction(removed), removed.text());
            }
            else if (prefix)
            {
                builder.directionFormat(PREFIXED);
                final var removed = removeFirst();
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
     * PARSES and STANDARDIZES: street, road, cir, st, ave
     */
    protected void parseAndRemoveRoadType()
    {
        if (size() >= 2)
        {
            // CONTEXT SENSITIVITY: If the last token is "st" this could be either an abbreviation
            // for STREET or the "st" in "1st". The same goes for "rd", which could be either an
            // abbreviation for ROAD or the "rd" in "3rd". We know if it's a road type if the prior
            // token is whitespace. In other words, "3 rd" would be "3 road" and not "3rd".

            final var last = last();
            final var nextToLast = token(size() - 2);

            if (TOKENIZER.WHITESPACE.equals(nextToLast))
            {
                // The road name ends in WHITESPACE "rd" (10 Mile Rd)
                if (last.equals(TOKENIZER.RD))
                {
                    removeLast();
                    builder.type(TOKENIZER.ROAD.name(), last.text());
                }

                // The road name ends in WHITESPACE "st" (Main St)
                else if (last.equals(TOKENIZER.ST))
                {
                    removeLast();
                    builder.type(TOKENIZER.STREET.name(), last.text());
                }

                // The road name ends in WHITESPACE "Wy" (Navareth Wy), which is ambiguous for
                // Wyoming (WY-99)
                else if ("Wy".equalsIgnoreCase(last.text()))
                {
                    removeLast();
                    builder.type(TOKENIZER.WAY.name(), last.text());
                }

                // If the last token is a road type
                else if (TOKENIZER.isRoadType(last))
                {
                    // remove the token and add to the builder
                    final var removed = removeLast();
                    builder.type(removed.name(), removed.text());
                }
            }
        }
    }

    /**
     * PARSES and STANDARDIZES: 'fifth', 'twenty-second', 'one hundred and ninth', 'two hundredth'
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean parseNamedOrdinal()
    {
        // If we're looking at an ordinal or a tens digit,
        if (TOKENIZER.isOrdinal(current()) || TOKENIZER.isTensDigit(current()))
        {
            // parse the one or two digit ordinal ('fifth', 'twenty-second') => (5th, 22nd)
            final var ordinal = parseTwoDigitNamedOrdinal();
            if (ordinal != null && !hasMore())
            {
                builder.baseName(ordinal, rawText());
                return true;
            }
        }

        // If we're looking at a named digit,
        if (TOKENIZER.isNamedDigit(current()))
        {
            // add the digit ('2')
            final var name = new StringBuilder(number(current()));

            // advance to next token
            next();
            skipAny(TOKENIZER.WHITESPACE);

            // If we're looking at 'hundredth'
            if (lookingAt(TOKENIZER.HUNDREDTH))
            {
                // advance to next token
                next();
                skipAny(TOKENIZER.WHITESPACE);

                // and if we're out of input,
                if (!hasMore())
                {
                    // add '00th' (300th)
                    name.append("00th");
                    builder.baseName(name.toString(), rawText());
                    return true;
                }
            }

            // If we're looking at 'thousandth'
            if (lookingAt(TOKENIZER.THOUSANDTH))
            {
                // advance to next token
                next();
                skipAny(TOKENIZER.WHITESPACE);

                // and if we're out of input,
                if (!hasMore())
                {
                    // add '000th' (1000th)
                    name.append("000th");
                    builder.baseName(name.toString(), rawText());
                    return true;
                }
            }

            // If we're looking at 'hundred' (one hundred twenty-sixth)
            if (lookingAt(TOKENIZER.HUNDRED))
            {
                // skip token and any 'and' token (two hundred and fourth)
                next();
                skipAny(TOKENIZER.WHITESPACE);
                skipAny(TOKENIZER.AND);
                skipAny(TOKENIZER.WHITESPACE);

                // parse out two digit ordinal (ninety-ninth, fifth)
                final var ordinal = parseTwoDigitNamedOrdinal();
                if (ordinal != null && !hasMore())
                {
                    name.append(ordinal);
                    builder.baseName(name.toString(), rawText());
                    return true;
                }
            }
        }
        reset();
        return false;
    }

    /**
     * PARSES: 1st, 2nd, 99th, 103rd
     * <p>
     * REJECTS: 10 mile road
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean parseNumericOrdinal()
    {
        // If we're looking at a simple nth value, like 1st, 2nd, 9th or 1th (as in 11th)
        if (lookingAt(TOKENIZER.DIGIT) && TOKENIZER.isNumericOrdinalSuffix(lookahead()) && size() == 2)
        {
            builder.baseName(current().text(), rawText());
        }

        // If we're looking at a digit,
        if (lookingAt(TOKENIZER.DIGIT))
        {
            // get the specified number
            final var number = number();
            if (number != null)
            {
                // and add any suffix
                final var name = new StringBuilder(number);
                if (TOKENIZER.isNumericOrdinalSuffix(current()))
                {
                    name.append(current().text());
                    next();
                    skipAny(TOKENIZER.WHITESPACE);
                }

                if (!hasMore())
                {
                    builder.baseName(name.toString(), rawText());
                    return true;
                }
            }
        }
        reset();
        return false;
    }

    protected abstract void parseRoadName();

    /**
     * PARSES: first, sixth, fifteenth, twenty-second, fifty ninth, sixty-sixth
     */
    protected String parseTwoDigitNamedOrdinal()
    {
        // If we're looking at a simple ordinal (first, 6th, fifteenth, 17th)
        if (TOKENIZER.isOrdinal(current()) && atLast())
        {
            // then use the standard ordinal for the token
            next();
            skipAny(TOKENIZER.WHITESPACE);
            return ordinal(last());
        }
        else
        {
            // If we're looking at a tens digit ('twenty')
            if (TOKENIZER.isTensDigit(current()))
            {
                // add the tens digit ('2')
                final var name = new StringBuilder(number(current()));
                next();
                skipAny(TOKENIZER.WHITESPACE);

                // skip any dash ('twenty-second')
                skipAny(TOKENIZER.WHITESPACE);
                skipAny(TOKENIZER.DASH);
                skipAny(TOKENIZER.WHITESPACE);

                // and then if we're looking at a simple ordinal ('1st')
                if (TOKENIZER.isOrdinal(current()) && atLast())
                {
                    // add that to the end ('21st')
                    next();
                    skipAny(TOKENIZER.WHITESPACE);
                    name.append(ordinal(last()));

                    // and we now have the ordinal
                    return name.toString();
                }
            }
        }

        return null;
    }

    protected boolean shouldCapitalize(final Token previous, final Token token)
    {
        if (previous == null)
        {
            return true;
        }
        if (TOKENIZER.isNumericOrdinalSuffix(token) && TOKENIZER.isDigit(previous))
        {
            return false;
        }
        switch (token.text().toLowerCase())
        {
            case "of":
            case "de":
            case "and":
            case "for":
                return false;
        }
        return true;
    }

    {
        ordinal.put(TOKENIZER.FIRST, "1st");
        ordinal.put(TOKENIZER.SECOND, "2nd");
        ordinal.put(TOKENIZER.THIRD, "3rd");
        ordinal.put(TOKENIZER.FOURTH, "4th");
        ordinal.put(TOKENIZER.FIFTH, "5th");
        ordinal.put(TOKENIZER.SIXTH, "6th");
        ordinal.put(TOKENIZER.SEVENTH, "7th");
        ordinal.put(TOKENIZER.EIGHTH, "8th");
        ordinal.put(TOKENIZER.NINTH, "9th");
        ordinal.put(TOKENIZER.TENTH, "10th");
        ordinal.put(TOKENIZER.ELEVENTH, "11th");
        ordinal.put(TOKENIZER.TWELFTH, "12th");
        ordinal.put(TOKENIZER.THIRTEENTH, "13th");
        ordinal.put(TOKENIZER.FOURTEENTH, "14th");
        ordinal.put(TOKENIZER.FIFTEENTH, "15th");
        ordinal.put(TOKENIZER.SIXTEENTH, "16th");
        ordinal.put(TOKENIZER.SEVENTEENTH, "17th");
        ordinal.put(TOKENIZER.EIGHTEENTH, "18th");
        ordinal.put(TOKENIZER.NINETEENTH, "19th");
        ordinal.put(TOKENIZER.TWENTIETH, "20th");
        ordinal.put(TOKENIZER.THIRTIETH, "30th");
        ordinal.put(TOKENIZER.FORTIETH, "40th");
        ordinal.put(TOKENIZER.FIFTIETH, "50th");
        ordinal.put(TOKENIZER.SIXTIETH, "60th");
        ordinal.put(TOKENIZER.SEVENTIETH, "70th");
        ordinal.put(TOKENIZER.EIGHTIETH, "80th");
        ordinal.put(TOKENIZER.NINETIETH, "90th");
        ordinal.put(TOKENIZER.HUNDREDTH, "100th");
        ordinal.put(TOKENIZER.THOUSAND, "1000th");

        number.put(TOKENIZER.ZERO, "0");
        number.put(TOKENIZER.ONE, "1");
        number.put(TOKENIZER.TWO, "2");
        number.put(TOKENIZER.THREE, "3");
        number.put(TOKENIZER.FOUR, "4");
        number.put(TOKENIZER.FIVE, "5");
        number.put(TOKENIZER.SIX, "6");
        number.put(TOKENIZER.SEVEN, "7");
        number.put(TOKENIZER.EIGHT, "8");
        number.put(TOKENIZER.NINE, "9");
        number.put(TOKENIZER.TEN, "10");
        number.put(TOKENIZER.ELEVEN, "11");
        number.put(TOKENIZER.TWELVE, "12");
        number.put(TOKENIZER.THIRTEEN, "13");
        number.put(TOKENIZER.FOURTEEN, "14");
        number.put(TOKENIZER.FIFTEEN, "15");
        number.put(TOKENIZER.SIXTEEN, "16");
        number.put(TOKENIZER.SEVENTEEN, "17");
        number.put(TOKENIZER.EIGHTEEN, "18");
        number.put(TOKENIZER.NINETEEN, "19");
        number.put(TOKENIZER.TWENTY, "2");
        number.put(TOKENIZER.THIRTY, "3");
        number.put(TOKENIZER.FORTY, "4");
        number.put(TOKENIZER.FIFTY, "5");
        number.put(TOKENIZER.SIXTY, "6");
        number.put(TOKENIZER.SEVENTY, "7");
        number.put(TOKENIZER.EIGHTY, "8");
        number.put(TOKENIZER.NINETY, "9");

        direction.put(TOKENIZER.NORTH, "N");
        direction.put(TOKENIZER.SOUTH, "S");
        direction.put(TOKENIZER.EAST, "E");
        direction.put(TOKENIZER.WEST, "W");
        direction.put(TOKENIZER.NORTHWEST, "NW");
        direction.put(TOKENIZER.NORTHEAST, "NE");
        direction.put(TOKENIZER.SOUTHWEST, "SW");
        direction.put(TOKENIZER.SOUTHEAST, "SE");
    }
}
