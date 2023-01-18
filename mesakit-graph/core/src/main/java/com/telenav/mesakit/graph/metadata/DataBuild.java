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

package com.telenav.mesakit.graph.metadata;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.conversion.core.time.kivakit.KivaKitLocalDateTimeConverter;
import com.telenav.kivakit.core.language.Hash;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.time.Day;
import com.telenav.kivakit.core.time.Hour;
import com.telenav.kivakit.core.time.LocalTime;
import com.telenav.kivakit.core.time.Minute;
import com.telenav.kivakit.core.time.Month;
import com.telenav.kivakit.core.time.Year;
import com.telenav.kivakit.resource.FileName;

import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;
import static com.telenav.kivakit.core.time.Second.second;

/**
 * A {@link DataBuild} is of the form: [year.month.day_hour.minute.meridiem_zone], for example "2015.09.23_4.01PM_PST"
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("SpellCheckingInspection")
public class DataBuild
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static DataBuild at(LocalTime time)
    {
        return new DataBuild(time);
    }

    public static DataBuild now()
    {
        return at(LocalTime.now());
    }

    public static DataBuild parse(String string)
    {
        var time = new KivaKitLocalDateTimeConverter(LOGGER).convert(string);
        return time == null ? null : new DataBuild(time);
    }

    public static class Converter extends BaseStringConverter<DataBuild>
    {
        public Converter(Listener listener)
        {
            super(listener, DataBuild.class);
        }

        @Override
        protected DataBuild onToValue(String date)
        {
            return parse(date);
        }
    }

    private Year year;

    private Month month;

    private Day day;

    private Hour hour;

    private Minute minute;

    private String zoneId;

    public DataBuild()
    {
    }

    public DataBuild(DataBuild that)
    {
        year = that.year;
        month = that.month;
        day = that.day;
        hour = that.hour;
        minute = that.minute;
        zoneId = that.zoneId;
    }

    public DataBuild(LocalTime time)
    {
        year = time.year();
        month = time.month();
        day = time.dayOfMonth();
        hour = time.hourOfDay();
        minute = time.minute();
        zoneId = time.timeZone().getId().trim();
    }

    public FileName asFileName()
    {
        return FileName.kivakitFileName(localTime());
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof DataBuild that)
        {
            return year == that.year
                && month == that.month
                && day == that.day
                && hour == that.hour
                && minute == that.minute
                && zoneId.equals(that.zoneId);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.hashMany(year, month, day, hour, minute, zoneId);
    }

    public LocalTime localTime()
    {
        return LocalTime.localTime(zone(), year, month, day, hour, minute, second(0));
    }

    @Override
    public String toString()
    {
        assertValid();
        return String.format("%d.%02d.%02d_%02d.%02d%s_%s",
            year.asUnits(),
            month.monthOfYear(),
            day.asUnits(),
            hour.asMeridiemHour(),
            minute.asUnits(),
            hour.meridiem().name().toUpperCase(),
            ZoneId.of(zoneId).getDisplayName(TextStyle.SHORT, Locale.getDefault()));
    }

    public LocalTime utcTime()
    {
        return localTime().inTimeZone(LocalTime.utcTimeZone());
    }

    public DataBuild withDay(Day day)
    {
        ensureNotNull(day);
        var build = new DataBuild(this);
        build.day = day;
        return build;
    }

    public DataBuild withHour(Hour hour)
    {
        ensureNotNull(hour);
        var build = new DataBuild(this);
        build.hour = hour;
        return build;
    }

    public DataBuild withMinute(Minute minute)
    {
        ensureNotNull(minute);
        var build = new DataBuild(this);
        build.minute = minute;
        return build;
    }

    public DataBuild withMonth(Month month)
    {
        ensureNotNull(month);
        var build = new DataBuild(this);
        build.month = month;
        return build;
    }

    public DataBuild withTimeZone(ZoneId zone)
    {
        var build = new DataBuild(this);
        build.zoneId = zone.getId().trim();
        return build;
    }

    public DataBuild withYear(Year year)
    {
        ensureNotNull(year);
        var build = new DataBuild(this);
        build.year = year;
        return build;
    }

    private void assertValid()
    {
        ensureNotNull(year);
        ensureNotNull(month);
        ensureNotNull(day);
        ensureNotNull(hour);
        ensureNotNull(minute);
        ensureNotNull(zoneId);
    }

    private ZoneId zone()
    {
        return ZoneId.of(zoneId);
    }
}
