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

import com.telenav.kivakit.core.kernel.language.strings.Align;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This {@link BitArray} class could be removed if BitArray supported accurate size in bits.
 *
 * @author Mihai Chintoanu
 * @author jonathanl (shibo)
 */
final class BitArray
{
    public static class Builder
    {
        private final List<Boolean> bits = new LinkedList<>();

        @SuppressWarnings("UnusedReturnValue")
        Builder appendBit(final boolean bit)
        {
            bits.add(bit);
            return this;
        }

        /**
         * @param value a decimal value to be represented in binary and appended to the bit array
         * @param bitCount the number of bits on which to represent the value
         * @return this builder
         */
        @SuppressWarnings("UnusedReturnValue")
        Builder appendBits(final int value, final int bitCount)
        {
            return appendBits(Align.right(Integer.toString(value, 2), bitCount, '0'));
        }

        Builder appendBits(final String bitsAsString)
        {
            for (final var c : bitsAsString.toCharArray())
            {
                switch (c)
                {
                    case '0':
                        bits.add(false);
                        break;
                    case '1':
                        bits.add(true);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Incorrect bit array: " + bitsAsString + "; contains invalid bit value: " + c);
                }
            }
            return this;
        }

        BitArray build()
        {
            return new BitArray(bits);
        }
    }

    private final List<Boolean> bits;

    private transient String stringRepresentation;

    private BitArray(final List<Boolean> bits)
    {
        this.bits = new ArrayList<>(bits);
    }

    public List<Boolean> allBits()
    {
        return new ArrayList<>(bits);
    }

    public boolean isSet(final int index)
    {
        if (index < 0 || index >= bits.size())
        {
            throw new IndexOutOfBoundsException("" + index);
        }
        return bits.get(index);
    }

    public int length()
    {
        return bits.size();
    }

    @Override
    public String toString()
    {
        if (stringRepresentation == null)
        {
            final var builder = new StringBuilder();
            for (final boolean bit : bits)
            {
                builder.append(bit ? "1" : "0");
            }
            stringRepresentation = builder.toString();
        }
        return stringRepresentation;
    }
}
