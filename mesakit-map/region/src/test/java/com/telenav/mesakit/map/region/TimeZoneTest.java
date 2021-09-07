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

package com.telenav.mesakit.map.region;

import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.project.RegionUnitTest;
import com.telenav.mesakit.map.region.regions.TimeZone;
import org.junit.Test;

/**
 * Verifies several known time zone location results, including complicated time zone geography cases such as northeast
 * Arizona and a case in the middle of the ocean that should not find a timezone.
 *
 * @author roberts
 * @author jonathanl (shibo)
 */
public class TimeZoneTest extends RegionUnitTest
{
    @Test
    public void testLosAngeles()
    {
        // SFO
        check("America/Los_Angeles", 37.616, -122.386);
    }

    @Test
    public void testLouisiana()
    {
        // Lake Ponchartrain -- this point is not actually inside the America/Chicago timezone due
        // to the outline
        check(null, 30.189, -90.124);
    }

    @Test
    public void testMexico()
    {
        // Monterrey, Mexico -- Was misspelled until recent versions of the timezone database
        check("America/Monterrey", 26.641, -101.802);

        // Gulf of Mexico -- no timezone
        check(null, 24.431, -92.503);
    }

    @Test
    public void testMountain()
    {
        // Denver airport (MST or MDT)
        //check("America/Denver", 39.849, -104.674);

        // Flagstaff, inside Arizona (AZ / MST)
        check("America/Phoenix", 35.1997205920378, -111.64855728338603);

        // Navajo reservation, inside Mountain time (MST or MDT)
        //check("America/Denver", 35.352, -110.985);

        // Walpi on Hopi Reservation, inside Navajo reservation, inside Mountain time (AZ / MST)
        check("America/Phoenix", 35.832910, -110.397973);

        // Jeddito, inside Hopi reservation, inside Navajo reservation, inside Mountain time (MST or MDT)
        check("America/Denver", 35.775685560512564, -110.13667148083405);
    }

    @Test
    public void testNorthDakota()
    {
        // North Dakota: New Salem
        check("America/North_Dakota/New_Salem", 46.816, -101.421);

        // North Dakota: Center
        check("America/North_Dakota/Center", 47.110, -101.348);

        // America/North_Dakota/Beulah is one of the newest time zones
        check("America/North_Dakota/Beulah", 47.353, -101.888);
    }

    @Test
    public void testPhoenix()
    {
        // PHX
        check("America/Phoenix", 33.436, -112.000);

        // Hopi
        check("America/Phoenix", 35.754, -110.688);
    }

    @Test
    public void testRome()
    {
        // Rome
        check("Europe/Rome", 46.74214, 11.64010);
    }

    @Test
    public void testShanghai()
    {
        // Telenav Shanghai
        check("Asia/Shanghai", 31.208, 121.394);
    }

    @Test
    public void testSitka()
    {
        // America/Sitka is one of the newest time zones
        check("America/Sitka", 57.053, -135.335);
    }

    private void check(final String expectedTimeZone, final double latitudeInDegrees, final double longitudeInDegrees)
    {
        final var location = Location.degrees(latitudeInDegrees, longitudeInDegrees);
        final var timeZone = TimeZone.forLocation(location);
        if (timeZone == null)
        {
            if (expectedTimeZone != null)
            {
                fail("Time zone $ was expected at $ but no time zone found", expectedTimeZone, location);
            }
        }
        else
        {
            ensureEqual(expectedTimeZone, timeZone.asZoneId().getId());
        }
    }
}
