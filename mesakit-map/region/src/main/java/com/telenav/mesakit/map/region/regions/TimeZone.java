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
import com.telenav.kivakit.core.string.Paths;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.RegionType;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.RegionLimits;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegions;

import java.time.ZoneId;
import java.util.Collection;

/**
 * @author Jonathan Locke
 */
@UmlClassDiagram(diagram = DiagramRegions.class)
public class TimeZone extends Region<TimeZone>
{
    static RegionBorderCache<TimeZone> borderCache;

    private static Logger LOGGER;

    static
    {
        // Load these here instead of in Region.boostrap because they're big
        register(TimeZone.class, new RegionType<>(TimeZone.class)
                .withName("TimeZone")
                .withMinimumIdentifier(TIME_ZONE_IDENTIFIER_MINIMUM)
                .withMaximumIdentifier(TIME_ZONE_IDENTIFIER_MAXIMUM)
                .withBorderCache(borderCache()));

        Region.type(TimeZone.class).loadIdentities();
    }

    public static RegionBorderCache<TimeZone> borderCache()
    {
        if (borderCache == null)
        {
            var settings = new BorderCache.Settings<TimeZone>()
                    .withType(TimeZone.class)
                    .withMaximumObjects(RegionLimits.TIME_ZONES)
                    .withMaximumPolygonsPerObject(RegionLimits.POLYGONS_PER_TIME_ZONE)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(TimeZone.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        PbfTagMap relationTags,
                        Collection<Border<TimeZone>> objects)
                {
                    if (relationTags.containsKey("code"))
                    {
                        for (var zone : objects)
                        {
                            zone.region().assignName(relationTags.get("code"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static TimeZone forIdentity(RegionIdentity identity)
    {
        return type(TimeZone.class).forIdentity(identity);
    }

    public static TimeZone forLocation(Location location)
    {
        return type(TimeZone.class).forLocation(location);
    }

    public static TimeZone forRegionCode(RegionCode code)
    {
        return type(TimeZone.class).forRegionCode(code);
    }

    public static SwitchParser.Builder<TimeZone> timeZoneSwitchParser(String name, String description)
    {
        return SwitchParser.builder(TimeZone.class)
                .name(name)
                .converter(new Converter<>(LOGGER()))
                .description(description);
    }

    public TimeZone(RegionInstance<TimeZone> instance)
    {
        super(World.INSTANCE, instance.prefix("TimeZone"));
    }

    public ZoneId asZoneId()
    {
        var code = identity().mesakit().code();
        var zone = Paths.pathTail(code, "TimeZone_");
        if (zone != null)
        {
            return ZoneId.of(zone);
        }
        return null;
    }

    @Override
    public Class<?> subclass()
    {
        return TimeZone.class;
    }

    private static Logger LOGGER()
    {
        if (LOGGER == null)
        {
            LOGGER = LoggerFactory.newLogger();
        }
        return LOGGER;
    }

    private static BaseExtractor<TimeZone, PbfWay> newExtractor()
    {
        return new BaseExtractor<>(LOGGER())
        {
            @Override
            public TimeZone onExtract(PbfWay way)
            {
                var code = code(way, "code");
                if (code == null)
                {
                    LOGGER.glitch("No code found for $", way);
                    return null;
                }

                var identity = new RegionIdentity(code.first().code())
                        .withMesaKitCode(code.first().code());
                return identity.findOrCreateRegion(TimeZone.class);
            }
        };
    }
}
