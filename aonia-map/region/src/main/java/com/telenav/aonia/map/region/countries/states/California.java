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

package com.telenav.aonia.map.region.countries.states;

import com.telenav.aonia.map.geography.Latitude;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.geography.Longitude;
import com.telenav.aonia.map.measurements.geographic.Distance;
import com.telenav.aonia.map.region.RegionIdentity;
import com.telenav.aonia.map.region.RegionInstance;
import com.telenav.aonia.map.region.regions.City;
import com.telenav.aonia.map.region.regions.Country;
import com.telenav.aonia.map.region.regions.County;
import com.telenav.aonia.map.region.regions.MetropolitanArea;
import com.telenav.aonia.map.region.regions.State;

public class California extends State
{
    public MetropolitanArea SAN_FRANCISCO_OAKLAND;

    public MetropolitanArea LOS_ANGELES;

    public MetropolitanArea SAN_JOSE;

    public County SAN_MATEO_COUNTY;

    public County SANTA_CLARA_COUNTY;

    public City MOUNTAIN_VIEW;

    public California()
    {
        super(Country.UNITED_STATES, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("California")
                        .withIsoCode("CA")));
    }

    @Override
    protected void onInitialize()
    {
        SAN_FRANCISCO_OAKLAND = new MetropolitanArea(this,
                new RegionInstance<>(MetropolitanArea.class)
                        .withIdentity(new RegionIdentity("San Francisco—Oakland")));

        LOS_ANGELES = new MetropolitanArea(this, new RegionInstance<>(MetropolitanArea.class)
                .withIdentity(new RegionIdentity("Long Beach—Anaheim")));

        SAN_JOSE = new MetropolitanArea(this, new RegionInstance<>(MetropolitanArea.class)
                .withIdentity(new RegionIdentity("San Jose")));

        SAN_MATEO_COUNTY = new County(this, new RegionInstance<>(County.class)
                .withIdentity(new RegionIdentity("San Mateo")));

        SANTA_CLARA_COUNTY = new County(this, new RegionInstance<>(County.class)
                .withIdentity(new RegionIdentity("Santa Clara")));

        MOUNTAIN_VIEW = new City(this, new RegionInstance<>(City.class)
                .withIdentity(new RegionIdentity("Mountain View"))
                .withBounds(
                        new Location(Latitude.degrees(37.38039), Longitude.degrees(-122.07298)).within(Distance.ONE_MILE)));
    }
}
