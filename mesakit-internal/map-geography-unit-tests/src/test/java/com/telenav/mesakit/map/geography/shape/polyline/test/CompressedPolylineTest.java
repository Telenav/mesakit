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

package com.telenav.mesakit.map.geography.shape.polyline.test;

import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.testing.GeographyUnitTest;
import com.telenav.mesakit.map.geography.shape.polyline.compression.differential.CompressedPolyline;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CompressedPolylineTest extends GeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    @Test
    public void testCompression()
    {
        testCompression(locations());
    }

    @Test
    public void testSegment()
    {
        var locationConverter = new Location.DegreesConverter(LOGGER);
        var a = locationConverter.convert("47.61524,-122.32147");
        var b = locationConverter.convert("47.61409,-122.32146");
        var line = CompressedPolyline.fromLocationSequence(a, b);
        ensureEqual("47.61524,-122.32147:47.61409,-122.32146", line.toString());
    }

    private List<Location> locations()
    {
        List<Location> locations = new ArrayList<>();
        var locationConverter = new Location.DegreesConverter(LOGGER);
        locations.add(locationConverter.convert("47.0,-122.0"));
        locations.add(locationConverter.convert("47.0000015,-122.0000015"));
        locations.add(locationConverter.convert("47.6777477,-122.3204223"));
        locations.add(locationConverter.convert("47.6778300,-122.3203624"));
        locations.add(locationConverter.convert("47.6779710,-122.3203510"));
        locations.add(locationConverter.convert("47.6786712,-122.3202723"));
        locations.add(locationConverter.convert("47.6790112,-122.3202412"));
        locations.add(locationConverter.convert("47.6793319,-122.3202000"));
        return locations;
    }

    private void testCompression(List<Location> locations)
    {
        var line = CompressedPolyline.fromLocationSequence(locations);
        ensureEqual(new Polyline(locations), line.decompress());
    }
}
