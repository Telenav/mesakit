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

import com.telenav.kivakit.kernel.language.locales.LanguageIsoCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.Country.CountryTmcCode;
import com.telenav.mesakit.map.region.regions.Country.DrivingSide;

import static com.telenav.kivakit.kernel.language.locales.CountryIsoCode.*;

public class SouthAmerica extends Continent
{
    public SouthAmerica()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("South America")
                        .withIsoCode("SA")));
    }

    @Override
    protected void onInitialize()
    {
        Country.ANTIGUA_AND_BARBUDA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Antigua and Barbuda")
                        .withCountryOrdinal(3)
                        .withCountryIsoCode(ANTIGUA_AND_BARBUDA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.NETHERLANDS_ANTILLES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Netherlands Antilles")
                        .withCountryOrdinal(7)
                        .withCountryIsoCode(NETHERLANDS_ANTILLES))) {};

        Country.ARGENTINA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Argentina")
                        .withCountryOrdinal(10)
                        .withCountryIsoCode(ARGENTINA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.ARUBA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Aruba")
                        .withCountryOrdinal(14)
                        .withCountryIsoCode(ARUBA))) {};

        Country.BARBADOS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Barbados")
                        .withCountryOrdinal(18)
                        .withCountryIsoCode(BARBADOS))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.BOLIVIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bolivia")
                        .withCountryOrdinal(29)
                        .withCountryIsoCode(BOLIVIA))) {};

        Country.BRAZIL = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Brazil")
                        .withCountryOrdinal(30)
                        .withCountryTmcCode(new CountryTmcCode(0x0b))
                        .withCountryIsoCode(BRAZIL))
                .withLanguage(LanguageIsoCode.PORTUGUESE)) {};

        Country.BELIZE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Belize")
                        .withCountryOrdinal(36)
                        .withCountryIsoCode(BELIZE))) {};

        Country.CHILE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Chile")
                        .withCountryOrdinal(45)
                        .withCountryIsoCode(CHILE))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.COLOMBIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Colombia")
                        .withCountryOrdinal(48)
                        .withCountryIsoCode(COLOMBIA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.COSTA_RICA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Costa Rica")
                        .withCountryOrdinal(49)
                        .withCountryIsoCode(COSTA_RICA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.DOMINICA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Dominica")
                        .withCountryOrdinal(58)
                        .withCountryIsoCode(DOMINICA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.ECUADOR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Ecuador")
                        .withCountryOrdinal(61)
                        .withCountryIsoCode(ECUADOR))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.FALKLAND_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Falkland Islands")
                        .withCountryOrdinal(70)
                        .withCountryIsoCode(FALKLAND_ISLANDS))) {};

        Country.FRENCH_SOUTHERN_AND_ANTARCTIC_LANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("French Southern and Antarctic Lands")
                        .withCountryOrdinal(246)
                        .withCountryIsoCode(FRENCH_SOUTHERN_AND_ANTARCTIC_LANDS))) {};

        Country.GRENADA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Grenada")
                        .withCountryOrdinal(76)
                        .withCountryIsoCode(GRENADA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.FRENCH_GUIANA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("French Guiana")
                        .withCountryOrdinal(78)
                        .withCountryIsoCode(FRENCH_GUIANA))) {};

        Country.GUADELOUPE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guadeloupe")
                        .withCountryOrdinal(85)
                        .withCountryIsoCode(GUADELOUPE))) {};

        Country.SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("South Georgia and South Sandwich Islands")
                        .withCountryOrdinal(88)
                        .withCountryIsoCode(SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS))) {};

        Country.GUATEMALA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guatemala")
                        .withCountryOrdinal(89)
                        .withCountryIsoCode(GUATEMALA))) {};

        Country.GUYANA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guyana")
                        .withCountryOrdinal(92)
                        .withCountryIsoCode(GUYANA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.HONDURAS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Honduras")
                        .withCountryOrdinal(95)
                        .withCountryIsoCode(HONDURAS))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.SAINT_LUCIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Lucia")
                        .withCountryOrdinal(126)
                        .withCountryIsoCode(SAINT_LUCIA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.MARTINIQUE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Martinique")
                        .withCountryOrdinal(148)
                        .withCountryIsoCode(MARTINIQUE))) {};

        Country.MONTSERRAT = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Montserrat")
                        .withCountryOrdinal(150)
                        .withCountryIsoCode(MONTSERRAT))) {};

        Country.NICARAGUA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Nicaragua")
                        .withCountryOrdinal(163)
                        .withCountryIsoCode(NICARAGUA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.PANAMA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Panama")
                        .withCountryOrdinal(171)
                        .withCountryIsoCode(PANAMA))) {};

        Country.PERU = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Peru")
                        .withCountryOrdinal(172)
                        .withCountryIsoCode(PERU))) {};

        Country.PARAGUAY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Paraguay")
                        .withCountryOrdinal(184)
                        .withCountryIsoCode(PARAGUAY))
                .withLanguage(LanguageIsoCode.SPANISH)) {};

        Country.SURINAME = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Suriname")
                        .withCountryOrdinal(205)
                        .withCountryIsoCode(SURINAME))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.TRINIDAD_AND_TOBAGO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Trinidad and Tobago")
                        .withCountryOrdinal(222)
                        .withCountryIsoCode(TRINIDAD_AND_TOBAGO))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.URUGUAY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Uruguay")
                        .withCountryOrdinal(230)
                        .withCountryIsoCode(URUGUAY))) {};

        Country.SAINT_VINCENT_AND_THE_GRENADINES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Vincent and the Grenadines")
                        .withCountryOrdinal(233)
                        .withCountryIsoCode(SAINT_VINCENT_AND_THE_GRENADINES))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.VENEZUELA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Venezuela")
                        .withCountryOrdinal(234)
                        .withCountryIsoCode(VENEZUELA))
                .withLanguage(LanguageIsoCode.SPANISH)) {};
    }
}
