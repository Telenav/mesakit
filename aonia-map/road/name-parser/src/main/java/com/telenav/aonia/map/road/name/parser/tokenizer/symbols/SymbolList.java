////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  © 2020 Telenav - All rights reserved.                                                                              /
//  This software is the confidential and proprietary information of Telenav ("Confidential Information").             /
//  You shall not disclose such Confidential Information and shall use it only in accordance with the                  /
//  terms of the license agreement you entered into with Telenav.                                                      /
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.road.name.parser.tokenizer.symbols;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SymbolList implements Iterable<Symbol>
{
    public static SymbolList of(final String input)
    {
        final var symbols = new SymbolList();

        var inWord = false;
        var inWhitespace = false;

        var word = new StringBuilder();

        for (var i = 0; i < input.length(); i++)
        {
            final var c = input.charAt(i);

            if (c == '.' || c == '?')
            {
                continue;
            }

            final var isLetter = isLetter(c);
            final var isWhitespace = Character.isWhitespace(c);

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

    public SymbolList(final List<Symbol> symbols)
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

    public Symbol get(final int index)
    {
        return index < size() ? symbols.get(index) : null;
    }

    public boolean isEmpty()
    {
        return symbols.isEmpty();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Symbol> iterator()
    {
        return symbols.iterator();
    }

    public int size()
    {
        return symbols.size();
    }

    public SymbolList subList(final int from)
    {
        return new SymbolList(symbols.subList(from, symbols.size()));
    }

    private static boolean isLetter(final char character)
    {
        // Characters that are considered to be letters so they are part of a word (like "can't
        // drive")
        if (character == '\'')
        {
            return true;
        }
        return Character.isLetter(character);
    }

    private void add(final Symbol symbol)
    {
        symbols.add(symbol);
    }
}
