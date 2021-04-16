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

package com.telenav.mesakit.map.region.border.cache;

import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagMap;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.geography.Latitude;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.Longitude;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSettings;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.polyline.PolygonBuilder;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.region.Region;
import com.telenav.mesakit.map.region.RegionIdentity;
import com.telenav.mesakit.map.region.RegionInstance;
import com.telenav.mesakit.map.region.RegionType;
import com.telenav.mesakit.map.region.RegionType.RegionFactory;
import com.telenav.mesakit.map.region.border.Border;
import com.telenav.mesakit.map.region.border.BorderSpatialIndex;
import com.telenav.mesakit.map.region.project.MapRegionProject;
import com.telenav.mesakit.map.region.project.lexakai.diagrams.DiagramBorder;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.region.regions.TimeZone;
import com.telenav.mesakit.map.ui.swing.debug.debuggers.indexing.rtree.VisualRTreeSpatialIndexDebugger;
import com.telenav.mesakit.map.ui.swing.debug.viewer.swing.SwingViewer;
import com.telenav.kivakit.core.collections.map.MultiMap;
import com.telenav.kivakit.core.filesystem.File;
import com.telenav.kivakit.core.filesystem.FileCache;
import com.telenav.kivakit.core.filesystem.Folder;
import com.telenav.kivakit.core.kernel.KivaKit;
import com.telenav.kivakit.core.kernel.data.extraction.Extractor;
import com.telenav.kivakit.core.kernel.interfaces.lifecycle.Configured;
import com.telenav.kivakit.core.kernel.language.collections.CompressibleCollection;
import com.telenav.kivakit.core.kernel.language.collections.CompressibleCollection.Method;
import com.telenav.kivakit.core.kernel.language.io.IO;
import com.telenav.kivakit.core.kernel.language.objects.Objects;
import com.telenav.kivakit.core.kernel.language.primitives.Booleans;
import com.telenav.kivakit.core.kernel.language.progress.ProgressReporter;
import com.telenav.kivakit.core.kernel.language.progress.reporters.Progress;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.kernel.language.time.Time;
import com.telenav.kivakit.core.kernel.language.values.count.Bytes;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.language.values.count.Estimate;
import com.telenav.kivakit.core.kernel.language.values.count.Maximum;
import com.telenav.kivakit.core.kernel.language.values.count.MutableCount;
import com.telenav.kivakit.core.kernel.language.values.version.VersionedObject;
import com.telenav.kivakit.core.kernel.messaging.Message;
import com.telenav.kivakit.core.kernel.messaging.messages.status.Problem;
import com.telenav.kivakit.core.kernel.messaging.repeaters.BaseRepeater;
import com.telenav.kivakit.core.network.core.Host;
import com.telenav.kivakit.core.network.core.NetworkPath;
import com.telenav.kivakit.core.network.http.HttpNetworkLocation;
import com.telenav.kivakit.core.resource.Resource;
import com.telenav.kivakit.core.resource.compression.archive.ZipArchive;
import com.telenav.kivakit.core.resource.path.FileName;
import com.telenav.kivakit.core.serialization.core.SerializationSession;
import com.telenav.kivakit.core.serialization.core.SerializationSessionFactory;
import com.telenav.kivakit.core.serialization.kryo.KryoSerializationSession;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;

import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensureEqual;
import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.fail;
import static com.telenav.kivakit.core.resource.CopyMode.OVERWRITE;
import static com.telenav.kivakit.core.resource.compression.archive.ZipArchive.Mode.READ;
import static com.telenav.kivakit.core.serialization.core.SerializationSession.Type.RESOURCE;

/**
 * There should be no need for end-users of MesaKit to use {@link BorderCache} objects since they are an implementation
 * detail of different {@link Region}s to provide containment testing via {@link Region#contains(Location)}, as well as
 * other such tests.
 * <p>
 * Loads PBF border data for regions and converts it to an indexed form, {@link BorderSpatialIndex}, that can be rapidly
 * queried. The border data can be found in the mesakit-data project under the folder "boundaries". The borders can be
 * loaded with {@link #loadBorders()} and region codes associated with the borders with {@link #loadIdentities()}. The
 * borders themselves can be retrieved with {@link #borders(Region)}.
 *
 * @author jonathanl (shibo)
 * @see BorderSpatialIndex
 * @see Region
 * @see RegionType
 * @see RegionInstance
 */
@UmlClassDiagram(diagram = DiagramBorder.class)
@UmlRelation(label = "loads borders for", referent = Region.class)
public abstract class BorderCache<T extends Region<T>> extends BaseRepeater
{
    /**
     * Command line switch to enable visual debugging of RTreeSpatialIndex.add() when loading borders. This is mainly
     * useful as a demonstration of the {@link SwingViewer} and for understanding how RTreeSpatialIndex builds an RTree,
     * as the bulk loading code that is used when DEBUG=false is much more efficient and results in a more performant
     * and balanced tree.
     */
    private static final boolean DETAILED = false;

    /**
     * Set this to true to use the fast (and large) polygon spatial index implementation. Set to false to use a method
     * based on RTreeSpatialIndex.
     * <p>
     * NOTE: Once the CACHED_SPATIAL_INDEX file has been created (using this value), it will be necessary to remove the
     * cached file if you change this value again (or it will keep loading the old cached data).
     */
    private static final boolean USE_FAST_POLYGON_SPATIAL_INDEX = true;

    /**
     * Set to true to show approximate sizes of polygon objects
     */
    private static final boolean SHOW_APPROXIMATE_SIZES = false;

    /**
     * Border data path on S3
     */
    private static final NetworkPath NETWORK_PATH = Host.parse("github.com").https().path(
            Message.format("Telenav/mesakit-data/raw/master/administrative-borders/$/administrative-borders-$.jar",
                    MapRegionProject.get().borderDataVersion(), MapRegionProject.get().borderDataVersion()));

    public static class Settings<R extends Region<R>>
    {
        private Class<R> type;

        private RegionFactory<R> regionFactory;

        private Extractor<R, PbfWay> regionExtractor;

        private Maximum maximumObjects;

        private Maximum maximumPolygonsPerObject;

        private Area minimumBorderArea;

        public Settings()
        {
        }

        public Settings(final Settings<R> that)
        {
            type = that.type;
            regionFactory = that.regionFactory;
            maximumObjects = that.maximumObjects;
            maximumPolygonsPerObject = that.maximumPolygonsPerObject;
            regionExtractor = that.regionExtractor;
            minimumBorderArea = that.minimumBorderArea;
        }

        public boolean isValid()
        {
            return !Objects.isAnyNull(type, regionFactory, maximumObjects,
                    maximumPolygonsPerObject, regionExtractor, minimumBorderArea);
        }

        public Count maximumObjects()
        {
            return maximumObjects;
        }

        public Count maximumPolygonsPerObject()
        {
            return maximumPolygonsPerObject;
        }

        public Area minimumBorderArea()
        {
            return minimumBorderArea;
        }

        public Extractor<R, PbfWay> regionExtractor()
        {
            return regionExtractor;
        }

        public RegionFactory<R> regionFactory()
        {
            return regionFactory;
        }

        public Class<R> type()
        {
            return type;
        }

        public Settings<R> withMaximumObjects(final Maximum maximumObjects)
        {
            final var copy = new Settings<>(this);
            copy.maximumObjects = maximumObjects;
            return copy;
        }

        public Settings<R> withMaximumPolygonsPerObject(final Maximum maximumPolygonsPerObject)
        {
            final var copy = new Settings<>(this);
            copy.maximumPolygonsPerObject = maximumPolygonsPerObject;
            return copy;
        }

        public Settings<R> withMinimumBorderArea(final Area minimumBorderArea)
        {
            final var copy = new Settings<>(this);
            copy.minimumBorderArea = minimumBorderArea;
            return copy;
        }

        public Settings<R> withRegionExtractor(final Extractor<R, PbfWay> extractor)
        {
            final var copy = new Settings<>(this);
            copy.regionExtractor = extractor;
            return copy;
        }

        public Settings<R> withRegionFactory(final RegionFactory<R> factory)
        {
            final var copy = new Settings<>(this);
            copy.regionFactory = factory;
            return copy;
        }

        public Settings<R> withType(final Class<R> type)
        {
            final var copy = new Settings<>(this);
            copy.type = type;
            return copy;
        }
    }

    /** Session factory for serialization */
    private final SerializationSessionFactory sessionFactory;

    /**
     * RTree spatial index of polygons so we can test only the minimum number of polygon outlines when locating an
     * object for a location
     */
    @UmlAggregation(label = "indexed by")
    private BorderSpatialIndex<T> index;

    /**
     * Polygon borders for each object
     */
    private final MultiMap<Region<T>, Polygon> polygonsForRegion;

    /**
     * Cache of region identities to preload
     */
    private final RegionIdentityCache<T> identityCache;

    /**
     * Settings for the cache
     */
    private final Settings<T> settings;

    protected BorderCache(final Settings<T> settings)
    {
        ensure(settings.isValid());
        sessionFactory = MapRegionProject.get().sessionFactory();
        this.settings = settings;
        identityCache = listenTo(new RegionIdentityCache<>(settings.type()));
        polygonsForRegion = new MultiMap<>(settings.maximumObjects, settings.maximumPolygonsPerObject);
    }

    public Collection<Polygon> borders(final T object)
    {
        loadBorders();
        return polygonsForRegion.containsKey(object) ? polygonsForRegion.get(object) : Polygon.EMPTY_SET;
    }

    public synchronized boolean loadBorders()
    {
        if (index() == null)
        {
            // Ensure data is installed in cache
            trace("Loading $", name());
            installData();

            // Load borders
            if (loadBorders(pbfFile()))
            {
                for (final var border : index().all())
                {
                    polygonsForRegion.add(border.region(), border.polygon());
                }
                trace("Loaded $", name());
            }
            else
            {
                index = null;
                problem("Unable to load $", name());
            }
        }

        return index() != null;
    }

    /**
     * Load the region codes from, the serialized file, file that was created from reading borders.
     */
    @SuppressWarnings("UnusedReturnValue")
    public synchronized BorderCache<T> loadIdentities()
    {
        // If the cache file doesn't exist or we can't load it
        final var cacheFile = identitiesFile();
        if (!cacheFile.exists() || !identityCache.load(cacheFile, serializationSession()))
        {
            // then load the borders (forcing identities to be created in the process)
            trace("Loading $", name());
            if (loadBorders())
            {
                // and if we loaded okay, extract all the identities from the index
                final Set<RegionIdentity> identities = new HashSet<>();
                index().all().forEach((border) -> identities.add(border.identity()));

                // open identities cache file
                try (final var out = cacheFile.openForWriting())
                {
                    // and save the identities to it
                    final var session = serializationSession();
                    session.open(RESOURCE, KivaKit.get().version(), out);
                    identityCache.save(session, MapRegionProject.get().borderDataVersion(), identities);
                }
                catch (final Exception e)
                {
                    problem(e, "Unable to write identities");
                }
                trace("Saved identities for $", name());

                // and finally give all users access to this data
                grantAccess();
            }
        }

        return this;
    }

    public String name()
    {
        return baseName(type()).replaceAll("-", " ");
    }

    /**
     * Searches border outlines for an object that contains the given location.
     *
     * @return The object that the given location is in, or null if the location is not in any object
     */
    @SuppressWarnings("ConstantConditions")
    public T object(final Location location)
    {
        ensure(location != null);
        if (loadBorders())
        {
            for (final var border : index().intersecting(location.bounds().expanded(Distance.ONE_METER)))
            {
                if (border.contains(location))
                {
                    return border.region();
                }
            }
        }
        return null;
    }

    /**
     * @param relationTags The tags on the multi-polygon relation
     * @param objects The objects that need an identity assigned based on the relation tags
     */
    protected void assignMultiPolygonIdentity(final PbfTagMap relationTags, final Collection<Border<T>> objects)
    {
    }

    protected abstract RegionIdentity identityForRegion(T region);

    protected BorderSpatialIndex<T> index()
    {
        return index;
    }

    protected abstract T regionForIdentity(RegionIdentity identity);

    protected Settings<T> settings()
    {
        return settings;
    }

    private String baseName(final Class<?> type)
    {
        if (type.equals(Continent.class))
        {
            return "continent-borders";
        }
        if (type.equals(Country.class))
        {
            return "country-borders";
        }
        if (type.equals(State.class))
        {
            return "state-borders";
        }
        if (type.equals(MetropolitanArea.class))
        {
            return "metropolitan-area-borders";
        }
        if (type.equals(County.class))
        {
            return "county-borders";
        }
        if (type.equals(TimeZone.class))
        {
            return "time-zone-borders";
        }
        throw new IllegalStateException();
    }

    private File borderCacheFile()
    {
        return cacheFolder().file(FileName.parse(baseName(type()) + ".kryo"));
    }

    private FileCache cache()
    {
        trace("Border cache folder for ${class} is $", settings.type(), cacheFolder());
        return new FileCache(cacheFolder());
    }

    private Folder cacheFolder()
    {
        return MapRegionProject.get().mesakitMapFolder().folder("region/borders");
    }

    private void grantAccess()
    {
        cacheFolder().chmodNested(PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.GROUP_WRITE,
                PosixFilePermission.GROUP_READ);
    }

    private File identitiesFile()
    {
        return cacheFolder().file(FileName.parse(baseName(type()) + "-identities.kryo"));
    }

    private void installData()
    {
        synchronized (BorderCache.class)
        {
            // Ensure the data folder exists
            cacheFolder().mkdirs();

            // then get the jar location on S3
            final var source = new HttpNetworkLocation(NETWORK_PATH);

            // and the zip file target we're going to download to and unzip.
            final var jar = localJar(NETWORK_PATH.fileName());
            trace("Trying to open $", jar);
            var archive = ZipArchive.open(jar, ProgressReporter.NULL, READ);
            try
            {
                // If archive isn't valid,
                if (archive == null)
                {
                    trace("$ is not a valid zip archive", jar);

                    // but it exists,
                    if (jar.exists())
                    {
                        // then remove it
                        trace("Removing bad zip archive $", jar);
                        jar.delete();
                    }

                    try
                    {
                        // then try to download the data into the cache
                        information(AsciiArt.textBox("Downloading", "from: $\nto: $",
                                NETWORK_PATH.asContractedString(80), jar.path().asContractedString(80)) + "\n ");
                        final var downloadProgress = Progress.create(this, "bytes");
                        downloadProgress.start("Downloading");
                        trace("Downloading $ from $", jar, source);
                        cache().add(source.get(), OVERWRITE, downloadProgress);
                        downloadProgress.end("Downloaded");

                        // and try to open the archive again
                        trace("Trying to open $", jar);
                        archive = ZipArchive.open(jar, ProgressReporter.NULL, READ);
                    }
                    catch (final Throwable e)
                    {
                        problem(e, "Unable to download and open $", source);

                        // and if the download fails,
                        if (jar.exists())
                        {
                            // remove the invalid zip file
                            jar.delete();
                        }
                    }
                }

                // At this point the archive should exist,
                if (archive == null)
                {
                    // or we have to fail because we couldn't get the data
                    fail("Couldn't download valid border data");
                    return;
                }

                try
                {
                    // Go through each border type
                    for (final var type : new Class<?>[]
                            {
                                    Continent.class,
                                    Country.class,
                                    State.class,
                                    MetropolitanArea.class,
                                    County.class,
                                    TimeZone.class
                            })
                    {
                        // and if a decent sized PBF doesn't already exist
                        final var extracted = pbfFile(type);
                        if (!extracted.exists() || extracted.bytes().isLessThan(Bytes.kilobytes(50)))
                        {
                            // then get the entry name
                            final var zipEntryName = baseName(type) + ".osm.pbf";
                            try
                            {
                                // and extract the PBF file from the archive
                                final var entry = archive.entry(zipEntryName);
                                if (entry != null)
                                {
                                    entry.safeCopyTo(extracted, OVERWRITE, ProgressReporter.NULL);
                                }
                            }
                            catch (final Exception e)
                            {
                                // If we can't extract the file, clear the whole folder and give up
                                problem(e, "Unable to unzip $", zipEntryName);
                                cacheFolder().clearAllAndDelete();
                                throw new IllegalStateException("Unable to install border data", e);
                            }
                        }
                    }
                }
                catch (final Exception e)
                {
                    // We were unable to install for some other reason
                    cacheFolder().clearAllAndDelete();
                    throw new IllegalStateException("Unable to install border data", e);
                }
            }
            finally
            {
                IO.close(archive);
            }

            // Give all users access to this data
            grantAccess();
        }
    }

    /**
     * Loads borders from the CACHED_SPATIAL_INDEX file or from the given PBF resource if the cache is missing,
     * corrupted or out of date.
     */
    private boolean loadBorders(final Resource borders)
    {
        // If we can load the borders from serialized index file
        if (loadBordersFromCache())
        {
            // we're done,
            return true;
        }

        // otherwise Read borders from PBF file into cached index file
        return loadBordersFromPbf(borders);
    }

    /**
     * @return True if the polygon spatial index could be read from the CACHED_SPATIAL_INDEX file.
     */
    @SuppressWarnings("unchecked")
    private boolean loadBordersFromCache()
    {
        final var start = Time.now();
        if (!borderCacheFile().exists())
        {
            information("Border cache '$' does not exist, will generate it", borderCacheFile().fileName());
        }
        else
        {
            try
            {
                trace("Loading cached borders from '$'", borderCacheFile().fileName());
                final var in = borderCacheFile().openForReading();
                try
                {
                    // Read the spatial index
                    final var session = serializationSession();
                    session.open(RESOURCE, KivaKit.get().version(), in);
                    final var loaded = session.read();
                    final var cachedIndex = (BorderSpatialIndex<T>) loaded.get();
                    final var version = loaded.version();
                    trace("Cached border index is version $", version);

                    // and if it is the current version
                    if (version.equals(MapRegionProject.get().borderDataVersion()))
                    {
                        var invalid = false;
                        var borders = 0;
                        for (final var border : cachedIndex.all())
                        {
                            if (!border.isValid())
                            {
                                invalid = true;
                                break;
                            }
                            borders++;
                        }
                        if (invalid || borders == 0)
                        {
                            information("Border cache '$' is invalid or has no borders", borderCacheFile());
                        }
                        else
                        {
                            index = cachedIndex;

                            // Convert persistent string identifiers back to bounded objects
                            for (final var border : index().all())
                            {
                                final var region = regionForIdentity(border.identity());
                                if (region == null)
                                {
                                    warning("regionForIdentity returned null for $", border.identity());
                                }
                                border.region(region);
                            }

                            trace("Loaded $ $ in $", Count.count(index().all()), name(), start.elapsedSince());
                            return true;
                        }
                    }
                    else
                    {
                        information("Ignoring version $ cache file '$' since current cache version is $",
                                version, borderCacheFile().fileName(), MapRegionProject.get().borderDataVersion());
                    }
                }
                finally
                {
                    IO.close(in);
                }
            }
            catch (final Exception e)
            {
                information("Unable to load border cache file '$'", borderCacheFile().fileName());
            }
            borderCacheFile().delete();
            information("Removed invalid border cache file '$', will regenerate it", borderCacheFile().fileName());
        }
        return false;
    }

    /**
     * Reads borders from PBF resource
     *
     * @param resource The PBF resource to read
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    private boolean loadBordersFromPbf(final Resource resource)
    {
        information(AsciiArt.textBox("Building " + type().getSimpleName() + " Borders",
                "Building borders from $\nThis may take a little while...", resource.fileName()));

        final var start = Time.now();
        final Map<Long, Location> locationForIdentifier = new HashMap<>();
        final var totalSize = new MutableCount();
        final var totalLocations = new MutableCount();
        final var total = new MutableCount();
        final Map<Long, Border<T>> borderForWayIdentifier = new HashMap<>();
        final List<Border<T>> borders = new ArrayList<>();
        final var settings = new RTreeSettings()
                .withMaximumChildrenPerInteriorNode(Maximum._8)
                .withMaximumChildrenPerLeaf(Maximum._32)
                .withEstimatedChildrenPerInteriorNode(Estimate._8)
                .withEstimatedChildrenPerLeaf(Estimate._32)
                .withEstimatedNodes(Estimate._1024);

        index = new BorderSpatialIndex<>(name() + ".index", settings);

        if (isDebugOn() && Booleans.isTrue(System.getProperty("MESAKIT_VISUAL_DEBUG")))
        {
            index().debugger(new VisualRTreeSpatialIndexDebugger<>());
        }

        try
        {
            final var reader = listenTo(new SerialPbfReader(resource));
            final var outer = this;
            reader.phase("Loading");
            reader.process(new PbfDataProcessor()
            {
                private int identifier = 1;

                @Override
                public Action onNode(final PbfNode node)
                {
                    final var identifier = node.identifierAsLong();
                    final var latitude = Latitude.degrees(node.latitude());
                    final var longitude = Longitude.degrees(node.longitude());
                    final var location = new Location(latitude, longitude);
                    locationForIdentifier.put(identifier, location);
                    totalLocations.increment();
                    return ACCEPTED;
                }

                @Override
                public Action onRelation(final PbfRelation relation)
                {
                    final var tags = relation.tagMap();
                    if ("multipolygon".equals(tags.get("type")))
                    {
                        final List<Border<T>> unnamed = new ArrayList<>();
                        final List<Border<T>> inners = new ArrayList<>();
                        final List<Border<T>> outers = new ArrayList<>();
                        for (final var member : relation.members())
                        {
                            final var border = borderForWayIdentifier.get(member.getMemberId());
                            if (border != null)
                            {
                                final var object = border.region();
                                if (object.name() == null)
                                {
                                    unnamed.add(border);
                                }
                                if ("inner".equalsIgnoreCase(member.getMemberRole()))
                                {
                                    inners.add(border);
                                }
                                if ("outer".equalsIgnoreCase(member.getMemberRole()))
                                {
                                    outers.add(border);
                                }
                            }
                            else
                            {
                                trace("Multi-polygon relation $: member $ was not found", relation.identifierAsLong(), member.getMemberId());
                            }
                        }
                        for (final var outer : outers)
                        {
                            for (final var inner : inners)
                            {
                                outer.polygon().addHole(inner.polygon());
                                borders.remove(inner);
                            }
                        }
                        assignMultiPolygonIdentity(tags, unnamed);
                        for (final var border : unnamed)
                        {
                            trace("Named inner polygon $ ($ segments)", border.region(),
                                    border.polygon().segmentCount());
                        }
                    }
                    return ACCEPTED;
                }

                @SuppressWarnings("ConstantConditions")
                @Override
                public Action onWay(final PbfWay way)
                {
                    final var region = settings().regionExtractor().extract(way);
                    if (region != null && region.identity().isValid())
                    {
                        // Start profiling time to load
                        final var start = Time.now();

                        // Read polygon nodes
                        final var builder = new PolygonBuilder();
                        for (final var node : way.nodes())
                        {
                            final var location = locationForIdentifier.get(node.getNodeId());
                            if (location != null)
                            {
                                builder.add(location);
                            }
                        }

                        // If the border polygon is valid,
                        if (builder.isValid())
                        {
                            // build the polygon and set an identifier so it can be efficiently stored in a map
                            final var border = builder.build();
                            if (border.bounds().area().isGreaterThan(settings().minimumBorderArea()))
                            {
                                border.hashCode(identifier++);
                                border.fast(USE_FAST_POLYGON_SPATIAL_INDEX);
                                border.initialize();
                                CompressibleCollection.compressReachableObjects(outer, border, Method.FREEZE,
                                        event ->
                                        {
                                        });
                                total.increment();

                                // Get time elapsed to build polygon
                                final var elapsed = start.elapsedSince();

                                // Get the approximate size of the polygon
                                Bytes size = null;
                                if (SHOW_APPROXIMATE_SIZES)
                                {
                                    size = Objects.primitiveSize(border.outline());
                                    ensure(size != null);
                                    totalSize.plus(size.asBytes());
                                }

                                // Show information about the loaded border
                                if (DETAILED)
                                {
                                    trace("Loaded $ ($ segments) in $ $", region, border.segmentCount(), elapsed,
                                            (size != null ? "(" + size + ")" : ""));
                                }

                                // Add polygon to spatial index
                                final var newBorder = new Border<>(region, border);
                                borders.add(newBorder);
                                borderForWayIdentifier.put(way.identifierAsLong(), newBorder);
                            }
                        }
                    }
                    else
                    {
                        trace("Unable to extract a valid " + type().getSimpleName() + " from " + way);
                    }
                    return ACCEPTED;
                }
            });

            // Bulk load spatial index
            index().bulkLoad(borders);

            if (isDebugOn())
            {
                index().dump(System.out);
            }

            // Show what was loaded
            if (DETAILED)
            {
                trace("Loaded $ polygons with $ locations $ in $", total.asCount(), totalLocations.asCount(),
                        SHOW_APPROXIMATE_SIZES ? "(" + Bytes.bytes(totalSize.asLong()) + ") " : "", start.elapsedSince());
            }

            saveBordersToCache();

            // We succeeded
            return true;
        }
        catch (final Exception e)
        {
            warning(e, "Unable to load borders from pbf file '$'", resource);
            return false;
        }
    }

    private File localJar(final FileName name)
    {
        return cacheFolder().file(name);
    }

    private File pbfFile()
    {
        return pbfFile(settings().type());
    }

    private File pbfFile(final Class<?> type)
    {
        return cacheFolder().file(FileName.parse(baseName(type) + ".osm.pbf"));
    }

    private void saveBordersToCache()
    {
        trace("Optimizing border index");
        CompressibleCollection.compressReachableObjects(this, index(), Method.FREEZE, Compressible ->
        {
        });
        trace("Optimized border index");

        trace("Saving borders to $", borderCacheFile().fileName());

        // Save spatial index in cache file
        trace("Saving border spatial index to $", borderCacheFile().fileName());
        try (final var output = borderCacheFile().openForWriting())
        {
            final var session = serializationSession();
            session.open(RESOURCE, KivaKit.get().version(), output);
            session.write(new VersionedObject<>(MapRegionProject.get().borderDataVersion(), index()));
            session.close();
        }
        catch (final Exception e)
        {
            throw new Problem(e, "Unable to save border spatial index to ${debug}", borderCacheFile().fileName()).asException();
        }
        if (borderCacheFile().exists())
        {
            trace("Saved border spatial index to $", borderCacheFile().fileName());
        }
        else
        {
            problem("Unable to save border spatial index to $", borderCacheFile().fileName());
        }

        // Read the index back in to verify it
        trace("Verifying border spatial index $", borderCacheFile().fileName());
        try (final var in = borderCacheFile().openForReading())
        {
            final var serialization = serializationSession();
            serialization.open(RESOURCE, KivaKit.get().version(), in);
            final var index = serialization.read();
            ensureEqual(index.version(), MapRegionProject.get().borderDataVersion());
            ensureEqual(index.get(), index());
        }
        catch (final IOException e)
        {
            problem(e, "Unable to verify spatial index");
        }
        trace("Verified border spatial index $", borderCacheFile().fileName());

        // Give all users access to this data
        grantAccess();
    }

    @SuppressWarnings({ "unchecked" })
    private SerializationSession serializationSession()
    {
        // Get a serialization session for this thread,
        final var session = sessionFactory.session(debug().listener());

        // and if it's a kryo session,
        if (session instanceof KryoSerializationSession)
        {
            // get the kryo serializer as a BorderSpatialIndexKryoSerializer
            final var kryoSession = (KryoSerializationSession) session;
            final var serializer = (Configured<Settings<T>>) kryoSession.serializer(BorderSpatialIndex.class);

            // and update its settings for this particular border cache.
            serializer.configure(settings);
        }

        return session;
    }

    private Class<T> type()
    {
        return settings.type();
    }
}
