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

package com.telenav.mesakit.navigation.routing.cost;

import com.telenav.kivakit.core.language.primitive.Doubles;

import com.telenav.kivakit.core.value.level.Weight;

/**
 * The cost of an edge or route
 *
 * @author jonathanl (shibo)
 */
public class Cost implements Comparable<Cost>
{
    public static final Cost ZERO = new Cost(0.0);

    public static final Cost MAXIMUM = new Cost(Integer.MAX_VALUE);

    /**
     * Returns a cost of 1.0 minus the given cost value
     */
    public static Cost inverse(double value)
    {
        return new Cost(1.0 - value);
    }

    /**
     * Returns the given cost between 0.0 and 1.0, inclusive
     */
    public static Cost of(double value)
    {
        return new Cost(value);
    }

    // The cost, from 0 to MAXIMUM, inclusive
    private final double cost;

    private Cost(double cost)
    {
        this.cost = cost;
    }

    public Cost add(Cost that)
    {
        return new Cost(Math.min(MAXIMUM.asDouble(), cost + that.cost));
    }

    public double asDouble()
    {
        return cost;
    }

    @Override
    public int compareTo(Cost that)
    {
        return Double.compare(cost, that.cost);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Cost)
        {
            var that = (Cost) object;
            return cost == that.cost;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(cost);
    }

    public boolean isGreaterThan(Cost that)
    {
        return cost > that.cost;
    }

    public boolean isLessThan(Cost that)
    {
        return cost < that.cost;
    }

    public boolean isMaximum()
    {
        return equals(MAXIMUM);
    }

    public Cost maximum(Cost that)
    {
        return cost > that.cost ? this : that;
    }

    public Cost minimum(Cost that)
    {
        return cost < that.cost ? this : that;
    }

    public Cost times(double multiplier)
    {
        return new Cost(asDouble() * multiplier);
    }

    @Override
    public String toString()
    {
        return toString(4);
    }

    public String toString(int places)
    {
        return Doubles.formatDouble(asDouble(), places);
    }

    public Cost weighted(Weight weight)
    {
        return new Cost(cost * weight.asZeroToOne());
    }
}
