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

package com.telenav.aonia.map.region;

import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.region.project.MapRegionUnitTest;
import com.telenav.aonia.map.region.regions.Country;
import com.telenav.aonia.map.region.regions.MetropolitanArea;
import com.telenav.aonia.map.region.regions.State;
import com.telenav.kivakit.core.kernel.language.locales.CountryIsoCode;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.loggers.ConsoleLogger;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.CHINESE_MANDARIN;
import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.ENGLISH;
import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.FRENCH;
import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.GERMAN;
import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.PORTUGUESE;
import static com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode.SPANISH;

public class CountryTest extends MapRegionUnitTest
{
    private static final Logger LOGGER = new ConsoleLogger();

    @Test
    public void testAttributes()
    {
        ensureEqual(Country.UNITED_STATES, Country.UNITED_STATES.WASHINGTON.country());
        ensureEqual("Canada", Country.CANADA.name());
        ensureEqual("Canada", Country.CANADA.identity().aonia().code());
        ensureEqual("CA", Country.CANADA.identity().iso().code());
    }

    @Test
    public void testCanada()
    {
        ensureFalse(Country.CANADA.contains(Location.degrees(44.997728, -73.947908)));
    }

    @Test
    public void testForLocation()
    {
        final var location = Location.degrees(39.747203, -104.987507);
        final var denverByLocation = MetropolitanArea.forLocation(location);
        ensureEqual(Country.UNITED_STATES, Country.forLocation(location));
        ensureEqual(Country.UNITED_STATES.COLORADO.metropolitanArea(new RegionIdentity("Denver Aurora")
                .withAoniaCode(code("United_States-Colorado-Metro_Denver_Aurora"))
                .withIsoCode(code("US-CO-METRODENVERAURORA"))), denverByLocation);
        ensureEqual(Country.UNITED_STATES.COLORADO, State.forLocation(location));
        ensureEqual(Country.UNITED_STATES,
                Country.forLocation(Country.UNITED_STATES.WASHINGTON.SEATTLE.DOWNTOWN.center()));
    }

    @Test
    public void testLanguages()
    {
        ensureEqual(ENGLISH, Country.UNITED_STATES.instance().defaultLanguage());
        ensureEqual(ENGLISH, Country.UNITED_KINGDOM.instance().defaultLanguage());
        ensureEqual(ENGLISH, Country.CANADA.instance().defaultLanguage());
        ensureEqual(ENGLISH, Country.AUSTRALIA.instance().defaultLanguage());
        ensureEqual(FRENCH, Country.FRANCE.instance().defaultLanguage());
        ensureEqual(GERMAN, Country.GERMANY.instance().defaultLanguage());
        ensureEqual(CHINESE_MANDARIN, Country.CHINA.instance().defaultLanguage());
        ensureEqual(PORTUGUESE, Country.BRAZIL.instance().defaultLanguage());
        ensureEqual(PORTUGUESE, Country.PORTUGAL.instance().defaultLanguage());
        ensureEqual(SPANISH, Country.MEXICO.instance().defaultLanguage());
        ensureEqual(SPANISH, Country.SPAIN.instance().defaultLanguage());
        ensureEqual(SPANISH, Country.ARGENTINA.instance().defaultLanguage());
        ensureEqual(SPANISH, Country.CHILE.instance().defaultLanguage());
    }

    @Test
    public void testNumeric()
    {
        final var map = alpha3ToNumericMap();
        map.forEach((key, value) ->
        {
            final var country = Country.forNumericCountryCode(value);
            if (country == null)
            {
                LOGGER.quibble("No country for numeric country code $", value);
            }
            ensure(country != null);
            final CountryIsoCode iso = ensureNotNull(country).identity().countryIsoCode();
            ensureEqual(key, iso.alpha3Code());
        });
    }

    private Map<String, Integer> alpha3ToNumericMap()
    {
        final Map<String, Integer> map = new LinkedHashMap<>();
        map.put("ABW", 533);
        map.put("AFG", 4);
        map.put("AGO", 24);
        map.put("AIA", 660);
        map.put("ALA", 248);
        map.put("ALB", 8);
        map.put("AND", 20);
        map.put("ANT", 530);
        map.put("ARE", 784);
        map.put("ARG", 32);
        map.put("ARM", 51);
        map.put("ASM", 16);
        map.put("ATF", 238);
        map.put("ATG", 28);
        map.put("AUS", 36);
        map.put("AUT", 40);
        map.put("AZE", 31);
        map.put("BDI", 108);
        map.put("BEL", 56);
        map.put("BEN", 204);
        map.put("BFA", 854);
        map.put("BGD", 50);
        map.put("BGR", 100);
        map.put("BHR", 48);
        map.put("BHS", 44);
        map.put("BIH", 70);
        map.put("BLM", 652);
        map.put("BLR", 112);
        map.put("BLZ", 84);
        map.put("BMU", 60);
        map.put("BOL", 68);
        map.put("BRA", 76);
        map.put("BRB", 52);
        map.put("BRN", 96);
        map.put("BTN", 64);
        map.put("BWA", 72);
        map.put("CAF", 140);
        map.put("CAN", 124);
        map.put("CCK", 166);
        map.put("CHE", 756);
        map.put("CHL", 152);
        map.put("CHN", 156);
        map.put("CIV", 384);
        map.put("CMR", 120);
        map.put("COD", 180);
        map.put("COG", 178);
        map.put("COK", 184);
        map.put("COL", 170);
        map.put("COM", 174);
        map.put("CPV", 132);
        map.put("CRI", 188);
        map.put("CUB", 192);
        map.put("CXR", 162);
        map.put("CYM", 136);
        map.put("CYP", 196);
        map.put("CZE", 203);
        map.put("DEU", 276);
        map.put("DJI", 262);
        map.put("DMA", 212);
        map.put("DNK", 208);
        map.put("DOM", 214);
        map.put("DZA", 12);
        map.put("ECU", 218);
        map.put("EGY", 818);
        map.put("ERI", 232);
        map.put("ESH", 732);
        map.put("ESP", 724);
        map.put("EST", 233);
        map.put("ETH", 231);
        map.put("FIN", 246);
        map.put("FJI", 242);
        map.put("FLK", 260);
        map.put("FRA", 250);
        map.put("FRO", 234);
        map.put("FSM", 583);
        map.put("GAB", 266);
        map.put("GBR", 826);
        map.put("GEO", 268);
        map.put("GGY", 831);
        map.put("GHA", 288);
        map.put("GIB", 292);
        map.put("GIN", 324);
        map.put("GLP", 312);
        map.put("GMB", 270);
        map.put("GNB", 624);
        map.put("GNQ", 226);
        map.put("GRC", 300);
        map.put("GRD", 308);
        map.put("GRL", 304);
        map.put("GTM", 320);
        map.put("GUF", 254);
        map.put("GUM", 316);
        map.put("GUY", 328);
        map.put("HKG", 344);
        map.put("HND", 340);
        map.put("HRV", 191);
        map.put("HTI", 332);
        map.put("HUN", 348);
        map.put("IDN", 360);
        map.put("IMN", 833);
        map.put("IND", 356);
        map.put("IOT", 86);
        map.put("IRL", 372);
        map.put("IRN", 364);
        map.put("IRQ", 368);
        map.put("ISL", 352);
        map.put("ISR", 376);
        map.put("ITA", 380);
        map.put("JAM", 388);
        map.put("JEY", 832);
        map.put("JOR", 400);
        map.put("JPN", 392);
        map.put("KAZ", 398);
        map.put("KEN", 404);
        map.put("KGZ", 417);
        map.put("KHM", 116);
        map.put("KIR", 296);
        map.put("KNA", 659);
        map.put("KOR", 410);
        map.put("KWT", 414);
        map.put("LAO", 418);
        map.put("LBN", 422);
        map.put("LBR", 430);
        map.put("LBY", 434);
        map.put("LCA", 662);
        map.put("LIE", 438);
        map.put("LKA", 144);
        map.put("LSO", 426);
        map.put("LTU", 440);
        map.put("LUX", 442);
        map.put("LVA", 428);
        map.put("MAC", 446);
        map.put("MAF", 663);
        map.put("MAR", 504);
        map.put("MCO", 492);
        map.put("MDA", 498);
        map.put("MDG", 450);
        map.put("MDV", 462);
        map.put("MEX", 484);
        map.put("MHL", 584);
        map.put("MKD", 807);
        map.put("MLI", 466);
        map.put("MLT", 470);
        map.put("MMR", 104);
        map.put("MNE", 499);
        map.put("MNG", 496);
        map.put("MNP", 580);
        map.put("MOZ", 508);
        map.put("MRT", 478);
        map.put("MSR", 500);
        map.put("MTQ", 474);
        map.put("MUS", 480);
        map.put("MWI", 454);
        map.put("MYS", 458);
        map.put("MYT", 175);
        map.put("NAM", 516);
        map.put("NCL", 540);
        map.put("NER", 562);
        map.put("NFK", 574);
        map.put("NGA", 566);
        map.put("NIC", 558);
        map.put("NIU", 570);
        map.put("NLD", 528);
        map.put("NOR", 578);
        map.put("NPL", 524);
        map.put("NRU", 520);
        map.put("NZL", 554);
        map.put("OMN", 512);
        map.put("PAK", 586);
        map.put("PAN", 591);
        map.put("PCN", 612);
        map.put("PER", 604);
        map.put("PHL", 608);
        map.put("PLW", 585);
        map.put("PNG", 598);
        map.put("POL", 616);
        map.put("PRI", 630);
        map.put("PRK", 408);
        map.put("PRT", 620);
        map.put("PRY", 600);
        map.put("PSE", 275);
        map.put("PYF", 258);
        map.put("QAT", 634);
        map.put("REU", 638);
        map.put("ROU", 642);
        map.put("RUS", 643);
        map.put("RWA", 646);
        map.put("SAU", 682);
        map.put("SDN", 729);
        map.put("SEN", 686);
        map.put("SGP", 702);
        map.put("SGS", 239);
        map.put("SHN", 654);
        map.put("SJM", 744);
        map.put("SLB", 90);
        map.put("SLE", 694);
        map.put("SLV", 222);
        map.put("SMR", 674);
        map.put("SOM", 706);
        map.put("SPM", 666);
        map.put("SRB", 688);
        map.put("STP", 678);
        map.put("SUR", 740);
        map.put("SVK", 703);
        map.put("SVN", 705);
        map.put("SWE", 752);
        map.put("SWZ", 748);
        map.put("SYC", 690);
        map.put("SYR", 760);
        map.put("TCA", 796);
        map.put("TCD", 148);
        map.put("TGO", 768);
        map.put("THA", 764);
        map.put("TJK", 762);
        map.put("TKL", 772);
        map.put("TKM", 795);
        map.put("TLS", 626);
        map.put("TON", 776);
        map.put("TTO", 780);
        map.put("TUN", 788);
        map.put("TUR", 792);
        map.put("TUV", 798);
        map.put("TWN", 158);
        map.put("TZA", 834);
        map.put("UGA", 800);
        map.put("UKR", 804);
        map.put("UMI", 581);
        map.put("URY", 858);
        map.put("USA", 840);
        map.put("UZB", 860);
        map.put("VAT", 336);
        map.put("VCT", 670);
        map.put("VEN", 862);
        map.put("VGB", 92);
        map.put("VIR", 850);
        map.put("VNM", 704);
        map.put("VUT", 548);
        map.put("WLF", 876);
        map.put("WSM", 882);
        map.put("YEM", 887);
        map.put("ZAF", 710);
        map.put("ZMB", 894);
        map.put("ZWE", 716);
        return map;
    }
}
