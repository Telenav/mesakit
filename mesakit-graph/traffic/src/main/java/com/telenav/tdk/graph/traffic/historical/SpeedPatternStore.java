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

import com.telenav.tdk.core.kernel.logging.*;
import com.telenav.tdk.core.kernel.time.LocalTime;
import com.telenav.tdk.core.resource.Resource;
import com.telenav.tdk.core.resource.compression.archive.ZipArchive;
import com.telenav.tdk.core.resource.resources.streamed.OutputResource;
import com.telenav.tdk.map.measurements.Speed;

import java.io.*;

public class SpeedPatternStore
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final String ZIP_ENTRY_KEY = "speed_pattern";

    public static SpeedPatternStore load(final Resource source)
    {
        final var resource = new SpeedPatternResource(LOGGER, source);
        return resource.read();
    }

    public static SpeedPatternStore load(final ZipArchive zip)
    {
        final var entry = zip.entry(ZIP_ENTRY_KEY);
        if (entry != null)
        {
            return load(entry);
        }
        return null;
    }

    private final DailyPattern[] dailyPatterns;

    private final WeeklyPattern[] weeklyPatterns;

    SpeedPatternStore(final DailyPattern[] dailyPatterns, final WeeklyPattern[] weeklyPatterns)
    {
        this.dailyPatterns = dailyPatterns;
        this.weeklyPatterns = weeklyPatterns;
    }

    public void saveTo(final Resource source)
    {
        final var resource = new SpeedPatternResource(LOGGER, source);
        resource.write(this);
    }

    public void saveTo(final ZipArchive zip)
    {
        zip.saveEntry("speed_pattern", out ->
        {
            // Wrap the ZipArchive OutputStream to prevent it from being closed by
            // SpeedPatternResource#write
            final var resource = new SpeedPatternResource(LOGGER, new OutputResource(new OutputStream()
            {
                @Override
                public void flush() throws IOException
                {
                    out.flush();
                }

                @Override
                public void write(final int value) throws IOException
                {
                    out.write(value);
                }
            }));
            resource.write(SpeedPatternStore.this);
        });
    }

    public Speed speed(final SpeedPatternIdentifier identifier, final Speed reference, final LocalTime time)
    {
        final var pattern = weeklyPattern(identifier);
        if (pattern != null && reference != null)
        {
            return pattern.speed(time, reference);
        }
        return null;
    }

    public SpeedPattern weeklyPattern(final SpeedPatternIdentifier identifier)
    {
        if (identifier == null || identifier.asInteger() < 0 || identifier.asInteger() >= weeklyPatterns.length)
        {
            return null;
        }
        return weeklyPatterns[identifier.asInteger()];
    }

    SpeedPattern dailyPattern(final int index)
    {
        return dailyPatterns[index];
    }

    DailyPattern[] dailyPatterns()
    {
        return dailyPatterns;
    }

    WeeklyPattern[] weeklyPatterns()
    {
        return weeklyPatterns;
    }
}
