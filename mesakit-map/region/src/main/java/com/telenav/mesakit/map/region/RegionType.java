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

import com.telenav.kivakit.core.collections.Sets;
import com.telenav.kivakit.core.collections.map.CaseFoldingStringMap;
import com.telenav.kivakit.core.logging.Logger;
import com.telenav.kivakit.core.logging.LoggerFactory;
import com.telenav.kivakit.core.messaging.Debug;
import com.telenav.kivakit.core.value.count.Maximum;
import com.telenav.kivakit.interfaces.comparison.Matcher;
import com.telenav.lexakai.annotations.UmlClassDiagram;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.region.border.cache.BorderCache;
import com.telenav.mesakit.map.region.internal.lexakai.DiagramRegion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;

@UmlClassDiagram(diagram = DiagramRegion.class)
public class RegionType<T extends Region<T>>
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    public interface RegionFactory<R extends Region<R>>
    {
        R newInstance(RegionIdentity identity);
    }

    // Name of region type
    private String name;

    // The subclass of region
    private final Class<?> subclass;

    // All regions of this type in sorted order
    private Set<T> all = new TreeSet<>();

    // Cache of borders for this region type
    private BorderCache<T> borderCache;

    // Next region identifier for this type of region
    private RegionIdentifier nextIdentifier;

    // Minimum and maximum identifiers for regions of this class
    private RegionIdentifier minimumIdentifier;

    private RegionIdentifier maximumIdentifier;

    // Maps from different codes to region instances
    private final Map<String, T> forIsoCode;

    private final Map<String, T> forMesaKitCode;

    private final Map<Integer, T> forNumericCountryCode;

    private final Map<RegionIdentifier, T> forRegionIdentifier;

    public RegionType(Class<T> subclass)
    {
        this.subclass = subclass;
        forIsoCode = new CaseFoldingStringMap<>(Maximum.MAXIMUM);
        forMesaKitCode = new CaseFoldingStringMap<>(Maximum.MAXIMUM);
        forNumericCountryCode = new HashMap<>();
        forRegionIdentifier = new HashMap<>();
    }

    private RegionType(RegionType<T> that)
    {
        name = that.name;
        all = Sets.copy(TreeSet::new, that.all);
        subclass = that.subclass;
        borderCache = that.borderCache;
        nextIdentifier = that.nextIdentifier;
        minimumIdentifier = that.minimumIdentifier;
        maximumIdentifier = that.maximumIdentifier;
        forIsoCode = that.forIsoCode;
        forMesaKitCode = that.forMesaKitCode;
        forNumericCountryCode = that.forNumericCountryCode;
        forRegionIdentifier = that.forRegionIdentifier;
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "UnusedReturnValue" })
    public RegionType<T> add(Region uncheckedRegion)
    {
        var region = (T) uncheckedRegion;

        // Assign the next identifier to the region
        if (nextIdentifier.asInt() < size() && forRegionIdentifier.get(nextIdentifier) != null)
        {
            throw new IllegalArgumentException("Identifier " + nextIdentifier + " has already been used by "
                    + forRegionIdentifier.get(nextIdentifier));
        }
        if (nextIdentifier.isLessThan(maximumIdentifier))
        {
            region.instance().identity().identifier(nextIdentifier);
            nextIdentifier = nextIdentifier.next();
        }
        else
        {
            throw new IllegalArgumentException(subclass.getSimpleName() + " identifier must be between "
                    + minimumIdentifier() + " and " + maximumIdentifier());
        }

        if (all.add(region))
        {
            if (!"Region".equals(name()))
            {
                DEBUG.trace("Created $ $", name(), region.identity());
            }
        }

        var identity = region.identity();

        forIsoCode.putIfAbsent(identity.iso().code(), region);
        forMesaKitCode.putIfAbsent(identity.mesakit().code(), region);
        forRegionIdentifier.putIfAbsent(region.identifier(), region);

        var countryIsoCode = identity.countryIsoCode();
        if (countryIsoCode != null)
        {
            forIsoCode.putIfAbsent(countryIsoCode.alpha2Code(), region);
            forIsoCode.putIfAbsent(countryIsoCode.alpha3Code(), region);
            forNumericCountryCode.putIfAbsent(countryIsoCode.numericCountryCode(), region);
        }

        return this;
    }

    public Collection<T> all()
    {
        return all;
    }

    @SuppressWarnings({ "rawtypes" })
    public Collection<Region> allUntyped()
    {
        // This crazy hack is necessary because eclipse will allow the cast of all() to
        // Collection<Region>, but the javac compiler doesn't allow it.
        return new ArrayList<>(all());
    }

    public boolean contains(T region)
    {
        return all().contains(region);
    }

    public T forIdentifier(RegionIdentifier identifier)
    {
        return forRegionIdentifier.get(identifier);
    }

    public T forIdentity(RegionIdentity identity)
    {
        if (identity.hasIsoCode())
        {
            var region = forIsoCode.get(identity.iso().code());
            if (region != null)
            {
                return region;
            }
        }
        if (identity.hasMesaKitCode())
        {
            return forMesaKitCode.get(identity.mesakit().code());
        }
        ensure(false);
        return null;
    }

    public T forLocation(Location location)
    {
        return borderCache().object(location);
    }

    public T forNumericCountryCode(int code)
    {
        return forNumericCountryCode.get(code);
    }

    public T forRegionCode(RegionCode code)
    {
        var region = forIsoCode.get(code.code());
        if (region != null)
        {
            return region;
        }
        return forMesaKitCode.get(code.code());
    }

    @SuppressWarnings("UnusedReturnValue")
    public RegionType<T> loadBorders()
    {
        if (borderCache() != null)
        {
            borderCache().loadBorders();
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public RegionType<T> loadIdentities()
    {
        if (borderCache() != null)
        {
            borderCache().loadIdentities();
        }
        return this;
    }

    public Collection<T> matching(Matcher<T> matcher)
    {
        return all().stream().filter(matcher::matches).collect(Collectors.toList());
    }

    public RegionIdentifier maximumIdentifier()
    {
        return maximumIdentifier;
    }

    public RegionIdentifier minimumIdentifier()
    {
        return minimumIdentifier;
    }

    public String name()
    {
        return name;
    }

    public int size()
    {
        return all().size();
    }

    public RegionType<T> withBorderCache(BorderCache<T> borderCache)
    {
        var copy = new RegionType<>(this);
        copy.borderCache = borderCache;
        return copy;
    }

    public RegionType<T> withMaximumIdentifier(RegionIdentifier maximumIdentifier)
    {
        var copy = new RegionType<>(this);
        copy.maximumIdentifier = maximumIdentifier;
        return copy;
    }

    public RegionType<T> withMinimumIdentifier(RegionIdentifier minimumIdentifier)
    {
        // Set next identifier to the minimum
        nextIdentifier = new RegionIdentifier(minimumIdentifier.asInt());

        var copy = new RegionType<>(this);
        copy.minimumIdentifier = minimumIdentifier;
        return copy;
    }

    public RegionType<T> withName(String name)
    {
        var copy = new RegionType<>(this);
        copy.name = name;
        return copy;
    }

    BorderCache<T> borderCache()
    {
        if (borderCache == null)
        {
            LOGGER.warning("Region '" + name() + "' has no border cache");
            return null;
        }
        else
        {
            return borderCache;
        }
    }
}
