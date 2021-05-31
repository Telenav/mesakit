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

package com.telenav.kivakit.graph.map;

import com.telenav.kivakit.kernel.language.iteration.Streams;
import com.telenav.kivakit.kernel.scalars.counts.Count;
import com.telenav.kivakit.data.formats.library.map.identifiers.*;
import com.telenav.kivakit.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.kivakit.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.kivakit.graph.*;
import com.telenav.kivakit.graph.navigation.Navigator;
import com.telenav.kivakit.map.geography.polyline.Polyline;
import com.telenav.kivakit.map.geography.rectangle.Rectangle;
import com.telenav.kivakit.map.measurements.Distance;
import com.telenav.kivakit.map.road.model.*;
import com.telenav.kivakit.map.road.model.RoadName.Type;

import java.util.*;
import java.util.stream.Stream;

import static com.telenav.kivakit.kernel.validation.Validate.*;

/**
 * This class represents a way, but not necessarily a {@link MapWay}. It might be a shorter or longer stretch of a road
 * involving one or more PBF ways.
 *
 * @author songg
 * @author jonathanl (shibo)
 */
public class Way implements Road, Iterable<Edge>
{
    /**
     * @return The contiguous, non-branching way connected to the given edge.
     */
    public static Way contiguousWayForEdge(final Edge edge)
    {
        if (edge.isOneWay())
        {
            return new Way(edge.route(Navigator.NON_BRANCHING, Distance.MAXIMUM));
        }
        else
        {
            return new Way(edge.forward().route(Navigator.NON_BRANCHING_NO_UTURN, Distance.MAXIMUM));
        }
    }

    public static Way forRoute(final Route route)
    {
        return new Way(route);
    }

    private final Route route;

    private WayIdentifier identifier;

    private Polyline shape;

    protected Way(final Route route)
    {
        ensureNotNull(route);
        ensure(route.size() > 0, "Zero length route");
        this.route = route;
    }

    /** Return Route of all edges */
    @Override
    public Route asRoute()
    {
        return route;
    }

    public Rectangle bounds()
    {
        return route.bounds();
    }

    public List<Edge> edges()
    {
        return asRoute() == null ? new ArrayList<>() : asRoute().asList();
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Way)
        {
            final var that = (Way) object;
            return wayIdentifier().equals(that.wayIdentifier());
        }
        return false;
    }

    public Edge first()
    {
        return route.first();
    }

    @Override
    public Vertex from()
    {
        return first().from();
    }

    @Override
    public int hashCode()
    {
        return wayIdentifier().hashCode();
    }

    public HighwayType highwayType()
    {
        return first().uniDbHighwayType();
    }

    public boolean isReversed()
    {
        return route.first().isReverse();
    }

    @Override
    public boolean isValid()
    {
        final var route = asRoute();
        return route != null && route.size() > 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Edge> iterator()
    {
        return asRoute().iterator();
    }

    @Override
    public Count laneCount()
    {
        return first().laneCount();
    }

    public Edge last()
    {
        return route.last();
    }

    @Override
    public Distance length()
    {
        return route.length();
    }

    public List<NodeIdentifier> pbfNodeIdentifiers()
    {
        final List<NodeIdentifier> identifiers = new ArrayList<>();
        for (final var point : route.graph().shapePoints(roadShape()))
        {
            identifiers.add(point.mapIdentifier());
        }
        return identifiers;
    }

    public PbfTagList pbfTags()
    {
        return first().tagList();
    }

    @Override
    public Way reversed()
    {
        final var reversed = route.reversed();
        if (reversed != null)
        {
            return new Way(reversed);
        }
        return null;
    }

    @Override
    public RoadFunctionalClass roadFunctionalClass()
    {
        return first().roadFunctionalClass();
    }

    @Override
    public RoadName roadName()
    {
        return first().roadName();
    }

    @Override
    public RoadName roadName(final Type type)
    {
        return first().roadName(type);
    }

    @Override
    public Polyline roadShape()
    {
        if (shape == null)
        {
            // It's fairly expensive to create this, so we cache it
            shape = route.polyline();
        }
        return shape;
    }

    @Override
    public RoadState roadState()
    {
        return first().roadState();
    }

    @Override
    public RoadSubType roadSubType()
    {
        return first().roadSubType();
    }

    @Override
    public RoadType roadType()
    {
        return first().roadType();
    }

    public int size()
    {
        return route == null ? 0 : route.size();
    }

    public Stream<Edge> stream()
    {
        return Streams.stream(this);
    }

    @Override
    public Vertex to()
    {
        return last().to();
    }

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        for (final var edge : route)
        {
            builder.append(edge.identifierAsLong()).append(",");
        }
        return builder.toString();
    }

    @Override
    public WayIdentifier wayIdentifier()
    {
        if (identifier == null)
        {
            identifier = new PbfWayIdentifier(route.hashCode());
        }
        return identifier;
    }
}
