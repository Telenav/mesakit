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

package com.telenav.mesakit.map.region.countries.states;

import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.State;

public class NewYork extends State
{
    public City BUFFALO;

    public City ROCHESTER;

    public NewYork()
    {
        super(Country.UNITED_STATES, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("New York")
                        .withIsoCode("NY")));
    }

    @Override
    protected void onInitialize()
    {
        BUFFALO = new City(this, new RegionInstance<>(City.class)
                .withIdentity(new RegionIdentity("Buffalo"))
                .withBounds(
                        new Location(Latitude.degrees(42.95899), Longitude.degrees(-78.76806)).within(Distance.ONE_MILE)));

        ROCHESTER = new City(this, new RegionInstance<>(City.class)
                .withIdentity(new RegionIdentity("Rochester"))
                .withBounds(
                        new Location(Latitude.degrees(43.13755), Longitude.degrees(-77.54082)).within(Distance.ONE_MILE)));
    }
}
