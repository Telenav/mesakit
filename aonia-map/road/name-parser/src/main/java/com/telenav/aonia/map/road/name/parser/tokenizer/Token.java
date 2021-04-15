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

package com.telenav.aonia.map.road.name.parser.tokenizer;

import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.Symbol;
import com.telenav.aonia.map.road.name.parser.tokenizer.symbols.SymbolStream;

import java.util.Set;

public class Token
{
    private final Tokenizer tokenizer;

    private final int identifier;

    private final String name;

    private final int symbolCount;

    private final String text;

    Token(final Tokenizer grammar, final int identifier, final String name, final SymbolStream stream)
    {
        tokenizer = grammar;
        this.identifier = identifier;
        this.name = name;
        if (stream != null)
        {
            symbolCount = stream.at();
            text = stream.text();
        }
        else
        {
            symbolCount = 0;
            text = null;
        }
    }

    public Token addTo(final Set<Token> tokens)
    {
        tokens.add(this);
        return this;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Token)
        {
            final var that = (Token) object;
            return identifier == that.identifier;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(identifier);
    }

    public int identifier()
    {
        return identifier;
    }

    public boolean isDash()
    {
        return equals(tokenizer.DASH);
    }

    public boolean isDot()
    {
        return equals(tokenizer.DOT);
    }

    public boolean isSlash()
    {
        return equals(tokenizer.SLASH);
    }

    public boolean isWhitespace()
    {
        return equals(tokenizer.WHITESPACE);
    }

    public boolean isWord()
    {
        return equals(tokenizer.WORD);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Token matches(final String symbol, final TokenMatcher matcher)
    {
        matches(Symbol.of(symbol), matcher);
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Token matches(final Symbol symbol, final TokenMatcher matcher)
    {
        tokenizer.add(symbol, matcher);
        return this;
    }

    /**
     * Registers matchers for several symbol values
     *
     * @param values The symbol values to match
     * @return The token
     */
    public Token matchesAnyOf(final String... values)
    {
        for (final var value : values)
        {
            final var symbol = Symbol.of(value);
            matches(symbol, (symbols) ->
            {
                if (symbols.lookingAt(symbol))
                {
                    symbols.next();
                    return of(symbols);
                }
                return null;
            });
        }
        return this;
    }

    public Token matchesSequence(final String... words)
    {
        matches(words[0], (stream) ->
        {
            final var symbols = new Symbol[words.length];
            var i = 0;
            for (final var word : words)
            {
                symbols[i++] = Symbol.of(word);
            }
            if (stream.matches(symbols))
            {
                return of(stream);
            }
            return null;
        });
        return this;
    }

    public String name()
    {
        return name;
    }

    public Token of(final SymbolStream stream)
    {
        return new Token(tokenizer, identifier(), name(), stream);
    }

    public int symbolCount()
    {
        return symbolCount;
    }

    public String text()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return name() + " [" + text + "]";
    }
}
