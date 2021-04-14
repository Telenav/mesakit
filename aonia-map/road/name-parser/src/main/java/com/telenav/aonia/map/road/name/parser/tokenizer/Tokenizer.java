////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer;

import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.Symbol;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.SymbolList;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.SymbolStream;
import com.telenav.kivakit.core.collections.map.MultiMap;

import java.util.List;

/**
 * Returns a list of tokens given an input string. Note that there may be more than one symbol in a token, for example
 * "NORTH WEST" is three symbols in a row: NORTH, WHITESPACE and WEST.
 *
 * @author jonathanl (shibo)
 */
public class Tokenizer
{
    /**
     * The list of token matchers that can recognize a token starting with the given symbol
     */
    private final MultiMap<Symbol, TokenMatcher> matchers = new MultiMap<>();

    /**
     * Token id sequence
     */
    private int nextTokenIdentifier = 1;

    public final Token WORD = create("Word");

    public final Token DASH = create("Dash").matchesAnyOf("-");

    public final Token DOT = create("Dot").matchesAnyOf(".");

    public final Token SEMICOLON = create("Semicolon").matchesAnyOf(".");

    public final Token POUND_SIGN = create("PoundSign").matchesAnyOf("#");

    public final Token SLASH = create("Slash").matchesAnyOf("/");

    public final Token WHITESPACE = create("Whitespace").matchesAnyOf(" ");

    /**
     * @return A new token with the next available token identifier
     */
    public Token create(final String name)
    {
        return new Token(this, nextTokenIdentifier++, name, null);
    }

    public boolean isDash(final Token token)
    {
        return token != null && token.equals(DASH);
    }

    public boolean isDot(final Token token)
    {
        return token != null && token.equals(DOT);
    }

    public boolean isLetter(final Token token)
    {
        return token != null && token.isWord() && token.symbolCount() == 1;
    }

    public boolean isPoundSign(final Token token)
    {
        return token != null && token.equals(POUND_SIGN);
    }

    public boolean isSemicolon(final Token token)
    {
        return token != null && token.equals(SEMICOLON);
    }

    public boolean isSlash(final Token token)
    {
        return token != null && token.equals(SLASH);
    }

    /**
     * @param stream The input symbols
     * @return Any token recognized by the registered token matchers or null if no token was recognized
     */
    public Token next(final SymbolStream stream)
    {
        final var at = stream.at();
        final List<TokenMatcher> matchers = this.matchers.get(stream.first());
        if (matchers != null)
        {
            for (final var matcher : matchers)
            {
                final var token = matcher.match(stream);
                if (token != null)
                {
                    return token;
                }
            }
        }
        stream.at(at);
        stream.next();
        return WORD.of(stream);
    }

    /**
     * @param input The input string
     * @return The list of tokens
     */
    public TokenList tokenize(final String input)
    {
        final var builder = new TokenList.Builder();

        // Parse the road name into terminal symbols
        var symbols = SymbolList.of(input);

        // While we have more symbols to tokenize
        while (!symbols.isEmpty())
        {
            // get the next token from list of symbols
            final var token = next(symbols.asStream());

            // and if we got a valid token
            if (token != null)
            {
                // add it to the list
                builder.add(token);

                // and move ahead in the input by the number of symbols the token matched
                symbols = symbols.subList(token.symbolCount());
            }
            else
            {
                // Unable to tokenize this road name
                return null;
            }
        }

        return builder.build();
    }

    void add(final Symbol symbol, final TokenMatcher matcher)
    {
        matchers.add(symbol, matcher);
    }
}
