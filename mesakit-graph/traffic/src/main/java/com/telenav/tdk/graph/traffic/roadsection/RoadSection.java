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


package com.telenav.tdk.graph.traffic.roadsection;

import com.telenav.tdk.core.kernel.conversion.collection.BaseListConverter;
import com.telenav.tdk.core.kernel.conversion.string.BaseStringConverter;
import com.telenav.tdk.core.kernel.interfaces.object.Source;
import com.telenav.tdk.core.kernel.language.collections.list.BoundedList;
import com.telenav.tdk.core.kernel.language.collections.list.ObjectList;
import com.telenav.tdk.core.kernel.language.object.Hash;
import com.telenav.tdk.core.kernel.language.reflection.property.filters.TdkIncludeProperty;
import com.telenav.tdk.core.kernel.language.string.formatting.ObjectFormatter;
import com.telenav.tdk.core.kernel.messaging.Listener;
import com.telenav.tdk.core.kernel.messaging.Message;
import com.telenav.tdk.data.formats.library.csv.CsvColumn;
import com.telenav.tdk.data.formats.library.csv.CsvLine;
import com.telenav.tdk.data.formats.library.csv.CsvSchema;
import com.telenav.tdk.data.formats.library.csv.CsvWriter;
import com.telenav.tdk.graph.traffic.project.TdkGraphTrafficLimits;
import com.telenav.tdk.map.geography.Latitude;
import com.telenav.tdk.map.geography.Location;
import com.telenav.tdk.map.geography.LocationSequence;
import com.telenav.tdk.map.geography.Longitude;
import com.telenav.tdk.map.geography.rectangle.Bounded;
import com.telenav.tdk.map.geography.rectangle.Intersectable;
import com.telenav.tdk.map.geography.rectangle.Rectangle;
import com.telenav.tdk.map.measurements.Distance;
import com.telenav.tdk.map.measurements.Speed;
import com.telenav.tdk.map.region.City;
import com.telenav.tdk.map.road.model.BetweenCrossRoads;
import com.telenav.tdk.map.road.model.DeCartaRoadType;
import com.telenav.tdk.map.road.model.RoadName;
import com.telenav.tdk.map.road.model.converters.DeCartaRoadTypeConverter;
import com.telenav.tdk.map.road.model.converters.RoadNameConverter;
import com.telenav.tdk.utilities.time.ZoneIdConverter;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.telenav.tdk.core.kernel.validation.Validate.fail;

public class RoadSection implements Bounded, Intersectable, LocationSequence, Source<RoadSection>
{
    private static final CsvColumn IDENTIFIER_COLUMN = new CsvColumn("roadSectionIdentifier");

    private static final CsvColumn UPSTREAM_COLUMN = new CsvColumn("upstreamIdentifier");

    private static final CsvColumn DOWNSTREAM_COLUMN = new CsvColumn("downstreamIdentifier");

    private static final CsvColumn START_LATITUDE_COLUMN = new CsvColumn("startLatitude");

    private static final CsvColumn START_LONGITUDE_COLUMN = new CsvColumn("startLongitude");

    private static final CsvColumn END_LATITUDE_COLUMN = new CsvColumn("endLatitude");

    private static final CsvColumn END_LONGITUDE_COLUMN = new CsvColumn("endLongitude");

    private static final CsvColumn LENGTH_COLUMN = new CsvColumn("lengthInMeters");

    private static final CsvColumn FREE_FLOW_COLUMN = new CsvColumn("freeFlowSpeedInMilesPerHour");

    private static final CsvColumn PARENT_IDENTIFIER = new CsvColumn("parentIdentifier");

    private static final CsvColumn CITY_COLUMN = new CsvColumn("cityName");

    private static final CsvColumn MAIN_ROAD_COLUMN = new CsvColumn("roadName");

    private static final CsvColumn FIRST_CROSS_STREET_COLUMN = new CsvColumn("firstCrossStreet");

    private static final CsvColumn SECOND_CROSS_STREET_COLUMN = new CsvColumn("secondCrossStreet");

    private static final CsvColumn TIME_ZONE_COLUMN = new CsvColumn("timeZone");

    public static final CsvSchema CSV_SCHEMA = new CsvSchema(IDENTIFIER_COLUMN, UPSTREAM_COLUMN, DOWNSTREAM_COLUMN,
            START_LATITUDE_COLUMN, START_LONGITUDE_COLUMN, END_LATITUDE_COLUMN, END_LONGITUDE_COLUMN, LENGTH_COLUMN,
            FREE_FLOW_COLUMN, PARENT_IDENTIFIER, CITY_COLUMN, MAIN_ROAD_COLUMN, FIRST_CROSS_STREET_COLUMN,
            SECOND_CROSS_STREET_COLUMN, TIME_ZONE_COLUMN);

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

    public RoadSection(final CsvLine line, final Listener<Message> listener)
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

        final var identifier = line.as(IDENTIFIER_COLUMN, roadSectionConverter);
        final var previous = line.as(UPSTREAM_COLUMN, roadSectionListConverter);
        final var next = line.as(DOWNSTREAM_COLUMN, roadSectionListConverter);
        final var startLatitude = line.as(START_LATITUDE_COLUMN, new Latitude.DegreesConverter(listener));
        final var startLongitude = line.as(START_LONGITUDE_COLUMN, new Longitude.DegreesConverter(listener));
        final var endLatitude = line.as(END_LATITUDE_COLUMN, new Latitude.DegreesConverter(listener));
        final var endLongitude = line.as(END_LONGITUDE_COLUMN, new Longitude.DegreesConverter(listener));
        final var length = line.as(LENGTH_COLUMN, new Distance.MetersConverter(listener));
        final var freeFlow = line.as(FREE_FLOW_COLUMN, new Speed.MilesPerHourConverter(listener));
        final var parent = line.as(PARENT_IDENTIFIER, roadSectionConverter);
        final var city = line.as(CITY_COLUMN, cityConverter);
        final var mainRoad = line.as(MAIN_ROAD_COLUMN, streetNameConverter);
        final var firstCrossStreet = line.as(FIRST_CROSS_STREET_COLUMN, streetNameConverter);
        final var secondCrossStreet = line.as(SECOND_CROSS_STREET_COLUMN, streetNameConverter);
        // Timezone is an optional column for now to support older CSV files
        timeZone = null;
        if (line.get(TIME_ZONE_COLUMN) != null)
        {
            timeZone = line.as(TIME_ZONE_COLUMN, new ZoneIdConverter(listener));
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

    public List<RoadSectionIdentifier> allPrevious()
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

    @TdkIncludeProperty
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

    @TdkIncludeProperty
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

    @TdkIncludeProperty
    public RoadSectionIdentifier identifier()
    {
        return RoadSectionIdentifier.forLongValues(identifierFlags, identifierValue);
    }

    public void identifier(final RoadSectionIdentifier identifier)
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

    @TdkIncludeProperty
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
            return RoadSectionIdentifier.forLongValues(next[0], next[1]).roadSection();
        }
        return null;
    }

    public void next(final List<RoadSectionIdentifier> next)
    {
        this.next = buildNeighbors(next);
    }

    public RoadSection parent()
    {
        return parentIdentifierFlags == 0 ? null
                : RoadSectionIdentifier.forLongValues(parentIdentifierFlags, parentIdentifierValue).roadSection();
    }

    public void parent(final RoadSectionIdentifier parent)
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
            return RoadSectionIdentifier.forLongValues(previous[0], previous[1]).roadSection();
        }
        return null;
    }

    public void previous(final List<RoadSectionIdentifier> previous)
    {
        this.previous = buildNeighbors(previous);
    }

    @TdkIncludeProperty
    public Location start()
    {
        return Location.dm7(start);
    }

    public void start(final Location start)
    {
        this.start = start.asLong();
    }

    @TdkIncludeProperty
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
        line.broadcastTo(writer);
        line.set(IDENTIFIER_COLUMN, identifier().toString());

        final var previousIdentifiers = allPrevious();
        if (previousIdentifiers.isEmpty())
        {
            line.set(UPSTREAM_COLUMN, "NULL");
        }
        else
        {
            final var identifiers = new ObjectList<RoadSectionIdentifier>(
                    TdkGraphTrafficLimits.MAXIMUM_UPSTREAM_ROAD_SECTIONS).appendAll(previousIdentifiers);
            line.set(UPSTREAM_COLUMN, identifiers.join(":-:"));
        }

        final var nextIdentifiers = allNext();
        if (nextIdentifiers.isEmpty())
        {
            line.set(DOWNSTREAM_COLUMN, "NULL");
        }
        else
        {
            final BoundedList<RoadSectionIdentifier> identifiers = new ObjectList<RoadSectionIdentifier>(
                    TdkGraphTrafficLimits.MAXIMUM_DOWNSTREAM_ROAD_SECTIONS).appendAll(nextIdentifiers);
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
    private List<RoadSectionIdentifier> buildNeighborListView(final long[] neighbors)
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

    private long[] buildNeighbors(final List<RoadSectionIdentifier> neighbors)
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
