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
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.polyline.PolylineSnapper;
import com.telenav.mesakit.map.geography.test.GeographyUnitTest;
import com.telenav.mesakit.map.measurements.geographic.Angle;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PolylineSnapperTest extends GeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    public LocationSequence locations()
    {
        var start = new Location(Latitude.degrees(37.385576), Longitude.degrees(-122.005974));
        var shape1 = new Location(Latitude.degrees(37.386482), Longitude.degrees(-122.004362));
        var shape2 = new Location(Latitude.degrees(37.386618), Longitude.degrees(-122.003158));
        var end = new Location(Latitude.degrees(37.38553), Longitude.degrees(-122.002442));
        final List<Location> locations = new ArrayList<>();
        locations.add(start);
        locations.add(shape1);
        locations.add(shape2);
        locations.add(end);
        return LocationSequence.of(locations);
    }

    @Test
    public void testSnapping()
    {
        var locations = locations();
        var snapper = new PolylineSnapper();

        var point = new Location(Latitude.degrees(37.386458), Longitude.degrees(-122.006001));

        var snapped = snapper.snap(locations, point);
        trace("Snapped ${debug} to ${debug}", point, snapped);

        var toCompare = new Location(Latitude.degrees(37.385860019), Longitude.degrees(-122.00546866));
        ensure(toCompare.isClose(snapped, Angle.degrees(0.000001)));

        trace("Offset on snap's Segment = ${debug}", snapped.offsetOnSegment());
        trace("Offset = ${debug}", snapped.offset());

        ensureClose(0.13070176067359085, snapped.offset().asZeroToOne(), 3);
    }
}
