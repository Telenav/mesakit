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

import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.continents.Africa;
import com.telenav.mesakit.map.region.continents.Antarctica;
import com.telenav.mesakit.map.region.continents.Asia;
import com.telenav.mesakit.map.region.continents.Europe;
import com.telenav.mesakit.map.region.continents.NorthAmerica;
import com.telenav.mesakit.map.region.continents.Oceania;
import com.telenav.mesakit.map.region.continents.SouthAmerica;
import com.telenav.mesakit.map.region.project.MapRegionLimits;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegions;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.core.kernel.data.extraction.Extractor;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@UmlClassDiagram(diagram = DiagramRegions.class)
@UmlExcludeSuperTypes(Iterable.class)
public abstract class Continent extends Region<Continent> implements Iterable<Country>
{
    public static Antarctica ANTARCTICA;

    public static Africa AFRICA;

    public static Asia ASIA;

    public static Europe EUROPE;

    public static NorthAmerica NORTH_AMERICA;

    public static SouthAmerica SOUTH_AMERICA;

    public static Oceania OCEANIA;

    private static Logger LOGGER;

    private static RegionBorderCache<Continent> borderCache;

    public static Collection<Continent> all()
    {
        return type(Continent.class).all();
    }

    public static RegionBorderCache<Continent> borderCache()
    {
        if (borderCache == null)
        {
            final var settings = new BorderCache.Settings<Continent>()
                    .withType(Continent.class)
                    .withMaximumObjects(MapRegionLimits.CONTINENTS)
                    .withMaximumPolygonsPerObject(MapRegionLimits.POLYGONS_PER_CONTINENT)
                    .withMinimumBorderArea(Area.squareMiles(1_000))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(Continent.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        final PbfTagMap relationTags,
                        final Collection<Border<Continent>> objects)
                {
                    if (relationTags.containsKey("continent"))
                    {
                        for (final var country : objects)
                        {
                            country.region().name(relationTags.get("continent"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static void create()
    {
        ANTARCTICA = new Antarctica();
        ANTARCTICA.initialize();

        AFRICA = new Africa();
        AFRICA.initialize();

        ASIA = new Asia();
        ASIA.initialize();

        EUROPE = new Europe();
        EUROPE.initialize();

        NORTH_AMERICA = new NorthAmerica();
        NORTH_AMERICA.initialize();

        SOUTH_AMERICA = new SouthAmerica();
        SOUTH_AMERICA.initialize();

        OCEANIA = new Oceania();
        OCEANIA.initialize();
    }

    public static Continent forIdentity(final RegionIdentity identity)
    {
        return type(Continent.class).forIdentity(identity);
    }

    public static Continent forLocation(final Location location)
    {
        return type(Continent.class).forLocation(location);
    }

    public static SwitchParser.Builder<Continent> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(Continent.class).name(name).converter(new Converter<>(LOGGER()))
                .description(description);
    }

    protected Continent(final RegionInstance<Continent> instance)
    {
        super(null, instance);
    }

    public List<Country> allSupportedCountries()
    {
        final List<Country> countries = new ArrayList<>();
        for (final var country : this)
        {
            final var border = country.largestBorder();
            if (border != null && border.bounds().area().isGreaterThan(Area.squareMiles(0)))
            {
                countries.add(country);
            }
        }
        Collections.sort(countries);
        return countries;
    }

    public boolean contains(final Country country)
    {
        return countries().contains(country);
    }

    public Collection<Country> countries()
    {
        return type(Country.class).all();
    }

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        for (final var polygon : borders())
        {
            if (polygon.intersects(rectangle))
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Country> iterator()
    {
        return countries().iterator();
    }

    @Override
    public Class<?> subclass()
    {
        return Continent.class;
    }

    private static Logger LOGGER()
    {
        if (LOGGER == null)
        {
            LOGGER = LoggerFactory.newLogger();
        }
        return LOGGER;
    }

    private static Extractor<Continent, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public Continent onExtract(final PbfWay way)
            {
                final var continent = way.tagValue("continent");
                if (continent != null)
                {
                    return forIdentity(new RegionIdentity(continent)
                            .withMesaKitCode(continent)
                            .withIsoCode((RegionCode) null));
                }
                return null;
            }
        };
    }
}