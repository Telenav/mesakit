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

package com.telenav.kivakit.graph.traffic.roadsection.codings.telenav;

import com.telenav.kivakit.kernel.conversion.collection.BaseListConverter;
import com.telenav.kivakit.kernel.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.map.BoundedConcurrentMap;
import com.telenav.kivakit.kernel.language.primitive.Longs;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.Message;
import com.telenav.kivakit.graph.traffic.project.KivaKitGraphTrafficLimits;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCode;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;

public class TelenavTrafficLocationCode extends RoadSectionCode
{
    private static final BoundedConcurrentMap<String, TelenavTrafficLocationCode> codes = new BoundedConcurrentMap<>(
            KivaKitGraphTrafficLimits.MAXIMUM_TTL_CODES)
    {
        @Override
        protected TelenavTrafficLocationCode onInitialize(final String code)
        {
            return new TelenavTrafficLocationCode(code);
        }
    };

    private static boolean cacheLocked;

    public static TelenavTrafficLocationCode forCode(final int code)
    {
        if (cacheLocked)
        {
            return new TelenavTrafficLocationCode(code);
        }
        else
        {
            return codes.getOrCreate(code);
        }
    }

    public static void lockCache()
    {
        cacheLocked = true;
    }

    public static class Converter extends BaseStringConverter<TelenavTrafficLocationCode>
    {
        public Converter(final Listener<Message> listener)
        {
            super(listener);
        }

        @Override
        protected TelenavTrafficLocationCode onConvertToObject(final String value)
        {
            return value == null ? null : new TelenavTrafficLocationCode(Long.parseLong(value));
        }
    }

    public static class ListConverter extends BaseListConverter<TelenavTrafficLocationCode>
    {
        public ListConverter(final Listener<Message> listener)
        {
            super(listener, new Converter(listener), ":");
        }

        public ListConverter(final String delimiter, final Listener<Message> listener)
        {
            super(listener, new Converter(listener), delimiter);
        }
    }

    private final long code;

    public TelenavTrafficLocationCode(final long code)
    {
        this.code = code;
    }

    public TelenavTrafficLocationCode(final String code)
    {
        this.code = Longs.parse(code, Long.MIN_VALUE);
        ensure(this.code != Long.MIN_VALUE);
    }

    @Override
    public RoadSectionIdentifier asIdentifier(final boolean lookupDatabase)
    {
        return RoadSectionIdentifier.forCodingSystemAndIdentifier(codingSystem(), code, lookupDatabase);
    }

    @Override
    public String code()
    {
        return Long.toString(code);
    }

    @Override
    public final RoadSectionCodingSystem codingSystem()
    {
        return RoadSectionCodingSystem.TELENAV_TRAFFIC_LOCATION;
    }
}
