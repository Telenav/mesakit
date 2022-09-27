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
import com.telenav.mesakit.map.region.regions.Country.CountryTmcCode;
import com.telenav.mesakit.map.region.regions.Country.DrivingSide;

import static com.telenav.kivakit.core.locale.LocaleCountry.ALAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.ALBANIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.ANDORRA;
import static com.telenav.kivakit.core.locale.LocaleCountry.ARMENIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.AUSTRIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.BELARUS;
import static com.telenav.kivakit.core.locale.LocaleCountry.BELGIUM;
import static com.telenav.kivakit.core.locale.LocaleCountry.BOSNIA_AND_HERZEGOVINA;
import static com.telenav.kivakit.core.locale.LocaleCountry.BULGARIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.CROATIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.CYPRUS;
import static com.telenav.kivakit.core.locale.LocaleCountry.CZECH_REPUBLIC;
import static com.telenav.kivakit.core.locale.LocaleCountry.DENMARK;
import static com.telenav.kivakit.core.locale.LocaleCountry.ESTONIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.FAROE_ISLANDS;
import static com.telenav.kivakit.core.locale.LocaleCountry.FINLAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.FRANCE;
import static com.telenav.kivakit.core.locale.LocaleCountry.GERMANY;
import static com.telenav.kivakit.core.locale.LocaleCountry.GIBRALTAR;
import static com.telenav.kivakit.core.locale.LocaleCountry.GREECE;
import static com.telenav.kivakit.core.locale.LocaleCountry.GREENLAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.GUERNSEY;
import static com.telenav.kivakit.core.locale.LocaleCountry.HUNGARY;
import static com.telenav.kivakit.core.locale.LocaleCountry.ICELAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.IRELAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.ISLE_OF_MAN;
import static com.telenav.kivakit.core.locale.LocaleCountry.ITALY;
import static com.telenav.kivakit.core.locale.LocaleCountry.JERSEY;
import static com.telenav.kivakit.core.locale.LocaleCountry.LATVIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.LIECHTENSTEIN;
import static com.telenav.kivakit.core.locale.LocaleCountry.LITHUANIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.LUXEMBOURG;
import static com.telenav.kivakit.core.locale.LocaleCountry.MACEDONIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.MALTA;
import static com.telenav.kivakit.core.locale.LocaleCountry.MOLDOVA;
import static com.telenav.kivakit.core.locale.LocaleCountry.MONACO;
import static com.telenav.kivakit.core.locale.LocaleCountry.MONTENEGRO;
import static com.telenav.kivakit.core.locale.LocaleCountry.NETHERLANDS;
import static com.telenav.kivakit.core.locale.LocaleCountry.NORWAY;
import static com.telenav.kivakit.core.locale.LocaleCountry.POLAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.PORTUGAL;
import static com.telenav.kivakit.core.locale.LocaleCountry.ROMANIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.RUSSIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.SAN_MARINO;
import static com.telenav.kivakit.core.locale.LocaleCountry.SERBIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.SLOVAKIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.SLOVENIA;
import static com.telenav.kivakit.core.locale.LocaleCountry.SPAIN;
import static com.telenav.kivakit.core.locale.LocaleCountry.SWEDEN;
import static com.telenav.kivakit.core.locale.LocaleCountry.SWITZERLAND;
import static com.telenav.kivakit.core.locale.LocaleCountry.TURKEY;
import static com.telenav.kivakit.core.locale.LocaleCountry.UKRAINE;
import static com.telenav.kivakit.core.locale.LocaleCountry.UNITED_KINGDOM;
import static com.telenav.kivakit.core.locale.LocaleCountry.VATICAN;

public class Europe extends Continent
{
    public Europe()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("Europe")
                        .withIsoCode("EU")));
    }

    @Override
    public void onInitialize()
    {
        Country.ANDORRA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Andorra")//
                        .withCountryOrdinal(1)
                        .withCountryIsoCode(ANDORRA))) {};

        Country.ALBANIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Albania")
                        .withCountryOrdinal(5)
                        .withCountryIsoCode(ALBANIA))) {};

        Country.ARMENIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Armenia")
                        .withCountryOrdinal(6)
                        .withCountryIsoCode(ARMENIA))) {};

        Country.AUSTRIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Austria")
                        .withCountryOrdinal(12)
                        .withCountryIsoCode(AUSTRIA))) {};

        Country.ALAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Aland")
                        .withCountryOrdinal(15)
                        .withCountryIsoCode(ALAND))) {};

        Country.BOSNIA_AND_HERZEGOVINA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bosnia and Herzegovina")
                        .withCountryOrdinal(17)
                        .withCountryIsoCode(BOSNIA_AND_HERZEGOVINA))) {};

        Country.BELGIUM = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Belgium")
                        .withCountryOrdinal(20)
                        .withCountryIsoCode(BELGIUM))) {};

        Country.BULGARIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bulgaria")
                        .withCountryOrdinal(22)
                        .withCountryIsoCode(BULGARIA))) {};

        Country.BELARUS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Belarus")
                        .withCountryOrdinal(35)
                        .withCountryIsoCode(BELARUS))) {};

        Country.SWITZERLAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Switzerland")
                        .withCountryOrdinal(42)
                        .withCountryIsoCode(SWITZERLAND))) {};

        Country.CYPRUS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cyprus")
                        .withCountryOrdinal(53)
                        .withCountryIsoCode(CYPRUS))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.CZECH_REPUBLIC = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Czech Republic")
                        .withCountryOrdinal(54)
                        .withCountryIsoCode(CZECH_REPUBLIC))) {};

        Country.GERMANY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Germany")
                        .withCountryOrdinal(55)
                        .withCountryTmcCode(new CountryTmcCode(0x0d))
                        .withCountryIsoCode(GERMANY))
                .withLanguage(LocaleLanguage.GERMAN)) {};

        Country.DENMARK = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Denmark")
                        .withCountryOrdinal(57)
                        .withCountryIsoCode(DENMARK))) {};

        Country.ESTONIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Estonia")
                        .withCountryOrdinal(62)
                        .withCountryIsoCode(ESTONIA))) {};

        Country.SPAIN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Spain")
                        .withCountryOrdinal(66)
                        .withCountryIsoCode(SPAIN))
                .withLanguage(LocaleLanguage.SPANISH)) {};

        Country.FINLAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Finland")
                        .withCountryOrdinal(68)
                        .withCountryIsoCode(FINLAND))) {};

        //noinspection SpellCheckingInspection
        Country.FAROE_ISLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Faroe Islands")
                        .withCountryOrdinal(72)
                        .withCountryIsoCode(FAROE_ISLANDS))) {};

        Country.FRANCE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("France")
                        .withCountryOrdinal(73)
                        .withCountryIsoCode(FRANCE))
                .withLanguage(LocaleLanguage.FRENCH)) {};

        Country.UNITED_KINGDOM = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("United Kingdom")
                        .withCountryOrdinal(75)
                        .withCountryTmcCode(new CountryTmcCode(0x0c))
                        .withCountryIsoCode(UNITED_KINGDOM))
                .withLanguage(LocaleLanguage.ENGLISH)
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.GUERNSEY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guernsey")
                        .withCountryOrdinal(79)
                        .withCountryIsoCode(GUERNSEY))) {};

        Country.GIBRALTAR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Gibraltar")
                        .withCountryOrdinal(81)
                        .withCountryIsoCode(GIBRALTAR))) {};

        Country.GREENLAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Greenland")
                        .withCountryOrdinal(82)
                        .withCountryIsoCode(GREENLAND))) {};

        Country.GREECE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Greece")
                        .withCountryOrdinal(87)
                        .withCountryIsoCode(GREECE))) {};

        Country.CROATIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Croatia")
                        .withCountryOrdinal(96)
                        .withCountryIsoCode(CROATIA))) {};

        Country.HUNGARY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Hungary")
                        .withCountryOrdinal(98)
                        .withCountryIsoCode(HUNGARY))) {};

        Country.IRELAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Ireland")
                        .withCountryOrdinal(100)
                        .withCountryIsoCode(IRELAND))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.ISLE_OF_MAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Isle of Man")
                        .withCountryOrdinal(102)
                        .withCountryIsoCode(ISLE_OF_MAN))) {};

        Country.ICELAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Iceland")
                        .withCountryOrdinal(107)
                        .withCountryIsoCode(ICELAND))) {};

        Country.ITALY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Italy")
                        .withCountryOrdinal(108)
                        .withCountryIsoCode(ITALY))) {};

        Country.JERSEY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Jersey")
                        .withCountryOrdinal(109)
                        .withCountryIsoCode(JERSEY))) {};

        Country.LIECHTENSTEIN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Liechtenstein")
                        .withCountryOrdinal(127)
                        .withCountryIsoCode(LIECHTENSTEIN))) {};

        Country.LITHUANIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Lithuania")
                        .withCountryOrdinal(131)
                        .withCountryIsoCode(LITHUANIA))) {};

        Country.LUXEMBOURG = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Luxembourg")
                        .withCountryOrdinal(132)
                        .withCountryIsoCode(LUXEMBOURG))) {};

        Country.LATVIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Latvia")
                        .withCountryOrdinal(133)
                        .withCountryIsoCode(LATVIA))) {};

        Country.MONACO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Monaco")
                        .withCountryOrdinal(136)
                        .withCountryIsoCode(MONACO))) {};

        Country.MOLDOVA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Moldova")
                        .withCountryOrdinal(137)
                        .withCountryIsoCode(MOLDOVA))) {};

        Country.MONTENEGRO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Montenegro")
                        .withCountryOrdinal(138)
                        .withCountryIsoCode(MONTENEGRO))) {};

        Country.MACEDONIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Macedonia")
                        .withCountryOrdinal(142)
                        .withCountryIsoCode(MACEDONIA))) {};

        Country.MALTA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Malta")
                        .withCountryOrdinal(151)
                        .withCountryIsoCode(MALTA))
                .withDrivingSide(DrivingSide.LEFT)) {};

        Country.NETHERLANDS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Netherlands")
                        .withCountryOrdinal(164)
                        .withCountryIsoCode(NETHERLANDS))) {};

        Country.NORWAY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Norway")
                        .withCountryOrdinal(165)
                        .withCountryIsoCode(NORWAY))) {};

        Country.POLAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Poland")
                        .withCountryOrdinal(177)
                        .withCountryIsoCode(POLAND))) {};

        Country.PORTUGAL = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Portugal")
                        .withCountryOrdinal(182)
                        .withCountryIsoCode(PORTUGAL))
                .withLanguage(LocaleLanguage.PORTUGUESE)) {};

        Country.ROMANIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Romania")
                        .withCountryOrdinal(187)
                        .withCountryIsoCode(ROMANIA))) {};

        Country.SERBIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Serbia")
                        .withCountryOrdinal(188)
                        .withCountryIsoCode(SERBIA))) {};

        Country.RUSSIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Russia")
                        .withCountryOrdinal(189)
                        .withCountryIsoCode(RUSSIA))) {};

        Country.SWEDEN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Sweden")
                        .withCountryOrdinal(195)
                        .withCountryIsoCode(SWEDEN))) {};

        Country.SLOVENIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Slovenia")
                        .withCountryOrdinal(198)
                        .withCountryIsoCode(SLOVENIA))) {};

        Country.SLOVAKIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Slovakia")
                        .withCountryOrdinal(200)
                        .withCountryIsoCode(SLOVAKIA))) {};

        Country.SAN_MARINO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("San Marino")
                        .withCountryOrdinal(202)
                        .withCountryIsoCode(SAN_MARINO))) {};

        Country.TURKEY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Turkey")
                        .withCountryOrdinal(221)
                        .withCountryIsoCode(TURKEY))) {};

        Country.UKRAINE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Ukraine")
                        .withCountryOrdinal(226)
                        .withCountryIsoCode(UKRAINE))) {};

        Country.VATICAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Vatican")
                        .withCountryOrdinal(232)
                        .withCountryIsoCode(VATICAN))) {};
    }
}
