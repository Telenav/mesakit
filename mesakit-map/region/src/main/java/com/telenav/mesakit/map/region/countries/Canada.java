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

package com.telenav.mesakit.map.region.countries;

import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.kivakit.kernel.language.locales.CountryIsoCode;
import com.telenav.kivakit.kernel.language.locales.LanguageIsoCode;

public class Canada extends Country
{
    public static State ONTARIO;

    public static State QUEBEC;

    public static State NOVA_SCOTIA;

    public static State NEW_BRUNSWICK;

    public static State MANITOBA;

    public static State BRITISH_COLUMBIA;

    public static State PRINCE_EDWARD_ISLAND;

    public static State SASKATCHEWAN;

    public static State ALBERTA;

    public static State NEWFOUNDLAND_AND_LABRADOR;

    public Canada()
    {
        super(Continent.NORTH_AMERICA, new RegionInstance<>(Country.class)
                .withOrdinal(37)
                .withIdentity(new RegionIdentity("Canada")
                        .withCountryTmcCode(new CountryTmcCode(0x0c))
                        .withCountryIsoCode(CountryIsoCode.CANADA))
                .withLanguage(LanguageIsoCode.ENGLISH)
                .withLanguage(LanguageIsoCode.FRENCH)
                .withAutomotiveSupportLevel(AutomotiveSupportLevel.UNDER_DEVELOPMENT)
                .withDrivingSide(DrivingSide.RIGHT));
    }

    @Override
    public void onInitialize()
    {
        ONTARIO = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Ontario")
                        .withIsoCode("ON")));

        QUEBEC = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Québec")
                        .withIsoCode("QC")));

        NOVA_SCOTIA = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Nova Scotia")
                        .withIsoCode("NS")));

        NEW_BRUNSWICK = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("New Brunswick")
                        .withIsoCode("NB")));

        MANITOBA = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Manitoba")
                        .withIsoCode("MB")));

        BRITISH_COLUMBIA = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("British Columbia")
                        .withIsoCode("BC")));

        PRINCE_EDWARD_ISLAND = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Prince Edward Island")
                        .withIsoCode("PE")));

        SASKATCHEWAN = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Saskatchewan")
                        .withIsoCode("SK")));

        ALBERTA = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Alberta")
                        .withIsoCode("AB")));

        NEWFOUNDLAND_AND_LABRADOR = new State(this, new RegionInstance<>(State.class)
                .withIdentity(new RegionIdentity("Newfoundland and Labrador")
                        .withIsoCode("NL")));
    }
}
