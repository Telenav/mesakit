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

package com.telenav.tdk.graph.specifications.unidb.graph.edge.model.attributes;

import com.telenav.tdk.core.kernel.interfaces.numeric.Quantizable;
import com.telenav.tdk.core.kernel.language.bits.BitDiagram;
import com.telenav.tdk.core.kernel.language.bits.BitDiagram.BitField;
import com.telenav.tdk.core.kernel.language.string.Strings;

public class TurnLane implements Quantizable
{
    public static final long NULL = 0;

    private static final BitDiagram diagram = new BitDiagram("FEDCBA98765432X");

    public enum Arrow
    {
        STRAIGHT('X'),
        SLIGHT_RIGHT('2'),
        RIGHT('3'),
        SHARP_RIGHT('4'),
        UTURN_LEFT('5'),
        SHARP_LEFT('6'),
        LEFT('7'),
        SLIGHT_LEFT('8'),
        UTURN_RIGHT('9'),
        MERGE_INTO_LEFT_LANE('A'),
        MERGING_LANES('B'),
        MERGE_INTO_RIGHT_LANE('C'),
        SECOND_RIGHT('D'),
        SECOND_LEFT('E');

        private final BitField field;

        Arrow(final char c)
        {
            field = diagram.field(c);
        }

        boolean hasTurnArrow(final int type)
        {
            return field.extractBoolean(type);
        }

        int withTurnArrow(final int bits, final boolean enabled)
        {
            return field.set(bits, enabled);
        }
    }

    private int bits;

    public TurnLane(final int bits)
    {
        this.bits = bits;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof TurnLane)
        {
            final var that = (TurnLane) object;
            return bits == that.bits;
        }
        return false;
    }

    public boolean hasTurnArrow(final Arrow type)
    {
        return type.hasTurnArrow(bits);
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(bits);
    }

    @Override
    public long quantum()
    {
        return bits;
    }

    @Override
    public String toString()
    {
        return Strings.toString(bits);
    }

    public TurnLane withTurnArrow(final Arrow type, final boolean enabled)
    {
        bits = type.withTurnArrow(bits, enabled);
        return this;
    }
}
