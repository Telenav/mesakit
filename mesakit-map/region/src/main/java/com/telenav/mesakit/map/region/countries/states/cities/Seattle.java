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

package com.telenav.mesakit.map.region.countries.states.cities;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.District;

public class Seattle extends City
{
    public District GREEN_LAKE;

    public District DOWNTOWN;

    public Seattle()
    {
        super(Country.UNITED_STATES.WASHINGTON, new RegionInstance<>(City.class)
                .withIdentity(new RegionIdentity("Seattle")));
    }

    @Override
    public void onInitialize()
    {
        GREEN_LAKE = new District(this, new RegionInstance<>(District.class)
                .withIdentity(new RegionIdentity("Green Lake"))
                .withBounds(new Location(Latitude.degrees(47.678623), Longitude.degrees(-122.337484))
                        .within(Distance.ONE_MILE)));

        DOWNTOWN = new District(this, new RegionInstance<>(District.class)
                .withIdentity(new RegionIdentity("Downtown Seattle"))
                .withBounds(Rectangle.parse("47.587296,-122.346804:47.616234,-122.317866")));
    }
}
