////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer.symbols;

import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;

public class SymbolStream
{
    private int at;

    private final SymbolList input;

    SymbolStream(final SymbolList input)
    {
        this.input = input;
    }

    public void advance(final int count)
    {
        at = Math.min(at + count, size());
    }

    public int at()
    {
        return at;
    }

    public void at(final int at)
    {
        this.at = at;
    }

    public Symbol current()
    {
        return input.get(at);
    }

    public String currentText()
    {
        return current() != null ? current().text() : null;
    }

    public Symbol first()
    {
        return input.first();
    }

    public boolean hasMore()
    {
        return at < input.size();
    }

    public boolean lookingAt(final String... symbols)
    {
        var index = at();
        for (final var symbol : symbols)
        {
            if (!Symbol.of(symbol).equals(input.get(index++)))
            {
                return false;
            }
        }
        return true;
    }

    public boolean lookingAt(final Symbol... symbols)
    {
        var index = at();
        for (final var symbol : symbols)
        {
            if (!symbol.equals(input.get(index++)))
            {
                return false;
            }
        }
        return true;
    }

    public boolean lookingAtDigit()
    {
        return AsciiArt.isNaturalNumber(currentText());
    }

    public boolean lookingAtWhiteSpace()
    {
        return lookingAt(Symbol.WHITESPACE);
    }

    public boolean matches(final String... symbols)
    {
        if (lookingAt(symbols))
        {
            advance(symbols.length);
            return true;
        }
        return false;
    }

    public boolean matches(final Symbol... symbols)
    {
        if (lookingAt(symbols))
        {
            advance(symbols.length);
            return true;
        }
        return false;
    }

    public void next()
    {
        if (at < input.size())
        {
            at++;
        }
    }

    public void skipAny(final String... symbols)
    {
        for (final var symbol : symbols)
        {
            skipAny(Symbol.of(symbol));
        }
    }

    public void skipAny(final Symbol symbol)
    {
        if (current() != null && current().equals(symbol))
        {
            next();
        }
    }

    public void skipAnyWhiteSpace()
    {
        skipAny(Symbol.WHITESPACE);
    }

    public String text()
    {
        final var builder = new StringBuilder();
        for (var i = 0; i < at; i++)
        {
            builder.append(input.get(i));
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        for (var i = 0; i < size(); i++)
        {
            if (i > 0)
            {
                builder.append(", ");
            }
            final var text = input.get(i).toString();
            if (i == at)
            {
                builder.append("** ").append(text).append(" **");
            }
            else
            {
                builder.append(text);
            }
        }
        if (at == size())
        {
            builder.append(" [END]");
        }
        return builder.toString();
    }

    private int size()
    {
        return input.size();
    }
}
