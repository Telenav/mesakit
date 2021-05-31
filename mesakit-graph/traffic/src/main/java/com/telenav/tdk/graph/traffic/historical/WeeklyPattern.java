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

package com.telenav.kivakit.graph.traffic.historical;

import com.telenav.kivakit.utilities.time.timebin.LocalTimeBinScheme;

public class WeeklyPattern extends SpeedPattern
{
    private final SpeedPatternStore store;

    private final int[] dailyPatternIds;

    private Integer numberOfTimeBins;

    public WeeklyPattern(final LocalTimeBinScheme timeBinType, final int[] dailyPatternIds,
                         final SpeedPatternStore store)
    {
        super(timeBinType);
        this.dailyPatternIds = dailyPatternIds;
        this.store = store;
    }

    @Override
    public int numberOfTimeBins()
    {
        if (this.numberOfTimeBins == null)
        {
            this.numberOfTimeBins = this.getTimeBinScheme().numberOfTimeBins();
        }
        return this.numberOfTimeBins;
    }

    protected int[] dailyPatternIds()
    {
        return this.dailyPatternIds;
    }

    @Override
    protected byte speedForTimeBinNumberAsByte(final int timeBinNumber)
    {
        final var timeBinNumberOfDay = (numberOfTimeBins() / 7);
        final var day = timeBinNumber / timeBinNumberOfDay;

        final var dailyPattern = this.store.dailyPattern(this.dailyPatternIds[day]);
        return dailyPattern.speedForTimeBinNumberAsByte(timeBinNumber % timeBinNumberOfDay);
    }
}
