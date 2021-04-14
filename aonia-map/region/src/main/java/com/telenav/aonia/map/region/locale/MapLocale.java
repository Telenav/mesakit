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

package com.telenav.aonia.map.region.locale;

import com.telenav.aonia.map.region.Region;
import com.telenav.aonia.map.region.regions.Country;
import com.telenav.aonia.map.region.regions.World;
import com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode;
import com.telenav.kivakit.core.kernel.language.locales.Locale;
import com.telenav.kivakit.core.kernel.language.objects.Lazy;
import com.telenav.kivakit.core.kernel.language.paths.PackagePath;
import com.telenav.kivakit.core.kernel.language.reflection.Type;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;

/**
 * A locale
 *
 * @author jonathanl (shibo)
 */
public class MapLocale extends Locale
{
    public static final Lazy<MapLocale> ENGLISH_UNITED_STATES = Lazy.of(() -> new MapLocale(Country.UNITED_STATES, LanguageIsoCode.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_CANADA = Lazy.of(() -> new MapLocale(Country.CANADA, LanguageIsoCode.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_UNITED_KINGDOM = Lazy.of(() -> new MapLocale(Country.UNITED_KINGDOM, LanguageIsoCode.ENGLISH));

    public static final Lazy<MapLocale> ENGLISH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LanguageIsoCode.ENGLISH));

    public static final Lazy<MapLocale> SPANISH_MEXICO = Lazy.of(() -> new MapLocale(Country.MEXICO, LanguageIsoCode.SPANISH));

    public static final Lazy<MapLocale> SPANISH_SPAIN = Lazy.of(() -> new MapLocale(Country.SPAIN, LanguageIsoCode.SPANISH));

    public static final Lazy<MapLocale> SPANISH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LanguageIsoCode.SPANISH));

    public static final Lazy<MapLocale> FRENCH_FRANCE = Lazy.of(() -> new MapLocale(Country.FRANCE, LanguageIsoCode.FRENCH));

    public static final Lazy<MapLocale> FRENCH_CANADA = Lazy.of(() -> new MapLocale(Country.CANADA, LanguageIsoCode.FRENCH));

    public static final Lazy<MapLocale> FRENCH_WORLD = Lazy.of(() -> new MapLocale(World.INSTANCE, LanguageIsoCode.FRENCH));

    public static final Lazy<MapLocale> GERMAN_GERMANY = Lazy.of(() -> new MapLocale(Country.GERMANY, LanguageIsoCode.GERMAN));

    public static final Lazy<MapLocale> PORTUGUESE_BRAZIL = Lazy.of(() -> new MapLocale(Country.BRAZIL, LanguageIsoCode.PORTUGUESE));

    public static final Lazy<MapLocale> PORTUGUESE_PORTUGAL = Lazy.of(() -> new MapLocale(Country.PORTUGAL, LanguageIsoCode.PORTUGUESE));

    public static final Lazy<MapLocale> MANDARIN_CHINA = Lazy.of(() -> new MapLocale(Country.CHINA, LanguageIsoCode.CHINESE_MANDARIN));

    public static final Lazy<MapLocale> INDONESIA = Lazy.of(() -> new MapLocale(Country.INDONESIA, LanguageIsoCode.INDONESIAN));

    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private final Region<?> region;

    public MapLocale(final Country country)
    {
        this(country, country.instance().defaultLanguage());
    }

    public MapLocale(final Region<?> region, final LanguageIsoCode language)
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
    public <T> T create(final PackagePath packagePath, final String suffix)
    {
        // Try to load most specific first
        try
        {
            final var languagePackage = packagePath + ".locales." + language().name().toLowerCase();
            final var language = language().name().replaceAll("[_ ]", "");
            final var region = region().name().replaceAll("[_ ]", "");
            final var className = language + region + suffix;
            return (T) Type.forName(languagePackage + "." + className).newInstance();
        }
        catch (final Exception e)
        {
            DEBUG.trace(e, "Locale sensitive '$' is not supported for $", suffix, this);
        }

        // Try to load language without region
        try
        {
            final var language = packagePath + ".locales." + language().name().toLowerCase();
            return (T) Type.forName(language + "." + language() + "" + suffix).newInstance();
        }
        catch (final Exception e)
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
        return language() + ", " + region.fileName();
    }
}
