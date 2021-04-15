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

import com.telenav.kivakit.core.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.core.kernel.language.strings.Join;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TokenList implements Iterable<Token>
{
    public static class Builder
    {
        private final List<Token> tokens = new ArrayList<>();

        @SuppressWarnings("UnusedReturnValue")
        public Builder add(final Token token)
        {
            tokens.add(token);
            return this;
        }

        public TokenList build()
        {
            return new TokenList(tokens);
        }
    }

    // The list of tokens
    private final List<Token> tokens;

    // Current position in token list
    private int at;

    private TokenList(final List<Token> tokens)
    {
        this.tokens = tokens;
    }

    public int at()
    {
        return at;
    }

    public void at(final int at)
    {
        this.at = Math.min(at, size());
    }

    public boolean atLast()
    {
        return at == size() - 1;
    }

    public Token current()
    {
        return token(at);
    }

    public Token first()
    {
        return token(0);
    }

    public Token get(final int index)
    {
        return tokens.get(index);
    }

    public boolean hasMore()
    {
        return current() != null;
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Token> iterator()
    {
        return tokens.iterator();
    }

    public Token last()
    {
        return isEmpty() ? null : token(size() - 1);
    }

    public boolean lookingAt(final Token token)
    {
        return current() != null && current().equals(token);
    }

    public boolean match(final Token token)
    {
        if (lookingAt(token))
        {
            next();
            return true;
        }
        return false;
    }

    public void next()
    {
        at(at + 1);
    }

    public int previous()
    {
        if (at > 0)
        {
            return at - 1;
        }
        return 0;
    }

    public String rawText()
    {
        return Join.join(tokens(), Token::text, " ");
    }

    public String remainder()
    {
        final var builder = new StringBuilder();
        while (hasMore())
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }
            builder.append(current().text());
            next();
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    public String remainderCapitalized()
    {
        final var builder = new StringBuilder();
        while (hasMore())
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }
            builder.append(CaseFormat.capitalizeOnlyFirstLetter(current().text()));
            next();
        }
        return builder.length() > 0 ? builder.toString() : null;
    }

    public Token remove(final int index)
    {
        final var removed = tokens.remove(index);
        reset();
        return removed;
    }

    public Token removeFirst()
    {
        return remove(0);
    }

    public Token removeLast()
    {
        return remove(size() - 1);
    }

    public void reset()
    {
        at(0);
    }

    public int size()
    {
        return tokens.size();
    }

    public void skipAny(final Token token)
    {
        if (lookingAt(token))
        {
            next();
        }
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
            final var text = token(i).toString();
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

    public Token token(final int index)
    {
        if (index < tokens.size())
        {
            return tokens.get(index);
        }
        return null;
    }

    public List<? extends Token> tokens()
    {
        return tokens;
    }
}
