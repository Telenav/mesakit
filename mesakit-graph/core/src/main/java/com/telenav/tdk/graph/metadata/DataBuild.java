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

package com.telenav.kivakit.graph.metadata;

import com.telenav.kivakit.kernel.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.object.Hash;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.kernel.time.*;
import com.telenav.kivakit.kernel.time.conversion.converters.LocalDateTimeConverter;
import com.telenav.kivakit.resource.path.FileName;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

/**
 * A {@link DataBuild} is of the form: [year.month.day_hour.minute.meridiem_zone], for example "2015.09.23_4.01PM_PST"
 *
 * @author jonathanl (shibo)
 */
public class DataBuild
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static DataBuild at(final LocalTime time)
    {
        return new DataBuild(time);
    }

    public static DataBuild now()
    {
        return at(LocalTime.now());
    }

    public static DataBuild parse(final String string)
    {
        final var time = new LocalDateTimeConverter(LOGGER).convert(string);
        return time == null ? null : new DataBuild(time);
    }

    public static class Converter extends BaseStringConverter<DataBuild>
    {
        public Converter(final Listener<Message> listener)
        {
            super(listener);
        }

        @Override
        protected DataBuild onConvertToObject(final String date)
        {
            return parse(date);
        }
    }

    private int year;

    private int month;

    private int day;

    private int hour;

    private int minute;

    private Meridiem ampm;

    private String zoneId;

    public DataBuild()
    {
    }

    public DataBuild(final DataBuild that)
    {
        year = that.year;
        month = that.month;
        day = that.day;
        hour = that.hour;
        minute = that.minute;
        ampm = that.ampm;
        zoneId = that.zoneId;
    }

    public DataBuild(final LocalTime time)
    {
        year = time.year();
        month = time.month();
        day = time.day();
        hour = time.meridiemHour();
        minute = time.minute();
        ampm = time.meridiem();
        zoneId = time.timeZone().getId().trim();
    }

    public FileName asFileName()
    {
        return FileName.dateTime(localTime());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof DataBuild)
        {
            final var that = (DataBuild) object;
            return year == that.year
                    && month == that.month
                    && day == that.day
                    && hour == that.hour
                    && minute == that.minute
                    && ampm == that.ampm
                    && zoneId.equals(that.zoneId);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(year, month, day, hour, minute, ampm, zoneId);
    }

    public LocalTime localTime()
    {
        return LocalTime.of(zone(), year, month, day, hour, minute, 0, ampm);
    }

    @Override
    public String toString()
    {
        assertValid();
        return String.format("%d.%02d.%02d_%02d.%02d%s_%s",
                year, month, day, hour, minute, ampm.name().toUpperCase(),
                ZoneId.of(zoneId).getDisplayName(TextStyle.SHORT, Locale.getDefault()));
    }

    public LocalTime utcTime()
    {
        return localTime().localTime(LocalTime.utcTimeZone());
    }

    public DataBuild withDay(final int day)
    {
        ensure(day >= 1 && day <= 31);
        final var build = new DataBuild(this);
        build.day = day;
        return build;
    }

    public DataBuild withHour(final int hour)
    {
        ensure(hour >= 1 && hour <= 12);
        final var build = new DataBuild(this);
        build.hour = hour;
        return build;
    }

    public DataBuild withMeridiem(final Meridiem ampm)
    {
        final var build = new DataBuild(this);
        build.ampm = ampm;
        return build;
    }

    public DataBuild withMinute(final int minute)
    {
        ensure(minute >= 0 && minute <= 59);
        final var build = new DataBuild(this);
        build.minute = minute;
        return build;
    }

    public DataBuild withMonth(final int month)
    {
        ensure(month >= 1 && month <= 12);
        final var build = new DataBuild(this);
        build.month = month;
        return build;
    }

    public DataBuild withTimeZone(final ZoneId zone)
    {
        final var build = new DataBuild(this);
        build.zoneId = zone.getId().trim();
        return build;
    }

    public DataBuild withYear(final int year)
    {
        ensure(year >= 1970 && year <= 2100);
        final var build = new DataBuild(this);
        build.year = year;
        return build;
    }

    private void assertValid()
    {
        assert year >= 1970 && year <= 2100 : "Year " + year + " is not valid";
        assert month >= 1 && month <= 12 : "Month " + month + " is not valid";
        assert day >= 1 && day <= 31 : "Day " + day + " is not valid";
        assert hour >= 0 && hour <= 12 : "Hour " + hour + " is not valid";
        assert minute >= 0 && minute <= 59 : "Minute " + minute + " is not valid";
        ensure(ampm != null);
        ensure(zoneId != null);
    }

    private ZoneId zone()
    {
        return ZoneId.of(zoneId);
    }
}
