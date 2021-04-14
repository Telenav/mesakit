////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.aonia.map.utilities.geojson;

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.geography.shape.polyline.Polygon;
import com.telenav.aonia.map.geography.shape.polyline.Polyline;
import com.telenav.aonia.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * A GeoJson polygon
 *
 * @author matthieun
 */
public class GeoJsonPolygon extends GeoJsonGeometry
{
    private final List<List<List<Double>>> coordinates = new ArrayList<>();

    public GeoJsonPolygon(final Polygon polygon)
    {
        // Add the outer polygon
        coordinates.add(new ArrayList<>());
        for (final var location : polygon.locationSequence())
        {
            add(location);
        }
    }

    @Override
    public Rectangle bounds()
    {
        return polyline().bounds();
    }

    public Polyline polyline()
    {
        final var builder = new PolylineBuilder();
        for (final var location : coordinates.get(0))
        {
            builder.add(new Location(Latitude.degrees(location.get(1)), Longitude.degrees(location.get(0))));
        }
        return builder.build();
    }

    protected void add(final Location location)
    {
        final List<Double> coordinates = new ArrayList<>();
        coordinates.add(location.longitude().asDegrees());
        coordinates.add(location.latitude().asDegrees());
        this.coordinates.get(0).add(coordinates);
    }
}
