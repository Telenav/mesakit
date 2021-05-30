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


package com.telenav.tdk.graph.traffic.historical;

import com.telenav.tdk.core.collections.primitive.iteration.ByteIterator;
import com.telenav.tdk.core.kernel.language.object.Hash;
import com.telenav.tdk.core.kernel.language.string.StringList;
import com.telenav.tdk.core.kernel.scalars.counts.Maximum;
import com.telenav.tdk.core.kernel.scalars.levels.Percentage;
import com.telenav.tdk.core.kernel.time.LocalTime;
import com.telenav.tdk.map.measurements.Speed;
import com.telenav.tdk.utilities.time.timebin.LocalTimeBinScheme;
import com.telenav.tdk.utilities.time.timebin.schemes.UniformTimeBinScheme;

/**
 * Speed pattern represents historical traffic speeds at different times of the day, over the course of a week.
 */
public abstract class SpeedPattern
{
    public static final Speed maximumSpeedProfileSpeed = Speed.milesPerHour(100);

    /** The time bin type (i.e. NAS20 or 15MIN etc.) for this speed profile */
    private final LocalTimeBinScheme timeBinType;

    /** The min speed of this speed profile */
    private Byte minSpeed;

    /** The max speed of this speed profile */
    private Byte maxSpeed;

    /** The min acceleration of this speed profile, in miles/hour^2 */
    private Byte minAcceleration;

    /** The max acceleration of this speed profile, in miles/hour^2 */
    private Byte maxAcceleration;

    private Integer hashCode;

    /**
     * Default constructor
     *
     * @param timeBinType The time bin type of the speed profile (15MIN for instance)
     */
    protected SpeedPattern(final LocalTimeBinScheme timeBinType)
    {
        this.timeBinType = timeBinType;
        if (!(this.timeBinType instanceof UniformTimeBinScheme))
        {
            throw new IllegalArgumentException(
                    "Cannot create a Historical Model Speed Profile if the time bin type is not uniform");
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof SpeedPattern)
        {
            final var that = (SpeedPattern) other;
            if (numberOfTimeBins() == that.numberOfTimeBins())
            {
                for (var i = 0; i < numberOfTimeBins(); i++)
                {
                    if (speedForTimeBinNumberAsByte(i) != that.speedForTimeBinNumberAsByte(i))
                    {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public LocalTimeBinScheme getTimeBinScheme()
    {
        return timeBinType;
    }

    @Override
    public int hashCode()
    {
        if (hashCode == null)
        {
            hashCode = Hash.many(new ByteIterator()
            {
                int i;

                @Override
                public boolean hasNext()
                {
                    return i < numberOfTimeBins();
                }

                @Override
                public byte next()
                {
                    final var result = speedForTimeBinNumberAsByte(i);
                    i++;
                    return result;
                }
            });
        }
        return hashCode;
    }

    public byte maxAccelerationInMilesPerSquareHour()
    {
        if (maxAcceleration == null)
        {
            minAccelerationInMilesPerSquareHour();
        }
        return maxAcceleration;
    }

    public byte maxSpeedInMilesPerHour()
    {
        if (maxSpeed == null)
        {
            minSpeedInMilesPerHour();
        }
        return maxSpeed;
    }

    @SuppressWarnings("UnusedReturnValue")
    public byte minAccelerationInMilesPerSquareHour()
    {
        if (minAcceleration == null)
        {
            minAcceleration = Byte.MAX_VALUE;
            maxAcceleration = Byte.MIN_VALUE;
            for (var i = 0; i < numberOfTimeBins() - 4; i++)
            {
                final var acceleration = (byte) (speedForTimeBinNumberAsByte(i + 4)
                        - speedForTimeBinNumberAsByte(i));
                if (acceleration < minAcceleration)
                {
                    minAcceleration = acceleration;
                }
                if (acceleration > maxAcceleration)
                {
                    maxAcceleration = acceleration;
                }
            }
        }
        return minAcceleration;
    }

    @SuppressWarnings("UnusedReturnValue")
    public byte minSpeedInMilesPerHour()
    {
        if (minSpeed == null)
        {
            minSpeed = Byte.MAX_VALUE;
            maxSpeed = Byte.MIN_VALUE;
            for (var i = 0; i < numberOfTimeBins(); i++)
            {
                final var speed = speedForTimeBinNumberAsByte(i);
                if (speed < minSpeed)
                {
                    minSpeed = speed;
                }
                if (speed > maxSpeed)
                {
                    maxSpeed = speed;
                }
            }
        }
        return minSpeed;
    }

    /**
     * The number of time bins in this speed profile
     */
    public abstract int numberOfTimeBins();

    public Speed speed(final LocalTime time, final Speed reference)
    {
        return speedForTimeBinNumber(timeBinType.timeBin(time).timeBinNumber(), reference);
    }

    /**
     * @param timeBinNumber The time bin number at which the speed is requested
     * @return The {@link Speed} for the given time bin number
     */
    public Speed speedForTimeBinNumber(final int timeBinNumber, final Speed reference)
    {
        return reference.scale(new Percentage(speedForTimeBinNumberAsByte(timeBinNumber)).asLevel());
    }

    /**
     * @return A csv list of the speeds in MPH
     */
    public String speedsAsText()
    {
        final var builder = new StringBuilder();
        final var speeds = new StringList(Maximum.of(numberOfTimeBins()));
        for (var i = 0; i < numberOfTimeBins(); i++)
        {
            speeds.add(String.valueOf(speedForTimeBinNumberAsByte(i)));
        }
        builder.append(speeds.join(","));
        return builder.toString();
    }

    /**
     * Checks the type of speed profile to an other by comparing the number of time bins
     *
     * @param that The speed profile to compare it to
     */
    protected void checkTypeWith(final SpeedPattern that)
    {
        if (!timeBinType.equals(that.getTimeBinScheme())
                && numberOfTimeBins() != that.numberOfTimeBins())
        {
            throw new IllegalArgumentException(String.format(
                    "Cannot cluster a speed profile of type %s and %d bins with a speed profile of type %s and %d bins",
                    timeBinType.name(), numberOfTimeBins(), that.getTimeBinScheme().name(),
                    that.numberOfTimeBins()));
        }
    }

    /**
     * Return the proper speed at a proper index
     */
    protected abstract byte speedForTimeBinNumberAsByte(final int timeBinNumber);
}
