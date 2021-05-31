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

package com.telenav.mesakit.graph.traffic.roadsection.codings.tmc;

import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCode;

public class TmcCode extends RoadSectionCode
{
    private static final Logger LOGGER = com.telenav.kivakit.kernel.logging.LoggerFactory.newLogger();

    private static final BoundedConcurrentMap<String, TmcCode> codes = new BoundedConcurrentMap<>(
            KivaKitGraphTrafficLimits.MAXIMUM_TMC_CODES)
    {
        @Override
        protected TmcCode onInitialize(final String code)
        {
            return new TmcCode(code);
        }
    };

    private static final com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCodeParser parser = new com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCodeParser();

    private static boolean cacheLocked;

    public static TmcCode forCode(final String code)
    {
        if (cacheLocked)
        {
            return new TmcCode(code);
        }
        else
        {
            return codes.getOrCreate(code);
        }
    }

    public static TmcCode forLong(final long code)
    {
        return new TmcCode.FromLongConverter(LOGGER).convert(code);
    }

    public static boolean isTmcCode(final String code)
    {
        if (codes.containsKey(code))
        {
            return true;
        }
        else
        {
            return parser.parse(code) != null;
        }
    }

    public static void lockCache()
    {
        cacheLocked = true;
    }

    public static class Converter extends com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter<TmcCode>
    {
        public Converter(final com.telenav.kivakit.kernel.messaging.Listener listener)
        {
            super(listener);
        }

        @Override
        protected TmcCode onConvertToObject(final String value)
        {
            return (value == null || !isTmcCode(value)) ? null : forCode(value.trim());
        }
    }

    public static class FromLongConverter extends com.telenav.kivakit.kernel.data.conversion.BaseConverter<Long, TmcCode>
    {
        public static final int REGION_MASK = 100, DIRECTION_MASK = 10, LOCATION_MASK = 100000;

        public enum TmcDirection
        {
            MINUS('-', 0),
            PLUS('+', 1),
            NEGATIVE('N', 2),
            POSITIVE('P', 3);

            public static TmcDirection fromChar(final char character)
            {
                for (final var direction : values())
                {
                    if (direction.character == character)
                    {
                        return direction;
                    }
                }
                return fail("Invalid direction character: " + character);
            }

            public static TmcDirection fromInt(final int integerRepresentation)
            {
                for (final var direction : values())
                {
                    if (direction.integerRepresentation == integerRepresentation)
                    {
                        return direction;
                    }
                }
                return fail("Invalid direction identifier: " + integerRepresentation);
            }

            public final char character;

            public final int integerRepresentation;

            TmcDirection(final char character, final int integerRepresentation)
            {
                this.character = character;
                this.integerRepresentation = integerRepresentation;
            }

            public boolean sameDirectionAs(final TmcDirection that)
            {
                return integerRepresentation % 2 == that.integerRepresentation % 2;
            }
        }

        public FromLongConverter(final com.telenav.kivakit.kernel.messaging.Listener listener)
        {
            super(listener);
        }

        public int countryField(final long value)
        {
            return (int) (value / (LOCATION_MASK * DIRECTION_MASK * REGION_MASK));
        }

        public TmcDirection directionField(final long value)
        {
            return TmcDirection.fromInt((int) (value / LOCATION_MASK) % DIRECTION_MASK);
        }

        public int locationField(final long value)
        {
            return (int) value % LOCATION_MASK;
        }

        public int regionField(final long value)
        {
            return (int) (value / (LOCATION_MASK * DIRECTION_MASK)) % REGION_MASK;
        }

        @Override
        protected TmcCode onConvert(final Long value)
        {
            final var location = locationField(value);

            final var direction = directionField(value).character;

            final var region = regionField(value);

            final var country = countryCharFromInt(countryField(value));

            final var tmc = country + com.telenav.kivakit.kernel.language.strings.Strings.alignRight(String.valueOf(region), 2, '0') + direction
                    + com.telenav.kivakit.kernel.language.strings.Strings.alignRight(String.valueOf(location), 5, '0');
            return forCode(tmc);
        }

        private char countryCharFromInt(final int countryCode)
        {
            if (countryCode >= 0 && countryCode <= 9)
            {
                return String.valueOf(countryCode).charAt(0);
            }
            else if (countryCode >= 10)
            {
                return (char) ('A' + countryCode - 10);
            }
            fail("Unknown country identifier: " + countryCode);
            return (char) -1;
        }
    }

    public static class ListConverter extends com.telenav.kivakit.kernel.data.conversion.string.collection.BaseListConverter<TmcCode>
    {
        public ListConverter(final com.telenav.kivakit.kernel.messaging.Listener listener)
        {
            super(listener, new Converter(listener), ":");
        }
    }

    public static class ToLongConverter extends com.telenav.kivakit.kernel.data.conversion.BaseConverter<TmcCode, Long>
    {
        private static final com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCodeParser parser = new com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCodeParser();

        public ToLongConverter(final com.telenav.kivakit.kernel.messaging.Listener listener)
        {
            super(listener);
        }

        @Override
        protected Long onConvert(final TmcCode tmc)
        {
            final var code = tmc.code();
            final var value = parser.parse(code);
            if (value == null)
            {
                problem("Unknown TMC code ${debug}", code);
            }
            return value;
        }
    }

    private final String code;

    private transient long identifier;

    private TmcCode(final String code)
    {
        if (parser.parse(code) == null)
        {
            fail("Invalid TMC code format: '$'", code);
        }
        this.code = code;
    }

    @Override
    public com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier asIdentifier(
            final boolean lookupDatabase)
    {
        if (identifier == 0)
        {
            final var converted = new ToLongConverter(LOGGER).convert(this);
            identifier = converted == null ? -1 : converted;
        }

        return com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier.forCodingSystemAndIdentifier(com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCodingSystem.TMC, identifier,
                lookupDatabase);
    }

    @Override
    public String code()
    {
        return code;
    }

    @Override
    public final com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCodingSystem codingSystem()
    {
        return com.telenav.kivakit.graph.traffic.roadsection.RoadSectionCodingSystem.TMC;
    }
}
