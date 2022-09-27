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

import com.telenav.kivakit.core.language.reflection.Type;
import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.kivakit.core.locale.Locale;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.object.Lazy;
import com.telenav.kivakit.core.language.module.PackageReference;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.World;

/**
 * A locale
 *
 * @author jonathanl (shibo)
 */
public class MapLocale extends Locale
{
    public static final Lazy<MapLocale> ENGLISH_UNITED_STATES = Lazy.of(() -> new MapLocale(Country.UNITED_STATES, LocaleLanguage.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_CANADA = Lazy.of(() -> new MapLocale(Country.CANADA, LocaleLanguage.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_UNITED_KINGDOM = Lazy.of(() -> new MapLocale(Country.UNITED_KINGDOM, LocaleLanguage.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LocaleLanguage.ENGLISH));

    public static final Lazy<MapLocale> SPANISH_MEXICO = Lazy.of(() -> new MapLocale(Country.MEXICO, LocaleLanguage.SPANISH));

    public static final Lazy<MapLocale> SPANISH_SPAIN = Lazy.of(() -> new MapLocale(Country.SPAIN, LocaleLanguage.SPANISH));

    public static final Lazy<MapLocale> SPANISH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LocaleLanguage.SPANISH));

    public static final Lazy<MapLocale> FRENCH_FRANCE = Lazy.of(() -> new MapLocale(Country.FRANCE, LocaleLanguage.FRENCH));

    public static final Lazy<MapLocale> FRENCH_CANADA = Lazy.of(() -> new MapLocale(Country.CANADA, LocaleLanguage.FRENCH));

    public static final Lazy<MapLocale> FRENCH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LocaleLanguage.FRENCH));

    public static final Lazy<MapLocale> GERMAN_GERMANY = Lazy.of(() -> new MapLocale(Country.GERMANY, LocaleLanguage.GERMAN));

    public static final Lazy<MapLocale> PORTUGUESE_BRAZIL = Lazy.of(() -> new MapLocale(Country.BRAZIL, LocaleLanguage.PORTUGUESE));

    public static final Lazy<MapLocale> PORTUGUESE_PORTUGAL = Lazy.of(() -> new MapLocale(Country.PORTUGAL, LocaleLanguage.PORTUGUESE));

    public static final Lazy<MapLocale> MANDARIN_CHINA = Lazy.of(() -> new MapLocale(Country.CHINA, LocaleLanguage.CHINESE_MANDARIN));

    public static final Lazy<MapLocale> INDONESIA = Lazy.of(() -> new MapLocale(Country.INDONESIA, LocaleLanguage.INDONESIAN));

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final Region<?> region;

    public MapLocale(Country country)
    {
        this(country, country.instance().defaultLanguage());
    }

    public MapLocale(Region<?> region, LocaleLanguage language)
    {
        super(language);
        this.region = region;
    }

    /**
     * @return An object created by loading the class which has the class name relative to the given package path of
     * "[package-path].locales.[language].[Language][Region][suffix]". For example,
     * "[package-path].locales.english.English_United_States_RoadNameParser"
     */
    @SuppressWarnings("unchecked")
    public <T> T create(PackageReference packageReference, String suffix)
    {
        // Try to load most specific first
        try
        {
            var languagePackage = packageReference + ".locales." + languages().name().toLowerCase();
            var language = languages().name().replaceAll("[_ ]", "");
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
            var language = packageReference + ".locales." + languages().name().toLowerCase();
            return (T) Type.typeForName(language + "." + languages() + "" + suffix).newInstance();
        }
        catch (Exception e)
        {
            DEBUG.trace(e, "Locale sensitive '$' is not supported for $", suffix, this);
        }

        return null;
    }

    public Region<?> region()
    {
        return region;
    }

    @Override
    public String toString()
    {
        return languages() + ", " + region.fileName();
    }
}
