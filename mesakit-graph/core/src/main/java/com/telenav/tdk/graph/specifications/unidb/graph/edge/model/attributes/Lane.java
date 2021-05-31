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

public class Lane implements Quantizable
{
    private static final BitDiagram diagram = new BitDiagram("IHGFEDCBA98765432X");

    public static final long NULL = 0;

    public enum Type
    {
        REGULAR('X'),
        HOV('2'),
        REVERSIBLE('3'),
        EXPRESS('4'),
        ACCELERATION('5'),
        DECELERATION('6'),
        AUXILIARY('7'),
        SLOW('8'),
        PASSING('9'),
        SHOULDER('A'),
        REGULATED('B'),
        TURN('C'),
        CENTER('D'),
        TRUCK('E'),
        PARKING('F'),
        VARIABLE('G'),
        BICYCLE('H'),
        BUS('I');

        private final BitField field;

        Type(final char c)
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

    public Lane()
    {
    }

    public Lane(final int bits)
    {
        this.bits = bits;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Lane)
        {
            final var that = (Lane) object;
            return bits == that.bits;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(bits);
    }

    public boolean is(final Type type)
    {
        return type.is(bits);
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
        for (final var type : Type.values())
        {
            if (type.is(bits))
            {
                types.add(type.name().toLowerCase());
            }
        }
        return Strings.toBinaryString(bits, 18) + ": " + types.join(", ");
    }

    public Lane withLaneType(final Type type, final boolean enabled)
    {
        bits = type.set(bits, enabled);
        return this;
    }
}
