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

package com.telenav.kivakit.graph;

import java.util.*;

/**
 * @author chghe
 */
public enum EdgeRelationMemberRole
{
    EMPTY(0, "empty"),
    NEGATIVE_ONE(1, "-1"),
    NEGATIVE_TWO(2, "-2"),
    ZERO(3, "0"),
    ONE(4, "1"),
    TWO(5, "2"),
    THREE(6, "3"),
    AL(7, "al"),
    BUILDING(8, "building"),
    CARTO(9, "carto"),
    CF(10, "cf"),
    GP(11, "gp"),
    PART(12, "part"),
    POI(13, "poi"),
    RG(14, "rg"),
    SC(15, "sc"),
    BACKWARD(16, "backward"),
    COUNTRY(17, "country"),
    FORWARD(18, "forward"),
    FROM(19, "from"),
    GATE(20, "gate"),
    INNER(21, "inner"),
    ORDER1(22, "order1"),
    ORDER2(23, "order2"),
    ORDER8(24, "order8"),
    OUTER(25, "outer"),
    TO(26, "to"),
    TOLL_BOOTH(27, "toll_booth"),
    VARIABLE_SPEED_SIGN(28, "variable_speed_sign"),
    VIA(29, "via");

    private static final Map<String, EdgeRelationMemberRole> valueToRole = new HashMap<>();

    private static final Map<Integer, EdgeRelationMemberRole> codeToRole = new HashMap<>();

    static
    {
        for (final var role : values())
        {
            codeToRole.put(role.code(), role);
            valueToRole.put(role.value().toLowerCase(), role);
        }
    }

    public static EdgeRelationMemberRole of(final int code)
    {
        return codeToRole.get(code);
    }

    public static EdgeRelationMemberRole of(final String value)
    {
        if (value != null)
        {
            return valueToRole.get(value.toLowerCase());
        }
        return null;
    }

    private final int code;

    private final String value;

    EdgeRelationMemberRole(final int code, final String value)
    {
        this.code = code;
        this.value = value;
    }

    public int code()
    {
        return code;
    }

    public String value()
    {
        return value;
    }
}
