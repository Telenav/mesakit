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

package com.telenav.mesakit.graph.specifications.common.graph.loader.extractors;

import com.telenav.kivakit.coredata.extraction.BaseExtractor;
import com.telenav.kivakit.coredata.extraction.Extractor;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

public class LocationExtractor extends BaseExtractor<Location, PbfEntity<?>>
{
    private final Extractor<Location, WayNode> wayNodeLocationExtractor;

    public LocationExtractor(Listener listener,
                             Extractor<Location, WayNode> wayNodeLocationExtractor)
    {
        super(listener);
        this.wayNodeLocationExtractor = wayNodeLocationExtractor;
    }

    @Override
    public Location onExtract(PbfEntity<?> entity)
    {
        switch (entity.type())
        {
            case Node:
            {
                var node = (PbfNode) entity;
                var latitude = Latitude.degrees(node.latitude());
                var longitude = Longitude.degrees(node.longitude());
                return new Location(latitude, longitude);
            }

            case Way:
            {
                var way = (PbfWay) entity;
                var builder = new BoundingBoxBuilder();
                for (var node : way.nodes())
                {
                    var location = wayNodeLocationExtractor.extract(node);
                    if (location != null)
                    {
                        builder.add(location);
                    }
                }
                var bounds = builder.build();
                if (bounds != null)
                {
                    return bounds.center();
                }
            }
        }
        return null;
    }
}
