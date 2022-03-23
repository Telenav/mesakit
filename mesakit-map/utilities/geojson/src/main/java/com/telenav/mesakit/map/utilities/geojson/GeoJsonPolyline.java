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

package com.telenav.mesakit.map.utilities.geojson;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonPolyline extends GeoJsonGeometry
{
    private final List<List<Double>> coordinates = new ArrayList<>();

    public GeoJsonPolyline(Polyline line)
    {
        for (var location : line)
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
        var builder = new PolylineBuilder();
        for (var location : coordinates)
        {
            builder.add(new Location(Latitude.degrees(location.get(1)), Longitude.degrees(location.get(0))));
        }
        if (!builder.isValid())
        {
            builder.add(builder.start());
        }
        return builder.build();
    }

    protected void add(Location location)
    {
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(location.longitude().asDegrees());
        coordinates.add(location.latitude().asDegrees());
        this.coordinates.add(coordinates);
    }
}
