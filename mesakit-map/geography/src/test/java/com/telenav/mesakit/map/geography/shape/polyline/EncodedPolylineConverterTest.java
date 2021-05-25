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

import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.project.MapGeographyUnitTest;
import org.junit.Test;

/**
 * Test {@link Polyline} encoding and decoding. Debug with
 * <a href="https://developers.google.com/maps/documentation/utilities/polylineutility">Google Polyline Utility</a>
 *
 * @author matthieun
 */
public class EncodedPolylineConverterTest extends MapGeographyUnitTest
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private final Polyline.GoogleEncodingConverter converter = new Polyline.GoogleEncodingConverter(LOGGER);

    @Test
    public void testEncodingDecoding()
    {
        // Line wrapping Telenav's buildings
        @SuppressWarnings("SpellCheckingInspection") final var encoded = "_}dcFrhtgVlA?G_JuDF";
        final var builder = new PolylineBuilder();
        builder.add(new Location(Latitude.degrees(37.38592), Longitude.degrees(-122.00602)));
        builder.add(new Location(Latitude.degrees(37.38553), Longitude.degrees(-122.00602)));
        builder.add(new Location(Latitude.degrees(37.38557), Longitude.degrees(-122.00426)));
        builder.add(new Location(Latitude.degrees(37.38648), Longitude.degrees(-122.00430)));

        final var newDecoded = converter.convert(encoded);
        final var newEncoded = converter.toStringConverter().convert(builder.build());

        ensureEqual(encoded, newEncoded);
        ensureEqual(builder.build(), newDecoded);
    }
}
