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

import com.telenav.kivakit.core.collections.list.ObjectList;
import com.telenav.kivakit.core.collections.set.ConcurrentHashSet;
import com.telenav.kivakit.core.language.reflection.property.KivaKitExcludeProperty;
import com.telenav.kivakit.core.locale.LocaleLanguage;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.string.AsciiArt;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.kivakit.interfaces.string.StringFormattable;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;
import com.telenav.mesakit.map.geography.shape.polyline.Polygon;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegion;
import com.telenav.mesakit.map.region.locale.MapLocale;
import com.telenav.mesakit.map.region.regions.Country;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static com.telenav.kivakit.core.collections.list.ObjectList.objectList;
import static com.telenav.kivakit.core.ensure.Ensure.ensure;

@SuppressWarnings("unused")
@UmlClassDiagram(diagram = DiagramRegion.class)
@UmlExcludeSuperTypes(StringFormattable.class)
public class RegionInstance<T extends Region<T>> implements StringFormattable
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    @UmlAggregation
    private Country.AutomotiveSupportLevel automotiveSupportLevel;

    // Specific to each region
    @UmlAggregation(label = "bounds")
    private Rectangle bounds;

    @KivaKitExcludeProperty
    @UmlAggregation(label = "children")
    private final RegionSet children = new RegionSet(new ConcurrentHashSet<>());

    @UmlAggregation
    private Country.DrivingSide drivingSide;

    // Identity
    @UmlAggregation
    private RegionIdentity identity;

    @UmlAggregation
    private ObjectList<LocaleLanguage> languages = objectList();

    @UmlAggregation
    private MapLocale locale;

    private int ordinal;

    private T region;

    private final Class<T> subclass;

    public RegionInstance(Class<T> subclass)
    {
        this.subclass = subclass;
    }

    private RegionInstance(RegionInstance<T> that)
    {
        identity = that.identity;
        ordinal = that.ordinal;
        subclass = that.subclass;
        region = that.region;
        bounds = that.bounds;
        drivingSide = that.drivingSide;
        automotiveSupportLevel = that.automotiveSupportLevel;
        languages = that.languages;
        locale = that.locale;
    }

    @SuppressWarnings("UnusedReturnValue")
    public RegionInstance<T> add(Region<?> region)
    {
        if (!children.contains(region))
        {
            children.add(region);
            if (DEBUG.isDebugOn())
            {
                DEBUG.trace("$ + $ => \n$", toString(), region, toDebugString(2));
            }
        }
        return this;
    }

    @Override
    public String asString(Format format)
    {
        return toDebugString(0);
    }

    public final Country.AutomotiveSupportLevel automotiveSupportLevel()
    {
        return automotiveSupportLevel;
    }

    public Collection<Polygon> borders()
    {
        return Region.type(subclass()).borderCache().borders(region());
    }

    public Rectangle bounds()
    {
        if (bounds == null)
        {
            var borders = borders();
            if (borders != null && !borders.isEmpty())
            {
                var builder = new BoundingBoxBuilder();
                for (var polygon : borders)
                {
                    builder.add(polygon);
                }
                bounds = builder.build();
            }
            else
            {
                LOGGER.warning("$ region $ has no border information", subclass().getSimpleName(), identity().name());
            }
        }
        return bounds;
    }

    public final RegionSet children()
    {
        return children;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <C extends Region> Set<C> children(Class<C> type)
    {
        var matching = children().matching((child) -> child.subclass().isAssignableFrom(type));
        Set<C> set = new TreeSet<>();
        for (var region : matching)
        {
            set.add((C) region);
        }
        return set;
    }

    public LocaleLanguage defaultLanguage()
    {
        return languages().isEmpty() ? LocaleLanguage.ENGLISH : languages().get(0);
    }

    public final Country.DrivingSide drivingSide()
    {
        return drivingSide;
    }

    public RegionIdentity identity()
    {
        return identity;
    }

    public RegionInstance<T> identity(RegionIdentity identity)
    {
        this.identity = identity;
        return this;
    }

    public boolean isValid()
    {
        return identity != null && identity().isValid();
    }

    public final ObjectList<LocaleLanguage> languages()
    {
        return languages;
    }

    public final MapLocale locale()
    {
        return locale;
    }

    public final String name()
    {
        return identity().name();
    }

    public int ordinal()
    {
        return ordinal;
    }

    public RegionInstance<T> prefix(Region<?> prefix)
    {
        assert prefix != null;
        identity = identity().withPrefix(prefix.identity());
        return this;
    }

    public RegionInstance<T> prefix(String prefix)
    {
        ensure(prefix != null);
        identity = identity().withPrefix(prefix);
        return this;
    }

    public T region()
    {
        return region;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "UnusedReturnValue" })
    public RegionInstance<T> region(Region region)
    {
        this.region = (T) region;
        return this;
    }

    public final Class<T> subclass()
    {
        return subclass;
    }

    public String toDebugString(int level)
    {
        var builder = new StringBuilder();
        builder.append(AsciiArt.repeat(level, ' '));
        builder.append(this);
        builder.append("\n");
        for (Region<?> child : children())
        {
            builder.append(child.instance().toDebugString(level + 2));
        }
        return builder.toString();
    }

    @Override
    public String toString()
    {
        return identity() + " (" + Count.count(children()) + " children)";
    }

    public RegionInstance<T> withAutomotiveSupportLevel(Country.AutomotiveSupportLevel automotiveSupportLevel)
    {
        var copy = new RegionInstance<>(this);
        copy.automotiveSupportLevel = automotiveSupportLevel;
        return copy;
    }

    public RegionInstance<T> withBounds(Rectangle bounds)
    {
        var copy = new RegionInstance<>(this);
        copy.bounds = bounds;
        return copy;
    }

    public RegionInstance<T> withDrivingSide(Country.DrivingSide drivingSide)
    {
        var copy = new RegionInstance<>(this);
        copy.drivingSide = drivingSide;
        return copy;
    }

    public RegionInstance<T> withIdentity(RegionIdentity identity)
    {
        var copy = new RegionInstance<>(this);
        copy.identity = identity;
        return copy;
    }

    public RegionInstance<T> withLanguage(LocaleLanguage language)
    {
        var copy = new RegionInstance<>(this);
        copy.languages.add(language);
        return copy;
    }

    public RegionInstance<T> withLanguages(Collection<LocaleLanguage> languages)
    {
        var copy = new RegionInstance<>(this);
        copy.languages = objectList(languages);
        return copy;
    }

    public RegionInstance<T> withLocale(MapLocale locale)
    {
        var copy = new RegionInstance<>(this);
        copy.locale = locale;
        return copy;
    }

    public RegionInstance<T> withOrdinal(int ordinal)
    {
        var copy = new RegionInstance<>(this);
        copy.ordinal = ordinal;
        return copy;
    }
}
