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

import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.time.Time;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.messaging.Listener;
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

    public PlaceExtractor(Listener listener, Metadata metadata,
                          LocationExtractor locationExtractor)
    {
        super(listener);
        this.metadata = metadata;
        this.locationExtractor = locationExtractor;
    }

    @Override
    public Place onExtract(PbfEntity<?> entity)
    {
        // If the entity has no tags (shape point node)
        if (entity.tagCount() == 0)
        {
            // reject immediately
            return null;
        }

        // otherwise, get tags from the entity,
        var tags = entity.tagMap();

        // extract the type from "place=[type]" tag
        var placeType = tags.value("place");

        // and return the extracted place, if any
        return placeType == null ? null : extractPlace(entity, placeType);
    }

    private HeavyWeightPlace extractPlace(PbfEntity<?> entity, String placeType)
    {
        var location = locationExtractor.extract(entity);
        if (location != null)
        {
            var place = metadata.dataSpecification().newHeavyWeightPlace(null, entity.identifierAsLong());
            place.location(location);
            place.tags(entity.tagList());
            place.pbfRevisionNumber(new PbfRevisionNumber(entity.version()));
            place.pbfUserIdentifier(new PbfUserIdentifier(entity.user().getId()));
            place.pbfUserName(new PbfUserName(entity.user().getName()));
            place.type(Place.Type.forString(placeType));
            var time = entity.timestamp().getTime();
            if (time > 0)
            {
                place.lastModificationTime(Time.epochMilliseconds(time));
            }
            var map = entity.tagMap();
            var name = name(map);
            if (name == null)
            {
                name = placeType;
            }
            place.name(name);
            var population = map.get("population");
            if (!Strings.isEmpty(population) && Strings.isNaturalNumber(population))
            {
                place.population(Count.parseCount(this, population));
            }
            else
            {
                place.population(Count.count(1));
            }
            return place;
        }
        return null;
    }

    private String name(PbfTagMap tags)
    {
        var name = tags.get("name");
        if (name == null)
        {
            name = tags.get("place_name");
            if (name == null)
            {
                var keys = tags.keys();
                while (keys.hasNext())
                {
                    var key = keys.next();
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
