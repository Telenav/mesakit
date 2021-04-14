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

import com.telenav.aonia.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GeoJsonGeometryCollection extends GeoJsonGeometry implements Iterable<GeoJsonGeometry>
{
    private final List<GeoJsonGeometry> geometries = new ArrayList<>();

    public void add(final GeoJsonGeometry geometry)
    {
        geometries.add(geometry);
    }

    @Override
    public Rectangle bounds()
    {
        final var builder = new BoundingBoxBuilder();
        for (final var geometry : this)
        {
            builder.add(geometry.bounds());
        }
        return builder.build();
    }

    public List<GeoJsonGeometry> geometries()
    {
        return geometries;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<GeoJsonGeometry> iterator()
    {
        return geometries.iterator();
    }
}
