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
import com.telenav.kivakit.extraction.BaseExtractor;
import com.telenav.kivakit.extraction.Extractor;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.RegionLimits;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegions;

import java.util.Collection;

import static com.telenav.kivakit.commandline.SwitchParser.switchParser;
import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

/**
 * @author Jonathan Locke
 */
@UmlClassDiagram(diagram = DiagramRegions.class)
public class County extends Region<County>
{
    static RegionBorderCache<County> borderCache;

    private static Logger LOGGER;

    private static Debug DEBUG;

    public static Collection<County> all()
    {
        return type(County.class).all();
    }

    public static Collection<County> all(Matcher<County> matcher)
    {
        return type(County.class).matching(matcher);
    }

    public static RegionBorderCache<County> borderCache()
    {
        if (borderCache == null)
        {
            var settings = new BorderCache.Settings<County>()
                    .withType(County.class)
                    .withMaximumObjects(RegionLimits.COUNTIES)
                    .withMaximumPolygonsPerObject(RegionLimits.POLYGONS_PER_COUNTY)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(County.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        PbfTagMap relationTags,
                        Collection<Border<County>> objects)
                {
                    if (relationTags.containsKey("CODE"))
                    {
                        for (var country : objects)
                        {
                            country.region().assignName(relationTags.get("CODE"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static SwitchParser.Builder<County> countySwitchParser(String name, String description)
    {
        return switchParser(County.class)
                .name(name)
                .converter(new Converter<>(LOGGER(), County.class))
                .description(description);
    }

    public static County forIdentifier(RegionIdentifier identifier)
    {
        return type(County.class).forIdentifier(identifier);
    }

    public static County forIdentity(RegionIdentity identity)
    {
        return type(County.class).forIdentity(identity);
    }

    public static County forLocation(Location location)
    {
        return type(County.class).forLocation(location);
    }

    public static County forRegionCode(RegionCode code)
    {
        return type(County.class).forRegionCode(code);
    }

    public County(State state, RegionInstance<County> instance)
    {
        super(state, instance.prefix("County").prefix(state));
    }

    public State state()
    {
        return (State) parent();
    }

    @Override
    public Class<?> subclass()
    {
        return County.class;
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

    private static Extractor<County, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public County onExtract(PbfWay way)
            {
                var name = Region.name(way);
                var code = code(way, "code");

                if (code == null)
                {
                    DEBUG().glitch("Way $ has no code tag", way);
                    return null;
                }

                var iso = code.isoized();

                if (name == null)
                {
                    DEBUG().glitch("Way $ has no name tag", way);
                    return null;
                }

                if (iso == null || iso.size() != 3)
                {
                    DEBUG().glitch("Way $ doesn't have a county ISO code", way);
                    return null;
                }

                var state = State.forRegionCode(iso.first(2));
                if (state == null)
                {
                    DEBUG().glitch("Can't locate parent state $ for $", iso.first(2), way);
                    return null;
                }

                // Return if it already exists
                var area = forRegionCode(iso);
                if (area != null)
                {
                    return area;
                }

                // Construct region identity
                var identity = new RegionIdentity(name.code())
                        .withIsoCode(iso.last());

                // Create new region so code gets hierarchically populated and
                // inserted into the RegionType cache for this region type
                var instance = new RegionInstance<>(County.class)
                        .withIdentity(identity);

                // Return either some old instance or the new one
                var newRegion = new County(state, instance);
                return ensureNotNull(forIdentity(newRegion.identity()));
            }
        };
    }
}
