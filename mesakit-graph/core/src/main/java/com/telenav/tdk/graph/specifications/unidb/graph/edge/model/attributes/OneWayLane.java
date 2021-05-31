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

public class OneWayLane implements Quantizable
{
    public static long NULL = 0;

    public static OneWayLane parse(final String value)
    {
        switch (value.trim())
        {
            case "yes":
                return new OneWayLane(Direction.FORWARD);
            case "-1":
                return new OneWayLane(Direction.BACKWARD);
            case "no":
                return new OneWayLane(Direction.NONE);
            default:
                return null;
        }
    }

    public enum Direction
    {
        NONE(0),
        FORWARD(1),
        BACKWARD(2),
        UNKNOWN(3);

        static Direction forIdentifier(final int identifier)
        {
            switch (identifier)
            {
                case 0:
                case 1:
                    return FORWARD;

                case 2:
                    return BACKWARD;

                case 3:
                    return UNKNOWN;

                default:
                    ensure(false, "Invalid OneWayLane.Direction identifier $", identifier);
            }
            return null;
        }

        private final int identifier;

        Direction(final int identifier)
        {
            this.identifier = identifier;
        }

        public int identifier()
        {
            return identifier;
        }
    }

    private final Direction direction;

    public OneWayLane(final Direction direction)
    {
        this.direction = direction;
    }

    public OneWayLane(final int identifier)
    {
        direction = Direction.forIdentifier(identifier);
    }

    public Direction direction()
    {
        return direction;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof OneWayLane)
        {
            final var that = (OneWayLane) object;
            return direction == that.direction;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return direction.hashCode();
    }

    @Override
    public long quantum()
    {
        return direction.ordinal();
    }

    public OneWayLane reversed()
    {
        return new OneWayLane(direction == Direction.FORWARD ? Direction.BACKWARD : Direction.FORWARD);
    }

    @Override
    public String toString()
    {
        return direction.name();
    }
}
