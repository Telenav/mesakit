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

package com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.extractors;

import com.telenav.kivakit.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.language.values.count.Count;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.Metadata;
import com.telenav.mesakit.graph.Place;
import com.telenav.mesakit.graph.specifications.common.graph.loader.extractors.LocationExtractor;
import com.telenav.mesakit.graph.specifications.common.place.HeavyWeightPlace;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfRevisionNumber;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.metadata.PbfUserName;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;

public class PlaceExtractor extends BaseExtractor<Place, PbfEntity<?>>
{
    private final Metadata metadata;

    private final LocationExtractor locationExtractor;

    public PlaceExtractor(final Listener listener, final Metadata metadata,
                          final LocationExtractor locationExtractor)
    {
        super(listener);
        this.metadata = metadata;
        this.locationExtractor = locationExtractor;
    }

    @Override
    public Place onExtract(final PbfEntity<?> entity)
    {
        // If the entity has no tags (shape point node)
        if (entity.tagCount() == 0)
        {
            // reject immediately
            return null;
        }

        // otherwise, get tags from the entity,
        final var tags = entity.tagMap();

        // extract the type from "place=[type]" tag
        final var placeType = tags.value("place");

        // and return the extracted place, if any
        return placeType == null ? null : extractPlace(entity, placeType);
    }

    private HeavyWeightPlace extractPlace(final PbfEntity<?> entity, final String placeType)
    {
        final var location = locationExtractor.extract(entity);
        if (location != null)
        {
            final var place = metadata.dataSpecification().newHeavyWeightPlace(null, entity.identifierAsLong());
            place.location(location);
            place.tags(entity.tagList());
            place.pbfRevisionNumber(new PbfRevisionNumber(entity.version()));
            place.pbfUserIdentifier(new PbfUserIdentifier(entity.user().getId()));
            place.pbfUserName(new PbfUserName(entity.user().getName()));
            place.type(Place.Type.forString(placeType));
            final var time = entity.timestamp().getTime();
            if (time > 0)
            {
                place.lastModificationTime(Time.milliseconds(time));
            }
            final var map = entity.tagMap();
            var name = name(map);
            if (name == null)
            {
                name = placeType;
            }
            place.name(name);
            final var population = map.get("population");
            if (!Strings.isEmpty(population) && Strings.isNaturalNumber(population))
            {
                place.population(Count.parse(population));
            }
            else
            {
                place.population(Count.count(1));
            }
            return place;
        }
        return null;
    }

    private String name(final PbfTagMap tags)
    {
        var name = tags.get("name");
        if (name == null)
        {
            name = tags.get("place_name");
            if (name == null)
            {
                final var keys = tags.keys();
                while (keys.hasNext())
                {
                    final var key = keys.next();
                    if (key.startsWith("name:") && Strings.occurrences(key, ':') == 1)
                    {
                        name = tags.get(key);
                        break;
                    }
                }
            }
        }
        return name;
    }
}
