////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.graph.specifications.unidb.graph.edge.model.attributes;

import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.bits.BitDiagram;
import com.telenav.kivakit.kernel.language.bits.BitDiagram.BitField;
import com.telenav.kivakit.kernel.language.string.*;

public class RouteType implements Quantizable
{
    public static final long NULL = 0;

    private static final BitDiagram diagram = new BitDiagram("65432X?");

    public static RouteType forBits(final int bits)
    {
        return new RouteType(bits);
    }

    public enum Level
    {
        LEVEL_1('X'),
        LEVEL_2('2'),
        LEVEL_3('3'),
        LEVEL_4('4'),
        LEVEL_5('5'),
        LEVEL_6('6');

        private final BitField field;

        Level(final char c)
        {
            field = diagram.field(c);
        }

        boolean is(final int bits)
        {
            return field.extractBoolean(bits);
        }

        int set(final int bits, final boolean enabled)
        {
            return field.set(bits, enabled);
        }
    }

    private int bits;

    protected RouteType()
    {
    }

    protected RouteType(final int bits)
    {
        this.bits = bits;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RouteType)
        {
            final var that = (RouteType) object;
            return bits == that.bits;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(bits);
    }

    public boolean is(final Level level)
    {
        return level.is(bits);
    }

    @Override
    public long quantum()
    {
        return bits;
    }

    @Override
    public String toString()
    {
        final var types = new StringList();
        for (final var type : Lane.Type.values())
        {
            if (type.is(bits))
            {
                types.add(type.name().toLowerCase());
            }
        }
        return Strings.toBinaryString(bits, 18) + ": " + types.join(", ");
    }

    public RouteType withLevel(final Level level, final boolean enabled)
    {
        bits = level.set(bits, enabled);
        return this;
    }
}
