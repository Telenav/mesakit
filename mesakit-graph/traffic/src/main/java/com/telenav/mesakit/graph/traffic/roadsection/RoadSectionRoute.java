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

import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.StringList;
import com.telenav.kivakit.kernel.language.iteration.Iterables;
import com.telenav.kivakit.kernel.language.iteration.Next;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitExcludeProperty;
import com.telenav.kivakit.kernel.language.reflection.property.filters.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.strings.formatting.ObjectFormatter;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.traffic.project.GraphTrafficLimits;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.LocationSequence;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Intersectable;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.fail;

/**
 * An arbitrary list of {@link RoadSectionIdentifier}s with a bounding rectangle. Note that the {@link RoadSection}s
 * identified in a {@link RoadSectionRoute} do not necessarily have to be linked.
 *
 * @author jonathanl (shibo)
 */
public class RoadSectionRoute implements Bounded, Intersectable, LocationSequence, RoadLocation
{
    public static class Converter extends BaseStringConverter<RoadSectionRoute>
    {
        public Converter(final Listener listener)
        {
            super(listener);
            allowNull(true);
        }

        @Override
        protected RoadSectionRoute onConvertToObject(final String value)
        {
            final var codes = new RoadSectionCode.ListConverter(this, "::").convert(value);
            if (codes != null)
            {
                final var route = new RoadSectionRoute();
                for (final var code : codes)
                {
                    route.add(code);
                }
                return route;
            }
            return null;
        }

        @Override
        protected String onConvertToString(final RoadSectionRoute route)
        {
            final var list = new StringList(GraphTrafficLimits.MAXIMUM_ROAD_SECTIONS_PER_ROUTE);
            final var converter = new RoadSectionCode.Converter(this);
            for (final var identifier : route.roadSectionIdentifiers())
            {
                list.add(converter.toString(identifier.asCode()));
            }
            return list.join("::");
        }
    }

    private final List<RoadSectionIdentifier> roadSectionIdentifiers = new ArrayList<>();

    private Rectangle bounds;

    /**
     * Construct.
     */
    public RoadSectionRoute()
    {
    }

    /**
     * Construct.
     *
     * @param identifiers Initial collection of {@link RoadSectionIdentifier}s.
     */
    public RoadSectionRoute(final Collection<RoadSectionIdentifier> identifiers)
    {
        roadSectionIdentifiers.addAll(identifiers);
    }

    /**
     * Construct.
     *
     * @param identifiers Initial collection of {@link RoadSectionIdentifier}s.
     * @param bounds The bounding rectangle for the given identifiers.
     */
    public RoadSectionRoute(final List<RoadSectionIdentifier> identifiers, final Rectangle bounds)
    {
        roadSectionIdentifiers.addAll(identifiers);
        this.bounds = bounds;
    }

    public RoadSectionRoute(final long[] longs)
    {
        if (longs != null)
        {
            if (longs.length % 2 != 0)
            {
                fail("PrimitiveArray must be an even length");
            }
            for (var i = 0; i < longs.length; i += 2)
            {
                final var flags = longs[i];
                final var value = longs[i + 1];
                add(RoadSectionIdentifier.forLongValues(flags, value, false));
            }
        }
    }

    /**
     * Construct.
     */
    public RoadSectionRoute(final Rectangle bounds)
    {
        this.bounds = bounds;
    }

    public RoadSectionRoute(final RoadSectionIdentifier identifier)
    {
        roadSectionIdentifiers.add(identifier);
    }

    /**
     * Adds a road section code to this route.
     *
     * @param code The code to add.
     */
    public void add(final RoadSectionCode code)
    {
        add(code.asIdentifier());
    }

    /**
     * Adds an identifier to this route.
     *
     * @param identifier The identifier to add.
     */
    public void add(final RoadSectionIdentifier identifier)
    {
        roadSectionIdentifiers.add(identifier);
    }

    /**
     * Adds all the road sections in a specified collection to this route
     *
     * @param codes the collection of road sections to add
     */
    public void addAll(final Collection<? extends RoadSectionCode> codes)
    {
        for (final RoadSectionCode code : codes)
        {
            add(code);
        }
    }

    public void addAllIdentifiers(final Collection<? extends RoadSectionIdentifier> identifiers)
    {
        for (final RoadSectionIdentifier identifier : identifiers)
        {
            add(identifier);
        }
    }

    public long[] asLongs()
    {
        final var longs = new long[roadSectionIdentifiers.size() * 2];
        var i = 0;
        for (final var identifier : roadSectionIdentifiers)
        {
            longs[i++] = identifier.flags();
            longs[i++] = identifier.value().asLong();
        }
        return longs;
    }

    /**
     * @return The minimum bounding rectangle of all road sections in this route
     */
    @Override
    @KivaKitIncludeProperty
    public Rectangle bounds()
    {
        if (bounds == null)
        {
            final var box = new BoundingBoxBuilder();
            for (final var identifier : roadSectionIdentifiers)
            {
                final var roadSection = identifier.roadSection();
                if (roadSection != null)
                {
                    box.add(roadSection.bounds());
                }
            }
            bounds = box.build();
        }
        return bounds;
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof RoadSectionRoute)
        {
            // NOTE: The order of road sections in a route *matters*. If a given data source were to
            // produce unordered routes, they would have to be sorted for this method to work.
            final var that = (RoadSectionRoute) object;
            return roadSectionIdentifiers.equals(that.roadSectionIdentifiers);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return roadSectionIdentifiers.hashCode();
    }

    /**
     * Clips this route to include all road section identifiers that intersect the given rectangle
     *
     * @param clipping The clipping rectangle
     * @return The route clipped to the given rectangle
     */
    public RoadSectionRoute intersecting(final Rectangle clipping)
    {
        final var route = new RoadSectionRoute(clipping);
        for (final var identifier : roadSectionIdentifiers)
        {
            final var roadSection = identifier.roadSection();
            if (roadSection != null)
            {
                if (roadSection.intersects(clipping))
                {
                    route.add(identifier);
                }
            }
        }
        return route;
    }

    @Override
    public boolean intersects(final Rectangle rectangle)
    {
        final var bounds = bounds();
        if (bounds != null)
        {
            if (bounds.intersects(rectangle))
            {
                for (final var location : locationSequence())
                {
                    if (rectangle.contains(location))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @return Total length of this route
     */
    @KivaKitIncludeProperty
    public Distance length()
    {
        var length = Distance.ZERO;
        for (final var roadSection : roadSections())
        {
            length = length.add(roadSection.length());
        }
        return length;
    }

    @Override
    public Iterable<Location> locationSequence()
    {
        return Iterables.iterable(() -> new Next<>()
        {
            final Iterator<RoadSection> sections = roadSections().iterator();

            RoadSection current;

            boolean startReturned;

            @Override
            public Location onNext()
            {
                if (current == null && sections.hasNext())
                {
                    current = sections.next();
                    startReturned = false;
                }
                if (current != null)
                {
                    if (startReturned)
                    {
                        final var end = current.end();
                        current = null;
                        return end;
                    }
                    else
                    {
                        startReturned = true;
                        return current.start();
                    }
                }
                return null;
            }
        });
    }

    /**
     * Finds the first road section past the middle of this route
     *
     * @return middle road section
     */
    public RoadSection midpoint()
    {
        // Compute half the length of this route
        final var halfLength = length().times(0.5);

        // Loop through road sections
        var length = Distance.millimeters(0);
        for (final var section : roadSections())
        {
            // and if the length puts us past halfway
            length = length.add(section.length());
            if (length.isGreaterThanOrEqualTo(halfLength))
            {
                // return that section as the midpoint.
                return section;
            }
        }
        return null;
    }

    public RoadSectionIdentifier primaryRoadSectionIdentifier()
    {
        if (!roadSectionIdentifiers.isEmpty())
        {
            return roadSectionIdentifiers.get(0);
        }
        return null;
    }

    @Override
    public Iterable<RoadSectionIdentifier> roadLocation()
    {
        return roadSectionIdentifiers;
    }

    /**
     * @return The road section identifiers in this route
     */
    public List<RoadSectionIdentifier> roadSectionIdentifiers()
    {
        return roadSectionIdentifiers;
    }

    public List<RoadSectionIdentifier> roadSectionIdentifiers(final List<RoadSectionCodingSystem> systems)
    {
        final List<RoadSectionIdentifier> identifiers = new ArrayList<>();
        for (final var identifier : roadSectionIdentifiers)
        {
            if (systems.contains(identifier.codingSystem()))
            {
                identifiers.add(identifier);
            }
        }
        return identifiers;
    }

    /**
     * @param system The road section coding system to get identifiers for
     * @return The road section identifiers in this route in the given system
     */
    public List<RoadSectionIdentifier> roadSectionIdentifiers(final RoadSectionCodingSystem system)
    {
        final List<RoadSectionIdentifier> identifiers = new ArrayList<>();
        for (final var identifier : roadSectionIdentifiers)
        {
            if (identifier.codingSystem().equals(system))
            {
                identifiers.add(identifier);
            }
        }
        return identifiers;
    }

    /**
     * @return Iterates through non-null road sections
     */
    @KivaKitExcludeProperty
    public Iterable<RoadSection> roadSections()
    {
        final var outer = this;
        return Iterables.iterable(() -> new Next<>()
        {
            private int index;

            @Override
            public RoadSection onNext()
            {
                while (index < outer.roadSectionIdentifiers.size())
                {
                    final var identifier = outer.roadSectionIdentifiers.get(index++);
                    final var section = identifier.roadSection();
                    if (section != null)
                    {
                        return section;
                    }
                }
                return null;
            }
        });
    }

    /**
     * @return The number of road sections in this route
     */
    @KivaKitIncludeProperty
    public int size()
    {
        return roadSectionIdentifiers.size();
    }

    @Override
    public String toString()
    {
        return new ObjectFormatter(this).toString();
    }
}
