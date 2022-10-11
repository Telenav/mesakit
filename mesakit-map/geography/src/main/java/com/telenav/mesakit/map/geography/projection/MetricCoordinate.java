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

package com.telenav.mesakit.map.geography.projection;

import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.language.reflection.property.IncludeProperty;

/**
 * An x,y cartesian coordinate in meters from the origin
 *
 * @author jonathanl (shibo)
 */
public class MetricCoordinate
{
    @IncludeProperty
    final
    double xInMeters;

    @IncludeProperty
    final
    double yInMeters;

    public MetricCoordinate(double xInMeters, double yInMeters)
    {
        this.xInMeters = xInMeters;
        this.yInMeters = yInMeters;
    }

    public boolean isCloseTo(MetricCoordinate that)
    {
        return Math.abs(xInMeters - that.xInMeters) < 1.0 &&
                Math.abs(yInMeters - that.yInMeters) < 1.0;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public double xInMeters()
    {
        return xInMeters;
    }

    public double yInMeters()
    {
        return yInMeters;
    }
}
