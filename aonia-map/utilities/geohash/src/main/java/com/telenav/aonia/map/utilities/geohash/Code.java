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

package com.telenav.aonia.map.utilities.geohash;

import com.telenav.kivakit.core.kernel.language.objects.Hash;

import java.util.Collection;
import java.util.HashSet;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

public class Code implements CharSequence
{
    /**
     * When checking if a {@code code} is root, don't use {@code code.equals(Code.root())}; use {@code code.isRoot()}
     * instead. This is because {@code Code.root()} uses the default alphabet, and if your {@code code} uses a different
     * alphabet, {@code code.equals(Code.root())} will return {@code false}.
     *
     * @return the root of all codes which use the default alphabet
     */
    public static Code root()
    {
        return root(GeohashAlphabet.DEFAULT);
    }

    public static Code root(final GeohashAlphabet alphabet)
    {
        return new Code(alphabet, "");
    }

    private final GeohashAlphabet alphabet;

    private final String value;

    public Code(final GeohashAlphabet alphabet, final String value)
    {
        ensure(alphabet.isValid(value), "Invalid code: $ for alphabet ${class}", value, alphabet);
        this.alphabet = alphabet;
        this.value = value;
    }

    /**
     * Builds a new code which uses the default alphabet.
     *
     * @param value the code value
     */
    public Code(final String value)
    {
        this(GeohashAlphabet.DEFAULT, value);
    }

    @Override
    public char charAt(final int index)
    {
        return value.charAt(index);
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Code)
        {
            final var that = (Code) object;
            return alphabet.equals(that.alphabet) && value.equals(that.value);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(alphabet, value);
    }

    public boolean isRoot()
    {
        return root().value.equals(value);
    }

    @Override
    public int length()
    {
        return value.length();
    }

    @Override
    public CharSequence subSequence(final int start, final int end)
    {
        return value.subSequence(start, end);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString()
    {
        return value;
    }

    GeohashAlphabet alphabet()
    {
        return alphabet;
    }

    Collection<Code> children()
    {
        final Collection<Code> children = new HashSet<>();
        if (length() < alphabet.maximumTextLength())
        {
            for (final char character : alphabet().characters())
            {
                children.add(new Code(alphabet, value + character));
            }
        }
        return children;
    }

    Code parent()
    {
        if (isRoot())
        {
            return null;
        }
        else
        {
            return new Code(alphabet, value.substring(0, value.length() - 1));
        }
    }
}
