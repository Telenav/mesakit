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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GeoJsonGeometryTypeAdapter implements JsonSerializer<GeoJsonGeometry>, JsonDeserializer<GeoJsonGeometry>
{
    @Override
    public GeoJsonGeometry deserialize(JsonElement json, Type typeOfT,
                                       JsonDeserializationContext context) throws JsonParseException
    {
        var jsonObject = json.getAsJsonObject();
        var type = jsonObject.get("type").getAsString();
        if ("Point".equalsIgnoreCase(type))
        {
            return context.deserialize(jsonObject, GeoJsonPoint.class);
        }
        if ("LineString".equalsIgnoreCase(type))
        {
            return context.deserialize(jsonObject, GeoJsonPolyline.class);
        }
        if ("GeometryCollection".equalsIgnoreCase(type))
        {
            return context.deserialize(jsonObject, GeoJsonGeometryCollection.class);
        }
        throw new IllegalStateException("Invalid GeoJson geometry type: " + type);
    }

    @Override
    public JsonElement serialize(GeoJsonGeometry src, Type typeOfSrc,
                                 JsonSerializationContext context)
    {
        return context.serialize(src, src.getClass());
    }
}
