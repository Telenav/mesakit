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

package com.telenav.tdk.graph.specifications.common.graph.loader.extractors;

import com.telenav.tdk.core.data.extraction.*;
import com.telenav.tdk.core.kernel.messaging.*;
import com.telenav.tdk.data.formats.pbf.model.tags.*;
import com.telenav.tdk.map.geography.*;
import com.telenav.tdk.map.geography.rectangle.BoundingBoxBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class LocationExtractor extends BaseExtractor<Location, PbfEntity<?>>
{
    private final Extractor<Location, WayNode> wayNodeLocationExtractor;

    public LocationExtractor(final Listener<Message> listener,
                             final Extractor<Location, WayNode> wayNodeLocationExtractor)
    {
        super(listener);
        this.wayNodeLocationExtractor = wayNodeLocationExtractor;
    }

    @Override
    public Location onExtract(final PbfEntity<?> entity)
    {
        switch (entity.type())
        {
            case Node:
            {
                final var node = (PbfNode) entity;
                final var latitude = Latitude.degrees(node.latitude());
                final var longitude = Longitude.degrees(node.longitude());
                return new Location(latitude, longitude);
            }

            case Way:
            {
                final var way = (PbfWay) entity;
                final var builder = new BoundingBoxBuilder();
                for (final var node : way.nodes())
                {
                    final var location = wayNodeLocationExtractor.extract(node);
                    if (location != null)
                    {
                        builder.add(location);
                    }
                }
                final var bounds = builder.build();
                if (bounds != null)
                {
                    return bounds.center();
                }
            }
        }
        return null;
    }
}
