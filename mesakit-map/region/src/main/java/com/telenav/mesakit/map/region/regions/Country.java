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

package com.telenav.mesakit.map.region.regions;

import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.reflection.property.IncludeProperty;
import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.kivakit.core.locale.LocaleRegion;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.ObjectFormatter;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.value.identifier.IntegerIdentifier;
import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.extraction.Extractor;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.interfaces.value.LongValued;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.RegionLimits;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.countries.Canada;
import com.telenav.mesakit.map.region.countries.Mexico;
import com.telenav.mesakit.map.region.countries.UnitedStates;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "DuplicatedCode", "unused" })
@UmlClassDiagram(diagram = DiagramRegions.class)
@UmlExcludeSuperTypes(LongValued.class)
public abstract class Country extends Region<Country> implements LongValued
{
    public static Country AFGHANISTAN;

    public static Country ANDORRA;

    public static Country UNITED_ARAB_EMIRATES;

    public static Country ANTIGUA_AND_BARBUDA;

    public static Country ANGUILLA;

    public static Country ALBANIA;

    public static Country ARMENIA;

    public static Country NETHERLANDS_ANTILLES;

    public static Country ANGOLA;

    public static Country ANTARCTICA;

    public static Country ARGENTINA;

    public static Country AMERICAN_SAMOA;

    public static Country AUSTRIA;

    public static Country AUSTRALIA;

    public static Country ARUBA;

    public static Country ALAND;

    public static Country AZERBAIJAN;

    public static Country BOSNIA_AND_HERZEGOVINA;

    public static Country BARBADOS;

    public static Country BANGLADESH;

    public static Country BELGIUM;

    public static Country BURKINA_FASO;

    public static Country BULGARIA;

    public static Country BAHRAIN;

    public static Country BURUNDI;

    public static Country BENIN;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country SAINT_BARTHELEMY;

    public static Country BERMUDA;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country BRUNEI_DARUSSALAM;

    public static Country BOLIVIA;

    public static Country BRAZIL;

    public static Country BAHAMAS;

    public static Country BHUTAN;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country BOUVET_ISLAND;

    public static Country BOTSWANA;

    public static Country BELARUS;

    public static Country BELIZE;

    public static Canada CANADA;

    public static Country COCOS_KEELING_ISLANDS;

    public static Country CONGO_KINSHASA;

    public static Country CENTRAL_AFRICAN_REPUBLIC;

    public static Country CONGO_BRAZZAVILLE;

    public static Country SWITZERLAND;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country COTE_D_IVOIRE;

    public static Country COOK_ISLANDS;

    public static Country CHILE;

    public static Country CAMEROON;

    public static Country CHINA;

    public static Country COLOMBIA;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country COSTA_RICA;

    public static Country CUBA;

    public static Country CAPE_VERDE;

    public static Country CHRISTMAS_ISLAND;

    public static Country CYPRUS;

    public static Country CZECH_REPUBLIC;

    public static Country GERMANY;

    public static Country DJIBOUTI;

    public static Country DENMARK;

    public static Country DOMINICA;

    public static Country DOMINICAN_REPUBLIC;

    public static Country ALGERIA;

    public static Country ECUADOR;

    public static Country ESTONIA;

    public static Country EGYPT;

    public static Country WESTERN_SAHARA;

    public static Country ERITREA;

    public static Country SPAIN;

    public static Country ETHIOPIA;

    public static Country FINLAND;

    public static Country FIJI;

    public static Country FALKLAND_ISLANDS;

    public static Country FRENCH_SOUTHERN_AND_ANTARCTIC_LANDS;

    public static Country MICRONESIA;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country FAROE_ISLANDS;

    public static Country FRANCE;

    public static Country GABON;

    public static Country UNITED_KINGDOM;

    public static Country GRENADA;

    public static Country GEORGIA;

    public static Country FRENCH_GUIANA;

    public static Country GUERNSEY;

    public static Country GHANA;

    public static Country GIBRALTAR;

    public static Country GREENLAND;

    public static Country GAMBIA;

    public static Country GUINEA;

    public static Country GUADELOUPE;

    public static Country EQUATORIAL_GUINEA;

    public static Country GREECE;

    public static Country SOUTH_GEORGIA_AND_SOUTH_SANDWICH_ISLANDS;

    public static Country GUATEMALA;

    public static Country GUAM;

    public static Country GUINEA_BISSAU;

    public static Country GUYANA;

    public static Country HONG_KONG;

    public static Country HEARD_AND_MCDONALD_ISLANDS;

    public static Country HONDURAS;

    public static Country CROATIA;

    public static Country HAITI;

    public static Country HUNGARY;

    public static Country INDONESIA;

    public static Country IRELAND;

    public static Country ISRAEL;

    public static Country ISLE_OF_MAN;

    public static Country INDIA;

    public static Country BRITISH_INDIAN_OCEAN_TERRITORY;

    public static Country IRAQ;

    public static Country IRAN;

    public static Country ICELAND;

    public static Country ITALY;

    public static Country JERSEY;

    public static Country JAMAICA;

    public static Country JORDAN;

    public static Country JAPAN;

    public static Country KENYA;

    public static Country KYRGYZSTAN;

    public static Country CAMBODIA;

    public static Country KIRIBATI;

    public static Country COMOROS;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country SAINT_KITTS_AND_NEVIS;

    public static Country KOREA_NORTH;

    public static Country KOREA_SOUTH;

    public static Country KUWAIT;

    public static Country CAYMAN_ISLANDS;

    public static Country KAZAKHSTAN;

    public static Country LAOS;

    public static Country LEBANON;

    public static Country SAINT_LUCIA;

    public static Country LIECHTENSTEIN;

    public static Country SRI_LANKA;

    public static Country LIBERIA;

    public static Country LESOTHO;

    public static Country LITHUANIA;

    public static Country LUXEMBOURG;

    public static Country LATVIA;

    public static Country LIBYA;

    public static Country MOROCCO;

    public static Country MONACO;

    public static Country MOLDOVA;

    public static Country MONTENEGRO;

    public static Country SAINT_MARTIN_FRENCH_PART;

    public static Country MADAGASCAR;

    public static Country MARSHALL_ISLANDS;

    public static Country MACEDONIA;

    public static Country MALI;

    public static Country MYANMAR;

    public static Country MONGOLIA;

    public static Country MACAU;

    public static Country NORTHERN_MARIANA_ISLANDS;

    public static Country MARTINIQUE;

    public static Country MAURITANIA;

    public static Country MONTSERRAT;

    public static Country MALTA;

    public static Country MAURITIUS;

    public static Country MALDIVES;

    public static Country MALAWI;

    public static Mexico MEXICO;

    public static Country MALAYSIA;

    public static Country MOZAMBIQUE;

    public static Country NAMIBIA;

    public static Country NEW_CALEDONIA;

    public static Country NIGER;

    public static Country NORFOLK_ISLAND;

    public static Country NIGERIA;

    public static Country NICARAGUA;

    public static Country NETHERLANDS;

    public static Country NORWAY;

    public static Country NEPAL;

    public static Country NAURU;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country NIUE;

    public static Country NEW_ZEALAND;

    public static Country OMAN;

    public static Country PANAMA;

    public static Country PERU;

    public static Country FRENCH_POLYNESIA;

    public static Country PAPUA_NEW_GUINEA;

    public static Country PHILIPPINES;

    public static Country PAKISTAN;

    public static Country POLAND;

    public static Country SAINT_PIERRE_AND_MIQUELON;

    public static Country PITCAIRN;

    public static Country PUERTO_RICO;

    public static Country PALESTINE;

    public static Country PORTUGAL;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country PALAU;

    public static Country PARAGUAY;

    public static Country QATAR;

    public static Country REUNION;

    public static Country ROMANIA;

    public static Country SERBIA;

    public static Country RUSSIA;

    public static Country RWANDA;

    public static Country SAUDI_ARABIA;

    public static Country SOLOMON_ISLANDS;

    public static Country SEYCHELLES;

    public static Country SUDAN;

    public static Country SOUTH_SUDAN;

    public static Country SWEDEN;

    public static Country SINGAPORE;

    public static Country SAINT_HELENA;

    public static Country SLOVENIA;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country SVALBARD_AND_JAN_MAYEN_ISLANDS;

    public static Country SLOVAKIA;

    public static Country SIERRA_LEONE;

    public static Country SAN_MARINO;

    public static Country SENEGAL;

    public static Country SOMALIA;

    public static Country SURINAME;

    public static Country SAO_TOME_AND_PRINCIPE;

    public static Country EL_SALVADOR;

    public static Country SYRIA;

    public static Country SWAZILAND;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country TURKS_AND_CAICOS_ISLANDS;

    public static Country CHAD;

    public static Country FRENCH_SOUTHERN_LANDS;

    public static Country TOGO;

    public static Country THAILAND;

    public static Country TAJIKISTAN;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country TOKELAU;

    public static Country TIMOR_LESTE;

    public static Country TURKMENISTAN;

    public static Country TUNISIA;

    public static Country TONGA;

    public static Country TURKEY;

    public static Country TRINIDAD_AND_TOBAGO;

    public static Country TUVALU;

    public static Country TAIWAN;

    public static Country TANZANIA;

    public static Country UKRAINE;

    public static Country UGANDA;

    public static Country UNITED_STATES_MINOR_OUTLYING_ISLANDS;

    public static UnitedStates UNITED_STATES;

    public static Country URUGUAY;

    public static Country UZBEKISTAN;

    public static Country VATICAN;

    public static Country SAINT_VINCENT_AND_THE_GRENADINES;

    public static Country VENEZUELA;

    public static Country VIRGIN_ISLANDS_BRITISH;

    public static Country VIRGIN_ISLANDS;

    public static Country VIETNAM;

    public static Country VANUATU;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country WALLIS_AND_FUTUNA_ISLANDS;

    public static Country SAMOA;

    public static Country YEMEN;

    @SuppressWarnings("SpellCheckingInspection")
    public static Country MAYOTTE;

    public static Country SOUTH_AFRICA;

    public static Country ZAMBIA;

    public static Country ZIMBABWE;

    static RegionBorderCache<Country> borderCache;

    private static Logger LOGGER;

    private static Set<Country> islands;

    private static final Map<String, Country> isoToCountry = new HashMap<>();

    public static Collection<Country> all()
    {
        return type(Country.class).all();
    }

    public static Collection<Country> allLargerThan(Area minimum)
    {
        return matching(country ->
        {
            var border = country.largestBorder();
            return border != null && border.bounds().area().isGreaterThan(minimum);
        });
    }

    public static Collection<Country> allSupported()
    {
        return allLargerThan(Area.squareMiles(0));
    }

    public static RegionInstance<Country> baseCountry()
    {
        return new RegionInstance<>(Country.class)
                .withAutomotiveSupportLevel(AutomotiveSupportLevel.UNSUPPORTED)
                .withDrivingSide(DrivingSide.RIGHT);
    }

    public static RegionIdentity baseRegionCode()
    {
        return new RegionIdentity().withCountryTmcCode(CountryTmcCode.UNKNOWN);
    }

    public static RegionBorderCache<Country> borderCache()
    {
        if (borderCache == null)
        {
            var settings = new BorderCache.Settings<Country>()
                    .withType(Country.class)
                    .withMaximumObjects(RegionLimits.COUNTRIES)
                    .withMaximumPolygonsPerObject(RegionLimits.POLYGONS_PER_COUNTRY)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(Country.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        PbfTagMap relationTags,
                        Collection<Border<Country>> objects)
                {
                    if (relationTags.containsKey("ISO3166-1"))
                    {
                        for (var country : objects)
                        {
                            country.region().assignName(relationTags.get("ISO3166-1"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static SwitchParser.Builder<Country> countrySwitchParser(String name, String description)
    {
        return SwitchParser.switchParser(Country.class).name(name).description(description).converter(new Converter(LOGGER()));
    }

    public static Country forIdentifier(RegionIdentifier identifier)
    {
        return type(Country.class).forIdentifier(identifier);
    }

    public static Country forIdentity(RegionIdentity identity)
    {
        return type(Country.class).forRegionCode(identity.iso().first(1));
    }

    public static Country forIsoCode(String iso)
    {
        // Look in the map for an existing country for the code,
        var country = isoToCountry.get(iso);
        if (country == null)
        {
            // and if there isn't one, parse the iso string into a region code
            var code = RegionCode.parse(iso);
            if (code != null)
            {
                // get the country for that code
                country = forRegionCode(code);

                // and store it in the map for the future
                isoToCountry.put(iso, country);
            }
        }
        return country;
    }

    /**
     * Returns the Country that the given location is in, or null if the location is not in any country.
     */
    public static Country forLocation(Location location)
    {
        return type(Country.class).forLocation(location);
    }

    /**
     * Returns the country for the given numeric code
     */
    public static Country forNumericCountryCode(int code)
    {
        return type(Country.class).forNumericCountryCode(code);
    }

    public static Country forRegionCode(RegionCode code)
    {
        return type(Country.class).forRegionCode(code);
    }

    public static synchronized Set<Country> islands()
    {
        if (islands == null)
        {
            islands = new HashSet<>();
            islands.add(ANTIGUA_AND_BARBUDA);
            islands.add(BAHAMAS);
            islands.add(BARBADOS);
            islands.add(BERMUDA);
            islands.add(CAPE_VERDE);
            islands.add(CAYMAN_ISLANDS);
            islands.add(COMOROS);
            islands.add(CUBA);
            islands.add(CYPRUS);
            islands.add(DOMINICA);
            islands.add(DOMINICAN_REPUBLIC);
            islands.add(FIJI);
            islands.add(GRENADA);
            islands.add(HAITI);
            islands.add(ICELAND);
            islands.add(MAURITIUS);
            islands.add(NETHERLANDS_ANTILLES);
            islands.add(SAINT_KITTS_AND_NEVIS);
            islands.add(SAINT_VINCENT_AND_THE_GRENADINES);
        }
        return islands;
    }

    protected static List<LocaleLanguage> languages(LocaleLanguage... languages)
    {
        List<LocaleLanguage> list = new ArrayList<>();
        Collections.addAll(list, languages);
        return list;
    }

    public static Set<Country> largeCountries()
    {
        Set<Country> large = new HashSet<>();
        large.add(UNITED_STATES);
        large.add(GERMANY);
        return large;
    }

    public static Collection<Country> matching(Matcher<Country> matcher)
    {
        List<Country> all = new ArrayList<>();
        for (var country : all())
        {
            if (matcher.matches(country))
            {
                all.add(country);
            }
        }
        return all;
    }

    public enum AutomotiveSupportLevel
    {
        SUPPORTED,
        UNSUPPORTED,
        UNDER_DEVELOPMENT
    }

    public enum DrivingSide
    {
        LEFT,
        RIGHT
    }

    public static class Converter extends BaseStringConverter<Country>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(Country country)
        {
            return country.identity().mesakit().code();
        }

        @Override
        protected Country onToValue(String country)
        {
            if (!Strings.isNullOrEmpty(country) && !"NULL".equalsIgnoreCase(country))
            {
                var regions = Region.allRegionsMatching(country);
                if (!regions.isEmpty())
                {
                    if (regions.first() instanceof Country)
                    {
                        return (Country) regions.first();
                    }
                }
                var code = RegionCode.parse(country);
                if (code != null)
                {
                    var region = Region.globalForRegionCode(code);
                    if (region instanceof Country)
                    {
                        return (Country) region;
                    }
                }
                LOGGER.warning("Country '$' couldn't be found", country);
                return null;
            }
            return null;
        }
    }

    public static class CountryTmcCode extends IntegerIdentifier
    {
        public static final CountryTmcCode UNKNOWN = new CountryTmcCode(-1);

        public CountryTmcCode(int value)
        {
            super(value);
        }
    }

    private final LocaleRegion localeRegion;

    protected Country(Continent continent, LocaleRegion localeRegion, RegionInstance<Country> instance)
    {
        super(continent, instance);
        this.localeRegion = localeRegion;
    }

    protected Country(Continent continent, RegionInstance<Country> instance)
    {
        this(continent, instance != null && instance.locale() != null ? instance.locale().region() : null, instance);
    }

    @Override
    public String asString()
    {
        return new ObjectFormatter(this).toString();
    }

    @Override
    public boolean contains(Location location)
    {
        for (var polygon : borders())
        {
            if (polygon.contains(location))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    @IncludeProperty
    public Continent continent()
    {
        return (Continent) parent();
    }

    @IncludeProperty
    public DrivingSide drivingSide()
    {
        return instance().drivingSide();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Country)
        {
            var that = (Country) object;
            return identity().equals(that.identity());
        }
        return false;
    }

    @Override
    public boolean isIsland()
    {
        return islands().contains(this);
    }

    @Override
    public LocaleRegion localeRegion()
    {
        return localeRegion;
    }

    @Override
    public final long longValue()
    {
        return identity().identifier().asLong();
    }

    @IncludeProperty
    public Iterable<State> states()
    {
        return children(State.class);
    }

    @Override
    public Class<?> subclass()
    {
        return Country.class;
    }

    private static Logger LOGGER()
    {
        if (LOGGER == null)
        {
            LOGGER = LoggerFactory.newLogger();
        }
        return LOGGER;
    }

    private static Extractor<Country, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public Country onExtract(PbfWay way)
            {
                var iso = isoCode(way);
                if (iso != null)
                {
                    return forRegionCode(iso.first());
                }
                return null;
            }
        };
    }
}
