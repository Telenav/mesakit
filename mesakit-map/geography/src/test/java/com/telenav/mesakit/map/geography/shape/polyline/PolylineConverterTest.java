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

package com.telenav.mesakit.map.geography.shape.polyline;

import com.telenav.kivakit.core.string.Separators;
import com.telenav.kivakit.core.test.UnitTest;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import org.junit.Test;

/**
 * Created by bogdantnv on 11/11/15.
 */
public class PolylineConverterTest extends UnitTest
{
    private final Polyline.Converter polylineConverter = new Polyline.Converter(this, Separators.DEFAULT);

    @Test
    public void testOnConvertToString()
    {
        var builder = new PolylineBuilder();
        builder.add(new Location(Latitude.degrees(37.38592), Longitude.degrees(-122.00602)));
        builder.add(new Location(Latitude.degrees(37.38553), Longitude.degrees(-122.00602)));
        builder.add(new Location(Latitude.degrees(37.38557), Longitude.degrees(-122.00426)));
        builder.add(new Location(Latitude.degrees(37.38648), Longitude.degrees(-122.00430)));

        var polyline = builder.build();
        var stringConversion = polylineConverter.unconvert(polyline);
        ensureEqual(polyline, polylineConverter.onConvert(stringConversion));
    }
}
