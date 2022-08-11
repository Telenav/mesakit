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

package com.telenav.mesakit.map.region;

import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.conversion.BaseStringConverter;
import com.telenav.kivakit.core.language.Objects;
import com.telenav.kivakit.core.language.Patterns;
import com.telenav.kivakit.core.language.object.ObjectFormatter;
import com.telenav.kivakit.core.language.reflection.property.KivaKitExcludeProperty;
import com.telenav.kivakit.core.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.core.locale.LanguageIsoCode;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.messaging.Listener;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.string.Strings;
import com.telenav.kivakit.core.thread.KivaKitThread;
import com.telenav.kivakit.core.thread.Threads;
import com.telenav.kivakit.core.time.Duration;
import com.telenav.kivakit.core.value.count.Bytes;
import com.telenav.kivakit.filesystem.Folder;
import com.telenav.kivakit.interfaces.naming.Nameable;
import com.telenav.kivakit.interfaces.naming.Named;
import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.kivakit.resource.FileName;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.associations.UmlRelation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfEntity;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.indexing.rtree.RTreeSpatialIndex;
import com.telenav.mesakit.map.geography.shape.Outline;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.geography.shape.segment.Segment;
import com.telenav.mesakit.map.measurements.geographic.Area;
import com.telenav.mesakit.map.region.border.Bordered;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.countries.UnitedStates;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegion;
import com.telenav.mesakit.map.region.regions.City;
import com.telenav.mesakit.map.region.regions.Continent;
import com.telenav.mesakit.map.region.regions.Country;
import com.telenav.mesakit.map.region.regions.County;
import com.telenav.mesakit.map.region.regions.District;
import com.telenav.mesakit.map.region.regions.MetropolitanArea;
import com.telenav.mesakit.map.region.regions.State;
import com.telenav.mesakit.map.region.regions.World;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.fail;
import static com.telenav.kivakit.core.ensure.Ensure.unsupported;
import static com.telenav.kivakit.filesystem.Folder.parseFolder;

/**
 * @author Jonathan Locke
 */
@UmlClassDiagram(diagram = DiagramRegion.class)
@UmlExcludeSuperTypes({ Stringable.class, Comparable.class, Nameable.class, Named.class })
public abstract class Region<T extends Region<T>> implements Bounded, Bordered, Intersectable, Outline, Nameable, Named, Comparable<Region<T>>, Stringable
{
    public static final RegionIdentifier WORLD_IDENTIFIER_MINIMUM = new RegionIdentifier(1_000_000);

    public static final RegionIdentifier WORLD_IDENTIFIER_MAXIMUM = new RegionIdentifier(1_100_000);

    public static final RegionIdentifier WORLD_CELL_IDENTIFIER_MINIMUM = new RegionIdentifier(1_200_000);

    public static final RegionIdentifier WORLD_CELL_IDENTIFIER_MAXIMUM = new RegionIdentifier(1_300_000);

    public static final RegionIdentifier TIME_ZONE_IDENTIFIER_MINIMUM = new RegionIdentifier(900_000);

    public static final RegionIdentifier TIME_ZONE_IDENTIFIER_MAXIMUM = new RegionIdentifier(1_000_000);

    private static final Logger LOGGER = LoggerFactory.newLogger();

    public static SwitchParser.Builder<RegionSet> REGIONS = regionListSwitchParser(LOGGER, "regions",
            "A comma separated list of regions");

    @SuppressWarnings({ "rawtypes" })
    public static SwitchParser.Builder<Region> REGION = regionSwitchParser(LOGGER, "region",
            "A comma separated list of regions");

    private static final Debug DEBUG = new Debug(LOGGER);

    private static final RegionIdentifier COUNTRY_IDENTIFIER_MINIMUM = new RegionIdentifier(0);

    private static final RegionIdentifier COUNTRY_IDENTIFIER_MAXIMUM = new RegionIdentifier(1_000);

    private static final RegionIdentifier CONTINENT_IDENTIFIER_MINIMUM = new RegionIdentifier(1_000);

    private static final RegionIdentifier CONTINENT_IDENTIFIER_MAXIMUM = new RegionIdentifier(2_000);

    private static final RegionIdentifier STATE_IDENTIFIER_MINIMUM = new RegionIdentifier(100_000);

    private static final RegionIdentifier STATE_IDENTIFIER_MAXIMUM = new RegionIdentifier(200_000);

    private static final RegionIdentifier METROPOLITAN_AREA_IDENTIFIER_MINIMUM = new RegionIdentifier(200_000);

    private static final RegionIdentifier METROPOLITAN_AREA_IDENTIFIER_MAXIMUM = new RegionIdentifier(300_000);

    private static final RegionIdentifier COUNTY_IDENTIFIER_MINIMUM = new RegionIdentifier(400_000);

    private static final RegionIdentifier COUNTY_IDENTIFIER_MAXIMUM = new RegionIdentifier(500_000);

    private static final RegionIdentifier CITY_IDENTIFIER_MINIMUM = new RegionIdentifier(600_000);

    private static final RegionIdentifier CITY_IDENTIFIER_MAXIMUM = new RegionIdentifier(700_000);

    private static final RegionIdentifier DISTRICT_IDENTIFIER_MINIMUM = new RegionIdentifier(800_000);

    private static final RegionIdentifier DISTRICT_IDENTIFIER_MAXIMUM = new RegionIdentifier(900_000);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final RegionType all = new RegionType(Region.class)
            .withMinimumIdentifier(COUNTRY_IDENTIFIER_MINIMUM)
            .withMaximumIdentifier(WORLD_IDENTIFIER_MAXIMUM)
            .withName("Region");

    private static boolean registered;

    @SuppressWarnings("rawtypes")
    private static final Map<Class, RegionType> typeForClass = new HashMap<>();

    /** True when loading borders in the background */
    private static boolean loadingBorders;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Collection<Region> allRegions()
    {
        return all.all();
    }

    @SuppressWarnings("rawtypes")
    public static RegionSet allRegionsMatching(String simplifiedPattern)
    {
        var pattern = Patterns.simplified(simplifiedPattern);
        var matches = new RegionSet();
        for (var object : all.allUntyped())
        {
            var region = (Region) object;
            if (Patterns.matches(pattern, region.identity().mesakit().code()))
            {
                matches.add(region);
            }
        }
        return matches;
    }

    public static synchronized void bootstrap()
    {
        if (!registered)
        {
            registered = true;

            DEBUG.trace("Registering regions and loading region identities");

            register(World.class, new RegionType<>(World.class)
                    .withName("World")
                    .withMinimumIdentifier(WORLD_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(WORLD_IDENTIFIER_MAXIMUM));

            register(Continent.class, new RegionType<>(Continent.class)
                    .withName("Continent")
                    .withMinimumIdentifier(CONTINENT_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(CONTINENT_IDENTIFIER_MAXIMUM)
                    .withBorderCache(Continent.borderCache()));

            register(Country.class, new RegionType<>(Country.class)
                    .withName("Country")
                    .withMinimumIdentifier(COUNTRY_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(COUNTRY_IDENTIFIER_MAXIMUM)
                    .withBorderCache(Country.borderCache()));

            register(State.class, new RegionType<>(State.class)
                    .withName("State")
                    .withMinimumIdentifier(STATE_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(STATE_IDENTIFIER_MAXIMUM)
                    .withBorderCache(State.borderCache()));

            register(County.class, new RegionType<>(County.class)
                    .withName("County")
                    .withMinimumIdentifier(COUNTY_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(COUNTY_IDENTIFIER_MAXIMUM)
                    .withBorderCache(County.borderCache()));

            register(MetropolitanArea.class, new RegionType<>(MetropolitanArea.class)
                    .withName("Metropolitan Area")
                    .withMinimumIdentifier(METROPOLITAN_AREA_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(METROPOLITAN_AREA_IDENTIFIER_MAXIMUM)
                    .withBorderCache(MetropolitanArea.borderCache()));

            // No border cache (yet) because we don't know how to extract these since
            // there is no PBF data for this kind of region
            register(City.class, new RegionType<>(City.class)
                    .withName("City")
                    .withMinimumIdentifier(CITY_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(CITY_IDENTIFIER_MAXIMUM));

            // No border cache (yet) because we don't know how to extract these since
            // there is no PBF data for this kind of region
            register(District.class, new RegionType<>(District.class)
                    .withName("District")
                    .withMinimumIdentifier(DISTRICT_IDENTIFIER_MINIMUM)
                    .withMaximumIdentifier(DISTRICT_IDENTIFIER_MAXIMUM));

            // Initialize all the continents and their child regions
            Continent.create();

            // Load region identity data
            var executor = Executors.newFixedThreadPool(RTreeSpatialIndex.visualDebug() ? 1 : 1);
            executor.execute(() -> type(Continent.class).loadIdentities());
            executor.execute(() -> type(Country.class).loadIdentities());
            executor.execute(() -> type(State.class).loadIdentities());
            executor.execute(() -> type(MetropolitanArea.class).loadIdentities());
            executor.execute(() -> type(County.class).loadIdentities());
            Threads.shutdownAndAwait(executor);
        }
    }

    public static RegionCode code(PbfEntity<?> entity, String key)
    {
        var value = entity.tagValue(key);
        return value == null ? null : RegionCode.parse(value);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T extends Region<T>> Region<T> globalForRegionCode(RegionCode code)
    {
        return all.forRegionCode(code);
    }

    public static Region<?> globalForRegionIdentifier(RegionIdentifier identifier)
    {
        return all.forIdentifier(identifier);
    }

    protected static RegionCode isoCode(PbfEntity<?> entity)
    {
        var iso = entity.tagValue("iso-3166-2");
        if (iso == null)
        {
            iso = entity.tagValue("ISO3166-1");
        }
        if (iso == null && name(entity) != null)
        {
            iso = name(entity).isoized().code();
        }
        if (iso != null)
        {
            return RegionCode.parse(iso);
        }
        return null;
    }

    public static synchronized void loadBordersInBackground()
    {
        if (!loadingBorders)
        {
            loadingBorders = true;
            KivaKitThread.run(LOGGER, "BorderLoader", () ->
            {
                type(Country.class).loadBorders();
                type(State.class).loadBorders();
                type(County.class).loadBorders();
                type(MetropolitanArea.class).loadBorders();
            });
        }
    }

    public static RegionCode name(PbfEntity<?> entity)
    {
        return code(entity, "name");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static ArgumentParser.Builder<Region> regionArgumentParser(Listener listener, String description)
    {
        return ArgumentParser.builder(Region.class).converter(new Converter(listener)).description(description);
    }

    public static SwitchParser.Builder<RegionSet> regionListSwitchParser(Listener listener, String name,
                                                                         String description)
    {
        return SwitchParser.builder(RegionSet.class)
                .name(name)
                .description(description)
                .converter(new SetConverter(listener));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static SwitchParser.Builder<Region> regionSwitchParser(Listener listener, String name, String description)
    {
        return SwitchParser.builder(Region.class).name(name).description(description).converter(new Converter(listener));
    }

    @SuppressWarnings({ "rawtypes" })
    public static void register(Class regionClass, RegionType type)
    {
        typeForClass.putIfAbsent(regionClass, type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Region<T>> RegionType<T> type(Class<T> regionClass)
    {
        return typeForClass.get(regionClass);
    }

    public static class Converter<R extends Region<R>> extends BaseStringConverter<R>
    {
        public Converter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(R value)
        {
            return value.identity().mesakit().code();
        }

        @Override
        @SuppressWarnings("unchecked")
        protected R onToValue(String value)
        {
            if (!Strings.isEmpty(value) && !"NULL".equalsIgnoreCase(value))
            {
                var regions = allRegionsMatching(value);
                if (regions.size() == 1)
                {
                    return (R) regions.first();
                }
                LOGGER.warning("Region pattern was ambiguous, matching $ regions: $", regions.size(),
                        AsciiArt.bulleted(regions));
            }
            return null;
        }
    }

    public static class SetConverter extends BaseStringConverter<RegionSet>
    {
        public SetConverter(Listener listener)
        {
            super(listener);
        }

        @Override
        protected String onToString(RegionSet regions)
        {
            return unsupported();
        }

        @Override
        protected RegionSet onToValue(String value)
        {
            var regions = new RegionSet();
            for (var pattern : value.split(","))
            {
                var matches = allRegionsMatching(pattern);
                if (!matches.isEmpty())
                {
                    regions.addAll(matches);
                }
                else
                {
                    return fail("Pattern '$' didn't match any regions", pattern);
                }
            }
            return regions;
        }
    }

    protected static class RegionBorderCache<R extends Region<R>> extends BorderCache<R>
    {
        public RegionBorderCache(Settings<R> settings)
        {
            super(settings);
        }

        @Override
        protected final RegionIdentity identityForRegion(R region)
        {
            var identity = region.identity();
            if (identity.isValid())
            {
                return region.identity();
            }
            return null;
        }

        @Override
        protected final R regionForIdentity(RegionIdentity identity)
        {
            if (identity.isValid())
            {
                return settings().regionFactory().newInstance(identity);
            }
            return null;
        }
    }

    /**
     * Instance data for this region
     */
    @UmlAggregation
    private RegionInstance<T> instance;

    /**
     * An object that can be associated with every region, for convenience
     */
    private transient Object metadata;

    /**
     * Region that is the parent of this region, or null if there is no parent (for example for the World region)
     */
    @UmlAggregation(label = "parent")
    private Region<?> parent;

    protected Region()
    {
    }

    protected Region(Region<?> parent, RegionInstance<T> instance)
    {
        // Save instance data and parent
        this.instance = instance;
        this.parent = parent;

        // Connect this region instance to the instance data
        instance.region(this);

        // Add this region to the type information for all regions
        all.add(this);

        // Add this region to the subclass' type information
        type().add(this);

        // Add this region to the parent
        if (parent != null)
        {
            parent.instance().add(this);
        }

        // Ensure validity
        ensure(instance.isValid());
        ensure(parent == null || parent.isValid());
    }

    @Override
    public String asString(Format format)
    {
        return new ObjectFormatter(this).toString();
    }

    @Override
    public Collection<Polygon> borders()
    {
        return instance().borders();
    }

    @Override
    @KivaKitIncludeProperty
    public Rectangle bounds()
    {
        return instance().bounds();
    }

    @KivaKitIncludeProperty
    public Location center()
    {
        return bounds().center();
    }

    public final RegionSet children()
    {
        return instance().children();
    }

    public final <C extends Region<?>> Set<C> children(Class<C> type)
    {
        return instance().children(type);
    }

    @Override
    public final int compareTo(Region<T> that)
    {
        return name().compareTo(that.name());
    }

    @Override
    public Containment containment(Location location)
    {
        for (var polygon : borders())
        {
            var containment = polygon.containment(location);
            if (containment.isInside())
            {
                return containment;
            }
        }
        return Containment.OUTSIDE;
    }

    @Override
    public boolean contains(Location location)
    {
        for (var polygon : borders())
        {
            if (polygon.contains(location))
            {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Polyline line)
    {
        for (var polygon : borders())
        {
            if (polygon.contains(line))
            {
                return true;
            }
        }
        return false;
    }

    public boolean contains(Rectangle bounds)
    {
        for (var polygon : borders())
        {
            if (polygon.contains(bounds))
            {
                return true;
            }
        }
        return false;
    }

    @KivaKitIncludeProperty
    public Continent continent()
    {
        Region<?> at = this;
        while (!(at instanceof Continent))
        {
            at = at.parent();
        }
        return (Continent) at;
    }

    @KivaKitIncludeProperty
    public Country country()
    {
        Region<?> at = this;
        while (!(at instanceof Country))
        {
            at = at.parent();
        }
        return (Country) at;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof Region)
        {
            @SuppressWarnings("unchecked") var that = (Region<T>) object;
            return Objects.equal(identity(), that.identity());
        }
        return false;
    }

    public Duration estimatedGraphExtractionTime(Bytes size)
    {
        // The linear formula here was found by taking the graph extraction profiling data created
        // by running StateExtractionProfiler on all country files on the graph server and then
        // using Numbers to fit a line to that data.
        if (size != null)
        {
            return Duration.minutes((size.asMegabytes() * 0.0112) + 0.5524);
        }
        return Duration.ONE_SECOND;
    }

    public Duration estimatedPbfToGraphConversionTime(Bytes size)
    {
        // The constant 0.0062 was found by taking the graph converter profiling data created by
        // running GraphConversionProfiler on all PBF files on the graph server and then using
        // Numbers to fit a line to that data.
        if (size != null)
        {
            return Duration.minutes(size.asMegabytes() * 0.0062);
        }
        return Duration.ONE_SECOND;
    }

    /**
     * @return This region's name as a file name
     */
    @KivaKitIncludeProperty
    public FileName fileName()
    {
        return FileName.parseFileName(LOGGER, name());
    }

    /**
     * @return The folder path of this region
     */
    @KivaKitIncludeProperty
    public Folder folder()
    {
        if (parent == null)
        {
            return parseFolder(fileName().name());
        }
        else
        {
            return parent().folder().folder(fileName());
        }
    }

    @SuppressWarnings({ "HttpUrlsUsage", "SpellCheckingInspection" })
    @KivaKitIncludeProperty
    public URI geofabrikUri()
    {
        try
        {
            if (this instanceof UnitedStates)
            {
                return fail("Geofabrik does not support US downloads");
            }
            return new URI("http://download.geofabrik.de/" + geofabrikPath() + "-latest.osm.pbf");
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public int hashCode()
    {
        return identity().hashCode();
    }

    public final RegionIdentifier identifier()
    {
        return instance().identity().identifier();
    }

    @Override
    @KivaKitIncludeProperty
    public final RegionIdentity identity()
    {
        return instance().identity();
    }

    public final void initialize()
    {
        onInitialize();
    }

    public RegionInstance<T> instance()
    {
        return instance;
    }

    public Location intersection(Segment segment)
    {
        for (var polygon : borders())
        {
            if (polygon.intersects(segment))
            {
                return polygon.intersection(segment);
            }
        }
        return null;
    }

    @Override
    public boolean intersects(Rectangle rectangle)
    {
        for (var polygon : borders())
        {
            if (polygon.intersects(rectangle))
            {
                return true;
            }
        }
        return false;
    }

    public boolean intersectsOrContains(Polyline line)
    {
        for (var polygon : borders())
        {
            if (polygon.intersectsOrContains(line))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @return True if this region is an island having an isolated road network
     */
    public boolean isIsland()
    {
        return false;
    }

    public boolean isLargerThan(Region<T> that)
    {
        return largestArea().isGreaterThan(that.largestArea());
    }

    @KivaKitExcludeProperty
    public boolean isValid()
    {
        return instance.isValid();
    }

    public List<LanguageIsoCode> languages()
    {
        return instance().languages();
    }

    public Area largestArea()
    {
        var largestBorder = largestBorder();
        if (largestBorder != null)
        {
            return largestBorder.bounds().area();
        }
        return null;
    }

    public Polygon largestBorder()
    {
        Polygon largest = null;
        for (var polygon : borders())
        {
            if (largest == null || polygon.bounds().area().isGreaterThan(largest.bounds().area()))
            {
                largest = polygon;
            }
        }
        return largest;
    }

    @KivaKitIncludeProperty
    public MapLocale locale()
    {
        // Other cases
        if (!instance().languages().isEmpty())
        {
            return new MapLocale(this, instance().defaultLanguage());
        }

        // Default is World English
        return MapLocale.ENGLISH_WORLD.get();
    }

    public Object metadata()
    {
        return metadata;
    }

    public void metadata(Object metadata)
    {
        this.metadata = metadata;
    }

    @Override
    @KivaKitIncludeProperty
    public final String name()
    {
        return identity().name();
    }

    @Override
    public void name(String name)
    {
        instance().identity().name(name);
    }

    public final Collection<Region<?>> nestedChildren()
    {
        List<Region<?>> children = new ArrayList<>();
        for (Region<?> region : children())
        {
            children.add(region);
            children.addAll(region.nestedChildren());
        }
        return children;
    }

    public void onInitialize()
    {
    }

    public final Region<?> parent()
    {
        return parent;
    }

    public abstract Class<?> subclass();

    @Override
    public String toString()
    {
        return instance().toString();
    }

    @SuppressWarnings("unchecked")
    @UmlRelation(label = "has")
    public RegionType<T> type()
    {
        return typeForClass.get(subclass());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private String geofabrikPath()
    {
        if (this instanceof Continent)
        {
            return geofabrikize(continent().identity().mesakit().code());
        }
        if (this instanceof Country)
        {
            var name = name();
            if (name.equals(Country.UNITED_STATES.name()))
            {
                name = "us";
            }
            return parent().geofabrikPath() + "/" + geofabrikize(name);
        }
        if (this instanceof State)
        {
            var state = (State) this;
            return parent().geofabrikPath() + "/" + geofabrikize(state.name());
        }
        if (this instanceof County)
        {
            var county = (County) this;
            return parent().geofabrikPath() + "/" + geofabrikize(county.name());
        }
        if (this instanceof MetropolitanArea)
        {
            var metro = (MetropolitanArea) this;
            return parent().geofabrikPath() + "/" + geofabrikize(metro.name());
        }
        return fail("Can't form geofabrik path for " + identity().mesakit());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private String geofabrikize(String name)
    {
        return name.toLowerCase().replace("_", "-").replace(" ", "-");
    }
}
