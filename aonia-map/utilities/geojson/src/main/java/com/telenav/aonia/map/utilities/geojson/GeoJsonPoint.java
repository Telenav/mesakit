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
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonPoint extends GeoJsonGeometry
{
    private final List<Double> coordinates = new ArrayList<>();

    public GeoJsonPoint(final Location location)
    {
        add(location);
    }

    @Override
    public Rectangle bounds()
    {
        return location().within(Distance.meters(1));
    }

    public Location location()
    {
        return new Location(Latitude.degrees(coordinates.get(1)), Longitude.degrees(coordinates.get(0)));
    }

    protected void add(final Location location)
    {
        coordinates.add(location.longitude().asDegrees());
        coordinates.add(location.latitude().asDegrees());
    }
}
