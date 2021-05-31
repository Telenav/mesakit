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

package com.telenav.mesakit.graph.specifications.common.edge;

import com.telenav.kivakit.kernel.language.object.*;
import com.telenav.kivakit.kernel.language.string.*;
import com.telenav.kivakit.map.road.model.RoadName;
import com.telenav.mesakit.graph.Edge;
import com.telenav.mesakit.graph.specifications.common.element.GraphElementProperties;
import com.telenav.mesakit.graph.specifications.library.attributes.Attribute;
import com.telenav.mesakit.graph.specifications.osm.graph.edge.model.attributes.OsmEdgeAttributes;

public class EdgeProperties extends GraphElementProperties<Edge>
{
    private static final Lazy<EdgeProperties> singleton = Lazy.of(EdgeProperties::new);

    public static EdgeProperties get()
    {
        return EdgeProperties.singleton.get();
    }

    public abstract class EdgeProperty extends GraphElementProperty
    {
        protected EdgeProperty(final String name, final Attribute<?> attribute)
        {
            super(name, attribute);
            add(this);
        }
    }

    public final EdgeProperty ALTERNATE_ROAD_NAME = new EdgeProperty("alternate-road-name", EdgeAttributes.get().ROAD_NAMES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadName(RoadName.Type.ALTERNATE);
        }
    };

    public final EdgeProperty BRIDGE_TYPE = new EdgeProperty("bridge-type", EdgeAttributes.get().BRIDGE_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.bridgeType();
        }
    };

    public final EdgeProperty COUNTRY = new EdgeProperty("country", EdgeAttributes.get().COUNTRY)
    {
        @Override
        public Object value(final Edge edge)
        {
            final var country = edge.country();
            if (country != null)
            {
                return country.identity().tdk().first();
            }
            return null;
        }
    };

    public final EdgeProperty FREE_FLOW_SPEED = new EdgeProperty("free-flow-speed", EdgeAttributes.get().FREE_FLOW_SPEED_CATEGORY)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.freeFlowSpeed();
        }
    };

    public final EdgeProperty FROM = new EdgeProperty("from", EdgeAttributes.get().FROM_VERTEX_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.from() == null ? null : edge.from().identifier();
        }
    };

    public final EdgeProperty FROM_IDENTIFIER = new EdgeProperty("from-identifier", EdgeAttributes.get().FROM_VERTEX_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.fromVertexIdentifier();
        }
    };

    public final EdgeProperty HEADING = new EdgeProperty("heading", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.heading();
        }
    };

    public final EdgeProperty HOV_LANE_COUNT = new EdgeProperty("hov-lane-count", EdgeAttributes.get().HOV_LANE_COUNT)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.hovLaneCount();
        }
    };

    public final EdgeProperty IS_CLOSED_TO_THROUGH_TRAFFIC = new EdgeProperty("closed", EdgeAttributes.get().IS_CLOSED_TO_THROUGH_TRAFFIC)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.isClosedToThroughTraffic();
        }
    };

    public final EdgeProperty IS_JUNCTION = new EdgeProperty("is-junction", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.isJunctionEdge();
        }
    };

    public final EdgeProperty IS_RAMP = new EdgeProperty("is-ramp", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.isRamp();
        }
    };

    public final EdgeProperty IS_SOFT_CUT = new EdgeProperty("is-soft-cut", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.tagList() == null ? null : edge.isSoftCut();
        }
    };

    public final EdgeProperty IS_TOLL_ROAD = new EdgeProperty("is-toll-road", EdgeAttributes.get().IS_TOLL_ROAD)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.isTollRoad();
        }
    };

    public final EdgeProperty IS_UNDER_CONSTRUCTION = new EdgeProperty("is-under-construction", EdgeAttributes.get().IS_UNDER_CONSTRUCTION)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.isUnderConstruction();
        }
    };

    public final EdgeProperty LANE_COUNT = new EdgeProperty("lanes", EdgeAttributes.get().LANE_COUNT)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.laneCount();
        }
    };

    public final EdgeProperty LENGTH = new EdgeProperty("length", EdgeAttributes.get().LENGTH)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.length();
        }
    };

    public final EdgeProperty METROPOLITAN_AREA = new EdgeProperty("metropolitan-area", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return Maybe.apply(edge.metropolitanArea(), metro -> metro.identity().tdk().third());
        }
    };

    public final EdgeProperty OFFICIAL_ROAD_NAME = new EdgeProperty("official-road-name", EdgeAttributes.get().ROAD_NAMES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadName(RoadName.Type.OFFICIAL);
        }
    };

    public final EdgeProperty MAP_EDGE_IDENTIFIER = new EdgeProperty("map-edge-identifier", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            if (edge.supportsNodeIdentifiers())
            {
                return edge.mapEdgeIdentifier();
            }
            return null;
        }
    };

    public final EdgeProperty FROM_NODE_IDENTIFIER = new EdgeProperty("from-node", EdgeAttributes.get().FROM_NODE_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.fromNodeIdentifier();
        }
    };

    public final EdgeProperty TO_NODE_IDENTIFIER = new EdgeProperty("to-node", EdgeAttributes.get().TO_NODE_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.toNodeIdentifier();
        }
    };

    public final EdgeProperty RELATIONS = new EdgeProperty("relations", EdgeAttributes.get().RELATIONS)
    {
        @Override
        public Object value(final Edge edge)
        {
            final var relations = new StringList();
            edge.relations().forEach(relation -> relations.add(Strings.toString(relation.identifier())));
            return relations.join(", ");
        }
    };

    public final EdgeProperty ROAD_FUNCTIONAL_CLASS = new EdgeProperty("road-functional-class", EdgeAttributes.get().ROAD_FUNCTIONAL_CLASS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadFunctionalClass();
        }
    };

    public final EdgeProperty ROAD_NAMES = new EdgeProperty("road-names", EdgeAttributes.get().ROAD_NAMES)
    {
        @Override
        public Object value(final Edge edge)
        {
            final var names = new StringList();
            for (final var type : RoadName.Type.values())
            {
                for (final var name : edge.roadNames(type))
                {
                    names.add(type + ": " + name);
                }
            }
            return names;
        }
    };

    public final EdgeProperty ROAD_SHAPE = new EdgeProperty("road-shape", EdgeAttributes.get().ROAD_SHAPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadShape();
        }
    };

    public final EdgeProperty ROAD_STATE = new EdgeProperty("road-state", EdgeAttributes.get().ROAD_STATE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadState();
        }
    };

    public final EdgeProperty ROAD_STATE_TYPE_SUBTYPE_AND_FUNCTIONAL_CLASS = new EdgeProperty("state", EdgeAttributes.get().ROAD_STATE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadState() + " / " + edge.roadType() + " / " + edge.roadSubType() + " / "
                    + edge.roadFunctionalClass();
        }
    };

    public final EdgeProperty ROAD_SUBTYPE = new EdgeProperty("road-sub-type", EdgeAttributes.get().ROAD_SUB_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadSubType();
        }
    };

    public final EdgeProperty ROAD_SURFACE = new EdgeProperty("surface", EdgeAttributes.get().ROAD_SURFACE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadSurface();
        }
    };

    public final EdgeProperty ROAD_TYPE = new EdgeProperty("road-type", EdgeAttributes.get().ROAD_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadType();
        }
    };

    public final EdgeProperty ROUTE_ROAD_NAME = new EdgeProperty("route-road-name", EdgeAttributes.get().NONE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.roadName(RoadName.Type.ROUTE);
        }
    };

    public final EdgeProperty SPEED_LIMIT = new EdgeProperty("speed-limit", EdgeAttributes.get().SPEED_LIMIT)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.speedLimit();
        }
    };

    public final EdgeProperty TO = new EdgeProperty("to", EdgeAttributes.get().TO_VERTEX_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.to() == null ? null : edge.to().identifier();
        }
    };

    public final EdgeProperty TO_IDENTIFIER = new EdgeProperty("to-identifier", EdgeAttributes.get().TO_VERTEX_IDENTIFIER)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.toVertexIdentifier();
        }
    };

    public final EdgeProperty TMC_IDENTIFIERS = new EdgeProperty("tmcs", EdgeAttributes.get().FORWARD_TMC_IDENTIFIERS)
    {
        @Override
        public Object value(final Edge edge)
        {
            if (edge.supports(OsmEdgeAttributes.get().FORWARD_TMC_IDENTIFIERS))
            {
                final var identifiers = new StringList();
                for (final var identifier : edge.tmcIdentifiers())
                {
                    identifiers.add("$ ($)", identifier, identifier.asCode());
                }
                return identifiers;
            }
            return null;
        }
    };

    protected EdgeProperties()
    {
    }
}
