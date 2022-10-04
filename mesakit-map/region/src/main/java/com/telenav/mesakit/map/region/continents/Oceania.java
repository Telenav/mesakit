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

import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.Country.DrivingSide;

import static com.telenav.kivakit.core.locale.LocaleRegion.AMERICAN_SAMOA;
import static com.telenav.kivakit.core.locale.LocaleRegion.AUSTRALIA;
import static com.telenav.kivakit.core.locale.LocaleRegion.CHRISTMAS_ISLAND;
import static com.telenav.kivakit.core.locale.LocaleRegion.COCOS_KEELING_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleRegion.COOK_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleRegion.FIJI;
import static com.telenav.kivakit.core.locale.LocaleRegion.FRENCH_POLYNESIA;
import static com.telenav.kivakit.core.locale.LocaleRegion.GUAM;
import static com.telenav.kivakit.core.locale.LocaleRegion.INDONESIA;
import static com.telenav.kivakit.core.locale.LocaleRegion.KIRIBATI;
import static com.telenav.kivakit.core.locale.LocaleRegion.MARSHALL_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleRegion.MICRONESIA;
import static com.telenav.kivakit.core.locale.LocaleRegion.NAURU;
import static com.telenav.kivakit.core.locale.LocaleRegion.NEW_CALEDONIA;
import static com.telenav.kivakit.core.locale.LocaleRegion.NEW_ZEALAND;
import static com.telenav.kivakit.core.locale.LocaleRegion.NIUE;
import static com.telenav.kivakit.core.locale.LocaleRegion.NORFOLK_ISLAND;
import static com.telenav.kivakit.core.locale.LocaleRegion.NORTHERN_MARIANA_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleRegion.PALAU;
import static com.telenav.kivakit.core.locale.LocaleRegion.PAPUA_NEW_GUINEA;
import static com.telenav.kivakit.core.locale.LocaleRegion.PITCAIRN;
import static com.telenav.kivakit.core.locale.LocaleRegion.SAMOA;
import static com.telenav.kivakit.core.locale.LocaleRegion.SOLOMON_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleRegion.TIMOR_LESTE;
import static com.telenav.kivakit.core.locale.LocaleRegion.TOKELAU;
import static com.telenav.kivakit.core.locale.LocaleRegion.TUVALU;
import static com.telenav.kivakit.core.locale.LocaleRegion.VANUATU;
import static com.telenav.kivakit.core.locale.LocaleRegion.WALLIS_AND_FUTUNA_ISLANDS;

public class Oceania extends Continent
{
    public Oceania()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("Oceania")
                        .withIsoCode("OC")));
    }

    @Override
    public void onInitialize()
    {
        Country.AMERICAN_SAMOA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("American Samoa")
                        .withCountryOrdinal(11)
                        .withCountryIsoCode(AMERICAN_SAMOA))) {};

        Country.AUSTRALIA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Australia")
                        .withCountryOrdinal(13)
                        .withCountryIsoCode(AUSTRALIA))
                .withLocaleLanguage(LocaleLanguage.ENGLISH)
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.COCOS_KEELING_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cocos Keeling Islands")
                        .withCountryOrdinal(38)
                        .withCountryIsoCode(COCOS_KEELING_ISLANDS))) {};

        Country.COOK_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cook Islands")
                        .withCountryOrdinal(44)
                        .withCountryIsoCode(COOK_ISLANDS))) {};

        Country.CHRISTMAS_ISLAND = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Christmas Island")
                        .withCountryOrdinal(52)
                        .withCountryIsoCode(CHRISTMAS_ISLAND))) {};

        Country.FIJI = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Fiji")
                        .withCountryOrdinal(69)
                        .withCountryIsoCode(FIJI))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.MICRONESIA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Micronesia")
                        .withCountryOrdinal(71)
                        .withCountryIsoCode(MICRONESIA))) {};

        Country.GUAM = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guam")
                        .withCountryOrdinal(90)
                        .withCountryIsoCode(GUAM))) {};

        Country.INDONESIA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Indonesia")
                        .withCountryOrdinal(99)
                        .withCountryIsoCode(INDONESIA))
                .withLocaleLanguage(LocaleLanguage.INDONESIAN)
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.KIRIBATI = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Kiribati")
                        .withCountryOrdinal(116)
                        .withCountryIsoCode(KIRIBATI))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.MARSHALL_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Marshall Islands")
                        .withCountryOrdinal(141)
                        .withCountryIsoCode(MARSHALL_ISLANDS))) {};

        Country.NORTHERN_MARIANA_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Northern Mariana Islands")
                        .withCountryOrdinal(147)
                        .withCountryIsoCode(NORTHERN_MARIANA_ISLANDS))) {};

        Country.NEW_CALEDONIA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("New Caledonia")
                        .withCountryOrdinal(159)
                        .withCountryIsoCode(NEW_CALEDONIA))) {};

        Country.NORFOLK_ISLAND = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Norfolk Island")
                        .withCountryOrdinal(161)
                        .withCountryIsoCode(NORFOLK_ISLAND))) {};

        Country.NAURU = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Nauru")
                        .withCountryOrdinal(167)
                        .withCountryIsoCode(NAURU))
                .withDrivingSide(DrivingSide.LEFT)) {};

        //noinspection SpellCheckingInspection
        Country.NIUE = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Niue")
                        .withCountryOrdinal(168)
                        .withCountryIsoCode(NIUE))) {};

        Country.NEW_ZEALAND = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("New Zealand")
                        .withCountryOrdinal(169)
                        .withCountryIsoCode(NEW_ZEALAND))
                .withLocaleLanguage(LocaleLanguage.ENGLISH)
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.FRENCH_POLYNESIA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("French Polynesia")
                        .withCountryOrdinal(173)
                        .withCountryIsoCode(FRENCH_POLYNESIA))) {};

        Country.PAPUA_NEW_GUINEA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Papua New Guinea")
                        .withCountryOrdinal(174)
                        .withCountryIsoCode(PAPUA_NEW_GUINEA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.PITCAIRN = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Pitcairn")
                        .withCountryOrdinal(179)
                        .withCountryIsoCode(PITCAIRN))) {};

        //noinspection SpellCheckingInspection
        Country.PALAU = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Palau")
                        .withCountryOrdinal(183)
                        .withCountryIsoCode(PALAU))) {};

        Country.SOLOMON_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Solomon Islands")
                        .withCountryOrdinal(192)
                        .withCountryIsoCode(SOLOMON_ISLANDS))
                .withDrivingSide(DrivingSide.LEFT)) {};

        //noinspection SpellCheckingInspection
        Country.TOKELAU = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tokelau")
                        .withCountryOrdinal(216)
                        .withCountryIsoCode(TOKELAU))) {};

        Country.TIMOR_LESTE = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Timor Leste")
                        .withCountryOrdinal(217)
                        .withCountryIsoCode(TIMOR_LESTE))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.TUVALU = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tuvalu")
                        .withCountryOrdinal(223)
                        .withCountryIsoCode(TUVALU))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.VANUATU = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Vanuatu")
                        .withCountryOrdinal(238)
                        .withCountryIsoCode(VANUATU))) {};

        //noinspection SpellCheckingInspection
        Country.WALLIS_AND_FUTUNA_ISLANDS = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Wallis and Futuna Islands")
                        .withCountryOrdinal(239)
                        .withCountryIsoCode(WALLIS_AND_FUTUNA_ISLANDS))) {};

        Country.SAMOA = new Country(this, Country.baseCountry()
                .withIdentity(Country.baseRegionCode()
                        .withName("Samoa")
                        .withCountryOrdinal(240)
                        .withCountryIsoCode(SAMOA))
                .withDrivingSide(DrivingSide.LEFT)) {};
    }
}
