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

public enum RoadState implements Quantizable
{
    NULL(0),
    ONE_WAY(1),
    TWO_WAY(2),
    CLOSED(3);

    public static RoadState forIdentifier(final int identifier)
    {
        switch (identifier)
        {
            case 0:
                return NULL;

            case 1:
                return ONE_WAY;

            case 2:
                return TWO_WAY;

            case 3:
                return CLOSED;

            default:
                assert false : "Invalid road state " + identifier;
                return null;
        }
    }

    /**
     * @return The identifier for the given state or NULL.identifier if the state is null
     */
    public static int identifierFor(final RoadState state)
    {
        if (state == null)
        {
            return NULL.identifier;
        }
        return state.identifier();
    }

    private final int identifier;

    RoadState(final int identifier)
    {
        this.identifier = identifier;
    }

    public int identifier()
    {
        return identifier;
    }

    public boolean isClosed()
    {
        return this == CLOSED;
    }

    public boolean isOneWay()
    {
        return this == ONE_WAY;
    }

    public boolean isOpen()
    {
        return this == ONE_WAY || this == TWO_WAY;
    }

    public boolean isTwoWay()
    {
        return this == TWO_WAY;
    }

    @Override
    public long quantum()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return super.toString() + " (" + identifier + ")";
    }
}
