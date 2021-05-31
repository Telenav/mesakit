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
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.scalars.counts.Estimate;
import com.telenav.kivakit.resource.Resource;
import com.telenav.kivakit.resource.path.Extension;
import com.telenav.kivakit.resource.store.BinaryObjectStore;
import com.telenav.kivakit.utilities.time.timebin.schemes.FifteenMinutesTimeBinScheme;
import com.telenav.kivakit.utilities.time.timebin.schemes.FiveMinutesTimeBinScheme;
import com.telenav.kivakit.utilities.time.timebin.schemes.UniformTimeBinScheme;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;
import static com.telenav.kivakit.kernel.validation.Validate.fail;

/**
 * Read or write speed pattern data from/to a resource. The format of speed pattern data please refer:
 * http://spaces.telenav.com:8080/display/syse/Delta+Historic+Traffic+Format
 */
public class SpeedPatternResource extends BinaryObjectStore<SpeedPatternStore>
{
    public static final Extension EXTENSION = new Extension(".speed_pattern.bin.gz");

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public SpeedPatternResource(final Listener listener, final Resource source)
    {
        super(listener, source);
    }

    @Override
    protected SpeedPatternStore onRead()
    {
        // The size of the time bin in minutes -- UNSIGNED
        final var timeBinSize = readInteger(8);

        // The number of bins in each profile -- UNSIGNED
        final var numberTimeBins = readInteger(16);

        // The number of profiles -- UNSIGNED
        final var numberProfiles = readInteger(32);
        final var profilesBits = numberProfiles > 0x0000FFFF ? 32 : 16;

        final var dailyPatterns = new DailyPattern[numberProfiles];

        // The number of weekly to daily profile mappings -- UNSIGNED
        final var numberWeeklyDailyMappings = readInteger(32);
        final var weeklyPatterns = new WeeklyPattern[numberWeeklyDailyMappings];

        final var store = new SpeedPatternStore(dailyPatterns, weeklyPatterns);

        DEBUG.trace("Will read ${debug} weekly IDs and ${debug} daily profiles", numberWeeklyDailyMappings,
                numberProfiles);

        final var timeBinScheme = timeBinSize == 15 ? new FifteenMinutesTimeBinScheme()
                : timeBinSize == 5 ? new FiveMinutesTimeBinScheme() : new UniformTimeBinScheme()
        {
            @Override
            public int timeBinSizeInMinutes()
            {
                return timeBinSize;
            }
        };

        // All the weekly to daily profile mappings
        for (var i = 0; i < numberWeeklyDailyMappings; i++)
        {
            final var dailyPatternIds = new int[7];
            for (var day = 0; day < 7; day++)
            {
                // Monday to Sunday
                dailyPatternIds[day] = readInteger(profilesBits);
            }
            weeklyPatterns[i] = new WeeklyPattern(timeBinScheme, dailyPatternIds, store);
        }
        // All the speed profiles
        for (var i = 0; i < numberProfiles; i++)
        {
            final var percentages = new ByteArray("percentages");
            percentages.initialSize(Estimate._128);
            percentages.initialize();

            for (var timeBinNumber = 0; timeBinNumber < numberTimeBins; timeBinNumber++)
            {
                // The speed percentage -- UNSIGNED
                final var percentageAsInt = readInteger(8);
                final var percentage = (byte) percentageAsInt;
                if (percentageAsInt <= 100)
                {
                    percentages.add(percentage);
                }
                else
                {
                    return fail("File Corrupted. One percentage is " + percentageAsInt);
                }
            }
            final var pattern = new DailyPattern(timeBinScheme, percentages);
            dailyPatterns[i] = pattern;
        }

        return store;
    }

    @Override
    protected void onWrite(final SpeedPatternStore store)
    {
        final var timeBinScheme = store.dailyPattern(0).getTimeBinScheme();
        final UniformTimeBinScheme uniformTimeBinScheme;
        if (timeBinScheme instanceof UniformTimeBinScheme)
        {
            uniformTimeBinScheme = (UniformTimeBinScheme) timeBinScheme;
        }
        else
        {
            fail("Cannot compress a speed profile if the time bin scheme is not uniform");
            return;
        }
        final var numberWeeklyPatterns = store.weeklyPatterns().length;
        final var numberDailyPatterns = store.dailyPatterns().length;
        final var numberTimeBins = timeBinScheme.numberOfTimeBins() / 7;
        final var timeBinSize = uniformTimeBinScheme.timeBinSizeInMinutes();

        final var profilesBits = numberDailyPatterns > 0x0000FFFF ? 32 : 16;

        DEBUG.trace("Will write ${debug} weekly IDs and ${debug} daily profiles", numberWeeklyPatterns,
                numberDailyPatterns);

        // The size of the time bin in minutes -- UNSIGNED
        write(timeBinSize, 8);

        // The number of bins in each profile -- UNSIGNED
        write(numberTimeBins, 16);

        // The number of profiles -- UNSIGNED
        write(numberDailyPatterns, 32);

        // The number of weekly to daily profile mappings -- UNSIGNED
        write(numberWeeklyPatterns, 32);

        // All the weekly to daily profile mappings
        for (final var pattern : store.weeklyPatterns())
        {
            for (var day = 0; day < 7; day++)
            {
                // Monday to Sunday
                write(pattern.dailyPatternIds()[day], profilesBits);
            }
        }
        // All the speed profiles
        for (final var pattern : store.dailyPatterns())
        {
            for (var timeBinNumber = 0; timeBinNumber < numberTimeBins; timeBinNumber++)
            {
                final var percentage = pattern.percentageForTimeBinNumberAsByte(timeBinNumber);
                ensure(percentage >= 0, "The database is corrupted, a percentage" + percentage + " was read");
                write(percentage, 8);
            }
        }
    }
}
