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

package com.telenav.tdk.graph.specifications.unidb.graph.edge.model;

import com.telenav.tdk.core.kernel.language.object.Lazy;
import com.telenav.tdk.graph.Edge;
import com.telenav.tdk.graph.specifications.common.edge.EdgeProperties;
import com.telenav.tdk.graph.specifications.library.attributes.Attribute;

public class UniDbEdgeProperties extends EdgeProperties
{
    private static final Lazy<UniDbEdgeProperties> singleton = new Lazy<>(UniDbEdgeProperties::new);

    public static UniDbEdgeProperties get()
    {
        return singleton.get();
    }

    public abstract class UniDbEdgeProperty extends EdgeProperty
    {
        protected UniDbEdgeProperty(final String name, final Attribute<?> attribute)
        {
            super(name, attribute);
            add(this);
        }
    }

    public final UniDbEdgeProperty ACCESS_TYPE = new UniDbEdgeProperty("access-type", UniDbEdgeAttributes.get().ACCESS_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbAccessType();
        }
    };

    public final UniDbEdgeProperty ADAS_Z_COORDINATES = new UniDbEdgeProperty("adas-z-coordinates", UniDbEdgeAttributes.get().ADAS_Z_COORDINATES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbAdasZCoordinates();
        }
    };

    public final UniDbEdgeProperty BACKWARD_LANE_COUNT = new UniDbEdgeProperty("backward-lane-count", UniDbEdgeAttributes.get().REVERSE_LANE_COUNT)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbReverseLaneCount();
        }
    };

    public final UniDbEdgeProperty COMPLEX_INTERSECTION = new UniDbEdgeProperty("complex-intersection", UniDbEdgeAttributes.get().IS_COMPLEX_INTERSECTION)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbIsComplexIntersection();
        }
    };

    public final UniDbEdgeProperty CURVATURES = new UniDbEdgeProperty("curvatures", UniDbEdgeAttributes.get().CURVATURES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbCurvatures();
        }
    };

    public final UniDbEdgeProperty DIVIDED_ROAD = new UniDbEdgeProperty("divided-road", UniDbEdgeAttributes.get().IS_DIVIDED_ROAD)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbIsDividedRoad();
        }
    };

    public final UniDbEdgeProperty FORM_OF_WAY = new UniDbEdgeProperty("form-of-way", UniDbEdgeAttributes.get().FORM_OF_WAY)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbFormOfWay();
        }
    };

    public final UniDbEdgeProperty FORWARD_LANE_COUNT = new UniDbEdgeProperty("forward-lane-count", UniDbEdgeAttributes.get().FORWARD_LANE_COUNT)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbForwardLaneCount();
        }
    };

    public final UniDbEdgeProperty HEADINGS = new UniDbEdgeProperty("headings", UniDbEdgeAttributes.get().HEADINGS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbHeadings();
        }
    };

    public final UniDbEdgeProperty BUILD_UP_AREA = new UniDbEdgeProperty("buildup-area", UniDbEdgeAttributes.get().IS_BUILD_UP_AREA)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbIsBuildUpArea();
        }
    };

    public final UniDbEdgeProperty HIGHWAY_TYPE = new UniDbEdgeProperty("highway-type", UniDbEdgeAttributes.get().HIGHWAY_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbHighwayType();
        }
    };

    public final UniDbEdgeProperty IS_BACKWARD_ONE_WAY = new UniDbEdgeProperty("backward-one-way", UniDbEdgeAttributes.get().IS_REVERSE_ONE_WAY)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbIsReverseOneWay();
        }
    };

    public final UniDbEdgeProperty TURN_LANE_ARROWS = new UniDbEdgeProperty("turn-lane-arrows", UniDbEdgeAttributes.get().TURN_LANE_ARROWS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbTurnLaneArrows();
        }
    };

    public final UniDbEdgeProperty LANE_DIVIDERS = new UniDbEdgeProperty("lane-dividers", UniDbEdgeAttributes.get().LANE_DIVIDERS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbLaneDividers();
        }
    };

    public final UniDbEdgeProperty LANE_ONEWAYS = new UniDbEdgeProperty("lane-one-ways", UniDbEdgeAttributes.get().LANE_ONE_WAYS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbLaneOneWays();
        }
    };

    public final UniDbEdgeProperty LANE_TYPES = new UniDbEdgeProperty("lane-types", UniDbEdgeAttributes.get().LANE_TYPES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbLaneTypes();
        }
    };

    public final UniDbEdgeProperty LEFT_SIDE_DRIVING = new UniDbEdgeProperty("left-side-driving", UniDbEdgeAttributes.get().IS_LEFT_SIDE_DRIVING)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbIsLeftSideDriving();
        }
    };

    public final UniDbEdgeProperty OVERPASS_UNDERPASS = new UniDbEdgeProperty("overpass-underpass", UniDbEdgeAttributes.get().OVERPASS_UNDERPASS)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbOverpassUnderpass();
        }
    };

    public final UniDbEdgeProperty REGION_CODE = new UniDbEdgeProperty("adas-region-code", UniDbEdgeAttributes.get().FORWARD_REGION_CODE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbAdasRegionCode();
        }
    };

    public final UniDbEdgeProperty ROUTE_TYPE = new UniDbEdgeProperty("route-type", UniDbEdgeAttributes.get().ROUTE_TYPE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbRouteType();
        }
    };

    public final UniDbEdgeProperty SLOPES = new UniDbEdgeProperty("slopes", UniDbEdgeAttributes.get().SLOPES)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbSlopes();
        }
    };

    public final UniDbEdgeProperty SPEED_LIMIT_SOURCE = new UniDbEdgeProperty("speed-limit-source", UniDbEdgeAttributes.get().SPEED_LIMIT_SOURCE)
    {
        @Override
        public Object value(final Edge edge)
        {
            return edge.uniDbSpeedLimitSource();
        }
    };

    protected UniDbEdgeProperties()
    {
    }
}
