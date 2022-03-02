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

package com.telenav.mesakit.map.road.name.parser.tokenizer.symbols;

import com.telenav.kivakit.core.language.strings.Strings;

public class SymbolStream
{
    private int at;

    private final SymbolList input;

    SymbolStream(SymbolList input)
    {
        this.input = input;
    }

    public void advance(int count)
    {
        at = Math.min(at + count, size());
    }

    public int at()
    {
        return at;
    }

    public void at(int at)
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

    public boolean lookingAt(String... symbols)
    {
        var index = at();
        for (var symbol : symbols)
        {
            if (!Symbol.of(symbol).equals(input.get(index++)))
            {
                return false;
            }
        }
        return true;
    }

    public boolean lookingAt(Symbol... symbols)
    {
        var index = at();
        for (var symbol : symbols)
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
        return Strings.isNaturalNumber(currentText());
    }

    public boolean lookingAtWhiteSpace()
    {
        return lookingAt(Symbol.WHITESPACE);
    }

    public boolean matches(String... symbols)
    {
        if (lookingAt(symbols))
        {
            advance(symbols.length);
            return true;
        }
        return false;
    }

    public boolean matches(Symbol... symbols)
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

    public void skipAny(String... symbols)
    {
        for (var symbol : symbols)
        {
            skipAny(Symbol.of(symbol));
        }
    }

    public void skipAny(Symbol symbol)
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
        var builder = new StringBuilder();
        for (var i = 0; i < at; i++)
        {
            builder.append(input.get(i));
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        for (var i = 0; i < size(); i++)
        {
            if (i > 0)
            {
                builder.append(", ");
            }
            var text = input.get(i).toString();
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
