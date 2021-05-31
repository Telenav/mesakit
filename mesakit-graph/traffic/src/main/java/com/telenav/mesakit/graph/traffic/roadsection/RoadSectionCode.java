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

package com.telenav.mesakit.graph.traffic.roadsection;

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.conversion.string.collection.BaseListConverter;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.traffic.roadsection.codings.navteq.NavteqRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.ngx.NgxRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.osm.PbfRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.telenav.TelenavTrafficLocationCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tomtom.TomTomRoadSectionCode;

import java.util.Objects;

/**
 * Abstraction that indicates a section of roadway in some coding system. Subclasses include {@link TmcCode}, {@link
 * TelenavTrafficLocationCode}, {@link TomTomRoadSectionCode}, and {@link PbfRoadSectionCode}
 *
 * @author jonathanl (shibo)
 */
public abstract class RoadSectionCode
{
    public static class Converter extends BaseStringConverter<RoadSectionCode>
    {
        public Converter(final Listener listener)
        {
            super(listener);
        }

        @Override
        protected RoadSectionCode onConvertToObject(final String value)
        {
            // Validate for a road section coding system
            final var system = com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem.prefix(value);
            if (system != null)
            {
                final var code = com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem.suffix(value);
                switch (system)
                {
                    case TMC:
                        return TmcCode.forCode(code);

                    case OSM_EDGE_IDENTIFIER:
                        return new PbfRoadSectionCode(Long.parseLong(code));

                    case TOMTOM_EDGE_IDENTIFIER:
                        return new TomTomRoadSectionCode(Long.parseLong(code));

                    case NAVTEQ_EDGE_IDENTIFIER:
                        return new NavteqRoadSectionCode(Long.parseLong(code));

                    case TELENAV_TRAFFIC_LOCATION:
                        return new TelenavTrafficLocationCode(Long.parseLong(code));

                    case NGX_WAY_IDENTIFIER:
                        return new NgxRoadSectionCode(Long.parseLong(code));

                    default:
                        return fail("Invalid road section coding system");
                }
            }

            // No coding system was specified, so try to convert to a TMC code
            final var tmc = TmcCode.forCode(value);
            // If it's not a TMC code, convert to a legacy edge identifier
            return Objects.requireNonNullElseGet(tmc, () -> new TomTomRoadSectionCode(Integer.parseInt(value)));
        }
    }

    public static class ListConverter extends BaseListConverter<RoadSectionCode>
    {
        public ListConverter(final Listener listener)
        {
            super(listener, new Converter(listener), ",");
        }

        public ListConverter(final Listener listener, final String delimiter)
        {
            super(listener, new Converter(listener), delimiter);
        }
    }

    public com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier asIdentifier()
    {
        return asIdentifier(true);
    }

    public abstract com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier asIdentifier(
            boolean lookupDatabase);

    public abstract String code();

    public abstract com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem codingSystem();

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RoadSectionCode)
        {
            final var that = (RoadSectionCode) object;
            return code().equals(that.code());
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return code().hashCode();
    }

    public RoadSection roadSection()
    {
        return asIdentifier().roadSection();
    }

    @Override
    public final String toString()
    {
        return codingSystem() + code();
    }
}
