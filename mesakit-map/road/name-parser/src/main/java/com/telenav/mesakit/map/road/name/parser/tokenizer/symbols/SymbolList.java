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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SymbolList implements Iterable<Symbol>
{
    public static SymbolList of(String input)
    {
        var symbols = new SymbolList();

        var inWord = false;
        var inWhitespace = false;

        var word = new StringBuilder();

        for (var i = 0; i < input.length(); i++)
        {
            var c = input.charAt(i);

            if (c == '.' || c == '?')
            {
                continue;
            }

            var isLetter = isLetter(c);
            var isWhitespace = Character.isWhitespace(c);

            if (inWhitespace && !isWhitespace)
            {
                symbols.add(Symbol.WHITESPACE);
            }

            if (inWord && !isLetter)
            {
                symbols.add(Symbol.of(word.toString()));
                word = new StringBuilder();
            }

            if (isLetter)
            {
                word.append(c);
            }

            if (!isLetter && !isWhitespace)
            {
                symbols.add(Symbol.of(Character.toString(c)));
            }

            inWhitespace = isWhitespace;
            inWord = isLetter;
        }

        if (inWhitespace)
        {
            symbols.add(Symbol.WHITESPACE);
        }

        if (inWord)
        {
            symbols.add(Symbol.of(word.toString()));
        }

        return symbols;
    }

    private final List<Symbol> symbols;

    public SymbolList()
    {
        this(new ArrayList<>());
    }

    public SymbolList(List<Symbol> symbols)
    {
        this.symbols = symbols;
    }

    public SymbolStream asStream()
    {
        return new SymbolStream(this);
    }

    public Symbol first()
    {
        return symbols.isEmpty() ? null : symbols.get(0);
    }

    public Symbol get(int index)
    {
        return index < size() ? symbols.get(index) : null;
    }

    public boolean isEmpty()
    {
        return symbols.isEmpty();
    }

    @Override
    public Iterator<Symbol> iterator()
    {
        return symbols.iterator();
    }

    public int size()
    {
        return symbols.size();
    }

    public SymbolList subList(int from)
    {
        return new SymbolList(symbols.subList(from, symbols.size()));
    }

    private static boolean isLetter(char character)
    {
        // Characters that are considered to be letters, so they are part of a word (like "can't
        // drive")
        if (character == '\'')
        {
            return true;
        }
        return Character.isLetter(character);
    }

    private void add(Symbol symbol)
    {
        symbols.add(symbol);
    }
}
