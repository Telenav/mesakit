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

package com.telenav.mesakit.map.road.name.parser.tokenizer;

import com.telenav.kivakit.core.collections.map.MultiMap;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.Symbol;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.SymbolList;
import com.telenav.mesakit.map.road.name.parser.tokenizer.symbols.SymbolStream;

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
    public Token create(String name)
    {
        return new Token(this, nextTokenIdentifier++, name, null);
    }

    public boolean isDash(Token token)
    {
        return token != null && token.equals(DASH);
    }

    public boolean isDot(Token token)
    {
        return token != null && token.equals(DOT);
    }

    public boolean isLetter(Token token)
    {
        return token != null && token.isWord() && token.symbolCount() == 1;
    }

    public boolean isPoundSign(Token token)
    {
        return token != null && token.equals(POUND_SIGN);
    }

    public boolean isSemicolon(Token token)
    {
        return token != null && token.equals(SEMICOLON);
    }

    public boolean isSlash(Token token)
    {
        return token != null && token.equals(SLASH);
    }

    /**
     * @param stream The input symbols
     * @return Any token recognized by the registered token matchers or null if no token was recognized
     */
    public Token next(SymbolStream stream)
    {
        var at = stream.at();
        List<TokenMatcher> matchers = this.matchers.get(stream.first());
        if (matchers != null)
        {
            for (var matcher : matchers)
            {
                var token = matcher.match(stream);
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
    public TokenList tokenize(String input)
    {
        var builder = new TokenList.Builder();

        // Parse the road name into terminal symbols
        var symbols = SymbolList.of(input);

        // While we have more symbols to tokenize
        while (!symbols.isEmpty())
        {
            // get the next token from list of symbols
            var token = next(symbols.asStream());

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

    void add(Symbol symbol, TokenMatcher matcher)
    {
        matchers.add(symbol, matcher);
    }
}
