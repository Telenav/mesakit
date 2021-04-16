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
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.project.MapRegionLimits;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegions;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.core.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.lexakai.annotations.UmlClassDiagram;

import java.util.Collection;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

/**
 * @author Jonathan Locke
 */
@UmlClassDiagram(diagram = DiagramRegions.class)
public class MetropolitanArea extends Region<MetropolitanArea>
{
    static RegionBorderCache<MetropolitanArea> borderCache;

    private static Logger LOGGER;

    private static Debug DEBUG;

    public static Collection<MetropolitanArea> all()
    {
        return type(MetropolitanArea.class).all();
    }

    public static RegionBorderCache<MetropolitanArea> borderCache()
    {
        if (borderCache == null)
        {
            final var settings = new BorderCache.Settings<MetropolitanArea>()
                    .withType(MetropolitanArea.class)
                    .withMaximumObjects(MapRegionLimits.METROPOLITAN_AREAS)
                    .withMaximumPolygonsPerObject(MapRegionLimits.POLYGONS_PER_METROPOLITAN_AREA)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(MetropolitanArea.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings));
        }
        return borderCache;
    }

    public static MetropolitanArea forIdentifier(final RegionIdentifier identifier)
    {
        return type(MetropolitanArea.class).forIdentifier(identifier);
    }

    public static MetropolitanArea forIdentity(final RegionIdentity identity)
    {
        return type(MetropolitanArea.class).forIdentity(identity);
    }

    public static MetropolitanArea forLocation(final Location location)
    {
        return type(MetropolitanArea.class).forLocation(location);
    }

    public static MetropolitanArea forRegionCode(final RegionCode code)
    {
        return type(MetropolitanArea.class).forRegionCode(code);
    }

    public static Collection<MetropolitanArea> matching(final Matcher<MetropolitanArea> matcher)
    {
        return type(MetropolitanArea.class).matching(matcher);
    }

    public static SwitchParser.Builder<MetropolitanArea> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(MetropolitanArea.class).name(name)
                .converter(new Converter<>(LOGGER())).description(description);
    }

    public MetropolitanArea(final State state, final RegionInstance<MetropolitanArea> instance)
    {
        super(state, instance.prefix("Metro").prefix(state));
    }

    public State state()
    {
        return (State) parent();
    }

    @Override
    public Class<?> subclass()
    {
        return MetropolitanArea.class;
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

    private static BaseExtractor<MetropolitanArea, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public MetropolitanArea onExtract(final PbfWay way)
            {
                // Get name and ISO code
                final var name = name(way).normalized();
                final var code = code(way, "code");

                if (name == null)
                {
                    DEBUG().quibble("Way $ has no name tag", way);
                    return null;
                }

                if (code == null || code.size() < 3)
                {
                    DEBUG().quibble("Way $ doesn't have a metro ISO tag", way);
                    return null;
                }

                final var state = State.forRegionCode(code.first(2));
                if (state == null)
                {
                    DEBUG().quibble("Can't find or construct state '$' for $", code.first(2), way);
                    return null;
                }

                // Construct region identity
                final var identity = new RegionIdentity(name.code());

                // Create new region so code gets hierarchically populated and
                // inserted into the RegionType cache for this region type
                final var instance = new RegionInstance<>(MetropolitanArea.class)
                        .withLanguages(state.languages())
                        .withIdentity(identity);

                // Create new instance of the metro area. If it doesn't already exist (as happens
                // with regions that have multiple boundary ways), it will be added to the list of
                // children of the parent.
                final var possiblyNew = new MetropolitanArea(state, instance);

                // Return either some old instance or possibly the new one
                final var area = forIdentity(possiblyNew.identity());
                ensure(area != null);
                return area;
            }
        };
    }
}
