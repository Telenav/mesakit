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

public enum DirectionOfTrafficFlow
{
    CLOSED("0"),
    POSITIVE("+"),
    NEGATIVE("-"),
    BOTH("+/-"),
    UNKNOWN("?");

    public static DirectionOfTrafficFlow forOrdinal(int ordinal)
    {
        for (var direction : values())
        {
            if (direction.ordinal() == ordinal)
            {
                return direction;
            }
        }
        return null;
    }

    private final String symbol;

    DirectionOfTrafficFlow(String symbol)
    {
        this.symbol = symbol;
    }

    public boolean isClosed()
    {
        return equals(CLOSED);
    }

    public boolean isOneWay()
    {
        return equals(POSITIVE) || equals(NEGATIVE);
    }

    public boolean isTwoWay()
    {
        return equals(BOTH);
    }

    public DirectionOfTrafficFlow reversed()
    {
        switch (this)
        {
            case POSITIVE:
                return NEGATIVE;

            case NEGATIVE:
                return POSITIVE;

            default:
                return this;
        }
    }

    public String symbol()
    {
        return symbol;
    }
}
