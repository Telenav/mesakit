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

public class Access implements Quantizable
{
    private static final BitDiagram diagram = new BitDiagram("medtkfhxbc");

    public enum Type
    {
        CAR('c'),
        BUS('b'),
        TAXI('x'),
        HOV('h'),
        FOOT('f'),
        TRUCK('k'),
        THOUGH_TRAFFIC('t'),
        DELIVERY('d'),
        EMERGENCY('e'),
        MOTORCYCLE('m');

        private final BitField field;

        Type(final char c)
        {
            field = diagram.field(c);
        }

        boolean hasTurnArrow(final int type)
        {
            return field.extractBoolean(type);
        }
    }

    private final int bits;

    public Access(final int bits)
    {
        this.bits = bits;
    }

    public boolean access(final Type type)
    {
        return type.hasTurnArrow(bits);
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
}
