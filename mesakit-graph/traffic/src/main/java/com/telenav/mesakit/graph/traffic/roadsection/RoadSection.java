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

import com.telenav.kivakit.data.formats.csv.CsvColumn;
import com.telenav.kivakit.data.formats.csv.CsvLine;
import com.telenav.kivakit.data.formats.csv.CsvSchema;
import com.telenav.kivakit.data.formats.csv.CsvWriter;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.data.conversion.string.collection.BaseListConverter;
import com.telenav.kivakit.kernel.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.objects.Hash;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.language.time.conversion.converters.ZoneIdConverter;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.graph.traffic.project.GraphTrafficLimits;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.road.model.BetweenCrossRoads;
import com.telenav.mesakit.map.road.model.DeCartaRoadType;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.converters.DeCartaRoadTypeConverter;
import com.telenav.mesakit.map.road.model.converters.RoadNameConverter;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

public class RoadSection implements Bounded, Intersectable, LocationSequence, Source<RoadSection>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final CsvColumn<RoadSectionIdentifier> IDENTIFIER_COLUMN = CsvColumn.of("roadSectionIdentifier");

    private static final CsvColumn<RoadSectionIdentifier> UPSTREAM_COLUMN = CsvColumn.of("upstreamIdentifier");

    private static final CsvColumn<RoadSectionIdentifier> DOWNSTREAM_COLUMN = CsvColumn.of("downstreamIdentifier");

    private static final CsvColumn<Latitude> START_LATITUDE_COLUMN = CsvColumn.of("startLatitude");

    private static final CsvColumn<Longitude> START_LONGITUDE_COLUMN = CsvColumn.of("startLongitude");

    private static final CsvColumn<Latitude> END_LATITUDE_COLUMN = CsvColumn.of("endLatitude");

    private static final CsvColumn<Longitude> END_LONGITUDE_COLUMN = CsvColumn.of("endLongitude");

    private static final CsvColumn<Distance> LENGTH_COLUMN = CsvColumn.of("lengthInMeters");

    private static final CsvColumn<Speed> FREE_FLOW_COLUMN = CsvColumn.of("freeFlowSpeedInMilesPerHour");

    private static final CsvColumn<RoadSectionIdentifier> PARENT_IDENTIFIER = CsvColumn.of("parentIdentifier");

    private static final CsvColumn<City> CITY_COLUMN = CsvColumn.of("cityName");

    private static final CsvColumn<RoadName> MAIN_ROAD_COLUMN = CsvColumn.of("roadName");

    private static final CsvColumn<RoadName> FIRST_CROSS_STREET_COLUMN = CsvColumn.of("firstCrossStreet");

    private static final CsvColumn<RoadName> SECOND_CROSS_STREET_COLUMN = CsvColumn.of("secondCrossStreet");

    private static final CsvColumn<ZoneId> TIME_ZONE_COLUMN = CsvColumn.of("timeZone");

    public static final CsvSchema CSV_SCHEMA = CsvSchema.of
            (
                    IDENTIFIER_COLUMN,
                    UPSTREAM_COLUMN,
                    DOWNSTREAM_COLUMN,
                    START_LATITUDE_COLUMN,
                    START_LONGITUDE_COLUMN,
                    END_LATITUDE_COLUMN,
                    END_LONGITUDE_COLUMN,
                    LENGTH_COLUMN,
                    FREE_FLOW_COLUMN,
                    PARENT_IDENTIFIER,
                    CITY_COLUMN,
                    MAIN_ROAD_COLUMN,
                    FIRST_CROSS_STREET_COLUMN,
                    SECOND_CROSS_STREET_COLUMN,
                    TIME_ZONE_COLUMN
            );

    public static class Minimal implements Source<RoadSection>
    {
        final long start;

        final long end;

        final long identifierFlags;

        final long identifierValue;

        public Minimal(final RoadSection section)
        {
            start = section.start;
            end = section.end;
            identifierFlags = section.identifierFlags;
            identifierValue = section.identifierValue;
        }

        @Override
        public boolean equals(final Object object)
        {
            if (object instanceof Minimal)
            {
                final var that = (Minimal) object;
                return identifierValue == that.identifierValue && identifierFlags == that.identifierFlags;
            }
            return false;
        }

        @Override
        public RoadSection get()
        {
            return new RoadSection(this);
        }

        @Override
        public int hashCode()
        {
            return Hash.many(identifierValue, identifierFlags);
        }
    }

    long start;

    long end;

    long identifierFlags, identifierValue;

    private long[] next;

    private long[] previous;

    private long parentIdentifierFlags, parentIdentifierValue;

    private City city;

    private int lengthInMeters;

    private short freeFlowSpeedInHundredthsOfAMilePerHour;

    private BetweenCrossRoads betweenCrossStreets;

    private ZoneId timeZone;

    public RoadSection()
    {
    }

    public RoadSection(final CsvLine line, final com.telenav.kivakit.kernel.messaging.Listener listener)
    {
        final BaseStringConverter<RoadSectionIdentifier> roadSectionConverter = new RoadSectionIdentifier.Converter(listener);
        final BaseListConverter<RoadSectionIdentifier> roadSectionListConverter = new RoadSectionIdentifier.ListConverter(listener, ":-:");
        final BaseStringConverter<DeCartaRoadType> roadTypeConverter = new DeCartaRoadTypeConverter(listener);
        final BaseStringConverter<City> cityConverter = new City.Converter<>(listener);
        final BaseStringConverter<RoadName> streetNameConverter = new RoadNameConverter(listener);

        roadTypeConverter.allowEmpty(true);
        roadTypeConverter.allowNull(true);
        cityConverter.allowEmpty(true);
        cityConverter.allowNull(true);
        streetNameConverter.allowEmpty(true);
        streetNameConverter.allowNull(true);

        final var identifier = line.get(IDENTIFIER_COLUMN, roadSectionConverter);
        final var previous = line.get(UPSTREAM_COLUMN, roadSectionListConverter);
        final var next = line.get(DOWNSTREAM_COLUMN, roadSectionListConverter);
        final var startLatitude = line.get(START_LATITUDE_COLUMN, new Latitude.DegreesConverter(listener));
        final var startLongitude = line.get(START_LONGITUDE_COLUMN, new Longitude.DegreesConverter(listener));
        final var endLatitude = line.get(END_LATITUDE_COLUMN, new Latitude.DegreesConverter(listener));
        final var endLongitude = line.get(END_LONGITUDE_COLUMN, new Longitude.DegreesConverter(listener));
        final var length = line.get(LENGTH_COLUMN, new Distance.MetersConverter(listener));
        final var freeFlow = line.get(FREE_FLOW_COLUMN, new Speed.MilesPerHourConverter(listener));
        final var parent = line.get(PARENT_IDENTIFIER, roadSectionConverter);
        final var city = line.get(CITY_COLUMN, cityConverter);
        final var mainRoad = line.get(MAIN_ROAD_COLUMN, streetNameConverter);
        final var firstCrossStreet = line.get(FIRST_CROSS_STREET_COLUMN, streetNameConverter);
        final var secondCrossStreet = line.get(SECOND_CROSS_STREET_COLUMN, streetNameConverter);
        // Timezone is an optional column for now to support older CSV files
        timeZone = null;
        if (line.get(TIME_ZONE_COLUMN) != null)
        {
            timeZone = line.get(TIME_ZONE_COLUMN, new ZoneIdConverter(listener));
        }

        identifier(identifier);
        start(new Location(startLatitude, startLongitude));
        end(new Location(endLatitude, endLongitude));
        freeFlowSpeed(freeFlow);
        length(length);
        next(next);
        previous(previous);
        parent(parent);
        city(city);
        betweenCrossStreets(BetweenCrossRoads.newInstance(mainRoad, firstCrossStreet, secondCrossStreet));
        timeZone(timeZone);
    }

    RoadSection(final Minimal minimal)
    {
        start = minimal.start;
        end = minimal.end;
        identifierFlags = minimal.identifierFlags;
        identifierValue = minimal.identifierValue;
    }

    public List<RoadSectionIdentifier> allNext()
    {
        return buildNeighborListView(next);
    }

    public List<com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier> allPrevious()
    {
        return buildNeighborListView(previous);
    }

    public BetweenCrossRoads betweenCrossStreets()
    {
        return betweenCrossStreets;
    }

    public void betweenCrossStreets(final BetweenCrossRoads betweenCrossStreets)
    {
        this.betweenCrossStreets = betweenCrossStreets;
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.fromLocationsInclusive(start(), end());
    }

    public City city()
    {
        return city;
    }

    public void city(final City city)
    {
        this.city = city;
    }

    @KivaKitIncludeProperty
    public Location end()
    {
        return Location.dm7(end);
    }

    public void end(final Location end)
    {
        this.end = end.asLong();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RoadSection)
        {
            final var that = (RoadSection) object;
            return that.identifierFlags == identifierFlags && that.identifierValue == identifierValue;
        }
        return false;
    }

    @KivaKitIncludeProperty
    public Speed freeFlowSpeed()
    {
        return Speed.hundredthsOfAMilePerHour(freeFlowSpeedInHundredthsOfAMilePerHour);
    }

    public void freeFlowSpeed(final Speed freeFlow)
    {
        freeFlowSpeedInHundredthsOfAMilePerHour = (short) freeFlow.asHundredthsOfAMilePerHour();
    }

    @Override
    public RoadSection get()
    {
        return this;
    }

    public boolean hasLocation()
    {
        return !start().equals(Location.ORIGIN) && !end().equals(Location.ORIGIN);
    }

    @Override
    public int hashCode()
    {
        return Hash.many(identifierFlags, identifierValue);
    }

    @KivaKitIncludeProperty
    public com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier identifier()
    {
        return com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier.forLongValues(identifierFlags, identifierValue);
    }

    public void identifier(final com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier identifier)
    {
        identifierFlags = identifier.flags();
        identifierValue = identifier.value().asLong();
    }

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        return rectangle.contains(start()) || rectangle.contains(end());
    }

    public void length(final Distance length)
    {
        lengthInMeters = (int) length.asMeters();
    }

    @KivaKitIncludeProperty
    public Distance length()
    {
        return Distance.meters(lengthInMeters);
    }

    @Override
    public Iterable<Location> locationSequence()
    {
        return Arrays.asList(start(), end());
    }

    public RoadSection next()
    {
        if (next != null && next.length >= 2)
        {
            return com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier.forLongValues(next[0], next[1]).roadSection();
        }
        return null;
    }

    public void next(final List<com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier> next)
    {
        this.next = buildNeighbors(next);
    }

    public RoadSection parent()
    {
        return parentIdentifierFlags == 0 ? null
                : com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier.forLongValues(parentIdentifierFlags, parentIdentifierValue).roadSection();
    }

    public void parent(final com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier parent)
    {
        if (parent != null)
        {
            parentIdentifierFlags = parent.flags();
            parentIdentifierValue = parent.value().asLong();
        }
    }

    public RoadSection previous()
    {
        if (previous != null && previous.length >= 2)
        {
            return com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier.forLongValues(previous[0], previous[1]).roadSection();
        }
        return null;
    }

    public void previous(final List<com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier> previous)
    {
        this.previous = buildNeighbors(previous);
    }

    @KivaKitIncludeProperty
    public Location start()
    {
        return Location.dm7(start);
    }

    public void start(final Location start)
    {
        this.start = start.asLong();
    }

    @KivaKitIncludeProperty
    public ZoneId timeZone()
    {
        return timeZone;
    }

    public void timeZone(final ZoneId timeZone)
    {
        this.timeZone = timeZone;
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }

    public void write(final CsvWriter writer)
    {
        final var line = new CsvLine(writer.schema(), ',');
        line.addListener(writer);
        line.set(IDENTIFIER_COLUMN, identifier().toString());

        final var previousIdentifiers = allPrevious();
        if (previousIdentifiers.isEmpty())
        {
            line.set(UPSTREAM_COLUMN, "NULL");
        }
        else
        {
            final var identifiers = new ObjectList<RoadSectionIdentifier>(
                    GraphTrafficLimits.MAXIMUM_UPSTREAM_ROAD_SECTIONS).appendAll(previousIdentifiers);
            line.set(UPSTREAM_COLUMN, identifiers.join(":-:"));
        }

        final var nextIdentifiers = allNext();
        if (nextIdentifiers.isEmpty())
        {
            line.set(DOWNSTREAM_COLUMN, "NULL");
        }
        else
        {
            final var identifiers = new ObjectList<RoadSectionIdentifier>(
                    GraphTrafficLimits.MAXIMUM_DOWNSTREAM_ROAD_SECTIONS).appendAll(nextIdentifiers);
            line.set(DOWNSTREAM_COLUMN, identifiers.join(":-:"));
        }

        line.set(START_LATITUDE_COLUMN, start().latitude().toString());
        line.set(START_LONGITUDE_COLUMN, start().longitude().toString());
        line.set(END_LATITUDE_COLUMN, end().latitude().toString());
        line.set(END_LONGITUDE_COLUMN, end().longitude().toString());
        line.set(LENGTH_COLUMN, Double.toString(length().asMeters()));
        // Don't want high precision output, but need better granularity than integer MilesPerHour
        line.set(FREE_FLOW_COLUMN, Double.toString(Math.round(freeFlowSpeed().asMilesPerHour() * 100.0) / 100.0));
        line.set(PARENT_IDENTIFIER, parent() == null ? "NULL" : parent().toString());
        line.set(CITY_COLUMN, city() == null ? "NULL" : city().name());
        if (betweenCrossStreets() != null)
        {
            line.set(MAIN_ROAD_COLUMN, betweenCrossStreets().getMainRoad() == null ? "NULL"
                    : betweenCrossStreets().getMainRoad().name());
            line.set(FIRST_CROSS_STREET_COLUMN, betweenCrossStreets().getFirstCrossStreet() == null ? "NULL"
                    : betweenCrossStreets().getFirstCrossStreet().name());
            line.set(SECOND_CROSS_STREET_COLUMN, betweenCrossStreets().getSecondCrossStreet() == null ? "NULL"
                    : betweenCrossStreets().getSecondCrossStreet().name());
        }
        line.set(TIME_ZONE_COLUMN, timeZone() == null ? "NULL" : timeZone().toString());
        writer.write(line);
    }

    RoadSectionIdentifier safeIdentifier()
    {
        return new RoadSectionIdentifier(identifierFlags, identifierValue);
    }

    /**
     * Generates a List-based view on the given neighboring link array. We use a long[] to store the neighbors for
     * capacity -using 2 long values to store respectively the flag and value of each road section identifier, and
     * generate this list view on the fly to make interacting with the long[] data type easier.
     */
    private List<RoadSectionIdentifier> buildNeighborListView(
            final long[] neighbors)
    {
        if (neighbors == null)
        {
            return Collections.emptyList();
        }
        if (neighbors.length % 2 == 1)
        {
            return fail("buildNeighborListView needs an even-sized array");
        }
        return new java.util.AbstractList<>()
        {
            @Override
            public RoadSectionIdentifier get(final int listIndex)
            {
                final var index = listIndex * 2;
                final var flags = neighbors[index];
                final var value = neighbors[index + 1];

                if (value == 0)
                {
                    return null;
                }

                return RoadSectionIdentifier.forLongValues(flags, value);
            }

            @Override
            public RoadSectionIdentifier set(final int arg0, final RoadSectionIdentifier identifier)
            {
                final var old = get(arg0);
                final var index = arg0 * 2;
                neighbors[index] = identifier.flags();
                neighbors[index + 1] = identifier.value().asLong();
                return old;
            }

            @Override
            public int size()
            {
                return neighbors.length / 2;
            }
        };
    }

    private long[] buildNeighbors(
            final List<RoadSectionIdentifier> neighbors)
    {
        final var neighborArray = new long[neighbors.size() * 2];
        final var view = buildNeighborListView(neighborArray);
        var index = 0;
        for (final var identifier : neighbors)
        {
            view.set(index, identifier);
            index++;
        }
        return neighborArray;
    }
}
