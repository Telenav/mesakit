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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.numeric.Quantizable;
import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.kivakit.coredata.comparison.Differences;
import com.telenav.kivakit.coredata.validation.Validatable;
import com.telenav.kivakit.coredata.validation.ValidationType;
import com.telenav.kivakit.coredata.validation.Validator;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.string.conversion.AsIndentedString;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.graph.identifiers.PlaceIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.graph.specifications.common.place.PlaceAttributes;
import com.telenav.mesakit.graph.specifications.common.place.store.PlaceStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.geography.Located;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;

import java.io.Serializable;
import java.util.List;

/**
 * A place marks geographical areas such as towns, cities and neighborhoods with a point location, usually in the center
 * of the area. Places can be retrieved with:
 * <p>
 * <b>Graph Query</b>
 * <ul>
 *     <li>{@link Graph#places()}</li>
 *     <li>{@link Graph#placesWithNonZeroPopulation()}</li>
 *     <li>{@link Graph#placesWithPopulationOfAtLeast(Count)}</li>
 *     <li>{@link Graph#placeForIdentifier(PlaceIdentifier)}</li>
 * </ul>
 * <b>Spatial Search</b>
 * <ul>
 *     <li>{@link Graph#placeNear(Location)}</li>
 *     <li>{@link Graph#placesNear(Location, Count, Distance, Count)}
 *     <li>{@link Graph#placesInside(Rectangle)}</li>
 *     <li>{@link Graph#placesInside(Region)}</li>
 * </ul>
 * <p>
 * Places have a {@link #name()}, a {@link #type()} and a location that can be retrieved through {@link
 * Located#location()}. They are also {@link Intersectable} and {@link Bounded} with a zero-sized rectangle that can be
 * expanded and they are {@link Validatable}.
 *
 * @author jonathanl (shibo)
 * @see Named
 * @see Located
 * @see Intersectable
 * @see Validatable
 */
public class Place extends GraphElement implements Located, Bounded, Intersectable, Comparable<Place>, Serializable
{
    public enum Type implements Quantizable
    {
        CITY(1),
        SUBURB(2),
        NEIGHBORHOOD(3),
        TOWN(4),
        HAMLET(5),
        VILLAGE(6),
        UNKNOWN(7),
        ;

        public static Type forIdentifier(int identifier)
        {
            if (identifier == 0)
            {
                return null;
            }
            for (var type : values())
            {
                if (type.identifier == identifier)
                {
                    return type;
                }
            }
            throw new IllegalArgumentException("Invalid identifier " + identifier);
        }

        public static Type forString(String type)
        {
            if ("city".equalsIgnoreCase(type))
            {
                return CITY;
            }
            if ("suburb".equalsIgnoreCase(type))
            {
                return SUBURB;
            }
            if ("neighborhood".equalsIgnoreCase(type)
                    || "neighbourhood".equalsIgnoreCase(type)
                    || "neigbourhood".equalsIgnoreCase(type))
            {
                return NEIGHBORHOOD;
            }
            if ("town".equalsIgnoreCase(type))
            {
                return TOWN;
            }
            if ("hamlet".equalsIgnoreCase(type))
            {
                return HAMLET;
            }
            if ("village".equalsIgnoreCase(type))
            {
                return VILLAGE;
            }
            return UNKNOWN;
        }

        private final int identifier;

        Type(int identifier)
        {
            this.identifier = identifier;
        }

        public int identifier()
        {
            return identifier;
        }

        public boolean isLargerThan(Type that)
        {
            return identifier < that.identifier;
        }

        public boolean isLargerThanOrEqualTo(Type that)
        {
            return identifier <= that.identifier;
        }

        public boolean isSmallerThan(Type that)
        {
            return identifier > that.identifier;
        }

        public boolean isSmallerThanOrEqualTo(Type that)
        {
            return identifier >= that.identifier;
        }

        @Override
        public long quantum()
        {
            return identifier;
        }
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public Place(Graph graph, long identifier, int index)
    {
        graph(graph);
        identifier(identifier);
        index(index);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public Place(Graph graph, PlaceIdentifier identifier)
    {
        assert graph != null;
        graph(graph);
        identifier(identifier.asLong());
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public Place(Graph graph, long identifier)
    {
        graph(graph);
        identifier(identifier);
    }

    protected Place()
    {
    }

    protected Place(Place that)
    {
        graph(that.graph());
        identifier(that.identifierAsLong());
        index(that.index());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphElement asHeavyWeight()
    {
        if (this instanceof HeavyWeightPlace)
        {
            return this;
        }

        var place = dataSpecification().newHeavyWeightPlace(graph(), identifierAsLong());
        place.copy(this);
        return place;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphElementAttributes<?> attributes()
    {
        return PlaceAttributes.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle bounds()
    {
        return location().bounds();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Place that)
    {
        return name().compareTo(that.name());
    }

    /**
     * @return The {@link Continent} where this place is
     */
    public Continent continent()
    {
        return Continent.forLocation(location());
    }

    /**
     * @return The {@link Country} where this place is
     */
    public Country country()
    {
        return Country.forLocation(location());
    }

    /**
     * @return The {@link County} where this place is
     */
    public County county()
    {
        return County.forLocation(location());
    }

    /**
     * @return Differences in attributes between this place and the given place
     */
    public Differences differences(Place that)
    {
        return dataSpecification().compare(this, that);
    }

    /**
     * @return The distance from this place to the given located object (having a location)
     */
    public Distance distanceTo(Located that)
    {
        return location().distanceTo(that.location());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlaceIdentifier identifier()
    {
        return new PlaceIdentifier(identifierAsLong());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean intersects(Rectangle rectangle)
    {
        return rectangle.contains(location());
    }

    /**
     * @return True if this place is a city
     */
    public boolean isCity()
    {
        return type() == Type.CITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInside(Rectangle bounds)
    {
        return bounds.contains(location());
    }

    /**
     * @return True if this place has a larger population than the given place
     */
    public boolean isLargerThan(Place that)
    {
        return population().isGreaterThan(that.population());
    }

    /**
     * @return True if the population is exact (rather than estimated based on the type of place)
     */
    public boolean isPopulationExact()
    {
        return store().retrievePopulation(this) != null;
    }

    /**
     * @return True if this place has a smaller population than the given place
     */
    public boolean isSmallerThan(Place that)
    {
        return population().isLessThan(that.population());
    }

    /**
     * @return True if this place is a town
     */
    public boolean isTown()
    {
        return type() == Type.TOWN;
    }

    /**
     * @return True if this place is a village
     */
    public boolean isVillage()
    {
        return type() == Type.VILLAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location location()
    {
        return store().retrieveLocation(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapIdentifier mapIdentifier()
    {
        return identifier();
    }

    /**
     * @return The {@link MetropolitanArea} where this place is (if any)
     */
    public MetropolitanArea metropolitanArea()
    {
        return MetropolitanArea.forLocation(location());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name()
    {
        return store().retrieveName(this);
    }

    /**
     * @return The exact or approximate (based on the type of place) population of this place
     */
    public Count population()
    {
        // Get population from place store
        var population = store().retrievePopulation(this);

        // If there's no exact population, make a round estimate
        if (population == null)
        {
            if (type() != null)
            {
                switch (type())
                {
                    case CITY:
                        return Count._100_000;

                    case TOWN:
                    case SUBURB:
                        return Count._10_000;

                    case VILLAGE:
                        return Count.count(2000);

                    case HAMLET:
                        return Count.count(500);

                    case UNKNOWN:
                    case NEIGHBORHOOD:
                    default:
                        return Count._1_000;
                }
            }
            else
            {
                return Count._1_000;
            }
        }
        return population;
    }

    /**
     * @return The properties of this element from its {@link DataSpecification},
     * @see GraphElementPropertySet
     * @see Stringable
     * @see AsIndentedString
     */
    @Override
    public GraphElementPropertySet<Place> properties()
    {
        return dataSpecification().placeProperties();
    }

    /**
     * @return The regions to which this place belongs
     */
    public List<Region<?>> regions()
    {
        var regions = new ObjectList<Region<?>>();
        regions.addIfNotNull(continent());
        regions.addIfNotNull(country());
        regions.addIfNotNull(state());
        regions.addIfNotNull(county());
        regions.addIfNotNull(metropolitanArea());
        return regions;
    }

    /**
     * @return The state where this place is
     */
    public State state()
    {
        return State.forLocation(location());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return name() + " (population " + population() + " at " + location() + ")";
    }

    /**
     * @return The type of place: city, suburb, neighborhood, town, village or hamlet
     */
    public Type type()
    {
        return store().retrieveType(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Validator validator(ValidationType type)
    {
        return new ElementValidator()
        {
            @Override
            protected void onValidate()
            {
                // Validate superclass,
                validate(Place.super.validator(type));

                // then check for other problems
                glitchIf(location() == null, "its location is missing");
                glitchIf(isEmpty(name()), "its name is empty");
                glitchIf(type() == null, "it has no type");
                glitchIf(isZero(population()), "it has no population");
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PlaceStore store()
    {
        return subgraph().placeStore();
    }
}
