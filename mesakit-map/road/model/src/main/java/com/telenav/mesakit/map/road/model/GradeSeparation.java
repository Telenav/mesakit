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

package com.telenav.mesakit.map.road.model;

import com.telenav.kivakit.core.kernel.interfaces.numeric.Quantizable;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;

public class GradeSeparation implements Quantizable
{
    public static final GradeSeparation LEVEL_MINUS_4 = new GradeSeparation(-4);

    public static final GradeSeparation LEVEL_MINUS_3 = new GradeSeparation(-3);

    public static final GradeSeparation LEVEL_MINUS_2 = new GradeSeparation(-2);

    public static final GradeSeparation LEVEL_MINUS_1 = new GradeSeparation(-1);

    public static final GradeSeparation GROUND = new GradeSeparation(0);

    public static final GradeSeparation LEVEL_1 = new GradeSeparation(1);

    public static final GradeSeparation LEVEL_2 = new GradeSeparation(2);

    public static final GradeSeparation LEVEL_3 = new GradeSeparation(3);

    public static final GradeSeparation LEVEL_4 = new GradeSeparation(4);

    public static final GradeSeparation LEVEL_5 = new GradeSeparation(5);

    public static final GradeSeparation LEVEL_6 = new GradeSeparation(6);

    public static final GradeSeparation LEVEL_7 = new GradeSeparation(7);

    public static final GradeSeparation LEVEL_8 = new GradeSeparation(8);

    public static final GradeSeparation LEVEL_9 = new GradeSeparation(9);

    public static GradeSeparation of(final int level)
    {
        switch (level)
        {
            case -4:
                return LEVEL_MINUS_4;
            case -3:
                return LEVEL_MINUS_3;
            case -2:
                return LEVEL_MINUS_2;
            case -1:
                return LEVEL_MINUS_1;
            case 0:
                return GROUND;
            case 1:
                return LEVEL_1;
            case 2:
                return LEVEL_2;
            case 3:
                return LEVEL_3;
            case 4:
                return LEVEL_4;
            case 5:
                return LEVEL_5;
            case 6:
                return LEVEL_6;
            case 7:
                return LEVEL_7;
            case 8:
                return LEVEL_8;
            case 9:
                return LEVEL_9;
            default:
                return fail("Grade separation level " + level + " is not valid");
        }
    }

    private final int level;

    private GradeSeparation(final int level)
    {
        this.level = level;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof GradeSeparation)
        {
            final var that = (GradeSeparation) object;
            return level == that.level;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(level);
    }

    public GradeSeparation higher()
    {
        return of(level + 1);
    }

    public boolean isGreaterThan(final GradeSeparation that)
    {
        return level > that.level;
    }

    public boolean isGround()
    {
        return equals(GROUND);
    }

    public boolean isHigherThan(final GradeSeparation that)
    {
        return level > that.level;
    }

    public boolean isLowerThan(final GradeSeparation that)
    {
        return level < that.level;
    }

    public int level()
    {
        return level;
    }

    public GradeSeparation lower()
    {
        return of(level - 1);
    }

    public GradeSeparation maximum(final GradeSeparation that)
    {
        return isGreaterThan(that) ? this : that;
    }

    @Override
    public long quantum()
    {
        return level;
    }

    @Override
    public String toString()
    {
        return Integer.toString(level);
    }
}
