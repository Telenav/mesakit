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
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionCode;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.RegionType;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.project.MapRegionLimits;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramRegions;
import com.telenav.kivakit.core.commandline.SwitchParser;
import com.telenav.kivakit.core.kernel.data.extraction.BaseExtractor;
import com.telenav.kivakit.core.kernel.language.strings.Paths;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.lexakai.annotations.UmlClassDiagram;

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
            final var settings = new BorderCache.Settings<TimeZone>()
                    .withType(TimeZone.class)
                    .withMaximumObjects(MapRegionLimits.TIME_ZONES)
                    .withMaximumPolygonsPerObject(MapRegionLimits.POLYGONS_PER_TIME_ZONE)
                    .withMinimumBorderArea(Area.squareMiles(5))
                    .withRegionExtractor(newExtractor())
                    .withRegionFactory((identity) -> identity.findOrCreateRegion(TimeZone.class));

            borderCache = LOGGER.listenTo(new RegionBorderCache<>(settings)
            {
                @Override
                protected void assignMultiPolygonIdentity(
                        final PbfTagMap relationTags,
                        final Collection<Border<TimeZone>> objects)
                {
                    if (relationTags.containsKey("code"))
                    {
                        for (final var zone : objects)
                        {
                            zone.region().name(relationTags.get("code"));
                        }
                    }
                }
            });
        }
        return borderCache;
    }

    public static TimeZone forIdentity(final RegionIdentity identity)
    {
        return type(TimeZone.class).forIdentity(identity);
    }

    public static TimeZone forLocation(final Location location)
    {
        return type(TimeZone.class).forLocation(location);
    }

    public static TimeZone forRegionCode(final RegionCode code)
    {
        return type(TimeZone.class).forRegionCode(code);
    }

    public static SwitchParser.Builder<TimeZone> switchParser(final String name, final String description)
    {
        return SwitchParser.builder(TimeZone.class).name(name).converter(new Converter<>(LOGGER()))
                .description(description);
    }

    public TimeZone(final RegionInstance<TimeZone> instance)
    {
        super(World.INSTANCE, instance.prefix("TimeZone"));
    }

    public ZoneId asZoneId()
    {
        final var code = identity().mesakit().code();
        final var zone = Paths.tail(code, "TimeZone_");
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
            public TimeZone onExtract(final PbfWay way)
            {
                final var code = code(way, "code");
                if (code == null)
                {
                    LOGGER.quibble("No code found for $", way);
                    return null;
                }

                final var identity = new RegionIdentity(code.first().code())
                        .withMesaKitCode(code.first().code());
                return identity.findOrCreateRegion(TimeZone.class);
            }
        };
    }
}
