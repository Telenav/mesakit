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

package com.telenav.mesakit.map.region.locale;

import com.telenav.kivakit.core.language.module.PackageReference;
import com.telenav.kivakit.core.language.reflection.Type;
import com.telenav.kivakit.core.locale.Locale;
import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.kivakit.core.locale.LocaleRegion;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.object.Lazy;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.regions.Country;

import java.util.Collection;

import static com.telenav.kivakit.core.collections.list.ObjectList.objectList;
import static com.telenav.kivakit.core.locale.LocaleLanguage.CHINESE_MANDARIN;
import static com.telenav.kivakit.core.locale.LocaleLanguage.ENGLISH;
import static com.telenav.kivakit.core.locale.LocaleLanguage.FRENCH;
import static com.telenav.kivakit.core.locale.LocaleLanguage.GERMAN;
import static com.telenav.kivakit.core.locale.LocaleLanguage.INDONESIAN;
import static com.telenav.kivakit.core.locale.LocaleLanguage.PORTUGUESE;
import static com.telenav.kivakit.core.locale.LocaleLanguage.SPANISH;
import static com.telenav.kivakit.core.locale.LocaleRegion.INDONESIA;
import static com.telenav.kivakit.core.logging.LoggerFactory.newLogger;
import static com.telenav.kivakit.core.object.Lazy.lazy;
import static com.telenav.mesakit.map.region.regions.Country.BRAZIL;
import static com.telenav.mesakit.map.region.regions.Country.CANADA;
import static com.telenav.mesakit.map.region.regions.Country.CHINA;
import static com.telenav.mesakit.map.region.regions.Country.FRANCE;
import static com.telenav.mesakit.map.region.regions.Country.GERMANY;
import static com.telenav.mesakit.map.region.regions.Country.MEXICO;
import static com.telenav.mesakit.map.region.regions.Country.PORTUGAL;
import static com.telenav.mesakit.map.region.regions.Country.SPAIN;
import static com.telenav.mesakit.map.region.regions.Country.UNITED_KINGDOM;
import static com.telenav.mesakit.map.region.regions.Country.UNITED_STATES;
import static com.telenav.mesakit.map.region.regions.World.WORLD;

/**
 * A locale
 *
 * @author jonathanl (shibo)
 */
@SuppressWarnings("unused")
public class MapLocale extends Locale
{
    public static final Lazy<MapLocale> ENGLISH_UNITED_STATES = lazy(() -> new MapLocale(LocaleRegion.UNITED_STATES, UNITED_STATES, ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_CANADA = lazy(() -> new MapLocale(LocaleRegion.CANADA, CANADA, ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_UNITED_KINGDOM = lazy(() -> new MapLocale(LocaleRegion.UNITED_KINGDOM, UNITED_KINGDOM, ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_WORLD = lazy(() -> new MapLocale(LocaleRegion.WORLD, WORLD, ENGLISH));

    public static final Lazy<MapLocale> SPANISH_MEXICO = lazy(() -> new MapLocale(LocaleRegion.MEXICO, MEXICO, SPANISH));

    public static final Lazy<MapLocale> SPANISH_SPAIN = lazy(() -> new MapLocale(LocaleRegion.SPAIN, SPAIN, SPANISH));

    public static final Lazy<MapLocale> SPANISH_WORLD = lazy(() -> new MapLocale(LocaleRegion.WORLD, WORLD, SPANISH));

    public static final Lazy<MapLocale> FRENCH_FRANCE = lazy(() -> new MapLocale(LocaleRegion.FRANCE, FRANCE, FRENCH));

    public static final Lazy<MapLocale> FRENCH_CANADA = lazy(() -> new MapLocale(LocaleRegion.CANADA, CANADA, FRENCH));

    public static final Lazy<MapLocale> FRENCH_WORLD = lazy(() -> new MapLocale(LocaleRegion.WORLD, WORLD, FRENCH));

    public static final Lazy<MapLocale> GERMAN_GERMANY = lazy(() -> new MapLocale(LocaleRegion.GERMANY, GERMANY, GERMAN));

    public static final Lazy<MapLocale> PORTUGUESE_BRAZIL = lazy(() -> new MapLocale(LocaleRegion.BRAZIL, BRAZIL, PORTUGUESE));

    public static final Lazy<MapLocale> PORTUGUESE_PORTUGAL = lazy(() -> new MapLocale(LocaleRegion.PORTUGAL, PORTUGAL, PORTUGUESE));

    public static final Lazy<MapLocale> MANDARIN_CHINA = lazy(() -> new MapLocale(LocaleRegion.CHINA, CHINA, CHINESE_MANDARIN));

    public static final Lazy<MapLocale> INDONESIA_INDONESIAN = lazy(() -> new MapLocale(INDONESIA, Country.INDONESIA, INDONESIAN));

    private static final Logger LOGGER = newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final Region<?> region;

    public MapLocale(LocaleRegion localeRegion, Country country)
    {
        this(localeRegion, country, country.instance().languages());
    }

    public MapLocale(LocaleRegion localeRegion, Region<?> region, Collection<LocaleLanguage> languages)
    {
        super(localeRegion, languages);
        this.region = region;
    }

    public MapLocale(LocaleRegion localeRegion, Region<?> region, LocaleLanguage language)
    {
        this(localeRegion, region, objectList(language));
    }

    /**
     * Returns an object created by loading the class which has the class name relative to the given package path of
     * "[package-path].locales.[language].[Language][Region][suffix]". For example,
     * "[package-path].locales.english.English_United_States_RoadNameParser"
     */
    @SuppressWarnings("unchecked")
    public <T> T create(PackageReference packageReference, String suffix)
    {
        // Try to load most specific first
        try
        {
            var languagePackage = packageReference + ".locales." + primaryLanguage().name().toLowerCase();
            var language = primaryLanguage().name().replaceAll("[_ ]", "");
            var region = region().name().replaceAll("[_ ]", "");
            var className = language + region + suffix;
            return (T) Type.typeForName(languagePackage + "." + className).newInstance();
        }
        catch (Exception e)
        {
            DEBUG.trace(e, "Locale sensitive '$' is not supported for $", suffix, this);
        }

        // Try to load language without region
        try
        {
            var language = packageReference + ".locales." + primaryLanguage().name().toLowerCase();
            return (T) Type.typeForName(language + "." + languages() + "" + suffix).newInstance();
        }
        catch (Exception e)
        {
            DEBUG.trace(e, "Locale sensitive '$' is not supported for $", suffix, this);
        }

        return null;
    }

    public Region<?> mapRegion()
    {
        return region;
    }

    @Override
    public String toString()
    {
        return languages() + ", " + region.fileName();
    }
}
