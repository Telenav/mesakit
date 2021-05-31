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

package com.telenav.mesakit.graph.specifications.unidb.graph.edge.model.attributes;

import com.telenav.kivakit.kernel.interfaces.numeric.Quantizable;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.string.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.language.string.formatting.ObjectFormatter.Format;
import com.telenav.mesakit.map.measurements.geographic.Distance;

public class AdasCurvature implements Quantizable
{
    public static int NULL = 1023;

    public static final AdasCurvature UNDEFINED = new AdasCurvature(Distance.ZERO, Direction.LEFT);

    public enum Direction
    {
        LEFT,
        RIGHT;

        public Direction reversed()
        {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    @KivaKitIncludeProperty
    private Distance radius;

    @KivaKitIncludeProperty
    private Direction sign;

    @KivaKitIncludeProperty
    private Integer curvature;

    public AdasCurvature(final Distance radius, final Direction sign)
    {
        this.radius = radius;
        this.sign = sign;
    }

    public AdasCurvature(final int curvature)
    {
        assert curvature >= 0;
        assert curvature <= 1022;
        this.curvature = curvature;
    }

    public Double asMeters()
    {
        return radius().asMeters() * (sign() == Direction.RIGHT ? 1 : -1);
    }

    public int curvature()
    {
        if (curvature == null)
        {
            curvature = asMeters().intValue();
        }
        return curvature;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof AdasCurvature)
        {
            final var that = (AdasCurvature) object;
            return curvature() == that.curvature();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Integer.hashCode(curvature());
    }

    @Override
    public long quantum()
    {
        return curvature();
    }

    public Distance radius()
    {
        if (radius == null)
        {
            radius = Distance.meters(Math.abs(curvature()));
        }
        return radius;
    }

    public AdasCurvature reversed()
    {
        return new AdasCurvature(radius, sign.reversed());
    }

    public Direction sign()
    {
        if (sign == null)
        {
            sign = curvature < 0 ? Direction.LEFT : Direction.RIGHT;
        }
        return sign;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString(Format.SINGLE_LINE);
    }
}
