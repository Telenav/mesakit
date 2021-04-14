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

package com.telenav.aonia.map.region.regions;

import com.telenav.aonia.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.aonia.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.aonia.map.geography.Location;
import com.telenav.aonia.map.measurements.geographic.Area;
import com.telenav.aonia.map.region.Region;
import com.telenav.aonia.map.region.RegionCode;
import com.telenav.aonia.map.region.RegionIdentifier;
import com.telenav.aonia.map.region.RegionIdentity;
import com.telenav.aonia.map.region.RegionInstance;
import com.telenav.aonia.map.region.border.Border;
import com.telenav.aonia.map.region.border.cache.BorderCache;
import com.telenav.aonia.map.region.project.MapRegionLimits;
import com.telenav.aonia.map.region.project.lexakai.diagrams.DiagramRegions;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.core.kernel.data.extraction.Extractor;
import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.Collection;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureNotNull;

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

    public static Collection<County> all(final Matcher<County> matcher)
    {
        return type(County.class).matching(matcher);
    }

    public static RegionBorderCache<County> borderCache()
    {
        if (borderCache == null)
        {
            final var settings = new BorderCache.Settings<County>()
                    .withType(County.class)
                    .withMaximumObjects(MapRegionLimits.COUNTIES)
                    .withMaximumPolygonsPerObject(MapRegionLimits.POLYGONS_PER_COUNTY)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(County.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        final PbfTagMap relationTags,
                        final Collection<Border<County>> objects)
                {
                    if (relationTags.containsKey("CODE"))
                    {
                        for (final var country : objects)
                        {
                            country.region().name(relationTags.get("CODE"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static County forIdentifier(final RegionIdentifier identifier)
    {
        return type(County.class).forIdentifier(identifier);
    }

    public static County forIdentity(final RegionIdentity identity)
    {
        return type(County.class).forIdentity(identity);
    }

    public static County forLocation(final Location location)
    {
        return type(County.class).forLocation(location);
    }

    public static County forRegionCode(final RegionCode code)
    {
        return type(County.class).forRegionCode(code);
    }

    public static SwitchParser.Builder<County> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(County.class).name(name).converter(new Converter<>(LOGGER()))
                .description(description);
    }

    public County(final State state, final RegionInstance<County> instance)
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
            public County onExtract(final PbfWay way)
            {
                final var name = Region.name(way);
                final var code = code(way, "code");

                if (code == null)
                {
                    DEBUG().quibble("Way $ has no code tag", way);
                    return null;
                }

                final var iso = code.isoized();

                if (name == null)
                {
                    DEBUG().quibble("Way $ has no name tag", way);
                    return null;
                }

                if (iso == null || iso.size() != 3)
                {
                    DEBUG().quibble("Way $ doesn't have a county ISO code", way);
                    return null;
                }

                final var state = State.forRegionCode(iso.first(2));
                if (state == null)
                {
                    DEBUG().quibble("Can't locate parent state $ for $", iso.first(2), way);
                    return null;
                }

                // Return if it already exists
                final var area = forRegionCode(iso);
                if (area != null)
                {
                    return area;
                }

                // Construct region identity
                final var identity = new RegionIdentity(name.code())
                        .withIsoCode(iso.last());

                // Create new region so code gets hierarchically populated and
                // inserted into the RegionType cache for this region type
                final var instance = new RegionInstance<>(County.class)
                        .withIdentity(identity);

                // Return either some old instance or the new one
                final var newRegion = new County(state, instance);
                return ensureNotNull(forIdentity(newRegion.identity()));
            }
        };
    }
}
