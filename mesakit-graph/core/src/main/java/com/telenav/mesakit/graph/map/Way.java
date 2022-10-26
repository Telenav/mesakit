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

package com.telenav.mesakit.graph.map;

import com.telenav.kivakit.core.language.Streams;
import com.telenav.kivakit.core.value.count.Count;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.Road;
import com.telenav.mesakit.graph.Route;
import com.telenav.mesakit.graph.Vertex;
import com.telenav.mesakit.graph.navigation.Navigator;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapNodeIdentifier;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.tags.PbfTagList;
import com.telenav.mesakit.map.geography.shape.polyline.Polyline;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import com.telenav.mesakit.map.road.model.HighwayType;
import com.telenav.mesakit.map.road.model.RoadFunctionalClass;
import com.telenav.mesakit.map.road.model.RoadName;
import com.telenav.mesakit.map.road.model.RoadName.Type;
import com.telenav.mesakit.map.road.model.RoadState;
import com.telenav.mesakit.map.road.model.RoadSubType;
import com.telenav.mesakit.map.road.model.RoadType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.telenav.kivakit.core.ensure.Ensure.ensure;
import static com.telenav.kivakit.core.ensure.Ensure.ensureNotNull;

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
     * Returns the contiguous, non-branching way connected to the given edge.
     */
    public static Way contiguousWayForEdge(Edge edge)
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

    public static Way forRoute(Route route)
    {
        return new Way(route);
    }

    private final Route route;

    private PbfWayIdentifier identifier;

    private Polyline shape;

    protected Way(Route route)
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
    public boolean equals(Object object)
    {
        if (object instanceof Way that)
        {
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
        return first().mapWay().highwayType();
    }

    public boolean isReversed()
    {
        return route.first().isReverse();
    }

    @Override
    public boolean isValid()
    {
        var route = asRoute();
        return route != null && route.size() > 0;
    }

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

    public List<MapNodeIdentifier> mapNodeIdentifiers()
    {
        List<MapNodeIdentifier> identifiers = new ArrayList<>();
        for (var point : route.graph().shapePoints(roadShape()))
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
        var reversed = route.reversed();
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
    public RoadName roadName(Type type)
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
        var builder = new StringBuilder();
        for (var edge : route)
        {
            builder.append(edge.identifierAsLong()).append(",");
        }
        return builder.toString();
    }

    @Override
    public MapWayIdentifier wayIdentifier()
    {
        if (identifier == null)
        {
            identifier = new PbfWayIdentifier(route.hashCode());
        }
        return identifier;
    }
}
