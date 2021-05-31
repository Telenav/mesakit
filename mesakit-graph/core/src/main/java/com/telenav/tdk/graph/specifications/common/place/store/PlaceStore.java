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

package com.telenav.kivakit.graph.specifications.common.place.store;

import com.telenav.kivakit.collections.primitive.array.packed.SplitPackedArray;
import com.telenav.kivakit.collections.primitive.array.scalars.*;
import com.telenav.kivakit.collections.primitive.list.store.PackedStringStore;
import com.telenav.kivakit.kernel.debug.Debug;
import com.telenav.kivakit.kernel.logging.*;
import com.telenav.kivakit.kernel.scalars.counts.*;
import com.telenav.kivakit.kernel.validation.*;
import com.telenav.kivakit.resource.compression.archive.KivaKitArchivedField;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.metadata.DataSpecification.GraphElementFactory;
import com.telenav.kivakit.graph.specifications.common.element.ArchivedGraphElementStore;
import com.telenav.kivakit.graph.specifications.common.place.PlaceAttributes;
import com.telenav.kivakit.graph.specifications.library.attributes.AttributeReference;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.geography.indexing.quadtree.QuadTreeSpatialIndex;

import static com.telenav.kivakit.collections.primitive.array.packed.PackedPrimitiveArray.OverflowHandling.NO_OVERFLOW;
import static com.telenav.kivakit.kernel.validation.Validation.VALIDATE_ALL;
import static com.telenav.kivakit.graph.Metadata.CountType.ALLOW_ESTIMATE;
import static com.telenav.kivakit.map.geography.Precision.DM7;

/**
 * Packed place attributes.
 *
 * @see PlaceAttributes
 * @see ArchivedGraphElementStore
 */
@SuppressWarnings("unused")
public class PlaceStore extends ArchivedGraphElementStore<Place>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final AttributeReference<LongArray> LOCATION =
            new AttributeReference<>(this, PlaceAttributes.get().LOCATION, "location",
                    () -> (LongArray) new LongArray("location").initialSize(estimatedElements()));

    @KivaKitArchivedField
    private LongArray location;

    private final AttributeReference<IntArray> POPULATION =
            new AttributeReference<>(this, PlaceAttributes.get().POPULATION, "population",
                    () -> (IntArray) new IntArray("population").initialSize(estimatedElements()));

    @KivaKitArchivedField
    private IntArray population;

    private final AttributeReference<PackedStringStore> NAME =
            new AttributeReference<>(this, PlaceAttributes.get().NAME, "name",
                    () -> (PackedStringStore) new PackedStringStore("name")
                            .initialSize(estimatedElements()).initialChildSize(32));

    @KivaKitArchivedField
    private PackedStringStore name;

    private final AttributeReference<SplitPackedArray> TYPE = new AttributeReference<>(this, PlaceAttributes.get().TYPE, "type",
            () -> (SplitPackedArray) new SplitPackedArray("type")
                    .bits(Bits._4, NO_OVERFLOW)
                    .initialSize(estimatedElements()));

    @KivaKitArchivedField
    private SplitPackedArray type;

    /** A spatial index for the edges in this graph */
    private QuadTreeSpatialIndex<Place> spatialIndex;

    public PlaceStore(final Graph graph)
    {
        super(graph);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Estimate initialSize()
    {
        return metadata().placeCount(ALLOW_ESTIMATE).asEstimate();
    }

    @Override
    public String objectName()
    {
        return "place-store";
    }

    public Location retrieveLocation(final Place place)
    {
        return LOCATION.retrieveObject(place, DM7::toLocation);
    }

    public String retrieveName(final Place place)
    {
        return NAME.retrieveString(place);
    }

    public Count retrievePopulation(final Place place)
    {
        return POPULATION.retrieveObject(place, Count::of);
    }

    public Place.Type retrieveType(final Place place)
    {
        return TYPE.retrieveObject(place, value -> Place.Type.forIdentifier((int) value));
    }

    public synchronized QuadTreeSpatialIndex<Place> spatialIndex()
    {
        if (spatialIndex == null)
        {
            spatialIndex = new QuadTreeSpatialIndex<>();
            for (final var place : this)
            {
                spatialIndex.add(place);
            }
        }
        return spatialIndex;
    }

    @Override
    public Validator validator(final Validation validation)
    {
        final var outer = this;

        return !validation.shouldValidate(getClass()) ? Validator.NULL : new StoreValidator()
        {
            @Override
            protected void onValidate()
            {
                if (outer.isEmpty())
                {
                    warning("Place store is empty");
                }
                else
                {
                    validate(PlaceStore.super.validator(validation));

                    final var outer = PlaceStore.this;

                    problemIf(outer.name == null, "there are no names");
                    problemIf(outer.type == null, "there are no types");
                    problemIf(outer.location == null, "there are no locations");
                    problemIf(outer.population == null, "there are no populations");

                    // Go through place indexes
                    for (var index = 1; index < size() && !isInvalid(); index++)
                    {
                        quibbleIf(isEmpty(outer.name.get(index)), "the name is empty or missing");
                        quibbleIf(outer.type.get(index) == 0, "the type is empty or missing");
                        quibbleIf(outer.location.get(index) == 0, "the location is missing");
                        quibbleIf(outer.population.get(index) == 0, "the population is zero or missing");

                        if (isInvalid())
                        {
                            problem("place at index $ of $ was invalid", index, size());
                        }
                    }
                }
            }
        };
    }

    @Override
    protected GraphElementFactory<Place> elementFactory()
    {
        return dataSpecification()::newPlace;
    }

    @Override
    protected Class<Place> elementType()
    {
        return Place.class;
    }

    @Override
    protected void onAdd(final Place place)
    {
        // Assign the place an index
        final var index = identifierToIndex(place.identifierAsLong(), IndexingMode.CREATE);
        place.index(index);

        // call the superclass,
        super.onAdd(place);

        // and then store the place attributes
        storeAttributes(place);

        if (DEBUG.isEnabled())
        {
            final var retrieved = dataSpecification().newPlace(graph(), place.identifierAsLong());
            assert retrieved.validator(VALIDATE_ALL).isValid(LOGGER);
            assert place.equals(retrieved);
        }
    }

    /**
     * Stores all of the simple attributes of the given place at the given index
     */
    private void storeAttributes(final Place place)
    {
        LOCATION.storeObject(place, place.location());
        NAME.storeString(place, place.name());
        POPULATION.storeObject(place, place.population());
        TYPE.storeObject(place, place.type());
    }
}
