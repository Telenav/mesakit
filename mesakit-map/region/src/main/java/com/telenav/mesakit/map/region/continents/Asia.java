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

import static com.telenav.kivakit.kernel.language.locales.CountryIsoCode.*;

public class Asia extends Continent
{
    public Asia()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("Asia")
                        .withIsoCode("AS")));
    }

    @Override
    protected void onInitialize()
    {
        Country.AFGHANISTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Afghanistan")
                        .withCountryOrdinal(0)
                        .withCountryIsoCode(AFGHANISTAN))) {};

        Country.UNITED_ARAB_EMIRATES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("United Arab Emirates")
                        .withCountryOrdinal(2)
                        .withCountryIsoCode(UNITED_ARAB_EMIRATES))) {};

        Country.AZERBAIJAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Azerbaijan")
                        .withCountryOrdinal(16)
                        .withCountryIsoCode(AZERBAIJAN))) {};

        Country.BANGLADESH = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bangladesh")
                        .withCountryOrdinal(19)
                        .withCountryIsoCode(BANGLADESH))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.BAHRAIN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bahrain")
                        .withCountryOrdinal(23)
                        .withCountryIsoCode(BAHRAIN))) {};

        Country.BRUNEI_DARUSSALAM = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Brunei Darussalam")
                        .withCountryOrdinal(28)
                        .withCountryIsoCode(BRUNEI_DARUSSALAM))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.BHUTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Bhutan")
                        .withCountryOrdinal(32)
                        .withCountryIsoCode(BHUTAN))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.CHINA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("China")
                        .withCountryOrdinal(47)
                        .withCountryTmcCode(new Country.CountryTmcCode(0x0c))
                        .withCountryIsoCode(CHINA))
                .withLanguage(LanguageIsoCode.CHINESE_MANDARIN)) {};

        Country.GEORGIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Georgia")
                        .withCountryOrdinal(77)
                        .withCountryIsoCode(GEORGIA))) {};

        Country.HONG_KONG = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Hong Kong")
                        .withCountryOrdinal(93)
                        .withCountryIsoCode(HONG_KONG))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.ISRAEL = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Israel")
                        .withCountryOrdinal(101)
                        .withCountryIsoCode(ISRAEL))) {};

        Country.INDIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("India")
                        .withCountryOrdinal(103)
                        .withCountryIsoCode(INDIA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.BRITISH_INDIAN_OCEAN_TERRITORY = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("British Indian Ocean Territory")
                        .withCountryOrdinal(104)
                        .withCountryIsoCode(BRITISH_INDIAN_OCEAN_TERRITORY))) {};

        Country.IRAQ = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Iraq")
                        .withCountryOrdinal(105)
                        .withCountryIsoCode(IRAQ))) {};

        Country.IRAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Iran")
                        .withCountryOrdinal(106)
                        .withCountryIsoCode(IRAN))) {};

        Country.JORDAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Jordan")
                        .withCountryOrdinal(111)
                        .withCountryIsoCode(JORDAN))) {};

        Country.JAPAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Japan")
                        .withCountryOrdinal(112)
                        .withCountryIsoCode(JAPAN))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.KYRGYZSTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Kyrgyzstan")
                        .withCountryOrdinal(114)
                        .withCountryIsoCode(KYRGYZSTAN))) {};

        Country.CAMBODIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cambodia")
                        .withCountryOrdinal(115)
                        .withCountryIsoCode(CAMBODIA))) {};

        Country.KOREA_NORTH = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("North Korea")
                        .withCountryOrdinal(119)
                        .withCountryIsoCode(KOREA_NORTH))) {};

        Country.KOREA_SOUTH = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("South Korea")
                        .withCountryOrdinal(120)
                        .withCountryIsoCode(KOREA_SOUTH))) {};

        Country.KUWAIT = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Kuwait")
                        .withCountryOrdinal(121)
                        .withCountryIsoCode(KUWAIT))) {};

        Country.KAZAKHSTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Kazakhstan")
                        .withCountryOrdinal(123)
                        .withCountryIsoCode(KAZAKHSTAN))) {};

        Country.LAOS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Laos")
                        .withCountryOrdinal(124)
                        .withCountryIsoCode(LAOS))) {};

        Country.LEBANON = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Lebanon")
                        .withCountryOrdinal(125)
                        .withCountryIsoCode(LEBANON))) {};

        Country.SRI_LANKA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Sri Lanka")
                        .withCountryOrdinal(128)
                        .withCountryIsoCode(SRI_LANKA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.MYANMAR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Myanmar")
                        .withCountryOrdinal(144)
                        .withCountryIsoCode(MYANMAR))) {};

        Country.MONGOLIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mongolia")
                        .withCountryOrdinal(145)
                        .withCountryIsoCode(MONGOLIA))) {};

        Country.MACAU = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Macau")
                        .withCountryOrdinal(146)
                        .withCountryIsoCode(MACAU))) {};

        Country.MALDIVES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Maldives")
                        .withCountryOrdinal(153)
                        .withCountryIsoCode(MALDIVES))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.MALAYSIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Malaysia")
                        .withCountryOrdinal(156)
                        .withCountryIsoCode(MALAYSIA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.NEPAL = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Nepal")
                        .withCountryOrdinal(166)
                        .withCountryIsoCode(NEPAL))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.OMAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Oman")
                        .withCountryOrdinal(170)
                        .withCountryIsoCode(OMAN))) {};

        Country.PHILIPPINES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Philippines")
                        .withCountryOrdinal(175)
                        .withCountryIsoCode(PHILIPPINES))) {};

        Country.PAKISTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Pakistan")
                        .withCountryOrdinal(176)
                        .withCountryIsoCode(PAKISTAN))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.PALESTINE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Palestine")
                        .withCountryOrdinal(181)
                        .withCountryIsoCode(PALESTINE))) {};

        Country.QATAR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Qatar")
                        .withCountryOrdinal(185)
                        .withCountryIsoCode(QATAR))) {};

        Country.SAUDI_ARABIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saudi Arabia")
                        .withCountryOrdinal(191)
                        .withCountryIsoCode(SAUDI_ARABIA))) {};

        Country.SINGAPORE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Singapore")
                        .withCountryOrdinal(196)
                        .withCountryIsoCode(SINGAPORE))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.SYRIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Syria")
                        .withCountryOrdinal(208)
                        .withCountryIsoCode(SYRIA))) {};

        Country.THAILAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Thailand")
                        .withCountryOrdinal(214)
                        .withCountryIsoCode(THAILAND))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.TAJIKISTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tajikistan")
                        .withCountryOrdinal(215)
                        .withCountryIsoCode(TAJIKISTAN))) {};

        Country.TURKMENISTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Turkmenistan")
                        .withCountryOrdinal(218)
                        .withCountryIsoCode(TURKMENISTAN))) {};

        Country.TAIWAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Taiwan")
                        .withCountryOrdinal(224)
                        .withCountryIsoCode(TAIWAN))) {};

        Country.UZBEKISTAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Uzbekistan")
                        .withCountryOrdinal(231)
                        .withCountryIsoCode(UZBEKISTAN))) {};

        Country.VIETNAM = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Vietnam")
                        .withCountryOrdinal(237)
                        .withCountryIsoCode(VIETNAM))) {};

        Country.YEMEN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Yemen")
                        .withCountryOrdinal(241)
                        .withCountryIsoCode(YEMEN))) {};
    }
}
