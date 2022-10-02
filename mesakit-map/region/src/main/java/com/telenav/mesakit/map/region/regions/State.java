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
import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.RegionLimits;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegions;

import java.util.Collection;
import java.util.Set;

import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

@SuppressWarnings("unused")
@UmlClassDiagram(diagram = DiagramRegions.class)
public class State extends Region<State>
{
    static RegionBorderCache<State> borderCache;

    private static Logger LOGGER;

    private static Debug DEBUG;

    public static Collection<State> all()
    {
        return type(State.class).all();
    }

    public static Collection<State> all(Matcher<State> matcher)
    {
        return type(State.class).matching(matcher);
    }

    public static RegionBorderCache<State> borderCache()
    {
        if (borderCache == null)
        {
            var settings = new BorderCache.Settings<State>()
                    .withType(State.class)
                    .withMaximumObjects(RegionLimits.STATES)
                    .withMaximumPolygonsPerObject(RegionLimits.POLYGONS_PER_STATE)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(State.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings));
        }
        return borderCache;
    }

    public static State forIdentifier(RegionIdentifier identifier)
    {
        return type(State.class).forIdentifier(identifier);
    }

    public static State forIdentity(RegionIdentity identity)
    {
        return type(State.class).forIdentity(identity);
    }

    public static State forLocation(Location location)
    {
        return type(State.class).forLocation(location);
    }

    public static State forRegionCode(RegionCode code)
    {
        return type(State.class).forRegionCode(code);
    }

    public static SwitchParser.Builder<State> stateSwitchParser(String name, String description)
    {
        return SwitchParser.switchParserBuilder(State.class).name(name).converter(new Converter<>(LOGGER()))
                .description(description);
    }

    private final ObjectList<LocaleLanguage> languages;

    public State(Country country, RegionInstance<State> instance)
    {
        super(country, instance.prefix(country));
        languages = country.languages();
    }

    public Set<City> cities()
    {
        return children(City.class);
    }

    public boolean contains(County county)
    {
        return counties().contains(county);
    }

    public boolean contains(MetropolitanArea area)
    {
        return metropolitanAreas().contains(area);
    }

    public Set<County> counties()
    {
        return children(County.class);
    }

    @Override
    public Country country()
    {
        return (Country) parent();
    }

    public County county(RegionIdentity identity)
    {
        for (var county : counties())
        {
            if (county.identity().last().equals(identity.last()))
            {
                return county;
            }
        }
        return null;
    }

    @Override
    public boolean isIsland()
    {
        return Country.UNITED_STATES.HAWAII.equals(this) || country().isIsland();
    }

    @Override
    public ObjectList<LocaleLanguage> languages()
    {
        return languages;
    }

    public MetropolitanArea metropolitanArea(RegionIdentity identifier)
    {
        for (var metropolitanArea : metropolitanAreas())
        {
            if (metropolitanArea.identity().last().equals(identifier.last()))
            {
                return metropolitanArea;
            }
        }
        return null;
    }

    public Set<MetropolitanArea> metropolitanAreas()
    {
        return children(MetropolitanArea.class);
    }

    @Override
    public Class<?> subclass()
    {
        return State.class;
    }

    private static Debug DEBUG()
    {
        if (DEBUG == null)
        {
            DEBUG = new Debug(LOGGER());
        }
        return DEBUG;
    }

    private static Logger LOGGER()
    {
        if (LOGGER == null)
        {
            LOGGER = LoggerFactory.newLogger();
        }
        return LOGGER;
    }

    private static BaseExtractor<State, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public State onExtract(PbfWay way)
            {
                // Get name and ISO codes
                var name = Region.name(way);
                if (name == null || Strings.isEmpty(name.code()))
                {
                    DEBUG().trace("Can't extract a name from $", way);
                    return null;
                }

                // Turn any dashes into underscores
                name = name.nameized();

                var iso = isoCode(way);
                if (iso == null)
                {
                    DEBUG().glitch("Way $ doesn't have any ISO code", way);
                    return null;
                }

                if (iso.size() == 1)
                {
                    iso = iso.append(name.isoized());
                }

                var mesakit = name;

                if (!iso.isState())
                {
                    DEBUG().glitch("Way $ doesn't have a state ISO code", way);
                    return null;
                }

                var country = Country.forRegionCode(iso.first());
                if (country == null)
                {
                    DEBUG().glitch("Can't find country for ISO code $", iso);
                    return null;
                }

                if (iso.size() != 2)
                {
                    DEBUG().glitch("Invalid state ISO code $", iso);
                    return null;
                }

                // Construct region identity
                var identity = new RegionIdentity(name.code())
                        .withIsoCode(iso.last().isoized())
                        .withMesaKitCode(mesakit.last().aonized());

                if (!identity.isValid())
                {
                    DEBUG().glitch("Can't construct a valid region identity from $", way);
                    return null;
                }
                else
                {
                    // Create new region so identity gets hierarchically populated and
                    // inserted into the RegionType cache for this region type
                    var instance = new RegionInstance<>(State.class)
                            .withIdentity(identity);

                    // Return either some old instance or the new one
                    var newRegion = new State(country, instance);
                    return ensureNotNull(forIdentity(newRegion.identity()));
                }
            }
        };
    }
}
