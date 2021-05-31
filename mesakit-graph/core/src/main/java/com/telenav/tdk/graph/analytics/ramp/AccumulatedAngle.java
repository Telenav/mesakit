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

package com.telenav.kivakit.graph.analytics.ramp;

import com.telenav.kivakit.graph.Edge;
import com.telenav.kivakit.map.measurements.Angle;
import com.telenav.kivakit.map.measurements.Angle.Chirality;
import com.telenav.kivakit.map.measurements.Heading;

/**
 * This class accumulates angles without bounds. The angles within 180 degrees counterclockwise are all minus, while the
 * ones within 180 degrees clockwise are all positive.
 *
 * @author ranl
 */
class AccumulatedAngle
{
    static final AccumulatedAngle ZERO = new AccumulatedAngle(0);

    static final AccumulatedAngle _100__8Y = new AccumulatedAngle(180);

    /**
     * Gets the curve angle by accumulating angles between all connected segments of the edge.
     */
    static AccumulatedAngle curveAngle(final Edge edge)
    {
        final var curveAngle = new AccumulatedAngle(0);
        Heading beginHeading = null;
        for (final var segment : edge.roadShape().segments())
        {
            if (beginHeading == null)
            {
                beginHeading = segment.heading();
            }
            else
            {
                final var endHeading = segment.heading();
                curveAngle.accumulate(beginHeading.difference(endHeading, Chirality.CLOCKWISE));
                beginHeading = endHeading;
            }
        }
        return curveAngle;
    }

    /** the accumulated degrees */
    private double degrees;

    AccumulatedAngle(final Angle angle)
    {
        this(0);
        accumulate(angle);
    }

    AccumulatedAngle(final double degrees)
    {
        this.degrees = degrees;
    }

    @SuppressWarnings("UnusedReturnValue")
    AccumulatedAngle accumulate(final AccumulatedAngle that)
    {
        this.degrees += that.degrees;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    AccumulatedAngle accumulate(final Angle angle)
    {
        if (angle.isGreaterThan(Angle._0_DEGREES))
        {
            if (angle.isLessThan(Angle._180_DEGREES))
            {
                this.degrees += angle.asDegrees();
            }
            else
            {
                this.degrees -= Angle.MAXIMUM.subtract(angle).asDegrees();
            }
        }
        else
        {
            if (angle.isGreaterThan(Angle._MINUS_180_DEGREES))
            {
                this.degrees += angle.asDegrees();
            }
            else
            {
                this.degrees += Angle.MAXIMUM.add(angle).asDegrees();
            }
        }
        return this;
    }

    /**
     * Compares the abstract degrees of this and that accumulate angles.
     */
    @SuppressWarnings("SameParameterValue")
    boolean isGreaterThan(final AccumulatedAngle that)
    {
        return Math.abs(this.degrees) > Math.abs(that.degrees);
    }
}
