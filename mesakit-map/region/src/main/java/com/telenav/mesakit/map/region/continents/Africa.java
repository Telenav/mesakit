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
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;

import static com.telenav.kivakit.core.kernel.language.locales.CountryIsoCode.*;

public class Africa extends Continent
{
    public Africa()
    {
        super(new RegionInstance<>(Continent.class)
                .withIdentity(new RegionIdentity("Africa")
                        .withIsoCode("AF")));
    }

    @Override
    protected void onInitialize()
    {
        Country.ANGOLA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Angola")
                        .withCountryOrdinal(8)
                        .withCountryIsoCode(ANGOLA))) {};

        Country.BURKINA_FASO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Burkina Faso")
                        .withCountryOrdinal(21)
                        .withCountryIsoCode(BURKINA_FASO))) {};

        Country.BURUNDI = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Burundi")
                        .withCountryOrdinal(24)
                        .withCountryIsoCode(BURUNDI))) {};

        Country.BENIN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Benin")
                        .withCountryOrdinal(25)
                        .withCountryIsoCode(BENIN))) {};

        Country.BOTSWANA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Botswana")
                        .withCountryOrdinal(34)
                        .withCountryIsoCode(BOTSWANA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.CONGO_KINSHASA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Congo Kinshasa")
                        .withCountryOrdinal(39)
                        .withCountryIsoCode(CONGO_KINSHASA))) {};

        Country.CENTRAL_AFRICAN_REPUBLIC = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Central African Republic")
                        .withCountryOrdinal(40)
                        .withCountryIsoCode(CENTRAL_AFRICAN_REPUBLIC))) {};

        Country.CONGO_BRAZZAVILLE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Congo Brazzaville")
                        .withCountryOrdinal(41)
                        .withCountryIsoCode(CONGO_BRAZZAVILLE))) {};

        Country.COTE_D_IVOIRE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cote d'Ivoire")
                        .withCountryOrdinal(43)
                        .withCountryIsoCode(COTE_D_IVOIRE))) {};

        Country.CAMEROON = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cameroon")
                        .withCountryOrdinal(46)
                        .withCountryIsoCode(CAMEROON))) {};

        Country.CAPE_VERDE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Cape Verde")
                        .withCountryOrdinal(51)
                        .withCountryIsoCode(CAPE_VERDE))) {};

        Country.DJIBOUTI = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Djibouti")
                        .withCountryOrdinal(56)
                        .withCountryIsoCode(DJIBOUTI))) {};

        Country.ALGERIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Algeria")
                        .withCountryOrdinal(60)
                        .withCountryIsoCode(ALGERIA))) {};

        Country.EGYPT = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Egypt")
                        .withCountryOrdinal(63)
                        .withCountryIsoCode(EGYPT))) {};

        Country.WESTERN_SAHARA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Western Sahara")
                        .withCountryOrdinal(64)
                        .withCountryIsoCode(WESTERN_SAHARA))) {};

        Country.ERITREA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Eritrea")
                        .withCountryOrdinal(65)
                        .withCountryIsoCode(ERITREA))) {};

        Country.ETHIOPIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Ethiopia")
                        .withCountryOrdinal(67)
                        .withCountryIsoCode(ETHIOPIA))) {};

        Country.GABON = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Gabon")
                        .withCountryOrdinal(74)
                        .withCountryIsoCode(GABON))) {};

        Country.GHANA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Ghana")
                        .withCountryOrdinal(80)
                        .withCountryIsoCode(GHANA))) {};

        Country.GAMBIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Gambia")
                        .withCountryOrdinal(83)
                        .withCountryIsoCode(GAMBIA))) {};

        Country.GUINEA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guinea")
                        .withCountryOrdinal(84)
                        .withCountryIsoCode(GUINEA))) {};

        Country.EQUATORIAL_GUINEA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Equatorial Guinea")
                        .withCountryOrdinal(86)
                        .withCountryIsoCode(EQUATORIAL_GUINEA))) {};

        Country.GUINEA_BISSAU = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Guinea Bissau")
                        .withCountryOrdinal(91)
                        .withCountryIsoCode(GUINEA_BISSAU))) {};

        Country.KENYA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Kenya")
                        .withCountryOrdinal(113)
                        .withCountryIsoCode(KENYA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.COMOROS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Comoros")
                        .withCountryOrdinal(117)
                        .withCountryIsoCode(COMOROS))) {};

        Country.LIBERIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Liberia")
                        .withCountryOrdinal(129)
                        .withCountryIsoCode(LIBERIA))) {};

        Country.LESOTHO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Lesotho")
                        .withCountryOrdinal(130)
                        .withCountryIsoCode(LESOTHO))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.LIBYA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Libya")
                        .withCountryOrdinal(134)
                        .withCountryIsoCode(LIBYA))) {};

        Country.MOROCCO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Morocco")
                        .withCountryOrdinal(135)
                        .withCountryIsoCode(MOROCCO))) {};

        Country.MADAGASCAR = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Madagascar")
                        .withCountryOrdinal(140)
                        .withCountryIsoCode(MADAGASCAR))) {};

        Country.MALI = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mali")
                        .withCountryOrdinal(143)
                        .withCountryIsoCode(MALI))) {};

        Country.MAURITANIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mauritania")
                        .withCountryOrdinal(149)
                        .withCountryIsoCode(MAURITANIA))) {};

        Country.MAURITIUS = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mauritius")
                        .withCountryOrdinal(152)
                        .withCountryIsoCode(MAURITIUS))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.MALAWI = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Malawi")
                        .withCountryOrdinal(154)
                        .withCountryIsoCode(MALAWI))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.MOZAMBIQUE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mozambique")
                        .withCountryOrdinal(157)
                        .withCountryIsoCode(MOZAMBIQUE))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.NAMIBIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Namibia")
                        .withCountryOrdinal(158)
                        .withCountryIsoCode(NAMIBIA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.NIGER = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Niger")
                        .withCountryOrdinal(160)
                        .withCountryIsoCode(NIGER))) {};

        Country.NIGERIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Nigeria")
                        .withCountryOrdinal(162)
                        .withCountryIsoCode(NIGERIA))) {};

        Country.REUNION = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Reunion")
                        .withCountryOrdinal(186)
                        .withCountryIsoCode(REUNION))) {};

        Country.RWANDA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Rwanda")
                        .withCountryOrdinal(190)
                        .withCountryIsoCode(RWANDA))) {};

        Country.SEYCHELLES = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Seychelles")
                        .withCountryOrdinal(193)
                        .withCountryIsoCode(SEYCHELLES))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.SUDAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Sudan")
                        .withCountryOrdinal(194)
                        .withCountryIsoCode(SUDAN))) {};

        Country.SOUTH_SUDAN = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("South Sudan")
                        .withCountryOrdinal(247)
                        .withCountryIsoCode(SOUTH_SUDAN))) {};

        Country.SAINT_HELENA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Saint Helena")
                        .withCountryOrdinal(197)
                        .withCountryIsoCode(SAINT_HELENA))) {};

        Country.SIERRA_LEONE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Sierra Leone")
                        .withCountryOrdinal(201)
                        .withCountryIsoCode(SIERRA_LEONE))) {};

        Country.SENEGAL = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Senegal")
                        .withCountryOrdinal(203)
                        .withCountryIsoCode(SENEGAL))) {};

        Country.SOMALIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Somalia")
                        .withCountryOrdinal(204)
                        .withCountryIsoCode(SOMALIA))) {};

        Country.SAO_TOME_AND_PRINCIPE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Sao Tome and Principe")
                        .withCountryOrdinal(206)
                        .withCountryIsoCode(SAO_TOME_AND_PRINCIPE))) {};

        Country.SWAZILAND = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Swaziland")
                        .withCountryOrdinal(209)
                        .withCountryIsoCode(SWAZILAND))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.CHAD = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Chad")
                        .withCountryOrdinal(211)
                        .withCountryIsoCode(CHAD))) {};

        Country.TOGO = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Togo")
                        .withCountryOrdinal(213)
                        .withCountryIsoCode(TOGO))) {};

        Country.TUNISIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tunisia")
                        .withCountryOrdinal(219)
                        .withCountryIsoCode(TUNISIA))) {};

        Country.TONGA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tonga")
                        .withCountryOrdinal(220)
                        .withCountryIsoCode(TONGA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.TANZANIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Tanzania")
                        .withCountryOrdinal(225)
                        .withCountryIsoCode(TANZANIA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.UGANDA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Uganda")
                        .withCountryOrdinal(227)
                        .withCountryIsoCode(UGANDA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.MAYOTTE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Mayotte")
                        .withCountryOrdinal(242)
                        .withCountryIsoCode(MAYOTTE))) {};

        Country.SOUTH_AFRICA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("South Africa")
                        .withCountryOrdinal(243)
                        .withCountryIsoCode(SOUTH_AFRICA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.ZAMBIA = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Zambia")
                        .withCountryOrdinal(244)
                        .withCountryIsoCode(ZAMBIA))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};

        Country.ZIMBABWE = new Country(this, Country.baseInstance()
                .withIdentity(Country.baseRegionCode()
                        .withName("Zimbabwe")
                        .withCountryOrdinal(245)
                        .withCountryIsoCode(ZIMBABWE))
                .withDrivingSide(Country.DrivingSide.LEFT)) {};
    }
}
