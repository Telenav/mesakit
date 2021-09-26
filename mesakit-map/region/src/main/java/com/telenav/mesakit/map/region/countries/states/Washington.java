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

import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.countries.states.cities.Seattle;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.State;

public class Washington extends State
{
    public Seattle SEATTLE;

    public Washington()
    {
        super(Country.UNITED_STATES, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Washington")
                        .withIsoCode("WA")));
    }

    @Override
    public void onInitialize()
    {
        SEATTLE = new Seattle();
        SEATTLE.initialize();
    }
}
