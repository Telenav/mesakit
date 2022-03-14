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

package com.telenav.mesakit.map.road.model.converters;

import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.Split;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.GeographyLimits;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;

import java.util.List;

public class RoadShapeConverter extends BaseStringConverter<Polyline>
{
    public enum Type
    {
        DM5,
        DEGREES
    }

    private Type type = Type.DM5;

    private final int columns;

    public RoadShapeConverter(Listener listener, int columns)
    {
        super(listener);
        this.columns = columns;
    }

    public void type(Type type)
    {
        this.type = type;
    }

    // Create a new RoadShape from comma delimited repeated lat/lon pairs (there are two extra
    // columns for altitude and "relative elevation" in unified TXD). This is how the
    // shape-points are stored in the TXD file format.
    @Override
    protected Polyline onToValue(String value)
    {
        List<Location> locations = new ObjectList<>(GeographyLimits.LOCATIONS_PER_POLYLINE);
        var i = 0;
        Latitude latitude = null;
        Longitude longitude;
        for (var coordinate : Split.split(value, ","))
        {
            if (i % columns == 0)
            {
                if (type == Type.DM5)
                {
                    latitude = Latitude.dm5(Integer.parseInt(coordinate));
                }
                else
                {
                    latitude = Latitude.degrees(Double.parseDouble(coordinate));
                }
            }
            else if (i % columns == 1)
            {
                if (type == Type.DM5)
                {
                    longitude = Longitude.dm5(Integer.parseInt(coordinate));
                }
                else
                {
                    longitude = Longitude.degrees(Double.parseDouble(coordinate));
                }
                locations.add(new Location(latitude, longitude));
            }
            i++;
        }
        return Polyline.fromLocations(locations);
    }
}
