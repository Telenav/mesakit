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

package com.telenav.mesakit.graph.traffic.roadsection;

import com.telenav.kivakit.configuration.lookup.Lookup;
import com.telenav.kivakit.data.formats.csv.CsvWriter;
import com.telenav.kivakit.kernel.interfaces.comparison.Matcher;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.interfaces.value.Source;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.iteration.BaseIterator;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.paths.PackagePath;
import com.telenav.kivakit.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.kernel.language.time.Time;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.kivakit.kernel.messaging.filters.operators.All;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.resource.resources.packaged.PackageResource;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.measurements.motion.Speed;
import com.telenav.mesakit.map.ui.desktop.tiles.ZoomLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoadSectionDatabase extends BaseRepeater implements NamedObject
{
    public static void load(final Listener listener, final ProgressReporter reporter, final String name)
    {
        final var configuration = new RoadSectionDatabase.Configuration();
        final var resource = PackageResource.of(PackagePath.of(com.telenav.kivakit.graph.traffic.roadsection.loaders.csv.CsvRoadSectionLoader.class), name);
        configuration.addLoader(listener.listenTo(new com.telenav.kivakit.graph.traffic.roadsection.loaders.csv.CsvRoadSectionLoader(resource, reporter)));
        final var database = listener.listenTo(new RoadSectionDatabase(configuration));
        database.load();
    }

    public enum Type
    {
        MINIMAL,
        FULL
    }

    public static class Configuration
    {
        private Boolean enabled = true;

        private Type type = Type.FULL;

        private Set<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionLoader> loaders = new HashSet<>();

        public void addLoader(final com.telenav.kivakit.graph.traffic.roadsection.RoadSectionLoader loader)
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

        public Set<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionLoader> loaders()
        {
            return loaders;
        }

        public void loaders(final Set<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionLoader> loaders)
        {
            this.loaders = loaders;
        }

        public Type type()
        {
            return type;
        }

        public void type(final Type type)
        {
            this.type = type;
        }
    }

    private static class RoadSectionIdentifierIterator extends BaseIterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier>
    {
        private final Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> sections;

        public RoadSectionIdentifierIterator(
                final Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> sections)
        {
            this.sections = sections;
        }

        @Override
        protected com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier onNext()
        {
            if (sections.hasNext())
            {
                final var section = sections.next();
                return section.identifier();
            }
            return null;
        }
    }

    private final Map<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier, Source<com.telenav.kivakit.graph.traffic.roadsection.RoadSection>> roadSectionForIdentifier = new BoundedMap<>(
            KivaKitGraphTrafficLimits.MAXIMUM_ROAD_SECTIONS);

    private final ObjectList<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier> identifiers = new ObjectList<>(
            KivaKitGraphTrafficLimits.MAXIMUM_ROAD_SECTIONS);

    /** Spatial indices by zoom level */
    private final List<RTreeSpatialIndex<com.telenav.kivakit.graph.traffic.roadsection.RoadSection>> spatialIndexes = new ArrayList<>();

    /** Spatial index for all road sections */
    private final RTreeSpatialIndex<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> spatialIndex = new RTreeSpatialIndex<>("objectName", new RTreeSettings());

    private final Configuration configuration;

    public RoadSectionDatabase(final Configuration configuration)
    {
        this.configuration = configuration;

        // Construct spatial indices
        for (var i = ZoomLevel.FURTHEST.level(); i <= ZoomLevel.CLOSEST.level(); i++)
        {
            spatialIndexes.add(new RTreeSpatialIndex<>(objectName() + ".spatialIndexes", new RTreeSettings()));
        }
    }

    public boolean exists(final com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier identifier)
    {
        return roadSectionForIdentifier.containsKey(identifier);
    }

    public List<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier> identifiers()
    {
        return identifiers;
    }

    public Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier> identifiersInside(
            final Rectangle bounds)
    {
        return new RoadSectionIdentifierIterator(inside(bounds));
    }

    public Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier> identifiersInside(
            final ZoomLevel level, final Rectangle bounds)
    {
        if (level == null)
        {
            return identifiersInside(bounds);
        }
        return new RoadSectionIdentifierIterator(inside(level, bounds));
    }

    public Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> inside(final Rectangle bounds)
    {
        return spatialIndex.intersecting(bounds).iterator();
    }

    public Iterator<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> inside(final ZoomLevel level,
                                                                                      final Rectangle bounds)
    {
        if (level == null)
        {
            return inside(bounds);
        }
        return spatialIndexForZoomLevel(level).intersecting(bounds).iterator();
    }

    public void load()
    {
        if (configuration.isEnabled() != null && !configuration.isEnabled())
        {
            return;
        }

        final Map<ZoomLevel, List<com.telenav.kivakit.graph.traffic.roadsection.RoadSection>> roadSectionsForZoomLevel = new HashMap<>();
        final List<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> all = new ArrayList<>();
        for (final var loader : configuration.loaders())
        {
            listenTo(loader);
            for (final var section : loader)
            {
                add(section);
                final var level = section.identifier().zoomLevel();
                final var sections = roadSectionsForZoomLevel.computeIfAbsent(level, k -> new ArrayList<>());
                sections.add(section);
                all.add(section);
            }
        }

        // Bulk load spatial indexes
        for (final var entry : roadSectionsForZoomLevel.entrySet())
        {
            spatialIndexForZoomLevel(entry.getKey()).bulkLoad(entry.getValue());
        }
        spatialIndex.bulkLoad(all);

        // Monitor the road section code caches so no more TMC codes can be added to the cache
        com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCode.lockCache();
        com.telenav.kivakit.graph.traffic.roadsection.codings.telenav.TelenavTrafficLocationCode.lockCache();

        Lookup.global().register(this);
    }

    public com.telenav.kivakit.graph.traffic.roadsection.RoadSection roadSectionForIdentifier(
            final com.telenav.kivakit.graph.traffic.roadsection.RoadSectionIdentifier identifier)
    {
        final var source = roadSectionForIdentifier.get(identifier);
        if (source != null)
        {
            return source.get();
        }

        final var dummy = new com.telenav.kivakit.graph.traffic.roadsection.RoadSection();
        dummy.identifier(identifier);
        dummy.start(Location.ORIGIN);
        dummy.end(Location.ORIGIN);
        dummy.freeFlowSpeed(Speed.milesPerHour(65));
        return dummy;
    }

    public Iterable<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> roadSections()
    {
        return roadSections(new All<>());
    }

    public Iterable<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> roadSections(
            final Matcher<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> matcher)
    {
        return Iterables.of(() -> new Next<>()
        {
            private final Iterator<Source<com.telenav.kivakit.graph.traffic.roadsection.RoadSection>> iterator = roadSectionForIdentifier
                    .values().iterator();

            @Override
            public com.telenav.kivakit.graph.traffic.roadsection.RoadSection onNext()
            {
                while (iterator.hasNext())
                {
                    final var section = iterator.next().get();
                    if (matcher.matches(section))
                    {
                        return section;
                    }
                }
                return null;
            }
        });
    }

    public void saveAsCsv(final File file, final ProgressReporter reporter)
    {
        final var writer = new CsvWriter(file.printWriter(), reporter, com.telenav.kivakit.graph.traffic.roadsection.RoadSection.CSV_SCHEMA);
        writer.writeComment("Saved at " + Time.now());
        for (final var section : roadSections())
        {
            section.write(writer);
        }
        writer.close();
    }

    protected void add(final com.telenav.kivakit.graph.traffic.roadsection.RoadSection roadSection)
    {
        // Get the road section identifier
        final var identifier = roadSection.identifier();
        identifiers.add(identifier);

        // If we're creating a minimal database,
        if (configuration.type() == Type.MINIMAL)
        {
            // load the minimal road section
            roadSectionForIdentifier.put(identifier, new com.telenav.kivakit.graph.traffic.roadsection.RoadSection.Minimal(roadSection));
        }
        else if (configuration.type() == Type.FULL)
        {
            // otherwise load the full road section
            roadSectionForIdentifier.put(identifier, roadSection);
        }
        else
        {
            unsupported();
        }
    }

    private RTreeSpatialIndex<com.telenav.kivakit.graph.traffic.roadsection.RoadSection> spatialIndexForZoomLevel(
            final ZoomLevel level)
    {
        return spatialIndexes.get(level.level());
    }
}
