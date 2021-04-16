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

package com.telenav.mesakit.map.road.name.parser;

import com.telenav.mesakit.map.road.name.parser.tokenizer.Token;
import com.telenav.mesakit.map.road.name.parser.tokenizer.TokenList;
import com.telenav.mesakit.map.road.name.parser.tokenizer.Tokenizer;
import com.telenav.kivakit.core.kernel.language.strings.CaseFormat;
import com.telenav.kivakit.core.kernel.language.strings.Join;

public abstract class BaseRoadNameParser implements RoadNameParser
{
    // Tokens parsed by tokenizer
    TokenList tokens;

    // Position in token list
    private int at;

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        for (var i = 0; i < size(); i++)
        {
            if (i > 0)
            {
                builder.append(' ');
            }
            if (i == at)
            {
                builder.append("[").append(token(i).text()).append("]");
            }
            else
            {
                builder.append(token(i).text());
            }
        }
        if (at == size())
        {
            builder.append(" [END]");
        }
        return builder.toString();
    }

    protected int at()
    {
        return at;
    }

    protected void at(final int at)
    {
        this.at = Math.min(at, size());
    }

    protected boolean atLast()
    {
        return at == size() - 1;
    }

    protected Token current()
    {
        return token(at);
    }

    protected Token first()
    {
        return token(0);
    }

    protected boolean hasMore()
    {
        return current() != null;
    }

    protected boolean isEmpty()
    {
        return size() == 0;
    }

    protected Token last()
    {
        return isEmpty() ? null : token(size() - 1);
    }

    protected Token lookahead()
    {
        return hasMore() ? token(at + 1) : null;
    }

    protected boolean lookingAt(final Token token)
    {
        return current() != null && current().equals(token);
    }

    protected boolean match(final Token token)
    {
        if (lookingAt(token))
        {
            next();
            return true;
        }
        return false;
    }

    protected void next()
    {
        at(at + 1);
    }

    protected int previous()
    {
        if (at > 0)
        {
            return at - 1;
        }
        return 0;
    }

    protected String rawText()
    {
        return Join.join(tokens.tokens(), Token::text, "");
    }

    protected String remainder()
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

    protected String remainderCapitalized()
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

    protected Token remove(final int index)
    {
        final var removed = tokens.remove(index);
        reset();
        return removed;
    }

    protected Token removeFirst()
    {
        final var token = remove(0);
        if (size() > 0 && token(0).isWhitespace())
        {
            remove(0);
        }
        return token;
    }

    protected Token removeLast()
    {
        final var token = remove(size() - 1);
        if (size() > 0 && token(size() - 1).isWhitespace())
        {
            remove(size() - 1);
        }
        return token;
    }

    protected void reset()
    {
        at(0);
    }

    protected int size()
    {
        return tokens.size();
    }

    protected void skipAny(final Token token)
    {
        while (lookingAt(token))
        {
            next();
        }
    }

    protected Token token(final int index)
    {
        if (index < tokens.size())
        {
            return tokens.get(index);
        }
        return null;
    }

    protected void tokenize(final Tokenizer tokenizer, final String input)
    {
        try
        {
            tokens = tokenizer.tokenize(input);
            reset();
        }
        catch (final Exception e)
        {
            throw new IllegalStateException("Unable to tokenize '" + input + "'", e);
        }
    }

    protected TokenList tokens()
    {
        return tokens;
    }
}
