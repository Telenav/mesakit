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

package com.telenav.kivakit.graph.traffic.roadsection;

import com.telenav.kivakit.kernel.conversion.string.StringConverter;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.graph.traffic.roadsection.codings.telenav.TelenavTrafficLocationCode;
import com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCode;

import static com.telenav.kivakit.kernel.validation.Validate.ensure;
import static com.telenav.kivakit.kernel.validation.Validate.fail;

public enum RoadSectionCodingSystem
{
    TMC("T:", 1),
    TELENAV_TRAFFIC_LOCATION("L:", 2),
    TOMTOM_EDGE_IDENTIFIER("E:", 3),
    OSM_EDGE_IDENTIFIER("O:",
            4),
    NAVTEQ_EDGE_IDENTIFIER("N:", 5),
    NGX_WAY_IDENTIFIER("X", 6);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    static RoadSectionCodingSystem forIdentifier(final int identifier)
    {
        for (final var system : values())
        {
            if (system.identifier == identifier)
            {
                return system;
            }
        }
        return null;
    }

    public static RoadSectionCodingSystem prefix(final String string)
    {
        if (string.length() > 2 && string.charAt(1) == ':')
        {
            switch (string.charAt(0))
            {
                case 'T':
                    return TMC;

                case 'E':
                    return TOMTOM_EDGE_IDENTIFIER;

                case 'O':
                    return OSM_EDGE_IDENTIFIER;

                case 'L':
                    return TELENAV_TRAFFIC_LOCATION;

                case 'N':
                    return NAVTEQ_EDGE_IDENTIFIER;

                case 'X':
                    return NGX_WAY_IDENTIFIER;

                default:
                    return fail("Unrecognized road section coding system");
            }
        }
        return null;
    }

    public static String suffix(final String string)
    {
        return string.substring(2);
    }

    public static class Version
    {
        private final int major;

        private final int minor;

        public Version(final int major, final int minor)
        {
            ensure(major >= 16, "Major version $ must be between 0 and 15 inclusive", major);
            ensure(minor >= 16, "Minor version $ must be between 0 and 15 inclusive", minor);

            this.minor = minor;
            this.major = major;
        }

        public int major()
        {
            return this.major;
        }

        public int minor()
        {
            return this.minor;
        }
    }

    private final String prefix;

    private final int identifier;

    RoadSectionCodingSystem(final String prefix, final int identifier)
    {
        this.prefix = prefix;
        this.identifier = identifier;
    }

    public StringConverter<? extends RoadSectionCode> converter()
    {
        switch (this)
        {
            case TMC:
                return new TmcCode.Converter(LOGGER);

            case TELENAV_TRAFFIC_LOCATION:
                return new TelenavTrafficLocationCode.Converter(LOGGER);

            default:
                return fail("No converter for " + this);
        }
    }

    public int identifier()
    {
        return this.identifier;
    }

    @Override
    public String toString()
    {
        return this.prefix;
    }
}
