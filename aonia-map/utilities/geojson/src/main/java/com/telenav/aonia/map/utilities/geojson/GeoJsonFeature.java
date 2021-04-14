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

import com.telenav.aonia.map.geography.shape.rectangle.Bounded;
import com.telenav.aonia.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.aonia.map.geography.shape.rectangle.Intersectable;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GeoJsonFeature implements Iterable<GeoJsonGeometry>, Bounded, Intersectable, Serializable
{
    private static final long serialVersionUID = 3010149983097611081L;

    private GeoJsonGeometry geometry;

    private final Map<String, Object> properties = new LinkedHashMap<>();

    public GeoJsonFeature()
    {
    }

    public GeoJsonFeature(final String title)
    {
        title(title);
    }

    public void add(final GeoJsonGeometry geometry)
    {
        if (this.geometry == null)
        {
            this.geometry = geometry;
        }
        else
        {
            final GeoJsonGeometryCollection collection;
            if (this.geometry instanceof GeoJsonGeometryCollection)
            {
                collection = (GeoJsonGeometryCollection) this.geometry;
            }
            else
            {
                collection = new GeoJsonGeometryCollection();
                if (this.geometry != null)
                {
                    collection.add(this.geometry);
                }
                this.geometry = collection;
            }
            collection.add(geometry);
        }
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
        final List<GeoJsonGeometry> geometries = new ArrayList<>();
        if (geometry instanceof GeoJsonGeometryCollection)
        {
            for (final var geometry : (GeoJsonGeometryCollection) geometry)
            {
                geometries.add(geometry);
            }
        }
        else
        {
            if (geometry != null)
            {
                geometries.add(geometry);
            }
        }
        return geometries;
    }

    @Override
    public boolean intersects(final Rectangle that)
    {
        final var bounds = bounds();
        return bounds != null && bounds.intersects(that);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<GeoJsonGeometry> iterator()
    {
        return geometries().iterator();
    }

    public Map<String, Object> properties()
    {
        return properties;
    }

    public void put(final String key, final Object value)
    {
        properties.put(key, value);
    }

    public String title()
    {
        final var title = properties.get("title");
        if (title != null)
        {
            return title.toString();
        }
        return null;
    }

    public void title(final String title)
    {
        if (title != null)
        {
            properties.put("title", title);
        }
    }

    @Override
    public String toString()
    {
        return title();
    }
}
