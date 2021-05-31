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

package com.telenav.mesakit.graph.traffic.historical;

import com.telenav.kivakit.collections.primitive.array.scalars.ByteArray;
import com.telenav.kivakit.utilities.time.timebin.LocalTimeBinScheme;

public class DailyPattern extends SpeedPattern
{
    private final ByteArray percentages;

    private Integer numberOfTimeBins;

    public DailyPattern(final LocalTimeBinScheme timeBinScheme, final ByteArray percentages)
    {
        super(timeBinScheme);
        if (percentages.size() != timeBinScheme.numberOfTimeBins() / 7)
        {
            throw new IllegalArgumentException(String.format("Expected a ByteArray with %d values, got one with %d",
                    timeBinScheme.numberOfTimeBins() / 7, percentages.size()));
        }
        this.percentages = percentages;
    }

    @Override
    public int numberOfTimeBins()
    {
        if (this.numberOfTimeBins == null)
        {
            this.numberOfTimeBins = this.getTimeBinScheme().numberOfTimeBins() / 7;
        }
        return this.numberOfTimeBins;
    }

    public byte percentageForTimeBinNumberAsByte(final int timeBinNumber)
    {
        if (timeBinNumber < this.numberOfTimeBins())
        {
            return this.percentages.get(timeBinNumber);
        }
        throw new IllegalArgumentException(
                String.format("Cannot retrieve a percentage of speed for index %d when the maximum index is %d",
                        timeBinNumber, this.numberOfTimeBins()));
    }

    public ByteArray percentages()
    {
        return this.percentages;
    }

    @Override
    public String toString()
    {
        return "DailySpeedPattern [percentages=" + this.percentages + "]";
    }

    @Override
    protected byte speedForTimeBinNumberAsByte(final int timeBinNumber)
    {
        return this.percentageForTimeBinNumberAsByte(timeBinNumber);
    }
}
