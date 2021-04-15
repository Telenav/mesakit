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

package com.telenav.aonia.map.road.name.parser.tokenizer.symbols;

import java.util.HashMap;
import java.util.Map;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

public class Symbol
{
    private static final Map<String, Symbol> symbols = new HashMap<>();

    public static final Symbol WHITESPACE = new Symbol(" ", "<WHITESPACE>");

    public static final Symbol DASH = new Symbol("-", "<DASH>");

    public static final Symbol SLASH = new Symbol("/", "<SLASH>");

    public static final Symbol DOT = new Symbol(".", "<DOT>");

    public static final Symbol OPEN_PARENTHESIS = new Symbol("(", "<OPEN-PARENTHESIS>");

    public static final Symbol CLOSE_PARENTHESIS = new Symbol(")", "<CLOSE-PARENTHESIS>");

    public static synchronized Symbol of(final String value)
    {
        final var symbol = symbols.get(value);
        return symbol != null ? symbol : new Word(value);
    }

    private final String text;

    private final String name;

    Symbol(final String text, final String name)
    {
        this.text = text;
        this.name = name;
        if (text != null)
        {
            if (symbols.get(text) != null)
            {
                fail("CodedSymbol '$' is defined twice", text);
            }
            symbols.put(text, this);
        }
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Symbol)
        {
            final var that = (Symbol) object;
            return text.equalsIgnoreCase(that.text);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return text.toUpperCase().hashCode();
    }

    public String name()
    {
        return name;
    }

    public String text()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return text();
    }
}
