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

package com.telenav.kivakit.graph.traffic.roadsection;

import com.telenav.kivakit.configuration.Lookup;
import com.telenav.kivakit.filesystem.File;
import com.telenav.kivakit.kernel.interfaces.naming.NamedObject;
import com.telenav.kivakit.kernel.interfaces.object.*;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.collections.map.BoundedMap;
import com.telenav.kivakit.kernel.language.iteration.*;
import com.telenav.kivakit.kernel.language.matching.All;
import com.telenav.kivakit.kernel.messaging.*;
import com.telenav.kivakit.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.kernel.operation.progress.ProgressReporter;
import com.telenav.kivakit.kernel.path.PackagePath;
import com.telenav.kivakit.kernel.time.Time;
import com.telenav.kivakit.resource.resources.packaged.PackageResource;
import com.telenav.kivakit.data.formats.library.csv.CsvWriter;
import com.telenav.kivakit.graph.traffic.project.KivaKitGraphTrafficLimits;
import com.telenav.kivakit.graph.traffic.roadsection.codings.telenav.TelenavTrafficLocationCode;
import com.telenav.kivakit.graph.traffic.roadsection.codings.tmc.TmcCode;
import com.telenav.kivakit.graph.traffic.roadsection.loaders.csv.CsvRoadSectionLoader;
import com.telenav.kivakit.map.geography.Location;
import com.telenav.kivakit.map.geography.indexing.rtree.*;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;
import com.telenav.kivakit.map.measurements.Speed;
import com.telenav.kivakit.map.ui.swing.map.tiles.ZoomLevel;

import java.util.*;

import static com.telenav.kivakit.kernel.validation.Validate.unsupported;

public class RoadSectionDatabase extends BaseRepeater<Message> implements NamedObject
{
    public static void load(final Listener<Message> listener, final ProgressReporter reporter, final String name)
    {
        final var configuration = new RoadSectionDatabase.Configuration();
        final var resource = PackageResource.of(PackagePath.of(CsvRoadSectionLoader.class), name);
        configuration.addLoader(listener.listenTo(new CsvRoadSectionLoader(resource, reporter)));
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

        private Set<RoadSectionLoader> loaders = new HashSet<>();

        public void addLoader(final RoadSectionLoader loader)
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

        public Set<RoadSectionLoader> loaders()
        {
            return loaders;
        }

        public void loaders(final Set<RoadSectionLoader> loaders)
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

    private static class RoadSectionIdentifierIterator extends BaseIterator<RoadSectionIdentifier>
    {
        private final Iterator<RoadSection> sections;

        public RoadSectionIdentifierIterator(final Iterator<RoadSection> sections)
        {
            this.sections = sections;
        }

        @Override
        protected RoadSectionIdentifier onNext()
        {
            if (sections.hasNext())
            {
                final var section = sections.next();
                return section.identifier();
            }
            return null;
        }
    }

    private final Map<RoadSectionIdentifier, Source<RoadSection>> roadSectionForIdentifier = new BoundedMap<>(
            KivaKitGraphTrafficLimits.MAXIMUM_ROAD_SECTIONS);

    private final ObjectList<RoadSectionIdentifier> identifiers = new ObjectList<>(
            KivaKitGraphTrafficLimits.MAXIMUM_ROAD_SECTIONS);

    /** Spatial indices by zoom level */
    private final List<RTreeSpatialIndex<RoadSection>> spatialIndexes = new ArrayList<>();

    /** Spatial index for all road sections */
    private final RTreeSpatialIndex<RoadSection> spatialIndex = new RTreeSpatialIndex<>("objectName", new RTreeSettings());

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

    public boolean exists(final RoadSectionIdentifier identifier)
    {
        return roadSectionForIdentifier.containsKey(identifier);
    }

    public List<RoadSectionIdentifier> identifiers()
    {
        return identifiers;
    }

    public Iterator<RoadSectionIdentifier> identifiersInside(final Rectangle bounds)
    {
        return new RoadSectionIdentifierIterator(inside(bounds));
    }

    public Iterator<RoadSectionIdentifier> identifiersInside(final ZoomLevel level, final Rectangle bounds)
    {
        if (level == null)
        {
            return identifiersInside(bounds);
        }
        return new RoadSectionIdentifierIterator(inside(level, bounds));
    }

    public Iterator<RoadSection> inside(final Rectangle bounds)
    {
        return spatialIndex.intersecting(bounds).iterator();
    }

    public Iterator<RoadSection> inside(final ZoomLevel level, final Rectangle bounds)
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

        final Map<ZoomLevel, List<RoadSection>> roadSectionsForZoomLevel = new HashMap<>();
        final List<RoadSection> all = new ArrayList<>();
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
        TmcCode.lockCache();
        TelenavTrafficLocationCode.lockCache();

        Lookup.global().register(this);
    }

    public RoadSection roadSectionForIdentifier(final RoadSectionIdentifier identifier)
    {
        final var source = roadSectionForIdentifier.get(identifier);
        if (source != null)
        {
            return source.get();
        }

        final var dummy = new RoadSection();
        dummy.identifier(identifier);
        dummy.start(Location.ORIGIN);
        dummy.end(Location.ORIGIN);
        dummy.freeFlowSpeed(Speed.milesPerHour(65));
        return dummy;
    }

    public Iterable<RoadSection> roadSections()
    {
        return roadSections(new All<>());
    }

    public Iterable<RoadSection> roadSections(final Matcher<RoadSection> matcher)
    {
        return Iterables.of(() -> new Next<>()
        {
            private final Iterator<Source<RoadSection>> iterator = roadSectionForIdentifier
                    .values().iterator();

            @Override
            public RoadSection onNext()
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
        final var writer = new CsvWriter(file.printWriter(), reporter, RoadSection.CSV_SCHEMA);
        writer.writeComment("Saved at " + Time.now());
        for (final var section : roadSections())
        {
            section.write(writer);
        }
        writer.close();
    }

    protected void add(final RoadSection roadSection)
    {
        // Get the road section identifier
        final var identifier = roadSection.identifier();
        identifiers.add(identifier);

        // If we're creating a minimal database,
        if (configuration.type() == Type.MINIMAL)
        {
            // load the minimal road section
            roadSectionForIdentifier.put(identifier, new RoadSection.Minimal(roadSection));
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

    private RTreeSpatialIndex<RoadSection> spatialIndexForZoomLevel(final ZoomLevel level)
    {
        return spatialIndexes.get(level.level());
    }
}
