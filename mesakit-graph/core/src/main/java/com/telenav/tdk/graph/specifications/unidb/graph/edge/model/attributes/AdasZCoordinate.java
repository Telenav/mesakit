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

public class AdasZCoordinate implements Quantizable
{
    public static AdasZCoordinate centimeters(final int centimeters)
    {
        if (centimeters > -500_00 && centimeters < 6_100_00)
        {
            return new AdasZCoordinate(centimeters);
        }
        return null;
    }

    public static AdasZCoordinate meters(final double meters)
    {
        if (meters > -500.0 && meters < 6_100.0)
        {
            return centimeters((int) (meters * 100.0));
        }
        return null;
    }

    private final int centimeters;

    private AdasZCoordinate(final int centimeters)
    {
        this.centimeters = centimeters;
    }

    public int centimeters()
    {
        return centimeters;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof AdasZCoordinate)
        {
            final var that = (AdasZCoordinate) object;
            return centimeters == that.centimeters;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(centimeters);
    }

    public double meters()
    {
        return centimeters * 100.0;
    }

    @Override
    public long quantum()
    {
        return centimeters;
    }

    @Override
    public String toString()
    {
        return Integer.toString(centimeters);
    }
}
