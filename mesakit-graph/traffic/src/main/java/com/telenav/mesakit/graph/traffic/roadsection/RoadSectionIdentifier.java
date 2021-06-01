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

import com.telenav.kivakit.configuration.lookup.Lookup;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.conversion.string.collection.BaseListConverter;
import com.telenav.kivakit.kernel.data.conversion.string.primitive.HexadecimalLongConverter;
import com.telenav.kivakit.kernel.data.conversion.string.primitive.LongConverter;
import com.telenav.kivakit.kernel.language.bits.BitDiagram;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.language.values.identifier.Identifier;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.traffic.roadsection.codings.navteq.NavteqRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.ngx.NgxRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.osm.PbfRoadSectionCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tomtom.TomTomRoadSectionCode;
import com.telenav.mesakit.map.road.model.DeCartaRoadType;
import com.telenav.mesakit.map.road.model.DirectionOfTrafficFlow;
import com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;
import static com.telenav.kivakit.kernel.language.bits.BitDiagram.BitField;

/**
 * Identifiers should have the following format and be stored in two 64 bit long values:
 *
 * <pre>
 *
 * 1. flags = [ ???????? ???????? ???????? ???????? ????SSSS VVVVvvvv ZZZZLLLL ?RCDTTTT ]
 *
 *     ? = unused
 *     S = map coding system (TMC, OSM, edge id, link, etc.)
 *     V = major version
 *     v = minor version
 *     Z = zoom level (TMDB feature set id as per Ron's email about TMDB)
 *     L = lane count
 *     R = ramp
 *     C = connector
 *     D = direction of travel value (0 = negative, 1 = positive relative to a predefined direction)
 *     T = road type
 *
 * 2. value = [ XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX XXXXXXXX ]
 *
 *     X = unique value within coding system
 * </pre>
 *
 * @author jonathanl (shibo)
 */
public final class RoadSectionIdentifier
{
    public static final RoadSectionIdentifier NULL = new RoadSectionIdentifier(0, 0);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    // The bit diagram for road section identifiers
    private static final BitDiagram DIAGRAM = new BitDiagram(
            "???????? ???????? ???????? ????????" + "????SSSS VVVVvvvv ZZZZLLLL ?RCDTTTT");

    // Bit fields from the bit diagram
    private static final BitField CODING_SYSTEM = DIAGRAM.field('S');

    private static final BitField MAJOR_VERSION = DIAGRAM.field('V');

    private static final BitField MINOR_VERSION = DIAGRAM.field('v');

    private static final BitField ROAD_TYPE = DIAGRAM.field('T');

    private static final BitField LANE_COUNT = DIAGRAM.field('L');

    private static final BitField RAMP = DIAGRAM.field('R');

    private static final BitField CONNECTOR = DIAGRAM.field('C');

    private static final BitField DIRECTION_OF_TRAFFIC_FLOW = DIAGRAM.field('D');

    private static final BitField ZOOM_LEVEL = DIAGRAM.field('Z');

    public static RoadSectionIdentifier forCodingSystemAndIdentifier(final RoadSectionCodingSystem system,
                                                                     final long value)
    {
        return forCodingSystemAndIdentifier(system, value, true);
    }

    public static RoadSectionIdentifier forCodingSystemAndIdentifier(final RoadSectionCodingSystem system,
                                                                     final long value, final boolean lookupDatabase)
    {
        return forLongValues(CODING_SYSTEM.set(0, system.identifier()), value, lookupDatabase);
    }

    public static RoadSectionIdentifier forLongValues(final long flags, final long value)
    {
        return forLongValues(flags, value, true);
    }

    public static RoadSectionIdentifier forLongValues(final long flags, final long value, final boolean lookupDatabase)
    {
        // If there are no bits other than CODING_SYSTEM set,
        if (lookupDatabase && (flags & ~CODING_SYSTEM.mask()) == 0)
        {
            // then the identifier needs to be looked up in the database to resolve the other flags
            final var roadSection = new RoadSectionIdentifier(flags, value).roadSection();
            if (roadSection != null)
            {
                // NOTE: We don't want to call identifier() here because that can cause infinite
                // recursion for TMC codes that aren't in the road section database (because the
                // flags will never get resolved from the database lookup and the first if statement
                // in this method will keep evaluating to true, causing another lookup).
                return roadSection.safeIdentifier();
            }
        }
        return new RoadSectionIdentifier(flags, value);
    }

    public static RoadSectionIdentifier nullForCodingSystem(final RoadSectionCodingSystem system)
    {
        return forCodingSystemAndIdentifier(system, 0, false);
    }

    public static class Converter extends BaseStringConverter<RoadSectionIdentifier>
    {
        private final LongConverter longConverter;

        private final HexadecimalLongConverter hexadecimalLongConverter;

        public Converter(final Listener listener)
        {
            super(listener);
            longConverter = new LongConverter(listener);
            hexadecimalLongConverter = new HexadecimalLongConverter(listener);
        }

        @Override
        protected RoadSectionIdentifier onConvertToObject(String value)
        {
            // Handles case of multiple road section identifiers by simply taking the first
            final var index = value.indexOf(' ');
            if (index > 0)
            {
                value = value.substring(0, index);
            }
            if ("null".equalsIgnoreCase(value) || Strings.isEmpty(value))
            {
                return null;
            }
            final var values = StringList.split(Maximum._3, value, ":");
            // RoadSectionIdentifier flags should be hexadecimal, but older Road Section databases
            // stored the flags as base 10 longs. Handling both cases here.
            final Long a;
            if (values.get(0).startsWith("0x"))
            {
                a = hexadecimalLongConverter.convert(values.get(0));
            }
            else
            {
                a = longConverter.convert(values.get(0));
            }
            final var b = longConverter.convert(values.get(1));
            return (a == null || b == null) ? null : forLongValues(a, b);
        }
    }

    public static class FlagsBuilder
    {
        private long currentFlags;

        public void codingSystem(final RoadSectionCodingSystem codingSystem)
        {
            set(CODING_SYSTEM, codingSystem.identifier());
        }

        public void connector(final boolean isConnector)
        {
            set(CONNECTOR, isConnector);
        }

        public void directionOfTravel(final DirectionOfTrafficFlow direction)
        {
            set(DIRECTION_OF_TRAFFIC_FLOW, direction.ordinal());
        }

        public void flags(final long flags)
        {
            unsupported();
        }

        public void laneCount(final Count count)
        {
            set(LANE_COUNT, count.asInt());
        }

        public void ramp(final boolean isRamp)
        {
            set(RAMP, isRamp);
        }

        public void roadType(final DeCartaRoadType roadType)
        {
            set(ROAD_TYPE, roadType.type());
        }

        public void version(final RoadSectionCodingSystem.Version version)
        {
            set(MAJOR_VERSION, version.major());
            set(MINOR_VERSION, version.minor());
        }

        /**
         * Build a RoadSectionIdentifier object containing the flags currently set in the builder as well as the
         * provided road section code.
         */
        public RoadSectionIdentifier withCode(final RoadSectionCode code)
        {
            // here we build a temporary identifier with no flags apart from the coding system
            final var value = code.asIdentifier().value;
            // then we build the real identifier with all the flags
            return withValue(value);
        }

        /**
         * Build a RoadSectionIdentifier object containing the flags currently set in the builder as well as the
         * provided value.
         */
        public RoadSectionIdentifier withValue(final long value)
        {
            return new RoadSectionIdentifier(currentFlags, value);
        }

        public void zoomLevel(final ZoomLevel zoom)
        {
            set(ZOOM_LEVEL, zoom.level());
        }

        private void set(final BitField field, final boolean value)
        {
            currentFlags = field.set(currentFlags, value);
        }

        private void set(final BitField field, final long value)
        {
            currentFlags = field.set(currentFlags, value);
        }
    }

    public static class ListConverter extends BaseListConverter<RoadSectionIdentifier>
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

    private final long flags;

    private final long value;

    RoadSectionIdentifier(final long flags, final long value)
    {
        this.flags = flags;
        this.value = value;
    }

    public RoadSectionCode asCode()
    {
        if (value == 0)
        {
            return null;
        }
        switch (codingSystem())
        {
            case TMC:
                return TmcCode.forLong(value);

            case TOMTOM_EDGE_IDENTIFIER:
                return new TomTomRoadSectionCode(value);

            case NAVTEQ_EDGE_IDENTIFIER:
                return new NavteqRoadSectionCode(value);

            case OSM_EDGE_IDENTIFIER:
                return new PbfRoadSectionCode(value);

            case NGX_WAY_IDENTIFIER:
                return new NgxRoadSectionCode(value);

            default:
                return unsupported();
        }
    }

    public RoadSectionCodingSystem codingSystem()
    {
        return RoadSectionCodingSystem.forIdentifier(CODING_SYSTEM.extractInt(flags));
    }

    public DirectionOfTrafficFlow directionOfTrafficFlow()
    {
        return DirectionOfTrafficFlow.forOrdinal(DIRECTION_OF_TRAFFIC_FLOW.extractInt(flags));
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RoadSectionIdentifier)
        {
            final var that = (RoadSectionIdentifier) object;
            return CODING_SYSTEM.extractInt(flags) == CODING_SYSTEM.extractInt(that.flags)
                    && value == that.value;
        }
        return false;
    }

    public boolean existsInRoadSectionDatabase()
    {
        return roadSectionDatabase().exists(this);
    }

    public long flags()
    {
        return flags;
    }

    @Override
    public int hashCode()
    {
        return Hash.many(CODING_SYSTEM.extractInt(flags), value);
    }

    public boolean isConnector()
    {
        return CONNECTOR.extractBoolean(flags);
    }

    public boolean isNull()
    {
        return equals(NULL) || value == 0;
    }

    public boolean isRamp()
    {
        return RAMP.extractBoolean(flags);
    }

    /**
     * This method is a HEURISTIC ONLY, relying on the naming conventions of TMC segments to recover partial
     * connectivity. It should not return any false positives, however false negatives are guaranteed to occur. In other
     * words, only a positive answer is relatively reliable, and only with a PRECONDITION that the underlying edges are
     * geometrically connected. Otherwise, no logical connectivity is possible anyway. This logic was ported from
     * Toffee's TmcUtil class.
     */
    public boolean isUTurnSuspected(final RoadSectionIdentifier that)
    {
        if (codingSystem() != RoadSectionCodingSystem.TMC
                || that.codingSystem() != RoadSectionCodingSystem.TMC)
        {
            return false;
        }
        final var longConverter = new TmcCode.FromLongConverter(LOGGER);
        final long thisLong = value().asLong(), thatLong = that.value().asLong();

        if (longConverter.countryField(thisLong) != longConverter.countryField(thatLong)
                || longConverter.regionField(thisLong) != longConverter.regionField(thatLong))
        {
            return false;
        }

        if (Math.abs(longConverter.locationField(thisLong) - longConverter.locationField(thatLong)) > 1)
        {
            return false;
        }

        return longConverter.directionField(thisLong).sameDirectionAs(longConverter.directionField(thatLong));
    }

    public Count laneCount()
    {
        return Count.count(LANE_COUNT.extractInt(flags));
    }

    /**
     * @return This road section identifier with the direction reversed
     */
    public RoadSectionIdentifier reversed()
    {
        final var flags = flags();
        final var value = DIRECTION_OF_TRAFFIC_FLOW.set(this.value, directionOfTrafficFlow().reversed().ordinal());
        return new RoadSectionIdentifier(flags, value);
    }

    public RoadSection roadSection()
    {
        return roadSectionDatabase().roadSectionForIdentifier(this);
    }

    public DeCartaRoadType roadType()
    {
        return DeCartaRoadType.forType(ROAD_TYPE.extractInt(flags));
    }

    // Provide a base 10 long representation of the flags
    public String toLongValuesString()
    {
        return flags + ":" + value;
    }

    @Override
    public String toString()
    {
        return "0x" + Long.toHexString(flags) + ":" + value;
    }

    public Identifier value()
    {
        return new Identifier(value);
    }

    public RoadSectionCodingSystem.Version version()
    {
        return new RoadSectionCodingSystem.Version(MAJOR_VERSION.extractInt(flags),
                MINOR_VERSION.extractInt(flags));
    }

    public ZoomLevel zoomLevel()
    {
        return ZoomLevel.telenav(ZOOM_LEVEL.extractInt(flags));
    }

    private RoadSectionDatabase roadSectionDatabase()
    {
        final var database = Lookup.global().locate(RoadSectionDatabase.class);
        if (database == null)
        {
            unsupported("Road section database is not available");
        }
        return database;
    }
}
