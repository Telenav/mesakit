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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.telenav.kivakit.core.collections.map.StringToStringMap;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.resource.writing.WritableResource;
import com.telenav.kivakit.resource.Extension;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.measurements.geographic.Heading;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.telenav.kivakit.core.ensure.Ensure.fail;

public class GeoJsonDocument implements Iterable<GeoJsonFeature>
{
    public static Extension EXTENSION = Extension.parseExtension(Listener.console(), ".geojson");

    public static GeoJsonDocument forJson(String json)
    {
        return gson().fromJson(json, GeoJsonDocument.class);
    }

    public static void main(String[] args)
    {
        var document = new GeoJsonDocument();
        var feature = new GeoJsonFeature("Title");
        feature.put("title", "Feature Title");
        feature.put("description", "This is the description of this feature");
        var location = Location.degrees(47.678714, -122.337366);
        feature.add(new GeoJsonPoint(location));
        var a = location.moved(Heading.EAST, Distance.meters(50));
        var b = a.moved(Heading.SOUTHEAST, Distance.meters(50));
        feature.add(new GeoJsonPolyline(Polyline.fromLocations(a, b)));
        document.add(feature);
        System.out.println(document);
    }

    private final List<GeoJsonFeature> features = new ArrayList<>();

    private final Map<String, Object> properties = new HashMap<>();

    public synchronized void add(GeoJsonFeature feature)
    {
        if (feature == null)
        {
            fail("Null feature");
        }
        features.add(feature);
    }

    public Rectangle bounds()
    {
        var builder = new BoundingBoxBuilder();
        for (var feature : this)
        {
            for (var geometry : feature)
            {
                builder.add(geometry.bounds());
            }
        }
        return builder.build();
    }

    public Count count()
    {
        return Count.count(size());
    }

    public List<GeoJsonFeature> features()
    {
        return features;
    }

    public GeoJsonFeature[] featuresAsArray()
    {
        return features.toArray(new GeoJsonFeature[size()]);
    }

    public GeoJsonFeature get(int index)
    {
        return features.get(index);
    }

    @Override
    public Iterator<GeoJsonFeature> iterator()
    {
        return features.iterator();
    }

    public Map<String, Object> properties()
    {
        return properties;
    }

    public Map<String, String> propertiesAsStringToStringMap()
    {
        var map = new StringToStringMap(Maximum.MAXIMUM);
        for (var key : properties.keySet())
        {
            map.put(key, properties.get(key).toString());
        }
        return map;
    }

    public void save(WritableResource resource)
    {
        var out = resource.printWriter();
        out.print(this);
        out.close();
    }

    public int size()
    {
        return features.size();
    }

    public void sortFeatures(Comparator<? super GeoJsonFeature> featureComparator)
    {
        features.sort(featureComparator);
    }

    @Override
    public String toString()
    {
        return gson().toJson(this);
    }

    private static Gson gson()
    {
        var builder = new GsonBuilder().setPrettyPrinting();
        builder.registerTypeAdapter(GeoJsonGeometry.class, new GeoJsonGeometryTypeAdapter());
        return builder.create();
    }
}
