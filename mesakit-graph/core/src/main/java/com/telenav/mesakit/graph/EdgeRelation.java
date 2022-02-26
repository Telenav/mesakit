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

package com.telenav.mesakit.graph;

import com.telenav.kivakit.interfaces.string.Stringable;
import com.telenav.kivakit.kernel.data.conversion.string.BaseStringConverter;
import com.telenav.kivakit.kernel.language.collections.list.ObjectList;
import com.telenav.kivakit.kernel.language.reflection.property.KivaKitIncludeProperty;
import com.telenav.kivakit.kernel.language.strings.Strings;
import com.telenav.kivakit.kernel.language.strings.conversion.AsIndentedString;
import com.telenav.kivakit.kernel.language.values.count.Maximum;
import com.telenav.kivakit.kernel.logging.Logger;
import com.telenav.kivakit.kernel.logging.LoggerFactory;
import com.telenav.kivakit.kernel.messaging.Debug;
import com.telenav.kivakit.kernel.messaging.Listener;
import com.telenav.mesakit.graph.collections.EdgeSet;
import com.telenav.mesakit.graph.collections.RouteList;
import com.telenav.mesakit.graph.identifiers.RelationIdentifier;
import com.telenav.mesakit.graph.metadata.DataSpecification;
import com.telenav.mesakit.graph.relations.restrictions.TurnRestriction;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementAttributes;
import com.telenav.mesakit.graph.specifications.common.relation.HeavyWeightRelation;
import com.telenav.mesakit.graph.specifications.common.relation.RelationAttributes;
import com.telenav.mesakit.graph.specifications.common.relation.store.RelationStore;
import com.telenav.mesakit.graph.specifications.library.properties.GraphElementPropertySet;
import com.telenav.mesakit.map.data.formats.library.map.identifiers.MapIdentifier;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfIdentifierType;
import com.telenav.mesakit.map.data.formats.pbf.model.identifiers.PbfWayIdentifier;
import com.telenav.mesakit.map.geography.Location;
import com.telenav.mesakit.map.geography.shape.rectangle.Bounded;
import com.telenav.mesakit.map.geography.shape.rectangle.BoundingBoxBuilder;
import com.telenav.mesakit.map.geography.shape.rectangle.Rectangle;
import com.telenav.mesakit.map.measurements.geographic.Distance;
import org.openstreetmap.osmosis.core.domain.v0_6.CommonEntityData;
import org.openstreetmap.osmosis.core.domain.v0_6.OsmUser;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.telenav.kivakit.kernel.data.validation.ensure.Ensure.unsupported;

public class EdgeRelation extends GraphElement implements Bounded
{
    private static final Logger LOGGER = LoggerFactory.newLogger();

    private static final Debug DEBUG = new Debug(LOGGER);

    private static final String[] noTurnRestrictions =
            {
                    "no_left_turn",
                    "no_right_turn",
                    "no_straight_on",
                    "no_u_turn"
            };

    private static final String[] onlyTurnRestrictions =
            {
                    "only_left_turn",
                    "only_right_turn",
                    "only_straight_on"
            };

    private static final String[] allTurnRestrictions =
            {
                    "no_left_turn",
                    "no_right_turn",
                    "no_straight_on",
                    "no_u_turn",
                    "only_left_turn",
                    "only_right_turn",
                    "only_straight_on"
            };

    public enum Type
    {
        // OSM
        ROUTE(0),
        TURN_RESTRICTION(1),
        BAD_TURN_RESTRICTION(2),
        OTHER(3),
        GRADE_SEPARATION(4),
        UNKNOWN(5),

        // UniDB
        UNIDB_ACCESS_RESTRICTION(7),
        UNIDB_ADAS_MAXSPEED(8),
        UNIDB_ADAS_NODE(9),
        UNIDB_BARRIER(10),
        UNIDB_BIFURCATION(11),
        UNIDB_BLACKSPOT(12),
        UNIDB_CONDITIONAL_ONEWAY(13),
        UNIDB_CONSTRUCTION(14),
        UNIDB_DIVIDED_JUNCTION(15),
        UNIDB_GENERIC_JUNCTION_VIEW(16),
        UNIDB_JUNCTION_VIEW(17),
        UNIDB_LANE_CONNECTIVITY(18),
        UNIDB_MISSING_TYPE(19),
        UNIDB_NATURAL_GUIDANCE(20),
        UNIDB_NO_THROUGH_RESTRICTION(21),
        UNIDB_ONE_WAY(22),
        UNIDB_RESTRICTION(23),
        UNIDB_SAFETY_CAMERA(24),
        UNIDB_SIGNPOST(25),
        UNIDB_THROUGH_RESTRICTION(26),
        UNIDB_TIMED_SPEED_LIMIT(27),
        UNIDB_TRAFFIC_SIGN(28),
        UNIDB_TRAFFIC_SIGNAL(29),
        UNIDB_TRAFFIC_SIGNALS(30),
        UNIDB_UNSUPPORTED(31);

        private static final Set<Type> RESTRICTIONS = new HashSet<>();

        static
        {
            for (var value : values())
            {
                if (value.name().contains("RESTRICTION"))
                {
                    RESTRICTIONS.add(value);
                }
            }
        }

        public static Type forIdentifier(int identifier)
        {
            switch (identifier)
            {
                case 0:
                    return ROUTE;
                case 1:
                    return TURN_RESTRICTION;
                case 2:
                    return BAD_TURN_RESTRICTION;
                case 3:
                    return OTHER;

                case 16:
                    return UNIDB_ACCESS_RESTRICTION;
                case 17:
                    return UNIDB_ADAS_MAXSPEED;
                case 18:
                    return UNIDB_ADAS_NODE;
                case 19:
                    return UNIDB_BARRIER;
                case 20:
                    return UNIDB_BIFURCATION;
                case 21:
                    return UNIDB_BLACKSPOT;
                case 22:
                    return UNIDB_CONDITIONAL_ONEWAY;
                case 23:
                    return UNIDB_CONSTRUCTION;
                case 24:
                    return UNIDB_DIVIDED_JUNCTION;
                case 25:
                    return UNIDB_GENERIC_JUNCTION_VIEW;
                case 26:
                    return UNIDB_JUNCTION_VIEW;
                case 27:
                    return UNIDB_LANE_CONNECTIVITY;
                case 28:
                    return UNIDB_MISSING_TYPE;
                case 29:
                    return UNIDB_NATURAL_GUIDANCE;
                case 30:
                    return UNIDB_NO_THROUGH_RESTRICTION;
                case 31:
                    return UNIDB_ONE_WAY;
                case 32:
                    return UNIDB_RESTRICTION;
                case 33:
                    return UNIDB_SAFETY_CAMERA;
                case 34:
                    return UNIDB_SIGNPOST;
                case 35:
                    return UNIDB_THROUGH_RESTRICTION;
                case 36:
                    return UNIDB_TIMED_SPEED_LIMIT;
                case 37:
                    return UNIDB_TRAFFIC_SIGN;
                case 38:
                    return UNIDB_TRAFFIC_SIGNAL;
                case 39:
                    return UNIDB_TRAFFIC_SIGNALS;
                case 63:
                    return GRADE_SEPARATION;
                case 40:
                default:
                    DEBUG.trace("The UniDb relation type ${integer} is not supported", identifier);
                    return UNIDB_UNSUPPORTED;
            }
        }

        public static Type forName(String type)
        {
            // Not using CheckType.valueOf because it might throw exceptions
            // if a new HERE relation type is added in the future. This
            // could be very inefficient.
            if (type == null)
            {
                return UNIDB_MISSING_TYPE;
            }
            switch (type)
            {
                case "restriction":
                    return UNIDB_RESTRICTION;
                case "barrier":
                    return UNIDB_BARRIER;
                case "traffic_signal":
                    return UNIDB_TRAFFIC_SIGNAL;
                case "traffic_sign":
                    return UNIDB_TRAFFIC_SIGN;
                case "construction":
                    return UNIDB_CONSTRUCTION;
                case "conditional_oneway":
                    return UNIDB_CONDITIONAL_ONEWAY;
                case "timed_speed_limit":
                    return UNIDB_TIMED_SPEED_LIMIT;
                case "divided_junction":
                    return UNIDB_DIVIDED_JUNCTION;
                case "adas_node":
                    return UNIDB_ADAS_NODE;
                case "signpost":
                    return UNIDB_SIGNPOST;
                case "traffic_signals":
                    return UNIDB_TRAFFIC_SIGNALS;
                case "gjv":
                    return UNIDB_GENERIC_JUNCTION_VIEW;
                case "lane_connectivity":
                    return UNIDB_LANE_CONNECTIVITY;
                case "adas:maxspeed":
                    return UNIDB_ADAS_MAXSPEED;
                case "natural_guidance":
                    return UNIDB_NATURAL_GUIDANCE;
                case "blackspot":
                    return UNIDB_BLACKSPOT;
                case "junction_view":
                    return UNIDB_JUNCTION_VIEW;
                case "bifurcation":
                    return UNIDB_BIFURCATION;
                case "safety_camera":
                    return UNIDB_SAFETY_CAMERA;
                case "oneway":
                    return UNIDB_ONE_WAY;
                case "grade_separation":
                    return GRADE_SEPARATION;
                default:
                    DEBUG.trace("The UniDb relation type '$' is not supported", type);
                    return UNIDB_UNSUPPORTED;
            }
        }

        private final int identifier;

        Type(int identifier)
        {
            this.identifier = identifier;
        }

        public int identifier()
        {
            return identifier;
        }
    }

    public static class Converter extends BaseStringConverter<EdgeRelation>
    {
        private final Graph graph;

        public Converter(Graph graph, Listener listener)
        {
            super(listener);
            this.graph = graph;
        }

        @Override
        protected String onToString(EdgeRelation relation)
        {
            return Long.toString(relation.identifierAsLong());
        }

        @Override
        protected EdgeRelation onToValue(String value)
        {
            var identifier = new RelationIdentifier(Long.parseLong(value));
            if (graph.contains(identifier))
            {
                return graph.relationForIdentifier(identifier);
            }
            return null;
        }
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public EdgeRelation(Graph graph, long identifier, int index)
    {
        this(graph, identifier);
        index(index);
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public EdgeRelation(Graph graph, RelationIdentifier identifier)
    {
        this(graph, identifier.asLong());
    }

    /**
     * It is not permissible to directly construct {@link GraphElement} objects. Elements may only be constructed by a
     * {@link DataSpecification}, which ensures proper initialization and specialization of elements.
     */
    public EdgeRelation(Graph graph, long identifier)
    {
        graph(graph);
        identifier(identifier);
    }

    protected EdgeRelation(EdgeRelation that)
    {
        this(that.graph(), that.identifierAsLong());
    }

    /**
     * @return This edge as a heavyweight edge (if it's not already one)
     */
    @Override
    public HeavyWeightRelation asHeavyWeight()
    {
        if (this instanceof HeavyWeightRelation)
        {
            return (HeavyWeightRelation) this;
        }
        var copy = graph().newHeavyWeightRelation(identifier());
        copy.copy(this);
        return copy;
    }

    public PbfRelation asPbfRelation()
    {
        List<RelationMember> members = new ArrayList<>();
        for (var member : members())
        {
            var identifier = member.identifier();
            members.add(new RelationMember(identifier.asLong(), ((PbfIdentifierType) identifier).entityType(), member.role()));
        }
        return new PbfRelation(new Relation(commonEntityData(), members));
    }

    public Route asRoute()
    {
        return asRoutes().asRoute();
    }

    public RouteList asRoutes()
    {
        var routes = new RouteList();
        for (var member : members())
        {
            if (member.type() == MapIdentifier.Type.WAY)
            {
                routes.add(member.route());
            }
        }
        return routes;
    }

    @Override
    public GraphElementAttributes<?> attributes()
    {
        return RelationAttributes.get();
    }

    @Override
    public Rectangle bounds()
    {
        var builder = new BoundingBoxBuilder();
        for (var element : elements())
        {
            builder.add(element.bounds());
        }
        return builder.build();
    }

    public Type classify()
    {
        if (graph().isOsm())
        {
            return isTurnRestriction(allTurnRestrictions) ? Type.TURN_RESTRICTION : Type.ROUTE;
        }
        return unsupported();
    }

    public EdgeSet edgeSet()
    {
        return EdgeSet.forCollection(Maximum.MAXIMUM, edges());
    }

    public ObjectList<Edge> edges()
    {
        var edges = new ObjectList<Edge>();
        for (var route : asRoutes())
        {
            edges.addAll(route.asList());
        }
        return edges;
    }

    public List<GraphElement> elements()
    {
        List<GraphElement> elements = new ArrayList<>();
        for (var member : members())
        {
            if (member.isWay())
            {
                var route = member.route();
                if (route != null)
                {
                    for (var edge : route)
                    {
                        elements.add(edge);
                    }
                }
            }
            else
            {
                var element = member.element();
                if (element != null)
                {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    public Edge firstEdge()
    {
        var iterator = edges().iterator();
        if (iterator.hasNext())
        {
            return iterator.next();
        }
        return null;
    }

    public boolean hasNetworkOrRefTag()
    {
        return !Strings.isEmpty(tagValue("network")) || !Strings.isEmpty(tagValue("ref"));
    }

    @Override
    public RelationIdentifier identifier()
    {
        return new RelationIdentifier(identifierAsLong());
    }

    public boolean is(Type type)
    {
        return type() == type;
    }

    public boolean isAdasNode()
    {
        return is(Type.UNIDB_ADAS_NODE);
    }

    @Override
    public boolean isInside(Rectangle bounds)
    {
        return bounds.contains(bounds());
    }

    public boolean isMoreImportantThan(EdgeRelation that)
    {
        return edgeSet().mostImportant().isMoreImportantThan(that.edgeSet().mostImportant());
    }

    public boolean isNoTurnRestriction()
    {
        return isTurnRestriction(noTurnRestrictions);
    }

    public boolean isOnlyTurnRestriction()
    {
        return isTurnRestriction(onlyTurnRestrictions);
    }

    public boolean isRestriction()
    {
        if (isOsm())
        {
            return isTurnRestriction();
        }
        if (isUniDb())
        {
            return Type.RESTRICTIONS.contains(type());
        }
        return false;
    }

    public boolean isTurnRestriction()
    {
        return type() == Type.TURN_RESTRICTION;
    }

    /**
     * Some turn restrictions are valid in OSM but NOT valid in the graph API because the Graph API's data structure is
     * a directed graph. We use this method to filter out bad turn restrictions that are actually okay in OSM.
     *
     * @return True if the restriction relation is valid in OSM
     */
    public boolean isValidOsmTurnRestriction()
    {
        // Go through all edges in the restriction
        Edge previous = null;
        for (var edge : edges())
        {
            // If the edge is not connected to the previous edge in any way,
            if (previous != null && !previous.isConnectedTo(edge))
            {
                // then it's really not valid in OSM
                return false;
            }

            // save current edge as previous
            previous = edge;
        }

        // There was some kind of connection between all the edges. This means that while the
        // restriction was not possible in the graph API due to edge direction it is possible
        // in OSM where ways are undirected.
        return true;
    }

    public Distance length()
    {
        return edgeSet().length();
    }

    public Location location()
    {
        var first = firstEdge();
        if (first != null)
        {
            return first.toLocation();
        }
        return null;
    }

    @Override
    public MapIdentifier mapIdentifier()
    {
        return new RelationIdentifier(identifier().asLong());
    }

    public EdgeRelationMember memberInRole(String role)
    {
        for (var member : members())
        {
            if (role.equalsIgnoreCase(member.role()))
            {
                return member;
            }
        }
        return null;
    }

    @KivaKitIncludeProperty
    public List<EdgeRelationMember> members()
    {
        return store().retrieveMembers(this);
    }

    public List<PbfWayIdentifier> pbfWayIdentifiers()
    {
        List<PbfWayIdentifier> identifiers = new ArrayList<>();
        for (var member : members())
        {
            if (member.isWay())
            {
                identifiers.add(member.route().first().wayIdentifier());
            }
        }
        return identifiers;
    }

    /**
     * @return The properties of this element from its {@link DataSpecification},
     * @see GraphElementPropertySet
     * @see Stringable
     * @see AsIndentedString
     */
    @Override
    public GraphElementPropertySet<? extends GraphElement> properties()
    {
        return dataSpecification().relationProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "[EdgeRelation identifier = " + identifier() + ", pbfIdentifier = " + mapIdentifier() + ", members = "
                + members().size() + "]";
    }

    public TurnRestriction turnRestriction()
    {
        // Get from, to and via members
        var fromMember = memberInRole("from");
        var toMember = memberInRole("to");
        var viaMember = memberInRole("via");

        var from = fromMember != null ? fromMember.route() : null;
        var to = toMember != null ? toMember.route() : null;

        // If we have all three members
        if (from != null && to != null && viaMember != null)
        {
            // and the via member is a node
            if (viaMember.isNode())
            {
                // then return the turn restriction route from the "from" edge, through the
                // via vertex to the "to" edge
                return turnRestrictionRouteViaNode(from, viaMember.vertex(), to);
            }

            // and if the via member is a way
            if (viaMember.isWay())
            {
                // then return the turn restriction route from the "from" edge, through the
                // via route to the "to" edge
                return turnRestrictionRouteViaRoute(from, viaMember.route(), to);
            }
        }

        // Return what we have as a bad restriction
        return new TurnRestriction(this, from, null, to);
    }

    public TurnRestriction.Type turnRestrictionType()
    {
        return TurnRestriction.Type.forEdgeRelation(this);
    }

    public Type type()
    {
        return store().retrieveType(this);
    }

    public Location viaNodeLocation()
    {
        return store().retrieveViaNodeLocation(this);
    }

    @Override
    protected RelationStore store()
    {
        return subgraph().relationStore();
    }

    private CommonEntityData commonEntityData()
    {
        return new CommonEntityData(identifierAsLong(),
                pbfRevisionNumber().asInt(),
                new Timestamp(lastModificationTime().asMilliseconds()),
                new OsmUser(pbfUserIdentifier().asInt(), pbfUserName().name()),
                pbfChangeSetIdentifier().asLong(),
                tagList().asList());
    }

    private boolean isTurnRestriction(String[] restrictionTypes)
    {
        if ("restriction".equalsIgnoreCase(tagValue("type")))
        {
            var restriction = tagValue("restriction");
            if (restriction != null)
            {
                for (var restrictionType : restrictionTypes)
                {
                    if (restrictionType.equalsIgnoreCase(restriction))
                    {
                        return true;
                    }
                }
            }
            var conditionalRestriction = tagValue("restriction:conditional");
            return conditionalRestriction != null;
        }
        return false;
    }

    private TurnRestriction turnRestrictionRouteViaNode(Route from, Vertex via, Route to)
    {
        // If the "from" route's start is the via vertex, then it's backwards
        from = from.start().equals(via) ? from.reversed() : from;

        // If the "to" route's end is the via vertex, then it's backwards
        to = to.end().equals(via) ? to.reversed() : to;

        // Reversing a route can result in a null value if the route has a one-way edge
        if (from != null && to != null)
        {
            // In OSM, it is not valid for a via node to be in the middle of a way. It has to be at
            // one end or the other of the way or it would be ambiguous. However, in TXD OSM,
            // edges are sectioned and can be positive or negative and so it is possible for the
            // "from" and/or "to" route to be joined at a vertex in the middle of either or both
            // routes.

            for (var edge : from)
            {
                if (edge.to().equals(via))
                {
                    from = from.upTo(edge);
                    break;
                }
            }

            for (var edge : to)
            {
                if (edge.from().equals(via))
                {
                    to = to.startingAt(edge);
                    break;
                }
            }
        }
        return new TurnRestriction(this, from, null, to);
    }

    private TurnRestriction turnRestrictionRouteViaRoute(Route from, Route via, Route to)
    {
        // If there is a via route
        if (via != null)
        {
            // and the end of the via route is connected to either end of the "from" route
            if (via.end().equals(from.start()) || via.end().equals(from.end()))
            {
                // then the via route is backwards, so we flip it around
                via = via.reversed();
            }

            // If there's a via route
            if (via != null)
            {
                // If the "from" route's start is the start of the via route, then it's backwards
                from = from.start().equals(via.start()) ? from.reversed() : from;

                // If the "to" route's end is the via vertex, then it's backwards
                to = to.end().equals(via.end()) ? to.reversed() : to;
            }
        }

        return new TurnRestriction(this, from, via, to);
    }
}
