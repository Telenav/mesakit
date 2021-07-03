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

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.time.LocalTime;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;

import java.util.regex.Pattern;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.ensure;

/**
 * A {@link DataVersion} is of the form: 2001Q1
 *
 * @author jonathanl (shibo)
 */
public class DataVersion
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static DataVersion parse(final String string)
    {
        // Parse strings like 2020Q4
        final var matcher = Pattern.compile("(?<year>20\\d\\d)"
                + "Q(?<quarter>[1-4])").matcher(string);

        if (matcher.find())
        {
            return new DataVersion()
                    .withYear(Integer.parseInt(matcher.group("year")))
                    .withQuarter(Integer.parseInt(matcher.group("quarter")));
        }

        return null;
    }

    public static SwitchParser.Builder<DataVersion> switchParser()
    {
        return switchParser("data-version", "The data version such as 2020Q4");
    }

    public static SwitchParser.Builder<DataVersion> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(DataVersion.class)
                .name(name)
                .converter(new Converter(LOGGER))
                .description(description);
    }

    public static class Converter extends BaseStringConverter<DataVersion>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected DataVersion onToValue(final String date)
        {
            final var version = parse(date);
            if (version == null)
            {
                problem("Data version '" + date + "' is not valid.");
            }
            return version;
        }
    }

    private int year;

    private int quarter;

    public DataVersion()
    {
    }

    public DataVersion(final DataVersion that)
    {
        this.year = that.year;
        this.quarter = that.quarter;
    }

    public DataVersion(final LocalTime time)
    {
        this.year = time.year();
        this.quarter = time.quarter();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof DataVersion)
        {
            final var that = (DataVersion) object;
            return this.year == that.year && this.quarter == that.quarter;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(this.year, this.quarter);
    }

    public boolean isValid()
    {
        return this.year >= 1970 && this.year <= 2100
                && this.quarter >= 1 && this.quarter <= 4;
    }

    @Override
    public String toString()
    {
        ensure(isValid());
        return this.year + "Q" + this.quarter;
    }

    public DataVersion withQuarter(final int quarter)
    {
        ensure(quarter >= 1 && quarter <= 4);
        final var version = new DataVersion(this);
        version.quarter = quarter;
        return version;
    }

    public DataVersion withYear(final int year)
    {
        ensure(year >= 1970 && year <= 2100);
        final var version = new DataVersion(this);
        version.year = year;
        return version;
    }
}
