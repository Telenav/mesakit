////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.mesakit.graph.traffic.roadsection.codings.ngx;

import com.telenav.kivakit.data.formats.csv.CsvColumn;
import com.telenav.kivakit.data.formats.csv.CsvReader;
import com.telenav.kivakit.data.formats.csv.CsvSchema;
import com.telenav.kivakit.kernel.data.conversion.string.primitive.LongConverter;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.values.count.Estimate;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.primitive.collections.array.scalars.LongArray;
import com.telenav.kivakit.primitive.collections.map.multi.dynamic.LongToLongMultiMap;
import com.telenav.kivakit.primitive.collections.map.scalars.LongToLongMap;
import com.telenav.kivakit.resource.Resource;
import com.telenav.mesakit.graph.traffic.project.GraphTrafficLimits;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionCodingSystem;
import com.telenav.mesakit.graph.traffic.roadsection.RoadSectionIdentifier;
import com.telenav.mesakit.graph.traffic.roadsection.codings.tmc.TmcCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WayIdentifierDictionary
{
    public static final CsvColumn<TmcCode> COLUMN_TMC_ID = CsvColumn.of("tmc id");

    public static final CsvColumn<String> COLUMN_WAY_IDS = CsvColumn.of("way ids");

    public static final CsvSchema SCHEMA = CsvSchema.of(COLUMN_TMC_ID, COLUMN_WAY_IDS);

    public static class Configuration
    {
        private List<Loader> loaders = new ArrayList<>();

        private Boolean enabled;

        public void addLoader(final WayIdentifierDictionary.Loader loader)
        {
            loaders.add(loader);
        }

        public void enabled(final Boolean enabled)
        {
            this.enabled = enabled;
        }

        public Boolean isEnabled()
        {
            return enabled;
        }

        public List<WayIdentifierDictionary.Loader> loaders()
        {
            return loaders;
        }

        public void loaders(final List<WayIdentifierDictionary.Loader> loaders)
        {
            this.loaders = loaders;
        }
    }

    public static class Loader
    {
        private static final Logger LOGGER = LoggerFactory.newLogger();

        private final Resource dictionaryFile;

        private final RoadSectionCodingSystem codingSystem;

        private final String region;

        private final TmcCode.Converter tmcConverter = new TmcCode.Converter(LOGGER);

        private final LongConverter wayIdConverter = new LongConverter(LOGGER);

        public Loader(final Resource dictionaryFile, final RoadSectionCodingSystem codingSystem, final String region)
        {
            this.dictionaryFile = dictionaryFile;
            this.codingSystem = codingSystem;
            this.region = region;
        }

        public RoadSectionCodingSystem codingSystem()
        {
            return codingSystem;
        }

        public LongToLongMap load(final LongToLongMultiMap reverseMap)
        {
            final var dictionary = new LongToLongMap("dictionary");
            dictionary.initialSize(GraphTrafficLimits.ESTIMATED_TRAFFIC_EDGES);
            dictionary.initialize();

            try (final var reader = new CsvReader(dictionaryFile, SCHEMA, ',', ProgressReporter.NULL))
            {
                reader.lineNumber();
                while (reader.hasNext())
                {
                    final var line = reader.next();
                    final var tmcId = line.get(COLUMN_TMC_ID, tmcConverter).asIdentifier(false).value()
                            .asLong();

                    final var wayIds = line.get(COLUMN_WAY_IDS).split("\\|");
                    final var identifiers = new LongArray("identifiers");
                    identifiers.initialSize(Estimate._4);
                    identifiers.initialize();

                    for (final var wayId : wayIds)
                    {
                        final var converted = wayIdConverter.convert(wayId);
                        if (converted != null)
                        {
                            final long wayIdentifier = converted;
                            identifiers.add(wayIdentifier);
                            dictionary.put(wayIdentifier, tmcId);
                        }
                    }
                    reverseMap.putAll(tmcId, identifiers);
                }
                LOGGER.information("road section for way id dictionary loaded: ${debug}", dictionary.size());
            }
            catch (final Exception e)
            {
                LOGGER.warning(e, "Failed to load road section for way id dictionary.");
            }
            return dictionary;
        }

        public String region()
        {
            return region;
        }
    }

    final Map<String, Map<RoadSectionCodingSystem, LongToLongMap>> dictionary = new HashMap<>();

    final Map<String, Map<RoadSectionCodingSystem, LongToLongMultiMap>> reverseDictionary = new HashMap<>();

    private final Configuration configuration;

    public WayIdentifierDictionary(final Configuration configuration)
    {
        this.configuration = configuration;
    }

    public void load()
    {
        for (final var loader : configuration.loaders())
        {
            final var reverseMap = new LongToLongMultiMap("reverseMap");
            reverseMap.initialSize(Estimate._65536);
            reverseMap.initialChildSize(Estimate._128);
            reverseMap.initialize();

            final var map = loader.load(reverseMap);

            final var dictionaryOfRegion = dictionary.computeIfAbsent(loader.region(), k -> new HashMap<>());
            dictionaryOfRegion.put(loader.codingSystem(), map);

            final var reverseDictionaryOfRegion = reverseDictionary.computeIfAbsent(loader.region(), k -> new HashMap<>());
            reverseDictionaryOfRegion.put(loader.codingSystem(), reverseMap);
        }
    }

    public RoadSectionIdentifier roadSectionForWayId(final long wayId, final RoadSectionCodingSystem codingSystem,
                                                     final String region)
    {
        final var dictionaryOfRegion = dictionary.get(region);
        if (dictionaryOfRegion != null)
        {
            final var map = dictionaryOfRegion.get(codingSystem);
            if (map != null)
            {
                final var identifier = map.get(wayId);
                if (!map.isNull(identifier))
                {
                    return RoadSectionIdentifier.forCodingSystemAndIdentifier(codingSystem, identifier, false);
                }
            }
        }
        return null;
    }

    public LongArray wayIdsForRoadSection(final long roadSectionIdentifier, final RoadSectionCodingSystem codingSystem,
                                          final String region)
    {
        final var dictionaryOfRegion = reverseDictionary.get(region);
        if (dictionaryOfRegion != null)
        {
            final var map = dictionaryOfRegion.get(codingSystem);
            if (map != null && map.containsKey(roadSectionIdentifier))
            {
                return map.get(roadSectionIdentifier);
            }
        }
        return null;
    }
}
