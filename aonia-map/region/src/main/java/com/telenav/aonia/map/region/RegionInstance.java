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

package com.telenav.aonia.map.region;

import com.telenav.aonia.map.geography.shape.polyline.Polygon;
import com.telenav.aonia.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.aonia.map.geography.shape.rectangle.Rectangle;
import com.telenav.aonia.map.region.locale.MapLocale;
import com.telenav.aonia.map.region.project.lexakai.diagrams.DiagramRegion;
import com.telenav.aonia.map.region.regions.Country;
import com.telenav.kivakit.core.collections.set.ConcurrentHashSet;
import com.telenav.kivakit.core.kernel.language.locales.LanguageIsoCode;
import com.telenav.kivakit.core.kernel.language.reflection.property.filters.KivaKitExcludeProperty;
import com.telenav.kivakit.core.kernel.language.strings.AsciiArt;
import com.telenav.kivakit.core.kernel.language.strings.conversion.AsString;
import com.telenav.kivakit.core.kernel.language.values.count.Count;
import com.telenav.kivakit.core.kernel.logging.Logger;
import com.telenav.kivakit.core.kernel.logging.LoggerFactory;
import com.telenav.kivakit.core.kernel.messaging.Debug;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.lexakai.annotations.associations.UmlAggregation;
import com.telenav.lexakai.annotations.visibility.UmlExcludeSuperTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.telenav.kivakit.core.kernel.data.validation.ensure.Ensure.ensure;

@UmlClassDiagram(diagram = DiagramRegion.class)
@UmlExcludeSuperTypes(AsString.class)
public class RegionInstance<T extends Region<T>> implements AsString
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    // Identity
    @UmlAggregation
    private RegionIdentity identity;

    private int ordinal;

    private final Class<T> subclass;

    private T region;

    // Specific to each region
    @UmlAggregation(label = "bounds")
    private Rectangle bounds;

    @KivaKitExcludeProperty
    @UmlAggregation(label = "children")
    private final RegionSet children = new RegionSet(new ConcurrentHashSet<>());

    @UmlAggregation
    private Country.DrivingSide drivingSide;

    @UmlAggregation
    private Country.AutomotiveSupportLevel automotiveSupportLevel;

    @UmlAggregation
    private List<LanguageIsoCode> languages = new ArrayList<>();

    @UmlAggregation
    private MapLocale locale;

    public RegionInstance(final Class<T> subclass)
    {
        this.subclass = subclass;
    }

    private RegionInstance(final RegionInstance<T> that)
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
    public RegionInstance<T> add(final Region<?> region)
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
    public String asString()
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
            final var borders = borders();
            if (borders != null && !borders.isEmpty())
            {
                final var builder = new BoundingBoxBuilder();
                for (final var polygon : borders)
                {
                    builder.add(polygon.locationSequence());
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
    public <C extends Region> Set<C> children(final Class<C> type)
    {
        final var matching = children().matching((child) -> child.subclass().isAssignableFrom(type));
        final Set<C> set = new TreeSet<>();
        for (final var region : matching)
        {
            set.add((C) region);
        }
        return set;
    }

    public LanguageIsoCode defaultLanguage()
    {
        return languages().isEmpty() ? LanguageIsoCode.ENGLISH : languages().get(0);
    }

    public final Country.DrivingSide drivingSide()
    {
        return drivingSide;
    }

    public RegionIdentity identity()
    {
        return identity;
    }

    public RegionInstance<T> identity(final RegionIdentity identity)
    {
        this.identity = identity;
        return this;
    }

    public boolean isValid()
    {
        return identity != null && identity().isValid();
    }

    public final List<LanguageIsoCode> languages()
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

    public RegionInstance<T> prefix(final Region<?> prefix)
    {
        assert prefix != null;
        identity = identity().withPrefix(prefix.identity());
        return this;
    }

    public RegionInstance<T> prefix(final String prefix)
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
    public RegionInstance<T> region(final Region region)
    {
        this.region = (T) region;
        return this;
    }

    public final Class<T> subclass()
    {
        return subclass;
    }

    public String toDebugString(final int level)
    {
        final var builder = new StringBuilder();
        builder.append(AsciiArt.repeat(level, ' '));
        builder.append(toString());
        builder.append("\n");
        for (final Region<?> child : children())
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

    public RegionInstance<T> withAutomotiveSupportLevel(final Country.AutomotiveSupportLevel automotiveSupportLevel)
    {
        final var copy = new RegionInstance<>(this);
        copy.automotiveSupportLevel = automotiveSupportLevel;
        return copy;
    }

    public RegionInstance<T> withBounds(final Rectangle bounds)
    {
        final var copy = new RegionInstance<>(this);
        copy.bounds = bounds;
        return copy;
    }

    public RegionInstance<T> withDrivingSide(final Country.DrivingSide drivingSide)
    {
        final var copy = new RegionInstance<>(this);
        copy.drivingSide = drivingSide;
        return copy;
    }

    public RegionInstance<T> withIdentity(final RegionIdentity identity)
    {
        final var copy = new RegionInstance<>(this);
        copy.identity = identity;
        return copy;
    }

    public RegionInstance<T> withLanguage(final LanguageIsoCode language)
    {
        final var copy = new RegionInstance<>(this);
        copy.languages.add(language);
        return copy;
    }

    public RegionInstance<T> withLanguages(final List<LanguageIsoCode> languages)
    {
        final var copy = new RegionInstance<>(this);
        copy.languages = languages;
        return copy;
    }

    public RegionInstance<T> withLocale(final MapLocale locale)
    {
        final var copy = new RegionInstance<>(this);
        copy.locale = locale;
        return copy;
    }

    public RegionInstance<T> withOrdinal(final int ordinal)
    {
        final var copy = new RegionInstance<>(this);
        copy.ordinal = ordinal;
        return copy;
    }
}
