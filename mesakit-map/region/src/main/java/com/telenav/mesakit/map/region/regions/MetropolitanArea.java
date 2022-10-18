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
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentifier;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.RegionLimits;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegions;

import java.util.Collection;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

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
            var settings = new BorderCache.Settings<MetropolitanArea>()
                    .withType(MetropolitanArea.class)
                    .withMaximumObjects(RegionLimits.METROPOLITAN_AREAS)
                    .withMaximumPolygonsPerObject(RegionLimits.POLYGONS_PER_METROPOLITAN_AREA)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(MetropolitanArea.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings));
        }
        return borderCache;
    }

    public static MetropolitanArea forIdentifier(RegionIdentifier identifier)
    {
        return type(MetropolitanArea.class).forIdentifier(identifier);
    }

    public static MetropolitanArea forIdentity(RegionIdentity identity)
    {
        return type(MetropolitanArea.class).forIdentity(identity);
    }

    public static MetropolitanArea forLocation(Location location)
    {
        return type(MetropolitanArea.class).forLocation(location);
    }

    public static MetropolitanArea forRegionCode(RegionCode code)
    {
        return type(MetropolitanArea.class).forRegionCode(code);
    }

    public static Collection<MetropolitanArea> matching(Matcher<MetropolitanArea> matcher)
    {
        return type(MetropolitanArea.class).matching(matcher);
    }

    public static SwitchParser.Builder<MetropolitanArea> metropolitanAreaSwitchParser(String name,
                                                                                      String description)
    {
        return SwitchParser.switchParser(MetropolitanArea.class).name(name)
                .converter(new Converter<>(LOGGER())).description(description);
    }

    public MetropolitanArea(State state, RegionInstance<MetropolitanArea> instance)
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
            public MetropolitanArea onExtract(PbfWay way)
            {
                // Get name and ISO code
                var name = Region.name(way).normalized();
                var code = code(way, "code");

                if (name == null)
                {
                    DEBUG().glitch("Way $ has no name tag", way);
                    return null;
                }

                if (code == null || code.size() < 3)
                {
                    DEBUG().glitch("Way $ doesn't have a metro ISO tag", way);
                    return null;
                }

                var state = State.forRegionCode(code.first(2));
                if (state == null)
                {
                    DEBUG().glitch("Can't find or construct state '$' for $", code.first(2), way);
                    return null;
                }

                // Construct region identity
                var identity = new RegionIdentity(name.code());

                // Create new region so code gets hierarchically populated and
                // inserted into the RegionType cache for this region type
                var instance = new RegionInstance<>(MetropolitanArea.class)
                        .withLanguages(state.languages())
                        .withIdentity(identity);

                // Create new instance of the metro area. If it doesn't already exist (as happens
                // with regions that have multiple boundary ways), it will be added to the list of
                // children of the parent.
                var possiblyNew = new MetropolitanArea(state, instance);

                // Return either some old instance or possibly the new one
                var area = forIdentity(possiblyNew.identity());
                ensure(area != null);
                return area;
            }
        };
    }
}
