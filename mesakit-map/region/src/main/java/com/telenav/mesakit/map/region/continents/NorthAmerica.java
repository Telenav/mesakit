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

package com.telenav.mesakit.map.region.continents;

import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.countries.Canada;
import com.telenav.mesakit.map.region.countries.Mexico;
import com.telenav.mesakit.map.region.countries.UnitedStates;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode;

import static com.telenav.kivakit.core.kernel.language.locales.CountryIsoCode.*;

public class NorthAmerica extends Continent
{
    public NorthAmerica()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("North America")
                        .withIsoCode("NA")));
    }

    @Override
    protected void onInitialize()
    {
        Country.UNITED_STATES = new UnitedStates();
        Country.UNITED_STATES.initialize();

        Country.CANADA = new Canada();
        Country.CANADA.initialize();

        Country.MEXICO = new Mexico();
        Country.MEXICO.initialize();

        Country.ANGUILLA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Anguilla")
                        .withCountryOrdinal(4)
                        .withCountryIsoCode(ANGUILLA))) {};

        Country.SAINT_BARTHELEMY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Barthelemy")
                        .withCountryOrdinal(26)
                        .withCountryIsoCode(SAINT_BARTHELEMY))) {};

        Country.BERMUDA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bermuda")
                        .withCountryOrdinal(27)
                        .withCountryIsoCode(BERMUDA))) {};

        Country.BAHAMAS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bahamas")
                        .withCountryOrdinal(31)
                        .withCountryIsoCode(BAHAMAS))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.CUBA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cuba")
                        .withCountryOrdinal(50)
                        .withCountryIsoCode(CUBA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.DOMINICAN_REPUBLIC = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Dominican Republic")
                        .withCountryOrdinal(59)
                        .withCountryIsoCode(DOMINICAN_REPUBLIC))) {};

        Country.HAITI = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Haiti")
                        .withCountryOrdinal(97)
                        .withCountryIsoCode(HAITI))) {};

        Country.JAMAICA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Jamaica")
                        .withCountryOrdinal(110)
                        .withCountryIsoCode(JAMAICA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.SAINT_KITTS_AND_NEVIS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Kitts and Nevis")
                        .withCountryOrdinal(118)
                        .withCountryIsoCode(SAINT_KITTS_AND_NEVIS))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.CAYMAN_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cayman Islands")
                        .withCountryOrdinal(122)
                        .withCountryIsoCode(CAYMAN_ISLANDS))) {};

        Country.SAINT_MARTIN_FRENCH_PART = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Martin French Part")
                        .withCountryOrdinal(139)
                        .withCountryIsoCode(SAINT_MARTIN_FRENCH_PART))) {};

        Country.SAINT_PIERRE_AND_MIQUELON = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Pierre and Miquelon")
                        .withCountryOrdinal(178)
                        .withCountryIsoCode(SAINT_PIERRE_AND_MIQUELON))) {};

        Country.PUERTO_RICO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Puerto Rico")
                        .withCountryOrdinal(180)
                        .withCountryIsoCode(PUERTO_RICO))
                .withLanguage(LanguageIsoCode.SPANISH)
                .withLanguage(LanguageIsoCode.ENGLISH)) {};

        Country.SVALBARD_AND_JAN_MAYEN_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Svalbard and Jan Mayen Islands")
                        .withCountryOrdinal(199)
                        .withCountryIsoCode(SVALBARD_AND_JAN_MAYEN_ISLANDS))) {};

        Country.EL_SALVADOR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("El Salvador")
                        .withCountryOrdinal(207)
                        .withCountryIsoCode(EL_SALVADOR))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.TURKS_AND_CAICOS_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Turks and Caicos Islands")
                        .withCountryOrdinal(210)
                        .withCountryIsoCode(TURKS_AND_CAICOS_ISLANDS))) {};

        Country.UNITED_STATES_MINOR_OUTLYING_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("United States Minor Outlying Islands")
                        .withCountryOrdinal(228)
                        .withCountryIsoCode(UNITED_STATES_MINOR_OUTLYING_ISLANDS))) {};

        Country.VIRGIN_ISLANDS_BRITISH = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Virgin Islands British")
                        .withCountryOrdinal(235)
                        .withCountryIsoCode(VIRGIN_ISLANDS_BRITISH))) {};

        Country.VIRGIN_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Virgin Islands")
                        .withCountryOrdinal(236)
                        .withCountryIsoCode(VIRGIN_ISLANDS))) {};
    }
}
