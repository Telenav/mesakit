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

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public enum LaneDivider implements Quantizable
{
    NONE(0),
    LONG_DASHED(1),
    DOUBLE_SOLID(2),
    SINGLE_SOLID(3),
    COMBINATION_OF_SINGLE_SOLID_AND_DASHED(4),
    COMBINATION_OF_DASHED_AND_SOLID(5),
    SHORT_DASHED(6),
    SHADED_AREA_MARKING(7),
    DASHED_BLOCKS(8),
    PHYSICAL_DIVIDER(9),
    DOUBLE_DASHED(10),
    NO_DIVIDER_MARKER(11),
    CROSSING_ALERT(12),
    CENTER_TURN_LANE(13),
    TOLL_BOOTH(15);

    public static long NULL = 0;

    public static LaneDivider forIdentifier(final int identifier)
    {
        for (final var divider : values())
        {
            if (divider.identifier == identifier)
            {
                return divider;
            }
        }
        ensure(false);
        return null;
    }

    private final int identifier;

    LaneDivider(final int identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public long quantum()
    {
        return identifier;
    }
}
