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

package com.telenav.mesakit.graph.library.osm.change;

import org.openstreetmap.osmosis.core.domain.common.TimestampContainer;
import org.openstreetmap.osmosis.core.domain.common.TimestampFormat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Represents a time in OSM style ISO format.
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("UseOfObsoleteDateTimeApi")
public class PbfTimestamp implements TimestampContainer
{
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    private final Date date;

    public PbfTimestamp()
    {
        this(new Date());
    }

    public PbfTimestamp(Date date)
    {
        this.date = date;
    }

    public PbfTimestamp(long time)
    {
        this(new Date(time));
    }

    @Override
    public String getFormattedTimestamp(TimestampFormat timestampFormat)
    {
        return timestampFormat.formatTimestamp(date);
    }

    @Override
    public Date getTimestamp()
    {
        return date;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return formatter.format(Instant.ofEpochMilli(date.getTime()));
    }
}
