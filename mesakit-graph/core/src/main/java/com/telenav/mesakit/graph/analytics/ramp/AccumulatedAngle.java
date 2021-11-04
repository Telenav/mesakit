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

package com.telenav.mesakit.graph.analytics.ramp;

import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import com.telenav.mesakit.map.measurements.geographic.Heading;

import static com.telenav.mesakit.map.measurements.geographic.Angle.Chirality;
import static com.telenav.mesakit.map.measurements.geographic.Angle.MAXIMUM;
import static com.telenav.mesakit.map.measurements.geographic.Angle._0_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle._180_DEGREES;
import static com.telenav.mesakit.map.measurements.geographic.Angle._MINUS_180_DEGREES;

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
    static AccumulatedAngle curveAngle(Edge edge)
    {
        var curveAngle = new AccumulatedAngle(0);
        Heading beginHeading = null;
        for (var segment : edge.roadShape().segments())
        {
            if (beginHeading == null)
            {
                beginHeading = segment.heading();
            }
            else
            {
                var endHeading = segment.heading();
                curveAngle.accumulate(beginHeading.difference(endHeading, Chirality.CLOCKWISE));
                beginHeading = endHeading;
            }
        }
        return curveAngle;
    }

    /** the accumulated degrees */
    private double degrees;

    AccumulatedAngle(Angle angle)
    {
        this(0);
        accumulate(angle);
    }

    AccumulatedAngle(double degrees)
    {
        this.degrees = degrees;
    }

    @SuppressWarnings("UnusedReturnValue")
    AccumulatedAngle accumulate(AccumulatedAngle that)
    {
        degrees += that.degrees;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    AccumulatedAngle accumulate(Angle angle)
    {
        if (angle.isGreaterThan(_0_DEGREES))
        {
            if (angle.isLessThan(_180_DEGREES))
            {
                degrees += angle.asDegrees();
            }
            else
            {
                degrees -= MAXIMUM.minus(angle).asDegrees();
            }
        }
        else
        {
            if (angle.isGreaterThan(_MINUS_180_DEGREES))
            {
                degrees += angle.asDegrees();
            }
            else
            {
                degrees += MAXIMUM.plus(angle).asDegrees();
            }
        }
        return this;
    }

    /**
     * Compares the abstract degrees of this and that accumulate angles.
     */
    @SuppressWarnings("SameParameterValue")
    boolean isGreaterThan(AccumulatedAngle that)
    {
        return Math.abs(degrees) > Math.abs(that.degrees);
    }
}
